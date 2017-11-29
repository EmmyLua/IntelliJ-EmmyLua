#pragma once

#include <windows.h>
#include <string>
#include <vector>

/**
* Returns the top level window for the specified process. The first such window
* that's found is returned.
*/
HWND GetProcessWindow(DWORD processId);

struct Process
{
	unsigned int    id;     // Windows process identifier
	std::string     name;   // Executable name
	std::string     title;  // Name from the main window of the process.
	std::string     path;   // Full path
	std::string     iconPath;// Icon path
};

/**
* Returns all of the processes on the machine that can be debugged.
*/
void GetProcesses(std::vector<Process>& processes);