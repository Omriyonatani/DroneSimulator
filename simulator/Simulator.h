#ifndef _SIMULATOR_H_
#define _SIMULATOR_H_

#include <stdint.h>


#include "../graphics/graphics.h"
#include "../drone/Drone.h"
#include "../sensors/lidar.h"

struct Simulator {
	char *map;
	uint32_t *mapPixels;
	int mapWidth,mapHeight;

	double pixelInCM;

	SDL_Window *window;
	SDL_Renderer *renderer;
	SDL_Texture *mapTexture;

	struct Drone *drone;
	int droneXPosOnMap, droneYPosOnMap;
};

struct Simulator* initSim(char *map, double pixelSize,float droneSizeCM);
void destroySim(struct Simulator *sim);

void start(struct Simulator *sim);


#endif