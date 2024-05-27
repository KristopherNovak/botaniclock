#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "wifi.h"
#include "cJSON.h"
#include "esp_http_server.h"
#include "mdns.h"
#include "esp_vfs.h"
#include "esp_vfs_fat.h"
#include "client.h"

//Notifies the BotaniClock server that the plant timestamp needs to be updated
//Only returns true if the provided registrationID and accountEmail are valid
bool updatePlantTimestamp(char* registrationID, char* accountEmail){

    //Create a payload body that includes the registration ID and account email
    char *payload_body = createDeviceBodyJSON(registrationID, accountEmail);

    //BotaniClock endpoint to send request to
    char *url = "https://192.168.1.153:8080/api/v1/devices";

    //Send the payload body to the endpoint
    int statusCode = httpRequestSend(payload_body, url, "PUT");

    //Free up dynamically allocated parameters
    if(payload_body != NULL) free(payload_body);
    if(registrationID != NULL) free(registrationID);
    if(accountEmail != NULL) free(accountEmail);

    //Indicate that response was successful, meaning that the provided registration ID and account username were valid
    //TODO: generalize this to include any 2XX status
    if(statusCode == 200){
        return true;
    }
    
    //Indicate that the response either failed or the provided registration ID/account email were invalid
    return false;
}