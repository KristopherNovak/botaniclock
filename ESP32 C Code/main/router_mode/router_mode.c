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
static void initializeServerForRouterMode();
static esp_err_t processReceivedRouterInformation(httpd_req_t *req);
static void extractSSIDAndPasswordFromRequest(httpd_req_t *req);
static esp_err_t serveRouterPageFile(httpd_req_t *req);


//Tag for logging
static char *TAG = "ROUTER_MODE";

//Semaphore that prevents function from returning before SSID and routerPassword have been set
static SemaphoreHandle_t routerSemaphore;

//Place that the SSID of the router/AP will be set
static char* ssid = NULL;

//Place that the password of the router/AP will be set
static char* routerPassword = NULL;


//Gets the SSID and router/AP password and returns these values in a parameterArray
//parameterArray[0] is SSID
//parameterArray[1] is routerPassword
char** getSSIDAndRouterPassword(){

    ESP_LOGI(TAG, "Entering router mode\n");

    //Initialize semaphore to prevent function from returning before SSID and routerPassword have been set
    routerSemaphore = xSemaphoreCreateBinary();

    //Start up the server
    initializeServerForRouterMode();

    //Wait until SSID and routerPassword have been set
    xSemaphoreTake(routerSemaphore, portMAX_DELAY);

    //Set SSID and routerPassword in parameter array
    char** parameterArray = malloc(2*sizeof(char*));
    parameterArray[0] = ssid;
    parameterArray[1] = routerPassword;

    ESP_LOGI(TAG, "Returning from router mode\n");

    return parameterArray;
}

//Starts up the server for router mode
static void initializeServerForRouterMode(){

    //Connect to WiFi as an Access Point with the provided SSID (botaniclock) and password (botaniclock)
    connectToWifiInAPMode("botaniclock", "botaniclock");

    //Initialize custom local domain name ("botaniclock.local")
    mdnsStartUp("botaniclock");

    //Start the server up
    bool matchWildcard = true;
    startServer(matchWildcard);

    //Add endpoints for:
    //GET- sending the page to the user that allows them to add router SSID and routerPassword information
    //POST- receiving SSID and routerPassword information back from the user and processing it
    addEndpoint("/*", "POST", processReceivedRouterInformation);
    addEndpoint("/*", "GET", serveRouterPageFile);

    ESP_LOGI(TAG, "Server initialized\n");

    //TODO: Look to see if function needed to power down server
}

//Processes router information received from the user, notifies the user, and indicates for router mode to end
static esp_err_t processReceivedRouterInformation(httpd_req_t *req){

    ESP_LOGI(TAG, "Request with router information received\n");

    //Extract the SSID and password from the request
    extractSSIDAndPasswordFromRequest(req);
    
    //Let the requester know that the request has been successfully received
    httpd_resp_send(req, NULL, 0);

    //Indicate that router mode is ready to end
    ESP_LOGI(TAG, "Requesting to end router mode\n");
    vTaskDelay(1000/portTICK_PERIOD_MS);
    xSemaphoreGive(routerSemaphore);

    return ESP_OK;
}

//Extracts SSID and AP password from a received request and puts them in the corresponding global variables
static void extractSSIDAndPasswordFromRequest(httpd_req_t *req){
    
    //Get the JSON payload from the request
    cJSON *payload = getJSONPayloadFromRequest(req);

    //TODO: check if tempSSID or tempRouterPassword need to be freed or deleted?
    char *tempSSID = cJSON_GetStringValue(cJSON_GetObjectItem(payload, "ssid"));
    char *tempRouterPassword = cJSON_GetStringValue(cJSON_GetObjectItem(payload, "routerPassword"));

    //Set ssid and routerPassword global variables
    ssid = strdup(tempSSID);
    routerPassword = strdup(tempRouterPassword);

    ESP_LOGI(TAG, "Router information extracted from request\n");

    //Free up the payload
    cJSON_Delete(payload);

}


//Handler that serves a requested router page file to the user
static esp_err_t serveRouterPageFile(httpd_req_t *req){

    ESP_LOGI(TAG, "Serving router page to client\n");
    
    //Serve the requested router page file to the user
    serveWebFile(req, "/store/router");

    return ESP_OK;

}
