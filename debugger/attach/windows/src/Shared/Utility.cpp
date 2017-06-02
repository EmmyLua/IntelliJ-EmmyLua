#include "Utility.h"

HWND g_wnd;

BOOL CALLBACK EnumWindowsProcGetWndTitle(HWND hWnd, LPARAM lparam)
{
	HANDLE handle = GetCurrentProcess();
	DWORD pid = GetProcessId(handle);

	DWORD ProcID = 0;
	GetWindowThreadProcessId(hWnd, &ProcID);
	if (ProcID == pid)
	{
		HWND pWnd = hWnd;
		while (GetParent(pWnd) != nullptr)
			pWnd = GetParent(pWnd);
		g_wnd = pWnd;
		return false;
	}
	return true;
}

HWND GetCurrentWnd()
{
	g_wnd = nullptr;
	EnumWindows(EnumWindowsProcGetWndTitle, 0);
	return g_wnd;
}
