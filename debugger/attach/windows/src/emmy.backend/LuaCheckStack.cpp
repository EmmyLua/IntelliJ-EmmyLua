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

#include "LuaCheckStack.h"    
#include <assert.h>

LuaCheckStack::LuaCheckStack(LAPI api, lua_State* L, int delta)
{
    m_api   = api;
    m_L     = L;
    m_delta = delta;
    m_top   = lua_gettop_dll(api, L);
}
    
LuaCheckStack::~LuaCheckStack()
{
    int top = lua_gettop_dll(m_api, m_L);
    assert(top - m_top == m_delta);
}
