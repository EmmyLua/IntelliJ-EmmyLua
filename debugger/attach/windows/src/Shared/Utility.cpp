#include "Utility.h"
#include <imagehlp.h>

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

bool GetExeInfo(LPCSTR fileName, ExeInfo&info)
{
	LOADED_IMAGE loadedImage;
	if (!MapAndLoad(const_cast<PSTR>(fileName), NULL, &loadedImage, FALSE, TRUE))
	{
		return false;
	}

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
	info.i386 = loadedImage.FileHeader->FileHeader.Machine == IMAGE_FILE_MACHINE_I386;

	UnMapAndLoad(&loadedImage);

	return true;
}