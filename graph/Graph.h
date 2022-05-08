#ifndef _GRAPH_H_
#define _GRAPH_H_

#include "Point.h"

struct Graph {
    int size;
    int capacity;
    struct Point *points[];
};

struct Graph* initGraph(int initialSize);
void destroyGraph(struct Graph *g);


#endif