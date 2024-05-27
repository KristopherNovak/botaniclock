#include "nvs_flash.h"
#include <stdlib.h>
#include "esp_system.h"

//Checks if NVS String parameters and NVS space have been initialized using the provided handle
//If so, return the requested NVS parameter
//If not, then initialize NVS parameter with "" and return that
char* getNVSParameterString(char* theParameterKey, nvs_handle handle){
    
    //Check if the parameter is in the NVS store
    size_t required_size;
    esp_err_t result = nvs_get_str(handle, theParameterKey, NULL, &required_size);

    //If the key hasn't been created yet, create an new key and set value to ""
    if(result == ESP_ERR_NVS_NOT_FOUND){
        nvs_set_str(handle, theParameterKey, "");
        nvs_commit(handle);
        result = nvs_get_str(handle, theParameterKey, NULL, &required_size);
    }

    //Create memory in which to put the string value of the parameter
    char* theParameter = malloc(required_size);

    //Get the parameter value from NVS
    result = nvs_get_str(handle, theParameterKey, theParameter, &required_size);

    return theParameter;
}


//Checks if NVS int32_t parameters and NVS space have been initialized using the provided handle
//If so, return the requested NVS parameter
//If not, then initialize NVS parameter with -1 and return that
//TODO: formulate a solution that allows -1 to be used for other purposes
int32_t getNVSParameterInt32(char* theParameterKey, nvs_handle handle){
    int32_t theParameter;

    //Attempt to get the parameter from NVS
    esp_err_t result = nvs_get_i32(handle,theParameterKey,&theParameter);

    //If the key hasn't been created yet, create an new key and set value to -1
    if(result == ESP_ERR_NVS_NOT_FOUND){
        nvs_set_i32(handle,theParameterKey, -1);
        theParameter = -1;
        nvs_commit(handle);
    }

    return theParameter;
}
