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

#include "DebugHelp.h"

#include <psapi.h>
#include <assert.h>

struct GetCStackData
{
    HANDLE                      hThread;
    unsigned int                stackSize;
    unsigned int                maxStackSize;
    STACKFRAME64*               stack;
};

SymEnumSymbols_t                SymEnumSymbols_dll              = NULL;
SymInitialize_t                 SymInitialize_dll               = NULL;
SymCleanup_t                    SymCleanup_dll                  = NULL;
SymLoadModule64_t               SymLoadModule64_dll             = NULL;
SymUnloadModule64_t             SymUnloadModule64_dll           = NULL;
SymGetModuleInfo64_t            SymGetModuleInfo64_dll          = NULL;
StackWalk64_t                   StackWalk64_dll                 = NULL;
SymGetSymFromAddr64_t           SymGetSymFromAddr64_dll         = NULL;
SymFunctionTableAccess64_t      SymFunctionTableAccess64_dll    = NULL;
SymGetModuleBase64_t            SymGetModuleBase64_dll          = NULL;

RtlCaptureContext_t             RtlCaptureContext_dll           = NULL;
RtlCaptureStackBackTrace_t      RtlCaptureStackBackTrace_dll    = NULL;

HMODULE hModule = NULL;

bool LoadRtl()
{

	HMODULE hKernel32Dll = GetModuleHandle("kernel32.dll");
	RtlCaptureContext_dll = (RtlCaptureContext_t)GetProcAddress(hKernel32Dll,"RtlCaptureContext");
	
	HMODULE hNtDll = GetModuleHandle("ntdll.dll");
    if (hNtDll != NULL)
    {
	    RtlCaptureStackBackTrace_dll = (RtlCaptureStackBackTrace_t)GetProcAddress(hNtDll,"RtlCaptureStackBackTrace");
    }

    return RtlCaptureContext_dll != NULL && RtlCaptureStackBackTrace_dll != NULL;

}

bool LoadDebugHelp(HINSTANCE hInstance)
{

    HANDLE hProcess = GetCurrentProcess();

    LoadRtl();

    char moduleFileName[_MAX_PATH];
    GetModuleFileNameEx(hProcess, hInstance, moduleFileName, _MAX_PATH);

    // Replace the file name with the dbghelp.dll file name.
    strcpy(strrchr(moduleFileName, '\\'), "\\dbghelp.dll");
    
    hModule = LoadLibrary(moduleFileName);

    if (hModule == NULL)
    {
        return false;
    }

    SymInitialize_dll       = reinterpret_cast<SymInitialize_t>(GetProcAddress(hModule, "SymInitialize"));
    SymCleanup_dll          = reinterpret_cast<SymCleanup_t>(GetProcAddress(hModule, "SymCleanup"));
    SymEnumSymbols_dll      = reinterpret_cast<SymEnumSymbols_t>(GetProcAddress(hModule, "SymEnumSymbols"));
    SymLoadModule64_dll     = reinterpret_cast<SymLoadModule64_t>(GetProcAddress(hModule, "SymLoadModule64"));
    SymUnloadModule64_dll   = reinterpret_cast<SymUnloadModule64_t>(GetProcAddress(hModule, "SymUnloadModule64"));
    SymGetModuleInfo64_dll  = reinterpret_cast<SymGetModuleInfo64_t>(GetProcAddress(hModule, "SymGetModuleInfo64"));
    StackWalk64_dll         = reinterpret_cast<StackWalk64_t>(GetProcAddress(hModule, "StackWalk64"));
    SymGetSymFromAddr64_dll = reinterpret_cast<SymGetSymFromAddr64_t>(GetProcAddress(hModule, "SymGetSymFromAddr64"));
    SymFunctionTableAccess64_dll = reinterpret_cast<SymFunctionTableAccess64_t>(GetProcAddress(hModule, "SymFunctionTableAccess64"));
    SymGetModuleBase64_dll  = reinterpret_cast<SymGetModuleBase64_t>(GetProcAddress(hModule, "SymGetModuleBase64"));

    return SymInitialize_dll &&
           SymCleanup_dll &&
           SymEnumSymbols_dll &&
           SymLoadModule64_dll &&
           SymGetModuleInfo64_dll &&
           StackWalk64_dll &&
           SymGetSymFromAddr64_dll &&
           SymFunctionTableAccess64_dll &&
           SymGetModuleBase64_dll;

}

void FreeDebugHelp()
{
	if (hModule != NULL) {
		FreeLibrary(hModule);
		hModule = NULL;
	}
}

DWORD WINAPI GetCStackThread(LPVOID p)
{

    GetCStackData* data = static_cast<GetCStackData*>(p);
    data->stackSize = 0;

    SuspendThread(data->hThread);
    
    CONTEXT context;
    ZeroMemory(&context, sizeof(context));
    context.ContextFlags = CONTEXT_FULL;

    HANDLE hProcess = GetCurrentProcess();
    
    if (GetThreadContext(data->hThread, &context))
    {
        
        STACKFRAME64 stackFrame;
        ZeroMemory(&stackFrame, sizeof(stackFrame));
#if _WIN64
        stackFrame.AddrPC.Offset    = context.Rip;
        stackFrame.AddrStack.Offset = context.Rsp;
        stackFrame.AddrFrame.Offset = context.Rbp;
#else
		stackFrame.AddrPC.Offset = context.Eip;
		stackFrame.AddrStack.Offset = context.Esp;
		stackFrame.AddrFrame.Offset = context.Ebp;
#endif
        stackFrame.AddrPC.Mode      = AddrModeFlat;
        stackFrame.AddrStack.Mode   = AddrModeFlat;
        stackFrame.AddrFrame.Mode   = AddrModeFlat;

        while (data->stackSize < data->maxStackSize)
        {

             if (!StackWalk64_dll(
                  IMAGE_FILE_MACHINE_I386,
                  hProcess,
                  data->hThread,
                  &stackFrame,
                  &context,
                  NULL,
                  SymFunctionTableAccess64_dll,
                  SymGetModuleBase64_dll,
                  NULL))
             {
                break;
             }

             data->stack[data->stackSize] = stackFrame;
             ++data->stackSize;

        }

        ResumeThread(data->hThread);

    }

    return 0;

}

unsigned int GetCStack(STACKFRAME64 stack[], unsigned int maxStackSize)
{

    HANDLE hProcess = GetCurrentProcess();

    GetCStackData data;

    // We have to duplicate the pseudo handle returned by GetCurrentThread because
    // that handle always refers to the current thread.
    DuplicateHandle(hProcess, GetCurrentThread(), hProcess, &data.hThread, THREAD_GET_CONTEXT, TRUE, 0);

    data.maxStackSize = maxStackSize;
    data.stack        = stack;

    DWORD dwThreadId;
    HANDLE hThread = CreateThread(NULL, 0, GetCStackThread, static_cast<LPVOID>(&data), 0, &dwThreadId);

    WaitForSingleObject(hThread, INFINITE);
    CloseHandle(data.hThread);

    return data.stackSize;

}

unsigned int GetCStack(HANDLE hThread, STACKFRAME64 stack[], unsigned int maxStackSize)
{

    if( GetCurrentThread() != hThread )
    {

        CONTEXT context;
        ZeroMemory(&context, sizeof(context));
        context.ContextFlags = CONTEXT_FULL;

        HANDLE hProcess = GetCurrentProcess();
        unsigned int stackSize = 0;

        if (GetThreadContext(hThread, &context))
        {
            
            STACKFRAME64 stackFrame;
            ZeroMemory(&stackFrame, sizeof(stackFrame));
#if _WIN64
            stackFrame.AddrPC.Offset    = context.Rip;
            stackFrame.AddrStack.Offset = context.Rsp;
            stackFrame.AddrFrame.Offset = context.Rbp;
#else
			stackFrame.AddrPC.Offset = context.Eip;
			stackFrame.AddrStack.Offset = context.Esp;
			stackFrame.AddrFrame.Offset = context.Ebp;
#endif
            stackFrame.AddrPC.Mode      = AddrModeFlat;
            stackFrame.AddrStack.Mode   = AddrModeFlat;
            stackFrame.AddrFrame.Mode   = AddrModeFlat;

            while (stackSize < maxStackSize)
            {

                 if (!StackWalk64_dll(
                      IMAGE_FILE_MACHINE_I386,
                      hProcess,
                      hThread,
                      &stackFrame,
                      &context,
                      NULL,
                      SymFunctionTableAccess64_dll,
                      SymGetModuleBase64_dll,
                      NULL))
                 {
                    break;
                 }

                 stack[stackSize] = stackFrame;
                 ++stackSize;

            }

        }

        return stackSize;
    
    }
    else
    {
        return GetCStack(stack, maxStackSize);
    }

}