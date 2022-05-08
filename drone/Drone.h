#ifndef _DRONE_H_
#define _DRONE_H_

#include <stdlib.h>

#include "../sensors/lidar.h"
#include "../graph/Graph.h"

struct Drone {
	int xPos, yPos;
	int front,left,back,right;
	int battary;
	float orientation;
	float speedMPS;
	float sizeRadiusCM; /* size of drone in cm*/

	struct Graph *g;
};

struct Drone* initDrone(float size);
void DestroyDrone(struct Drone* drone);
/**
 * @brief calculate new orientation of drone
 * 
 * @param drone 
 * @param deltaMilli - time to rotate
 * @param rotation - need to rotate
 * @return float - degrees left to rotate
 */
float rotateDrone(struct Drone *drone, int deltaMilli, float rotation);

void updateDrone(struct Drone *drone, int deltaMilli);


#endif