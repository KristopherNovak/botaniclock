#ifndef SERVER_H
#define SERVER_H

#include "cJSON.h"
#include "esp_http_server.h"

//Converts the request to a JSON payload
cJSON* getJSONPayloadFromRequest(httpd_req_t *req);

//Serves the web file requested in the request using the provided base path
void serveWebFile(httpd_req_t *req, char* basePath);

//Starts up the server (uses wildcard matching if matchWildcard is true)
void startServer(bool matchWildcard);

//Adds an endpoint with the provided URI for the provided HTTP Method (GET, POST, PUT, DELETE)
//that calls the provided handler when invoked
//Returns -1 if the provided HTTP method is invalid
void addEndpoint(char* uri, char* httpMethod, void *handler);

//start up mDNS for the server according to the request domain name
//Should result in "<domainName>.local" being the requestable URL
void mdnsStartUp(char* domainName);

#endif