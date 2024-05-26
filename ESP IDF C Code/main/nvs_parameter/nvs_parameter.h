#ifndef NVS_PARAMETER_H
#define NVS_PARAMETER_H

#include "nvs_flash.h"

//Checks if NVS String parameters and NVS space have been initialized using the provided handle
//If so, return the requested NVS parameter
//If not, then initialize NVS parameter with "" and return that
char* getNVSParameterString(char* theParameterKey, nvs_handle handle);

//Checks if NVS int32_t parameters and NVS space have been initialized using the provided handle
//If so, return the requested NVS parameter
//If not, then initialize NVS parameter with -1 and return that
int32_t getNVSParameterInt32(char* theParameterKey, nvs_handle handle);

#endif