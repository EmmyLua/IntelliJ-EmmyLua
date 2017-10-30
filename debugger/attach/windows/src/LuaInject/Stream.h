#pragma once
#include <string>

class ByteOutStream
{
	char* m_buff;
	int m_size;
	int m_position;
public:
	ByteOutStream(char* buff, int size);
	~ByteOutStream();
	/**
	* Reads a 32-bit unsigned integer from the channel. This operation blocks
	* until the data is available.
	*/
	void ReadUInt32(unsigned int& value);

	void ReadSize(size_t& size);

	/**
	* Reads a string from the channel. This operation blocks until the
	* data is available.
	*/
	void ReadString(std::string& value);
};

class ByteInStream
{
	char* m_buff;
	size_t m_size;
	int m_position;
public:
	ByteInStream();
	~ByteInStream();

	void Write(void* data, int size);

	void WriteUInt32(unsigned int value);

	void WriteSize(size_t value);

	void WriteString(const std::string& value);

	const char* GetBuf() const { return m_buff; }

	size_t GetSize() const { return m_size; }

	void Reset() { m_position = 0; }
};