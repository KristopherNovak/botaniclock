#ifndef BUTTON_H
#define BUTTON_H

//Initializes the button as a GPIO input according to the provided pin (note that only works for one button currently)
void initializeButton(int providedButtonPin);

//Checks if the button is pressed longer than the provided duration (in milliseconds)
//If so, return true. Otherwise, return false
bool buttonPressedLongerThan(int durationInMS);

//Runs until the button is no longer being pressed
void waitUntilButtonNoLongerPressed();

//Sets the button up to be used to wake a device from deep sleep
void setUpButtonForWakeUp();

//Configure the button to trigger the provided interrupt handler
void addInterruptCapabilityToButton(void *gpio_isr_handler);

#endif