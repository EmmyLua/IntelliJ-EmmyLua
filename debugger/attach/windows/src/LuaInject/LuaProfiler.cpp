#include "LuaProfiler.h"
#include <ctime>

int idCounter = 0;

LPFunctionCall::LPFunctionCall(): id(idCounter++), line(0), count(0), time(0), isDirty(true)
{
}

void LPFunctionCall::Push()
{
	m_timeStack.push(clock());
}

void LPFunctionCall::Pop()
{
	if (!m_timeStack.empty())
	{
		auto t = m_timeStack.top();
		m_timeStack.pop();

		time_t dt = clock() - t;

		time += dt;
		count++;
	}
}
