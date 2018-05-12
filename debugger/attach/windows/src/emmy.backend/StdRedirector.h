#pragma once

#include <Windows.h>
#include <cstdio>
#include <functional>

class StdRedirector
{
public:
	StdRedirector(int target): m_target(target) {};
	~StdRedirector();

	void redirect(const std::function<void(const char*, size_t)>& handler);
private:
	int m_target;
	HANDLE m_hChildStd_Rd = nullptr;
	HANDLE m_hChildStd_Wr = nullptr;
	int old, stream;
	FILE    *test = nullptr;
};
