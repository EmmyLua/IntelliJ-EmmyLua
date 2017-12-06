#include <wtypes.h>
#include <stdio.h>
#include <string>
#include <psapi.h>
#include "Utility.h"
#include "WindowUtility.h"

using namespace std;

int main(int argc, char** argv)
{
	string cmd = argv[1];
	if (cmd == "arch") {
		string type = argv[2];
		if (type == "-file") {
			const char* fileName = argv[3];
			ExeInfo info;
			if (GetExeInfo(fileName, info)) {
				printf("%d", info.i386);
				return info.i386;
			}
			return -1;//file not exist
		}
		else if (type == "-pid") {
			const char* pid_str = argv[3];
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
	else if (cmd == "list_processes") {
		std::vector<Process> list;
		GetProcesses(list);

		printf("<list>");
		for (int i = 0; i < list.size(); ++i)
		{
			auto& value = list[i];
			printf("<process pid=\"%d\">", value.id);
			printf("<title><![CDATA[%s]]></title>", value.title.c_str());
			printf("<path><![CDATA[%s]]></path>", value.path.c_str());
			printf("</process>");
		}
		printf("</list>\n");
	}
	return 0;
}