#include "DebugMessage.h"
#include "Stream.h"

DebugMessage::DebugMessage(DebugMessageId idValue) : id(idValue), L(nullptr)
{
}

void DebugMessage::Read(ByteInputStream * stream)
{
	size_t t = stream->ReadSize();
	L = (lua_State*)t;
}

void DebugMessage::Write(ByteOutputStream * stream)
{
	stream->WriteUInt32((unsigned int)id);
	stream->WriteSize(reinterpret_cast<size_t>(L));
}

DMInitEmmy::DMInitEmmy(): DebugMessage(DebugMessageId::InitEmmy)
{
}

void DMInitEmmy::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	stream->ReadString(symbolsDirectory);
	stream->ReadString(emmyLuaFile);
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

DMMessage::DMMessage(MessageType type, const char* message): DebugMessage(DebugMessageId::Message), type(type), message(message)
{
}

void DMMessage::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteUInt32(type);
	stream->WriteString(message);
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
	stream->WriteUInt32(state);
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

DMBreak::DMBreak(): DebugMessage(DebugMessageId::Break)
{
}

void DMBreak::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteString(stackXML);
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

DMEvaluate::DMEvaluate() : DebugMessage(DebugMessageId::Evaluate), evalId(0), stackLevel(0),
                                               depth(0)
{
}

void DMEvaluate::Read(ByteInputStream* stream)
{
	DebugMessage::Read(stream);
	evalId = stream->ReadUInt32();
	stackLevel = stream->ReadUInt32();
	depth = stream->ReadUInt32();
	stream->ReadString(expression);
}

DMEvalResult::DMEvalResult(): DebugMessage(DebugMessageId::EvalResult), success(false), evalId(0)
{
}

void DMEvalResult::Write(ByteOutputStream* stream)
{
	DebugMessage::Write(stream);
	stream->WriteUInt32(success);
	stream->WriteUInt32(evalId);
	stream->WriteString(result);
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
