#ifndef _BITMAP_H_
#define _BITMAP_H_

#include "pixel.h"
#include <stdlib.h>
#include <stdint.h>

typedef struct bitmap {
	pixel_t *pixels;
	uint32_t *raw_pixels;
	size_t width, height;
} bitmap_t;

#endif