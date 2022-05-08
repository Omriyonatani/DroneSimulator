#include "Simulator.h"

#include <math.h>
#include <unistd.h>
#include <stdio.h>
#include <stdbool.h>
#include <pthread.h>

#include <sys/time.h>

#include "../graphics/graphics.h"

bool dronePosSelected=false;

void render(struct Simulator *sim) {
	int err = SDL_RenderClear(sim->renderer);
	if (err) {
		printf("failed to clear renderer, what %s\n",getError());
		return;
	}
	err = SDL_RenderCopy(sim->renderer,sim->mapTexture,NULL,NULL);
	if (err) {
		printf("failed to copy texture to renderer, what %s\n",getError());
		return;
	}
	if (dronePosSelected) {
		SDL_Texture *drone=NULL;
		loadTexture(&sim->renderer,&drone,"textures/drone.png");
		SDL_Rect *d = (SDL_Rect*)malloc(sizeof(SDL_Rect));
		d->x=sim->droneXPosOnMap-sim->drone->sizeRadiusCM;
		d->y=sim->droneYPosOnMap-sim->drone->sizeRadiusCM;
		d->w=sim->drone->sizeRadiusCM*2;
		d->h=sim->drone->sizeRadiusCM*2;
		err = SDL_RenderCopy(sim->renderer,drone,NULL,d);
		if (err) {
			printf("failed to copy drone texture to renderer, what %s\n",getError());
			return;
		}
		free(d);
		SDL_DestroyTexture(drone);
	}
	SDL_RenderPresent(sim->renderer);
}

void moveDrone(struct Drone *drone, int deltaMilli, int *xOnMap, int *yOnMap, float pixelCM) {
	float cmPms = drone->speedMPS/1000;
	float CMmoved = cmPms*deltaMilli;
	double yChange = CMmoved*sin(drone->orientation);
	double xChange = CMmoved*cos(drone->orientation);
	if (drone->orientation <= 90 || drone->orientation >=270) {
		drone->xPos += xChange;
		xOnMap += (int)(xChange/pixelCM);
	}else{
		drone->xPos -= xChange;
		xOnMap -= (int)(xChange/pixelCM);
	}
	if (drone->orientation >= 0 && drone->orientation <= 180) {
		drone->yPos -= yChange;
		yOnMap -= (int)(yChange/pixelCM);
	}else{
		drone->yPos += yChange;
		yOnMap += (int)(yChange/pixelCM);
	}
}

bool isPixelWhite(struct Simulator *sim, int w, int h) {
	uint32_t pix = sim->mapPixels[w * sim->mapWidth + h];
	uint8_t a = (uint8_t)(pix >> 24);
	uint8_t b = (uint8_t)(pix >> 16);
	uint8_t g = (uint8_t)(pix >> 8);
	uint8_t r = (uint8_t)(pix);
	if (r == 255 && g == 255 && b == 255) {
		return true;
	}
	return false;
}

float LidarDistance(struct Simulator *sim, int offset) {
	int maxLidarDist = 300; //in CM
	float lidarOrientation = sim->drone->orientation + offset;
	if (lidarOrientation < 0) {
		lidarOrientation = 360 - lidarOrientation;
	}
	if (lidarOrientation > 360) {
		lidarOrientation = (int)lidarOrientation % 360;
	}
	double yLidarRange = (maxLidarDist+sim->drone->sizeRadiusCM)*sin(lidarOrientation);
	double xLidarRange = (maxLidarDist+sim->drone->sizeRadiusCM)*cos(lidarOrientation);
	int xLidar;
	int yLidar;
	if (lidarOrientation >= 270 || lidarOrientation <= 90) {
		xLidar=sim->droneXPosOnMap+xLidarRange;
	}else{
		xLidar=sim->droneXPosOnMap-xLidarRange;
	}
	if (lidarOrientation >= 0 && lidarOrientation <= 180) {
		yLidar=sim->droneYPosOnMap-yLidarRange;
	}else{
		yLidar=sim->droneYPosOnMap+yLidarRange;
	}
	if (lidarOrientation >= 0 && lidarOrientation <= 90) {
		for (int i=sim->droneXPosOnMap;i<=xLidar;i++) {
			for (int j=sim->droneYPosOnMap; j>=yLidar;j--) {
				if (!isPixelWhite(sim, i, j)) {
					float pixelDist = sqrt(((i-sim->droneXPosOnMap)^2) + ((j-sim->droneYPosOnMap)^2));
					return pixelDist*sim->pixelInCM;
				}
			}
		}
	}else if (lidarOrientation > 90 && lidarOrientation <= 180) {
		for (int i=sim->droneXPosOnMap;i>=xLidar;i--) {
			for (int j=sim->droneYPosOnMap; j>=yLidar;j--) {
				if (!isPixelWhite(sim, i, j)) {
					float pixelDist = sqrt(((i-sim->droneXPosOnMap)^2) + ((j-sim->droneYPosOnMap)^2));
					return pixelDist*sim->pixelInCM;
				}
			}
		}
	}else if (lidarOrientation > 180 && lidarOrientation <= 270) {
		for (int i=sim->droneXPosOnMap;i>=xLidar;i--) {
			for (int j=sim->droneYPosOnMap; j<=yLidar;j++) {
				if (!isPixelWhite(sim, i, j)) {
					float pixelDist = sqrt(((i-sim->droneXPosOnMap)^2) + ((j-sim->droneYPosOnMap)^2));
					return pixelDist*sim->pixelInCM;
				}
			}
		}
	}else if (lidarOrientation > 270 && lidarOrientation <= 360) {
		for (int i=sim->droneXPosOnMap;i<=xLidar;i++) {
			for (int j=sim->droneYPosOnMap; j<=yLidar;j++) {
				if (!isPixelWhite(sim, i, j)) {
					float pixelDist = sqrt(((i-sim->droneXPosOnMap)^2) + ((j-sim->droneYPosOnMap)^2));
					return pixelDist*sim->pixelInCM;
				}
			}
		}
	}
	return 300;
}

void updateDroneLidars(struct Simulator *sim) {
	sim->drone->front = LidarDistance(sim,0);
	sim->drone->left = LidarDistance(sim,90);
	sim->drone->back = LidarDistance(sim,180);
	sim->drone->right = LidarDistance(sim,270);
}

void start(struct Simulator *sim) {
	SDL_Event event;
	bool quit = false;
	//drone position
	int x,y;
	while (!quit) {
		struct timeval sTime, eTime;
		gettimeofday(&sTime,NULL);
		render(sim);
		//poll event return 1 if event exist
		if (SDL_WaitEvent(&event)) {
			switch (event.type) {
			case SDL_QUIT:
				return;
			case SDL_MOUSEBUTTONUP:
				if (event.button.button == SDL_BUTTON_LEFT) {
        			SDL_GetMouseState(&x, &y);
				}
				quit=true;
				break;
			}
		}else {
			printf("failed to wait for event, what: %s\n",getError());
		}
		gettimeofday(&eTime,NULL);
		if (eTime.tv_sec-sTime.tv_sec == 0 && eTime.tv_usec-sTime.tv_usec < 10000){
			usleep(eTime.tv_usec-sTime.tv_usec);
		}
	}
	sim->droneXPosOnMap = x;
	sim->droneYPosOnMap = y;
	dronePosSelected=true;
	quit=false;
	int lastTime = 0; //delta time milli
	while (!quit) {
		struct timeval sTime, eTime;
		gettimeofday(&sTime,NULL);
		render(sim);
		//poll event return 1 if event exist
		if (SDL_WaitEvent(&event)) {
			switch (event.type) {
			case SDL_QUIT:
				quit=true;
				break;
			}
		}else {
			printf("failed to wait for event, what: %s\n",getError());
		}
		moveDrone(sim->drone, lastTime,&sim->droneXPosOnMap,&sim->droneYPosOnMap,sim->pixelInCM);
		updateDroneLidars(sim);
		printf("here\n");
		updateDrone(sim->drone,lastTime);
		gettimeofday(&eTime,NULL);
		lastTime = eTime.tv_usec-sTime.tv_usec;
		if (eTime.tv_sec-sTime.tv_sec == 0 && eTime.tv_usec-sTime.tv_usec < 10000){
			usleep(eTime.tv_usec-sTime.tv_usec);
		}
	}
}





struct Simulator* initSim(char *map, double pixelSize, float droneSizeCM){
    struct Simulator *sim = (struct Simulator *)malloc(sizeof(struct Simulator));
	sim->pixelInCM=pixelSize;
	sim->drone=initDrone(droneSizeCM);
    sim->map=map;
    sim->window=NULL;
    sim->renderer=NULL;
    SDL_Surface *surface = IMG_Load(sim->map);
    sim->mapWidth = surface->w;
    sim->mapHeight= surface->h;
    int err = initGraphics(&sim->window, &sim->renderer, sim->mapWidth,sim->mapHeight);
    if (err) {
		printf("failed to init SDL, what: %s\n", getError());
		return NULL;
	}
	err = SDL_LockSurface(surface);
	 	if (err) {
	 	printf("failed to lock surface, what: %s\n", getError());
	 	return NULL;
	}
    int size = sim->mapWidth * sim->mapHeight;
	uint32_t *pixels = (uint32_t*)surface->pixels;
	sim->mapPixels = (uint32_t*) malloc(size*sizeof(uint32_t));
	memset(sim->mapPixels,0,size*sizeof(uint32_t));
	memcpy(sim->mapPixels,pixels,size*sizeof(uint32_t));
	SDL_UnlockSurface(surface);
	sim->mapTexture = SDL_CreateTextureFromSurface(sim->renderer,surface);
    SDL_FreeSurface(surface);
    return sim;
}

void destroySim(struct Simulator *sim){
	SDL_DestroyTexture(sim->mapTexture);
    SDL_DestroyRenderer(sim->renderer);
    SDL_DestroyWindow(sim->window);
	DestroyDrone(sim->drone);
    free(sim->mapPixels);
	free(sim);
}

void loadMap(struct Simulator *sim) {

}