#include <wtypes.h>
#include <stdio.h>
#include <string>
#include <psapi.h>
#include "Utility.h"

int main(int argc, char** argv)
{
	if (argc >= 3)
	{
		std::string cmd = argv[1];
		if (cmd == "-file") {
			const char* fileName = argv[2];
			ExeInfo info;
			if (GetExeInfo(fileName, info)) {
				printf("%d", info.i386);
				return info.i386;
			}
			return -1;//file not exist
		}
		else if(cmd == "-pid") {
			const char* pid_str = argv[2];
			DWORD processId = atoi(pid_str);

			char fileName[_MAX_PATH];
			HANDLE m_process = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, FALSE, processId);
			GetModuleFileNameEx(m_process, nullptr, fileName, _MAX_PATH);

			ExeInfo info;
			if (GetExeInfo(fileName, info)) {
				printf("%d", info.i386);
				return info.i386;
			}
		}
	}
	return 0;
}