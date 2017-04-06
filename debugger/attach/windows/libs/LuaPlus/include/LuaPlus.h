#ifndef LUA_PLUS_H
#define LUA_PLUS_H

#define LUA_TWSTRING	9

typedef unsigned short lua_WChar;

const lua_WChar* lua_towstring (lua_State *L, int index);

#endif