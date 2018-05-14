#ifndef _LUA_PROFILER_H_
#define _LUA_PROFILER_H_
#include <string>
#include <stack>
#include <ctime>

class LPFunctionCall
{
public:
	LPFunctionCall();

	unsigned int id;
	std::string file;
	std::string name;
	unsigned int line;
	unsigned int count;
	unsigned int time;

	bool isDirty;

	void Push();
	void Pop();

private:
	std::stack<clock_t> m_timeStack;
};

#endif