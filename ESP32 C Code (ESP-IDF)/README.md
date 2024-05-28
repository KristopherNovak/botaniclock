| Supported Targets | ESP32 | ESP32-C2 | ESP32-C3 | ESP32-C6 | ESP32-H2 | ESP32-P4 | ESP32-S2 | ESP32-S3 |
| ----------------- | ----- | -------- | -------- | -------- | -------- | -------- | -------- | -------- |

# ESP32 C Code (ESP-IDF)

Within this repository is the C code for device used in this project (the ESP32).

## What does this code do?
At a high level, the device has three states. The first state is referred to as "update mode" and it involves sending a notification to the Spring server in order for the server to update a timestamp for a plant. The second state is referred to as "register mode" and it involves the device attempting to get a valid account email and plant registration ID from a user. The third state is referred to as "router mode" and it involves the device attempting to get a router SSID and password from a user.

The code written here navigates across these three states. The starting point for this code is the main.c file and it essentially describes initialization and contains the logic for how to navigate between the above three states.

## What does each module/sub-folder do?

The button module is responsible for controlling the operation of the button coupled to the device.

The client module is responsible for sending HTTP requests as a client. It is necessary for registration mode and update mode.

The led module is response for controlling the operation of the LED coupled to the device.

The NVS parameter module is response for retrieving parameters from non-volatile memory storage (in this case, router SSID/password, account email, and plant registration ID).

The registration mode module includes the logic for what the device does in registration mode (namely, acquiring the account email and plant registration ID).

The router mode module includes the logic for what the device does in router mode (namely, acquiring the router SSID and password).

The server module is responsible for hosting a server to which HTTP requests can be received and processed. It is necessary for registration mode and router mode.

The update mode module includes the logic for what the device does in update mode (sending a notification to the server to update a plant timestamp).

The wifi module is response for connecting and disconnecting from WiFi.

## Dependencies

As an important note, the operation of this device depends on the MDNS library, which may need to be added separately.
