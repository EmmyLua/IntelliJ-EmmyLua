#include <ctime>
#include "LuaProfiler.h"
#include "DebugBackend.h"
#include "LuaDll.h"
#include "StackNode.h"
#include "DebugMessage.h"
#include "DebugPipeline.h"

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

void DebugBackend::BeginProfiler()
{
	if (m_profiler)
		return;

	m_profiler = true;

	DebugMessage resp(DebugMessageId::RespProfilerBegin);
	m_debugPipeline->Send(&resp);

	SetAllHookModeLazy(HookMode_CallsAndReturns);
}

void DebugBackend::ProcProfiler(LAPI api, lua_State * L, lua_Debug * ar)
{
	static char* TEMP_NAME = new char[_MAX_PATH];
	int evt = GetEvent(api, ar);

	bool isCall = GetIsHookEventCall(api, evt);
	bool isRet = GetIsHookEventRet(api, evt);
	if (isCall || isRet)
	{
		lua_getinfo_dll(api, L, "S", ar);
		const char* what = GetWhat(api, ar);
		if (strcmp(what, "Lua") == 0)
		{
			lua_getinfo_dll(api, L, "n", ar);

			const char* source = GetSource(api, ar);
			const char* name = GetName(api, ar);
			if (name == nullptr)
			{
				ZeroMemory(TEMP_NAME, _MAX_PATH);
				sprintf(TEMP_NAME, "line<%d>", GetLineDefined(api, ar));
				name = TEMP_NAME;
			}

			if (name != nullptr)
			{
				LPFunctionCall* call = GetProfilerCall(source, name, GetLineDefined(api, ar));
				call->isDirty = true;
				if (isCall)
				{
					call->Push();
				}
				else if (isRet)
				{
					call->Pop();

					DMProfilerData msg;
					msg.L = L;
					msg.callList.push_back(call);
					m_debugPipeline->Send(&msg);
				}
			}
		}
	}
}

LPFunctionCall * DebugBackend::GetProfilerCall(const char* source, const char* name, int line)
{
	auto iter = m_profilerCallMap.find(name);
	LPFunctionCall* call;
	if (iter == m_profilerCallMap.end()) {
		call = new LPFunctionCall();
		call->name = name;
		call->file = source;
		call->line = line;
		m_profilerCallMap[name] = call;
	}
	else {
		call = iter->second;
	}
	return call;
}

void DebugBackend::EndProfiler()
{
	if (m_profiler)
	{
		m_profiler = false;

		DebugMessage resp(DebugMessageId::RespProfilerEnd);
		m_debugPipeline->Send(&resp);

		for (auto it = m_profilerCallMap.begin(); it != m_profilerCallMap.end(); it++)
		{
			delete it->second;
		}
		m_profilerCallMap.clear();
	}
}