#pragma once
#include "Stream.h"
#include "Channel.h"
#include "TCPServer.h"

class DebugMessage;

class DebugPipeline
{
public:
	virtual ~DebugPipeline() = default;

	virtual bool Send(DebugMessage* message);
	virtual bool Initialize() = 0;
	virtual void Destroy() = 0;
	virtual bool IsAttached() = 0;
};

class ChannelPipeline : public DebugPipeline
{
	Channel m_eventChannel;
	Channel m_commandChannel;
public:
	bool Initialize() override;
	void Destroy() override;
	bool IsAttached() override { return true; }
};

class SocketPipeline : public DebugPipeline, public DebugServerListener
{
	DebugServer server;
	ByteOutputStream bodyStream;
	ByteOutputStream dataStream;
public:
	bool Initialize() override;
	void Destroy() override {}
	bool IsAttached() override;
	bool Send(DebugMessage* message) override;

	void onDisconnect(DebugClient* client) override;
	void handleStream(ByteInputStream* stream) override;
};