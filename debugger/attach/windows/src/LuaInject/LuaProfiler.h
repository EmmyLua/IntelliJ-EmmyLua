#ifndef _LUA_PROFILER_H_
#define _LUA_PROFILER_H_
#include <string>

class LPFunctionCall
{
public:
	LPFunctionCall();

	size_t id;
	std::string name;
	size_t line;
	size_t count;
	size_t time;
};

#endif