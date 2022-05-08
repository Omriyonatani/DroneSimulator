#include <stdio.h>
#include <stdlib.h>

#include "game/game.h"
#include "graphics/graphics.h"
//#include "sensors/lidar.h"
#include "simulator/Simulator.h"

char *path = "textures/p11.png";

int main(int argc, char *argv[]) {
	struct Simulator *sim = initSim(path,2.5,10);
	printf("height: %d\n",sim->mapHeight);
	start(sim);
	destroySim(sim);
	return 0;
}