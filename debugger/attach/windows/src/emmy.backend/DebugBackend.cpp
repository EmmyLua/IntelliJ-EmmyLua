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
#include "DebugHelp.h"

#include <assert.h>
#include <algorithm>
#include <EasyHook.h>

#include "DebugPipeline.h"
#include "DebugMessage.h"
#include "LuaProfiler.h"
#include "StackNode.h"
#include <thread>
#include "StdRedirector.h"
#include "Utility.h"

DebugBackend* DebugBackend::s_instance = nullptr;
StdRedirector* redirectorOUT = nullptr;
StdRedirector* redirectorERR = nullptr;

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
		return nullptr;
	}

}

BOOL inputStdin(const char* str)
{
	std::wstring s = CharToWchar(str);

	for (const wchar_t* p = s.c_str(); *p; ++p)
	{
		INPUT_RECORD buf[2];
		buf[0].EventType = KEY_EVENT;
		buf[0].Event.KeyEvent.bKeyDown = TRUE;
		buf[0].Event.KeyEvent.wRepeatCount = 1;
		buf[0].Event.KeyEvent.wVirtualKeyCode = toupper(*p);
		buf[0].Event.KeyEvent.wVirtualScanCode = MapVirtualKey(toupper(*p), MAPVK_VK_TO_VSC);
		//buf[0].Event.KeyEvent.uChar.AsciiChar = *p;
		buf[0].Event.KeyEvent.uChar.UnicodeChar = *p;
		buf[0].Event.KeyEvent.dwControlKeyState = 0;
		buf[1] = buf[0];
		buf[1].Event.KeyEvent.bKeyDown = FALSE;

		DWORD n = 0;
		BOOL b = WriteConsoleInputW(GetStdHandle(STD_INPUT_HANDLE), buf, 2, &n);
		if (!b || n != 2)
			return FALSE;
	}
	return TRUE;
}

bool DebugBackend::Script::GetHasBreakPoint(unsigned int line) const
{

	for (size_t i = 0; i < breakpoints.size(); i++)
	{
		if (breakpoints[i]->line == line)
		{
			return true;
		}
	}

	return false;
}

DebugBackend::Breakpoint* DebugBackend::Script::GetBreakpoint(unsigned int line)
{
	for (auto bp : breakpoints)
	{
		if (bp->line == line)
		{
			return bp;
		}
	}
	return nullptr;
}

bool DebugBackend::Script::HasBreakPointInRange(unsigned int start, unsigned int end) const
{

	for (size_t i = 0; i < breakpoints.size(); i++)
	{
		auto bp = breakpoints[i];
		if (bp->line >= start && bp->line < end)
		{
			return true;
		}
	}

	return false;
}

void DebugBackend::Script::AddBreakpoint(unsigned int line, const std::string & condtion)
{
	Breakpoint* bp = nullptr;
	for (auto b : breakpoints)
	{
		if (b->line == line)
		{
			bp = b;
			break;
		}
	}

	if (bp == nullptr)
	{
		bp = new Breakpoint();
		bp->line = line;
		bp->condtion = condtion;
		bp->hasCondition = !condtion.empty();
		breakpoints.push_back(bp);
	}
	else
	{
		bp->condtion = condtion;
		bp->hasCondition = !condtion.empty();
	}
}

void DebugBackend::Script::DelBreakpoint(unsigned int line)
{
	for (size_t i = 0; i < breakpoints.size(); ++i)
	{
		auto bp = breakpoints[i];

		if (bp->line == line)
		{
			breakpoints.erase(breakpoints.begin() + i);
			delete bp;
			break;
		}
	}
}

void DebugBackend::Script::ClearBreakpoints()
{
	breakpoints.resize(0);
}

bool DebugBackend::Script::ReadyToSend()
{
	return state == CodeState_Normal || state == CodeState_Binary;
}

bool DebugBackend::Script::HasBreakpointsActive() const
{
	return breakpoints.size() != 0;
}

DebugBackend& DebugBackend::Get()
{
	if (s_instance == nullptr)
	{
		s_instance = new DebugBackend;
	}
	return *s_instance;
}

void DebugBackend::Destroy()
{
	delete s_instance;
	s_instance = nullptr;
}

DebugBackend::DebugBackend(): m_hooked(false), m_debugPipeline(nullptr), m_profiler(false), m_checkReloadNextTime(false)
{
	m_commandThread = nullptr;
	m_stepEvent = nullptr;
	m_loadEvent = nullptr;
	m_detachEvent = nullptr;
	m_mode = Mode_Continue;
	m_log = nullptr;
	m_warnedAboutUserData = false;
	m_evalEvent = nullptr;
	m_evalResultEvent = nullptr;
}

DebugBackend::~DebugBackend()
{

	// Check if we successfully hooked the functions. If we didn't, send a warning.
	/*if (!GetIsLuaLoaded())
	{
	Message("Warning 1000: Lua functions were not found during debugging session", MessageType_Warning);
	}*/

	if (m_log != nullptr)
	{
		fclose(m_log);
		m_log = nullptr;
	}

	m_debugPipeline->Destroy();
	//m_eventChannel.Destroy();
	//m_commandChannel.Destroy();

	if (m_commandThread != nullptr)
	{
		CloseHandle(m_commandThread);
		m_commandThread = nullptr;
	}

	if (m_stepEvent != nullptr)
	{
		CloseHandle(m_stepEvent);
		m_stepEvent = nullptr;
	}

	if (m_evalEvent != nullptr)
	{
		CloseHandle(m_evalEvent);
		m_evalEvent = nullptr;
	}

	if (m_evalResultEvent != nullptr)
	{
		CloseHandle(m_evalResultEvent);
		m_evalResultEvent = nullptr;
	}

	if (m_loadEvent != nullptr)
	{
		CloseHandle(m_loadEvent);
		m_loadEvent = nullptr;
	}

	if (m_detachEvent != nullptr)
	{
		CloseHandle(m_detachEvent);
		m_detachEvent = nullptr;
	}

	for (Script* script : m_scripts)
	{
		if (script) delete script;
	}

	m_scripts.clear();
	m_nameToScript.clear();

}

void DebugBackend::CreateApi(LAPI apiIndex)
{

	// Make room for the data for this api.
	if (m_apis.size() < apiIndex + 1)
	{
		m_apis.resize(apiIndex + 1);
	}

	assert(m_apis[apiIndex].IndexChained == NULL);
	assert(m_apis[apiIndex].NewIndexChained == NULL);

	// Create instances of the functions will need to use as callbacks with this API.
	m_apis[apiIndex].IndexChained = (lua_CFunction)CreateCFunction(apiIndex, IndexChained, IndexChained_intercept);
	m_apis[apiIndex].NewIndexChained = (lua_CFunction)CreateCFunction(apiIndex, NewIndexChained, IndexChained_intercept);

}

void DebugBackend::Log(const char* fmt, ...)
{

	if (m_log == nullptr)
	{
		char fileName[_MAX_PATH];
		if (GetStartupDirectory(fileName, _MAX_PATH))
		{
			strcat(fileName, "log.txt");
			m_log = fopen("c:/temp/log.txt", "wt");
		}
	}

	if (m_log != nullptr)
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
	// handshake
	char handshakeChannelName[256];
	_snprintf(handshakeChannelName, 256, "Decoda.Handshake.%x", GetCurrentProcessId());
	Channel handshakeChannel;
	if (!handshakeChannel.Connect(handshakeChannelName))
	{
		return false;
	}

	// Create the event used to signal when we should stop "breaking"
	// and step to the next line.
	m_stepEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);

	// Create the event used to signal when the frontend is finished processing
	// the load of a script.w
	m_loadEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);

	// Create the detach event used to signal when the debugger has been detached
	// from our process. Note this event doesn't reset itself automatically.
	m_detachEvent = CreateEvent(nullptr, TRUE, FALSE, nullptr);

	m_evalEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);
	m_evalResultEvent = CreateEvent(nullptr, FALSE, FALSE, nullptr);

	// Give the front end the address of our Initialize function so that
	// it can call it once we're done loading.
	handshakeChannel.WriteUInt32(EventId_Initialize);
	handshakeChannel.WriteUInt64(reinterpret_cast<uint64_t>(FinishInitialize));
	handshakeChannel.Flush();

	return true;

}

bool DebugBackend::InitializePipeline()
{
	// init debug pipeline
	m_debugPipeline = new SocketPipeline();
	if (!m_debugPipeline->Initialize())
	{
		return false;
	}

	// Start a new thread to handle the incoming event channel.
	//DWORD threadId;
	//m_commandThread = CreateThread(nullptr, 0, StaticCommandThreadProc, this, 0, &threadId);

	return true;
}


DebugBackend::VirtualMachine* DebugBackend::AttachState(LAPI api, lua_State* L)
{
	CriticalSectionLock lock(m_criticalSection);
	CriticalSectionLock lock_vms(m_vmsLock);

	// Check if the virtual machine is aleady in our list. This happens
	// if we're attaching this virtual machine implicitly through lua_call
	// or lua_pcall.

	auto stateIterator = m_stateToVm.find(L);

	if (stateIterator != m_stateToVm.end())
	{
		auto vmm = stateIterator->second;
		//lazy set hook mode.
		//@see SetAllHookModeLazy
		if (vmm->lazySetHook)
		{
			vmm->lazySetHook = false;
			SetHookMode(vmm->api, vmm->L, vmm->lazyHookMode);
		}
		return vmm;
	}

	VirtualMachine* vm = new VirtualMachine;
	ZeroMemory(vm, sizeof(VirtualMachine));
	vm->L = L;
	vm->hThread = GetCurrentThread();
	vm->initialized = false;
	vm->callCount = 0;
	vm->callStackDepth = 0;
	vm->lastStepLine = -2;
	vm->lastStepScript = -1;
	vm->api = api;
	vm->stackTop = 0;
	vm->luaJitWorkAround = false;
	vm->breakpointInStack = true;// Force the stack tobe checked when the first script is entered
	vm->haveActiveBreakpoints = true;
	vm->lazyHookMode = HookMode_None;
	vm->lazySetHook = false;

	m_vms.push_back(vm);
	m_stateToVm.insert(std::make_pair(L, vm));

	if (!lua_checkstack_dll(api, L, 3))
	{
		return nullptr;
	}

	if (GetIsAttached())
	{
		DebugMessage dm_create_vm(DebugMessageId::CreateVM);
		dm_create_vm.L = L;
		m_debugPipeline->Send(&dm_create_vm);

		// Register the debug API.
		RegisterDebugLibrary(api, L);
		RegisterEmmyLibrary(api, L);

		// Start debugging on this VM.
		SetHookMode(api, L, HookMode_Full);
	}

	// This state may be a thread which will be garbage collected, so we need to register
	// to recieve notification when it is destroyed.
	if (lua_pushthread_dll(api, L))
	{
		lua_pushlightuserdata_dll(api, L, L);
		lua_pushcclosure_dll(api, L, ThreadEndCallback, 1);

		SetGarbageCollectionCallback(api, L, -2);
		lua_pop_dll(api, L, 1);
	}

	return vm;
}

void DebugBackend::DetachState(LAPI api, lua_State* L)
{

	CriticalSectionLock lock1(m_criticalSection);
	CriticalSectionLock lock_vms(m_vmsLock);

	// Remove all of the class names associated with this state.
	{
		auto it = m_classInfos.begin();

		while (it != m_classInfos.end())
		{
			if (it->L == L)
				it = m_classInfos.erase(it);
			else
				++it;
		}
	}

	//Remove VM
	{
		auto it = m_vms.begin();
		while (it != m_vms.end())
		{
			auto vm = *it;
			if (vm->L == L)
			{
				CloseHandle(vm->hThread);
				delete vm;
				it = m_vms.erase(it);
			}
			else ++it;
		}
	}

	// Remove the state from our list.
	{
		auto it = m_stateToVm.find(L);

		if (it != m_stateToVm.end())
		{
			m_stateToVm.erase(it);

			DebugMessage dm_destroy_vm(DebugMessageId::DestroyVM);
			dm_destroy_vm.L = L;
			m_debugPipeline->Send(&dm_destroy_vm);
		}
	}
}

int DebugBackend::PostLoadScript(LAPI api, int result, lua_State* L, const char* source, size_t size, const char* name)
{

	if (!GetIsAttached())
	{
		return result;
	}

	auto vm = GetVm(L);
	if (vm == nullptr || vm->skipPostLoadScript)
	{
		return result;
	}
	if (!vm->isEmmyLoaded)
	{
		InitEmmy(api, L);
	}

	size_t index = -1;
	// Register the script before dealing with errors, since the front end has enough
	// information to display the error.
	ScriptRegisterResult regResult = RegisterScript(api, L, source, size, name, false, &index);

	if (result != 0)
	{
		// Make sure no other threads are running Lua while we handle the error.
		CriticalSectionLock lock(m_criticalSection);
		CriticalSectionLock lock2(m_breakLock);

		// Get the error mesasge.
		const char* message = lua_tostring_dll(api, L, -1);

		// Stop execution.
		if (SendBreakEvent(api, L, 1))
		{
			// Send an error event.
			DMLoadError dm_load_error;
			dm_load_error.L = L;
			dm_load_error.message = message;
			if (m_debugPipeline->Send(&dm_load_error))
			{
				// TODO : Wait for the front-end to tell use to continue.
				// WaitForContinue();
			}
		}
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

	if (regResult == ScriptRegisterResult::RegisterAndSend)
	{
		// Stop execution so that the frontend has an opportunity to send us the break points
		// before we start executing the first line of the script.
		WaitForEvent(m_loadEvent);
	}

	return result;

}

ScriptRegisterResult DebugBackend::RegisterScript(LAPI api, lua_State* L,
	const char* source,
	size_t size,
	const char* name,
	bool unavailable,
	size_t* index)
{
	CriticalSectionLock lock(m_criticalSection);
	CriticalSectionLock lockScripts(m_scriptsLock);

	ScriptRegisterResult result = ScriptRegisterResult::Exist;

	// If no name was specified, use the source as the name. This is similar to what
	// built-in Lua functions like luaL_loadstring do.
	if (name == nullptr)
		name = source;
	std::string fixedName = FixFileName(name);

	UnregisterScript(name, fixedName.c_str());

	Script* script = nullptr;
	{
		// Since the name can be a file name, and multiple names can map to the same file,
		// extract the file title from the name and compare the code with any matches.

		std::string title;
		GetFileTitle(name, title);

		for (unsigned int i = 0; i < m_scripts.size(); ++i)
		{
			const auto sc = m_scripts[i];

			if (sc && sc->title == title)
			{
				// Check that the source matches.
				if (sc->source == std::string(source, size))
				{
					script = sc;
					// Record the script index under this other name.
					m_nameToScript.insert(std::make_pair(name, i));
					break;
				}
			}
		}

		if (script == nullptr)
		{
			script = new Script;
			script->name = name;
			script->title = title;
			script->state = CodeState_Unavailable;
			script->reloading = false;

			size_t scriptIndex = m_scripts.size();
			m_scripts.push_back(script);
			m_nameToScript.insert(std::make_pair(name, scriptIndex));
			if (fixedName != name)
				m_nameToScript.insert(std::make_pair(fixedName.c_str(), scriptIndex));

			script->index = scriptIndex;

			std::string fileName;

			// Check if the file name is actually the source. This happens when calling
			// luaL_loadstring and doesn't make for a very good display.
			/*
			size_t length = strlen(name);
			if (source != nullptr && strncmp(name, source, length) == 0)
			{
				char buffer[32];
				sprintf(buffer, "@Untitled%d.lua", scriptIndex + 1);
				fileName = buffer;
			}
			else*/
			{
				fileName = fixedName;

				// Remove the @ sign in front of file names when we pass it to the UI.
				if (fileName[0] == '@')
				{
					fileName.erase(0, 1);
				}
			}

			script->fileName = fileName;

			result = ScriptRegisterResult::RegisterOnly;
		}
	}

	bool updateSource = result != ScriptRegisterResult::Exist|| script->state == CodeState_Reload;

	// update source & state
	if (updateSource)
	{
		if (size > 0 && source != nullptr)
		{
			script->source = std::string(source, size);
		}
		CodeState state = unavailable ? CodeState_Unavailable : CodeState_Normal;
		// Check if this is a compiled/binary file.
		if (source != nullptr && size >= 4)
		{
			if (source[0] >= 27 && source[0] <= 33 && memcmp(source + 1, "Lua", 3) == 0)
			{
				state = CodeState_Binary;
			}
		}

		script->state = state;

		if (GetIsAttached())
		{
			result = ScriptRegisterResult::RegisterAndSend;
			SendScript(L, script);
		}
	}

	*index = script->index;

	return result;
}

ScriptRegisterResult DebugBackend::RegisterScript(LAPI api, lua_State* L, lua_Debug* ar, size_t* index)
{
	const char* arsource = GetSource(api, ar);
	const char* source = nullptr;
	size_t size = 0;

	if (arsource != nullptr && arsource[0] != '@')
	{
		source = arsource;
		size = strlen(source);
	}

	ScriptRegisterResult result = RegisterScript(api, L, source, size, arsource, true/*arsource == nullptr*/, index);

	// We need to exit the critical section before waiting so that we don't
	// monopolize it. Specifically, ToggleBreakpoint will need it.
	m_criticalSection.Exit();

	if (result == ScriptRegisterResult::RegisterAndSend)
	{
		// Stop execution so that the frontend has an opportunity to send us the break points
		// before we start executing the first line of the script.
		WaitForEvent(m_loadEvent);
	}

	m_criticalSection.Enter();

	// Since the script indices may have changed while we released the critical section,
	// require the script index.
	*index = GetScriptIndex(arsource);

	return result;
}

void DebugBackend::UnregisterScript(const char* name, const char* fixedName)
{
	int oldIndex = GetScriptIndex(name);
	if (oldIndex == -1)
		oldIndex = GetScriptIndex(fixedName);
	if (oldIndex != -1)
	{
		auto oldScript = GetScript(oldIndex);
		if (oldScript != nullptr)
		{
			m_scripts[oldIndex] = nullptr;
			delete oldScript;
		}
	}
	auto find = m_nameToScript.find(name);
	if (find != m_nameToScript.end())
		m_nameToScript.erase(find);
	find = m_nameToScript.find(fixedName);
	if (find != m_nameToScript.end())
		m_nameToScript.erase(find);
}


void DebugBackend::Message(const char* message, MessageType type) const
{
	Message(message, strlen(message), type);
}

void DebugBackend::Message(const char* message, size_t size, MessageType type) const
{
	DMMessage dm_message(type, message, size);
	m_debugPipeline->Send(&dm_message);
}

void DebugBackend::Message(MessageType type, const char *fmt, ...) const
{
	va_list args;
	va_start(args, fmt);
	char buff[1024] = { 0 };
	vsnprintf_s(buff, 1024, fmt, args);
	va_end(args);
	Message(buff, type);
}

void DebugBackend::HookCallback(LAPI api, lua_State* L, lua_Debug* ar)
{

	m_criticalSection.Enter();

	if (!lua_checkstack_dll(api, L, 2))
	{
		m_criticalSection.Exit();
		return;
	}

	CheckReload(api, L);

	// Note this executes in the thread of the script being debugged,
	// not our debugger, so we can block.

	VirtualMachine* vm = nullptr;
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

	if (name == nullptr)
	{
		name = "";
	}

	if (name != vm->name)
	{
		vm->name = name;
		DMNameVM dm_name_vm;
		dm_name_vm.L = L;
		dm_name_vm.name = vm->name;
		m_debugPipeline->Send(&dm_name_vm);
	}

	lua_pop_dll(api, L, 1);

	if (m_profiler)
	{
		ProcProfiler(api, L, ar);
	}

	// Log for debugging.
	//LogHookEvent(api, L, ar);

	//Only try to downgrade the hook when the debugger is not stepping   
	if (m_mode == Mode_Continue)
	{
		UpdateHookMode(api, L, ar);
	}
	else
	{
		if (GetHookMode(api, L) != HookMode_Full)
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
		size_t scriptIndex = GetScriptIndex(arsource);

		if (scriptIndex == -1)
		{
			// This isn't a script we've seen before, so tell the debugger about it.
			RegisterScript(api, L, ar, &scriptIndex);
		}

		bool stop = false;
		bool onLastStepLine = false;
		Breakpoint* breakpoint = nullptr;

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
					vm->callCount = 0;
					vm->callStackDepth = 0;
				}
			}
		}

		if (scriptIndex != -1)
		{
			Script* script = GetScript(scriptIndex);
			// Check to see if we're on a breakpoint and should break.
			if (!onLastStepLine && script)
			{
				breakpoint = script->GetBreakpoint(GetCurrentLine(api, ar) - 1);
				if (breakpoint && CheckCondition(api, L, breakpoint))
				{
					stop = true;
				}
			}
		}

		//Break if were doing some kind of stepping 
		if (!onLastStepLine)
		{
			if (m_mode == Mode_StepInto) {
				stop = true;
			}
			else if (m_mode == Mode_StepOver) {
				stop = vm->callCount == 0;
			}
			else if (m_mode == Mode_StepOut) {
				int stackDepth = GetStackDepth(api, L);
				stop = vm->callStackDepth == stackDepth + 1;
			}
		}

		// We need to exit the critical section before waiting so that we don't
		// monopolize it.
		m_criticalSection.Exit();

		if (stop)
		{
			BreakFromScript(api, L);
			vm->callStackDepth = GetStackDepth(api, L);

			if (vm->luaJitWorkAround)
			{
				vm->lastStepScript = scriptIndex;
			}
		}

		vm->lastStepLine = GetCurrentLine(api, ar);
	}
	else
	{
		if (m_mode == Mode_StepOver)
		{
			if (GetIsHookEventRet(api, arevent)) // only LUA_HOOKRET for Lua 5.2, can also be LUA_HOOKTAILRET for older versions
			{
				if (vm->callCount > 0)
				{
					--vm->callCount;
				}
			}
			else if (GetIsHookEventCall(api, arevent)) // only LUA_HOOKCALL for Lua 5.1, can also be LUA_HOOKTAILCALL for newer versions
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

void DebugBackend::SetAllHookModeLazy(HookMode mode)
{
	CriticalSectionLock lock_vms(m_vmsLock);
	for (auto vm : m_vms)
	{
		//may crash!!
		//so we set hook mode lazy.
		vm->lazyHookMode = mode;
		vm->lazySetHook = true;
	}
}

void DebugBackend::UpdateHookMode(LAPI api, lua_State* L, lua_Debug* hookEvent)
{
	int arevent = GetEvent(api, hookEvent);
	//Only update the hook mode for call or return hook events 
	if (arevent == LUA_HOOKLINE)
	{
		return;
	}

	VirtualMachine* vm = GetVm(L);
	if (vm == nullptr)
		return;

	CheckReload(api, L);

	HookMode mode = HookMode_CallsOnly;

	// Populate the line number and source name debug fields
	lua_getinfo_dll(api, L, "S", hookEvent);
	int linedefined = GetLineDefined(api, hookEvent);

	if (GetIsHookEventCall(api, arevent) && linedefined != -1)
	{
		vm->lastFunctions = GetSource(api, hookEvent);

		size_t scriptIndex = GetScriptIndex(vm->lastFunctions.c_str());

		if (scriptIndex == -1)
		{
			RegisterScript(api, L, hookEvent, &scriptIndex);
			scriptIndex = GetScriptIndex(vm->lastFunctions.c_str());
		}

		Script* script = GetScript(scriptIndex);
		if (script)
		{
			const int lastLineDefined = GetLastLineDefined(api, hookEvent);
			bool hasBPInRange = script->HasBreakPointInRange(linedefined, lastLineDefined);
			if (!hasBPInRange)
				hasBPInRange = linedefined == 0 && lastLineDefined == 0 && script->HasBreakpointsActive();
			if (hasBPInRange)
			{
				mode = HookMode_Full;
				vm->breakpointInStack = true;
			}
		}
	}

	//Keep the hook in Full mode while theres a function in the stack somewhere that has a breakpoint in it
	if (mode != HookMode_Full && vm->breakpointInStack)
	{
		if (StackHasBreakpoint(api, L))
		{
			mode = HookMode_Full;
		}
		else
		{
			vm->breakpointInStack = false;
		}
	}

	HookMode currentMode = GetHookMode(api, L);

	if (!vm->haveActiveBreakpoints)
	{
		mode = HookMode_None;
	}
	if (m_profiler && mode < HookMode_CallsAndReturns)
	{
		mode = HookMode_CallsAndReturns;
	}
	if (currentMode != mode)
	{
		//Always switch to Full hook mode when stepping
		if (m_mode != Mode_Continue)
		{
			mode = HookMode_Full;
		}
		SetHookMode(api, L, mode);
	}
}

bool DebugBackend::StackHasBreakpoint(LAPI api, lua_State* L)
{
	VirtualMachine* vm = GetVm(L);
	if (vm == nullptr)
		return false;

	lua_Debug functionInfo = { 0 };
	int stackIndex = 0;
	while (true)
	{
		int status = lua_getstack_dll(api, L, stackIndex, &functionInfo);
		if (status != 1)
			break;

		stackIndex = stackIndex + 1;
		lua_getinfo_dll(api, L, "S", &functionInfo);

		int linedefined = GetLineDefined(api, &functionInfo);
		if (linedefined == -1)
		{
			//ignore c functions
			continue;
		}

		vm->lastFunctions = GetSource(api, &functionInfo);

		Script* script = GetScript(GetScriptIndex(vm->lastFunctions.c_str()));

		const int lastLineDefined = GetLastLineDefined(api, &functionInfo);
		if (script != nullptr && (script->HasBreakPointInRange(linedefined, lastLineDefined) ||
			//Check if the function is the top level chunk of a source file                       
			(script->HasBreakpointsActive() && linedefined == 0 && lastLineDefined == 0)))
		{
			return true;
		}

	}

	return false;
}

size_t DebugBackend::GetScriptIndex(const char* name) const
{
	if (name == nullptr)
	{
		return -1;
	}

	auto && find = m_nameToScript.find(name);
	if (find != m_nameToScript.end())
		return find->second;

	return -1;
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
			m_evalData.result = Evaluate(
				m_evalData.api,
				m_evalData.L,
				m_evalData.expression,
				m_evalData.stackLevel,
				m_evalData.depth);
			SetEvent(m_evalResultEvent);
		}
		else break;
	}
}

void DebugBackend::WaitForEvent(HANDLE hEvent) const
{
	HANDLE hEvents[] = { hEvent, m_detachEvent };
	WaitForMultipleObjects(2, hEvents, FALSE, INFINITE);
}

bool DebugBackend::GetIsAttached() const
{
	if (m_debugPipeline == nullptr || !m_debugPipeline->IsAttached())
		return false;
	return WaitForSingleObject(m_detachEvent, 0) != WAIT_OBJECT_0;
}

void DebugBackend::HandleMessage(DebugMessage* message)
{
	switch (message->getId())
	{
	case DebugMessageId::ReqInitialize:
		{
		DMReqInitialize* init_emmy = dynamic_cast<DMReqInitialize*>(message);
		if (!m_hooked) {
			m_hooked = InstallLuaHooker(g_hInstance, init_emmy->emmyLuaFile.c_str());
			if (m_hooked && init_emmy->captureOutputDebugString)
				HookOuputDebugString();
		}

		if (m_hooked)
		{
			Message("Attach finish.\n", MessageType_Stdout);
			SetAllHookModeLazy(HookMode_Full);
			//reset detach event
			ResetEvent(m_detachEvent);
			ResetEvent(m_evalEvent);
			ResetEvent(m_evalResultEvent);
			ResetEvent(m_stepEvent);
			ResetEvent(m_loadEvent);

			if (init_emmy->captureStd && redirectorOUT == nullptr)
			{
				redirectorOUT = new StdRedirector(1);
				redirectorOUT->redirect([this](const char* buf, size_t size)
				{
					Message(buf, size, MessageType_Stdout);
				});

				redirectorERR = new StdRedirector(2);
				redirectorERR ->redirect([this](const char* buf, size_t size)
				{
					Message(buf, size, MessageType_Stderr);
				});
			}
		}
		else
		{
			Message("Attach failed.\n", MessageType_Stderr);
		}
		m_mode = Mode_Continue;
		m_emmyLuaFilePath = init_emmy->emmyLuaFile;
		m_profiler = false;
		DebugMessage resp(DebugMessageId::RespInitialize);
		m_debugPipeline->Send(&resp);

		//send scripts
		std::thread sendScriptThread([this, init_emmy]()
		{
			m_scriptsLock.Enter();
			for (Script* script : m_scripts)
			{
				if (script)
				{
					SendScript(init_emmy->L, script);
					WaitForEvent(m_loadEvent);
				}
			}
			m_scriptsLock.Exit();
		});
		sendScriptThread.detach();
		}
		break;
	case DebugMessageId::ReqStdin:
		{
		DMReqStdin* req = static_cast<DMReqStdin*>(message);
		inputStdin(req->text.c_str()); // aa\r\n
		}
		break;
	case DebugMessageId::LoadDone:
		SetEvent(m_loadEvent);
		break;
	case DebugMessageId::Continue:
		Continue();
		break;
	case DebugMessageId::StepOver:
		StepOver();
		break;
	case DebugMessageId::StepInto:
		StepInto();
		break;
	case DebugMessageId::StepOut:
		StepOut();
		break;
	case DebugMessageId::DeleteAllBreakpoints:
		DeleteAllBreakpoints();
		break;
	case DebugMessageId::AddBreakpoint:
		{
		DMAddBreakpoint* add_breakpoint = (DMAddBreakpoint*)message;
		AddBreakpoint(add_breakpoint->L, add_breakpoint->scriptIndex, add_breakpoint->line, add_breakpoint->expr);
		}
		break;
	case DebugMessageId::DelBreakpoint:
		{
		DMDelBreakpoint* del_breakpoint = (DMDelBreakpoint*)message;
		DelBreakpoint(del_breakpoint->L, del_breakpoint->scriptIndex, del_breakpoint->line);
		}
		break;
	case DebugMessageId::Break:
		Break();
		break;
	case DebugMessageId::ReqEvaluate:
		{
		DMReqEvaluate* evaluate = (DMReqEvaluate*)message;
		LAPI api = GetApiForVm(evaluate->L);

		m_evalData.expression = evaluate->expression;
		m_evalData.stackLevel = evaluate->stackLevel;
		m_evalData.depth = evaluate->depth;
		m_evalData.api = api;
		m_evalData.L = evaluate->L;
		m_evalData.result = nullptr;

		if (api != -1)
		{
			SetEvent(m_evalEvent);
			WaitForEvent(m_evalResultEvent);
		}

		DMRespEvaluate dm_eval_result;
		dm_eval_result.L = evaluate->L;
		dm_eval_result.evalId = evaluate->evalId;
		dm_eval_result.result = m_evalData.result;
		m_debugPipeline->Send(&dm_eval_result);
		}
		break;
	case DebugMessageId::Detach:
		{
		CriticalSectionLock lock(m_criticalSection);
		SetAllHookModeLazy(HookMode_None);
		EndProfiler();
		// Signal that we're detached.
		SetEvent(m_detachEvent);
		}
		break;
	case DebugMessageId::ReqProfilerBegin:
		BeginProfiler();
		break;
	case DebugMessageId::ReqProfilerEnd:
		EndProfiler();
		break;
	case DebugMessageId::ReqReloadScript:
		{
		DMReqReloadScript* rs = (DMReqReloadScript*)message;
		auto script = GetScript(rs->index);
		if (script != nullptr)
		{
			m_checkReloadNextTime = true;
			script->state = CodeState_ReqReload;
		}
		}
		break;
	default: assert(false);//"never goes here!"
	}
}

void DebugBackend::ActiveLuaHookInAllVms()
{
	//this following code may causes crash!!!
	/*for (auto pair : m_stateToVm)
	{
		auto vm = pair.second;
		//May have issues with L not being the currently running thread
		SetHookMode(vm->api, vm->L, HookMode_Full);
	}*/
	//so we set it lazy
	SetAllHookModeLazy(HookMode_Full);
}

void DebugBackend::StepInto()
{

	CriticalSectionLock lock(m_criticalSection);
	CriticalSectionLock lock_vms(m_vmsLock);

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
	CriticalSectionLock lock_vms(m_vmsLock);

	for (unsigned int i = 0; i < m_vms.size(); ++i)
	{
		m_vms[i]->callCount = 0;
	}

	m_mode = Mode_StepOver;
	SetEvent(m_stepEvent);

	ActiveLuaHookInAllVms();
}

void DebugBackend::StepOut()
{
	CriticalSectionLock lock(m_criticalSection);
	CriticalSectionLock lock_vms(m_vmsLock);

	for (unsigned int i = 0; i < m_vms.size(); ++i)
	{
		m_vms[i]->callCount = 0;
	}

	m_mode = Mode_StepOut;
	SetEvent(m_stepEvent);

	ActiveLuaHookInAllVms();
}

void DebugBackend::Continue()
{

	CriticalSectionLock lock(m_criticalSection);
	CriticalSectionLock lock_vms(m_vmsLock);

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

void DebugBackend::AddBreakpoint(lua_State* L, unsigned int scriptIndex, unsigned int line, const std::string& expr)
{
	assert(GetIsLuaLoaded());

	CriticalSectionLock lock(m_criticalSection);

	Script* script = GetScript(scriptIndex);
	if (script == nullptr)
		return;

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

	// Send back the event telling the frontend that we set/unset the breakpoint.
	DMSetBreakpoint dm_set_breakpoint;
	dm_set_breakpoint.L = L;
	dm_set_breakpoint.scriptIndex = scriptIndex;
	dm_set_breakpoint.line = line;
	if (foundValidLine)
	{
		script->AddBreakpoint(line, expr);
		BreakpointsActiveForScript(scriptIndex);

		dm_set_breakpoint.success = true;
	}
	m_debugPipeline->Send(&dm_set_breakpoint);
}

void DebugBackend::DelBreakpoint(lua_State * L, unsigned int scriptIndex, unsigned int line)
{
	assert(GetIsLuaLoaded());

	CriticalSectionLock lock(m_criticalSection);

	Script* script = GetScript(scriptIndex);
	if (script == nullptr)
		return;

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
		script->DelBreakpoint(line);

		// Send back the event telling the frontend that we set/unset the breakpoint.
		DMSetBreakpoint dm_set_breakpoint;
		dm_set_breakpoint.L = L;
		dm_set_breakpoint.scriptIndex = scriptIndex;
		dm_set_breakpoint.line = line;
		dm_set_breakpoint.success = false;
		m_debugPipeline->Send(&dm_set_breakpoint);
	}
}

void DebugBackend::BreakpointsActiveForScript(int scriptIndex)
{
	//TODO this per VM
	SetHaveActiveBreakpoints(true);
}

bool DebugBackend::GetHaveActiveBreakpoints()
{
	CriticalSectionLock lockScripts(m_scriptsLock);
	for (Script* script : m_scripts)
	{
		if (script && script->HasBreakpointsActive())
			return true;
	}

	return false;
}

void DebugBackend::SetHaveActiveBreakpoints(bool breakpointsActive)
{

	//m_HookLock.Enter();
	for (auto pair : m_stateToVm)
	{
		pair.second->haveActiveBreakpoints = breakpointsActive;
		pair.second->breakpointInStack = breakpointsActive;
	}

	//We defer to UpdateHookMode to turn off the hook fully
	if (breakpointsActive)
	{
		ActiveLuaHookInAllVms();
	}
}

void DebugBackend::DeleteAllBreakpoints()
{
	for (Script* script : m_scripts)
	{
		if (script) script->ClearBreakpoints();
	}

	//Set all haveActiveBreakpoints for the vms back to false we leave to the hook being called for the vm
	SetHaveActiveBreakpoints(false);
}

bool DebugBackend::SendBreakEvent(LAPI api, lua_State* L, int stackTop)
{

	CriticalSectionLock lock(m_criticalSection);

	VirtualMachine* vm = GetVm(L);
	if (vm == nullptr)
		return false;

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

	if (vm != nullptr)
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

	StackNodeContainer* stacksNode = new StackNodeContainer(StackNodeId::List);
	lua_Debug scriptStack[s_maxStackSize];
	unsigned int scriptStackSize = 0;

	for (int level = stackTop; scriptStackSize < s_maxStackSize && lua_getstack_dll(api, L, level, &scriptStack[scriptStackSize]); ++level)
	{
		lua_Debug& ar = scriptStack[scriptStackSize];
		lua_getinfo_dll(api, L, "nSlu", &scriptStack[scriptStackSize]);

		StackRootNode* stackNode = new StackRootNode();
		stackNode->scriptIndex = GetScriptIndex(GetSource(api, &ar));
		stackNode->line = GetCurrentLine(api, &ar) - 1;
		const char* functionName = GetName(api, &ar);
		if (functionName == nullptr)
			functionName = GetWhat(api, &ar);
		if (functionName == nullptr)
			functionName = "<unknown>";
		stackNode->functionName = functionName;
		stacksNode->AddChild(stackNode);
		const char *name;
		int j = 1;
		while ((name = lua_getlocal_dll(api, L, &ar, j++)) != nullptr) {
			if (!GetIsInternalVariable(name)) {
				StackLuaObjectNode* valueNode = GetValueAsText(api, L, -1, 1);
				valueNode->name = name;
				stackNode->AddChild(valueNode);
			}
			lua_pop_dll(api, L, 1);  /* remove variable value */
		}
		lua_getinfo_dll(api, L, "f", &ar);  /* retrieves function */
		j = 1;
		while ((name = lua_getupvalue_dll(api, L, -1, j++)) != nullptr) {
			if (!GetIsInternalVariable(name)) {
				StackLuaObjectNode* valueNode = GetValueAsText(api, L, -1, 1);
				valueNode->name = name;
				stackNode->AddChild(valueNode);
			}
			lua_pop_dll(api, L, 1);  /* remove upvalue value */
		}

		++scriptStackSize;
	}

	DMBreak dm_break(stacksNode);
	dm_break.L = L;
	return m_debugPipeline->Send(&dm_break);
}

bool DebugBackend::SendExceptionEvent(lua_State* L, const char* message)
{
	DMException dm_exception;
	dm_exception.L = L;
	dm_exception.message = message;
	return m_debugPipeline->Send(&dm_exception);
}

void DebugBackend::BreakFromScript(LAPI api, lua_State* L)
{
	CriticalSectionLock lock(m_breakLock);

	if (SendBreakEvent(api, L))
		WaitForContinue();
}

bool DebugBackend::CheckCondition(LAPI api, lua_State* L, Breakpoint * bp)
{
	if (bp == nullptr || !bp->hasCondition)
		return true;

	bool result = false;
	auto node = Evaluate(api, L, bp->condtion, 0, 1);
	if (node != nullptr)
	{
		auto child = (StackLuaObjectNode*) node->GetChild(0);
		if (child != nullptr)
		{
			std::string value = child->data;
			result = value != "false" && value != "nil";
		}
		delete node;
	}

	return result;
}

int DebugBackend::Call(LAPI api, lua_State* L, int nargs, int nresults, int errorfunc) const
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
	LAPI api = s_instance->GetApiForVm(L);
	return s_instance->ErrorHandler(api, L);
}

int DebugBackend::ErrorHandler(LAPI api, lua_State* L)
{
	// Get the error mesasge.
	const char* message = lua_tostring_dll(api, L, -1);

	if (message == nullptr)
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
			if (SendBreakEvent(api, L, 1) && SendExceptionEvent(L, message))
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

bool DebugBackend::GetStartupDirectory(char* path, int maxPathLength) const
{

	if (!GetModuleFileName(g_hInstance, path, maxPathLength))
	{
		return false;
	}

	char* lastSlash = strrchr(path, '\\');

	if (lastSlash == nullptr)
	{
		return false;
	}

	// Terminate the path after the last slash.

	lastSlash[1] = 0;
	return true;

}

void DebugBackend::ChainTables(LAPI api, lua_State* L, int child, int parent) const
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

bool DebugBackend::CreateEnvironment(LAPI api, lua_State* L, int stackLevel, int nilSentinel)
{

	int t1 = lua_gettop_dll(api, L);

	lua_Debug stackEntry = { 0 };

	if (lua_getstack_dll(api, L, stackLevel, &stackEntry) != 1)
	{
		return false;
	}

	const char* name = nullptr;

	// Copy the local variables into a new table.

	lua_newtable_dll(api, L);
	int localTable = lua_gettop_dll(api, L);

	for (int local = 1; (name = lua_getlocal_dll(api, L, &stackEntry, local)); ++local)
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

	for (int upValue = 1; (name = lua_getupvalue_dll(api, L, functionIndex, upValue)); ++upValue)
	{
		// C function up values has no name, so skip those.
		if (name && *name)
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

int IndexChained_worker(LAPI api, lua_State* L)
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
	LAPI api = (LAPI)lp;

	return IndexChained_worker(api, L);
}

int NewIndexChained_worker(LAPI api, lua_State* L)
{

	LUA_CHECK_STACK(api, L, 0)

		int key = 2;
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
	LAPI api = (LAPI)lp;

	return NewIndexChained_worker(api, L);
}


void DebugBackend::CreateChainedTable(LAPI api, lua_State* L, int nilSentinel, int localTable, int upValueTable, int globalTable)
{
	lua_newtable_dll(api, L);
	const int metaTable = lua_gettop_dll(api, L);

	// Set the __index method of the metatable.

	lua_pushstring_dll(api, L, "__index");

	lua_pushvalue_dll(api, L, nilSentinel);
	lua_pushvalue_dll(api, L, localTable);
	lua_pushvalue_dll(api, L, upValueTable);
	lua_pushvalue_dll(api, L, globalTable);

	assert(m_apis.size() > api);//"Out of apis range!"
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

void DebugBackend::SetLocals(LAPI api, lua_State* L, int stackLevel, int localTable, int nilSentinel) const
{

	lua_Debug stackEntry;

	int result = lua_getstack_dll(api, L, stackLevel, &stackEntry);
	assert(result);

	const char* name = nullptr;

	for (int local = 1; (name = lua_getlocal_dll(api, L, &stackEntry, local)); ++local)
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

void DebugBackend::SetUpValues(LAPI api, lua_State* L, int stackLevel, int upValueTable, int nilSentinel) const
{

	lua_Debug stackEntry;

	int result = lua_getstack_dll(api, L, stackLevel, &stackEntry);
	assert(result);

	// Get the function at the stack level.
	lua_getinfo_dll(api, L, "f", &stackEntry);
	int functionIndex = lua_gettop_dll(api, L);

	const char* name = nullptr;

	for (int upValue = 1; (name = lua_getupvalue_dll(api, L, functionIndex, upValue)); ++upValue)
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

EvalResultNode* DebugBackend::Evaluate(LAPI api, lua_State* L, const std::string& expression, int stackLevel, int depth)
{

	if (!GetIsLuaLoaded())
	{
		SetEvent(m_evalResultEvent);
		return nullptr;
	}

	// Adjust the desired stack level based on the number of stack levels we skipped when
	// we sent the front end the call stack.

	{

		CriticalSectionLock lock(m_criticalSection);

		auto stateIterator = m_stateToVm.find(L);
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
		return nullptr;
	}

	int envTable = lua_gettop_dll(api, L);
	int upValueTable = envTable - 1;
	int localTable = envTable - 2;

	// Disable the debugger hook so that we don't try to debug the expression.
	SetHookMode(api, L, HookMode_None);
	EnableIntercepts(api, false);
	auto vm = GetVm(L);
	vm->skipPostLoadScript = true;

	int stackTop = lua_gettop_dll(api, L);

	// Turn the expression into a statement by making it a return.

	std::string statement = "return \n";
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

	EvalResultNode* root = new EvalResultNode();

	if (error == 0)
	{
		root->success = true;
		// Figure out how many values were pushed into the stack when we evaluated the
		// expression.
		int nresults = lua_gettop_dll(api, L) - stackTop;

		//todo: If there are multiple results, create a root "values" node.
		/*if (nresults > 1)
		{

		}
		else
		{

		}*/

		for (int i = 0; i < nresults; ++i)
		{
			StackNode* node = GetValueAsText(api, L, -1 - (nresults - 1 - i), depth);

			if (node != nullptr)
			{
				root->AddChild(node);
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

		if (errorMessage == nullptr)
		{
			errorMessage = wholeMessage;
		}
		else
		{
			// Skip over the ":2: " part.
			errorMessage += 4;
		}

		std::string text = "Error: ";
		text += errorMessage;

		root->error = text;

		lua_pop_dll(api, L, 1);

	}

	//恢复现场，MS不恢复也没问题？
	// Copy any changes to the up values due to evaluating the watch back.
	SetLocals(api, L, stackLevel, localTable, nilSentinel);
	SetUpValues(api, L, stackLevel, upValueTable, nilSentinel);

	// Remove the local, up value and environment tables from the stack.
	lua_pop_dll(api, L, 3);

	// Remove the nil sentinel.
	lua_pop_dll(api, L, 1);

	// Reenable the debugger hook
	EnableIntercepts(api, true);
	SetHookMode(api, L, HookMode_Full);
	vm->skipPostLoadScript = false;

	int t2 = lua_gettop_dll(api, L);
	assert(t1 == t2);

	return root;

}

bool DebugBackend::CallMetaMethod(LAPI api, lua_State* L, int valueIndex, const char* method, int numResults, int& result) const
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

void DebugBackend::MergeTables(LAPI api, lua_State* L, unsigned int tableIndex1, unsigned int tableIndex2) const
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

StackNode* DebugBackend::GetLuaBindClassValue(LAPI api, lua_State* L, unsigned int maxDepth, bool displayAsKey) const
{

	if (!lua_checkstack_dll(api, L, 3))
	{
		return nullptr;
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
			return nullptr;
		}

	}

	const char* className = "luabind";

	// Luabind stores the accessible methods in the environment for the userdata
	// so we can directly convert that into the value.
	lua_getfenv_dll(api, L, -1);

	StackNode* node = nullptr;

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

StackLuaObjectNode* DebugBackend::GetValueAsText(LAPI api, lua_State* L, int n, int maxDepth, const char* typeNameOverride, bool displayAsKey, bool askEmmy) const
{

	int t1 = lua_gettop_dll(api, L);

	if (!lua_checkstack_dll(api, L, 1))
	{
		return nullptr;
	}

	int type = lua_type_dll(api, L, n);
	const char* typeName = lua_typename_dll(api, L, type);
	StackLuaObjectNode* node = nullptr;

	if (askEmmy)
	{
		//存value index
		lua_pushvalue_dll(api, L, n);
		int valueIndex = lua_gettop_dll(api, L);

		lua_rawgetglobal_dll(api, L, "emmy");
		int emmyTable = lua_gettop_dll(api, L);
		if (!lua_isnil_dll(api, L, emmyTable))
		{
			lua_pushstring_dll(api, L, "GetValueAsText");
			lua_rawget_dll(api, L, emmyTable);
			if (!lua_isnil_dll(api, L, -1))
			{
				lua_pushstring_dll(api, L, typeName);
				lua_pushvalue_dll(api, L, valueIndex);
				lua_pushinteger_dll(api, L, maxDepth);
				lua_pushstring_dll(api, L, typeNameOverride);
				lua_pushinteger_dll(api, L, displayAsKey);

				int r = lua_pcall_dll(api, L, 5, 1, 0);
				if (r == 0)
				{
					void* data = lua_touserdata_dll(api, L, -1);
					node = (StackLuaObjectNode*)data;
				}
				else
				{
					const char* error = lua_tostring_dll(api, L, -1);
					Get().Message(error);
				}
			}
		}
		lua_settop_dll(api, L, t1);

		if (node != nullptr)
		{
			int t2 = lua_gettop_dll(api, L);
			assert(t2 - t1 == 0);
			return node;
		}
	}

	// Duplicate the item since calling to* can modify the value.
	lua_pushvalue_dll(api, L, n);

	if (typeNameOverride == nullptr)
	{
		typeNameOverride = typeName;
	}

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

				if (numResults == 0)
				{
					lua_pushnil_dll(api, L);
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
		if (node == nullptr)
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

		size_t scriptIndex = GetScriptIndex(GetSource(api, &ar));

		auto functionNode = new StackFunctionNode();
		functionNode->scriptIndex = scriptIndex;
		functionNode->line = GetLineDefined(api, &ar) - 1;
		node = functionNode;
	}
	else
	{
		if (strcmp(typeName, "wstring") == 0)
		{

			size_t length = 0;
			const lua_WChar* string = lua_towstring_dll(api, L, -1);

			if (string != nullptr)
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

			node = new StackStringNode(text);
			node->type = typeNameOverride;
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

			node = new StackStringNode(text);
			node->type = typeNameOverride;
		}
		else if (strcmp(typeName, "userdata") == 0)
		{

			const char* temp = GetClassNameForUserdata(api, L, -1);
			std::string className;

			if (temp != nullptr)
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
			if (lua_getmetatable_dll(api, L, -1))
			{
				int tableIndex = lua_gettop_dll(api, L);
				node = GetValueAsText(api, L, tableIndex, maxDepth, className.c_str(), displayAsKey);

				lua_pop_dll(api, L, 1);
			}

			if (node == nullptr)
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

						if (numResults == 0)
						{
							lua_pushnil_dll(api, L);
							++tableIndex;
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
				
				// Check to see if we called a meta-method and got an error back.
				if (result != 0)
				{

					const char* error = lua_tostring_dll(api, L, -1);

					if (error == nullptr)
					{
						// This shouldn't happen, but we check just to make it a little
						// more robust.
						error = "Error executing __tostring";
					}

					node = new StackErrorNode(error);

					// Remove the error message.
					lua_pop_dll(api, L, 1);

				}

			}

			// tostring()
			int result = 0;
			if (CallMetaMethod(api, L, valueIndex, "__tostring", 1, result))
			{
				if (result == 0)
				{

					const char* string = lua_tostring_dll(api, L, -1);

					if (string != nullptr)
					{
						if (node == nullptr)
							node = new StackUserData(string);

						node->type = className;
						node->data = string;
					}

					// Remove the string value.
					lua_pop_dll(api, L, 1);

				}
			}

			// If we did't find a way to display the user data, just display the class name.
			if (node == nullptr)
			{

				if (!m_warnedAboutUserData)
				{
					Get().Message("Warning 1008: No __tostring or __towatch metamethod was provided for userdata", MessageType_Warning);
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

				node = new StackUserData(buffer);
				node->type = className;
			}
		}
		else
		{

			const char* string = lua_tostring_dll(api, L, -1);
			std::string result;

			if (string == nullptr)
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

			if (displayAsKey)
			{
				result = "[" + result + "]";
			}

			node = new StackPrimitiveNode(result);
			node->type = typeNameOverride;
		}

		// Remove the duplicated value.
		lua_pop_dll(api, L, 1);

	}

	int t2 = lua_gettop_dll(api, L);
	assert(t2 - t1 == 0);

	return node;

}

StackTableNode* DebugBackend::GetTableAsText(LAPI api, lua_State* L, int t, int maxDepth, const char* typeNameOverride) const
{

	if (!lua_checkstack_dll(api, L, 2))
	{
		return nullptr;
	}

	int t1 = lua_gettop_dll(api, L);

	// Get the absolute index since we need to refer to the table position
	// later once we've put additional stuff on the stack.
	t = lua_absindex_dll(api, L, t);

	if (typeNameOverride == nullptr)
		typeNameOverride = "table";

	StackTableNode* node = new StackTableNode();

	if (typeNameOverride)
	{
		node->type = typeNameOverride;
	}

	if (maxDepth > 0)
	{
		node->deep = true;
		// First key.
		lua_pushnil_dll(api, L);

		while (lua_next_dll(api, L, t) != 0)
		{
			auto key = GetValueAsText(api, L, -2, maxDepth - 1, nullptr, true);
			auto value = GetValueAsText(api, L, -1, maxDepth - 1);
			node->AddChild(key, value);

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

bool DebugBackend::GetClassNameForMetatable(LAPI api, lua_State* L, int mt) const
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

	while (lua_next_dll(api, L, t1 + 1) != 0)
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
				lua_remove_dll(api, L, -2);
				int t2 = lua_gettop_dll(api, L);
				assert(t2 - t1 == 1);

				return true;

			}
		}

		// Leave the key on the stack for the next call to lua_next.
		lua_pop_dll(api, L, 1);

	}

	// Pop global table
	lua_pop_dll(api, L, 1);

	int t2 = lua_gettop_dll(api, L);
	assert(t1 == t2);

	return false;

}

const char* DebugBackend::GetClassNameForUserdata(LAPI api, lua_State* L, int ud) const
{

	if (!lua_checkstack_dll(api, L, 2))
	{
		return nullptr;
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

	return nullptr;

}

void DebugBackend::RegisterClassName(LAPI api, lua_State* L, const char* name, int metaTable)
{

	CriticalSectionLock lock(m_criticalSection);

	ClassInfo classInfo;

	classInfo.L = L;
	classInfo.name = name;

	lua_pushvalue_dll(api, L, metaTable);
	classInfo.metaTableRef = luaL_ref_dll(api, L, GetRegistryIndex(api));

	m_classInfos.push_back(classInfo);

}

int DebugBackend::LoadScriptWithoutIntercept(LAPI api, lua_State* L, const char* buffer, size_t size, const char* name) const
{
	return lua_loadbuffer_dll(api, L, buffer, size, name, nullptr);
}

int DebugBackend::LoadScriptWithoutIntercept(LAPI api, lua_State* L, const std::string& string) const
{
	return LoadScriptWithoutIntercept(api, L, string.c_str(), string.length(), string.c_str());
}

DWORD WINAPI DebugBackend::FinishInitialize(LPVOID param)
{
	if (!Get().InitializePipeline())
		return FALSE;

	/*const char* symbolsDirectory = static_cast<const char*>(param);

	extern HINSTANCE g_hInstance;
	return static_cast<DWORD>(InstallLuaHooker(g_hInstance, symbolsDirectory));*/
	return TRUE;
}

LAPI DebugBackend::GetApiForVm(lua_State* L) const
{
	StateToVmMap::const_iterator iterator = m_stateToVm.find(L);

	if (iterator == m_stateToVm.end())
	{
		return INVALID_API;
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
	if (!GetIsAttached())
		return true;

	CriticalSectionLock lock(m_exceptionCriticalSection);
	return m_ignoreExceptions.find(message) != m_ignoreExceptions.end();
}

std::string DebugBackend::GetAsciiString(const void* buffer, size_t length, bool& wide, bool force) const
{

	wide = false;
	const char* string = reinterpret_cast<const char*>(buffer);

	if (string == nullptr)
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
	size_t convertedLength = WideCharToMultiByte(CP_UTF8, 0, (const wchar_t*)string, length / sizeof(wchar_t), result, length + 1, nullptr, nullptr);

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

	delete[] result;
	return converted;

}

void DebugBackend::CreateWeakTable(LAPI api, lua_State* L, const char* type) const
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

	if (!Get().GetIsAttached())
	{
		return 0;
	}

	LAPI api = Get().GetApiForVm(L);

	int tableIndex = lua_upvalueindex_dll(api, 1);
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

		Get().CreateGarbageCollectionSentinel(api, L);

	}

	lua_pop_dll(api, L, 1);

	return 0;

}

void DebugBackend::CreateGarbageCollectionSentinel(LAPI api, lua_State* L) const
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

void DebugBackend::SetGarbageCollectionCallback(LAPI api, lua_State* L, int index) const
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

	LAPI api = Get().GetApiForVm(L);
	lua_State* thread = static_cast<lua_State*>(lua_touserdata_dll(api, L, lua_upvalueindex_dll(api, 1)));

	if (thread != L)
	{
		// This is actually a thread and not the main thread (which for some
		// reason gets garbage collected immediately)
		Get().DetachState(api, thread);
	}

	return 0;

}

void DebugBackend::GetFileTitle(const char* name, std::string& title) const
{

	const char* slash1 = strrchr(name, '\\');
	const char* slash2 = strrchr(name, '/');

	const char* pathEnd = max(slash1, slash2);

	if (pathEnd == nullptr)
	{
		// There's no path so the whole thing is the file title.
		title = name;
	}
	else
	{
		title = pathEnd + 1;
	}

}

bool DebugBackend::EnableJit(LAPI api, lua_State* L, bool enable) const
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

void DebugBackend::LogHookEvent(LAPI api, lua_State* L, lua_Debug* ar)
{

	const char* eventType = GetHookEventName(api, ar);

	// Get some more information about the event.
	lua_getinfo_dll(api, L, "Sln", ar);

	Log("Hook Event %s, line %d %s %s\n", eventType, GetCurrentLine(api, ar), GetName(api, ar), GetSource(api, ar));

}

unsigned int DebugBackend::GetCStack(HANDLE hThread, StackEntry stack[], unsigned int maxStackSize) const
{

	const unsigned int maxNameLength = 256;
	const unsigned int maxStackFrames = 64;

	IMAGEHLP_SYMBOL64* symbol = static_cast<IMAGEHLP_SYMBOL64*>(alloca(sizeof(IMAGEHLP_SYMBOL64) + maxNameLength));

	symbol->SizeOfStruct = sizeof(IMAGEHLP_SYMBOL64);
	symbol->MaxNameLength = s_maxEntryNameLength;

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
		stack[i].line = 0;

		// Try to get the symbol name from the address.

		if (SymGetSymFromAddr64_dll(hProcess, stackFrame[i].AddrPC.Offset, nullptr, symbol))
		{
			sprintf(stack[i].name, "%s", symbol->Name);
		}
		else
		{
			sprintf(stack[i].name, "0x%llu", stackFrame[i].AddrPC.Offset);
		}

	}

	return numStackFrames;

}

int DebugBackend::GetStackDepth(LAPI api, lua_State* L) const
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
	auto stateIterator = m_stateToVm.find(L);

	if (stateIterator != m_stateToVm.end())
	{
		return stateIterator->second;
	}

	return nullptr;
}

unsigned int DebugBackend::GetUnifiedStack(LAPI api, const StackEntry nativeStack[], unsigned int nativeStackSize, const lua_Debug scriptStack[], unsigned int scriptStackSize, StackEntry stack[]) const
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
				strncmp(nativeStack[nativePos].name, "lua_", 4) != 0)
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
			if (function == nullptr || function[0] == '\0')
			{
				if (arwhat != nullptr)
				{
					function = arwhat;
				}
				else
				{
					function = "<Unknown>";
				}
			}

			if (arwhat != nullptr && strcmp(arwhat, "C") == 0)
			{
				--scriptPos;
				break;
			}

			stack[stackSize].scriptIndex = GetScriptIndex(GetSource(api, ar));
			stack[stackSize].line = GetCurrentLine(api, ar) - 1;
			stack[stackSize].scriptPos = scriptPos;

			strncpy(stack[stackSize].name, function, s_maxEntryNameLength);

			++stackSize;
			--scriptPos;

		}

	}

	return stackSize;

}

DebugBackend::Script* DebugBackend::GetScript(size_t index) const
{
	if (index < 0 || index >= m_scripts.size())
		return nullptr;
	return m_scripts[index];
}

void DebugBackend::SendScript(lua_State* L, Script* script) const
{
	DMLoadScript dm_load_script;
	dm_load_script.L = L;
	dm_load_script.fileName = script->fileName;
	dm_load_script.source = script->source;
	dm_load_script.index = script->index;
	dm_load_script.state = script->state;
	m_debugPipeline->Send(&dm_load_script);
}

void DebugBackend::CheckReload(LAPI api, lua_State* L)
{
	if (m_checkReloadNextTime)
	{
		m_checkReloadNextTime = false;

		std::vector<Script*> tempList;
		for (Script* script : m_scripts)
		{
			if (script && script->state == CodeState_ReqReload)
			{
				tempList.push_back(script);
			}
		}

		for (auto script : tempList)
		{
			Reload(api, L, script);
		}
	}
}

void DebugBackend::Reload(LAPI api, lua_State* L, Script* script)
{
	if (script->reloading)
		return;
	script->state = CodeState_Reload;
	script->reloading = true;

	InitEmmy(api, L);

	lua_rawgetglobal_dll(api, L, "emmy");
	int emmyTable = lua_gettop_dll(api, L);
	if (!lua_isnil_dll(api, L, emmyTable))
	{
		lua_pushstring_dll(api, L, "Reload");
		lua_rawget_dll(api, L, emmyTable);
		if (!lua_isnil_dll(api, L, -1))
		{
			lua_pushstring_dll(api, L, script->fileName.c_str());

			lua_pcall_dll(api, L, 1, 1, 0);
		}
	}

	// reload fail!
	if (script->state == CodeState_Reload)
		script->state = CodeState_Unavailable;
	script->reloading = false;
}
