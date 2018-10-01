#include "DebugFrontend.h"
#include "cxxopts.hpp"
#include <iostream>

using namespace std;

DebugFrontend& inst = DebugFrontend::Get();

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
		if (line.length() == 0)
			break;

		const auto index = line.find(" ");

		const auto cmd = line.substr(0, index);

		if (cmd == "resume")
		{
			inst.Resume();
		}
	}
}

int main(int argc, char** argv)
{
	ErrorCode code = ErrorCode::OK;
	//::MessageBox(NULL, "Wait for debugger.", "EmmyLua", MB_OK);
	cxxopts::Options options("EmmyLua", "EmmyLua Debugger");
	options.add_options()
		("m,mode", "debug model attach/run", cxxopts::value<std::string>())
		("p,pid", "the pid we will attach to", cxxopts::value<int>())

		("c,cmd", "command line", cxxopts::value<std::string>())
		("a,args", "args", cxxopts::value<std::string>())
		("d,debug", "is debug", cxxopts::value<bool>())
		("console", "show console window", cxxopts::value<std::string>())
		("w,workdir", "working directory", cxxopts::value<std::string>())
		("e,emmy", "emmy lua", cxxopts::value<std::string>());
	options.parse(argc, argv);
	if (options.count("m") > 0) {
		const auto mode = options["m"].as<std::string>();
		if (mode == "attach") {
			if (options.count("p")) {
				const auto pid = options["p"].as<int>();
				code = inst.Attach(pid, "");
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
			auto debug = true;
			if (options.count("d")) {
				debug = options["d"].as<bool>();
			}
			auto console = false;
			if (options.count("console")) {
				console = options["console"].as<std::string>() == "true";
			}
			//working dir
			std::string wd;
			if (options.count("w")) {
				wd = options["w"].as<std::string>();
			}

			if (!cmd.empty()) {
				code = inst.Start(cmd.c_str(),
					args.c_str(),
					wd.c_str(),
					"",
					debug,
					console,
					false);
				if (code == ErrorCode::OK) {
					mainLoop();
				}
			}
		}
	}
	else {
		auto help = options.help();
		printf("%s", help.c_str());
	}
	return static_cast<int>(code);
}