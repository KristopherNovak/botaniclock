#ifndef CLIENT_H
#define CLIENT_H

//Sends the provided JSON payloadBody to the given URL according to the request type (GET, PUT, POST, DELETE)
//and provides the status code of the response (or -1 if an error occurs)
int httpRequestSend(char* payloadBody, char* url, char* requestType);

//Creates the JSON needed for a request to BotaniClock from the provided plant registration ID
//and email. The returned payloadBody needs to deallocated.
char *createDeviceBodyJSON(char* registrationID, char* accountUsername);

#endif