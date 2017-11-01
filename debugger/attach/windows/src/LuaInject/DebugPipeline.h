#pragma once
#include <string>
#include "Channel.h"
#include "TCPServer.h"

class DebugMessage;

class DebugPipeline
{
public:
	virtual ~DebugPipeline() = default;
	virtual void WriteUInt32(unsigned int value) = 0;
	virtual void WriteSize(size_t value) = 0;
	virtual void WriteString(const char* value) = 0;
	virtual void WriteString(const std::string& value) = 0;
	virtual void WriteBool(bool value) = 0;
	virtual void Flush() = 0;

	virtual bool ReadUInt32(unsigned int& value) = 0;
	virtual bool ReadSize(size_t& size) = 0;
	virtual bool ReadString(std::string& value) = 0;
	virtual bool ReadBool(bool& value) = 0;

	virtual void Send(DebugMessage* message);

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

	void WriteUInt32(unsigned value) override { m_eventChannel.WriteUInt32(value); }
	void WriteSize(size_t value) override { m_eventChannel.WriteSize(value); }
	void WriteString(const char* value) override { m_eventChannel.WriteString(value); }
	void WriteString(const std::string& value) override { m_eventChannel.WriteString(value); }
	void WriteBool(bool value) override { m_eventChannel.WriteBool(value); }
	void Flush() override { m_eventChannel.Flush(); }

	bool ReadUInt32(unsigned& value) override { return m_commandChannel.ReadUInt32(value); }
	bool ReadSize(size_t& value) override { return m_commandChannel.ReadSize(value); }
	bool ReadString(std::string& value) override { return m_commandChannel.ReadString(value); }
	bool ReadBool(bool& value) override { return m_commandChannel.ReadBool(value); }
};

class SocketPipeline : public DebugPipeline, public DebugServerListener
{
	DebugServer server;
public:
	bool Initialize() override;
	void Destroy() override {}
	bool IsAttached() override;

	void WriteUInt32(unsigned value) override {  }
	void WriteSize(size_t value) override { }
	void WriteString(const char* value) override {  }
	void WriteString(const std::string& value) override { }
	void WriteBool(bool value) override { }
	void Flush() override { }

	bool ReadUInt32(unsigned& value) override { return false; }
	bool ReadSize(size_t& value) override { return false; }
	bool ReadString(std::string& value) override { return false; }
	bool ReadBool(bool& value) override { return false; }

	void onDisconnect(DebugClient* client) override;
	void handleStream(ByteInputStream* stream) override;
	void Send(DebugMessage* message) override;
};