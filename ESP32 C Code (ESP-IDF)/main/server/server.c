#include "cJSON.h"
#include "esp_http_server.h"
#include "esp_log.h"
#include "esp_vfs.h"
#include "esp_vfs_fat.h"
#include "mdns.h"


static char *TAG = "SERVER";
static httpd_handle_t httpdHandle = NULL;
static const char *BASE_PATH = "/store";

//Converts the request to a JSON payload
cJSON* getJSONPayloadFromRequest(httpd_req_t *req){
    //Initialize buffer for receiving request
    char requestBuffer[req->content_len+1];

    //Set last entry in buffer to 0
    requestBuffer[req->content_len] = 0;

    //Insert request into request buffer
    httpd_req_recv(req, requestBuffer, req->content_len);

    //Print the request buffer
    printf("%s\n", requestBuffer);

    //Extract the registration ID and account email from the payload
    return cJSON_Parse(requestBuffer);
}


//Serves the web file requested in the request using the provided base path
void serveWebFile(httpd_req_t *req, char* basePath){

    //generate path to the requested file
    char pathToRequestedFile[600];
    sprintf(pathToRequestedFile, "%s/%s", basePath, req->uri);
    ESP_LOGI(TAG, "Path to requested file: %s", pathToRequestedFile);

    //Attempt to open the requested file
    //If file cannot be found, then open the index HTML file
    //If that can't be located, then send 404
    FILE *webFile = fopen(pathToRequestedFile,"r");

    char indexHTMLFilePath[100];
    sprintf(indexHTMLFilePath, "%s/index.html", basePath);
    if(webFile == NULL) webFile = fopen(indexHTMLFilePath,"r");
    if(webFile == NULL){
        httpd_resp_send_404(req);
        return;
    }

    //Determine requested file type from the requested extension and set response type accordingly
    char *extension = strrchr(req->uri,'.');
    if(extension)
    {
        if(strcmp(extension,".css") == 0) httpd_resp_set_type(req,"text/css");
        if(strcmp(extension,".js") == 0) httpd_resp_set_type(req,"text/javascript");
    }

    //Send web page to client in the response
    char fileDataBuffer[1024];
    int additionalDataToSend = fread(fileDataBuffer,sizeof(char),sizeof(fileDataBuffer),webFile);
    while(additionalDataToSend > 0){
        httpd_resp_send_chunk(req,fileDataBuffer,additionalDataToSend);
        additionalDataToSend = fread(fileDataBuffer,sizeof(char),sizeof(fileDataBuffer),webFile);
    }

    //Send null termination
    httpd_resp_send_chunk(req,NULL,0);

    //Close the file
    fclose(webFile);
}

//Starts up the web page file system
static void initializeWebPageFileSystem(){

    //Set up the mount configuration
    esp_vfs_fat_mount_config_t espVFSFATMountConfig = {
        .allocation_unit_size = CONFIG_WL_SECTOR_SIZE,
        .max_files = 6,
        .format_if_mount_failed = true,
    };

    //Initialize file system in read-only mode
    esp_vfs_fat_spiflash_mount_ro(BASE_PATH, "storage", &espVFSFATMountConfig);

}

//Starts up the server (uses wildcard matching if matchWildcard is true)
void startServer(bool matchWildcard){

    initializeWebPageFileSystem();

    httpd_config_t httpdConfig = HTTPD_DEFAULT_CONFIG();

    if(matchWildcard){
        httpdConfig.uri_match_fn = httpd_uri_match_wildcard;
    }

    ESP_ERROR_CHECK(httpd_start(&httpdHandle, &httpdConfig));
}

//Converts the request type to a esp_httpd_method_t
//Provides a -1 if it fails
static httpd_method_t strToHTTPMethod(char* httpMethod){

    if(strcmp(httpMethod, "PUT") == 0) return HTTP_PUT;

    if(strcmp(httpMethod, "POST") == 0) return HTTP_POST;

    if(strcmp(httpMethod, "GET") == 0) return HTTP_GET;

    if(strcmp(httpMethod, "DELETE") == 0) return HTTP_DELETE;

    ESP_LOGE(TAG, "Invalid HTTP Method type");
    return -1;

}

//Adds an endpoint with the provided URI for the provided HTTP Method (GET, POST, PUT, DELETE)
//that calls the provided handler when invoked
void addEndpoint(char* uri, char* httpMethod, void *handler){

    //Convert the httpMethod string to an httpd_method_t type
    httpd_method_t hM = strToHTTPMethod(httpMethod);
    if(hM == -1){
        //TODO: Add handling here
        return;
    }

    //Create the new endpoint
    httpd_uri_t newEndpoint = {
    .uri = uri,
    .method = hM,
    .handler = handler
  };

    //Attempt to register the new endpoint
    ESP_ERROR_CHECK(httpd_register_uri_handler(httpdHandle, &newEndpoint));
}

//start up mDNS for the server according to the request domain name
//Should result in "<domainName>.local" being the requestable URL
void mdnsStartUp(char* domainName){
    mdns_init();
    mdns_hostname_set(domainName);
    mdns_instance_name_set(domainName);
}


