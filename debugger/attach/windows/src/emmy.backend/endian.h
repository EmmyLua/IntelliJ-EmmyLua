#ifndef ENDIAN_H
#define ENDIAN_H

#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

	/* Big-Endian */
	uint16_t readUint16InBigEndian(void* memory);
	uint32_t readUint32InBigEndian(void* memory);
	uint64_t readUint64InBigEndian(void* memory);

	void writeUint16InBigEndian(void* memory, uint16_t value);
	void writeUint32InBigEndian(void* memory, uint32_t value);
	void writeUint64InBigEndian(void* memory, uint64_t value);

	int16_t readInt16InBigEndian(void* memory);
	int32_t readInt32InBigEndian(void* memory);
	int64_t readInt64InBigEndian(void* memory);

	void writeInt16InBigEndian(void* memory, int16_t value);
	void writeInt32InBigEndian(void* memory, int32_t value);
	void writeInt64InBigEndian(void* memory, int64_t value);


	/* Little-Endian */
	uint16_t readUint16InLittleEndian(void* memory);
	uint32_t readUint32InLittleEndian(void* memory);
	uint64_t readUint64InLittleEndian(void* memory);

	void writeUint16InLittleEndian(void* memory, uint16_t value);
	void writeUint32InLittleEndian(void* memory, uint32_t value);
	void writeUint64InLittleEndian(void* memory, uint64_t value);

	int16_t readInt16InLittleEndian(void* memory);
	int32_t readInt32InLittleEndian(void* memory);
	int64_t readInt64InLittleEndian(void* memory);

	void writeInt16InLittleEndian(void* memory, int16_t value);
	void writeInt32InLittleEndian(void* memory, int32_t value);
	void writeInt64InLittleEndian(void* memory, int64_t value);

#ifdef __cplusplus
}
#endif

#endif