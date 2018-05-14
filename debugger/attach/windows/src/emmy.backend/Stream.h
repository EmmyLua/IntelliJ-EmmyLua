#pragma once
#include <string>

class ByteInputStream
{
	char* m_buff;
	size_t m_size;
	size_t m_position;
public:
	ByteInputStream();
	ByteInputStream(char* buff, size_t size);
	~ByteInputStream();

	void Reset(const void* data, size_t size);
	/**
	* Reads a 32-bit unsigned integer from the channel. This operation blocks
	* until the data is available.
	*/
	uint32_t ReadUInt32();

	uint64_t ReadUInt64();

	/**
	* Reads a string from the channel. This operation blocks until the
	* data is available.
	*/
	void ReadString(std::string& value);

	char ReadByte();

	bool ReadBool();
};

class ByteOutputStream
{
	char* m_buff;
	size_t m_size;
	size_t m_position;
public:
	ByteOutputStream();
	~ByteOutputStream();

	void Write(void* data, size_t size);

	void WriteUInt32(uint32_t value);

	void WriteUInt64(uint64_t value);

	void WriteString(const std::string& value);

	void WriteByte(char value);

	void WriteBool(bool value);

	const char* GetBuf() const { return m_buff; }

	size_t GetSize() const { return m_size; }

	size_t GetPositon() { return m_position; }

	void Reset() { m_position = 0; }
};