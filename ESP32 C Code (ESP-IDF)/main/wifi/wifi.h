#ifndef WIFI_CONNECT_H
#define WIFI_CONNECT_H

//Function to start up NetIF, create the event loop, initialize WiFi according to an initial WiFi configuration,
// and register WIFI events and IP events with the event handler
void initializeWifiDriver();

//Attempts to connect to WiFi in Station mode using the provided ssid and routerPassword
//Time out for this operation is determined by connectionAttemptDurationInMS
bool connectToWifiInStationMode(char *ssid, char* routerPassword, int connectionAttemptDurationInMS);

//Attempts to connect to WiFi as an AP using the provided apName as its SSID and
//the provided apPassword as its password
void connectToWifiInAPMode(char* ssid, char* password);

//Function to disconnect from WiFi (note that this DOES NOT destroy the NetIF variable)
void disconnectFromWifi();

//Destroys NetIF, generally intended if WiFi capability os no longer needed
void shutDownWifi();

#endif