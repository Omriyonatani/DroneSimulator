CC=g++
CFLAGS=-Wall -g
SDLFLAGS=-lSDL2main -lSDL2 -lSDL2_image

GRAPHICS_LIB=graphics/graphics.h utils/bitmap.h utils/pixel.h simulator/Simulator.h sensors/lidar.h drone/Drone.h graph/Graph.h graph/Point.h
OUT=main

$(OUT): graphics.o main.o Simulator.o Drone.o Graph.o Point.o
	$(CC) $(CFLAGS) -o $(OUT) main.c graphics.o Simulator.o Drone.o Graph.o Point.o $(SDLFLAGS)

graphics.o: graphics/graphics.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c graphics/graphics.c

Simulator.o: simulator/Simulator.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c simulator/Simulator.c

Drone.o: drone/Drone.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c drone/Drone.c

Graph.o: graph/Graph.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c graph/Graph.c

Point.o: graph/Point.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c graph/Point.c

main.o: main.c $(GRAPHICS_LIB)
	$(CC) $(CFLAGS) -c main.c

clean:
	rm -f *.o $(OUT)