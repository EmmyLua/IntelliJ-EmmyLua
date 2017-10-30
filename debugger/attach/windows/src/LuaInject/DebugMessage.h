#pragma once
#include <string>

class ByteOutStream;
class ByteInStream;

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
};

class DebugMessage
{
	DebugMessageId id;
public:
	virtual ~DebugMessage() = default;
	DebugMessage(DebugMessageId id);

	DebugMessageId getId() const { return id; }

	virtual void Read(ByteOutStream* stream);
	virtual void Write(ByteInStream* stream);

	size_t  vm;
};

class DebugMessageAddBreakpoint : public DebugMessage
{
public:
	DebugMessageAddBreakpoint();

	void Read(ByteOutStream* stream) override;

	unsigned int scriptIndex;
	unsigned int line;
	std::string expr;
};

class DebugMessageDelBreakpoint : public DebugMessage
{
public:
	DebugMessageDelBreakpoint();

	unsigned int scriptIndex;
	unsigned int line;
};

class DebugMessageEvaluate : public DebugMessage
{
public:
	DebugMessageEvaluate();

	unsigned int evalId;
	unsigned int stackLevel;
	unsigned int depth;
	std::string expression;
};