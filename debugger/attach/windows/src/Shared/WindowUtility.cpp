#include "WindowUtility.h"
#include <tlhelp32.h>
#include <psapi.h>
#include <shlobj.h>
#include <algorithm>

HWND GetProcessWindow(DWORD processId)
{

	HWND hWnd = FindWindowEx(nullptr, nullptr, nullptr, nullptr);

	while (hWnd != nullptr)
	{

		if (GetParent(hWnd) == nullptr && GetWindowTextLength(hWnd) > 0 && IsWindowVisible(hWnd))
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

inline char char_tolower(char  c) {
	return (char)tolower(c);
}

inline char char_toupper(char  c) {
	return (char)toupper(c);
}


static void EmmyToLowerCase(std::string& str)
{
	std::transform(
		str.begin(),
		str.end(),
		str.begin(),
		char_tolower);
}

//-----------------------------------------------------------------------
static void EmmyToUpperCase(std::string& str)
{
	std::transform(
		str.begin(),
		str.end(),
		str.begin(),
		char_toupper);
}

void GetProcesses(std::vector<Process>& processes)
{
	static char fileName[_MAX_PATH];
	static char tempPath[_MAX_PATH];
	// Get the id of this process so that we can filter it out of the list.
	DWORD currentProcessId = GetCurrentProcessId();

	HANDLE snapshot = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0);

	if (snapshot != INVALID_HANDLE_VALUE)
	{

		PROCESSENTRY32 processEntry = { 0 };
		processEntry.dwSize = sizeof(processEntry);

		if (Process32First(snapshot, &processEntry))
		{
			char windowsPath[MAX_PATH] = { 0 };
			bool isGetWindowsPath = false;
			std::string strWinPath;
			if (SHGetFolderPath(nullptr, CSIDL_WINDOWS, nullptr, SHGFP_TYPE_CURRENT, windowsPath) == 0)
			{
				strWinPath = windowsPath;
				EmmyToLowerCase(strWinPath);
				isGetWindowsPath = true;
			}


			do
			{
				if (processEntry.th32ProcessID != currentProcessId && processEntry.th32ProcessID != 0)
				{
					Process process;

					process.id = processEntry.th32ProcessID;
					process.name = processEntry.szExeFile;

					HANDLE m_process = OpenProcess(PROCESS_QUERY_INFORMATION, FALSE, processEntry.th32ProcessID);
					if (m_process) {
						int err = GetModuleFileNameEx(m_process, nullptr, fileName, _MAX_PATH);
						if (err == 0) // ERROR_ACCESS_DENIED = 5
							process.path = "error";
						else process.path = fileName;

						EmmyToLowerCase(process.path);

						if (!process.path.empty())
						{
							if (isGetWindowsPath &&  process.path.find(strWinPath.c_str()) != std::string::npos)
							{
								//on windows path exe
								continue;
							}

							std::string skipExeNameList[] = {
								"360se",
								"MSBuild",
								"vcpkgsrv",
								"ServiceHub",
								"VcxprojReader",
								"mspdbsrv",
								"TGitCache",
								"TortoiseGitProc",
								"devenv.exe",
								"PerfWatson2",
								"TSVNCache",
								"steamwebhelper",
								"UnrealCEFSubProcess",
								"Microsoft.",
								"VaCodeInspectionsServer",
								"Steam.exe",
							};

							
							size_t totalSkip = sizeof(skipExeNameList) / sizeof(skipExeNameList[0]);
							bool needSkip = false;
							for (int i = 0; i < totalSkip; i++)
							{
								EmmyToLowerCase(skipExeNameList[i]);
								needSkip = process.path.find(skipExeNameList[i].c_str()) != std::string::npos;
								if (needSkip)
								{
									break;
								}
							}

							if (needSkip)
							{
								continue;
							}

							HWND hWnd = GetProcessWindow(processEntry.th32ProcessID);

							if (hWnd != nullptr)
							{
								char buffer[1024];
								GetWindowText(hWnd, buffer, 1024);
								process.title = buffer;
							}
							processes.push_back(process);
						}
					}
				}
			} while (Process32Next(snapshot, &processEntry));

		}

		CloseHandle(snapshot);

	}

}