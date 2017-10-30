#pragma once
#include <string>
#include "Channel.h"

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

	virtual bool Initialize() = 0;
	virtual void Destroy() = 0;
};

class ChannelPipeline : public DebugPipeline
{
	Channel m_eventChannel;
	Channel m_commandChannel;
public:
	bool Initialize() override;
	void Destroy() override;

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

class SocketPipeline : public DebugPipeline
{
	
};