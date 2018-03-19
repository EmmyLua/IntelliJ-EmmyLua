#include "Utility.h"
#include <ImageHlp.h>

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
	if (!MapAndLoad(const_cast<PSTR>(fileName), nullptr, &loadedImage, FALSE, TRUE))
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

std::wstring CharToWchar(const char* c, size_t m_encode)
{
	int len = MultiByteToWideChar(m_encode, 0, c, strlen(c), nullptr, 0);
	wchar_t*    m_wchar = new wchar_t[len + 1];
	MultiByteToWideChar(m_encode, 0, c, strlen(c), m_wchar, len);
	m_wchar[len] = '\0';
	std::wstring str = m_wchar;
	delete[] m_wchar;
	return str;
}

std::string WcharToChar(const wchar_t* wp, size_t m_encode)
{
	int len = WideCharToMultiByte(m_encode, 0, wp, wcslen(wp), nullptr, 0, nullptr, nullptr);
	char    *m_char = new char[len + 1];
	WideCharToMultiByte(m_encode, 0, wp, wcslen(wp), m_char, len, nullptr, nullptr);
	m_char[len] = '\0';
	std::string str = m_char;
	delete[] m_char;
	return str;
}