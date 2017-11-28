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

void SaveIcon(HICON hIcon, const char* path) {
	unsigned int cx = GetSystemMetrics(SM_CXICON), cy = GetSystemMetrics(SM_CYICON);
	HDC hScreenDC = ::GetDC(nullptr);
	HDC hMemDC = CreateCompatibleDC(hScreenDC);
	HBITMAP hBitmap = CreateCompatibleBitmap(hScreenDC, cx, cy);
	HBITMAP hOldBitmap = (HBITMAP)SelectObject(hMemDC, hBitmap);

	//BitBlt(hMemDC, 0, 0, cx, cy, hScreenDC, 0, 0, SRCCOPY);
	DrawIcon(hMemDC, 0, 0, hIcon);

	SelectObject(hMemDC, hOldBitmap);
	::ReleaseDC(nullptr, hScreenDC);

	size_t headerSize = sizeof(BITMAPINFOHEADER) + 3 * sizeof(RGBQUAD);
	BYTE* pHeader = new BYTE[headerSize];
	LPBITMAPINFO pbmi = (LPBITMAPINFO)pHeader;
	memset(pHeader, 0, headerSize);
	pbmi->bmiHeader.biSize = sizeof(BITMAPINFOHEADER);
	pbmi->bmiHeader.biBitCount = 0;
	if (!GetDIBits(hMemDC, hBitmap, 0, 0, nullptr, pbmi, DIB_RGB_COLORS))
		return;

	BITMAPFILEHEADER bmf;
	if (pbmi->bmiHeader.biSizeImage <= 0)
		pbmi->bmiHeader.biSizeImage = pbmi->bmiHeader.biWidth*abs(pbmi->bmiHeader.biHeight)*(pbmi->bmiHeader.biBitCount + 7) / 8;
	BYTE* pData = new BYTE[pbmi->bmiHeader.biSizeImage];
	bmf.bfType = 0x4D42; bmf.bfReserved1 = bmf.bfReserved2 = 0;
	bmf.bfSize = sizeof(BITMAPFILEHEADER) + headerSize + pbmi->bmiHeader.biSizeImage;
	bmf.bfOffBits = sizeof(BITMAPFILEHEADER) + headerSize;
	if (!GetDIBits(hMemDC, hBitmap, 0, abs(pbmi->bmiHeader.biHeight), pData, pbmi, DIB_RGB_COLORS))
	{
		delete[] pData;
		return;
	}
	FILE* hFile = fopen(path, "wb");
	fwrite(&bmf, sizeof(BITMAPFILEHEADER), 1, hFile);
	fwrite(pbmi, headerSize, 1, hFile);
	fwrite(pData, pbmi->bmiHeader.biSizeImage, 1, hFile);
	fclose(hFile);

	DeleteObject(hBitmap);
	DeleteDC(hMemDC);

	delete[] pData;
}

void GetProcesses(std::vector<Process>& processes)
{
	static char fileName[_MAX_PATH];
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
						GetModuleFileNameEx(m_process, nullptr, fileName, _MAX_PATH);
						process.path = fileName;

						SHFILEINFO info;
						SHGetFileInfo(fileName, 0, &info, sizeof(info), SHGFI_ICON | SHGFI_LARGEICON);
						if (info.hIcon) {
							sprintf(fileName, "c:/temp/%d.bmp", process.id);
							SaveIcon(info.hIcon, fileName);
						}
					}

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
