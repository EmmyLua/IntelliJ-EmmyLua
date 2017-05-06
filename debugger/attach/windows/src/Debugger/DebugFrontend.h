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

#include <windows.h>
#include <string>
#include <vector>

#include "wxEvtHandler.h"
#include "Channel.h"
#include "Protocol.h"
#include "CriticalSection.h"
#include "LineMapper.h"

/**
 * Frontend for the debugger.
 */
class DebugFrontend
{

public:

    struct Script
    {
        std::string     name;       // Identifying name of the script (usually a file name)
        std::string     source;     // Source code for the script
        CodeState       state;
        LineMapper      lineMapper; // Current mapping from lines in the local file to backend script lines.
    };

    struct StackFrame
    {
        unsigned int    scriptIndex;
        unsigned int    line;
        std::string     function;
    };

    enum State
    {
        State_Inactive,         // Not debugging.
        State_Running,          // Debugging a program is is currently running.
        State_Broken,           // Debugging a program is is currently break point.
    };

    struct Process
    {
        unsigned int    id;     // Windows process identifier
        std::string     name;   // Executable name
        std::string     title;  // Name from the main window of the process.
    };

    /**
     * Singleton accessor.
     */
    static DebugFrontend& Get();
    
    /**
     * Destroys the singleton.
     */
    static void Destroy();

    /**
     * Set the event handler for messages from the client.
     */
    void SetEventHandler(wxEvtHandler* eventHandler);
 
    /**
     * Starts a new process that will be debugged.
     */
    bool Start(const char* command, const char* commandArguments, const char* currentDirectory, const char* symbolsDirectory, bool debug, bool startBroken);

    /**
     * Cleans up after the debugger has disconnected from the debugee. This should be
     * called after processing the end event.
     */
    void Shutdown();

    /**
     * Attaches the debugger to a currently running process.
     */
    bool Attach(unsigned int processId, const char* symbolsDirectory);

    /**
     * Attaches the default debugger (set on the machine) to the application
     * hosting the scripting language.
     */
    bool AttachDebuggerToHost();

    /**
     * Stops the debugger. If kill is true the debugee process will be
     * terminated. Otherwise the debugger will merely be detached.
     */
    void Stop(bool kill);

    /**
     * Instructs the debugger to continue until it hits the next breakpoint.
     */
    void Continue(size_t vm);

    /**
     * Instructs the debugger to break on the next line of script code it
     * executes. Since the process being deugged may not currently be executing
     * script code, this may not happen immediately.
     */
    void Break(size_t vm);

    /**
     * Instructs the debugger to step to the next line. If the current line
     * is a function this will step over the function.
     */
    void StepOver(size_t vm);

    /**
     * Instructs the debugger to step to the next line. If the current line
     * is a function this will step into the function.
     */
    void StepInto(size_t vm);

    /**
     * Signals to the debugger that we've finished the processing we needed to
     * do in response to a load script event.
     */
    void DoneLoadingScript(size_t vm);

    /**
     * Evaluates the expression in the current context.
     */
	bool Evaluate(size_t vm, int evalId, const char * expression, unsigned int stackLevel, unsigned int depath, std::string & result);

    /**
     * Toggles a breakpoint on the specified line.
     */
    void ToggleBreakpoint(size_t vm, unsigned int scriptIndex, unsigned int line);
    
    /**
     * Removes all breakpoints set this will also disable the line hook if the debug mode is set to continue
     */
    void RemoveAllBreakPoints(size_t vm);

    /**
     * Returns the specified script.
     */
    Script* GetScript(unsigned int scriptIndex);

    /**
     * Returns the index of the script with te specified name. If the name could not be
     * matched the method returns -1.
     */
    unsigned int GetScriptIndex(const char* name) const;

    /**
     * Returns the number of frames in the call stack.
     */
    size_t GetNumStackFrames() const;

    /**
     * Returns the ith stack frame. The frames are numbered so that 0 is the
     * top (current location) of the stack.
     */
    const StackFrame& GetStackFrame(unsigned int i) const;

    /**
     * Returns the current state of the debugger.
     */
    State GetState() const;

    /**
     * Returns all of the processes on the machine that can be debugged.
     */
    void GetProcesses(std::vector<Process>& processes) const;

    /**
     * Instructs the backend to ignore the specified exception whenever it happens
     * in the future.
     */
    void IgnoreException(const std::string& message);

private:

    struct ExeInfo
    {
        size_t			entryPoint;
        bool            managed;
        bool            i386;
    };

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
    bool InjectDll(DWORD processId, const char* dllFileName);

    /**
     * Executes an OS kernel function call inside another process.
     */
    bool ExecuteRemoteKernelFuntion(HANDLE process, const char* functionName, LPVOID param, DWORD& exitCode);

    /**
     * Gets the path that the EXE resides in.
     */
    bool GetStartupDirectory(char* path, int maxPathLength);
    
    /**
     * Entry point into the event handling thread.
     */
    void EventThreadProc();

    /**
     * Static version of the event handler thread entry point. This just
     * forwards to the non-static version.
     */
    static DWORD WINAPI StaticEventThreadProc(LPVOID param);

    /**
     * Called when the break event is received.
     */
    void OnBreak();

    /**
     * Sends a message event.
     */
    void MessageEvent(const std::string& message, MessageType type = MessageType_Normal);

    /**
     * Handles the initialzation handshake between the frontend and the backend.
     * This includes calling the DLLs post-load initialization function. If there
     * was an error the method returns false.
     */
    bool ProcessInitialization(const char* symbolsDirectory);

    /**
     * Removes characters from the file name that would make it invalid (colons)
     */
    std::string MakeValidFileName(const std::string& name);

    /**
     * Returns the top level window for the specified process. The first such window
     * that's found is returned.
     */
    HWND GetProcessWindow(DWORD processId) const;

    /**
     * Initializes the debugger backend for the currently started process.
     */
    bool InitializeBackend(const char* symbolsDirectory);

    /**
     * Duplicates a string into the memory of the specified process.
     */
    void* RemoteDup(HANDLE process, const void* string, size_t len);

    /**
     * Counts the number of lines in a piece of text.
     */
    unsigned int GetNumLines(const std::string& source) const;

    /**
     * Returns true if the specified process is currently being debugged by Decoda (or
     * was debugged at some point).
     */
    bool GetIsBeingDebugged(DWORD processId);

    /**
     * Gets the entry point for the specified executable file from the PE data. If the PE
     * is a .NET/managed application the managed parameter will be set to true.
     */
    bool GetExeInfo(LPCSTR fileName, ExeInfo& info) const;

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
    bool StartProcessAndRunToEntry(LPCSTR exeFileName, LPSTR commandLine, LPCSTR directory, PROCESS_INFORMATION& processInfo);

    /**
     * Outputs the error message for Win32 error code.
     */
    void OutputError(DWORD error);

private:

    static DebugFrontend*       s_instance;

    DWORD                       m_processId;
    HANDLE                      m_process;

    wxEvtHandler*               m_eventHandler;    
    Channel                     m_eventChannel;
    HANDLE                      m_eventThread;

    Channel                     m_commandChannel;

    mutable CriticalSection     m_criticalSection;
    std::vector<Script*>        m_scripts;

    std::vector<StackFrame>     m_stackFrames;

    State                       m_state;

};

#endif