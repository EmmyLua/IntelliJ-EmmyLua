#pragma once
#include <string>

class DebugPipeline
{
public:
	virtual ~DebugPipeline() = default;
	virtual void WriteUInt32(unsigned int value) = 0;
	virtual void WriteSize(size_t value) = 0;
	virtual void WriteString(const std::string& value) = 0;
	virtual void WriteBool(bool value) = 0;

	virtual void ReadUInt32(unsigned int& value) = 0;
	virtual void ReadSize(size_t& size) = 0;
	virtual void ReadString(std::string& value) = 0;
	virtual void ReadBool(bool& value) = 0;
};
