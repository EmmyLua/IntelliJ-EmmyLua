#if !defined( __LuaTypes_h__)
#define __LuaTypes_h__ 1
// this is what we include instead of the standard Lua headers because there are mismatches that need to be taken care of at compile time
// and obviously we can't include simultaneously the headers from several Lua versions

extern "C"
{
	// comes from luaconf.h, must match the configuration of the VM being debugged
	// 注意：有的人会在 luaconf.h 里修改这个数值，所以这里直接把这个值改大一些，
	// 为的是保证lua_Debug结构体的size比宿主的大，否则可能i_ci值异常
	#define LUA_IDSIZE 1024

	// the lua_Debug structure changes between Lua 5.1 and Lua 5.2
	struct lua_Debug_51
	{
		int event;
		const char *name;	/* (n) */
		const char *namewhat;	/* (n) `global', `local', `field', `method' */
		const char *what;	/* (S) `Lua', `C', `main', `tail' */
		const char *source;	/* (S) */
		int currentline;	/* (l) */
		int nups;		/* (u) number of upvalues */
		int linedefined;	/* (S) */
		int lastlinedefined;	/* (S) */
		char short_src[LUA_IDSIZE]; /* (S) */
		/* private part */
		struct CallInfo *i_ci;  /* active function */
	};

	struct lua_Debug_52
	{
		int event;
		const char *name;	/* (n) */
		const char *namewhat;	/* (n) 'global', 'local', 'field', 'method' */
		const char *what;	/* (S) 'Lua', 'C', 'main', 'tail' */
		const char *source;	/* (S) */
		int currentline;	/* (l) */
		int linedefined;	/* (S) */
		int lastlinedefined;	/* (S) */
		unsigned char nups;	/* (u) number of upvalues */
		unsigned char nparams;/* (u) number of parameters */
		char isvararg;        /* (u) */
		char istailcall;	/* (t) */
		char short_src[LUA_IDSIZE]; /* (S) */
		/* private part */
		struct CallInfo *i_ci;  /* active function */
	};

	union lua_Debug
	{
		lua_Debug_51 ld51;
		lua_Debug_52 ld52;
	};

	// =====================================================
	// must match the configuration of the VM being debugged
	// =====================================================

	typedef double lua_Number;
	typedef ptrdiff_t lua_Integer;

	// ===========================
	// same in Lua 5.1 and Lua 5.2
	// ===========================

	// comes from lauxlib.h
	#define LUA_NOREF       (-2)
	#define LUA_REFNIL      (-1)

	#define LUA_MULTRET	(-1)

	#define LUA_HOOKCALL	0
	#define LUA_HOOKRET	1
	#define LUA_HOOKLINE	2
	#define LUA_HOOKCOUNT	3
	#define LUA_HOOKTAILRET 4 // absent from Lua 5.2
	#define LUA_HOOKTAILCALL 4 // Lua 5.2 specific

	#define LUA_MASKCALL	(1 << LUA_HOOKCALL)
	#define LUA_MASKRET	(1 << LUA_HOOKRET)
	#define LUA_MASKLINE	(1 << LUA_HOOKLINE)
	#define LUA_MASKCOUNT	(1 << LUA_HOOKCOUNT)

	#define LUA_TNIL		0
	#define LUA_TBOOLEAN		1
	#define LUA_TLIGHTUSERDATA	2
	#define LUA_TNUMBER		3
	#define LUA_TSTRING		4
	#define LUA_TTABLE		5
	#define LUA_TFUNCTION		6
	#define LUA_TUSERDATA		7
	#define LUA_TTHREAD		8

	#define LUA_OK		0 // Since Lua 5.2
	#define LUA_YIELD	1
	#define LUA_ERRRUN	2
	#define LUA_ERRSYNTAX	3
	#define LUA_ERRMEM	4
	#define LUA_ERRGCMM	5 // Since Lua 5.2
	// LUA_ERRERR	is 5 in Lua 5.1, but 6 in Lua 5.2

	struct lua_State;
	typedef void * (*lua_Alloc) (void *ud, void *ptr, size_t osize, size_t nsize);
	typedef void (*lua_Hook) (lua_State *L, lua_Debug *ar);
	typedef int (*lua_CFunction) (lua_State *L);
	typedef const char * (*lua_Reader) (lua_State *L, void *ud, size_t *sz);
	typedef unsigned long LAPI;

	#define INVALID_API 0xFFFFFF
} // extern "C"

#endif // __LuaTypes_h__