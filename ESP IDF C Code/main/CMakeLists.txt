idf_component_register(SRCS 
                    "main.c"
                    "wifi/wifi.c"
                    "nvs_parameter/nvs_parameter.c"
                    "client/client.c"
                    "router_mode/router_mode.c"
                    "registration_mode/registration_mode.c"
                    "button/button.c"
                    "update_mode/update_mode.c"
                    "led/led.c"
                    "server/server.c"
                    INCLUDE_DIRS 
                    "."
                    "wifi"
                    "nvs_parameter"
                    "client"
                    "router_mode"
                    "registration_mode"
                    "button"
                    "update_mode"
                    "led"
                    "server")

fatfs_create_rawflash_image(storage ../site FLASH_IN_PROJECT)
