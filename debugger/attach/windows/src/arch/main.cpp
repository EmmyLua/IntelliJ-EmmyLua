#include <wtypes.h>
#include <stdio.h>
#include <imagehlp.h>
#include <string>

struct ExeInfo
{
	size_t			entryPoint;
	bool            managed;
	bool            i386;
};

bool GetExeInfo(LPCSTR fileName, ExeInfo&info)
{
	LOADED_IMAGE loadedImage;
	if (!MapAndLoad(const_cast<PSTR>(fileName), NULL, &loadedImage, FALSE, TRUE))
	{
		return false;
	}

	info.managed = false;
	if (loadedImage.FileHeader->Signature == IMAGE_NT_SIGNATURE)
	{

		DWORD netHeaderAddress =
			loadedImage.FileHeader->OptionalHeader.DataDirectory[IMAGE_DIRECTORY_ENTRY_COM_DESCRIPTOR].VirtualAddress;

		if (netHeaderAddress)
		{
			info.managed = true;
		}
	}

	info.entryPoint = loadedImage.FileHeader->OptionalHeader.AddressOfEntryPoint;
	info.i386 = loadedImage.FileHeader->FileHeader.Machine == IMAGE_FILE_MACHINE_I386;

	UnMapAndLoad(&loadedImage);

	return true;
}

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
		}
		else if(cmd == "-pid") {
			const char* pid_str = argv[2];
			DWORD processId = atoi(pid_str);
			HANDLE m_process = OpenProcess(PROCESS_ALL_ACCESS, FALSE, processId);
			BOOL is32;
			IsWow64Process(m_process, &is32);
			printf("%d", is32);
			return is32;
		}
	}
	return 0;
}