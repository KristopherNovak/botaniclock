#include "esp_http_client.h"
#include "esp_log.h"
#include "cJSON.h"

//Type representing the payload received in an HTTP repsonse
typedef struct response_payload_t{
    uint8_t *buffer;
    int bufferLength;
} response_payload_t;

//Function declarations
static esp_http_client_method_t strToClientMethod(char* requestType);
static esp_err_t httpClientEventHandler(esp_http_client_event_t *event);
static void concatenateNewDataOntoResponsePayload(response_payload_t *currentResponsePayload, uint8_t *newData, int newDataLength);

//Logging tag
static char *TAG = "CLIENT";

//Sends the provided JSON payloadBody to the given URL according to the request type (GET, PUT, POST, DELETE)
//and provides the status code of the response
int httpRequestSend(char* payloadBody, char* url, char* requestType){

    //Convert the request type to a esp_http_client_method_t
    //send back -1 if the conversion fails
    esp_http_client_method_t requestedMethod = strToClientMethod(requestType);
    if(requestedMethod == -1) return -1;

    //Initialize the response payload with a zero-length buffer
    response_payload_t httpResponsePayload = {0};

    //Prepare the HTTP client configuration
    esp_http_client_config_t espHTTPClientConfig = {
        .url = url,
        .method = requestedMethod,
        .event_handler = httpClientEventHandler,
        .user_data = &httpResponsePayload
    };

    //Initialize the client
    esp_http_client_handle_t client = esp_http_client_init(&espHTTPClientConfig);

    //Set the header of the client to indicate that the payloadBody is in JSON
    esp_http_client_set_header(client, "Content-Type", "application/json");

    //Add the payloadBody to the request
    esp_http_client_set_post_field(client, payloadBody, strlen(payloadBody));

    //Send the request and receive the response
    esp_err_t err = esp_http_client_perform(client);

    //If something went wrong with the request, log it and return -1
    if(err != ESP_OK){
        ESP_LOGE(TAG, "HTTP request error: %s", esp_err_to_name(err));
        return -1;
    }

    //Acquire the status code of the response
    int statusCode = esp_http_client_get_status_code(client);

    //Log the status code and the payload
    ESP_LOGI(TAG, "HTTP status: %d\n", statusCode);
    ESP_LOGI(TAG, "Response Payload:  %s\n", httpResponsePayload.buffer);

    //Clean up the client
    esp_http_client_cleanup(client);

    //Free the httpResponsePayload if it includes any information
    if(httpResponsePayload.buffer != NULL) free(httpResponsePayload.buffer);

    //Return the status code of the response
    return statusCode;
}

//Converts the request type to a esp_http_client_method_t
//Provides a -1 if it fails
static esp_http_client_method_t strToClientMethod(char* requestType){

    if(strcmp(requestType, "PUT") == 0) return HTTP_METHOD_PUT;

    if(strcmp(requestType, "POST") == 0) return HTTP_METHOD_POST;

    if(strcmp(requestType, "GET") == 0) return HTTP_METHOD_GET;

    if(strcmp(requestType, "DELETE") == 0) return HTTP_METHOD_DELETE;

    ESP_LOGE(TAG, "Invalid request type");
    return -1;

}

//Creates the JSON needed for a request to BotaniClock from the provided plant registration ID
//and email. The returned payloadBody needs to deallocated.
char *createDeviceBodyJSON(char* registrationID, char* accountEmail){
    //Create a json payload
    cJSON *jsonPayload = cJSON_CreateObject();

    //Add JSON fpr th eregistration ID and the account email to the payload
    cJSON_AddStringToObject(jsonPayload, "registrationID", registrationID);
    cJSON_AddStringToObject(jsonPayload, "accountEmail", accountEmail);

    //Convert the payload to a string
    char *payloadBody = cJSON_Print(jsonPayload);

    //Print the payload body to the screen
    ESP_LOGI(TAG, "Body: %s\n", payloadBody);

    //Delete the JSON payload (at it is no longer needed once printed)
    cJSON_Delete(jsonPayload);

    //Return the payload body
    return payloadBody;
}

//Handler used to make sure all data is appended onto the HTTP response (as it may not be received all at once
//and thus this function may be called multiple times)
static esp_err_t httpClientEventHandler(esp_http_client_event_t *event){
    //If no new data is received, no need to do anything
    if(event->event_id != HTTP_EVENT_ON_DATA){
        return ESP_OK;
    }

    response_payload_t *currentResponsePayload = event->user_data;
    uint8_t *newData = event->data;
    int newDataLength = event->data_len;

    //Concatenate the newly received data onto the response payload
    concatenateNewDataOntoResponsePayload(currentResponsePayload, newData, newDataLength);

    return ESP_OK;
}

//Concatenates the new data onto a current response payload
static void concatenateNewDataOntoResponsePayload(response_payload_t *currentResponsePayload, uint8_t *newData, int newDataLength){

    //resize the buffer in the current response payload to accomodate the new data
    int newBufferLength =  currentResponsePayload->bufferLength + newDataLength + 1;
    currentResponsePayload->buffer = realloc(currentResponsePayload->buffer, newBufferLength);

    //Copy the content of the new data to the end of the current buffer
    uint8_t *addressToWhichToCopyNewData = &currentResponsePayload->buffer[currentResponsePayload->bufferLength];
    memcpy(addressToWhichToCopyNewData, newData, newDataLength);

    //Increase the size of the buffer to the new length
    currentResponsePayload->bufferLength = currentResponsePayload->bufferLength + newDataLength;

    //Add zero to the end of the buffer to null terminate it
    currentResponsePayload->buffer[currentResponsePayload->bufferLength] = 0;

}