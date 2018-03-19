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

#ifndef DEBUG_FRONTEND_H
#define DEBUG_FRONTEND_H

#include <Windows.h>

#include "Channel.h"
#include "Protocol.h"

enum class ErrorCode
{
	OK = 0,

	UNKNOWN = 1,
	CAN_NOT_OPEN_PROCESS = 2,
	ALREADY_ATTACHED = 3,
	INJECT_ERROR = 4,
	BACKEND_INIT_ERROR = 5,
};

/**
 * Frontend for the debugger.
 */
class DebugFrontend
{

public:

    /**
     * Singleton accessor.
     */
    static DebugFrontend& Get();
    
    /**
     * Destroys the singleton.
     */
    static void Destroy();

    /**
     * Starts a new process that will be debugged.
     */
	ErrorCode Start(const char* command,
		const char* commandArguments,
		const char* currentDirectory,
		const char* symbolsDirectory,
		bool debug,
		bool console,
		bool startBroken);

	void Resume();

    /**
     * Attaches the debugger to a currently running process.
     */
    ErrorCode Attach(unsigned int processId, const char* symbolsDirectory);

    /**
     * Attaches the default debugger (set on the machine) to the application
     * hosting the scripting language.
     */
    bool AttachDebuggerToHost() const;

    /**
     * Stops the debugger. If kill is true the debugee process will be
     * terminated. Otherwise the debugger will merely be detached.
     */
    void Stop(bool kill);
private:

    /**
     * Constructor.
     */
    DebugFrontend();
    
    /**
     * Destructor.
     */
    ~DebugFrontend();

    /**
     * Injects a DLL into a process. This causes the DLL to be loaded into that
     * process, allowing for manipulation in that processes memory space.
     */
    bool InjectDll(DWORD processId, const char* dllFileName) const;

    /**
     * Executes an OS kernel function call inside another process.
     */
    bool ExecuteRemoteKernelFuntion(HANDLE process, const char* functionName, LPVOID param, DWORD& exitCode) const;

    /**
     * Gets the path that the EXE resides in.
     */
    bool GetStartupDirectory(char* path, int maxPathLength) const;

    /**
     * Sends a message event.
     */
    void MessageEvent(const std::string& message, MessageType type = MessageType_Normal) const;

    /**
     * Handles the initialzation handshake between the frontend and the backend.
     * This includes calling the DLLs post-load initialization function. If there
     * was an error the method returns false.
     */
    bool ProcessInitialization(Channel& handshakeChannel, const char* symbolsDirectory);

    /**
     * Removes characters from the file name that would make it invalid (colons)
     */
    std::string MakeValidFileName(const std::string& name) const;

    /**
     * Initializes the debugger backend for the currently started process.
     */
	ErrorCode InitializeBackend(const char* symbolsDirectory);

    /**
     * Duplicates a string into the memory of the specified process.
     */
    void* RemoteDup(HANDLE process, const void* string, size_t len) const;

    /**
     * Returns true if the specified process is currently being debugged by Decoda (or
     * was debugged at some point).
     */
    bool GetIsBeingDebugged(DWORD processId) const;

    /**
     * Writes or clears a breakpoint (interrupt 3) at the address in the specified
     * process. When writing a breakpoint, the overwritten byte is store in data. The
     * same value should be passed in when later clearing the breakpoint.
     */
    void SetBreakpoint(HANDLE hProcess, LPVOID entryPoint, bool set, BYTE* data) const;

    /**
     * Starts up a process and runs it until the entry point of the executable (i.e. runs
     * the windows startup code and suspends execution at the beginning of the real code)
     */
    bool StartProcessAndRunToEntry(LPCSTR exeFileName,
		LPSTR commandLine,
		LPCSTR directory,
		PROCESS_INFORMATION& processInfo,
		bool console);

    /**
     * Outputs the error message for Win32 error code.
     */
    void OutputError(DWORD error) const;

	void Output(std::string &message) const;

    static DebugFrontend*       s_instance;

	PROCESS_INFORMATION			processInfo;

    DWORD                       m_processId;
    HANDLE                      m_process;

    HANDLE                      m_eventThread;
};

#endif