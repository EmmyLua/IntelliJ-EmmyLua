#pragma once
#include <string>
#include "Protocol.h"

class ByteInputStream;
class ByteOutputStream;
struct lua_State;

enum class DebugMessageId
{
	//req
	Continue,
	StepOver,
	StepInto,
	StepOut,
	AddBreakpoint,
	DelBreakpoint,
	Break,
	Evaluate,
	Detach,
	PatchReplaceLine,
	PatchInsertLine,
	PatchDeleteLine,
	LoadDone,
	IgnoreException,
	DeleteAllBreakpoints,
	InitEmmy,

	ReqProfilerBegin,
	ReqProfilerEnd,

	//resp
	Initialize,
	CreateVM,
	DestroyVM,
	LoadScript,
	SetBreakpoint,
	Exception,
	LoadError,
	Message,
	SessionEnd,
	NameVM,
	EvalResult,

	RespProfilerData,
};

class DebugMessage
{
	DebugMessageId id;
public:
	virtual ~DebugMessage() = default;
	DebugMessage(DebugMessageId id);

	DebugMessageId getId() const { return id; }

	virtual void Read(ByteInputStream* stream);
	virtual void Write(ByteOutputStream* stream);

	lua_State* L;
};

class DMInitEmmy : public DebugMessage
{
public:
	DMInitEmmy();

	void Read(ByteInputStream* stream) override;

	std::string symbolsDirectory;
	std::string emmyLuaFile;
};

class DMException : public DebugMessage
{
public:
	DMException();

	void Write(ByteOutputStream* stream) override;

	std::string message;
};

class DMLoadError : public DebugMessage
{
public:
	DMLoadError();

	void Write(ByteOutputStream* stream) override;

	std::string message;
};

class DMMessage : public DebugMessage
{
public:
	DMMessage(MessageType type, const char* message);

	void Write(ByteOutputStream* stream) override;

	MessageType type;
	std::string message;
};

class DMLoadScript : public DebugMessage
{
public:
	DMLoadScript();

	void Write(ByteOutputStream* stream) override;

	std::string fileName;
	std::string source;
	unsigned int index;
	unsigned int state;
};

class DMAddBreakpoint : public DebugMessage
{
public:
	DMAddBreakpoint();

	void Read(ByteInputStream* stream) override;

	unsigned int scriptIndex;
	unsigned int line;
	std::string expr;
};

class DMSetBreakpoint : public DebugMessage
{
public:
	DMSetBreakpoint();

	void Write(ByteOutputStream* stream) override;

	unsigned int scriptIndex;
	unsigned int line;
	bool success;
};

class DMBreak : public DebugMessage
{
public:
	DMBreak();

	void Write(ByteOutputStream* stream) override;

	std::string stackXML;
};

class DMDelBreakpoint : public DebugMessage
{
public:
	DMDelBreakpoint();

	void Read(ByteInputStream* stream) override;

	unsigned int scriptIndex;
	unsigned int line;
};

class DMEvaluate : public DebugMessage
{
public:
	DMEvaluate();

	void Read(ByteInputStream* stream) override;

	unsigned int evalId;
	unsigned int stackLevel;
	unsigned int depth;
	std::string expression;
};

class DMEvalResult : public DebugMessage
{
public:
	DMEvalResult();

	void Write(ByteOutputStream* stream) override;

	bool success;
	unsigned int evalId;
	std::string result;
};

class DMNameVM : public DebugMessage
{
public:
	DMNameVM();

	void Write(ByteOutputStream* stream) override;

	std::string name;
};

class DMProfilerBegin : public DebugMessage
{
public:
	DMProfilerBegin();
};

class DMProfilerEnd: public DebugMessage
{
public:
	DMProfilerEnd();
};

class DMProfilerData : public DebugMessage
{
public:
	DMProfilerData();

};