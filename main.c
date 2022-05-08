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
	// // if (argc < 2) {
	// // 	printf("please enter map number\n");
	// // 	return EXIT_FAILURE;
	// // }
	// SDL_Window *window = NULL;
	// SDL_Renderer *renderer = NULL;
	// SDL_Texture *texture = NULL;
	// SDL_Surface *surface = IMG_Load(path);
	// int err = initGraphics(&window,&renderer, surface->w, surface->w);
	// if (err) {
	// 	printf("failed to init SDL, what: %s\n", getError());
	// 	return EXIT_FAILURE;
	// }
	// // texture = SDL_CreateTextureFromSurface(renderer, surface);
	// // if (texture == NULL) {
	// // 	printf("failed to create texute from surface, what: %s\n", getError());
	// // 	return EXIT_FAILURE;
	// // }
	// err = SDL_LockSurface(surface);
	// 	if (err) {
	// 	printf("failed to lock surface, what: %s\n", getError());
	// 	return EXIT_FAILURE;
	// }
	// int size = surface->w * surface->h;
	// uint32_t *pixels = (uint32_t*)surface->pixels;
	// uint32_t p[size];
	// memset(p,0,size*sizeof(uint32_t));
	// memcpy(p,pixels,size*sizeof(uint32_t));
	// for (int i=0; i < surface->w; i++) {
	// 	uint32_t pix = pixels[i];
	// 	uint8_t a = (uint8_t)(pix >> 24);
	// 	uint8_t b = (uint8_t)(pix >> 16);
	// 	uint8_t g = (uint8_t)(pix >> 8);
	// 	uint8_t r = (uint8_t)(pix);
	// 	printf("i: %d, (%d,%d,%d,%d)\n",i,a,b,g,r);
	// }
	// SDL_Delay(5000);


	// printf("format: %s\n", SDL_GetPixelFormatName(surface->format->format));
	// printf("height: %d, width: %d\n",surface->h, surface->w);

	// // game g(path, 1);
	// // g.start();

	// /*
	// SDL_Surface *surface = IMG_Load(path);
	// std::cout << "format: " << SDL_GetPixelFormatName(surface->format->format) << std::endl;
	// std::cout << "height: " << surface->h << ", width: " << surface->w << std::endl;
	// std::cout << "pitch: " << surface->pitch << std::endl;
	// std::cout << "pixels: " << surface->pixels << std::endl;
	// Uint32 *pixels = (Uint32*) surface->pixels;
	// Uint8 r = 0;
	// Uint8 g = 0;
	// Uint8 b = 0;
	// Uint32 pixel = pixels[0];
	// SDL_GetRGB(pixel, surface->format,&r,&g,&b);
	// std::cout << "pixel:" << pixel << std::endl;
	// std::cout << "rgb: [" << (int)r << "," << (int)g << "," << (int)b << "]" << std::endl;

	// //bitmap_t *bmp;
	// //getPixels(bmp, path);
	// SDL_FreeSurface(surface);
	// */
	destroySim(sim);
	return 0;
}