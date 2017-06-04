#ifndef UTILITY_H
#define UTILITY_H
#include <windows.h>

struct ExeInfo
{
	size_t			entryPoint;
	bool            managed;
	bool            i386;
};

/**
* Gets the entry point for the specified executable file from the PE data. If the PE
* is a .NET/managed application the managed parameter will be set to true.
*/
bool GetExeInfo(LPCSTR fileName, ExeInfo&info);

// 获取当前主窗口
HWND GetCurrentWnd();

#endif