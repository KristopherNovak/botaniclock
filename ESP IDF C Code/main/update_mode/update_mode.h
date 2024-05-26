#ifndef UPDATE_MODE_H
#define UPDATE_MODE_H

//Notifies the BotaniClock server that the plant timestamp needs to be updated
//Only returns true if the provided registrationID and accountEmail are valid
bool updatePlantTimestamp(char* registrationID, char* accountEmail);

#endif