
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

#define PRESSED 1
#define NOT_PRESSED 0

static int buttonPin = -1;

//Initializes the button as a GPIO input according to the provided pin (note that only works for one button currently)
//TODO: Modify code to accomodate multiple buttons (using multiple button-dedicated tasks?)
void initializeButton(int providedButtonPin){

    //Set the variable to the provided button pin
    buttonPin = providedButtonPin;

    //Deactivate the ability of the button to wake the device
    rtc_gpio_deinit(buttonPin);
    
    //Set up the GPIO pin for the button
    gpio_set_direction(buttonPin, GPIO_MODE_INPUT);
    gpio_pullup_dis(buttonPin);
    gpio_pulldown_en(buttonPin);
}

//Checks if the button is pressed longer than the provided duration (in milliseconds)
//If so, return true. Otherwise, return false
bool buttonPressedLongerThan(int durationInMS){

    //Return false if the button pin is not yet configured
    if(buttonPin == -1){
        return false;
    }

    uint64_t referenceTime = esp_timer_get_time();
    uint64_t actualTime = esp_timer_get_time();
    int32_t isPressed = gpio_get_level(buttonPin);

    //Track if the button is being pressed at least for the provided duration
    while(((actualTime - referenceTime) < durationInMS) && (isPressed == PRESSED)){
        vTaskDelay(100/portTICK_PERIOD_MS);
        actualTime = esp_timer_get_time();
        isPressed = gpio_get_level(buttonPin);
        printf("%" PRIu64 " seconds \n", actualTime);
    }
    
    //If the button was indeed held for at least the provided duration, return true
    //Otherwise, return false
    if(actualTime >= durationInMS){
        return true;
    }
    return false;
}

//Runs until the button is no longer being pressed
void waitUntilButtonNoLongerPressed(){
    //Returns if the button pin is not configured
    if(buttonPin == -1){
        return;
    }

    int32_t isPressed = gpio_get_level(buttonPin);

    //While the button is being pressed, stay in this loop
    //TODO: could probably use a semaphore to reduce spinning
    while(isPressed == PRESSED){
        vTaskDelay(100/portTICK_PERIOD_MS);
        isPressed = gpio_get_level(buttonPin);
        printf("Button still being pressed\n");

    }
}

//Sets the button up to be used to wake a device from deep sleep
void setUpButtonForWakeUp(){

    //Return if the button pin has not been configured yet
    if(buttonPin == -1){
        return;
    }

    //Configure the button to wake device from deep sleep
    rtc_gpio_pullup_dis(buttonPin);
    rtc_gpio_pulldown_en(buttonPin);
    esp_sleep_enable_ext0_wakeup(buttonPin,1);
}

//Configure the button to trigger the provided interrupt handler
void addInterruptCapabilityToButton(void *gpio_isr_handler){

    //If button pin not yet configured, return -1
    if(buttonPin == -1){
        return;
    }

    //Configure the button to trigger the interrupt handler
    gpio_set_intr_type(buttonPin, GPIO_INTR_NEGEDGE);
    gpio_install_isr_service(0);
    gpio_isr_handler_add(buttonPin, gpio_isr_handler, NULL);
}
