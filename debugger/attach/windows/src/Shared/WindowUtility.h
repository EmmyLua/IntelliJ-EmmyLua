#pragma once

#include <windows.h>

/**
* Returns the top level window for the specified process. The first such window
* that's found is returned.
*/
HWND GetProcessWindow(DWORD processId);