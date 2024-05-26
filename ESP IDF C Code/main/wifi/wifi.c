#include <stdio.h>
#include <string.h>
#include "freertos/FreeRTOS.h"
#include "esp_system.h"
#include "esp_wifi.h"
#include "esp_log.h"
#include "esp_event.h"


//Total number of times that a device can attempt to reconnect to WiFi
#define NUMBER_OF_RETRIES 3

//Function declarations
static void setWifiConfigurationForStationMode(char *ssid, char* routerPassword);
static void setWifiConfigurationForAPMode(char *apName, char* apPassword);
static void wifiEventHandler(void *eventHandlerArguments, esp_event_base_t eventBase, int32_t eventCause, void *eventData);
static void attemptToReconnect(void* eventData);


//Tag for ESP_LOG calls
static char *TAG = "WIFI";

//Network Interface variable
static esp_netif_t *espNetIf = NULL;

//Variable that tracks whether the device intended to disconnect or not
//If it intended to disconnect, it will not try to reconnect
//If it did not intend to disconnect, it will try to reconnect up to the maximum number of times
static bool intentionalDisconnect = true;

//Variable that tracks whether or not the device has connected to the WiFi successfully
static bool isConnected = false;

//Semaphore used to wait until the wifi operation successfully passes or fails before leaving the function
static SemaphoreHandle_t wifiSemaphore;

//Variable that tracks how many times the device has failed to connect to WiFi
static int numberOfDisconnections = 0;


//Function to start up NetIF, create the event loop, initialize WiFi according to an initial WiFi configuration,
// and register WIFI events and IP events with the event handler
void initializeWifiDriver(){
  ESP_ERROR_CHECK(esp_netif_init());
  ESP_ERROR_CHECK(esp_event_loop_create_default());
  wifi_init_config_t wifiInitConfig = WIFI_INIT_CONFIG_DEFAULT();
  ESP_ERROR_CHECK(esp_wifi_init(&wifiInitConfig));
  ESP_ERROR_CHECK(esp_event_handler_register(WIFI_EVENT, ESP_EVENT_ANY_ID, wifiEventHandler, NULL));
  ESP_ERROR_CHECK(esp_event_handler_register(IP_EVENT, IP_EVENT_STA_GOT_IP, wifiEventHandler, NULL));
}

//Attempts to connect to WiFi in Station mode
bool connectToWifiInStationMode(char *ssid, char* routerPassword, int connectionAttemptDurationInMS){

    //Update NetIF for WiFi Station
    espNetIf = esp_netif_create_default_wifi_sta();
    
    //Set WiFi mode to Station mode
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_STA));

    //Indicate that the device would should not intentionally disconnect
    intentionalDisconnect = false;

    //Create a semaphore to prevent this function from returning until the device has finished attempting
    //To connect to WiFi
    wifiSemaphore = xSemaphoreCreateBinary();

    //Set WiFi configuration including provided SSID and router/AP password
    setWifiConfigurationForStationMode(ssid, routerPassword);

    //Start WiFi
    ESP_ERROR_CHECK(esp_wifi_start());

    //Wait until either WiFi connection has succeeded (an IP address for the device has been acquired)
    //or has failed (timeout occurs or number of retries is exceeded)
    xSemaphoreTake(wifiSemaphore, pdMS_TO_TICKS(connectionAttemptDurationInMS));

    //Return true if the device is successfully connected, return false otherwise
    if(isConnected){
        return true;
    }

    return false;
}

//Sets WiFi configuration for connecting to the router/AP with the provided SSID and router/AP password
static void setWifiConfigurationForStationMode(char *ssid, char* routerPassword){

    wifi_config_t wifiConfig = {};

    //Add SSId and router password to WiFi configuration
    strncat((char*)wifiConfig.sta.ssid, ssid, strlen(ssid));
    strncat((char*)wifiConfig.sta.password, routerPassword, strlen(routerPassword));

    //Set the configuration
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_STA, &wifiConfig));
}

//Attempts to connect to WiFi as an AP using the provided apName as its SSID and
//the provided apPassword as its password
void connectToWifiInAPMode(char* apName, char* apPassword){

    //Updates NetIF for Access Point (AP)
    espNetIf = esp_netif_create_default_wifi_ap();

    //Set WiFi mode to AP mode
    ESP_ERROR_CHECK(esp_wifi_set_mode(WIFI_MODE_AP));

    //Set the WiFi configuration for the device
    setWifiConfigurationForAPMode(apName, apPassword);

    //Start WiFi
    ESP_ERROR_CHECK(esp_wifi_start());
}

//Sets the WiFi configuration for the device operating as an Access Point (AP)
//Specifcally, sets SSID for AP to aqpName and connection password to apPassword
static void setWifiConfigurationForAPMode(char *apName, char* apPassword){

    //Initialize WiFi configuration
    wifi_config_t wifiConfig = {};

    //Add SSID and password to AP
    strncat((char*)wifiConfig.ap.ssid, apName, strlen(apName));
    strncat((char*)wifiConfig.ap.password, apPassword, strlen(apPassword));

    //Set up additional WiFi configuration parameters
    wifiConfig.ap.authmode = WIFI_AUTH_WPA_WPA2_PSK;
    wifiConfig.ap.max_connection = 1;
    wifiConfig.ap.beacon_interval = 100;
    wifiConfig.ap.channel = 1;

    //Set the WiFi configuration
    ESP_ERROR_CHECK(esp_wifi_set_config(WIFI_IF_AP, &wifiConfig));

}

//Function to disconnect from WiFi (note that this DOES NOT destroy the NetIF variable)
void disconnectFromWifi(){

    //Indicate that disconnecting from WiFi is intentional
    intentionalDisconnect = true;

    //Disconnect from WiFi
    esp_wifi_stop();

    //Indicate that the device is no longer connected to WiFi
    isConnected = false;
}

//Destroys NetIF, generally intended if WiFi capability os no longer needed
void shutDownWifi(){
    esp_netif_destroy(espNetIf);
}

//Event handler triggered by operations associated with connecting to WiFi
static void wifiEventHandler(void *eventHandlerArguments, esp_event_base_t eventBase, int32_t eventCause, void *eventData){

    //If the device starts up WiFi, then attempt to connect to the router/AP
    if(eventCause == WIFI_EVENT_STA_START){

        ESP_LOGI(TAG, "Station starting up");

        esp_wifi_connect();

        return;
    }

    //If the device connects to the router/AP successfully, reset the number of disconnections
    if(eventCause == WIFI_EVENT_STA_CONNECTED){

            ESP_LOGI(TAG, "Station connected to AP");

            numberOfDisconnections = 0;
            return;
    }

    //If the device is assigned an IP (after a successful connection), then finish the WiFi procedure
    if(eventCause ==IP_EVENT_STA_GOT_IP){
            
            ESP_LOGI(TAG, "Station has gotten IP");

            //Indicate that device has connected to WiFi
            isConnected = true;

            //Give semaphore to finish the WiFi procedure
            xSemaphoreGive(wifiSemaphore);

            return;
    }

    //If the device fails to connect to the router/AP, attempt to reconnect up to NUMBER_OF_RETRIES time
    if(eventCause == WIFI_EVENT_STA_DISCONNECTED){

        ESP_LOGI(TAG, "Station disconnected from AP");

        attemptToReconnect(eventData);
    }
}


//Attempts to reconnect to the router if a disconnection is not intentional and depending
//on whether one or more reasons for disconnection occur (more details below)
static void attemptToReconnect(void* eventData){
    //Don't attempt to reconnect if purposely attempting to disconnect
    if(intentionalDisconnect){
        return;
    }

    //Don't attempt to reconnect if the disconnect reason involves:
    //Not being able to locate an AP
    //Being moved to another AP
    //Or a previous authentication is no longer valid
    wifi_event_sta_disconnected_t *wifi_event_sta_disconnected = eventData;
    uint8_t disconnectReason = wifi_event_sta_disconnected->reason;
    if( disconnectReason != WIFI_REASON_NO_AP_FOUND &&
        disconnectReason != WIFI_REASON_ASSOC_LEAVE &&
        disconnectReason != WIFI_REASON_AUTH_EXPIRE){
        return;
    }

    //Continue attempting to connect as long as the number of retries have not been exceeded
    numberOfDisconnections++;
    if (numberOfDisconnections <= NUMBER_OF_RETRIES){
        vTaskDelay(pdMS_TO_TICKS(2500));
        esp_wifi_connect();
        return;
    }

    ESP_LOGE(TAG, "Number of retries for attempting to connect to wifi has been exceed");

    //Indicate that the device is not connected
    isConnected = false;

    //Give semaphore to finish WiFi procedure
    xSemaphoreGive(wifiSemaphore);
}