#include "endian.h"

/* Big-Endian */

uint16_t readUint16InBigEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint16_t)p[0]) << 8) |
		(((uint16_t)p[1]));
}

uint32_t readUint32InBigEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint32_t)p[0]) << 24) |
		(((uint32_t)p[1]) << 16) |
		(((uint32_t)p[2]) << 8) |
		(((uint32_t)p[3]));
}


uint64_t readUint64InBigEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint64_t)p[0]) << 56) |
		(((uint64_t)p[1]) << 48) |
		(((uint64_t)p[2]) << 40) |
		(((uint64_t)p[3]) << 32) |
		(((uint64_t)p[4]) << 24) |
		(((uint64_t)p[5]) << 16) |
		(((uint64_t)p[6]) << 8) |
		(((uint64_t)p[7]));
}


void writeUint16InBigEndian(void* memory, uint16_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[0] = (uint8_t)(value >> 8);
	p[1] = (uint8_t)(value);
}


void writeUint32InBigEndian(void* memory, uint32_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[0] = (uint8_t)(value >> 24);
	p[1] = (uint8_t)(value >> 16);
	p[2] = (uint8_t)(value >> 8);
	p[3] = (uint8_t)(value);
}


void writeUint64InBigEndian(void* memory, uint64_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[0] = (uint8_t)(value >> 56);
	p[1] = (uint8_t)(value >> 48);
	p[2] = (uint8_t)(value >> 40);
	p[3] = (uint8_t)(value >> 32);
	p[4] = (uint8_t)(value >> 24);
	p[5] = (uint8_t)(value >> 16);
	p[6] = (uint8_t)(value >> 8);
	p[7] = (uint8_t)(value);
}


int16_t readInt16InBigEndian(void* memory)
{
	return (int16_t)readUint16InBigEndian(memory);
}


int32_t readInt32InBigEndian(void* memory)
{
	return (int32_t)readUint32InBigEndian(memory);
}


int64_t readInt64InBigEndian(void* memory)
{
	return (int64_t)readUint64InBigEndian(memory);
}


void writeInt16InBigEndian(void* memory, int16_t value)
{
	writeUint16InBigEndian(memory, (uint16_t)value);
}


void writeInt32InBigEndian(void* memory, int32_t value)
{
	writeUint32InBigEndian(memory, (uint32_t)value);
}


void writeInt64InBigEndian(void* memory, int64_t value)
{
	writeUint64InBigEndian(memory, (uint64_t)value);
}

/* Little-Endian */

uint16_t readUint16InLittleEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint16_t)p[1]) << 8) |
		(((uint16_t)p[0]));
}

uint32_t readUint32InLittleEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint32_t)p[3]) << 24) |
		(((uint32_t)p[2]) << 16) |
		(((uint32_t)p[1]) << 8) |
		(((uint32_t)p[0]));
}


uint64_t readUint64InLittleEndian(void* memory)
{
	uint8_t* p = (uint8_t*)memory;
	return (((uint64_t)p[7]) << 56) |
		(((uint64_t)p[6]) << 48) |
		(((uint64_t)p[5]) << 40) |
		(((uint64_t)p[4]) << 32) |
		(((uint64_t)p[3]) << 24) |
		(((uint64_t)p[2]) << 16) |
		(((uint64_t)p[1]) << 8) |
		(((uint64_t)p[0]));
}


void writeUint16InLittleEndian(void* memory, uint16_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[1] = (uint8_t)(value >> 8);
	p[0] = (uint8_t)(value);
}


void writeUint32InLittleEndian(void* memory, uint32_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[3] = (uint8_t)(value >> 24);
	p[2] = (uint8_t)(value >> 16);
	p[1] = (uint8_t)(value >> 8);
	p[0] = (uint8_t)(value);
}


void writeUint64InLittleEndian(void* memory, uint64_t value)
{
	uint8_t* p = (uint8_t*)memory;
	p[7] = (uint8_t)(value >> 56);
	p[6] = (uint8_t)(value >> 48);
	p[5] = (uint8_t)(value >> 40);
	p[4] = (uint8_t)(value >> 32);
	p[3] = (uint8_t)(value >> 24);
	p[2] = (uint8_t)(value >> 16);
	p[1] = (uint8_t)(value >> 8);
	p[0] = (uint8_t)(value);
}

int16_t readInt16InLittleEndian(void* memory)
{
	return (int16_t)readUint16InLittleEndian(memory);
}


int32_t readInt32InLittleEndian(void* memory)
{
	return (int32_t)readUint32InLittleEndian(memory);
}


int64_t readInt64InLittleEndian(void* memory)
{
	return (int64_t)readUint64InLittleEndian(memory);
}


void writeInt16InLittleEndian(void* memory, int16_t value)
{
	writeUint16InLittleEndian(memory, (uint16_t)value);
}


void writeInt32InLittleEndian(void* memory, int32_t value)
{
	writeUint32InLittleEndian(memory, (uint32_t)value);
}


void writeInt64InLittleEndian(void* memory, int64_t value)
{
	writeUint64InLittleEndian(memory, (uint64_t)value);
}