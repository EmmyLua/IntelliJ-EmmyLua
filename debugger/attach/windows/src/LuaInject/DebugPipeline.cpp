#include "DebugPipeline.h"
#include "TCPServer.h"
#include "Stream.h"
#include "DebugMessage.h"
#include "DebugBackend.h"

bool ChannelPipeline::Initialize()
{
	DWORD processId = GetCurrentProcessId();

	char eventChannelName[256];
	_snprintf(eventChannelName, 256, "Decoda.Event.%x", processId);

	char commandChannelName[256];
	_snprintf(commandChannelName, 256, "Decoda.Command.%x", processId);

	// Open up a communication channel with the debugger that is used to send
	// events back to the frontend.
	if (!m_eventChannel.Connect(eventChannelName))
	{
		return false;
	}

	// Open up a communication channel with the debugger that is used to receive
	// commands from the backend.
	if (!m_commandChannel.Connect(commandChannelName))
	{
		return false;
	}
	return true;
}

void ChannelPipeline::Destroy()
{
	m_eventChannel.Destroy();
	m_commandChannel.Destroy();
}

bool SocketPipeline::Initialize()
{
	DWORD processId = GetCurrentProcessId();
	return server.startup(processId, this);
}

bool SocketPipeline::IsAttached()
{
	return server.numConnections() > 0;
}

void SocketPipeline::onDisconnect(DebugClient* client)
{
	DebugMessage detach(DebugMessageId::Detach);
	DebugBackend::Get().HandleMessage(&detach);
}

void SocketPipeline::handleStream(ByteInputStream * stream)
{
	unsigned id = stream->ReadUInt32();
	DebugMessage* msg = nullptr;
	DebugMessageId msgId = (DebugMessageId)id;
	switch (msgId)
	{
	case DebugMessageId::InitEmmy:
		msg = new DMInitEmmy();
		break;
	case DebugMessageId::AddBreakpoint:
		msg = new DMAddBreakpoint();
		break;
	case DebugMessageId::DelBreakpoint:
		msg = new DMDelBreakpoint();
		break;
	case DebugMessageId::Evaluate:
		msg = new DMEvaluate();
		break;
	case DebugMessageId::ReqProfilerBegin:
		msg = new DMProfilerBegin();
		break;
	case DebugMessageId::ReqProfilerEnd:
		msg = new DMProfilerEnd();
		break;
	case DebugMessageId::Detach:
	case DebugMessageId::Break:
	case DebugMessageId::LoadDone:
	case DebugMessageId::Continue:
	case DebugMessageId::StepOver:
	case DebugMessageId::StepInto:
	case DebugMessageId::StepOut:
	case DebugMessageId::DeleteAllBreakpoints:
	default:
		msg = new DebugMessage(msgId);
		break;
	}

	msg->Read(stream);
	DebugBackend::Get().HandleMessage(msg);
	delete msg;
}

bool SocketPipeline::Send(DebugMessage* message)
{
	ByteOutputStream body;
	message->Write(&body);
	ByteOutputStream stream;
	stream.WriteUInt32(body.GetPositon());
	stream.Write((void*) body.GetBuf(), body.GetPositon());
	return server.sendMsg(stream.GetBuf(), stream.GetPositon());
}

bool DebugPipeline::Send(DebugMessage * message)
{
	return true;
}
