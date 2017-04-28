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

#include "DebugBackend.h"
#include "LuaDll.h"
#include "LuaCheckStack.h"
#include "CriticalSectionLock.h"
#include "CriticalSectionTryLock.h"
#include "StlUtility.h"
#include "XmlUtility.h"
#include "DebugHelp.h"

#include <assert.h>
#include <algorithm>
#include <sstream>
#include <EasyHook.h>

DebugBackend* DebugBackend::s_instance = NULL;

extern HINSTANCE g_hInstance;

/**
 * Data structure passed into the MemoryReader function.
 */
struct Memory
{
    const char* buffer;
    size_t      size;
};

/**
 * lua_Reader function used to read from a memory buffer.
 */
const char* MemoryReader(lua_State* L, void* data, size_t* size)
{
    
    Memory* memory = static_cast<Memory*>(data);
    
    if (memory->size > 0)
    {
        *size = memory->size;
        memory->size = 0;
        return memory->buffer;
    }
    else
    {
        return NULL;
    }

}

bool DebugBackend::Script::GetHasBreakPoint(unsigned int line) const
{
    
    for (size_t i = 0; i < breakpoints.size(); i++)
    {
        if(breakpoints[i] == line)
        {
            return true;
        }
    }
    
    return false;
}

bool DebugBackend::Script::HasBreakPointInRange(unsigned int start, unsigned int end) const
{
    
    for (size_t i = 0; i < breakpoints.size(); i++)
    {
        if(breakpoints[i] >= start && breakpoints[i] < end)
        {
            return true;
        }
    }
    
    return false;
}

bool DebugBackend::Script::ToggleBreakpoint(unsigned int line)
{

    std::vector<unsigned int>::iterator result = std::find(breakpoints.begin(), breakpoints.end(), line);

    if (result == breakpoints.end())
    {
        breakpoints.push_back(line);
        return true;
    }
    else
    {
        breakpoints.erase(result);
        return false;
    }
}

void DebugBackend::Script::ClearBreakpoints()
{
    breakpoints.resize(0);
}

bool DebugBackend::Script::HasBreakpointsActive()
{
  return breakpoints.size() != 0;
}

DebugBackend& DebugBackend::Get()
{
    if (s_instance == NULL)
    {
        s_instance = new DebugBackend;
    }
    return *s_instance;
}

void DebugBackend::Destroy()
{
    delete s_instance;
    s_instance = NULL;
}

DebugBackend::DebugBackend()
{
    m_commandThread         = NULL;
    m_stepEvent             = NULL;
    m_loadEvent             = NULL;
    m_detachEvent           = NULL;
    m_mode                  = Mode_Continue;
    m_log                   = NULL;
    m_warnedAboutUserData   = false;
    m_evalEvent             = NULL;
    m_evalResultEvent       = NULL;
}

DebugBackend::~DebugBackend()
{

    // Check if we successfully hooked the functions. If we didn't, send a warning.
	/*if (!GetIsLuaLoaded())
	{
		Message("Warning 1000: Lua functions were not found during debugging session", MessageType_Warning);
	}*/

    if (m_log != NULL)
    {
        fclose(m_log);
        m_log = NULL;
    }

    m_eventChannel.Destroy();
    m_commandChannel.Destroy();

    if (m_commandThread != NULL)
    {
        CloseHandle(m_commandThread);
        m_commandThread = NULL;
    }

    if (m_stepEvent != NULL)
    {
        CloseHandle(m_stepEvent);
        m_stepEvent = NULL;
    }

    if (m_evalEvent != NULL)
    {
        CloseHandle(m_evalEvent);
        m_evalEvent = NULL;
    }

    if (m_evalResultEvent != NULL)
    {
        CloseHandle(m_evalResultEvent);
        m_evalResultEvent = NULL;
    }

    if (m_loadEvent != NULL)
    {
        CloseHandle(m_loadEvent);
        m_loadEvent = NULL;
    }

    if (m_detachEvent != NULL)
    {
        CloseHandle(m_detachEvent);
        m_detachEvent = NULL;
    }

    for (unsigned int i = 0; i < m_scripts.size(); ++i)
    {
        delete m_scripts[i];
    }

    m_scripts.clear();
    m_nameToScript.clear();

}

void DebugBackend::CreateApi(unsigned long apiIndex)
{

    // Make room for the data for this api.
    if (m_apis.size() < apiIndex + 1)
    {
        m_apis.resize(apiIndex + 1);
    }

    assert(m_apis[apiIndex].IndexChained    == NULL);
    assert(m_apis[apiIndex].NewIndexChained == NULL);

    // Create instances of the functions will need to use as callbacks with this API.
    m_apis[apiIndex].IndexChained    = (lua_CFunction)CreateCFunction(apiIndex, IndexChained, IndexChained_intercept);
    m_apis[apiIndex].NewIndexChained = (lua_CFunction)CreateCFunction(apiIndex, NewIndexChained, IndexChained_intercept);

}

void DebugBackend::Log(const char* fmt, ...)
{

    if (m_log == NULL)
    {
        char fileName[_MAX_PATH];
        if (GetStartupDirectory(fileName, _MAX_PATH))
        {
            strcat(fileName, "log.txt");
            m_log = fopen("c:/temp/log.txt", "wt");
        }
    }

    if (m_log != NULL)
    {

        char buffer[1024];

        va_list    ap;

        va_start(ap, fmt);
        _vsnprintf(buffer, 1024, fmt, ap);
        va_end(ap);

        fputs(buffer, m_log);
        fflush(m_log);

    }

}

bool DebugBackend::Initialize(HINSTANCE hInstance)
{

    DWORD processId = GetCurrentProcessId();

    char eventChannelName[256];
    _snprintf(eventChannelName, 256, "Decoda.Event.%x", processId);

    char commandChannelName[256];
    _snprintf(commandChannelName, 256, "Decoda.Command.%x", processId);

    // Open up a communication channel with the debugger that is used to send
    // events back to the frontend.
    if (!m_eventChannel.Connect(eventChannelName))
    {
        return false;
    }

    // Open up a communication channel with the debugger that is used to receive
    // commands from the backend.
    if (!m_commandChannel.Connect(commandChannelName))
    {
        return false;
    }

    // Create the event used to signal when we should stop "breaking"
    // and step to the next line.
    m_stepEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

    // Create the event used to signal when the frontend is finished processing
    // the load of a script.w
    m_loadEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

    // Create the detach event used to signal when the debugger has been detached
    // from our process. Note this event doesn't reset itself automatically.
    m_detachEvent = CreateEvent(NULL, TRUE, FALSE, NULL);

    m_evalEvent = CreateEvent(NULL, FALSE, FALSE, NULL);
    m_evalResultEvent = CreateEvent(NULL, FALSE, FALSE, NULL);

    // Start a new thread to handle the incoming event channel.
    DWORD threadId;
    m_commandThread = CreateThread(NULL, 0, StaticCommandThreadProc, this, 0, &threadId);

    // Give the front end the address of our Initialize function so that
    // it can call it once we're done loading.
    m_eventChannel.WriteUInt32(EventId_Initialize);
    m_eventChannel.WriteSize(reinterpret_cast<size_t>(FinishInitialize));
    m_eventChannel.Flush();

    return true;

}

DebugBackend::VirtualMachine* DebugBackend::AttachState(unsigned long api, lua_State* L)
{

    if (!GetIsAttached())
    {
        return NULL;
    }

    CriticalSectionLock lock(m_criticalSection);

    // Check if the virtual machine is aleady in our list. This happens
    // if we're attaching this virtual machine implicitly through lua_call
    // or lua_pcall.

    StateToVmMap::iterator stateIterator = m_stateToVm.find(L);

    if (stateIterator != m_stateToVm.end())
    {
        return stateIterator->second;
    }

    VirtualMachine* vm = new VirtualMachine;

    vm->L                   = L;
    vm->hThread             = GetCurrentThread();
    vm->initialized         = false;
    vm->callCount           = 0;
    vm->callStackDepth      = 0;
    vm->lastStepLine        = -2;
    vm->lastStepScript      = -1;
    vm->api                 = api;
    vm->stackTop            = 0;
    vm->luaJitWorkAround    = false;
    vm->breakpointInStack   = true;// Force the stack tobe checked when the first script is entered
    vm->haveActiveBreakpoints = true;
    
    m_vms.push_back(vm);
    m_stateToVm.insert(std::make_pair(L, vm));
   
    if (!lua_checkstack_dll(api, L, 3))
    {
        return NULL;
    }

    m_eventChannel.WriteUInt32(EventId_CreateVM);
    m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
    m_eventChannel.Flush();

    // Register the debug API.
    RegisterDebugLibrary(api, L);

    // Start debugging on this VM.
    SetHookMode(api, L, HookMode_Full);

    // This state may be a thread which will be garbage collected, so we need to register
    // to recieve notification when it is destroyed.

	//todo:打开注释在前端主动detach并卸载dll后，线程销毁时会报错
    /*if (lua_pushthread_dll(api, L))
    {

        lua_pushlightuserdata_dll(api, L, L);
        lua_pushcclosure_dll(api, L, ThreadEndCallback, 1);
        
        SetGarbageCollectionCallback(api, L, -2);
        lua_pop_dll(api, L, 1);
    
    }*/

    return vm;

}

void DebugBackend::DetachState(unsigned long api, lua_State* L)
{

    CriticalSectionLock lock1(m_criticalSection);

    // Remove all of the class names associated with this state.

    std::list<ClassInfo>::iterator iterator = m_classInfos.begin();

    while (iterator != m_classInfos.end())
    {
        if (iterator->L == L)
        {
            m_classInfos.erase(iterator++);
        }
        else
        {
            ++iterator;
        }
    }

	//Remove VM
	for (unsigned int i = 0; i < m_vms.size(); ++i)
	{
		VirtualMachine* vm = m_vms[i];
		if (vm->L == L)
		{
			CloseHandle(vm->hThread);
			delete vm;
			m_vms.erase(m_vms.begin() + i);
		}
	}
    // Remove the state from our list.

    StateToVmMap::iterator stateIterator = m_stateToVm.find(L);

    if (stateIterator != m_stateToVm.end())
    {
		m_stateToVm.erase(stateIterator);

        m_eventChannel.WriteUInt32(EventId_DestroyVM);
        m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
        m_eventChannel.Flush();
    }
}

int DebugBackend::PostLoadScript(unsigned long api, int result, lua_State* L, const char* source, size_t size, const char* name)
{

    if (!GetIsAttached())
    {
        return result;
    }

	auto vm = GetVm(L);
	if (vm -> inEval)
	{
		return result;
	}

    bool registered = false;

    // Register the script before dealing with errors, since the front end has enough
    // information to display the error.
    if (RegisterScript(L, source, size, name, false) != -1)
    {
        registered = true;
    }

    if (result != 0)
    {

        {

            // Make sure no other threads are running Lua while we handle the error.
            CriticalSectionLock lock(m_criticalSection);
            CriticalSectionLock lock2(m_breakLock);

            // Get the error mesasge.
            const char* message = lua_tostring_dll(api, L, -1);

            // Stop execution.
            SendBreakEvent(api, L, 1);

            // Send an error event.
            m_eventChannel.WriteUInt32(EventId_LoadError);
            m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
            m_eventChannel.WriteString(message);
            m_eventChannel.Flush();
        
        }

        // Wait for the front-end to tell use to continue.
        WaitForContinue();

    }
    /*
    else
    {

        // Get the valid line numbers for placing breakpoints for this script.

        lua_Debug ar;
        lua_pushvalue_dll(L, -1);
        
        if (lua_getinfo_dll(L, ">L", &ar))
        {

            int lineTable = lua_gettop_dll(L);

            lua_pushnil_dll(L);
            while (lua_next_dll(L, lineTable) != 0)
            {

                int lineNumber = lua_tointeger_dll(L, -2);
                script->validLines.push_back(lineNumber);

                lua_pop_dll(L, 1);

            }

            // Pop the line table.
            lua_pop_dll(L, 1);

        }

        // Sort the valid line numbers for easier/faster processing.
        std::sort(script->validLines.begin(), script->validLines.end());
        
    }
    */

    if (registered)
    {
        // Stop execution so that the frontend has an opportunity to send us the break points
        // before we start executing the first line of the script.
        WaitForEvent(m_loadEvent);
    }

    return result;

}

int DebugBackend::RegisterScript(lua_State* L, const char* source, size_t size, const char* name, bool unavailable)
{

    CriticalSectionLock lock(m_criticalSection);

    bool freeName = false;

    // If no name was specified, use the source as the name. This is similar to what
    // built-in Lua functions like luaL_loadstring do.
    if (name == NULL)
    {
        // Null terminate the source in case it isn't already.
        char* temp = new char[size + 1];
        memcpy(temp, source, size);
        temp[size] = 0;
        name = temp;
        freeName = true;
    }

    // Check that we haven't already assigned this script an index. That happens
    // if the same script is loaded twice by the application.

    if (GetScriptIndex(name) != -1)
    {
        if (freeName)
        {
            delete [] name;
            name = NULL;
        }
        return -1;
    }

    // Since the name can be a file name, and multiple names can map to the same file,
    // extract the file title from the name and compare the code with any matches.

    std::string title;
    GetFileTitle(name, title);

    for (unsigned int i = 0; i < m_scripts.size(); ++i)
    {   
        if (m_scripts[i]->title == title)
        {
            // Check that the source matches.
            if (m_scripts[i]->source == std::string(source, size))
            {
                // Record the script index under this other name.
                m_nameToScript.insert(std::make_pair(name, i));
                if (freeName)
                {
                    delete [] name;
                    name = NULL;
                }
                return -1;
            }
        }
    }
    
    Script* script = new Script;
    script->name    = name;
    script->title   = title;

    if (size > 0 && source != NULL)
    {
        script->source = std::string(source, size);
    }
    
    unsigned int scriptIndex = m_scripts.size();
    m_scripts.push_back(script);

    m_nameToScript.insert(std::make_pair(name, scriptIndex));

    std::string fileName;

    size_t length = strlen(name);

    // Check if the file name is actually the source. This happens when calling
    // luaL_loadstring and doesn't make for a very good display.
    /*if (source != NULL && strncmp(name, source, length) == 0)
    {
        char buffer[32];
        sprintf(buffer, "@Untitled%d.lua", scriptIndex + 1);
        fileName = buffer;
    }
    else*/
    {
    
        fileName = name;

        // Remove the @ sign in front of file names when we pass it to the UI.
        if (fileName[0] == '@')
        {
            fileName.erase(0, 1);
        }

    }

    CodeState state = CodeState_Normal;

    if (unavailable)
    {
        state = CodeState_Unavailable;
    }

    // Check if this is a compiled/binary file.
    if (source != NULL && size >= 4)
    {
        if (source[0] >= 27 && source[0] <= 33 && memcmp(source + 1, "Lua", 3) == 0)
        {
            state = CodeState_Binary;
            source = NULL;
        }
    }

    m_eventChannel.WriteUInt32(EventId_LoadScript);
    m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
    m_eventChannel.WriteString(fileName);
    m_eventChannel.WriteString(script->source);

    m_eventChannel.WriteUInt32(state);
    m_eventChannel.Flush();

    if (freeName)
    {
        delete [] name;
        name = NULL;
    }

    return scriptIndex;

}

int DebugBackend::RegisterScript(unsigned long api, lua_State* L, lua_Debug* ar)
{
    const char* arsource = GetSource( api, ar);
    const char* source = NULL;
    size_t size = 0;
  
    if (arsource != NULL && arsource[0] != '@')
    {
        source = arsource;
        size   = strlen(source);
    }
  
    int scriptIndex = RegisterScript(L, source, size, arsource, source == NULL);
  
    // We need to exit the critical section before waiting so that we don't
    // monopolize it. Specifically, ToggleBreakpoint will need it.
    m_criticalSection.Exit();
  
    if (scriptIndex != -1)
    {
        // Stop execution so that the frontend has an opportunity to send us the break points
        // before we start executing the first line of the script.
        WaitForEvent(m_loadEvent);
    }
  
    m_criticalSection.Enter();
  
    // Since the script indices may have changed while we released the critical section,
    // require the script index.
    return GetScriptIndex(arsource);
}

void DebugBackend::Message(const char* message, MessageType type)
{
    // Send a message.
    m_eventChannel.WriteUInt32(EventId_Message);
    m_eventChannel.WriteUInt32(0);
    m_eventChannel.WriteUInt32(type);
    m_eventChannel.WriteString(message);
    m_eventChannel.Flush();
}

void DebugBackend::HookCallback(unsigned long api, lua_State* L, lua_Debug* ar)
{

    m_criticalSection.Enter(); 
   
    if (!lua_checkstack_dll(api, L, 2))
    {
        m_criticalSection.Exit();
        return;
    }

    // Note this executes in the thread of the script being debugged,
    // not our debugger, so we can block.

    VirtualMachine* vm = NULL;
    StateToVmMap::const_iterator iterator = m_stateToVm.find(L);

    if (iterator == m_stateToVm.end())
    {
        // If somehow a thread was started without us intercepting the
        // lua_newthread call, we can reach this point. If so, attach
        // to the VM.
        vm = AttachState(api, L);
    }
    else
    {
        vm = iterator->second;
    }

	//TODO: 同一个L在不同的lua代码里被调用?
	//assert(vm->api == api);
	if (vm->api != api) {
		api = vm->api;
	}

    if (!vm->initialized && GetEvent(api, ar) == LUA_HOOKLINE)
    {
            
        // We do this initialization work here since we check for things that
        // are registered after the state is created.

        // Check if we need to use the LuaJIT work around for the debug API.

        lua_rawgetglobal_dll(api, L, "jit");
        int jitTable = lua_gettop_dll(api, L);

        if (!lua_isnil_dll(api, L, -1))
        {
            
            lua_pushstring_dll(api, L, "version_num");
            lua_gettable_dll(api, L, jitTable);

            int version = lua_tointeger_dll(api, L, -1);
            if (version >= 20000)
            {
                vm->luaJitWorkAround = true;
                Message("Warning 1009: Enabling LuaJIT C call return work-around", MessageType_Warning);
            }

            lua_pop_dll(api, L, 1);

        }

        lua_pop_dll(api, L, 1);

        vm->initialized = true;

    }

    // Get the name of the VM. Polling like this is pretty excessive since the
    // name won't change often, but it's the easiest way and works fine.

    lua_rawgetglobal_dll(api, L, "decoda_name");
    const char* name = lua_tostring_dll(api, L, -1);

    if (name == NULL)
    {
        name = "";
    }

    if (name != vm->name)
    {
        vm->name = name;
        m_eventChannel.WriteUInt32(EventId_NameVM);
        m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
        m_eventChannel.WriteString(vm->name);
    }

    lua_pop_dll(api, L, 1);

    // Log for debugging.
    //LogHookEvent(api, L, ar);

    //Only try to downgrade the hook when the debugger is not stepping   
    if(m_mode == Mode_Continue)
    {
        UpdateHookMode(api, L, ar);
    }
    else
    {
        if(GetHookMode(api, L) != HookMode_Full)
        {
            SetHookMode(api, L, HookMode_Full);
        }
        
        //Force UpdateHookMode to recheck the call stack for functions with breakpoints when switching back to Mode_Continue
        vm->breakpointInStack = true;
    }

    int arevent = GetEvent(api, ar);
    if (arevent == LUA_HOOKLINE)
    {

        // Fill in the rest of the structure.
        lua_getinfo_dll(api, L, "Sl", ar);
        const char* arsource = GetSource(api, ar);
        int scriptIndex = GetScriptIndex(arsource);

        if (scriptIndex == -1)
        {
            // This isn't a script we've seen before, so tell the debugger about it.
            scriptIndex = RegisterScript( api, L, ar);
        }

        bool stop = false;
        bool onLastStepLine = false;

        //Keep updating onLastStepLine even if the mode is Mode_Continue if were still on the same line so we don't trigger
        if (vm->luaJitWorkAround)
        {    
            int stackDepth = GetStackDepth(api, L);

            //We will get multiple line events for the same line in LuaJIT if there are only calls to C functions on the line 
            if (vm->lastStepLine == GetCurrentLine(api, ar))
            {
                onLastStepLine = vm->lastStepScript == scriptIndex && vm->callStackDepth != 0 && stackDepth == vm->callStackDepth;
            }
            
            // If we're stepping on each line or we just stepped out of a function that
            // we were stepping over, break.
            if (m_mode == Mode_StepOver && vm->callStackDepth > 0)
            {
                if (stackDepth < vm->callStackDepth || (stackDepth == vm->callStackDepth && !onLastStepLine))
                {
                    // We've returned to the level when the function was called.
                    vm->callCount       = 0;   
                    vm->callStackDepth  = 0;
                }
            }
        }

        if (scriptIndex != -1)
        {
            // Check to see if we're on a breakpoint and should break.
            if (!onLastStepLine && m_scripts[scriptIndex]->GetHasBreakPoint(GetCurrentLine(api, ar) - 1))
            {
                stop = true;
            }
        } 
        
        //Break if were doing some kind of stepping 
        if (!onLastStepLine && (m_mode == Mode_StepInto || (m_mode == Mode_StepOver && vm->callCount == 0)))
        {
            stop = true;
        }
       
        // We need to exit the critical section before waiting so that we don't
        // monopolize it.
        m_criticalSection.Exit();

        if (stop)
        {
            BreakFromScript(api, L);
            
            if(vm->luaJitWorkAround)
            {
                vm->callStackDepth = GetStackDepth(api, L);
                vm->lastStepLine = GetCurrentLine(api, ar);
                //TODO:这个的用意是什么？如果不注释会导致二次断点失败
				//vm->lastStepScript = scriptIndex;
            }
        }

    }
    else
    {
        if (m_mode == Mode_StepOver)
        {
            if (GetIsHookEventRet( api, arevent)) // only LUA_HOOKRET for Lua 5.2, can also be LUA_HOOKTAILRET for older versions
            {
                if (vm->callCount > 0)
                {
                    --vm->callCount;
                }
            }
            else if( GetIsHookEventCall( api, arevent)) // only LUA_HOOKCALL for Lua 5.1, can also be LUA_HOOKTAILCALL for newer versions
            {
                if (m_mode == Mode_StepOver)
                {
                    ++vm->callCount;
                }
            }
        }

        m_criticalSection.Exit(); 
    
    }

}

void DebugBackend::UpdateHookMode(unsigned long api, lua_State* L, lua_Debug* hookEvent)
{
    int arevent = GetEvent(api, hookEvent);
    //Only update the hook mode for call or return hook events 
    if (arevent == LUA_HOOKLINE)
    {
        return;
    }

    VirtualMachine* vm = GetVm(L);
    HookMode mode = HookMode_CallsOnly;

    // Populate the line number and source name debug fields
    lua_getinfo_dll(api, L, "S", hookEvent);
    int linedefined = GetLineDefined(api, hookEvent);

    if( GetIsHookEventCall( api, arevent) && linedefined != -1)
    {
        vm->lastFunctions = GetSource(api, hookEvent);

        int scriptIndex = GetScriptIndex(vm->lastFunctions.c_str());

        if (scriptIndex == -1)
        {
            RegisterScript(api, L, hookEvent);
            scriptIndex = GetScriptIndex(vm->lastFunctions.c_str());
        }

        Script* script = scriptIndex != -1 ? m_scripts[scriptIndex] : NULL;

        int lastlinedefined = GetLastLineDefined( api, hookEvent);
        if(script != NULL && (script->HasBreakPointInRange(linedefined, lastlinedefined) ||
           //Check if the function is the top level chunk of a script because they always have there lastlinedefined set to 0                  
           (script->HasBreakpointsActive() && linedefined == 0 && lastlinedefined == 0)))
        {
            mode = HookMode_Full;
            vm->breakpointInStack = true;
        }
    }

    //Keep the hook in Full mode while theres a function in the stack somewhere that has a breakpoint in it
    if(mode != HookMode_Full && vm->breakpointInStack)
    {
      if(StackHasBreakpoint(api, L))
      {
          mode = HookMode_Full;
      }
      else
      {
          vm->breakpointInStack = false;
      }
    }

    HookMode currentMode = GetHookMode(api, L);

    if(!vm->haveActiveBreakpoints)
    {
        mode = HookMode_None;
    }

    if(currentMode != mode)
    {
        //Always switch to Full hook mode when stepping
        if(m_mode != Mode_Continue)
        {
            mode = HookMode_Full;
        }
        SetHookMode(api, L, mode);
    }
}

bool DebugBackend::StackHasBreakpoint(unsigned long api, lua_State* L)
{
	lua_Debug functionInfo = { 0 };
    VirtualMachine* vm = GetVm(L);
	int stackIndex = 0;
    while (true)
    {
		int status = lua_getstack_dll(api, L, stackIndex, &functionInfo);
		if (status != 1)
			break;

		stackIndex = stackIndex + 1;
        lua_getinfo_dll(api, L, "S", &functionInfo);

        int linedefined = GetLineDefined( api, &functionInfo);
        if (linedefined == -1)
        {
            //ignore c functions
            continue;
        }

        vm->lastFunctions = GetSource( api, &functionInfo);

        int scriptIndex = GetScriptIndex(vm->lastFunctions.c_str());
        
        Script* script = scriptIndex != -1 ? m_scripts[scriptIndex] : NULL;

        int lastlinedefined = GetLastLineDefined( api, &functionInfo);
        if(script != NULL && (script->HasBreakPointInRange(linedefined, lastlinedefined) ||
           //Check if the function is the top level chunk of a source file                       
           (script->HasBreakpointsActive() && linedefined == 0 && lastlinedefined == 0)))
        {
            return true;
        }

    }

    return false;            
}

int DebugBackend::GetScriptIndex(const char* name) const
{
    if (name == NULL) 
    {
        return -1;
    }

    NameToScriptMap::const_iterator iterator = m_nameToScript.find(name);

    if (iterator == m_nameToScript.end())
    {
        return -1;
    }

    return iterator->second;

}

void DebugBackend::WaitForContinue()
{
    // Wait until the UI to tell us to step to the next line.
    HANDLE hEvents[] = { m_stepEvent, m_detachEvent, m_evalEvent };
    while (true)
    {
        DWORD ret = WaitForMultipleObjects(3, hEvents, FALSE, INFINITE);
        DWORD index = ret - WAIT_OBJECT_0;
        if (index == 2)
        {
            m_evalData.success = Evaluate(
                m_evalData.api,
                m_evalData.L,
                m_evalData.expression,
                m_evalData.stackLevel,
                m_evalData.depth,
                m_evalData.result);
            SetEvent(m_evalResultEvent);
        } else break;
    }
}

void DebugBackend::WaitForEvent(HANDLE hEvent)
{
    HANDLE hEvents[] = { hEvent, m_detachEvent };
    WaitForMultipleObjects(2, hEvents, FALSE, INFINITE);
}

bool DebugBackend::GetIsAttached() const
{
    return WaitForSingleObject(m_detachEvent, 0) != WAIT_OBJECT_0;
}

void DebugBackend::CommandThreadProc()
{

    unsigned int commandId;

    bool continueRunning = false;

    while (m_commandChannel.ReadUInt32(commandId))
    {

        if (commandId == CommandId_Detach)
        {
            
            m_commandChannel.ReadBool(continueRunning);

            // Detach the hook function from all of the script virtual machines.

            CriticalSectionLock lock(m_criticalSection);

            for (unsigned int i = 0; i < m_vms.size(); ++i)
            {
                SetHookMode(m_vms[i]->api, m_vms[i]->L, HookMode_None);
            }
            LhUninstallAllHooks();

            // Signal that we're detached.
            SetEvent(m_detachEvent);

            // Note, we don't remove the vms here since they will be removed when
            // lua_close is called by the host.

            // If we're supposed to continue running the application after detaching,
            // set the step event so that we don't stay broken forever.
            if (continueRunning)
            {
                SetEvent(m_stepEvent);
                SetEvent(m_loadEvent);
            }
            else
            {
                break;
            }
        }
        else if (commandId == CommandId_IgnoreException)
        {
            std::string message;
            m_commandChannel.ReadString(message);
            IgnoreException(message);
        }
        else
        {

            size_t vm;
            m_commandChannel.ReadSize(vm);

            lua_State* L = reinterpret_cast<lua_State*>(vm);

            switch (commandId)
            {
            case CommandId_Continue:
                Continue();
                break;
            case CommandId_StepOver:
                StepOver();
                break;
            case CommandId_StepInto:
                StepInto();
                break;
            case CommandId_DeleteAllBreakpoints:
                DeleteAllBreakpoints();
                break;
            case CommandId_ToggleBreakpoint:
                {
                    
                    unsigned int scriptIndex;
                    unsigned int line;

                    m_commandChannel.ReadUInt32(scriptIndex);
                    m_commandChannel.ReadUInt32(line);
                    
                    ToggleBreakpoint(L, scriptIndex, line);
                
                }
                break;
            case CommandId_Break:
                Break();
                break;
            case CommandId_Evaluate:
                {

                    std::string expression;
                    m_commandChannel.ReadString(expression);

                    unsigned int stackLevel;
                    m_commandChannel.ReadUInt32(stackLevel);
                    unsigned int depth;
                    m_commandChannel.ReadUInt32(depth);

                    unsigned long api = GetApiForVm(L);

                    m_evalData.expression = expression;
                    m_evalData.stackLevel = stackLevel;
                    m_evalData.depth = depth;
                    m_evalData.api = api;
                    m_evalData.L = L;
                    m_evalData.success = false;
                    m_evalData.result = "";

                    if (api != -1)
                    {
                        SetEvent(m_evalEvent);
                        WaitForEvent(m_evalResultEvent);
                    }
                    
                    m_commandChannel.WriteUInt32(m_evalData.success);
                    m_commandChannel.WriteString(m_evalData.result);
                    m_commandChannel.Flush();

                }
                break;
            case CommandId_LoadDone:
                SetEvent(m_loadEvent);
                break;

            }

        }

    }

    // Cleanup.

    m_classInfos.clear();

    for (unsigned int i = 0; i < m_scripts.size(); ++i)
    {
        delete m_scripts[i];
    }

    m_nameToScript.clear();

    m_scripts.clear();
    ClearVector(m_vms);
    m_stateToVm.clear();

    m_eventChannel.Destroy();
    m_commandChannel.Destroy();

    //Free
    Destroy();
    FreeLuaDll();
}

DWORD WINAPI DebugBackend::StaticCommandThreadProc(LPVOID param)
{
    DebugBackend* self = static_cast<DebugBackend*>(param);
    self->CommandThreadProc();
    return 0;
}

void DebugBackend::ActiveLuaHookInAllVms()
{
    StateToVmMap::iterator end = m_stateToVm.end();

    for (StateToVmMap::iterator it = m_stateToVm.begin(); it != end; it++)
    {
      VirtualMachine* vm = it->second;
      //May have issues with L not being the currently running thread
      SetHookMode(vm->api, vm->L, HookMode_Full);
    }
}

void DebugBackend::StepInto()
{
    
    CriticalSectionLock lock(m_criticalSection);
    
    for (unsigned int i = 0; i < m_vms.size(); ++i)
    {
        m_vms[i]->callCount = 0;
    }

    m_mode = Mode_StepInto;
    SetEvent(m_stepEvent);

    ActiveLuaHookInAllVms();
}

void DebugBackend::StepOver()
{

    CriticalSectionLock lock(m_criticalSection);
    
    for (unsigned int i = 0; i < m_vms.size(); ++i)
    {
        m_vms[i]->callCount = 0;
    }

    m_mode = Mode_StepOver;
    SetEvent(m_stepEvent);

    ActiveLuaHookInAllVms();
}


void DebugBackend::Continue()
{
    
    CriticalSectionLock lock(m_criticalSection);
    
    for (unsigned int i = 0; i < m_vms.size(); ++i)
    {
        m_vms[i]->callCount = 0;
    }

    m_mode = Mode_Continue;
    SetEvent(m_stepEvent);

}

void DebugBackend::Break()
{
    m_mode = Mode_StepInto;
    ActiveLuaHookInAllVms();
}

void DebugBackend::ToggleBreakpoint(lua_State* L, unsigned int scriptIndex, unsigned int line)
{

    assert(GetIsLuaLoaded());

    CriticalSectionLock lock(m_criticalSection);

    Script* script = m_scripts[scriptIndex];

    // Move the line to the next line after the one the user specified that is
    // valid for a breakpoint.

    bool foundValidLine = true;

    // Disabled since right now we are only generating valid lines at file scope.
    /*
    for (unsigned int i = 0; i < script->validLines.size() && !foundValidLine; ++i)
    {
        if (script->validLines[i] >= line)
        {
            line = script->validLines[i];
            foundValidLine = true;
        }
    }
    */

    if (foundValidLine)
    {
        
        bool breakpointSet = script->ToggleBreakpoint(line);

        if(breakpointSet)
        {
            BreakpointsActiveForScript(scriptIndex);
        }
        else
        {
            //Check to see if this was the last active breakpoint set if so switch back to fast mode
			//TODO:暂时去除优化，这会导致HookMode_None
			/*if(!GetHaveActiveBreakpoints())
			{
				for(StateToVmMap::iterator it = m_stateToVm.begin(); it != m_stateToVm.end(); it++)
				{
					it->second->haveActiveBreakpoints = false;
				}
			}*/
        }

        // Send back the event telling the frontend that we set/unset the breakpoint.
        m_eventChannel.WriteUInt32(EventId_SetBreakpoint);
        m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
        m_eventChannel.WriteUInt32(scriptIndex);
        m_eventChannel.WriteUInt32(line);
        m_eventChannel.WriteUInt32(breakpointSet);
        m_eventChannel.Flush();
    
    }

}

void DebugBackend::BreakpointsActiveForScript(int scriptIndex)
{
    //TODO this per VM
    SetHaveActiveBreakpoints(true);
}

bool DebugBackend::GetHaveActiveBreakpoints(){

    for(std::vector<Script*>::iterator it = m_scripts.begin(); it != m_scripts.end(); it++)
    {
        if((*it)->HasBreakpointsActive())
        {
            return true;
        } 
    }

    return false;
}

void DebugBackend::SetHaveActiveBreakpoints(bool breakpointsActive)
{

    //m_HookLock.Enter();

    for(StateToVmMap::iterator it = m_stateToVm.begin(); it != m_stateToVm.end(); it++)
    {
        it->second->haveActiveBreakpoints = breakpointsActive;
    }
  
    //We defer to UpdateHookMode to turn off the hook fully
    if(breakpointsActive)
    {
        ActiveLuaHookInAllVms();
    }
}

void DebugBackend::DeleteAllBreakpoints(){

    for(std::vector<Script*>::iterator it = m_scripts.begin(); it != m_scripts.end(); it++)
    {
        (*it)->breakpoints.clear();
    }

    //Set all haveActiveBreakpoints for the vms back to false we leave to the hook being called for the vm
    SetHaveActiveBreakpoints(false);
}

void DebugBackend::SendBreakEvent(unsigned long api, lua_State* L, int stackTop)
{

    CriticalSectionLock lock(m_criticalSection);

    VirtualMachine* vm = GetVm(L);

    // The C call stack will look something like this (may be any number of
    // these stacked on top of each other):
    //
    //   +------+
    //   |      | C function
    //   +------+
    //   |      |
    //   | XXXX | Lua call mechanism (luaD_*, luaV_*, lua_*)
    //   |      |
    //   +------+
    //   | XXXX | lua_call/lua_pcall/lua_load, etc.
    //   +------+
    //   |      |
    //   |      | Pre-Lua code (main, etc.)
    //   |      |
    //   +------+

    StackEntry nativeStack[100];
    unsigned int nativeStackSize = 0;

    if (vm != NULL)
    {
        // Remember how many stack levels to skip so when we evaluate we can adjust
        // the stack level accordingly.
        vm->stackTop = stackTop;
        nativeStackSize = GetCStack(vm->hThread, nativeStack, 100);
    }
    else
    {
        // For whatever reason we couldn't remember how many stack levels we want to skip,
        // so don't skip any. This shouldn't happen under any normal circumstance since
        // the state should have a corresponding VM.
        stackTop = 0;
    }
    // Send the call stack.

    TiXmlDocument document;
    TiXmlElement* stacksNode = new TiXmlElement("stacks");
    lua_Debug scriptStack[s_maxStackSize];
    unsigned int scriptStackSize = 0;

    for (int level = stackTop; scriptStackSize < s_maxStackSize && lua_getstack_dll(api, L, level, &scriptStack[scriptStackSize]); ++level)
    {
        lua_Debug& ar = scriptStack[scriptStackSize];
        lua_getinfo_dll(api, L, "nSlu", &scriptStack[scriptStackSize]);
        
        TiXmlElement* stackNode = new TiXmlElement("stack");
        stackNode->SetAttribute("script_index", GetScriptIndex(GetSource(api, &ar)));
        stackNode->SetAttribute("line", GetCurrentLine(api, &ar) - 1);
        const char* functionName = GetName(api, &ar);
        if (functionName == NULL)
            functionName = GetWhat(api, &ar);
        if (functionName == NULL)
            functionName = "<unknown>";
        stackNode->SetAttribute("function", functionName);
        stacksNode->LinkEndChild(stackNode);
        const char *name;
        int j = 1;
        while ((name = lua_getlocal_dll(api, L, &ar, j++)) != NULL) {
            if (!GetIsInternalVariable(name)) {
                TiXmlNode* valueNode = GetValueAsText(api, L, -1, 1);
                valueNode->LinkEndChild(WriteXmlNode("name", name));
                stackNode->LinkEndChild(valueNode);
            }
            lua_pop_dll(api, L, 1);  /* remove variable value */
        }
        lua_getinfo_dll(api, L, "f", &ar);  /* retrieves function */
        j = 1;
        while ((name = lua_getupvalue_dll(api, L, -1, j++)) != NULL) {
            if (!GetIsInternalVariable(name)) {
                TiXmlNode* valueNode = GetValueAsText(api, L, -1, 1);
                valueNode->LinkEndChild(WriteXmlNode("name", name));
                stackNode->LinkEndChild(valueNode);
            }
            lua_pop_dll(api, L, 1);  /* remove upvalue value */
        }

        ++scriptStackSize;
    }

    TiXmlPrinter p;
    document.LinkEndChild(stacksNode);
    document.Accept(&p);
    auto xml = p.Str();
    
    m_eventChannel.WriteUInt32(EventId_Break);
    m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
    m_eventChannel.WriteString(xml);
    m_eventChannel.Flush();

}

void DebugBackend::SendExceptionEvent(lua_State* L, const char* message)
{
    m_eventChannel.WriteUInt32(EventId_Exception);
    m_eventChannel.WriteSize(reinterpret_cast<size_t>(L));
    m_eventChannel.WriteString(message);
    m_eventChannel.Flush();
}

void DebugBackend::BreakFromScript(unsigned long api, lua_State* L)
{
    CriticalSectionLock lock(m_breakLock);

    SendBreakEvent(api, L);
    WaitForContinue();        
}

int DebugBackend::Call(unsigned long api, lua_State* L, int nargs, int nresults, int errorfunc)
{

    // Check it's not our error handler that's getting called (happens when there's an
    // error). We also need to check that our error handler is not the error function,
    // since that can happen if one of the Lua interfaces we hooked calls another one
    // internally.
    if (lua_tocfunction_dll(api, L, -1) == StaticErrorHandler || (errorfunc != 0 && lua_tocfunction_dll(api, L, errorfunc) == StaticErrorHandler))
    {
        return lua_pcall_dll(api, L, nargs, nresults, errorfunc);
    }
    else
    {

        int result = 0;

        if (lua_gettop_dll(api, L) >= nargs + 1)
        {
            
            // Push our error handler onto the stack before the function and the arguments.
            if (errorfunc != 0)
            {
                lua_pushvalue_dll(api, L, errorfunc);
            }
            else
            {
                lua_pushnil_dll(api, L);
            }
            lua_pushcclosure_dll(api, L, StaticErrorHandler, 1);

            int errorHandler = lua_gettop_dll(api, L) - (nargs + 1);
            lua_insert_dll(api, L, errorHandler);

            // Invoke the function
            result = lua_pcall_dll(api, L, nargs, nresults, errorHandler);

            // Remove our error handler from the stack.
            lua_remove_dll(api, L, errorHandler);
        
        }
        else
        {
            // In this case there wasn't a function on the top of the stack, so don't push our
            // error handler there since it will get called instead.
            result = lua_pcall_dll(api, L, nargs, nresults, errorfunc);
        }

        return result;

    }

}

int DebugBackend::StaticErrorHandler(lua_State* L)
{
    unsigned long api = s_instance->GetApiForVm(L);
    return s_instance->ErrorHandler(api, L);
}

int DebugBackend::ErrorHandler(unsigned long api, lua_State* L)
{

    int top = lua_gettop_dll(api, L);

    // Get the error mesasge.
    const char* message = lua_tostring_dll(api, L, -1);

    if (message == NULL)
    {
        message = "No error message available";
    }

    if (!GetIsExceptionIgnored(message))
    {

        // Send the exception event. Ignore the top of the stack when we send the
        // call stack since the top of the call stack is this function.

        // Sometimes the break lock will already be held.  For example, consider
        // the following sequence of events:
        //   1) The main lua thread is stopped in the debugger, holding the
        //      break lock in DebugBackend::BreakFromScript.
        //   2) The decoda thread tries to evaluate a variable from the watch
        //      window
        //   3) The evaluation of __towatch or __tostring calls lua_error()
        // That will lead us right here, unable to grab the break lock.  To avoid
        // deadlocking in this case, just send an error message.

        CriticalSectionTryLock lock(m_breakLock);
        if (lock.IsHeld()) 
        {
            SendBreakEvent(api, L, 1);
            SendExceptionEvent(L, message);
            WaitForContinue();
        } 
        else 
        {
            Message(message, MessageType_Error);
        }

    }
        
    // Try invoking the user specified error function.

    lua_pushvalue_dll(api, L, lua_upvalueindex_dll(api, 1));

    if (!lua_isnil_dll(api, L, -1))
    {
        lua_pushvalue_dll(api, L, -2);
        lua_pcall_dll(api, L, 1, 1, 0);
    }
    else
    {
        lua_pop_dll(api, L, 1);
        // Push the original message back as the return so that it will be available
        // to custom error handlers.
        lua_pushvalue_dll(api, L, -1);
    }
        
    return 1;

}

bool DebugBackend::GetStartupDirectory(char* path, int maxPathLength)
{

    if (!GetModuleFileName(g_hInstance, path, maxPathLength))
    {
        return false;
    }

    char* lastSlash = strrchr(path, '\\');

    if (lastSlash == NULL)
    {
        return false;
    }

    // Terminate the path after the last slash.

    lastSlash[1] = 0;
    return true;

}

void DebugBackend::ChainTables(unsigned long api, lua_State* L, int child, int parent)
{
    
    int t1 = lua_gettop_dll(api, L);

    // Create the metatable to link them.
    lua_newtable_dll(api, L);
    int metaTable = lua_gettop_dll(api, L);

    // Set the __index metamethod to point to the parent table.
    lua_pushstring_dll(api, L, "__index");
    lua_pushvalue_dll(api, L, parent);
    lua_rawset_dll(api, L, metaTable);

    // Set the __newindex metamethod to point to the parent table.
    lua_pushstring_dll(api, L, "__newindex");
    lua_pushvalue_dll(api, L, parent);
    lua_rawset_dll(api, L, metaTable);

    // Set the child's metatable.
    lua_setmetatable_dll(api, L, child);

    int t2 = lua_gettop_dll(api, L);
    assert(t1 == t2);

}

bool DebugBackend::CreateEnvironment(unsigned long api, lua_State* L, int stackLevel, int nilSentinel)
{

    int t1 = lua_gettop_dll(api, L);

    lua_Debug stackEntry = { 0 };

    if (lua_getstack_dll(api, L, stackLevel, &stackEntry) != 1)
    {
        return false;
    }

    const char* name = NULL;

    // Copy the local variables into a new table.

    lua_newtable_dll(api, L);
    int localTable = lua_gettop_dll(api, L);

    for (int local = 1; name = lua_getlocal_dll(api, L, &stackEntry, local); ++local) 
    {
        if (!GetIsInternalVariable(name))
        {
            // If the value is nil, we use the nil sentinel so we can differentiate
            // between undeclared, and declared but set to nil for lexical scoping.
            if (lua_isnil_dll(api, L, -1))
            {
                lua_pop_dll(api, L, 1);
                lua_pushstring_dll(api, L, name);
                lua_pushvalue_dll(api, L, nilSentinel);
            }
            else
            {
                lua_pushstring_dll(api, L, name);
                lua_insert_dll(api, L, -2);
            }
            lua_rawset_dll(api, L, localTable);
        }
        else
        {
            lua_pop_dll(api, L, 1);
        }
    }

    // Copy the up values into a new table.
    
    lua_newtable_dll(api, L);
    int upValueTable = lua_gettop_dll(api, L);
    
    // Get the function which is the call stack entry so that we can examine
    // the up values and get the environment.
    lua_getinfo_dll(api, L, "fu", &stackEntry);
    int functionIndex = lua_gettop_dll(api, L);

    for (int upValue = 1; name = lua_getupvalue_dll(api, L, functionIndex, upValue); ++upValue) 
    {
        // C function up values has no name, so skip those.
        if( name && *name)
        {
            // If the value is nil, we use the nil sentinel so we can differentiate
            // between undeclared, and declared but set to nil for lexical scoping.
            if (lua_isnil_dll(api, L, -1))
            {
                lua_pop_dll(api, L, 1);
                lua_pushstring_dll(api, L, name);
                lua_pushvalue_dll(api, L, nilSentinel);
            }
            else
            {
                lua_pushstring_dll(api, L, name);
                lua_insert_dll(api, L, -2);
            }
            lua_rawset_dll(api, L, upValueTable);
        }
        else
        {
            lua_pop_dll(api, L, 1);
        }
    }

    // Create an environment table that chains all three of the tables together.
    // They are accessed like this: local -> upvalue -> global
    
    lua_getfenv_dll(api, L, functionIndex);
    int globalTable = lua_gettop_dll(api, L);

    CreateChainedTable(api, L, nilSentinel, localTable, upValueTable, globalTable);

    // Remove the function and global table from the stack.
    lua_remove_dll(api, L, globalTable);
    lua_remove_dll(api, L, functionIndex);

    int t2 = lua_gettop_dll(api, L);
    assert(t2 - t1 == 3);
    
    return true;

}

int IndexChained_worker(unsigned long api, lua_State* L)
{

    LUA_CHECK_STACK(api, L, 1)

    int key = 2;

    int nilSentinel = lua_upvalueindex_dll(api, 1);

    int table[3];
    table[0] = lua_upvalueindex_dll(api, 2); // Locals
    table[1] = lua_upvalueindex_dll(api, 3); // Up values
    table[2] = lua_upvalueindex_dll(api, 4); // Globals

    // Get from the local table.
    lua_pushvalue_dll(api, L, key);
    lua_gettable_dll(api, L, table[0]);
        
    // If it wasn't found, get from the up value table.
    if (lua_isnil_dll(api, L, -1))
    {
        lua_pop_dll(api, L, 1);
        lua_pushvalue_dll(api, L, key);
        lua_gettable_dll(api, L, table[1]);
    }
    
    // If it wasn't found, get from the global table.
    if (lua_isnil_dll(api, L, -1))
    {
        lua_pop_dll(api, L, 1);
        lua_pushvalue_dll(api, L, key);
        lua_gettable_dll(api, L, table[2]);
    }

    // If the value is our nil sentinel, convert it to an actual nil.
    if (lua_rawequal_dll(api, L, -1, nilSentinel))
    {
        lua_pop_dll(api, L, 1);
        lua_pushnil_dll(api, L);
    }

    return 1;

}

int DebugBackend::IndexChained(lua_State* L)
{
    return 0;
}

int DebugBackend::IndexChained_intercept(lua_State* L)
{
    LPVOID lp;
    LhBarrierGetCallback(&lp);
    unsigned long api = (unsigned long)lp;

    return IndexChained_worker(api, L);
}

int NewIndexChained_worker(unsigned long api, lua_State* L)
{

    LUA_CHECK_STACK(api, L, 0)

    int key   = 2;
    int value = 3;

    int nilSentinel = lua_upvalueindex_dll(api, 1);
    
    int table[3];
    table[0] = lua_upvalueindex_dll(api, 2); // Locals
    table[1] = lua_upvalueindex_dll(api, 3); // Up values
    table[2] = lua_upvalueindex_dll(api, 4); // Globals

    // Try to set the value in the local table.
    
    for (int i = 0; i < 2; ++i)
    {
    
        lua_pushvalue_dll(api, L, key);
        lua_rawget_dll(api, L, table[i]); 

        bool exists = !lua_isnil_dll(api, L, -1);
        lua_pop_dll(api, L, 1);
        
        if (exists)
        {
            
            lua_pushvalue_dll(api, L, key);
            
            // Convert from nils to sentinel nils if necessary.
            if (lua_isnil_dll(api, L, value))
            {
                lua_pushvalue_dll(api, L, nilSentinel);
            }
            else
            {
                lua_pushvalue_dll(api, L, value);
            }
            
            lua_settable_dll(api, L, table[i]);
            return 0;

        }

    }

    // Set on the global table.
    lua_pushvalue_dll(api, L, key);
    lua_pushvalue_dll(api, L, value);
    lua_settable_dll(api, L, table[2]);

    return 0;

}

int DebugBackend::NewIndexChained(lua_State* L)
{
    return 0;
}

int DebugBackend::NewIndexChained_intercept(lua_State* L)
{
    LPVOID lp;
    LhBarrierGetCallback(&lp);
    unsigned long api = (unsigned long)lp;

    return NewIndexChained_worker(api, L);
}


void DebugBackend::CreateChainedTable(unsigned long api, lua_State* L, int nilSentinel, int localTable, int upValueTable, int globalTable)
{

    lua_newtable_dll(api, L);
    int metaTable = lua_gettop_dll(api, L);

    // Set the __index method of the metatable.

    lua_pushstring_dll(api, L, "__index");
    
    lua_pushvalue_dll(api, L, nilSentinel);
    lua_pushvalue_dll(api, L, localTable);
    lua_pushvalue_dll(api, L, upValueTable);
    lua_pushvalue_dll(api, L, globalTable);
    
    lua_pushcclosure_dll(api, L, m_apis[api].IndexChained, 4);
    lua_settable_dll(api, L, metaTable);
    
    // Set the __newindex method of the metatable.

    lua_pushstring_dll(api, L, "__newindex");
    
    lua_pushvalue_dll(api, L, nilSentinel);
    lua_pushvalue_dll(api, L, localTable);
    lua_pushvalue_dll(api, L, upValueTable);
    lua_pushvalue_dll(api, L, globalTable);
    
    lua_pushcclosure_dll(api, L, m_apis[api].NewIndexChained, 4);
    lua_settable_dll(api, L, metaTable);

    // Set the table's metatable to be itself so we don't need an extra table
    // to act as the proxy.

    lua_pushvalue_dll(api, L, metaTable);
    lua_setmetatable_dll(api, L, -1);

}

void DebugBackend::SetLocals(unsigned long api, lua_State* L, int stackLevel, int localTable, int nilSentinel)
{

    lua_Debug stackEntry;

    int result = lua_getstack_dll(api, L, stackLevel, &stackEntry);
    assert(result);        

    const char* name = NULL;

    for (int local = 1; name = lua_getlocal_dll(api, L, &stackEntry, local); ++local) 
    {
        
        // Drop the local value, we don't need it.
        lua_pop_dll(api, L, 1);

        if (!GetIsInternalVariable(name))
        {

            // Get the new value for the local from the same named global.
            lua_pushstring_dll(api, L, name);
            lua_rawget_dll(api, L, localTable);

            // Convert from nil sentinels to real nils.
            if (lua_rawequal_dll(api, L, -1, nilSentinel))
            {
                lua_pop_dll(api, L, 1);
                lua_pushnil_dll(api, L);
            }
            
            // Update the local with this value.
            lua_setlocal_dll(api, L, &stackEntry, local);

        }

    }              

}

void DebugBackend::SetUpValues(unsigned long api, lua_State* L, int stackLevel, int upValueTable, int nilSentinel)
{

    lua_Debug stackEntry;

    int result = lua_getstack_dll(api, L, stackLevel, &stackEntry);
    assert(result);        

    // Get the function at the stack level.
    lua_getinfo_dll(api, L, "f", &stackEntry);
    int functionIndex = lua_gettop_dll(api, L);

    const char* name = NULL;

    for (int upValue = 1; name = lua_getupvalue_dll(api, L, functionIndex, upValue); ++upValue)
    {
        
        // Drop the up value value, we don't need it.
        lua_pop_dll(api, L, 1);

        if (strlen(name) > 0)
        {

            // Get the new value for the local from the same named global.
            lua_pushstring_dll(api, L, name);
            lua_rawget_dll(api, L, upValueTable);

            // Convert from nil sentinels to real nils.
            if (lua_rawequal_dll(api, L, -1, nilSentinel))
            {
                lua_pop_dll(api, L, 1);
                lua_pushnil_dll(api, L);
            }

            // Update the up value with this value.
            lua_setupvalue_dll(api, L, functionIndex, upValue);

        }

    }    

    // Remove the function from the stack.
    lua_pop_dll(api, L, 1);

}

bool DebugBackend::Evaluate(unsigned long api, lua_State* L, const std::string& expression, int stackLevel, int depth, std::string& result)
{

    if (!GetIsLuaLoaded())
    {
        SetEvent(m_evalResultEvent);
        return false;
    }
            
    // Adjust the desired stack level based on the number of stack levels we skipped when
    // we sent the front end the call stack.

    {

        CriticalSectionLock lock(m_criticalSection);

        StateToVmMap::iterator stateIterator = m_stateToVm.find(L);
        assert(stateIterator != m_stateToVm.end());

        if (stateIterator != m_stateToVm.end())
        {
            stackLevel += stateIterator->second->stackTop;
        }
    
    }

    int t1 = lua_gettop_dll(api, L);

    // Create a sentinel value used in place of nil in the local and upvalue tables.
    // We do this since we can't store a nil value in a table, but we need to preserve
    // the fact that those variables were declared.
    
    lua_newuserdata_dll(api, L, 0);
    int nilSentinel = lua_gettop_dll(api, L);    
    
    if (!CreateEnvironment(api, L, stackLevel, nilSentinel))
    {
        lua_pop_dll(api, L, 1);
        return false;
    }

    int envTable     = lua_gettop_dll(api, L);
    int upValueTable = envTable - 1;
    int localTable   = envTable - 2;

    // Disable the debugger hook so that we don't try to debug the expression.
    SetHookMode(api, L, HookMode_None);
    EnableIntercepts(api, false);
	auto vm = GetVm(L);
	vm->inEval = true;
    
    int stackTop = lua_gettop_dll(api, L);    
    
    // Turn the expression into a statement by making it a return.

    std::string statement;

    statement  = "return \n";
    statement += expression;
    
    int error = LoadScriptWithoutIntercept(api, L, statement.c_str());

    if (error == LUA_ERRSYNTAX)
    {
        // The original expression may be a statement, so try loading it that way.
        lua_pop_dll(api, L, 1);
        error = LoadScriptWithoutIntercept(api, L, expression.c_str());
    }

    if (error == 0)
    {
        lua_pushvalue_dll(api, L, envTable);
        lua_setfenv_dll(api, L, -2);
        error = lua_pcall_dll(api, L, 0, LUA_MULTRET, 0);
    }

    TiXmlDocument document;
        
    if (error == 0)
    {

        // Figure out how many values were pushed into the stack when we evaluated the
        // expression.
        int nresults = lua_gettop_dll(api, L) - stackTop;

        TiXmlNode* root = NULL;

        // If there are multiple results, create a root "values" node.

        if (nresults > 1)
        {
            root = new TiXmlElement("values");
            document.LinkEndChild(root);
        }
        else
        {
            root = &document;
        }

        for (int i = 0; i < nresults; ++i)
        {
            TiXmlNode* node = GetValueAsText(api, L, -1 - (nresults - 1 - i), depth);

            if (node != NULL)
            {
                root->LinkEndChild(node);
            }
        }

        // Remove the results from the stack.
        lua_pop_dll(api, L, nresults);
    }
    else
    {
        
        // The error message will have the form "junk:2: message" so remove the first bit
        // that isn't useful.

        const char* wholeMessage = lua_tostring_dll(api, L, -1);
        const char* errorMessage = strstr(wholeMessage, ":2: ");
        
        if (errorMessage == NULL)
        {
            errorMessage = wholeMessage;
        }
        else
        {
            // Skip over the ":2: " part.
            errorMessage += 4;
        }

        std::string text;

        text = "Error: ";
        text += errorMessage;

        document.LinkEndChild(WriteXmlNode("error", text));

        lua_pop_dll(api, L, 1);

    }

    //恢复现场，MS不恢复也没问题？
    // Copy any changes to the up values due to evaluating the watch back.
    //SetLocals(api, L, stackLevel, localTable, nilSentinel);
    //SetUpValues(api, L, stackLevel, upValueTable, nilSentinel);

    // Remove the local, up value and environment tables from the stack.
    lua_pop_dll(api, L, 3);

    // Remove the nil sentinel.
    lua_pop_dll(api, L, 1);

    // Convert from XML to a string.

    TiXmlPrinter printer;
    printer.SetIndent("");
    printer.SetLineBreak("");

    document.Accept( &printer );
    result = printer.Str();

    // Reenable the debugger hook
    EnableIntercepts(api, true);
    SetHookMode(api, L, HookMode_Full);
	vm->inEval = false;

    int t2 = lua_gettop_dll(api, L);
    assert(t1 == t2);

    return error == 0;

}

bool DebugBackend::CallMetaMethod(unsigned long api, lua_State* L, int valueIndex, const char* method, int numResults, int& result) const
{

    if (!lua_checkstack_dll(api, L, 3))
    {
        return false;
    }

    if (lua_getmetatable_dll(api, L, valueIndex))
    {

        int metaTableIndex = lua_gettop_dll(api, L);

        if (!lua_isnil_dll(api, L, metaTableIndex))
        {

            lua_pushstring_dll(api, L, method);
            //lua_gettable_dll(api, L, metaTableIndex); //在tolua中直接获取table字段会挂的
            lua_rawget_dll(api, L, metaTableIndex);

            if (lua_isnil_dll(api, L, -1))
            {
                // The meta-method doesn't exist.
                lua_pop_dll(api, L, 1);
                lua_remove_dll(api, L, metaTableIndex);
                return false;
            }
            else
            {
                lua_pushvalue_dll(api, L, valueIndex);
                result = lua_pcall_dll(api, L, 1, numResults, 0);
            }

        }

        lua_remove_dll(api, L, metaTableIndex);
        return true;

    }

    return false;

}

void DebugBackend::MergeTables(unsigned long api, lua_State* L, unsigned int tableIndex1, unsigned int tableIndex2) const
{
    
    if (!lua_checkstack_dll(api, L, 3))
    {
        return;
    }

    tableIndex1 = lua_absindex_dll(api, L, tableIndex1);
    tableIndex2 = lua_absindex_dll(api, L, tableIndex2);

    lua_newtable_dll(api, L);
    int dstTableIndex = lua_gettop_dll(api, L);

    // Iterate over the keys in table 1, inserting them into the
    // target table.

    lua_pushnil_dll(api, L);

    while (lua_next_dll(api, L, tableIndex1) != 0)
    {
        lua_pushvalue_dll(api, L, -2);
        lua_insert_dll(api, L, -2);
        lua_rawset_dll(api, L, dstTableIndex);
    }

    // Iterate over the keys in table 2, inserting them into the
    // target table.

    lua_pushnil_dll(api, L);

    while (lua_next_dll(api, L, tableIndex2) != 0)
    {
        lua_pushvalue_dll(api, L, -2);
        lua_insert_dll(api, L, -2);
        lua_rawset_dll(api, L, dstTableIndex);
    }

}

TiXmlNode* DebugBackend::GetLuaBindClassValue(unsigned long api, lua_State* L, unsigned int maxDepth, bool displayAsKey) const
{

    if (!lua_checkstack_dll(api, L, 3))
    {
        return NULL;
    }

    if (lua_getmetatable_dll(api, L, -1))
    {
        
        lua_pushstring_dll(api, L, "__luabind_class");
        lua_rawget_dll(api, L, -2);

        bool luabindClass = !lua_isnil_dll(api, L, -1);

        // Remove the value and the metatable from the stack.
        lua_pop_dll(api, L, 2);

        if (!luabindClass)
        {
            // This userdata doesn't have the luabind class signature in its
            // metatable.
            return NULL;
        }

    }

    const char* className = "luabind";

    // Luabind stores the accessible methods in the environment for the userdata
    // so we can directly convert that into the value.
    lua_getfenv_dll(api, L, -1);

    TiXmlNode* node = NULL;

    // If the environment has a metatable, those are the class methods and we
    // need to merge them into the 
    if (lua_getmetatable_dll(api, L, -1))
    {

        MergeTables(api, L, -1, -2);

        int tableIndex = lua_gettop_dll(api, L);
        node = GetValueAsText(api, L, tableIndex, maxDepth, className, displayAsKey); 

        lua_pop_dll(api, L, 2);

    }
    else
    {
        int tableIndex = lua_gettop_dll(api, L);
        node = GetValueAsText(api, L, tableIndex, maxDepth, className, displayAsKey); 
    }

    // Remove the value from the stack.
    lua_pop_dll(api, L, 1);

    return node;

}

TiXmlNode* DebugBackend::GetValueAsText(unsigned long api, lua_State* L, int n, int maxDepth, const char* typeNameOverride, bool displayAsKey) const
{

    int t1 = lua_gettop_dll(api, L);

    if (!lua_checkstack_dll(api, L, 1))
    {
        return NULL;
    }

    // Duplicate the item since calling to* can modify the value.
    lua_pushvalue_dll(api, L, n);

    int type = lua_type_dll(api, L, -1);
    const char* typeName = lua_typename_dll(api, L, type);

    if (typeNameOverride == NULL)
    {
        typeNameOverride = typeName;
    }

    TiXmlNode* node = NULL;

    if (strcmp(typeName, "table") == 0)
    {
        int stackStart = lua_gettop_dll(api, L);
        int result = 0;
        std::string className;
        if (CallMetaMethod(api, L, stackStart, "__towatch", LUA_MULTRET, result))
        {
            if (result == 0)
            {
                int numResults = lua_gettop_dll(api, L) - stackStart;

                if( numResults == 0)
                {
                    lua_pushnil_dll( api, L);
                    numResults = 1;
                }
                else if (numResults > 1)
                {
                    // First result is the class name if multiple results are 
                    // returned.
                    className = lua_tostring_dll(api, L, -numResults);
                }

                node = GetValueAsText(api, L, -1, maxDepth, className.c_str(), displayAsKey);

                // Remove the table value.
                lua_pop_dll(api, L, numResults);

            }
        }
        if( node == NULL)
        {
            node = GetTableAsText(api, L, -1, maxDepth - 1, typeNameOverride);
        }
        // Remove the duplicated value.
        lua_pop_dll(api, L, 1);
    }
    else if (strcmp(typeName, "function") == 0)
    {

        lua_Debug ar;
        lua_getinfo_dll(api, L, ">Sn", &ar);

        int scriptIndex = GetScriptIndex(GetSource(api, &ar));

        node = new TiXmlElement("function");
        node->LinkEndChild(WriteXmlNode("script", scriptIndex));
        node->LinkEndChild(WriteXmlNode("line",   GetLineDefined(api, &ar) - 1));
    
    }
    else
    {
        if (strcmp(typeName, "wstring") == 0)
        {

            size_t length = 0;
            const lua_WChar* string = lua_towstring_dll(api, L, -1); 
            
            if (string != NULL)
            {
                length = wcslen(reinterpret_cast<const wchar_t*>(string)) * sizeof(lua_WChar);
            }

            std::string text;
            bool wide;

            if (!displayAsKey)
            {
                text += "L\"";
            }
            
            text += GetAsciiString(string, length, wide, true);
            
            if (!displayAsKey)
            {
                text += "\"";
            }

            node = new TiXmlElement("value");
            node->LinkEndChild( WriteXmlNode("data", text) );
            node->LinkEndChild( WriteXmlNode("type", typeNameOverride) );

        }
        else if (strcmp(typeName, "string") == 0)
        {

            size_t length;
            const char* string = lua_tolstring_dll(api, L, -1, &length); 

            bool wide;
            std::string result = GetAsciiString(string, length, wide);

            std::string text;

            if (!displayAsKey)
            {
                if (wide)
                {
                    text += "L"; 
                }
                text += "\"";
            }

            text += result;

            if (!displayAsKey)
            {
                text += "\"";
            }

            node = new TiXmlElement("value");
            node->LinkEndChild( WriteXmlNode("data", text) );
            node->LinkEndChild( WriteXmlNode("type", typeNameOverride) );

        }
        else if (strcmp(typeName, "userdata") == 0)
        {

            const char* temp = GetClassNameForUserdata(api, L, -1);
            std::string className;

            if (temp != NULL)
            {
                className = temp;
            }

            int valueIndex = lua_gettop_dll(api, L);

            if (className.empty())
            {
                className = "userdata";
            }

            // Check if this is a luabind class instance.
            //node = GetLuaBindClassValue(api, L, maxDepth, displayAsKey);

            if (node == NULL)
            {

                // Check to see if the user data's metatable has a __towatch method. This is
                // a custom method used to provide data to the watch window.

                int result = 0;
                int stackStart = lua_gettop_dll(api, L);

                if (CallMetaMethod(api, L, valueIndex, "__towatch", LUA_MULTRET, result))
                {
                    if (result == 0)
                    {
                       
                        int tableIndex = lua_gettop_dll(api, L);
                        int numResults = tableIndex - stackStart;

                        if( numResults == 0)
                        {
                            lua_pushnil_dll( api, L);
                            ++ tableIndex;
                            numResults = 1;
                        }
                        else if (numResults > 1)
                        {
                            // First result is the class name if multiple results are 
                            // returned.
                            className = lua_tostring_dll(api, L, -numResults);
                        }

                        node = GetValueAsText(api, L, tableIndex, maxDepth, className.c_str(), displayAsKey); 

                        // Remove the table value.
                        lua_pop_dll(api, L, numResults);

                    }
                }
                // 在 unity 中会卡死
                
                else if (CallMetaMethod(api, L, valueIndex, "__tostring", 1, result))
                {
                    if (result == 0)
                    {

                        const char* string = lua_tostring_dll(api, L, -1);
                        
                        if (string != NULL)
                        {
                            node = new TiXmlElement("value");
                            node->LinkEndChild( WriteXmlNode("data", string) );
                            node->LinkEndChild( WriteXmlNode("type", className) );
                        }

                        // Remove the string value.
                        lua_pop_dll(api, L, 1);

                    }
                }

                // Check to see if we called a meta-method and got an error back.
                if (result != 0)
                {

                    const char* error = lua_tostring_dll(api, L, -1);

                    if (error == NULL)
                    {
                        // This shouldn't happen, but we check just to make it a little
                        // more robust.
                        error = "Error executing __tostring";
                    }

                    node = WriteXmlNode("error", error);
                
                    // Remove the error message.
                    lua_pop_dll(api, L, 1);

                }

            }

            // If we did't find a way to display the user data, just display the class name.
            if (node == NULL)
            {

                if (!m_warnedAboutUserData)
                {
                    DebugBackend::Get().Message("Warning 1008: No __tostring or __towatch metamethod was provided for userdata", MessageType_Warning);
                    m_warnedAboutUserData = true;
                }
        
                void* p = lua_touserdata_dll(api, L, valueIndex);

                char buffer[32];

                if (displayAsKey)
                {
                    sprintf(buffer, "[0x%p]", p);
                }
                else
                {
                    sprintf(buffer, "0x%p", p);
                }

                node = new TiXmlElement("value");
                node->LinkEndChild( WriteXmlNode("data", buffer) );
                node->LinkEndChild( WriteXmlNode("type", className) );

            }

        }
        else
        {

            const char* string = lua_tostring_dll(api, L, -1);
            std::string result;

            if (string == NULL)
            {
            
                // If tostring failed for some reason, fallback to our own version.
                char value[64] = { 0 };

                if (strcmp(typeName, "nil") == 0)
                {
                    _snprintf(value, 64, "%s", "nil");
                }
                else if (strcmp(typeName, "number") == 0)
                {
                    _snprintf(value, 64, "%0.2f", lua_tonumber_dll(api, L, -1));
                }
                else if (strcmp(typeName, "boolean") == 0)
                {
                    _snprintf(value, 64, "%s", lua_toboolean_dll(api, L, -1) ? "true" : "false");
                }
                else if (strcmp(typeName, "thread") == 0)
                {
                    _snprintf(value, 64, "%s", "thread");
                }

                result = value;

            }
            else
            {
                result = string;
            }

            node = new TiXmlElement("value");

            if (displayAsKey)
            {
                result = "[" + result + "]"; 
            }

            node->LinkEndChild( WriteXmlNode("data", result) );
            node->LinkEndChild( WriteXmlNode("type", typeNameOverride) );

        }

        // Remove the duplicated value.
        lua_pop_dll(api, L, 1);

    }

    int t2 = lua_gettop_dll(api, L);
    assert(t2 - t1 == 0);

    return node;

}

TiXmlNode* DebugBackend::GetTableAsText(unsigned long api, lua_State* L, int t, int maxDepth, const char* typeNameOverride) const
{

    if (!lua_checkstack_dll(api, L, 2))
    {
        return NULL;
    }    
    
    int t1 = lua_gettop_dll(api, L);

    // Get the absolute index since we need to refer to the table position
    // later once we've put additional stuff on the stack.
    t = lua_absindex_dll(api, L, t);

    TiXmlNode* node = new TiXmlElement("table");

    if (typeNameOverride)
    {
        node->LinkEndChild( WriteXmlNode("type", typeNameOverride) );
    }

    if (maxDepth > 0)
    {

        // First key.
        lua_pushnil_dll(api, L);

        while (lua_next_dll(api, L, t) != 0)
        {

            TiXmlNode* key = new TiXmlElement("key");
            key->LinkEndChild( GetValueAsText(api, L, -2, maxDepth - 1, NULL, true) );

            TiXmlNode* value = new TiXmlElement("data");
            value->LinkEndChild( GetValueAsText(api, L, -1, maxDepth - 1) );

            TiXmlNode* element = new TiXmlElement("element");

            element->LinkEndChild(key);
            element->LinkEndChild(value);
            node->LinkEndChild(element);
            
            // Leave the key on the stack for the next call to lua_next.
            lua_pop_dll(api, L, 1);
        
        }    
    
    }

    int t2 = lua_gettop_dll(api, L);
    assert(t2 - t1 == 0);

    return node;

}

bool DebugBackend::GetIsInternalVariable(const char* name) const
{
    // These could be names like (*temporary), (for index), (for step), (for limit), etc.
    return name[0] == '(';
}

bool DebugBackend::GetClassNameForMetatable(unsigned long api, lua_State* L, int mt) const
{
    
    if (!lua_checkstack_dll(api, L, 2))
    {
        return false;
    }

    int t1 = lua_gettop_dll(api, L);

    mt = lua_absindex_dll(api, L, mt);

    // Iterate over global table (can't do it with the globals pseudo index since it doesn't exist in Lua 5.2)
    lua_pushglobaltable_dll(api, L);

    // First key.
    lua_pushnil_dll(api, L);

    while (lua_next_dll(api, L, t1+1) != 0)
    {

        if (lua_type_dll(api, L, -1) == LUA_TTABLE &&
            lua_type_dll(api, L, -2) == LUA_TSTRING)
        {

            const char* className = lua_tostring_dll(api, L, -2);

            if (lua_rawequal_dll(api, L, -1, mt))
            {

                // Remove the value (the metatable) from the stack and just leave
                // the key (the class name).
                lua_pop_dll(api, L, 1);
                // Remove the global table too
                lua_remove_dll( api, L, -2);
                int t2 = lua_gettop_dll(api, L);
                assert(t2 - t1 == 1);

                return true;
            
            }
        }

        // Leave the key on the stack for the next call to lua_next.
        lua_pop_dll(api, L, 1);
    
    }    

    // Pop global table
    lua_pop_dll( api, L, 1);

    int t2 = lua_gettop_dll(api, L);
    assert(t1 == t2);

    return false;

}

const char* DebugBackend::GetClassNameForUserdata(unsigned long api, lua_State* L, int ud) const
{

    if (!lua_checkstack_dll(api, L, 2))
    {
        return NULL;
    }

    bool result = false;

    if (lua_getmetatable_dll(api, L, ud))
    {

        std::list<ClassInfo>::const_iterator iterator = m_classInfos.begin();

        while (iterator != m_classInfos.end())
        {
            if (iterator->L == L)
            {

                lua_rawgeti_dll(api, L, GetRegistryIndex(api), iterator->metaTableRef);

                if (lua_rawequal_dll(api, L, -1, -2))
                {
                    lua_pop_dll(api, L, 2);
                    return iterator->name.c_str();
                }
        
                lua_pop_dll(api, L, 1);

            }
            ++iterator;
        }

        lua_pop_dll(api, L, 1);

    }

    return NULL;

}

void DebugBackend::RegisterClassName(unsigned long api, lua_State* L, const char* name, int metaTable)
{

    CriticalSectionLock lock(m_criticalSection);

    ClassInfo classInfo;

    classInfo.L             = L;
    classInfo.name          = name;

    lua_pushvalue_dll(api, L, metaTable);
    classInfo.metaTableRef  = luaL_ref_dll(api, L, GetRegistryIndex(api));

    m_classInfos.push_back(classInfo);

}

int DebugBackend::LoadScriptWithoutIntercept(unsigned long api, lua_State* L, const char* buffer, size_t size, const char* name)
{
    return lua_loadbuffer_dll(api, L, buffer, size, name, NULL);
}

int DebugBackend::LoadScriptWithoutIntercept(unsigned long api, lua_State* L, const std::string& string)
{
    return LoadScriptWithoutIntercept(api, L, string.c_str(), string.length(), string.c_str());
}

DWORD WINAPI DebugBackend::FinishInitialize(LPVOID param)
{

    const char* symbolsDirectory = static_cast<const char*>(param);

    extern HINSTANCE g_hInstance;
    return static_cast<DWORD>(InstallLuaHooker(g_hInstance, symbolsDirectory));

}

unsigned long DebugBackend::GetApiForVm(lua_State* L) const
{

    StateToVmMap::const_iterator iterator = m_stateToVm.find(L);

    if (iterator == m_stateToVm.end())
    {
        //assert(iterator != m_stateToVm.end());
        return 0;
    }

    return iterator->second->api;

}

void DebugBackend::IgnoreException(const std::string& message)
{
    CriticalSectionLock lock(m_exceptionCriticalSection);
    m_ignoreExceptions.insert(message);
}

bool DebugBackend::GetIsExceptionIgnored(const std::string& message) const
{
    CriticalSectionLock lock(m_exceptionCriticalSection);
    return m_ignoreExceptions.find(message) != m_ignoreExceptions.end();
}

std::string DebugBackend::GetAsciiString(const void* buffer, size_t length, bool& wide, bool force) const
{
    
    wide = false;
    const char* string = reinterpret_cast<const char*>(buffer);

    if (string == NULL)
    {
        return "";
    }

    bool hasEmbeddedZeros = force;

    for (size_t i = 0; i < length && !hasEmbeddedZeros; ++i)
    {
        if (string[i] == 0)
        {
            hasEmbeddedZeros = true;
        }
    }

    if (!hasEmbeddedZeros)
    {
        return std::string(string, length);
    }
    
    std::string converted;
    converted.reserve(length);
    
    char* result = new char[length + 2]; 
    size_t convertedLength = WideCharToMultiByte(CP_UTF8, 0, (const wchar_t*)string, length / sizeof(wchar_t), result, length + 1, 0, 0);

    if (convertedLength != 0)
    {
        result[convertedLength] = 0;
        wide = true;
    }
    else
    {
        convertedLength = length;
        memcpy(result, string, length);
    }

    // Change the embedded zeros characters to "\0"
    for (size_t i = 0; i < convertedLength; ++i)
    {
        if (result[i] != 0)
        {
            converted += result[i];
        }
        else
        {
            converted += "\\0";
        }
    }
    
    delete [] result;
    return converted;

}

void DebugBackend::CreateWeakTable(unsigned long api, lua_State* L, const char* type)
{

    lua_newtable_dll(api, L);
    int tableIndex = lua_gettop_dll(api, L);

    lua_newtable_dll(api, L);
    int metaTableIndex = lua_gettop_dll(api, L);

    lua_pushstring_dll(api, L, "__mode");
    lua_pushstring_dll(api, L, type);
    lua_settable_dll(api, L, metaTableIndex);

    lua_setmetatable_dll(api, L, tableIndex);

}

int DebugBackend::ObjectCollectionCallback(lua_State* L)
{

    if (!DebugBackend::Get().GetIsAttached())
    {
        return 0;
    }

    unsigned long api = DebugBackend::Get().GetApiForVm(L);

    int tableIndex    = lua_upvalueindex_dll(api, 1);
    int callbackIndex = lua_upvalueindex_dll(api, 2);

    // Check if the object is still around or if it has been collected.
    lua_pushstring_dll(api, L, "object");
    lua_rawget_dll(api, L, tableIndex);

    if (lua_isnil_dll(api, L, -1))
    {
        // The object has been collected, so call the callback.
        lua_pushvalue_dll(api, L, callbackIndex);
        lua_call_dll(api, L, 0, 0);
    }
    else
    {
        
        // Recreate the sentinel.        
            
        lua_pushvalue_dll(api, L, tableIndex);
        lua_pushvalue_dll(api, L, callbackIndex);
        lua_pushcclosure_dll(api, L, ObjectCollectionCallback, 2);

        DebugBackend::Get().CreateGarbageCollectionSentinel(api, L);

    }

    lua_pop_dll(api, L, 1);
    
    return 0;

}

void DebugBackend::CreateGarbageCollectionSentinel(unsigned long api, lua_State* L)
{

    int t1 = lua_gettop_dll(api, L);

    int callbackIndex = lua_gettop_dll(api, L);

    // Create a new user data which can associate our garbage collection callback.
    lua_newuserdata_dll(api, L, 4);
    int userDataIndex = lua_gettop_dll(api, L);

    lua_newtable_dll(api, L);
    int metaTableIndex = lua_gettop_dll(api, L);

    lua_pushstring_dll(api, L, "__gc");
    lua_pushvalue_dll(api, L, callbackIndex);
    lua_settable_dll(api, L, metaTableIndex);
    
    lua_setmetatable_dll(api, L, userDataIndex);

    // Remove the user data from the stack. It will be collected when the garbage
    // collector runs. We also remove the callback.
    lua_pop_dll(api, L, 2);

    int t2 = lua_gettop_dll(api, L);
    assert(t1 - t2 == 1);

}

void DebugBackend::SetGarbageCollectionCallback(unsigned long api, lua_State* L, int index)
{

    int t1 = lua_gettop_dll(api, L);

    index = lua_absindex_dll(api, L, index);
    int callbackIndex = lua_gettop_dll(api, L);

    // Create a new weak table to store the object in, which allows us to detect
    // if it's been collected or not.
    CreateWeakTable(api, L, "v");
    int tableIndex = lua_gettop_dll(api, L);

    // Store the object in a field called "object".
    lua_pushstring_dll(api, L, "object");
    lua_pushvalue_dll(api, L, index);
    lua_rawset_dll(api, L, tableIndex);

    lua_pushvalue_dll(api, L, callbackIndex);
    lua_pushcclosure_dll(api, L, ObjectCollectionCallback, 2);

    CreateGarbageCollectionSentinel(api, L);

    // Pop the callback.
    lua_pop_dll(api, L, 1);

    int t2 = lua_gettop_dll(api, L);
    assert(t1 - t2 == 1);

}

int DebugBackend::ThreadEndCallback(lua_State* L)
{

    unsigned long api = DebugBackend::Get().GetApiForVm(L);
    lua_State* thread = static_cast<lua_State*>(lua_touserdata_dll(api, L, lua_upvalueindex_dll(api, 1)));

    if (thread != L)
    {
        // This is actually a thread and not the main thread (which for some
        // reason gets garbage collected immediately)
        DebugBackend::Get().DetachState(api, thread);
    }

    return 0;

}

void DebugBackend::GetFileTitle(const char* name, std::string& title) const
{

    const char* slash1 = strrchr(name, '\\');
    const char* slash2 = strrchr(name, '/');

    const char* pathEnd = max(slash1, slash2);

    if (pathEnd == NULL)
    {
        // There's no path so the whole thing is the file title.
        title = name;
    }
    else
    {
        title = pathEnd + 1;
    }

}

bool DebugBackend::EnableJit(unsigned long api, lua_State* L, bool enable)
{

    LUA_CHECK_STACK(api, L, 0);

    lua_rawgetglobal_dll(api, L, "jit");
    int jitTable = lua_gettop_dll(api, L);

    if (lua_isnil_dll(api, L, -1))
    {
        // LuaJIT api doesn't exist.
        lua_pop_dll(api, L, 1);
        return false;
    }

    if (enable)
    {
        lua_pushstring_dll(api, L, "on");
    }
    else
    {
        lua_pushstring_dll(api, L, "off");
    }

    bool success = false;
    lua_rawget_dll(api, L, jitTable);

    if (!lua_isnil_dll(api, L, -1))
    {
        // Call the function.
        success = (lua_pcall_dll(api, L, 0, 0, 0) == 0);
    }
    else
    {
        // Remove the nil.
        lua_pop_dll(api, L, 1);
    }

    // Remove the JIT table.
    lua_pop_dll(api, L, 1);

    return success;

}

void DebugBackend::LogHookEvent(unsigned long api, lua_State* L, lua_Debug* ar)
{

    const char* eventType = GetHookEventName( api, ar);

    // Get some more information about the event.
    lua_getinfo_dll(api, L, "Sln", ar);

    Log("Hook Event %s, line %d %s %s\n", eventType, GetCurrentLine(api, ar), GetName(api, ar), GetSource(api, ar));

}

unsigned int DebugBackend::GetCStack(HANDLE hThread, StackEntry stack[], unsigned int maxStackSize)
{

    const unsigned int maxNameLength = 256;
    const unsigned int maxStackFrames = 64; 

    IMAGEHLP_SYMBOL64* symbol = static_cast<IMAGEHLP_SYMBOL64*>(alloca(sizeof(IMAGEHLP_SYMBOL64) + maxNameLength));
    
    symbol->SizeOfStruct    = sizeof(IMAGEHLP_SYMBOL64);
    symbol->MaxNameLength   = s_maxEntryNameLength;

    HANDLE hProcess = GetCurrentProcess();

    STACKFRAME64* stackFrame = reinterpret_cast<STACKFRAME64*>(alloca(sizeof(STACKFRAME64) * maxStackSize));
    unsigned int numStackFrames = ::GetCStack(hThread, stackFrame, maxStackSize);

    for (unsigned int i = 0; i < numStackFrames; ++i)
    {

        IMAGEHLP_MODULE64 module;
        module.SizeOfStruct = sizeof(module);

        if (SymGetModuleInfo64_dll(hProcess, stackFrame[i].AddrPC.Offset, &module))
        {
            strcpy(stack[i].module, module.ModuleName);
        }
        else
        {
            stack[i].module[0] = 0;
        }
        
        stack[i].scriptIndex = -1;
        stack[i].line        = 0;

        // Try to get the symbol name from the address.

        if (SymGetSymFromAddr64_dll(hProcess, stackFrame[i].AddrPC.Offset, NULL, symbol))
        {
            sprintf(stack[i].name, "%s", symbol->Name);
        }
        else
        {
            sprintf(stack[i].name, "0x%x", stackFrame[i].AddrPC.Offset);
        }

    }

    return numStackFrames;

}

int DebugBackend::GetStackDepth(unsigned long api, lua_State* L) const
{

    lua_Debug ar;

    int level = 0;
    while (lua_getstack_dll(api, L, level, &ar))
    {
        ++level;
    }

    return level;

}

DebugBackend::VirtualMachine* DebugBackend::GetVm(lua_State* L)
{

    StateToVmMap::iterator stateIterator = m_stateToVm.find(L);
    assert(stateIterator != m_stateToVm.end());

    if (stateIterator != m_stateToVm.end())
    {
        return stateIterator->second;

    }

    return NULL;

}

unsigned int DebugBackend::GetUnifiedStack(unsigned long api, const StackEntry nativeStack[], unsigned int nativeStackSize, const lua_Debug scriptStack[], unsigned int scriptStackSize, StackEntry stack[])
{

    // Print out the unified call stack.

    int nativePos = nativeStackSize - 1;
    int scriptPos = scriptStackSize - 1;

    unsigned int stackSize = 0;

    while (stackSize < s_maxStackSize && scriptPos >= 0)
    {

        // Walk up the native stack until we hit a transition into Lua.
        while (nativePos >= 0)
        {
            if (strcmp(nativeStack[nativePos].name, "lua_pcall") == 0)
            {
                --nativePos;
                break;
            }
            if (strncmp(nativeStack[nativePos].name, "luaD_", 5) != 0 &&
                strncmp(nativeStack[nativePos].name, "luaV_", 5) != 0 &&
                strncmp(nativeStack[nativePos].name, "lua_", 4)  != 0)
            {
                stack[stackSize] = nativeStack[nativePos];
                stack[stackSize].scriptPos = -1;
                ++stackSize;
            }
            --nativePos;
        }

        // Walk up the script stack until we hit a transition into C.
        while (scriptPos >= 0 && stackSize < s_maxStackSize)
        {
            const lua_Debug* ar = &scriptStack[scriptPos];
            const char* function = GetName(api, ar);
            const char* arwhat = GetWhat(api, ar);
            if (function == NULL || function[0] == '\0')
            {
                if (arwhat != NULL)
                {
                    function = arwhat;
                }
                else
                {
                    function = "<Unknown>";
                }
            }

            if (arwhat != NULL && strcmp(arwhat, "C") == 0)
            {
                --scriptPos;
                break;
            }

            stack[stackSize].scriptIndex = GetScriptIndex(GetSource(api, ar));
            stack[stackSize].line        = GetCurrentLine(api, ar) - 1;
            stack[stackSize].scriptPos   = scriptPos;
            
            strncpy(stack[stackSize].name, function, s_maxEntryNameLength);
            
            ++stackSize;
            --scriptPos;
  
        }

    }

    return stackSize;

}

