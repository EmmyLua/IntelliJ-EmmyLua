#pragma once
#include <string>
#include <vector>
#include "Protocol.h"

class StackNodeContainer;
class EvalResultNode;
class ByteInputStream;
class ByteOutputStream;
struct lua_State;
class LPFunctionCall;

enum class DebugMessageId
{
	ReqInitialize,
	RespInitialize,

	Continue,
	StepOver,
	StepInto,
	StepOut,
	AddBreakpoint,
	DelBreakpoint,
	Break,
	Detach,
	PatchReplaceLine,
	PatchInsertLine,
	PatchDeleteLine,
	LoadDone,
	IgnoreException,
	DeleteAllBreakpoints,

	CreateVM,
	NameVM,
	DestroyVM,
	LoadScript,
	SetBreakpoint,
	Exception,
	LoadError,
	Message,
	SessionEnd,

	ReqEvaluate,
	RespEvaluate,

	ReqProfilerBegin,
	RespProfilerBegin,
	ReqProfilerEnd,
	RespProfilerEnd,
	RespProfilerData,

	ReqReloadScript,
	RespReloadScript,

	ReqStdin,
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

class DMReqInitialize : public DebugMessage
{
public:
	DMReqInitialize();

	void Read(ByteInputStream* stream) override;

	std::string symbolsDirectory;
	std::string emmyLuaFile;
	bool captureStd;
	bool captureOutputDebugString;
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
	DMMessage(MessageType type, const char* message, size_t size);
	~DMMessage();

	void Write(ByteOutputStream* stream) override;

	MessageType type;
	char* message;
	size_t messageSize;
};

class DMLoadScript : public DebugMessage
{
public:
	DMLoadScript();

	void Write(ByteOutputStream* stream) override;

	std::string fileName;
	std::string source;
	unsigned int index;
	char state;
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
	DMBreak(StackNodeContainer* stacks);
	~DMBreak();

	void Write(ByteOutputStream* stream) override;

	StackNodeContainer* stackList;
};

class DMDelBreakpoint : public DebugMessage
{
public:
	DMDelBreakpoint();

	void Read(ByteInputStream* stream) override;

	unsigned int scriptIndex;
	unsigned int line;
};

class DMReqEvaluate : public DebugMessage
{
public:
	DMReqEvaluate();

	void Read(ByteInputStream* stream) override;

	unsigned int evalId;
	unsigned int stackLevel;
	unsigned int depth;
	std::string expression;
};

class DMRespEvaluate : public DebugMessage
{
public:
	DMRespEvaluate();
	~DMRespEvaluate();

	void Write(ByteOutputStream* stream) override;

	unsigned int evalId;
	EvalResultNode* result;
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

	void Write(ByteOutputStream* stream) override;

	std::vector<LPFunctionCall*> callList;
};

class DMReqReloadScript : public DebugMessage
{
public:
	DMReqReloadScript();

	void Read(ByteInputStream* stream) override;

	size_t index;
};

class DMReqStdin : public DebugMessage
{
public:
	DMReqStdin();
	void Read(ByteInputStream* stream) override;
	std::string text;
};