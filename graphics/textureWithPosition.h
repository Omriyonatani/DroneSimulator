#ifndef _TEXTUREWITHPOSITION_H_
#define _TEXTUREWITHPOSITION_H_

#include <SDL2/SDL.h>

struct textureWithPosition {
    SDL_Texture *texture;
    SDL_Rect *texturePosition;
    SDL_Rect *onRendererPosition;
};


#endif