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

#ifndef LUA_DLL_H
#define LUA_DLL_H

#include <windows.h>

#include "LuaTypes.h"

#include <LuaPlus.h>

typedef int (*lua_CFunction_dll) (unsigned long api, lua_State *L);

lua_State*      lua_newstate_dll        (unsigned long api, lua_Alloc, void*);
lua_State*      lua_newthread_dll       (unsigned long api, lua_State* L);
void            lua_close_dll           (unsigned long api, lua_State*);
int             lua_error_dll           (unsigned long api, lua_State*);
int             lua_absindex_dll        (unsigned long api, lua_State*, int);
int             lua_sethook_dll         (unsigned long api, lua_State*, lua_Hook, int, int);
int             lua_getinfo_dll         (unsigned long api, lua_State*, const char*, lua_Debug* ar);
void            lua_remove_dll          (unsigned long api, lua_State*, int);
void            lua_settable_dll        (unsigned long api, lua_State*, int);
void            lua_gettable_dll        (unsigned long api, lua_State*, int);
void            lua_rawget_dll          (unsigned long api, lua_State*, int idx);
void            lua_rawgeti_dll         (unsigned long api, lua_State*, int idx, int n);
void            lua_rawset_dll          (unsigned long api, lua_State* L, int index);
void            lua_pushstring_dll      (unsigned long api, lua_State*, const char*);
void            lua_pushlstring_dll     (unsigned long api, lua_State*, const char*, size_t);
int             lua_type_dll            (unsigned long api, lua_State*, int);
const char*     lua_typename_dll        (unsigned long api, lua_State*, int);
void            lua_getfield_dll        (unsigned long api, lua_State*, int, const char*);
void            lua_setfield_dll        (unsigned long api, lua_State*, int, const char*);
void            lua_setglobal_dll       (unsigned long api, lua_State*, const char* s);
void            lua_getglobal_dll       (unsigned long api, lua_State*, const char* s);
void            lua_settop_dll          (unsigned long api, lua_State*, int);
const char*     lua_getlocal_dll        (unsigned long api, lua_State*, const lua_Debug*, int);
const char*     lua_setlocal_dll        (unsigned long api, lua_State*, const lua_Debug*, int);
int             lua_getstack_dll        (unsigned long api, lua_State*, int, lua_Debug*);
void            lua_insert_dll          (unsigned long api, lua_State*, int);
void            lua_pushnil_dll         (unsigned long api, lua_State*);
void            lua_pushcclosure_dll    (unsigned long api, lua_State*, lua_CFunction, int);
void            lua_pushvalue_dll       (unsigned long api, lua_State*, int);
void            lua_pushinteger_dll     (unsigned long api, lua_State*, int);
void            lua_pushumber_dll       (unsigned long api, lua_State*, lua_Number);
void            lua_pushlightuserdata_dll(unsigned long api, lua_State *L, void *p);
void            lua_pushglobaltable_dll (unsigned long api, lua_State *L);
const char*     lua_tostring_dll        (unsigned long api, lua_State*, int);
const char*     lua_tolstring_dll       (unsigned long api, lua_State*, int, size_t* len);
int             lua_toboolean_dll       (unsigned long api, lua_State*, int);
int             lua_tointeger_dll       (unsigned long api, lua_State*, int);
lua_CFunction   lua_tocfunction_dll     (unsigned long api, lua_State*, int);
lua_Number      lua_tonumber_dll        (unsigned long api, lua_State*, int);
void*           lua_touserdata_dll      (unsigned long api, lua_State* L, int index);
int             lua_gettop_dll          (unsigned long api, lua_State*);
int             lua_loadbuffer_dll      (unsigned long api, lua_State*, const char*, size_t, const char*, const char*);
void            lua_call_dll            (unsigned long api, lua_State*, int, int);
int             lua_pcall_dll           (unsigned long api, lua_State*, int, int, int);
void            lua_newtable_dll        (unsigned long api, lua_State*);
int             lua_next_dll            (unsigned long api, lua_State*, int);
int             lua_rawequal_dll        (unsigned long api, lua_State *L, int idx1, int idx2);
int             lua_getmetatable_dll    (unsigned long api, lua_State*, int objindex);
int             lua_setmetatable_dll    (unsigned long api, lua_State* L, int index);
int             luaL_loadfile_dll       (unsigned long api, lua_State*, const char*);
int             luaL_loadfilex_dll      (unsigned long api, lua_State*, const char*, const char*);
lua_State*      luaL_newstate_dll       (unsigned long api);
int             luaL_loadbuffer_dll     (unsigned long api, lua_State*, const char*, size_t, const char*);
int             luaL_loadbufferx_dll    (unsigned long api, lua_State*, const char*, size_t, const char*, const char*);
int             luaL_ref_dll            (unsigned long api, lua_State *L, int t);
void            luaL_unref_dll          (unsigned long api, lua_State *L, int t, int ref);
int             luaL_newmetatable_dll   (unsigned long api, lua_State *L, const char *tname);
const char *    lua_getupvalue_dll      (unsigned long api, lua_State *L, int funcindex, int n);
const char *    lua_setupvalue_dll      (unsigned long api, lua_State *L, int funcindex, int n);
void            lua_getfenv_dll         (unsigned long api, lua_State *L, int index);
int             lua_setfenv_dll         (unsigned long api, lua_State *L, int index);
void *          lua_newuserdata_dll     (unsigned long api, lua_State *L, size_t size);
int             lua_checkstack_dll      (unsigned long api, lua_State *L, int extra);

/**
 * Similar to lua_pushthread, but will be emulated under Lua 5.0. The return
 * value is true if the function was successful, or false if otherwise. Note
 * this is different than lua_pushthread.
 */
bool lua_pushthread_dll(unsigned long api, lua_State *L);

// These functoin only exists in LuaPlus. If the application is not using LuaPlus,
// lua_towstring will return NULL and lua_iswstring will return 0.
const lua_WChar*    lua_towstring_dll   (unsigned long api, lua_State *L, int index);
int                 lua_iswstring_dll   (unsigned long api, lua_State *L, int index);

#define lua_pop_dll(api,L,n)                lua_settop_dll(api, L, -(n)-1)
#define lua_isnil_dll(api,L,n)              (lua_type_dll(api, L, (n)) == LUA_TNIL)
#define lua_pushcfunction_dll(api,L,f)      lua_pushcclosure_dll(api, L, (f), 0)
#define lua_register_dll(api, L,n,f)        (lua_pushcfunction_dll(api, L, f), lua_setglobal_dll(api, L, n))

/**
 * This is similar to lua_cpcall_dll, but it handles the differences between
 * stdcall and cdecl and passes an api parameter to the C function. Therefore
 * using this function is preferred over lua_cpcall_dll.
 * and lua_cpcall is deprecated in Lua 5.2, so it's gone anyway.
 */
int lua_cpcall_dll(unsigned long api, lua_State *L, lua_CFunction_dll func, void *ud);

/**
 * Like lua_getglobal, but does't invoke metamethods.
 */
void lua_rawgetglobal_dll(unsigned long api, lua_State* L, const char* s);

/**
 * Similar to lua_upvalueindex.
 */
int lua_upvalueindex_dll(unsigned long api, int i);

/**
 * Attempts to load the relevant Lua functions from one of the DLLs loaded in the
 * process. If none of the loaded DLLs contain all of the needed functions, the
 * function returns false.
 */
bool InstallLuaHooker(HINSTANCE hInstance, const char* symbolsDirectory);

/**
 * Returns true if the Lua dll has been loaded.
 */
bool GetIsLuaLoaded();

/**
 * Registers the "decoda" Lua library with the state.
 */
void RegisterDebugLibrary(unsigned long api, lua_State* L);

int GetGlobalsIndex(unsigned long api);
int GetRegistryIndex(unsigned long api);

void EnableIntercepts(unsigned long api, bool enableIntercepts);
bool GetAreInterceptsEnabled();

enum HookMode
{
    HookMode_None,
    HookMode_CallsOnly,
    HookMode_CallsAndReturns,
    HookMode_Full,
};
/**
 * Sets the debug hook mode for the specified state. The state
 * can only be debugged when the hook is higher than HookMode_None.
 */
void SetHookMode(unsigned long api, lua_State* L, HookMode mode);

/**
 * Returns the current hook mode for the specified state.
 */
HookMode GetHookMode(unsigned long api, lua_State* L);

bool GetIsHookEventRet(unsigned long api, int event);
bool GetIsHookEventCall(unsigned long api, int event);
int GetEvent(unsigned long api, const lua_Debug* ar);
int GetNups(unsigned long api, const lua_Debug* ar);
int GetCurrentLine(unsigned long api, const lua_Debug* ar);
int GetLineDefined(unsigned long api, const lua_Debug* ar);
int GetLastLineDefined(unsigned long api, const lua_Debug* ar);
const char* GetSource(unsigned long api, const lua_Debug* ar);
const char* GetWhat(unsigned long api, const lua_Debug* ar);
const char* GetName(unsigned long api, const lua_Debug* ar);
const char* GetHookEventName(unsigned long api, const lua_Debug* ar);
const void* GetCI(unsigned long api, const lua_Debug* ar);
/**
 * Creates a new function that can be used with the specified API. The new function
 * automatically handles stdcall (if necessary) and adds an api parameter.
 */
void* CreateCFunction(unsigned long api, void* function, void* worker);

void FreeLuaDll();
#endif