/*

Decoda
Copyright (C) 2007-2013 Unknown Worlds Entertainment, Inc. 

This file is part of Decoda.

Decoda is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Decoda is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Decoda.  If not, see <http://www.gnu.org/licenses/>.

*/

#ifndef LUA_CHECK_STACK_H
#define LUA_CHECK_STACK_H

#include "LuaDll.h"

#define LUA_CHECK_STACK(api, L, delta) LuaCheckStack __checkStack(api, L, delta);

/**
 * Helper class used to make sure the Lua stack has the correct number of
 * elements on it when the scope level exits.
 */
class LuaCheckStack
{

public:

    LuaCheckStack(LAPI api, lua_State* L, int delta);
    ~LuaCheckStack();

private:

    unsigned long       m_api;
    lua_State*          m_L;
    int                 m_top;
    int                 m_delta;

};

#endif