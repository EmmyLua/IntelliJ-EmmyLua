#include "DebugPipeline.h"

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
