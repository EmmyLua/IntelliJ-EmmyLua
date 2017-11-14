#include "WindowUtility.h"
#include <tlhelp32.h>
#include <psapi.h>

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


					HWND hWnd = GetProcessWindow(processEntry.th32ProcessID);

					if (hWnd != nullptr)
					{
						char buffer[1024];
						GetWindowText(hWnd, buffer, 1024);
						process.title = buffer;
					}

					processes.push_back(process);

				}
			} while (Process32Next(snapshot, &processEntry));

		}

		CloseHandle(snapshot);

	}

}

bool GetProcess(DWORD processId, Process & process)
{
	char fileName[_MAX_PATH];
	HANDLE m_process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, processId);
	if (m_process == nullptr)
		return false;

	GetModuleFileNameEx(m_process, nullptr, fileName, _MAX_PATH);

	process.id = processId;
	process.path = fileName;

	HWND hWnd = GetProcessWindow(processId);

	if (hWnd != nullptr)
	{
		char buffer[1024];
		GetWindowText(hWnd, buffer, 1024);
		process.title = buffer;
	}
	return true;
}
