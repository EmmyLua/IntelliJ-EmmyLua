#include "StdRedirector.h"
#include <process.h>
#include <io.h>
#include <thread>

StdRedirector::~StdRedirector()
{

}

void StdRedirector::redirect(const std::function<void(const char*, int)>& handler)
{
	SECURITY_ATTRIBUTES saAttr;
	saAttr.nLength = sizeof(SECURITY_ATTRIBUTES);
	saAttr.bInheritHandle = TRUE;
	saAttr.lpSecurityDescriptor = nullptr;
	BOOL s = CreatePipe(&m_hChildStd_Rd, &m_hChildStd_Wr, &saAttr, 0);
	old = _dup(m_target);
	if (old == -1)
	{
		perror("_dup failure");
		return;
	}
	stream = _open_osfhandle(reinterpret_cast<long>(m_hChildStd_Wr), 0);
	if (stream != -1)
		test = _fdopen(stream, "wt");

	// stdout now refers to file "test" 
	if (_dup2(/*_fileno(test)*/stream, m_target) == -1)
	{
		perror("Can't _dup2 stdout");
		return;
	}
	// no buffer
	setvbuf(stdout, nullptr, _IONBF, 0);
	std::thread thread([this, handler]()
	{
		const int BUFF_SIZE = 1024;
		const int MAX_BUFF_SIZE = BUFF_SIZE + 5;
		char buf[MAX_BUFF_SIZE] = { 0 };
		char lastChars[5] = { 0 };
		int lastCharsLength = 0;
		while (true) {
			DWORD w;
			ZeroMemory(buf, MAX_BUFF_SIZE);
			char* pBuff = buf;
			if (lastCharsLength > 0) {
				pBuff = &buf[lastCharsLength];
				// copy the last half chars to buff
				memcpy(buf, lastChars, lastCharsLength);
			}
			const BOOL suc = ReadFile(m_hChildStd_Rd, pBuff, BUFF_SIZE, &w, nullptr);
			if (suc && w > 0) {
				// process utf8 char array
				int i = 0;
				int charLength = 0;
				
				int buffRealLength = w + lastCharsLength;

				while (true) {
					unsigned char c = (unsigned)buf[i];

					if (c >= 0xFC) {
						charLength = 6;
					} else if (c >= 0xF8) {
						charLength = 5;
					} else if (c >= 0xF0) {
						charLength = 4;
					} else if (c >= 0xE0) {
						charLength = 3;
					} else if (c >= 0xC8) {
						charLength = 2;
					} else {
						charLength = 1;
					}

					if (i + charLength > buffRealLength) {
						break;
					} else {
						i += charLength;
						// the last char has been processed, break.
						if (i == buffRealLength) {
							break;
						}
					}
				}

				// store the last char to cache for next read-buff to combine
				ZeroMemory(lastChars, sizeof(lastChars) / sizeof(lastChars[0]));
				lastCharsLength = 0;
				for (int j = i; j < buffRealLength; j++) {
					lastChars[j - i] = buf[j];
					// mark string end
					buf[j] = 0;
					lastCharsLength++;
				}

				handler(buf, buffRealLength);
			}
		}
	});
	thread.detach();
}