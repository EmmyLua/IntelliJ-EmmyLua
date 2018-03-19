#ifndef UTILITY_H
#define UTILITY_H
#include <Windows.h>
#include <string>

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

std::wstring CharToWchar(const char* c, size_t m_encode = CP_ACP);

std::string WcharToChar(const wchar_t* wp, size_t m_encode = CP_ACP);

#endif