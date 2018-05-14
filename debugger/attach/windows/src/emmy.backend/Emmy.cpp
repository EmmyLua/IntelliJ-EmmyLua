#include "DebugBackend.h"
#include "LuaDll.h"
#include "StackNode.h"

void DebugBackend::InitEmmy(LAPI api, lua_State * L) const
{
	auto vm = Get().GetVm(L);
	if (vm == nullptr || vm->isEmmyLoaded || m_emmyLuaFilePath.empty())
		return;

	vm->skipPostLoadScript = true;
	vm->isEmmyLoaded = true;
	EnableIntercepts(api, false);
	SetHookMode(api, L, HookMode_None);

	int t = lua_gettop_dll(api, L);

	lua_rawgetglobal_dll(api, L, "emmy");
	if (lua_isnil_dll(api, L, -1))
	{
		int err = 1;
		const char* fileName = m_emmyLuaFilePath.c_str();
		FILE* file = fopen(fileName, "rb");

		if (file != nullptr)
		{
			std::string name = "@";
			name += fileName;

			fseek(file, 0, SEEK_END);
			unsigned int length = ftell(file);

			char* buffer = new char[length];
			fseek(file, 0, SEEK_SET);
			fread(buffer, 1, length, file);

			fclose(file);

			err = lua_dobuffer_dll(api, L, buffer, length, name.c_str());

			delete[] buffer;
		}
		if (err != 0)
			err = lua_dofile_dll(api, L, fileName);
		//TODO err != 0
	}

	RegisterEmmyLibrary(api, L);

	lua_settop_dll(api, L, t);

	//reset
	vm->skipPostLoadScript = false;
	SetHookMode(api, L, HookMode_Full);
	EnableIntercepts(api, true);
}

int EmmyCreateNode(lua_State* L)
{
	LAPI api = DebugBackend::Get().GetApiForVm(L);
	if (api != INVALID_API)
	{
		//const char* tag = lua_tostring_dll(api, L, -2);
		//const char* data = lua_tostring_dll(api, L, -1);
	}
	return 0;
}

int EmmyAddChildNode(lua_State* L)
{
	LAPI api = DebugBackend::Get().GetApiForVm(L);
	if (api != INVALID_API)
	{
		StackTableNode* parent = (StackTableNode*)lua_touserdata_dll(api, L, -3);
		StackLuaObjectNode* key = (StackLuaObjectNode*)lua_touserdata_dll(api, L, -2);
		StackLuaObjectNode* value = (StackLuaObjectNode*)lua_touserdata_dll(api, L, -1);

		if (parent != nullptr && value != nullptr && key != nullptr)
		{
			parent->AddChild(key, value);
		}
	}
	return 0;
}

int EmmyGetValueAsText(lua_State* L)
{
	LAPI api = DebugBackend::Get().GetApiForVm(L);
	if (api != INVALID_API)
	{
		// -4 : object
		int maxDepth = lua_tonumber_dll(api, L, -3);
		const char* typeOverrideName = lua_tostring_dll(api, L, -2);
		bool displayAsKey = lua_toboolean_dll(api, L, -1);

		auto node = DebugBackend::Get().GetValueAsText(api, L, -4, maxDepth, typeOverrideName, displayAsKey, false);
		lua_pushlightuserdata_dll(api, L, node);
		return 1;
	}
	return 0;
}

int EmmyDebugLog(lua_State* L)
{
	LAPI api = DebugBackend::Get().GetApiForVm(L);
	if (api != INVALID_API)
	{
		const char* message = lua_tostring_dll(api, L, -2);
		int type = lua_tonumber_dll(api, L, -1);
		DebugBackend::Get().Message(message, (MessageType)type);
	}
	return 0;
}

int EmmyBeginProfiler(lua_State* L)
{
	DebugBackend::Get().BeginProfiler();
	return 0;
}

int EmmyEndProfiler(lua_State* L)
{
	DebugBackend::Get().EndProfiler();
	return 0;
}

void DebugBackend::RegisterEmmyLibrary(LAPI api, lua_State * L) const
{
	lua_rawgetglobal_dll(api, L, "emmy");
	int emmyTable = lua_gettop_dll(api, L);
	if (!lua_isnil_dll(api, L, -1))
	{
		//emmy.CreateNnode = function()end
		lua_pushstring_dll(api, L, "CreateNode");
		lua_pushcfunction_dll(api, L, EmmyCreateNode);
		lua_settable_dll(api, L, emmyTable);
		//emmy.AddChildNode = function(parentNode, childNode)end
		lua_pushstring_dll(api, L, "AddChildNode");
		lua_pushcfunction_dll(api, L, EmmyAddChildNode);
		lua_settable_dll(api, L, emmyTable);
		//emmy.RawGetValueAsText = function(n, maxDepth, typeNameOverride, displayAsKey)end
		lua_pushstring_dll(api, L, "RawGetValueAsText");
		lua_pushcfunction_dll(api, L, EmmyGetValueAsText);
		lua_settable_dll(api, L, emmyTable);
		//emmy.DebugLog = function(log, type)
		lua_pushstring_dll(api, L, "DebugLog");
		lua_pushcfunction_dll(api, L, EmmyDebugLog);
		lua_settable_dll(api, L, emmyTable);
		//emmy.BeginProfiler = function()
		lua_pushstring_dll(api, L, "BeginProfiler");
		lua_pushcfunction_dll(api, L, EmmyBeginProfiler);
		lua_settable_dll(api, L, emmyTable);
		//emmy.EndProfiler = function()
		lua_pushstring_dll(api, L, "EndProfiler");
		lua_pushcfunction_dll(api, L, EmmyEndProfiler);
		lua_settable_dll(api, L, emmyTable);
	}
	lua_pop_dll(api, L, 1);
}