#ifndef LED_H
#define LED_H

#include "esp_system.h"

//Initializes the LED at the provided LED pin to change state when a state associated with the LED event handle changes
//Note that this is currently only meant to work for one LED pin
void startLED(int providedLEDPin, EventGroupHandle_t providedLEDEvents);

#endif