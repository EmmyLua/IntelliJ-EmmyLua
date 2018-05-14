#pragma once

#include <Windows.h>
#include <functional>

class StdRedirector
{
public:
	StdRedirector(int target): m_target(target) {}

	void redirect(const std::function<void(const char*, size_t)>& handler);
private:
	int m_target;
	HANDLE m_hChildStd_Rd = nullptr;
	HANDLE m_hChildStd_Wr = nullptr;
};
