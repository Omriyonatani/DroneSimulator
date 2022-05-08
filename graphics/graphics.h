#ifndef _GRAPHICS_H_
#define _GRAPHICS_H_

#include <SDL2/SDL.h>
#include <SDL2/SDL_image.h>
#include <stdio.h>
#include <stdbool.h>

#include "textureWithPosition.h"
#include "../utils/bitmap.h"

const int SCREEN_WIDTH = 640;
const int SCREEN_HEIGHT = 480;

/**
 * @brief initalize SDL window and renderer
 * 
 * @param window - SDL window
 * @param renderer - SDL renderer
 * @param width - width of window
 * @param height - height of windows
 * @return int - 0 for success, non-zero otherwise (error message will be printed to stdout)
 */
int initGraphics(SDL_Window **window, SDL_Renderer **renderer, int width, int height);

/**
 * @brief load texture from file
 * 
 * @param renderer - SDL renderer
 * @param texture  - SDL texture
 * @param path - file path
 */
void loadTexture(SDL_Renderer **renderer, SDL_Texture **texture, const char *path);

/**
 * @brief load the renderer with textures to desplay
 * 
 * @param renderer - SDL renderer
 * @param texturePos - array of texture, position of the texture, postion on renderer
 * @param size - size of the array
 * @return 0 for success, non-zero otherwise (message will be printed to stdout)
 */
int loadRenderer(SDL_Renderer **renderer, struct textureWithPosition *texturePos[], size_t size);

void getPixels(bitmap_t *bmp, const char *path);

const char* getError();

/*
void close() {
	//Free loaded image
	SDL_FreeSurface( gPNGSurface );
	gPNGSurface = NULL;

	//Destroy window
	SDL_DestroyWindow( gWindow );
	gWindow = NULL;

	//Quit SDL subsystems
	IMG_Quit();
	SDL_Quit();
}

*/

#endif //_GRAPHICS_H_