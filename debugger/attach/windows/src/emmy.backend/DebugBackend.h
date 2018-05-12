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

#ifndef DEBUG_BACKEND_H
#define DEBUG_BACKEND_H

#include "Channel.h"
#include "Protocol.h"
#include "CriticalSection.h"
#include "LuaDll.h"

#include <vector>
#include <string>
#include <list>
#include <hash_set>
#include <hash_map>

//
// Forward declarations.
//

class StackNode;
class StackLuaObjectNode;
class StackTableNode;
class EvalResultNode;
class DebugPipeline;
class DebugMessage;
class LPFunctionCall;

enum class ScriptRegisterResult
{
	RegisterAndSend,
	RegisterOnly,
	Exist
};

/**
 * This class encapsulates the part of the debugger that runs inside the
 * process begin debugged. It communicates with a DebugClient via a channel.
 */
class DebugBackend
{
    struct VirtualMachine;

public:

    /**
     * Singleton accessor.
     */
    static DebugBackend& Get();

    /**
     * Destroys the singleton.
     */
    static void Destroy();

    /**
     * Writes to the log.
     */
    void Log(const char* fmt, ...);

    /**
     * Initializes the server.
     */
    bool Initialize(HINSTANCE hInstance);

	bool InitializePipeline();

    /**
     * Attaches the debugger to the state.
     */
    VirtualMachine* AttachState(LAPI api, lua_State* L);

    /**
     * Detaches the debugger from the state.
     */
    void DetachState(LAPI api, lua_State* L);

    /**
     * Sends information about the script to the front end. This is called after
     * calling the lua_load (or similar) method.
     */
    int PostLoadScript(LAPI api, int result, lua_State* L, const char* source, size_t size, const char* name);

    /**
     * Registers a script with the backend. This will tell track this source file
     * and send notification to the front end about it. If the script is already
     * loaded the method returns -1. The unavailable flag specifies that the code
     * was not available for the script. This should be set if the script was encountered
     * through a call other than the load function.
     */
	ScriptRegisterResult RegisterScript(LAPI api, lua_State* L, const char* source, size_t size, const char* name, bool unavailable, size_t* index);

	ScriptRegisterResult RegisterScript(LAPI api, lua_State* L, lua_Debug* ar, size_t* index);

	void UnregisterScript(const char* name, const char* fixedName);

    /**
     * Steps execution of a "broken" script by one line. If the current line
     * is a function call, this will step into the function call.
     */
    void StepInto();

    /**
     * Steps execution of a "broken" script by one line. If the current line
     * is a function call, this will step over the function call.
     */
    void StepOver();

	void StepOut();

    /**
     * Continues execution until a breakpoint is hit.
     */
    void Continue();

    /**
     * Breaks execution of the script on the next line executed.
     * thread.
     */
    void Break();

    void ActiveLuaHookInAllVms();

    /**
     * Evalates the expression. If there was an error evaluating the expression the
     * method returns false and the error message is stored in the result.
     */
	EvalResultNode* Evaluate(LAPI api, lua_State* L, const std::string& expression, int stackLevel, int depth);

    /**
     * Toggles a breakpoint on the line on or off.
	 */
	void AddBreakpoint(lua_State* L, unsigned int scriptIndex, unsigned int line, const std::string& expr);
	void DelBreakpoint(lua_State* L, unsigned int scriptIndex, unsigned int line);
    
    void BreakpointsActiveForScript(int scriptIndex);
    
    /**
     * Returns whether any loaded script still have any breakpoints set
     */
    bool GetHaveActiveBreakpoints();

    void SetHaveActiveBreakpoints(bool breakpointsActive);

    void DeleteAllBreakpoints();
    /**
     * Calls the function on the top of the stack in a protected environment that
     * triggers a debugger exception on error.
     */
    int Call(LAPI api, lua_State* L, int nargs, int nresults, int errorfunc) const;

    /**
     * Returns the index of the script in the scripts array with the specified
     * name. The name is the same name that was supplied when the script was
     * loaded.
     */
	size_t GetScriptIndex(const char* name) const;

    bool StackHasBreakpoint(LAPI api, lua_State* L);

    /**
     * Returns the class name associated with the metatable index. This makes
     * a few assumptions, namely that the metatable was associated with a global
     * variable which is the class name (i.e. what luaL_newmetatable does).
     */
    bool GetClassNameForMetatable(LAPI api, lua_State* L, int mt) const;

    /**
     * Pushes onto the stack the class name for a userdata.
     */
    const char* GetClassNameForUserdata(LAPI api, lua_State* L, int ud) const;

    /**
     * Called to register a metatable with a class name. This allows the lookup
     * of class names based on the userdata object's metatable.
     */
    void RegisterClassName(LAPI api, lua_State* L, const char* name, int metaTable);

    /**
     * Sends a text message to the front end.
     */
	void Message(const char* message, MessageType type = MessageType_Normal) const;
	void Message(const char* message, size_t size, MessageType type = MessageType_Normal) const;
	void Message(MessageType type, const char* fmt, ...) const;

    /**
     * Ignores the specified exception whenever it occurs.
     */
    void IgnoreException(const std::string& message);

    /**
     * Returns true if the specified exception is set to be ignored.
     */
    bool GetIsExceptionIgnored(const std::string& message) const;

    /**
     * Callback from Lua when a debug event (new line, function enter or exit)
     * occurs.
     */
    void HookCallback(LAPI api, lua_State* L, lua_Debug* ar);

	void SetAllHookModeLazy(HookMode mode);

    /**
     * Called when a new API is created.
     */
    void CreateApi(LAPI api);

    /**
     * Enables or disable just-in-time compilation for the state. If LuaJIT is not being used
     * this has no effect and returns false. If JIT was successfully enabled or disabled the 
     * function returns true.
     */
    bool EnableJit(LAPI api, lua_State* L, bool enable) const;

	/**
	 * Load emmy!
	 */
	void InitEmmy(LAPI api, lua_State* L) const;

	void RegisterEmmyLibrary(LAPI api, lua_State* L) const;

	/**
	* Gets the value at location n on the stack as text. If expandTable is true
	* then tables will be returned in their expanded form (i.e. "{ ... }")
	*/
	StackLuaObjectNode* GetValueAsText(LAPI api, lua_State* L, int n, int maxDepth = 10, const char* typeNameOverride = nullptr, bool displayAsKey = false, bool askEmmy = true) const;

	/**
	* Returns the index of the API that the VM was created in.
	*/
	LAPI GetApiForVm(lua_State* L) const;

	void HandleMessage(DebugMessage* message);

	void BeginProfiler();

	void ProcProfiler(LAPI api, lua_State* L, lua_Debug* ar);

	LPFunctionCall* GetProfilerCall(const char* source, const char* name, int line);

	void EndProfiler();

	/**
	* Returns true if a debugger is currently attached to our process, or false
	* if otherwise.
	*/
	bool GetIsAttached() const;
private:
	class Breakpoint
	{
	public:
		unsigned int line;
		bool hasCondition;
		std::string condtion;
	};
    struct Script
    {

        /**
         * Returns true if there is a break point on the specified line
         * of the script.
         */
        bool GetHasBreakPoint(unsigned int line) const;

		Breakpoint* GetBreakpoint(unsigned int line);
        
        bool HasBreakPointInRange(unsigned int start, unsigned int end) const;

		void AddBreakpoint(unsigned int line, const std::string & condtion);

		void DelBreakpoint(unsigned int line);

        bool HasBreakpointsActive() const;

        void ClearBreakpoints();

		bool ReadyToSend();

		CodeState                   state;
		bool                        reloading;
		size_t                      index;
		std::string                 name;
		std::string                 fileName;
        std::string                 source;
        std::string                 title;
		std::vector<Breakpoint*>    breakpoints;    // Lines that have breakpoints on them.
        std::vector<unsigned int>   validLines;     // Lines that can have breakpoints on them.

    };

    struct EvaluateData
    {
		lua_State* L;
		LAPI api;
        int             stackLevel;
		int				depth;
        std::string     expression;
		EvalResultNode* result;
    };

    /**
     * Constructor.
     */
    DebugBackend();
        
    /**
     * Destructor.
     */
    ~DebugBackend();

    /**
     * Blocks execution until the the debugger is instructed to continue
     * executing.
     */
    void WaitForContinue();

    /**
     * Breaks from inside the script code. This will block until execution
     * is resumed.
     */
    void BreakFromScript(LAPI api, lua_State* L);

	bool CheckCondition(LAPI api, lua_State* L, Breakpoint* bp);

    /**
     * Error handling call back for relaying exceptions.
     */
    int ErrorHandler(LAPI api, lua_State* L);

    /**
     * Static version of the error handler that just forwards to the non-static
     * version.
     */
    static int StaticErrorHandler(lua_State* L);

    /**
     * Sends a break event to the frontend. The stack will be treated is if it
     * starts at the stackTop entry so that frames on the top of the stack can
     * be skipped. This is usful when the current execution point is an error
     * handler we defined.
     */
	bool SendBreakEvent(LAPI api, lua_State* L, int stackTop = 0);

    /**
     * Sends an exception event to the frontend. Break events should be sent
     * immediately before exception events so that the frontend has access
     * to the call stack.
     */
	bool SendExceptionEvent(lua_State* L, const char* message);

    /**
     * Gets the directory that the DLL is in. The directory ends in a slash.
     */
    bool GetStartupDirectory(char* path, int maxPathLength) const;

    /**
     * Gets the value at location n on the stack as text. If expandTable is true
     * then tables will be returned in their expanded form (i.e. "{ ... }")
     */
	StackNode* GetLuaBindClassValue(LAPI api, lua_State* L, unsigned int maxDepth, bool displayAsKey = false) const;

    /**
     * Gets the table value at location n on the stack as text. Nested tables are
     * not expanded.
     */
	StackTableNode* GetTableAsText(LAPI api, lua_State* L, int t, int maxDepth = 10, const char* typeNameOverride = NULL) const;

    /**
     * Returns true if the name belongs to a Lua internal variable that we
     * should just ignore.
     */
    bool GetIsInternalVariable(const char* name) const;

    /**
     * This is semantically equivalent to luaL_loadstring.
     */
    int LoadScriptWithoutIntercept(LAPI api, lua_State* L, const char* string, size_t size, const char* name) const;

    /**
     * This is semantically equivalent to luaL_loadstring.
     */
    int LoadScriptWithoutIntercept(LAPI api, lua_State* L, const std::string& string) const;
    
    /**
     * Called by the front end once the DLL is finished loading to finish
     * the initialization. This does the major work like hooking the script functions.
     * This function gets used as a thread entry point, so it must have this
     * prototype.
     */
    static DWORD WINAPI FinishInitialize(LPVOID param);

    static const int s_maxModuleNameLength = 32;
    static const int s_maxEntryNameLength  = 256;

	enum Mode
	{
		Mode_Continue,
		Mode_StepOver,
		Mode_StepInto,
		Mode_StepOut,
    };
    
    struct Api
    {
        Api() : IndexChained(nullptr), NewIndexChained(nullptr) { }
        lua_CFunction   IndexChained;
        lua_CFunction   NewIndexChained;
    };

    struct ClassInfo
    {
        lua_State*      L;
        int             metaTableRef;
        std::string     name;
    };

    struct VirtualMachine
    {
        lua_State*      L;
        HANDLE          hThread;
        bool            initialized;
        int             callCount;
        int             callStackDepth;
        int             lastStepLine;
        int             lastStepScript;
        LAPI            api;
        std::string     name;
        unsigned int    stackTop;
        bool            luaJitWorkAround;
        bool            breakpointInStack;
        bool            haveActiveBreakpoints;
        std::string     lastFunctions;
		bool			skipPostLoadScript;
		bool			isEmmyLoaded;
		HookMode        lazyHookMode;
		bool            lazySetHook;
    };

    struct StackEntry
    {
        char            module[s_maxModuleNameLength];
        char            name[s_maxEntryNameLength];
        void*           address;
        unsigned int    scriptIndex;
        unsigned int    line;
		unsigned int	scriptPos;
    };

	struct FrameValue
	{
		std::string name;
		std::string value;
		std::string typeName;
		unsigned int type;
	};

    /**
     * Waits for the specified event or the detached event.
     */
    void WaitForEvent(HANDLE hEvent) const;

    /**
     * Converts from a string with (possibly) embedded zeros to an ASCII string. If force
     * is true, the function will force a conversion from wide character to ASCII. Otherwise
     * it makes the decision about how to convert it based on the content. If the string was
     * converted from a multi-byte character string, the wide parameter is set to true.
     */
    std::string GetAsciiString(const void* buffer, size_t length, bool& wide, bool force = false) const;

    /**
     * Creates an environment for the specified level of the stack. This table has all of the locals,
     * up values and globals for the function's scope. If the function is successful, the return
     * value is true and the table is placed on the stack. Otherwise the function returns false and
     * nothing is put on the stack.
     */
    bool CreateEnvironment(LAPI api, lua_State* L, int stackLevel, int nilSentinel);

    /**
     * Chains two tables together so that accessing members of a child table that don't exist
     * will then attempt to access them on the parent table.
     */
    void ChainTables(LAPI api, lua_State* L, int child, int parent) const;

    /**
     *
     */
    void CreateChainedTable(LAPI api, lua_State* L, int nilSentinel, int localTable, int upValueTable, int globalTable);

    /**
     * Sets the values of the locals at the specified stack level based on values in the table.
     * Values which are equal to the value stored at the nilSentinel stack index will be converted
     * to nils.
     */
    void SetLocals(LAPI api, lua_State* L, int stackLevel, int localTable, int nilSentinel) const;

    /**
     * Sets the values of the up values at the specified stack level based on values in the table.
     * Values which are equal to the value stored at the nilSentinel stack index will be converted
     * to nils.
     */
    void SetUpValues(LAPI api, lua_State* L, int stackLevel, int upValueTable, int nilSentinel) const;

    /**
     * Sets the function (on the top of the Lua stack) to be called when a Lua
     * object is garbage collected.
     */
    void SetGarbageCollectionCallback(LAPI api, lua_State* L, int index) const;

    /**
     * Creates a new table with weak keys or values (specified by setting the type as "k" or "v").
     */
    void CreateWeakTable(LAPI api, lua_State* L, const char* type) const;

    /**
     * Callback when a thread is garbage collected.
     */
    static int ThreadEndCallback(lua_State* L);

    /**
     * Calls the function on the top of the stack when the garbage collector runs.
     * The function is popped from the stack.
     */
    void CreateGarbageCollectionSentinel(LAPI api, lua_State* L) const;

    /**
     * Used by the mechism to setup a garbage collection callback. This function is used
     * internally as a callback and checks if the object is still alive or not.
     */
    static int ObjectCollectionCallback(lua_State* L);
    
	static int IndexChained(lua_State* L);
	static int IndexChained_intercept(lua_State* L);
	static int NewIndexChained(lua_State* L);
	static int NewIndexChained_intercept(lua_State* L);

    /**
     * Returns the end part of a file name.
     */
    void GetFileTitle(const char* name, std::string& title) const;

    /**
     * Logs information about a hook callback event. This is used for debugging.
     */
    void LogHookEvent(LAPI api, lua_State* L, lua_Debug* ar);

    void UpdateHookMode(LAPI api, lua_State* L, lua_Debug* hookEvent);

    /**
     * Calls the named meta-method for the specified value. If the value does
     * not have a meta-table or the named meta-method, the function returns false.
     * Otherwise the result of calling the method is stored in the result parameter
     * (same return as lua_pcall).
     */
    bool CallMetaMethod(LAPI api, lua_State* L, int valueIndex, const char* method, int numResults, int& result) const;

    /**
     * Gets the current C/C++ call stack.
     */
    unsigned int GetCStack(HANDLE hThread, StackEntry stack[], unsigned int maxStackSize) const;

    /**
     * Creates a new table on the top of the stack which is the result of merging
     * the two specified tables.
     */
    void MergeTables(LAPI api, lua_State* L, unsigned int tableIndex1, unsigned int tableIndex2) const;

    /**
     * Gets the number of functions on the Lua stack.
     */
    int GetStackDepth(LAPI api, lua_State* L) const;

    /**
     * Returns the virtual machine that corresponds to the specified Lua state.
     * If there isn't one, the method returns NULL.
     */
    VirtualMachine* GetVm(lua_State* L);

    /**
     * Creates a call stack that unifies the native call stack and the script
     * call stack.
     */
    unsigned int GetUnifiedStack(LAPI api,
		const StackEntry nativeStack[],
		unsigned int nativeStackSize,
        const lua_Debug scriptStack[],
		unsigned int scriptStackSize,
        StackEntry unifiedStack[]) const;

	Script* GetScript(size_t index) const;

	void SendScript(lua_State* L, Script* script) const;

	void CheckReload(LAPI api, lua_State* L);

	void Reload(LAPI api, lua_State* L, Script* script);
private:

    typedef stdext::hash_map<lua_State*, VirtualMachine*>   StateToVmMap;
    typedef stdext::hash_map<std::string, size_t>     NameToScriptMap;

    static DebugBackend*            s_instance;
    static const unsigned int       s_maxStackSize  = 100;

    FILE*                           m_log;

    Mode                            m_mode;
    HANDLE                          m_stepEvent;
    HANDLE                          m_loadEvent;
	HANDLE                          m_detachEvent;
	HANDLE							m_evalEvent;
	HANDLE							m_evalResultEvent;
	EvaluateData					m_evalData;

    CriticalSection                 m_criticalSection;
    CriticalSection                 m_breakLock;
	CriticalSection                 m_vmsLock;
	CriticalSection                 m_scriptsLock;

    std::vector<Script*>            m_scripts;
    NameToScriptMap                 m_nameToScript;

    HANDLE                          m_commandThread;

    std::list<ClassInfo>            m_classInfos;
    std::vector<VirtualMachine*>    m_vms;
    StateToVmMap                    m_stateToVm;
    
    mutable CriticalSection         m_exceptionCriticalSection; // Controls access to ignoreExceptions 
    stdext::hash_set<std::string>   m_ignoreExceptions;

    std::vector<Api>                m_apis;

    mutable bool                    m_warnedAboutUserData;

	bool							m_hooked;
	std::string						m_emmyLuaFilePath;
	DebugPipeline*					m_debugPipeline;

	bool                            m_profiler;
	std::hash_map<std::string, LPFunctionCall*> m_profilerCallMap;
	bool                            m_checkReloadNextTime;
};

#endif