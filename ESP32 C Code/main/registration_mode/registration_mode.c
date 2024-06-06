#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "esp_log.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "wifi.h"
#include "cJSON.h"
#include "client.h"
#include "server.h"

//Function declarations
static void initializeServerForRegistrationMode();
static esp_err_t registerDevice(httpd_req_t *req);
static void extractEmailAndRegistrationIDFromRequest(httpd_req_t *req);
static esp_err_t serveRegisterPageFile(httpd_req_t *req);

//Tag for logging
static char *TAG = "REGISTRATION_MODE";

//Semaphore that prevents function from returning before SSID and routerPassword have been set
static SemaphoreHandle_t registrationSemaphore;

//Place that the SSID of the router/AP will be set
static char* registrationID = NULL;

//Place that the password of the router/AP will be set
static char* accountEmail = NULL;

//Gets the account email and plant registration ID and returns these values in a parameterArray
//parameterArray[0] is account email
//parameterArray[1] is plant registration ID
char** getEmailAndRegistrationID(){

    ESP_LOGI(TAG, "Entering registration mode");

    //Initialize semaphore to prevent function from returning before registrationID and accountEmail have been set
    registrationSemaphore = xSemaphoreCreateBinary();

    //Start up the server
    initializeServerForRegistrationMode();

    //Wait until accountEmail and registrationID have been set
    xSemaphoreTake(registrationSemaphore, portMAX_DELAY);

     //Set accountEmail and registrationID in parameter array
    char** parameterArray = malloc(2*sizeof(char*));
    parameterArray[0] = accountEmail;
    parameterArray[1] = registrationID;

    ESP_LOGI(TAG, "Returning from registration mode");

    return parameterArray;
}

//starts up the server for registration mode
static void initializeServerForRegistrationMode(){

    //Initialize custom local domain name ("botaniclock.local")
    mdnsStartUp("botaniclock");

    //Start the server up
    bool matchWildcard = true;
    startServer(matchWildcard);

    //Add endpoints for:
    //GET- sending the page to the user that allows them to add accountEmail and registrationID information
    //POST- receiving accountEmail and registrationID information back from the user and processing it
    addEndpoint("/register", "POST", registerDevice);
    addEndpoint("/*", "GET", serveRegisterPageFile);

    ESP_LOGI(TAG, "Server initialized");

}

//Retrieves an account email and registration ID from a request and verifies if the email and registration ID
//are linked to a valid plant
static esp_err_t registerDevice(httpd_req_t *req){

    ESP_LOGI(TAG, "Request for registering device received");

    //Extract email and registration ID from request
    extractEmailAndRegistrationIDFromRequest(req);

    //Create payload body to send to the BotaniClock server
    char *payload_body = createDeviceBodyJSON(registrationID, accountEmail);

    //Endpoint for the BotaniClock server
    char *url = "https://192.168.1.153:8080/api/v1/devices";

    //Send a request to the BotaniClock server to see if they are valid
    ESP_LOGI(TAG, "Verifying email and registration ID with server");
    int statusCode = httpRequestSend(payload_body, url, "POST");
    
    //If the provided registration ID and account username are valid, notify the request sender and finish request mode
    //Otherwise, notify the request sender that the provided registration ID and account username are not valid
    if(statusCode == 200){
        ESP_LOGI(TAG, "Registration successful");
        httpd_resp_send(req, NULL, 0);

        ESP_LOGI(TAG, "Attempting to return from registration mode");
        vTaskDelay(1000/portTICK_PERIOD_MS);
        xSemaphoreGive(registrationSemaphore);
    }
    else{
        ESP_LOGE(TAG, "Registration failed");
        httpd_resp_send_err(req, HTTPD_400_BAD_REQUEST, "Invalid account");
    }

    //Free up the payload body
    if(payload_body != NULL) free(payload_body);

    return ESP_OK;
}


//Extracts email and registration ID from a received request and puts them in the corresponding global variables
static void extractEmailAndRegistrationIDFromRequest(httpd_req_t *req){

    //Get the payload from the request
    ESP_LOGI(TAG, "Extracting email and registration ID from request");
    cJSON *payload = getJSONPayloadFromRequest(req);

    //Extract registrationID and accountEmail from the payload
    //TODO: check if tempRegistrationID and tempAccountEmail need to be freed or deleted?
    char *tempRegistrationID = cJSON_GetStringValue(cJSON_GetObjectItem(payload, "registrationID"));
    char *tempAccountEmail = cJSON_GetStringValue(cJSON_GetObjectItem(payload, "accountEmail"));

    //Set registration ID and accountEmail global variables
    registrationID = strdup(tempRegistrationID);
    accountEmail = strdup(tempAccountEmail);

    //Free up the payload
    cJSON_Delete(payload);


}

//Provides files of page to user that allows them to send account email and registration ID to server
static esp_err_t serveRegisterPageFile(httpd_req_t *req){

    ESP_LOGI(TAG, "Serving registration page to client");
    serveWebFile(req, "/store/register");

    return ESP_OK;

}