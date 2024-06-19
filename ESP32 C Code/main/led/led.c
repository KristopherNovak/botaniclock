#include "esp_system.h"
#include "driver/gpio.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_log.h"

//Function Declarations
static void flashLED();
static void noFlash();
static void singleFlash();
static void doubleFlash();

//Defining LED states
//NO_FLASH means the LED stays with the light on
//SINGLE_FLASH means the LED flashes once per period
//DOUBLE_FLASH means the LED flashes twice per period
#define NO_FLASH BIT0
#define SINGLE_FLASH BIT1
#define DOUBLE_FLASH BIT2

//Event group handle used to determine when to switch between one LED state to another
static EventGroupHandle_t ledEvents;

//The pin number of the LED
static int ledPin;

//The tag used when calling ESP_LOG
static char* TAG;

//Initializes the LED at the provided LED pin to change state when a state associated with the LED event handle changes
//Note that this is currently only meant to work for one LED pin
//TODO: Make this work for any number of LEDs by passing LEDPin and LEDEvent to handle locally
void startLED(int providedLEDPin, EventGroupHandle_t providedLEDEvents){

    //Set global variables
    ledPin = providedLEDPin;
    ledEvents = providedLEDEvents;

    //Set LED pin to act as an output
    gpio_set_direction(ledPin, GPIO_MODE_OUTPUT);

    //Initialize the flash LED task
    xTaskCreate(&flashLED, "flashLED", 2048, NULL, 1, NULL);

    //Notify user that the LED is turned on
    ESP_LOGI(TAG, "LED initialized\n");

}

//Task keeping track of LED state to determine which mode the LED should operate in (no flash, single flash, double flash)
static void flashLED(){

    //Initially assume that the LED should be in no flash mode
    TaskHandle_t ledHandle;
    xTaskCreate(&noFlash, "noFlash", 1024, NULL, 1, &ledHandle);

    EventBits_t result;

    while(true){
        //Wait until a new LED state is indicated
        result = xEventGroupWaitBits(ledEvents, NO_FLASH | SINGLE_FLASH | DOUBLE_FLASH, true, false, portMAX_DELAY);

        //Delete the task associated with the old state
        vTaskDelete(ledHandle);

        //Create a task associated with the new state
        switch(result){
            case(NO_FLASH):
                ESP_LOGI(TAG, "LED switching to no flash mode\n");
                xTaskCreate(&noFlash, "noFlash", 1024, NULL, 1, &ledHandle);
                break;
            case(SINGLE_FLASH):
                ESP_LOGI(TAG, "LED switching to single flash mode\n");
                xTaskCreate(&singleFlash, "singleFlash", 1024, NULL, 1, &ledHandle);
                break;
            case(DOUBLE_FLASH):
                ESP_LOGI(TAG, "LED switching to double flash mode\n");
                xTaskCreate(&doubleFlash, "doubleFlash", 1024, NULL, 1, &ledHandle);
                break;
        }
    }
}

//Mode in which the LED stays on constantly
static void noFlash(){

    int level = 1;
    gpio_set_level(ledPin,level);
    while(true){
        vTaskDelay(100/portTICK_PERIOD_MS);
    }

}

//Mode in which the LED flashes a single time per period
static void singleFlash(){

    int level = 1;

    while(true){
        level = !level;
        gpio_set_level(ledPin,level);
        vTaskDelay(500/portTICK_PERIOD_MS);
    }

}

//Mode in which the LED flashes multiple times per period
static void doubleFlash(){

    while(true){
        gpio_set_level(ledPin,1);
        vTaskDelay(100/portTICK_PERIOD_MS);
        gpio_set_level(ledPin,0);
        vTaskDelay(100/portTICK_PERIOD_MS);
        gpio_set_level(ledPin,1);
        vTaskDelay(100/portTICK_PERIOD_MS);
        gpio_set_level(ledPin,0);
        vTaskDelay(700/portTICK_PERIOD_MS);
    }

}

