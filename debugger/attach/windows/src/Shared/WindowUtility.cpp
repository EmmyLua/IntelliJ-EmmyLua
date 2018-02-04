#include "WindowUtility.h"
#include <tlhelp32.h>
#include <psapi.h>
#include <shlobj.h>

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

						if (!process.path.empty()) {
							char windowsPath[MAX_PATH];
							if (SHGetFolderPath(nullptr, CSIDL_WINDOWS, nullptr, SHGFP_TYPE_CURRENT, windowsPath) == 0) {
								if (process.path.find(windowsPath) == std::string::npos) {

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
					}
				}
			} while (Process32Next(snapshot, &processEntry));

		}

		CloseHandle(snapshot);

	}

}