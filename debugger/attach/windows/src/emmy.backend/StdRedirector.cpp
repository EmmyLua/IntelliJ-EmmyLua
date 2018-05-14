#include "StdRedirector.h"
#include <process.h>
#include <io.h>
#include <thread>

void StdRedirector::redirect(const std::function<void(const char*, size_t)>& handler)
{
	SECURITY_ATTRIBUTES saAttr;
	saAttr.nLength = sizeof(SECURITY_ATTRIBUTES);
	saAttr.bInheritHandle = TRUE;
	saAttr.lpSecurityDescriptor = nullptr;
	
	CreatePipe(&m_hChildStd_Rd, &m_hChildStd_Wr, &saAttr, 0);
	
	const auto old = _dup(m_target);
	if (old == -1)
	{
		perror("_dup failure");
		return;
	}

	const auto stream = _open_osfhandle(reinterpret_cast<long>(m_hChildStd_Wr), 0);
	FILE* test = nullptr;
	if (stream != -1)
		test = _fdopen(stream, "wt");

	// stdout now refers to file "test" 
	if (_dup2(_fileno(test), m_target) == -1)
	{
		perror("Can't _dup2 stdout");
		return;
	}
	setvbuf(stdout, nullptr, _IONBF, 0);

	std::thread thread([this, handler]()
	{
		char buf[1024] = { 0 };
		while (true) {
			DWORD w;
			ZeroMemory(buf, 1024);
			const bool suc = ReadFile(m_hChildStd_Rd, buf, 1024, &w, nullptr);
			if (suc && w > 0) {
				handler(buf, w);
			}
		}
	});
	thread.detach();
}