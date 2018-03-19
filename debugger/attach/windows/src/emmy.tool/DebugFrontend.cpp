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
#include "StlUtility.h"
#include "Utility.h"

#include <iostream>
#include <Windows.h>
#include <ImageHlp.h>
#include <cassert>
#include <Psapi.h>

#define DBG_BACKEND_DLL "emmy.backend.dll"

DebugFrontend* DebugFrontend::s_instance = nullptr;

DebugFrontend& DebugFrontend::Get()
{
    if (s_instance == nullptr)
    {
        s_instance = new DebugFrontend;
    }
    return *s_instance;
}

void DebugFrontend::Destroy()
{
    delete s_instance;
    s_instance = nullptr;
}

DebugFrontend::DebugFrontend()
{
    m_processId     = 0;
    m_process       = nullptr;
    m_eventThread   = nullptr;
}

DebugFrontend::~DebugFrontend()
{
    Stop(false);
}

ErrorCode DebugFrontend::Start(
	const char* command,
	const char* commandArguments,
	const char* currentDirectory,
	const char* symbolsDirectory,
	bool debug,
	bool console,
	bool startBroken)
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

    if (debug)
    {
        if (!StartProcessAndRunToEntry(command, commandLine, directory.c_str(), processInfo, console))
        {
            return ErrorCode::UNKNOWN;
        }
    }
    else
    {

        if (!CreateProcess(nullptr, commandLine, nullptr, nullptr, TRUE, 0, nullptr, directory.c_str(), &startUpInfo, &processInfo))
        {
            OutputError(GetLastError());
            return ErrorCode::UNKNOWN;
        }

        // We're not debugging, so no need to proceed.
        CloseHandle(processInfo.hThread);
        CloseHandle(processInfo.hProcess);
        return ErrorCode::OK;
    }

    DWORD exitCode;

    if (GetExitCodeProcess(processInfo.hProcess, &exitCode) && exitCode != STILL_ACTIVE)
    {
        MessageEvent("The process has terminated unexpectedly", MessageType_Error);
        return ErrorCode::UNKNOWN;
    }

    m_process   = processInfo.hProcess;
    m_processId = processInfo.dwProcessId;

	ErrorCode code = InitializeBackend(symbolsDirectory);
    if (code != ErrorCode::OK)
    {
		MessageEvent("The backend can't be initialized", MessageType_Error);
        Stop(true);
        return code;
    }

	//tell IDEA debugger connect
	u_short port = m_processId;
	std::cout << "port:" << port << std::endl;
    return ErrorCode::OK;
}

void DebugFrontend::Resume()
{
	// Now that our initialization is complete, let the process run.
	ResumeThread(processInfo.hThread);
	CloseHandle(processInfo.hThread);
}

ErrorCode DebugFrontend::Attach(unsigned int processId, const char* symbolsDirectory)
{

    m_processId = processId;
    m_process   = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (m_process == nullptr)
    {
        MessageEvent("The process could not be opened", MessageType_Error);
        m_processId = 0;
        return ErrorCode::CAN_NOT_OPEN_PROCESS;
    }
    
	ErrorCode code = InitializeBackend(symbolsDirectory);
    if (code != ErrorCode::OK)
    {
		MessageEvent("Backend couldn't be initialized", MessageType_Error);
        CloseHandle(m_process);
        m_process   = nullptr;
        m_processId = 0;
        return code;
    }

	//tell IDEA debugger connect
	u_short port = m_processId;
	std::cout << "port:" << port << std::endl;
    return ErrorCode::OK;

}

ErrorCode DebugFrontend::InitializeBackend(const char* symbolsDirectory)
{
	//MessageBox(nullptr, "Waiting to attach the debugger", nullptr, MB_OK);

    if (GetIsBeingDebugged(m_processId))
    {
        MessageEvent("The process already attached.");
        return ErrorCode::OK;//ALREADY_ATTACHED
    }

	MessageEvent("Initialize backend ...");
	// Handshake channel
	char handshakeChannelName[256];
	_snprintf(handshakeChannelName, 256, "Decoda.Handshake.%x", m_processId);
	Channel handshakeChannel;
	if (!handshakeChannel.Create(handshakeChannelName))
	{
		return ErrorCode::UNKNOWN;
	}

    // Inject our debugger DLL into the process so that we can monitor from
    // inside the process's memory space.
    if (!InjectDll(m_processId, DBG_BACKEND_DLL))
    {
        MessageEvent("emmy.backend.dll could not be loaded into the process", MessageType_Error);
        return ErrorCode::INJECT_ERROR;
    }

    // Wait for the client to connect.
	handshakeChannel.WaitForConnection();

    // Read the initialization function from the event channel.
    if (!ProcessInitialization(handshakeChannel, symbolsDirectory))
    {
        MessageEvent("Backend couldn't be initialized", MessageType_Error);
        return ErrorCode::BACKEND_INIT_ERROR;
    }

    return ErrorCode::OK;
}

bool DebugFrontend::AttachDebuggerToHost() const
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

            if (RegQueryValueEx(key, "Debugger", nullptr, &type, nullptr, &size) == ERROR_SUCCESS && type == REG_SZ)
            {
                char* buffer = new char[size + 1];
                RegQueryValueEx(key, "Debugger", nullptr, &type, reinterpret_cast<PBYTE>(buffer), &size);
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

            if (!CreateProcess(nullptr, (LPTSTR)((LPCTSTR)(commandLine.c_str())), nullptr, nullptr, TRUE, 0,
                    nullptr, nullptr, &startUpInfo, &processInfo))
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
    // Store the handle to the process, since when the thread exists it will close the
    // handle.
    HANDLE hProcess = nullptr;
    DuplicateHandle(GetCurrentProcess(), m_process, 
        GetCurrentProcess(), &hProcess, 0, TRUE, DUPLICATE_SAME_ACCESS);

    if (m_eventThread != nullptr)
    {

        // Wait for the thread to exit.
        WaitForSingleObject(m_eventThread, INFINITE);
    
        CloseHandle(m_eventThread);
        m_eventThread = nullptr;
    
    }

    if (kill)
    {
        TerminateProcess(hProcess, 0);
    }

    CloseHandle(hProcess);

}

bool DebugFrontend::InjectDll(DWORD processId, const char* dllFileName) const
{
	MessageEvent("Start inject dll ...");
    bool success = true;

    // Get the absolute path to the DLL.
     
    char fullFileName[_MAX_PATH];
    
    if (!GetStartupDirectory(fullFileName, _MAX_PATH))
    {
		MessageEvent("GetStartupDirectory = false");
        return false;
    }

    strcat(fullFileName, dllFileName);

    HANDLE process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (process == nullptr)
    {
		MessageEvent("Failed to open process");
		OutputError(GetLastError());
        return false;
	}
	DWORD exitCode;
    
	// set dll directory
	char path[MAX_PATH];
	HANDLE hProcess = GetCurrentProcess();
	GetModuleFileNameEx(hProcess, nullptr, path, MAX_PATH);
	strcpy(strrchr(path, '\\'), "");
	void* dllDirRemote = RemoteDup(process, path, strlen(path) + 1);
	ExecuteRemoteKernelFuntion(process, "SetDllDirectoryA", dllDirRemote, exitCode);

	// Load the DLL.
	void* remoteFileName = RemoteDup(process, fullFileName, strlen(fullFileName) + 1);
    if (!ExecuteRemoteKernelFuntion(process, "LoadLibraryA", remoteFileName, exitCode))
    {
		MessageEvent("Failed to load library");
        success = false;
    }
    HMODULE dllHandle = reinterpret_cast<HMODULE>(exitCode);
	if (dllHandle == nullptr)
	{
		success = false;
		OutputError(GetLastError());
	}

	// reset dll directory
	ExecuteRemoteKernelFuntion(process, "SetDllDirectoryA", nullptr, exitCode);

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

    if (remoteFileName != nullptr)
    {
        VirtualFreeEx(process, remoteFileName, 0, MEM_RELEASE);
    }

    if (process != nullptr)
    {
        CloseHandle(process);
    }

	if (success)
	{
		MessageEvent("Successfully inject dll!");
	}

    return success;

}

bool DebugFrontend::GetIsBeingDebugged(DWORD processId) const
{

    LPCSTR moduleFileName = DBG_BACKEND_DLL;

    HANDLE process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);

    if (process == nullptr)
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

    if (remoteFileName != nullptr)
    {
        VirtualFreeEx(process, remoteFileName, 0, MEM_RELEASE);
    }

    if (process != nullptr)
    {
        CloseHandle(process);
    }

    return result;

}

bool DebugFrontend::ExecuteRemoteKernelFuntion(HANDLE process, const char* functionName, LPVOID param, DWORD& exitCode) const
{

    HMODULE kernelModule = GetModuleHandle("Kernel32");
    FARPROC function = GetProcAddress(kernelModule, functionName);

    if (function == nullptr)
    {
        return false;
    }

    DWORD threadId;
    HANDLE thread = CreateRemoteThread(process, nullptr, 0,
        (LPTHREAD_START_ROUTINE)function, param, 0, &threadId);

    if (thread != nullptr)
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

bool DebugFrontend::GetStartupDirectory(char* path, int maxPathLength) const
{

    if (!GetModuleFileName(nullptr, path, maxPathLength))
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

void DebugFrontend::MessageEvent(const std::string& message, MessageType type) const
{
	std::cout << "[F]" << message << std::endl;
}

bool DebugFrontend::ProcessInitialization(Channel& handshakeChannel, const char* symbolsDirectory)
{
	MessageEvent("Initialize handshake channel");
    unsigned int command;
	handshakeChannel.ReadUInt32(command);

    if (command != EventId_Initialize)
    {
		MessageEvent("Initialize handshake channel failed", MessageType_Error);
        return false;
    }
	MessageEvent("Handshake ok");

	uint64_t function;
	handshakeChannel.ReadUint64(function);

	MessageEvent("Initialize backend...");
    // Call the initializtion function.
    void* remoteSymbolsDirectory = RemoteDup(m_process, symbolsDirectory, strlen(symbolsDirectory) + 1);
    
    DWORD threadId;
    HANDLE thread = CreateRemoteThread(m_process, nullptr, 0, (LPTHREAD_START_ROUTINE)function, remoteSymbolsDirectory, 0, &threadId);

    if (thread == nullptr)
    {
		MessageEvent("Failed to initialize backend 1");
		OutputError(GetLastError());
        return false;
    }

    DWORD exitCode;
    WaitForSingleObject(thread, INFINITE);
    GetExitCodeThread(thread, &exitCode);
    
    CloseHandle(thread);

	if (exitCode == 0)
	{
		MessageEvent("Failed to initialize backend 2");
	} else
	{
		MessageEvent("Initialize backend successful!");
	}

	return exitCode != 0;
}

std::string DebugFrontend::MakeValidFileName(const std::string& name) const
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

void* DebugFrontend::RemoteDup(HANDLE process, const void* source, size_t length) const
{
    void* remote = VirtualAllocEx(process, nullptr, length, MEM_COMMIT, PAGE_READWRITE);
	SIZE_T numBytesWritten;
    WriteProcessMemory(process, remote, source, length, &numBytesWritten);
    return remote;
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

bool DebugFrontend::StartProcessAndRunToEntry(LPCSTR exeFileName,
	LPSTR commandLine,
	LPCSTR directory,
	PROCESS_INFORMATION& processInfo,
	bool console)
{

    STARTUPINFO startUpInfo = { 0 };
    startUpInfo.cb = sizeof(startUpInfo);

    ExeInfo info;
    if (!GetExeInfo(exeFileName, info) || info.entryPoint == 0)
    {
        MessageEvent("Error: The entry point for the application could not be located", MessageType_Error);
        return false;
    }

	DWORD flags = DEBUG_PROCESS | DEBUG_ONLY_THIS_PROCESS ;
	if (console)
		flags |= CREATE_NEW_CONSOLE;
	else
		flags |= CREATE_NO_WINDOW;

    if (!CreateProcess(nullptr, commandLine, nullptr, nullptr, TRUE, flags, nullptr, directory, &startUpInfo, &processInfo))
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

void DebugFrontend::OutputError(DWORD error) const
{

    char buffer[1024];
    if (FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, nullptr, error, 0, buffer, 1024, nullptr))
    {
        std::string message = "Error: ";
        message += buffer;
        MessageEvent(message, MessageType_Error);
    }

}

void DebugFrontend::Output(std::string& message) const
{
	std::cout << message << std::endl;
}
