#include "Stream.h"
#include <cstdlib>
#include <cassert>

ByteOutStream::ByteOutStream(char * buff, int size) : m_size(size), m_position(0)
{
	m_buff = (char*)malloc(size);
	memcpy(m_buff, buff, size);
}

ByteOutStream::~ByteOutStream()
{
	free(m_buff);
}

void ByteOutStream::ReadUInt32(unsigned int & value)
{
	int size = sizeof(unsigned int);
	assert(m_position + size <= m_size, "overflow");
	value = *(unsigned int*)m_buff[m_position];
	m_position = m_position + size;
}

void ByteOutStream::ReadSize(size_t & value)
{
	int size = sizeof(size_t);
	assert(m_position + size <= m_size, "overflow");
	value = *(size_t*)m_buff[m_position];
	m_position = m_position + size;
}

void ByteOutStream::ReadString(std::string & value)
{
	unsigned int size;
	ReadUInt32(size);
	assert(m_position + size <= m_size, "overflow");
	value.copy((char*)m_buff[m_position], size);
	m_position = m_position + size;
}

ByteInStream::ByteInStream() : m_position(0)
{
	m_size = 1024 * 1024;
	m_buff = (char*)malloc(m_size);
}

ByteInStream::~ByteInStream()
{
	free(m_buff);
}

void ByteInStream::Write(void * data, int size)
{
	assert(m_position + size <= m_size);
	memcpy(m_buff, data, size);
	m_position += size;
}

void ByteInStream::WriteUInt32(unsigned int value)
{
	Write(&value, 4);
}

void ByteInStream::WriteSize(size_t value)
{
	Write(&value, 8);
}

void ByteInStream::WriteString(const std::string& value)
{
	int len = value.length();
	WriteUInt32(len);
	if (len > 0)
	{
		Write((void*)value.c_str(), len);
	}
}
