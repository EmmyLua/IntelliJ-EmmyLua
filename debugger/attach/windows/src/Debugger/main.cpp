#include "DebugFrontend.h"
#include "cxxopts.hpp"
#include <iostream>
#include <assert.h>

using namespace std;

DebugFrontend& inst = DebugFrontend::Get();
wxEvtHandler* handler = new wxEvtHandler();

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
		size_t index = line.find(" ");

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
		else if (cmd == "stepout") {
			inst.StepOut(handler->vm);
		}
		else if (cmd == "setb") {
			//setb [index] [line] [condition]
			vector<string> list;
			split(line, string(" "), &list, 4);
			assert(list.size() >= 4);
			int scriptIndex = atoi(list[1].c_str());
			int pointPos = atoi(list[2].c_str());
			inst.AddBreakpoint(handler->vm, scriptIndex, pointPos, list[3]);
		}
		else if (cmd == "delb") {
			//delb [index] [line]
			vector<string> list;
			split(line, string(" "), &list, 3);
			assert(list.size() >= 3);
			int scriptIndex = atoi(list[1].c_str());
			int pointPos = atoi(list[2].c_str());
			inst.DelBreakpoint(handler->vm, scriptIndex, pointPos);
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

			inst.Evaluate(
				handler->vm,
				atoi(idString.c_str()),
				script.c_str(),
				atoi(stack.c_str()),
				atoi(depath.c_str()));
		}
		else if (cmd == "done") {
			inst.DoneLoadingScript(handler->vm);
		}
		else if (cmd == "detach" || cmd.empty()) {
			inst.Stop(false);
			break;
		}
	}
}

int main(int argc, char** argv)
{
	cxxopts::Options options("EmmyLua", "EmmyLua Debugger");
	options.add_options()
		("m,mode", "debug model attach/run", cxxopts::value<std::string>())
		("p,pid", "the pid we will attach to", cxxopts::value<int>())

		("c,cmd", "command line", cxxopts::value<std::string>())
		("a,args", "args", cxxopts::value<std::string>())
		("d,debug", "is debug", cxxopts::value<bool>())
		("w,workdir", "working directory", cxxopts::value<std::string>())
		("e,emmy", "emmy lua", cxxopts::value<std::string>());
	options.parse(argc, argv);
	if (options.count("m") > 0) {
		inst.SetEventHandler(handler);

		if (options.count("e")) {
			std::string emmy = options["e"].as<std::string>();
			inst.SetEmmyEnv(emmy);
		}

		std::string mode = options["m"].as<std::string>();
		if (mode == "attach") {
			if (options.count("p")) {
				int pid = options["p"].as<int>();
				if (inst.Attach(pid, "")) {
					//mainLoop();
				}
			}
		}
		else if (mode == "run") {
			//command
			std::string cmd;
			if (options.count("c")) {
				cmd = options["c"].as<std::string>();
			}
			//command
			std::string args;
			if (options.count("a")) {
				args = options["a"].as<std::string>();
			}
			//is debug mode
			bool debug = true;
			if (options.count("d")) {
				debug = options["d"].as<bool>();
			}
			//working dir
			std::string wd;
			if (options.count("w")) {
				wd = options["w"].as<std::string>();
			}

			if (!cmd.empty()) {
				if (inst.Start(cmd.c_str(), args.c_str(), wd.c_str(), "", debug, false)) {
					mainLoop();
				}
			}
		}
	}
	else {
		auto help = options.help();
		printf("%s", help.c_str());
	}
	return 0;
}