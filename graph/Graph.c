#include "Graph.h"


#include <stdlib.h>

struct Graph* initGraph(int initialSize) {
    struct Point *p = (struct Point*)malloc(initialSize*sizeof(struct Point));
    struct Graph* g = (struct Graph*)malloc(sizeof(struct Graph));
    g->capacity = initialSize;
    g->size = 0;
    *g->points = p;
    return g;
}

void destroyGraph(struct Graph *g) {
    for (int i=g->size-1;i>=0;i--) {
        free(g->points[i]);
    }
    free(g->points);
    free(g);
}