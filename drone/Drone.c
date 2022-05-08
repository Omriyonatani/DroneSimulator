#include "Drone.h"

#include <stdbool.h>
#include "../graph/Graph.h"
#include "../graph/Point.h"

struct Drone* initDrone(float size) {
    struct Drone *drone = (struct Drone*)malloc(sizeof(struct Drone));
    drone->sizeRadiusCM=size;
    drone->orientation=0;
    drone->xPos=0;
    drone->yPos=0;
    drone->speedMPS=0;
    drone->front=0;
    drone->left=0;
    drone->back=0;
    drone->right=0;
    struct Graph *g = initGraph(32);
    return drone;
}

void DestroyDrone(struct Drone* drone){
    free(drone);
}

float rotateDrone(struct Drone *drone, int deltaMilli, float rotation){
	float degreePmilli = 0.1;
	float tmp = drone->orientation + degreePmilli*deltaMilli;
	if (tmp < 0) {
		tmp = 360 - tmp;
	}
	if (tmp > 360) {
		tmp = (int)tmp % 360;
	}
	drone->orientation = tmp;
	return (rotation - degreePmilli*deltaMilli);
}

void speedUp(struct Drone *drone) {
    drone->speedMPS = 2;
}

void speedDown(struct Drone *drone) {
    drone->speedMPS = 0;
}

bool is_init=false;
void updateDrone(struct Drone *drone, int deltaMilli) {
    if (!is_init) {
        speedUp(drone);
        // struct Graph *g = drone->g;
        // g->points[g->size] = createPoint(drone->xPos, drone->yPos);
        // is_init = true;
    }


}