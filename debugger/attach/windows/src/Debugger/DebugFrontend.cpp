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

#include "DebugFrontend.h"
#include "DebugEvent.h"
#include "CriticalSectionLock.h"
#include "StlUtility.h"

#include <assert.h>
#include <imagehlp.h>
#include <tlhelp32.h>
#include <iostream>
#include <psapi.h>

DebugFrontend* DebugFrontend::s_instance = NULL;

DebugFrontend& DebugFrontend::Get()
{
    if (s_instance == NULL)
    {
        s_instance = new DebugFrontend;
    }
    return *s_instance;
}

void DebugFrontend::Destroy()
{
    delete s_instance;
    s_instance = NULL;
}

DebugFrontend::DebugFrontend()
{
    m_processId     = 0;
    m_process       = NULL;
    m_eventHandler  = NULL;
    m_eventThread   = NULL;
    m_state         = State_Inactive;
}

DebugFrontend::~DebugFrontend()
{
    Stop(false);
    ClearVector(m_scripts);
}

void DebugFrontend::SetEventHandler(wxEvtHandler* eventHandler)
{
    m_eventHandler = eventHandler;
}

bool DebugFrontend::Start(const char* command, const char* commandArguments, const char* currentDirectory, const char* symbolsDirectory, bool debug, bool startBroken)
{

    Stop(false);

    STARTUPINFO startUpInfo = { 0 };
    startUpInfo.cb = sizeof(startUpInfo);

    char commandLine[8191];
    _snprintf(commandLine, sizeof(commandLine), "\"%s\" %s", command, commandArguments);

    // If no directory was specified, then use the directory from the exe.
    
    std::string directory = TrimSpaces(currentDirectory);

    if (directory.empty())
    {
        directory = GetDirectory(command);
    }

    PROCESS_INFORMATION processInfo;

    if (debug)
    {
        if (!StartProcessAndRunToEntry(command, commandLine, directory.c_str(), processInfo))
        {
            return false;
        }
    }
    else
    {

        if (!CreateProcess(NULL, commandLine, NULL, NULL, TRUE, 0, NULL, directory.c_str(), &startUpInfo, &processInfo))
        {
            OutputError(GetLastError());
            return false;
        }

        // We're not debugging, so no need to proceed.
        CloseHandle(processInfo.hThread);
        CloseHandle(processInfo.hProcess);
        return true;
    
    }

    DWORD exitCode;

    if (GetExitCodeProcess(processInfo.hProcess, &exitCode) && exitCode != STILL_ACTIVE)
    {
        MessageEvent("The process has terminated unexpectedly", MessageType_Error);
        return false;
    }

    m_process   = processInfo.hProcess;
    m_processId = processInfo.dwProcessId;

    if (!InitializeBackend(symbolsDirectory))
    {
        Stop(true);
        return false;
    }

    if (startBroken)
    {
        Break(0);
    }

    // Now that our initialization is complete, let the process run.
    ResumeThread(processInfo.hThread);
    CloseHandle(processInfo.hThread);

    return true;

}

bool DebugFrontend::Attach(unsigned int processId, const char* symbolsDirectory)
{

    m_processId = processId;
    m_process   = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (m_process == NULL)
    {
        MessageEvent("Error: The process could not be opened", MessageType_Error);
        m_processId = 0;
        return false;
    }
    
    if (!InitializeBackend(symbolsDirectory))
    {
        CloseHandle(m_process);
        m_process   = NULL;
        m_processId = 0;
        return false;
    }

    return true;

}

bool DebugFrontend::InitializeBackend(const char* symbolsDirectory)
{

    if (GetIsBeingDebugged(m_processId))
    {
        MessageEvent("Error: The process cannot be debugged because it contains hooks from a previous session", MessageType_Error);
        return false;
    }

    char eventChannelName[256];
    _snprintf(eventChannelName, 256, "Decoda.Event.%x", m_processId);

    char commandChannelName[256];
    _snprintf(commandChannelName, 256, "Decoda.Command.%x", m_processId);
    
    // Setup communication channel with the process that is used to receive events
    // back to the frontend.
    if (!m_eventChannel.Create(eventChannelName))
    {
        return false;
    }

    // Setup communication channel with the process that is used to send commands
    // to the backend.
    if (!m_commandChannel.Create(commandChannelName))
    {
        return false;
    }

    // Inject our debugger DLL into the process so that we can monitor from
    // inside the process's memory space.
    if (!InjectDll(m_processId, "LuaInject.dll"))
    {
        MessageEvent("Error: LuaInject.dll could not be loaded into the process", MessageType_Error);
        return false;
    }

    // Wait for the client to connect.
    m_eventChannel.WaitForConnection();

    // Read the initialization function from the event channel.

    if (!ProcessInitialization(symbolsDirectory))
    {
        MessageEvent("Error: Backend couldn't be initialized", MessageType_Error);
        return false;
    }

    m_state = State_Running;

    // Start a new thread to handle the incoming event channel.
    DWORD threadId;
    m_eventThread = CreateThread(NULL, 0, StaticEventThreadProc, this, 0, &threadId);

    return true;

}

bool DebugFrontend::AttachDebuggerToHost()
{

    if (m_processId != 0)
    {

        // Get the default debugger on the machine.

        std::string commandLine;
        HKEY key;
        
        if (RegOpenKey(HKEY_LOCAL_MACHINE, "SOFTWARE\\MICROSOFT\\WINDOWS NT\\CURRENTVERSION\\AEDEBUG", &key) == ERROR_SUCCESS)
        {

            DWORD type;
            DWORD size;

            if (RegQueryValueEx(key, "Debugger", NULL, &type, NULL, &size) == ERROR_SUCCESS && type == REG_SZ)
            {
                char* buffer = new char[size + 1];
                RegQueryValueEx(key, "Debugger", NULL, &type, reinterpret_cast<PBYTE>(buffer), &size);
                commandLine = buffer;
                delete [] buffer;
            }
             
            
            RegCloseKey(key);

        }

        if (!commandLine.empty())
        {

            // Substitute the process id into the command line.

            char processId[10];
            sprintf(processId, "%d", m_processId);

            ReplaceAll(commandLine, "%ld", processId);

            // Invoke the command line.

            STARTUPINFO startUpInfo = { 0 };
            startUpInfo.cb = sizeof(startUpInfo);

            PROCESS_INFORMATION processInfo;

            if (!CreateProcess(NULL, (LPTSTR)((LPCTSTR)(commandLine.c_str())), NULL, NULL, TRUE, 0,
                    NULL, NULL, &startUpInfo, &processInfo))
            {
                return false;
            }

            CloseHandle(processInfo.hProcess);
            return true;
        
        }

    }

    return false;

}

void DebugFrontend::Stop(bool kill)
{

    if (m_state != State_Inactive)
    {
        m_commandChannel.WriteUInt32(CommandId_Detach);
        m_commandChannel.WriteBool(!kill);
        m_commandChannel.Flush();
    }

    // Close the channel. This will cause the thread to exit since reading from the
    // channel will fail. Perhaps a little bit hacky.
    m_eventChannel.Destroy();
    m_commandChannel.Destroy();

    // Store the handle to the process, since when the thread exists it will close the
    // handle.
    HANDLE hProcess = NULL;
    DuplicateHandle(GetCurrentProcess(), m_process, 
        GetCurrentProcess(), &hProcess, 0, TRUE, DUPLICATE_SAME_ACCESS);

    if (m_eventThread != NULL)
    {

        // Wait for the thread to exit.
        WaitForSingleObject(m_eventThread, INFINITE);
    
        CloseHandle(m_eventThread);
        m_eventThread = NULL;
    
    }

    if (kill)
    {
        TerminateProcess(hProcess, 0);
    }

    CloseHandle(hProcess);

}

bool DebugFrontend::InjectDll(DWORD processId, const char* dllFileName)
{

    bool success = true;

    // Get the absolute path to the DLL.
     
    char fullFileName[_MAX_PATH];
    
    if (!GetStartupDirectory(fullFileName, _MAX_PATH))
    {
        return false;
    }

    strcat(fullFileName, dllFileName);

    HANDLE process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (process == NULL)
    {
        return false;
	}
	DWORD exitCode;
    
	// set dll directory
	char path[MAX_PATH];
	HANDLE hProcess = GetCurrentProcess();
	GetModuleFileNameEx(hProcess, NULL, path, MAX_PATH);
	strcpy(strrchr(path, '\\'), "");
	void* dllDirRemote = RemoteDup(process, path, strlen(path) + 1);
	ExecuteRemoteKernelFuntion(process, "SetDllDirectoryA", dllDirRemote, exitCode);

	// Load the DLL.
	void* remoteFileName = RemoteDup(process, fullFileName, strlen(fullFileName) + 1);
    if (!ExecuteRemoteKernelFuntion(process, "LoadLibraryA", remoteFileName, exitCode))
    {
        success = false;
    }
    HMODULE dllHandle = reinterpret_cast<HMODULE>(exitCode);

	// reset dll directory
	ExecuteRemoteKernelFuntion(process, "SetDllDirectoryA", NULL, exitCode);
    if (dllHandle == NULL)
    {
        success = false;
    }

    /*
    // Unload the DLL.
    // This is currently not needed since the process will automatically unload
    // the DLL when it exits, however at some point in the future we may need to
    // explicitly unload it so I'm leaving the code here.

    if (dllHandle != NULL)
    {

        if (!ExecuteRemoteKernelFuntion(process, "FreeLibrary", dllHandle, exitCode))
        {
            success = false;
        }
    
    }
    */

    if (remoteFileName != NULL)
    {
        VirtualFreeEx(process, remoteFileName, 0, MEM_RELEASE); 
        remoteFileName = NULL;
    }

    if (process != NULL)
    {
        CloseHandle(process);
    }

    return success;

}

bool DebugFrontend::GetIsBeingDebugged(DWORD processId)
{

    LPCSTR moduleFileName = "LuaInject.dll";

    HANDLE process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (process == NULL)
    {
        return false;
    }

    bool result = false;

    DWORD exitCode;
    void* remoteFileName = RemoteDup(process, moduleFileName, strlen(moduleFileName) + 1);

    if (ExecuteRemoteKernelFuntion(process, "GetModuleHandleA", remoteFileName, exitCode))
    {
        result = (exitCode != 0);
    }

    if (remoteFileName != NULL)
    {
        VirtualFreeEx(process, remoteFileName, 0, MEM_RELEASE); 
        remoteFileName = NULL;
    }

    if (process != NULL)
    {
        CloseHandle(process);
    }

    return result;

}

bool DebugFrontend::ExecuteRemoteKernelFuntion(HANDLE process, const char* functionName, LPVOID param, DWORD& exitCode)
{

    HMODULE kernelModule = GetModuleHandle("Kernel32");
    FARPROC function = GetProcAddress(kernelModule, functionName);

    if (function == NULL)
    {
        return false;
    }

    DWORD threadId;
    HANDLE thread = CreateRemoteThread(process, NULL, 0,
        (LPTHREAD_START_ROUTINE)function, param, 0, &threadId);

    if (thread != NULL)
    {
        
        WaitForSingleObject(thread, INFINITE);
        GetExitCodeThread(thread, &exitCode);
        
        CloseHandle(thread);
        return true;

    }
    else
    {
        return false;
    }

}

bool DebugFrontend::GetStartupDirectory(char* path, int maxPathLength)
{

    if (!GetModuleFileName(NULL, path, maxPathLength))
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

void DebugFrontend::EventThreadProc()
{

    unsigned int eventId;

    while (m_eventChannel.ReadUInt32(eventId))
    {

		size_t vm;
        m_eventChannel.ReadSize(vm);

        wxDebugEvent event(static_cast<EventId>(eventId), vm);

        if (eventId == EventId_LoadScript)
        {

            CriticalSectionLock lock(m_criticalSection);        

            Script* script = new Script;

            m_eventChannel.ReadString(script->name);
            m_eventChannel.ReadString(script->source);

            unsigned int codeState;
            m_eventChannel.ReadUInt32(codeState);

            script->state = static_cast<CodeState>(codeState);

            // If the debuggee does wacky things when it specifies the file name
            // we need to correct for that or it can make trying to access the
            // file bad.
            script->name = MakeValidFileName(script->name);

            unsigned int scriptIndex = m_scripts.size();
            m_scripts.push_back(script);
        
            event.SetScriptIndex(scriptIndex);

        }
        else if (eventId == EventId_Break)
        {
            m_state = State_Broken;
			std::string xml;
			m_eventChannel.ReadString(xml);
			event.SetMessageString(xml);
        }
        else if (eventId == EventId_SetBreakpoint)
        {
            
            unsigned int scriptIndex;
            m_eventChannel.ReadUInt32(scriptIndex);
            
            unsigned int line;
            m_eventChannel.ReadUInt32(line);

            unsigned int set;
            m_eventChannel.ReadUInt32(set);

            event.SetScriptIndex(scriptIndex);
            event.SetLine(line);
            event.SetEnabled(set != 0);

        }
        else if (eventId == EventId_Exception)
        {
            
            std::string message;
            m_eventChannel.ReadString(message);
            
            event.SetMessageString(message);
        
        }
        else if (eventId == EventId_LoadError)
        {

            std::string message;
            m_eventChannel.ReadString(message);
            
            event.SetMessageString(message);
        
        }
        else if (eventId == EventId_Message)
        {

            unsigned int type;
            m_eventChannel.ReadUInt32(type);

            std::string message;
            m_eventChannel.ReadString(message);
            
            event.SetMessageString(message);
            event.SetMessageType(static_cast<MessageType>(type));
        
        }        
        else if (eventId == EventId_SessionEnd)
        {
            // Backends shouldn't send this.
            assert(0);
            continue;
        }
        else if (eventId == EventId_NameVM)
        {
            
            std::string message;
            m_eventChannel.ReadString(message);
            
            event.SetMessageString(message);            

        }

        // Dispatch the message to the UI.
        if (m_eventHandler != NULL)
        {
            m_eventHandler->AddPendingEvent(event);
        }

    }

    // Send the exit event message to the UI.
    if (m_eventHandler != NULL)
    {
        wxDebugEvent event(static_cast<EventId>(EventId_SessionEnd), 0);
        m_eventHandler->AddPendingEvent(event);        
    }

}

void DebugFrontend::Shutdown()
{

    m_state = State_Inactive;

    // Clean up the scripts.
    ClearVector(m_scripts);

    // Clean up.
    CloseHandle(m_process);

    m_process   = NULL;
    m_processId = 0;

}

DWORD WINAPI DebugFrontend::StaticEventThreadProc(LPVOID param)
{
    DebugFrontend* self = static_cast<DebugFrontend*>(param);
    self->EventThreadProc();
    return 0;

}

void DebugFrontend::Continue(size_t vm)
{
    m_state = State_Running;
    m_commandChannel.WriteUInt32(CommandId_Continue);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.Flush();
}

void DebugFrontend::Break(size_t vm)
{
    m_commandChannel.WriteUInt32(CommandId_Break);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.Flush();
}

void DebugFrontend::StepOver(size_t vm)
{
    m_state = State_Running;
    m_commandChannel.WriteUInt32(CommandId_StepOver);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.Flush();
}

void DebugFrontend::StepInto(size_t vm)
{
    m_state = State_Running;
    m_commandChannel.WriteUInt32(CommandId_StepInto);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.Flush();
}

void DebugFrontend::DoneLoadingScript(size_t vm)
{
    m_commandChannel.WriteUInt32(CommandId_LoadDone);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.Flush();
}

bool DebugFrontend::Evaluate(size_t vm, int evalId, const char* expression, unsigned int stackLevel, unsigned int depath, std::string& result)
{

    if (vm == 0)
    {
        return false;
    }

    m_commandChannel.WriteUInt32(CommandId_Evaluate);
    m_commandChannel.WriteSize(vm);
	m_commandChannel.WriteString(expression);
	m_commandChannel.WriteUInt32(stackLevel);
	m_commandChannel.WriteUInt32(depath);
    m_commandChannel.Flush();

    unsigned int success;
    m_commandChannel.ReadUInt32(success);
    m_commandChannel.ReadString(result);

	wxDebugEvent event(EventId_EvalResult, vm);
	event.SetMessageString(result);
	event.SetEvalResult(success == 1);
	event.SetEvalId(evalId);
	m_eventHandler->AddPendingEvent(event);

    return success != 0;

}

void DebugFrontend::ToggleBreakpoint(size_t vm, unsigned int scriptIndex, unsigned int line)
{

    m_commandChannel.WriteUInt32(CommandId_ToggleBreakpoint);
    m_commandChannel.WriteSize(vm);
    m_commandChannel.WriteUInt32(scriptIndex);
    m_commandChannel.WriteUInt32(line);
    m_commandChannel.Flush();

}

void DebugFrontend::RemoveAllBreakPoints(size_t vm)
{

    m_commandChannel.WriteUInt32(CommandId_DeleteAllBreakpoints);
    m_commandChannel.WriteUInt32(0);
    m_commandChannel.Flush();

}

DebugFrontend::Script* DebugFrontend::GetScript(unsigned int scriptIndex)
{
    CriticalSectionLock lock(m_criticalSection);
    if (scriptIndex == -1)
    {
        return NULL;
    }
    else
    {
        return m_scripts[scriptIndex];
    }
}

unsigned int DebugFrontend::GetScriptIndex(const char* name) const
{

    for (unsigned int i = 0; i < m_scripts.size(); ++i)
    {
        if (m_scripts[i]->name == name)
        {
            return i;
        }
    }

    return -1;

}

size_t DebugFrontend::GetNumStackFrames() const
{
    return m_stackFrames.size();
}

const DebugFrontend::StackFrame& DebugFrontend::GetStackFrame(unsigned int i) const
{
    return m_stackFrames[i];
}

DebugFrontend::State DebugFrontend::GetState() const
{
    return m_state;
}

void DebugFrontend::MessageEvent(const std::string& message, MessageType type)
{

    wxDebugEvent event(EventId_Message, 0);
    event.SetMessageString(message);
    event.SetMessageType(type);

    // Dispatch the message to the UI.
    if (m_eventHandler != NULL)
    {
        m_eventHandler->AddPendingEvent(event);
    }

}

bool DebugFrontend::ProcessInitialization(const char* symbolsDirectory)
{

    unsigned int command;
    m_eventChannel.ReadUInt32(command);

    if (command != EventId_Initialize)
    {
        return false;
    }

	size_t function;
    m_eventChannel.ReadSize(function);

    // Call the initializtion function.

    void* remoteSymbolsDirectory = RemoteDup(m_process, symbolsDirectory, strlen(symbolsDirectory) + 1);
    
    DWORD threadId;
    HANDLE thread = CreateRemoteThread(m_process, NULL, 0, (LPTHREAD_START_ROUTINE)function, remoteSymbolsDirectory, 0, &threadId);

    if (thread == NULL)
    {
        return false;
    }

    DWORD exitCode;
    WaitForSingleObject(thread, INFINITE);
    GetExitCodeThread(thread, &exitCode);
    
    CloseHandle(thread);
    thread = NULL;

    return exitCode != 0;

}

std::string DebugFrontend::MakeValidFileName(const std::string& name)
{

    std::string result;

    for (unsigned int i = 0; i < name.length(); ++i)
    {
        if (name[i] == ':')
        {
            if (i + 1 == name.length() || !GetIsSlash(name[i + 1]))
            {
                result += '_';
                continue;
            }
        }

        result += name[i];

    }
    
    return result;

}

HWND DebugFrontend::GetProcessWindow(DWORD processId) const
{

    HWND hWnd = FindWindowEx(NULL, NULL, NULL, NULL);

    while (hWnd != NULL)
    {

        if (GetParent(hWnd) == NULL && GetWindowTextLength(hWnd) > 0 && IsWindowVisible(hWnd))
        {

            DWORD windowProcessId;
            GetWindowThreadProcessId(hWnd, &windowProcessId);

            if (windowProcessId == processId)
            {
                // Found a match.
                break;
            }

        }
        
        hWnd = GetWindow(hWnd, GW_HWNDNEXT);

    }

    return hWnd;

}

void DebugFrontend::GetProcesses(std::vector<Process>& processes) const
{

    // Get the id of this process so that we can filter it out of the list.
    DWORD currentProcessId = GetCurrentProcessId();

    HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

    if (snapshot != INVALID_HANDLE_VALUE)
    {
        
        PROCESSENTRY32 processEntry = { 0 };
        processEntry.dwSize = sizeof(processEntry);

        if (Process32First(snapshot, &processEntry))
        {
            do
            {
                if (processEntry.th32ProcessID != currentProcessId && processEntry.th32ProcessID != 0)
                {

                    Process process;

                    process.id   = processEntry.th32ProcessID;
                    process.name = processEntry.szExeFile;
                    
                    
                    HWND hWnd = GetProcessWindow(processEntry.th32ProcessID);

                    if (hWnd != NULL)
                    {
                        char buffer[1024];
                        GetWindowText(hWnd, buffer, 1024);
                        process.title = buffer;
                    }

                    processes.push_back(process);

                }
            }
            while (Process32Next(snapshot, &processEntry));
        
        }

        CloseHandle(snapshot);

    }

}

void DebugFrontend::IgnoreException(const std::string& message)
{
    m_commandChannel.WriteUInt32(CommandId_IgnoreException);
    m_commandChannel.WriteString(message);
    m_commandChannel.Flush();
}

void* DebugFrontend::RemoteDup(HANDLE process, const void* source, size_t length)
{
    void* remote = VirtualAllocEx(process, NULL, length, MEM_COMMIT, PAGE_READWRITE);
	SIZE_T numBytesWritten;
    WriteProcessMemory(process, remote, source, length, &numBytesWritten);
    return remote;
}

unsigned int DebugFrontend::GetNumLines(const std::string& source) const
{

    unsigned int numLines = 1;

    for (unsigned int i = 0; i < source.length(); ++i)
    {
        if (source[i] == '\n')
        {
            ++numLines;
        }
    }

    return numLines;

}

bool DebugFrontend::GetExeInfo(LPCSTR fileName, ExeInfo& info) const
{
    
    LOADED_IMAGE loadedImage;
    if (!MapAndLoad(const_cast<PSTR>(fileName), NULL, &loadedImage, FALSE, TRUE))
    {
        return false;
    }

    // Check if this is a managed application.
    // http://www.codeguru.com/cpp/w-p/system/misc/print.php/c14001

    info.managed = false;
    if (loadedImage.FileHeader->Signature == IMAGE_NT_SIGNATURE)
    {
       
        DWORD netHeaderAddress =
            loadedImage.FileHeader->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR].VirtualAddress;

        if (netHeaderAddress)
        {
            info.managed = true;
        }
    
    }
    
    info.entryPoint = loadedImage.FileHeader->OptionalHeader.AddressOfEntryPoint;
    info.i386       = loadedImage.FileHeader->FileHeader.Machine == IMAGE_FILE_MACHINE_I386;

    UnMapAndLoad(&loadedImage);

    return true;

}

void DebugFrontend::SetBreakpoint(HANDLE hProcess, LPVOID entryPoint, bool set, BYTE* data) const
{

    DWORD protection;

    // Give ourself write access to the region.
    if (VirtualProtectEx(hProcess, entryPoint, 1, PAGE_EXECUTE_READWRITE, &protection))
    {

        BYTE buffer[1];
        
        if (set)
        {

			SIZE_T numBytesRead;
            ReadProcessMemory(hProcess, entryPoint, data, 1, &numBytesRead);

            // Write the int 3 instruction.
            buffer[0] = 0xCC;

        }
        else
        {
            // Restore the original byte.
            buffer[0] = data[0];
        }

		SIZE_T numBytesWritten;
        WriteProcessMemory(hProcess, entryPoint, buffer, 1, &numBytesWritten);

        // Restore the original protections.
        VirtualProtectEx(hProcess, entryPoint, 1, protection, &protection);

        // Flush the cache so we know that our new code gets executed.
        FlushInstructionCache(hProcess, entryPoint, 1);

    }

}

bool DebugFrontend::StartProcessAndRunToEntry(LPCSTR exeFileName, LPSTR commandLine, LPCSTR directory, PROCESS_INFORMATION& processInfo)
{

    STARTUPINFO startUpInfo = { 0 };
    startUpInfo.cb = sizeof(startUpInfo);

    ExeInfo info;
    if (!GetExeInfo(exeFileName, info) || info.entryPoint == 0)
    {
        MessageEvent("Error: The entry point for the application could not be located", MessageType_Error);
        return false;
    }

    /*if (!info.i386)
    {
        MessageEvent("Error: Debugging 64-bit applications is not supported", MessageType_Error);
        return false;
    }*/

    DWORD flags = DEBUG_PROCESS | DEBUG_ONLY_THIS_PROCESS;

    if (!CreateProcess(NULL, commandLine, NULL, NULL, TRUE, flags, NULL, directory, &startUpInfo, &processInfo))
    {
        OutputError(GetLastError());
        return false;
    }

    // Running to the entry point currently doesn't work for managed applications, so
    // just start it up.

    if (!info.managed)
    {

        size_t entryPoint = info.entryPoint;

        BYTE breakPointData;
        bool done = false;
        
        while (!done)
        {

            DEBUG_EVENT debugEvent;
            WaitForDebugEvent(&debugEvent, INFINITE);

            DWORD continueStatus = DBG_EXCEPTION_NOT_HANDLED;

            if (debugEvent.dwDebugEventCode == EXCEPTION_DEBUG_EVENT)
            {
                if (debugEvent.u.Exception.ExceptionRecord.ExceptionCode == EXCEPTION_SINGLE_STEP ||
                    debugEvent.u.Exception.ExceptionRecord.ExceptionCode == EXCEPTION_BREAKPOINT)
                {

                    CONTEXT context;
                    context.ContextFlags = CONTEXT_FULL;

                    GetThreadContext(processInfo.hThread, &context);
#if _WIN64
					if (context.Rip == entryPoint + 1)
#else
					if (context.Eip == entryPoint + 1)
#endif
                    
                    {

                        // Restore the original code bytes.
                        SetBreakpoint(processInfo.hProcess, (LPVOID)entryPoint, false, &breakPointData);
                        done = true;

                        // Backup the instruction pointer so that we execute the original instruction.
#if _WIN64
                        --context.Rip;
#else
						--context.Eip;
#endif
                        SetThreadContext(processInfo.hThread, &context);

                        // Suspend the thread before we continue the debug event so that the program
                        // doesn't continue to run.
                        SuspendThread(processInfo.hThread);

                    }

                    continueStatus = DBG_CONTINUE;

                }
            }
            else if (debugEvent.dwDebugEventCode == EXIT_PROCESS_DEBUG_EVENT)
            {
                done = true;
            }
            else if (debugEvent.dwDebugEventCode == CREATE_PROCESS_DEBUG_EVENT)
            {
            
                // Offset the entry point by the load address of the process.
                entryPoint += reinterpret_cast<size_t>(debugEvent.u.CreateProcessInfo.lpBaseOfImage);

                // Write a break point at the entry point of the application so that we
                // will stop when we reach that point.
                SetBreakpoint(processInfo.hProcess, reinterpret_cast<void*>(entryPoint), true, &breakPointData);

                CloseHandle(debugEvent.u.CreateProcessInfo.hFile);

            }
            else if (debugEvent.dwDebugEventCode == LOAD_DLL_DEBUG_EVENT)
            {
                CloseHandle(debugEvent.u.LoadDll.hFile);
            }

            ContinueDebugEvent(debugEvent.dwProcessId, debugEvent.dwThreadId, continueStatus);
        
        }

    }

    DebugActiveProcessStop(processInfo.dwProcessId);
    return true;

}

void DebugFrontend::OutputError(DWORD error)
{

    char buffer[1024];
    if (FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, NULL, error, 0, buffer, 1024,  NULL))
    {
        std::string message = "Error: ";
        message += buffer;
        MessageEvent(message, MessageType_Error);
    }

}