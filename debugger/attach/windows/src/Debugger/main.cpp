#include "DebugFrontend.h"
#include <iostream>
#include <assert.h>

using namespace std;

DebugFrontend& inst = DebugFrontend::Get();
wxEvtHandler* handler = new wxEvtHandler();

// attach to target process
void doAttach() {
	int pid = 0;
	bool attached = false;
	while (!attached) {
		scanf("%d", &pid);
		if (pid > 0) {
			attached = inst.Attach(pid, "");
		}
	}
}

void split(std::string& s, std::string& delim, std::vector< std::string >* ret, size_t n)
{
	size_t count = 0;
	size_t last = 0;
	size_t index = s.find_first_of(delim, last);
	while (index != std::string::npos && ++count < n)
	{
		ret->push_back(s.substr(last, index - last));
		last = index + 1;
		index = s.find_first_of(delim, last);
	}
	if (count <= n) {
		ret->push_back(s.substr(last));
	}
	else if (index - last > 0)
	{
		ret->push_back(s.substr(last, index - last));
	}
}

// events loop
void mainLoop() {
	while (true) {
		char input[2048];
		cin.getline(input, 2048);

		string line = input;
		int index = line.find(" ");

		string cmd = line.substr(0, index);

		if (cmd == "run") {
			inst.Continue(handler->vm);
		}
		else if (cmd == "stepover") {
			inst.StepOver(handler->vm);
		}
		else if (cmd == "stepinto") {
			inst.StepInto(handler->vm);
		}
		else if (cmd == "setb") {
			//setb 3 2
			vector<string> list;
			split(line, string(" "), &list, 3);
			assert(list.size() >= 3);
			int scriptIndex = atoi(list[1].c_str());
			int pointPos = atoi(list[2].c_str());
			inst.ToggleBreakpoint(handler->vm, scriptIndex, pointPos);
		}
		else if (cmd == "eval") {
			//eval [id] [stack] [maxDepth] [script]
			vector<string> list;
			split(line, string(" "), &list, 5);
			assert(list.size() >= 5);

			string idString = list[1];
			string stack = list[2];
			string depath = list[3];
			string script = list[4];

			string result;
			inst.Evaluate(
				handler->vm,
				atoi(idString.c_str()),
				script.c_str(),
				atoi(stack.c_str()),
				atoi(depath.c_str()),
				result);
		}
		else if (cmd == "done") {
			inst.DoneLoadingScript(handler->vm);
		}
		else if (cmd == "detach") {
			inst.Stop(false);
			break;
		}
		else if (cmd == "stack") {
			int num = inst.GetNumStackFrames();
			for (int i = 0; i < num; i++) {
				const DebugFrontend::StackFrame frame = inst.GetStackFrame(i);
				cout << frame.function << endl;
			}
		}
	}
}

int main(int argc, char** argv)
{
	if (argc == 2)
	{
		char* pid_str = argv[1];
		DWORD processId = atoi(pid_str);
		HANDLE m_process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);
		BOOL is32;
		IsWow64Process(m_process, &is32);
		return is32;
	}

	inst.SetEventHandler(handler);

	doAttach();

	mainLoop();

	return 0;
}