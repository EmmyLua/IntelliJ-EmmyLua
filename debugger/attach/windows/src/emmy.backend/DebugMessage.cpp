#include "DebugMessage.h"
#include "Stream.h"
#include "LuaProfiler.h"
#include "StackNode.h"

DebugMessage::DebugMessage(DebugMessageId idValue) : id(idValue), L(nullptr)
{
}

void DebugMessage::Read(ByteInputStream * stream)
{
	L = (lua_State*)stream->ReadUInt64();
}

void DebugMessage::Write(ByteOutputStream * stream)
{
	stream->WriteUInt32((unsigned int)id);
	stream->WriteUInt64(reinterpret_cast<uint64_t>(L));
}

DMReqInitialize::DMReqInitialize(): DebugMessage(DebugMessageId::ReqInitialize), captureStd(false), captureOutputDebugString(false)
{
}

void DMReqInitialize::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	stream->ReadString(symbolsDirectory);
	stream->ReadString(emmyLuaFile);
	captureStd = stream->ReadBool();
	captureOutputDebugString = stream->ReadBool();
}

DMException::DMException(): DebugMessage(DebugMessageId::Exception)
{
}

void DMException::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteString(message);
}

DMLoadError::DMLoadError(): DebugMessage(DebugMessageId::LoadError)
{
}

void DMLoadError::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteString(message);
}

DMMessage::DMMessage(MessageType type, const char* message, size_t size): DebugMessage(DebugMessageId::Message), type(type)
{
	this->message = (char*) malloc(size);
	memcpy(this->message, message, size);
	this->messageSize = size;
}

DMMessage::~DMMessage()
{
	delete this->message;
}

void DMMessage::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteUInt32(type);
	stream->WriteUInt32(this->messageSize);
	stream->Write((void*)this->message, this->messageSize);
}

DMLoadScript::DMLoadScript(): DebugMessage(DebugMessageId::LoadScript), index(0), state(0)
{
}

void DMLoadScript::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteString(fileName);
	stream->WriteString(source);
	stream->WriteUInt32(index);
	stream->WriteByte(state);
}

DMAddBreakpoint::DMAddBreakpoint() : DebugMessage(DebugMessageId::AddBreakpoint), scriptIndex(0), line(0)
{
}

void DMAddBreakpoint::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);

	scriptIndex = stream->ReadUInt32();
	line = stream->ReadUInt32();
	stream->ReadString(expr);
}

DMSetBreakpoint::DMSetBreakpoint(): DebugMessage(DebugMessageId::SetBreakpoint), scriptIndex(0), line(0), success(false)
{
}

void DMSetBreakpoint::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteUInt32(scriptIndex);
	stream->WriteUInt32(line);
	stream->WriteUInt32(success);
}

DMBreak::DMBreak(StackNodeContainer* stacks): DebugMessage(DebugMessageId::Break), stackList(stacks)
{
}

DMBreak::~DMBreak()
{
	if (stackList != nullptr)
		delete stackList;
}

void DMBreak::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stackList->Write(stream);
}

DMDelBreakpoint::DMDelBreakpoint() : DebugMessage(DebugMessageId::DelBreakpoint), scriptIndex(0),
                                                         line(0)
{
}

void DMDelBreakpoint::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	scriptIndex = stream->ReadUInt32();
	line = stream->ReadUInt32();
}

DMReqEvaluate::DMReqEvaluate() : DebugMessage(DebugMessageId::ReqEvaluate), evalId(0), stackLevel(0),
                                               depth(0)
{
}

void DMReqEvaluate::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	evalId = stream->ReadUInt32();
	stackLevel = stream->ReadUInt32();
	depth = stream->ReadUInt32();
	stream->ReadString(expression);
}

DMRespEvaluate::DMRespEvaluate(): DebugMessage(DebugMessageId::RespEvaluate), evalId(0), result(nullptr)
{
}

DMRespEvaluate::~DMRespEvaluate()
{
	if (result != nullptr)
		delete result;
}

void DMRespEvaluate::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteUInt32(evalId);
	stream->WriteBool(result != nullptr);
	if (result != nullptr)
		result->Write(stream);
}

DMNameVM::DMNameVM(): DebugMessage(DebugMessageId::NameVM)
{
}

void DMNameVM::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteString(name);
}

DMProfilerBegin::DMProfilerBegin() : DebugMessage(DebugMessageId::ReqProfilerBegin)
{
}

DMProfilerEnd::DMProfilerEnd() : DebugMessage(DebugMessageId::ReqProfilerEnd)
{
}

DMProfilerData::DMProfilerData() : DebugMessage(DebugMessageId::RespProfilerData)
{
}

void DMProfilerData::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);

	stream->WriteUInt32(this->callList.size());
	for (auto call : callList)
	{
		stream->WriteUInt32(call->id);
		stream->WriteString(call->file);
		stream->WriteString(call->name);
		stream->WriteUInt32(call->line);
		stream->WriteUInt32(call->count);
		stream->WriteUInt32(call->time);
	}
}

DMReqReloadScript::DMReqReloadScript() : DebugMessage(DebugMessageId::ReqReloadScript), index(0)
{
}

void DMReqReloadScript::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	index = stream->ReadUInt32();
}

DMReqStdin::DMReqStdin() : DebugMessage(DebugMessageId::ReqStdin)
{
}

void DMReqStdin::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	stream->ReadString(this->text);
}
