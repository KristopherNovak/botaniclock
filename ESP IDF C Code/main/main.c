#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "driver/gpio.h"
#include <inttypes.h>
#include "nvs.h"
#include "nvs_flash.h"
#include "esp_timer.h"
#include "esp_sleep.h"
#include "driver/rtc_io.h"
#include "freertos/semphr.h"
#include "freertos/timers.h"
#include "esp_system.h"
#include "wifi.h"
#include "cJSON.h"
#include "esp_http_client.h"
#include "esp_http_server.h"
#include "mdns.h"
#include "esp_vfs.h"
#include "esp_vfs_fat.h"
#include "nvs_parameter.h"
#include "router_mode.h"
#include "client.h"
#include "registration_mode.h"
#include "button.h"
#include "update_mode.h"
#include "led.h"

#define LED_PIN 27
#define BUTTON_PIN 26


//UPDATE_TIMESTAMP_STATE represents update mode 
#define UPDATE_TIMESTAMP_STATE BIT0
//REGISTER_DEVICE_STATE represents registration mode
#define REGISTER_DEVICE_STATE BIT1
//ROUTER_CONNECT_STATE represents router mode
#define ROUTER_CONNECT_STATE BIT2


//Declaring all functions that are below main
void initializeLED();
bool userRequestingRegistrationMode();
void ensureRegistrationModeIsTriggered();
void configureButtonWithDeepSleepCapability();
bool attemptToConnectToWiFi();
void routerMode();
bool attemptUpdateMode();
void registrationMode();
static void gpio_isr_handler(void *args);
void turnOffDevice();


//Used to retrieve router and plant information from NVS
static nvs_handle handle;

//Used to control which mode the LED is in
static EventGroupHandle_t ledEvents;

//used to check if wifi deinitialization is needed when going to sleep
static bool connectedToWifi = false;

//Sempahore to allow for device to go to sleep (in turnOffDevice)
static SemaphoreHandle_t sleepSemaphore;



//Function that controls the main operation of the device
//Essentially, the device has three modes
//Update Mode (normal operation) involves the device notifying the server that a plant has been watered
//and that the corresponding timestamp can be updated
//Registration Mode occurs when the device has not been linked to a plant or is linked to a plant that no longer exists
//Its purpose is to link the device to a plant
//Router Mode occurs when the device does not have router credentials (SSID/Password)
//Its purpose is to allow the device to connect to the router
void app_main(void)
{
    //Set up the button on the device as a GPIO input
    initializeButton(BUTTON_PIN);

    //Turn on LED to indicate the device is on
    initializeLED();

    //Initialize NVS
    ESP_ERROR_CHECK(nvs_flash_init());

    //Open NVS store for router information and plant/account information
    ESP_ERROR_CHECK(nvs_open("store", NVS_READWRITE, &handle));

    //Check if the user is requesting registration mode
    if(userRequestingRegistrationMode()){

        //Ensure that update mode will fail
        ensureRegistrationModeIsTriggered();
    };

    //Change the GPIO pin for the button to cause pressing the button to trigger an interrupt
    //The interrupt is used to put the device in deep sleep
    configureButtonWithDeepSleepCapability();

    //Attempt to connect to the wifi
    connectedToWifi = attemptToConnectToWiFi();

    //If connection fails, then go into Router Mode (device may need to connect to router)
    if(!connectedToWifi){
        routerMode();
    }

    //Attempt to go into update mode to update a plant timestamp
    bool updateSuccessful = attemptUpdateMode();

    //If the plant timestamp could not be updated succesfully, go into registration mode
    if(!updateSuccessful){
        registrationMode();
    }

}


//Starts up LED
void initializeLED(){
    //Create an event group to state of the device
    ledEvents = xEventGroupCreate();

    //Start up the LED at the specified LED_PIN
    startLED(LED_PIN, ledEvents);
}

//Checks if a user is requesting registration mode
bool userRequestingRegistrationMode(){

    //Return true if button pressed longer than 5 seconds
    if(buttonPressedLongerThan(5000000)){
        return true;
    }
    return false;
}

//Ensures that update mode will fail when attemptUpdatedMode is called
void ensureRegistrationModeIsTriggered(){

    //Set the LED to inform the user that registration mode will be triggered
    xEventGroupSetBits(ledEvents, REGISTER_DEVICE_STATE);

    //Clear out the values of registrationID and accountEmail to ensure update mode fails
    nvs_set_str(handle, "registrationID", "");
    nvs_set_str(handle, "accountEmail", "");
    nvs_commit(handle);
}

//Allow the button to be used to put the device to sleep
void configureButtonWithDeepSleepCapability(){

    //Wait for the user to stop pressing the button
    waitUntilButtonNoLongerPressed();
    
    //Slight delay in case user is slow to take finger off button
    vTaskDelay(20/portTICK_PERIOD_MS);

    //Set up semaphore that can be given to put device to sleep
    sleepSemaphore = xSemaphoreCreateBinary();
    addInterruptCapabilityToButton(gpio_isr_handler);
    xTaskCreate(&turnOffDevice, "turnOffDevice", 2048, NULL, 1, NULL);
};

//Attempts to connect the device to WiFi
bool attemptToConnectToWiFi(){
    //Initialize Wifi Driver
    initializeWifiDriver();

    //Acquire SSID and routerPassword from NVS store
    char* ssid = getNVSParameterString("ssid", handle);
    char* routerPassword = getNVSParameterString("routerPassword", handle);

    //Attempt to connect to wifi using the acquired SSID and router password
    connectedToWifi = connectToWifiInStationMode(ssid, routerPassword, 10000);

    //free up ssid and routerPassword back to memory
    if(ssid != NULL) free(ssid);
    if(routerPassword!= NULL) free(routerPassword);

    //Return back whether the attempt was successful (for readability)
    return connectedToWifi;
}

//Mode in which device connects to the router
void routerMode(){
    //Set the LED to indicate to the user that the device is in router mode
    xEventGroupSetBits(ledEvents, ROUTER_CONNECT_STATE);

    //See if this fully necessary
    //disconnectFromWifi();
    //vTaskDelay(10/portTICK_PERIOD_MS);

    //Acquire the SSID and router password
    char** parameterArray = getSSIDAndRouterPassword();
    char* ssid = parameterArray[0];
    char* routerPassword = parameterArray[1];

    //Store the SSID and router password in NVS
    nvs_set_str(handle,"ssid", ssid);
    nvs_set_str(handle,"routerPassword", routerPassword);
    nvs_commit(handle);

    //Free parameter array and corresponding parameters
    if(parameterArray != NULL) free(parameterArray);
    if(ssid != NULL) free(ssid);
    if(routerPassword!= NULL) free(routerPassword);

    //Put the device to sleep
    xSemaphoreGive(sleepSemaphore);

}

//Attempts to notify the server to update a timestamp for a plant
bool attemptUpdateMode(){
    //Acquire plant registration ID
    char* registrationID = getNVSParameterString("registrationID", handle);

    //Acquire account email
    char* accountEmail = getNVSParameterString("accountEmail", handle);

    //Attempt to update the plant timestamp
    bool updateSuccessful = updatePlantTimestamp(registrationID, accountEmail);

    //If update successful, go to sleep
    if(updateSuccessful){
        xSemaphoreGive(sleepSemaphore);
        return true;
    }

    //Otherwise, indicate update mode failed
   return false;
}

//Links the device to a particular plant
void registrationMode(){

    //Set the LED to indicate to the user that the device is in registration mode
    xEventGroupSetBits(ledEvents, REGISTER_DEVICE_STATE);

    //Acquire account email and plant registration ID
    char** parameterArray = getEmailAndRegistrationID();
    char* accountEmail = parameterArray[0];
    char* registrationID = parameterArray[1];

    //Set plant registration ID and account email in NVS
    nvs_set_str(handle,"registrationID", registrationID);
    nvs_set_str(handle,"accountEmail", accountEmail);
    nvs_commit(handle);

    //Free parameterArray and corresponing parameters
    if(parameterArray != NULL) free(parameterArray);
    if(registrationID != NULL) free(registrationID);
    if(accountEmail != NULL) free(accountEmail);

    //Put the device in deep sleep
    xSemaphoreGive(sleepSemaphore);
    
}

//Interrupt handler triggered by the button to put the device to sleep
static void IRAM_ATTR gpio_isr_handler(void *args){
    xSemaphoreGiveFromISR(sleepSemaphore, NULL);
}



//Puts the device to sleep mode when the sleepSemaphore is given
void turnOffDevice(){
    while(true){
        
        //Waits for an indication to put the device to sleep to be received
        xSemaphoreTake(sleepSemaphore, portMAX_DELAY);

        //Close the NVS store
        nvs_close(handle);

        //If connected to WiFi, then disconnect and shut it down
        if(connectedToWifi){
            disconnectFromWifi();
            shutDownWifi();
        }

        //Set the button to be used to wake up the device
        setUpButtonForWakeUp();

        ESP_LOGI("MAIN", "Device going to sleep\n");

        //Put the device to sleep
        esp_deep_sleep_start();
    }

}

