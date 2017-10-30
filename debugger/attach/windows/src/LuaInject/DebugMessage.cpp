#include "DebugMessage.h"
#include "Stream.h"

DebugMessage::DebugMessage(DebugMessageId id) : id(id), vm(0)
{
}

void DebugMessage::Read(ByteOutStream * stream)
{
	unsigned int idValue;
	stream->ReadUInt32(idValue);
	id = (DebugMessageId)idValue;
	stream->ReadSize(vm);
}

void DebugMessage::Write(ByteInStream * stream)
{

}

DebugMessageAddBreakpoint::DebugMessageAddBreakpoint() : DebugMessage(DebugMessageId::AddBreakpoint), scriptIndex(0), line(0)
{
}

void DebugMessageAddBreakpoint::Read(ByteOutStream* stream)
{
	DebugMessage::Read(stream);

	stream->ReadUInt32(scriptIndex);
	stream->ReadUInt32(line);
}

DebugMessageDelBreakpoint::DebugMessageDelBreakpoint() : DebugMessage(DebugMessageId::DelBreakpoint), scriptIndex(0),
                                                         line(0)
{
}

DebugMessageEvaluate::DebugMessageEvaluate() : DebugMessage(DebugMessageId::Evaluate), evalId(0), stackLevel(0),
                                               depth(0)
{
}
