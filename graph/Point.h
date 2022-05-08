#ifndef _POINT_H_
#define _POINT_H_

#include <stdlib.h>

struct Point {
    int x,y;
};

struct Point * createPoint(int x, int y);

void destroyPoint(struct Point* p);

#endif