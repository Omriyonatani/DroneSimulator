#include "Point.h"

struct Point * createPoint(int x, int y) {
    struct Point *p = (struct Point *)malloc(sizeof(struct Point));
    p->x=x;
    p->y=y;
    return p;
}

void destroyPoint(struct Point* p) {
    free(p);
}