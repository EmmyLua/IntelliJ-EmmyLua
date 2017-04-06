#include <wtypes.h>
#include <stdio.h>


int main(int argc, char** argv)
{
	if (argc == 2)
	{
		char* pid_str = argv[1];
		DWORD processId = atoi(pid_str);
		HANDLE m_process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);
		BOOL is32;
		IsWow64Process(m_process, &is32);
		printf("%d", is32);
		return is32;
	}
	return 0;
}