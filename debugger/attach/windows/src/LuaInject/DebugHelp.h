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

#ifndef DEBUG_HELP_H
#define DEBUG_HELP_H

#include <windows.h>
#include <dbgeng.h>
#include <dbghelp.h>

#include <vector>

typedef BOOL            (WINAPI *SymEnumSymbols_t)              (__in HANDLE, __in ULONG64, __in_opt PCSTR, __in PSYM_ENUMERATESYMBOLS_CALLBACK, __in_opt PVOID);
typedef BOOL            (WINAPI *SymInitialize_t)               (__in HANDLE, __in_opt PCSTR, __in BOOL);
typedef BOOL            (WINAPI *SymCleanup_t)                  (__in HANDLE);
typedef DWORD64         (WINAPI *SymLoadModule64_t)             (__in HANDLE, __in_opt HANDLE, __in_opt PCSTR, __in_opt PCSTR, __in DWORD64, __in DWORD);
typedef DWORD64         (WINAPI *SymUnloadModule64_t)           (__in HANDLE, __in DWORD64);
typedef BOOL            (WINAPI *SymGetModuleInfo64_t)          (HANDLE, DWORD64 dwAddr, PIMAGEHLP_MODULE64 ModuleInfo);
typedef BOOL            (WINAPI *StackWalk64_t)                 (DWORD, HANDLE, HANDLE, LPSTACKFRAME64, PVOID, PREAD_PROCESS_MEMORY_ROUTINE64, PFUNCTION_TABLE_ACCESS_ROUTINE64, PGET_MODULE_BASE_ROUTINE64, PTRANSLATE_ADDRESS_ROUTINE64);
typedef PVOID           (WINAPI *SymFunctionTableAccess64_t)    (HANDLE, DWORD64);
typedef BOOL            (WINAPI *SymGetSymFromAddr64_t)         (HANDLE, DWORD64, PDWORD64, PIMAGEHLP_SYMBOL64);
typedef DWORD64         (WINAPI *SymGetModuleBase64_t)          (HANDLE, DWORD64);

typedef VOID            (WINAPI *RtlCaptureContext_t)           (PCONTEXT);
typedef USHORT          (WINAPI *RtlCaptureStackBackTrace_t)    (ULONG, ULONG, PVOID*, PULONG);

extern SymEnumSymbols_t             SymEnumSymbols_dll;
extern SymInitialize_t              SymInitialize_dll;
extern SymCleanup_t                 SymCleanup_dll;
extern SymLoadModule64_t            SymLoadModule64_dll;
extern SymUnloadModule64_t          SymUnloadModule64_dll;
extern SymGetModuleInfo64_t         SymGetModuleInfo64_dll;
extern StackWalk64_t                StackWalk64_dll;
extern SymFunctionTableAccess64_t   SymFunctionTableAccess64_dll;
extern SymGetSymFromAddr64_t        SymGetSymFromAddr64_dll;
extern SymGetModuleBase64_t         SymGetModuleBase64_dll;

extern RtlCaptureContext_t          RtlCaptureContext_dll;
extern RtlCaptureStackBackTrace_t   RtlCaptureStackBackTrace_dll;

/**
 * Dynamically loads the Debug Help library functions from the dbghelp.dll in
 * the same directory as the specified executable.
 */
bool LoadDebugHelp(HINSTANCE hInstance);

void FreeDebugHelp();

/**
 * Gets the current C/C++ stack.
 */
unsigned int GetCStack(STACKFRAME64 stack[], unsigned int maxStackSize);

/**
 * Gets the C/C++ stack for the specified thead.
 */
unsigned int GetCStack(HANDLE hThread, STACKFRAME64 stack[], unsigned int maxStackSize);

#endif