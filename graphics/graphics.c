#include "graphics.h"

#include <stdlib.h>
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>

int initGraphics(SDL_Window **window, SDL_Renderer **renderer, int width, int height) {
	//Initialize SDL
	if(SDL_Init( SDL_INIT_VIDEO ) < 0) {
		printf( "SDL could not initialize! SDL_Error: %s\n", SDL_GetError() );
		return EXIT_FAILURE;
	}
	//Create window
	*window = SDL_CreateWindow( "SDL Tutorial", SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, width, height, SDL_WINDOW_SHOWN );
	if(*window == NULL) {
		printf( "Window could not be created! SDL_Error: %s\n", SDL_GetError() );
		return EXIT_FAILURE;
	}
	*renderer = SDL_CreateRenderer(*window, -1, SDL_RENDERER_ACCELERATED);
	if (*renderer == NULL) {
		printf( "Renderer could not be created! SDL Error: %s\n", SDL_GetError() );
		return EXIT_FAILURE;
	}
	if (SDL_SetRenderDrawColor(*renderer, 0xFF, 0xFF, 0xFF, 0xFF)) {
		printf("Failed to set renderer color, SDL_Error: %s\n", SDL_GetError());
		return EXIT_FAILURE;
	}
	//Initialize PNG, JPG loading
	//if (!(IMG_Init(IMG_INIT_PNG | IMG_INIT_JPG) & (IMG_INIT_PNG | IMG_INIT_JPG))) {
	if (!(IMG_Init(IMG_INIT_PNG) & IMG_INIT_PNG)) {
		printf( "SDL_image could not initialize! SDL_image Error: %s\n", IMG_GetError() );
		return EXIT_FAILURE;
	}

	return EXIT_SUCCESS;
}

void loadTexture(SDL_Renderer **renderer, SDL_Texture **texture, const char *path){
	//Load image at specified path
    SDL_Surface* loadedSurface = IMG_Load(path);
    if (loadedSurface == NULL) {
        printf("Unable to load image %s! SDL_image Error: %s\n", path, IMG_GetError());
    }
	*texture = SDL_CreateTextureFromSurface(*renderer, loadedSurface);
	SDL_FreeSurface(loadedSurface);
}

int loadRenderer(SDL_Renderer **renderer, struct textureWithPosition *texturePos[], size_t size) {
	int err = SDL_RenderClear(*renderer);
	if (err) {
		printf("Failed to clear renderer, SDL_Error, %s\n", SDL_GetError());
		return err;
	}
	for (size_t index=0; index < size; index++) {
		err = SDL_RenderCopy(*renderer, texturePos[index]->texture, texturePos[index]->texturePosition, texturePos[index]->onRendererPosition);
		if (err) {
			printf("Failed to add texture to renderer, SDL_Error: %s\n", SDL_GetError());
			return err;
		}
	}
	return EXIT_SUCCESS;
}

void getPixels(bitmap_t *bmp, const char *path) {
	SDL_Surface *surface = IMG_Load(path);
	bmp->raw_pixels = (uint32_t*) surface->pixels;
	bmp->height = surface->h;
	bmp->width = surface->w;
}

const char* getError(){
	return SDL_GetError();
}