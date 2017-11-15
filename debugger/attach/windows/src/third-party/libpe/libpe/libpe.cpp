/*
 * This file is part of the libpe portable executable parsing library.
 *
 * Copyleft of Simone Margaritelli aka evilsocket <evilsocket@gmail.com>
 * http://www.evilsocket.net/
 *
 * libpe is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * libpe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with libpe.  If not, see <http://www.gnu.org/licenses/>.
*/
#include "libpe.h"
#include <stdio.h>
#include <stdlib.h>
#include <dbghelp.h>

#pragma comment( lib, "dbghelp" )

#pragma region Macros

#define PE_INVALID_OFFSET ((uint64_t)-1)

#define PE_IS_VALID_ADDRESS( PEADDR ) \
	( (PEADDR).VA     != PE_INVALID_OFFSET && \
	  (PEADDR).Offset != PE_INVALID_OFFSET && \
	  (PEADDR).Data   != NULL \
	)

#define PE_IS_ADDRESS_BETWEEN( left, right, address ) \
	( (address) >= (left) && (address) < (right) )

#define PE_SET_PARSED( PE, WHAT ) \
	(PE)->dwParseState |= PE_##WHAT##_PARSED

#define PE_MASK_HAS_BIT( MASK, BIT ) \
	( ( (MASK) & (BIT) ) != 0 )

#define PE_IS_PARSED( PE, WHAT ) \
	PE_MASK_HAS_BIT( (PE)->dwParseState, PE_##WHAT##_PARSED )

#define PE_HEADERS_OPT_FIELD( PE, NAME ) \
	( (PE)->Headers.Plus ? (PE)->Headers.OPT.p64->##NAME : (PE)->Headers.OPT.p32->##NAME )

#define PE_SECTION_HEADER_SIZE( SEC_HEAD ) \
	( (SEC_HEAD)->SizeOfRawData > 0 ? (SEC_HEAD)->SizeOfRawData : (SEC_HEAD)->Misc.VirtualSize )

#define PE_ADDRESS_FROM_VA( PE, PE_ADDR, VADDR ) \
	(PE_ADDR).Size   = 0; \
	(PE_ADDR).VA     = VADDR; \
	(PE_ADDR).Offset = peRawOffsetByVA( (PE), (PE_ADDR).VA ); \
	if( (PE_ADDR).Offset == PE_INVALID_OFFSET ) \
	{ \
		(PE_ADDR).VA     = \
		(PE_ADDR).Offset = PE_INVALID_OFFSET; \
		(PE_ADDR).Data   = NULL; \
	} \
	else \
	{ \
		(PE_ADDR).Size = (PE)->dwFileSize - (PE_ADDR).Offset; \
		(PE_ADDR).Data = &(PE)->pData[(PE_ADDR).Offset]; \
	}

#define PE_GET_POINTER(PE,RVA) \
	( (PE)->pData + peRawOffsetByVA( (PE), (PE)->qwBaseAddress + (RVA) ) )

#define PE_INIT_MODULE( MOD, NAME ) \
	peCopyString( (MOD)->Name, NAME, MAX_PATH ); \
	ll_init( &(MOD)->Symbols ); \
	(MOD)->ByName = HT_CREATE_BY_STRING()

#define PE_ADD_IMPORTED_MODULE( PE, MOD ) \
	ht_add( (PE)->ImportTable.ByName, (void *)(MOD)->Name, (MOD) ); \
	ll_append( &(PE)->ImportTable.Modules, (MOD) )

#define PE_IMAGE_FIRST_SECTION32( ntheader ) ((PIMAGE_SECTION_HEADER)        \
    ((ULONG_PTR)(ntheader) +                                            \
     FIELD_OFFSET( IMAGE_NT_HEADERS32, OptionalHeader ) +                 \
     ((ntheader))->FileHeader.SizeOfOptionalHeader   \
    ))

#define PE_IMAGE_FIRST_SECTION64( ntheader ) ((PIMAGE_SECTION_HEADER)        \
    ((ULONG_PTR)(ntheader) +                                            \
     FIELD_OFFSET( IMAGE_NT_HEADERS64, OptionalHeader ) +                 \
     ((ntheader))->FileHeader.SizeOfOptionalHeader   \
    ))

typedef void *(* PE_STRING_DUP)( void * );

#pragma endregion

#pragma region Utilities

inline void peCopyString( char *pszDest, char *pszSource, uint32_t dwSize )
{
	char *pd = pszDest, *ps = pszSource;
	uint8_t b;
	uint32_t i;

	ZeroMemory( pszDest, dwSize );

	for( i = 0; i < dwSize; ++i, ++pd, ++ps )
	{
		__try
		{
			b = (uint8_t)*ps;
		}
		__except(EXCEPTION_EXECUTE_HANDLER)
		{
			b = 0x00;
		}

		if( b == 0x00 )
			break;

		else if( isprint(b) == false )
			break;

		else
			*pd = b;
	}
}

bool peIsValidSectionHeader( PIMAGE_SECTION_HEADER pHeader )
{
	__try
	{
		return ( pHeader && PE_SECTION_HEADER_SIZE( pHeader ) > 0 );
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{

	}

	return false;
}

bool peIsValidImportDescriptor( PIMAGE_IMPORT_DESCRIPTOR pDescriptor )
{
	__try
	{
		return ( pDescriptor && pDescriptor->Name != 0 && pDescriptor->FirstThunk );
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
	{

	}

	return false;
}

bool peFileExists( const char *pszFileName )
{
	uint32_t dwAttrib = GetFileAttributes(pszFileName);

	return (dwAttrib != INVALID_FILE_ATTRIBUTES && !(dwAttrib & FILE_ATTRIBUTE_DIRECTORY));
}

PIMAGE_SECTION_HEADER peSectionByVA( PE *pe, uint64_t va )
{
	PIMAGE_SECTION_HEADER pSectionHeader = pe->Sections.pHeaders;

	for( uint32_t i = 0; i < pe->Sections.dwNumber; i++, pSectionHeader++ )
	{
		if( peIsValidSectionHeader( pSectionHeader ) )
		{
			uint64_t qwStart = pe->qwBaseAddress + pSectionHeader->VirtualAddress,
					  qwEnd = qwStart + PE_SECTION_HEADER_SIZE( pSectionHeader );

			if( PE_IS_ADDRESS_BETWEEN( qwStart, qwEnd, va ) )
			{
				return pSectionHeader;
			}
		}
	}

	return 0;
}

uint64_t peRawOffsetByVA( PE *pe, uint64_t va )
{
	PIMAGE_SECTION_HEADER pSectionHeader;
	uint64_t qwOffset, qwDelta;

	pSectionHeader = peSectionByVA( pe, va );
	if ( !pSectionHeader )
	{
		return PE_INVALID_OFFSET;
	}

	qwDelta  = va - ( pe->qwBaseAddress + pSectionHeader->VirtualAddress );
	qwOffset = pSectionHeader->PointerToRawData + qwDelta;

	return qwOffset >= pe->dwFileSize ? PE_INVALID_OFFSET : qwOffset;
}

static int peIsPrintable( int byte, PE_STRING_ENCODING encoding )
{
	return encoding == Ascii ? ( byte >= -1 && byte <= 255 && isprint(byte) ) : iswprint(byte);
}

template<typename T> void peExtractStrings( PE *pe, uint32_t dwMinSize, PE_STRING_ENCODING encoding, PE_STRING_DUP dup, ht_t *LookupTable )
{
#define STRING_BUFF_SIZE 0xFFFF

	T zBuffer[STRING_BUFF_SIZE] = {0};
	int byte = 0;
	uint32_t dwOffset, dwEnd = ( pe->dwFileSize - sizeof(T) ), dwCurrent = 0;
	PE_STRING *pString = NULL;

	for( dwOffset = 0; dwOffset < dwEnd; dwOffset += sizeof(T) )
	{
		byte = *(T *)&pe->pData[dwOffset];
		if( peIsPrintable( byte, encoding ) == false )
		{
			if( dwCurrent > dwMinSize )
			{
				zBuffer[dwCurrent] = 0x00;

				if( ht_get( LookupTable, zBuffer ) == NULL )
				{
					pString = (PE_STRING *)malloc( sizeof(PE_STRING) );

					pString->Encoding   = encoding;
					pString->CharLength = dwCurrent - 1;
					pString->ByteLength = pString->CharLength * sizeof(T);
					pString->Data		= (uint8_t *)dup(zBuffer);
					pString->Offset		= dwOffset;

					ht_add( LookupTable, pString->Data, pString );
					ll_append( &pe->Strings.List, pString );
				}
			}

			dwCurrent = 0;
		}
		else if( dwCurrent < STRING_BUFF_SIZE - 2 )
		{
			zBuffer[dwCurrent++] = byte;
		}
	}
}

#pragma endregion

#pragma region Internals

PE_STATUS peParseBuffer( PE *pe )
{
#define PE_SAFE_CAST( WHAT, TYPE, POS ) \
	if( POS < pe->dwFileSize && pe->dwFileSize - POS >= sizeof(TYPE) ) \
	{ \
		WHAT = (P##TYPE)(pe->pData + (POS)); \
	} \
	else \
	{ \
		return PE_UNEXPECTED_EOF; \
	} \

	PE_SAFE_CAST( pe->Headers.DOS, IMAGE_DOS_HEADER, 0 );
	if( pe->Headers.DOS->e_magic != IMAGE_DOS_SIGNATURE )
	{
		return PE_NOT_PE;
	}

	PE_SAFE_CAST( pe->Headers.NT, IMAGE_NT_HEADERS, (ULONG)pe->Headers.DOS->e_lfanew );
	if( pe->Headers.NT->Signature != IMAGE_NT_SIGNATURE )
	{
		return PE_NOT_PE;
	}

	if( pe->Headers.NT->OptionalHeader.Magic == IMAGE_NT_OPTIONAL_HDR32_MAGIC )
	{
		pe->Headers.Plus    = FALSE;
		pe->Headers.OPT.p32 = &((PIMAGE_NT_HEADERS32)pe->Headers.NT)->OptionalHeader;
		pe->qwBaseAddress   = pe->Headers.OPT.p32->ImageBase;
		pe->dwImageSize     = pe->Headers.OPT.p32->SizeOfImage;

		pe->Sections.pHeaders = PE_IMAGE_FIRST_SECTION32( pe->Headers.NT );
	}
	else if( pe->Headers.NT->OptionalHeader.Magic == IMAGE_NT_OPTIONAL_HDR64_MAGIC )
	{
		pe->Headers.Plus    = TRUE;
		pe->Headers.OPT.p64 = &((PIMAGE_NT_HEADERS64)pe->Headers.NT)->OptionalHeader;
		pe->qwBaseAddress   = pe->Headers.OPT.p64->ImageBase;
		pe->dwImageSize     = pe->Headers.OPT.p64->SizeOfImage;

		pe->Sections.pHeaders = PE_IMAGE_FIRST_SECTION64( pe->Headers.NT );
	}
	else
	{
		return PE_NOT_PE;
	}

	pe->Sections.dwNumber = pe->Headers.NT->FileHeader.NumberOfSections;

	PE_SET_PARSED( pe, SECTIONS );

	PE_ADDRESS_FROM_VA
	(
		pe,
		pe->EntryPoint,
		pe->qwBaseAddress + PE_HEADERS_OPT_FIELD( pe, AddressOfEntryPoint )
	);

	PE_SET_PARSED( pe, ENTRY );

	return PE_SUCCESS;
}

/*
 * With both implicit and explicit linking, Windows first searches for "known DLLs", such as Kernel32.dll and User32.dll.
 * Windows then searches for the DLLs in the following sequence:
 *
 * - The directory where the executable module for the current process is located.
 * - The current directory.
 * - The Windows system directory. The GetSystemDirectory function retrieves the path of this directory.
 * - The Windows directory. The GetWindowsDirectory function retrieves the path of this directory.
 * - The directories listed in the PATH environment variable.
 */
PE_STATUS peLocateModule( PE *exe, PE_IMPORT_MODULE *pModule, PE *peModule )
{
	PE_STATUS status = PE_NOT_FOUND;
	char szExeFilePath[MAX_PATH + 1] = {0},
		 szModuleFilePath[MAX_PATH + 1] = {0},
		 szSystemDirectory[MAX_PATH + 1] = {0},
		 szWindowsDirectory[MAX_PATH + 1] = {0},
		*pszPaths = NULL,
		*pszPointer = NULL;

	if( exe->szFileName[0] != '[' )
	{
		GetFullPathName( exe->szFileName, MAX_PATH, szExeFilePath, NULL );

		pszPointer = strrchr( szExeFilePath, '\\' );
		if( pszPointer )
			*pszPointer = '\0';
	}

	sprintf_s( szModuleFilePath, "%s\\%s", szExeFilePath, pModule->Name );
	if( peFileExists( szModuleFilePath ) )
	{
		return peOpenFile( peModule, szModuleFilePath );
	}

	GetSystemDirectory( szSystemDirectory, MAX_PATH );
	sprintf_s( szModuleFilePath, "%s\\%s", szSystemDirectory, pModule->Name );
	if( peFileExists( szModuleFilePath ) )
	{
		return peOpenFile( peModule, szModuleFilePath );
	}

	GetWindowsDirectory( szWindowsDirectory, MAX_PATH );
	sprintf_s( szModuleFilePath, "%s\\%s", szWindowsDirectory, pModule->Name );
	if( peFileExists( szModuleFilePath ) )
	{
		return peOpenFile( peModule, szModuleFilePath );
	}

	size_t len;
	errno_t err = _dupenv_s( &pszPaths, &len, "PATH" );
	if( err == 0 && pszPaths )
	{
		char *pszCurrentPath = pszPaths;
		for(;;)
		{
			pszPointer = strchr( pszCurrentPath, ';' );
			if( pszPointer )
			{
				*pszPointer = '\0';

				sprintf_s( szModuleFilePath, "%s\\%s", pszCurrentPath, pModule->Name );
				if( peFileExists( szModuleFilePath ) )
				{
					free( pszPaths );

					return peOpenFile( peModule, szModuleFilePath );
				}

				*pszPointer = ';';
				pszCurrentPath = pszPointer + 1;
			}
			else
			{
				break;
			}
		}

		free( pszPaths );
	}

	return status;
}

#pragma endregion

#pragma region API

PE_STATUS peOpenFile( PE *pe, const char *pszFileName )
{
	PE_STATUS status = PE_SUCCESS;

	ZeroMemory( pe, sizeof(PE) );

	strncpy_s( pe->szFileName, pszFileName, MAX_PATH );

	pe->hFile = CreateFile
    (
        pszFileName,
        GENERIC_READ,
        FILE_SHARE_READ | FILE_SHARE_DELETE,
        NULL,
        OPEN_EXISTING,
        FILE_ATTRIBUTE_NORMAL,
        NULL
    );

    if( pe->hFile == INVALID_HANDLE_VALUE )
    {
        status = PE_OPEN_FAILED;
		goto done;
    }

	pe->dwFileSize = GetFileSize( pe->hFile, NULL );

    pe->hMap = CreateFileMapping( pe->hFile, NULL, PAGE_READONLY, 0, 0, NULL );
    if( pe->hMap == NULL )
    {
        status = PE_MMAP_FAILED;
		goto done;
    }

	pe->pData = (uint8_t *)MapViewOfFile( pe->hMap, FILE_MAP_READ, 0, 0, 0 );
    if( pe->pData == NULL )
    {
        status = PE_MMAP_FAILED;
		goto done;
    }

    if( pe->dwFileSize < 10 || ( pe->pData[0] != 'M' || pe->pData[1] != 'Z' ) )
    {
		status = PE_NOT_PE;
		goto done;
    }

	status = peParseBuffer( pe );

done:

	if( status != PE_SUCCESS )
	{
		peClose(pe);
	}

	return status;
}

PE_STATUS peOpenBuffer( PE *pe, uint8_t * pData, uint32_t dwSize )
{
	PE_STATUS status = PE_SUCCESS;

	ZeroMemory( pe, sizeof(PE) );

	sprintf_s( pe->szFileName, "[%08X]", pData );

	if( dwSize < 10 || ( pData[0] != 'M' || pData[1] != 'Z' ) )
    {
		status = PE_NOT_PE;
		goto done;
    }

	pe->pData      = pData;
	pe->dwFileSize = dwSize;

	status = peParseBuffer( pe );

done:

	if( status != PE_SUCCESS )
	{
		peClose(pe);
	}

	return status;
}

BOOL peResolveVirtualAddress( PE *pe, uint64_t qwVirtualAddress, PE_ADDRESS *pAddress )
{
	PE_ADDRESS_FROM_VA
	(
		pe,
		*pAddress,
		qwVirtualAddress
	);

	return PE_IS_VALID_ADDRESS( *pAddress );
}

BOOL peResolveSectionAddress( PE *pe, PIMAGE_SECTION_HEADER pSection, PE_ADDRESS *pAddress )
{
	return peResolveVirtualAddress( pe, pe->qwBaseAddress + pSection->VirtualAddress, pAddress );
}

PIMAGE_SECTION_HEADER peGetSectionByName( PE *pe, const char *pszName )
{
	if( PE_IS_PARSED( pe, SECTIONS ) == TRUE )
	{
		PE_FOREACH_SECTION( pe, pSection )
		{
			if( strcmp( (const char *)pSection->Name, pszName ) == 0 )
			{
				return pSection;
			}
		}
	}

	return NULL;
}

PIMAGE_SECTION_HEADER peGetSectionByAddress( PE *pe, uint64_t qwVirtualAddress )
{
	if( PE_IS_PARSED( pe, SECTIONS ) == TRUE )
	{
		return peSectionByVA( pe, qwVirtualAddress );
	}

	return NULL;
}

PE_STATUS peParseExportTable( PE *pe, uint32_t dwMaxExports, uint32_t dwOptions /* = PE_EXPORT_OPT_DEFAULT */ )
{
	PE_STATUS status = PE_SUCCESS;

	if( PE_IS_PARSED( pe, EXPORTS ) == FALSE )
	{
		uint32_t dwExportRVA  = PE_HEADERS_OPT_FIELD( pe, DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].VirtualAddress ),
			     dwExportSize = PE_HEADERS_OPT_FIELD( pe, DataDirectory[IMAGE_DIRECTORY_ENTRY_EXPORT].Size );

		if( dwExportRVA != 0 && dwExportSize != 0 )
		{
			PE_ADDRESS_FROM_VA
			(
				pe,
				pe->ExportTable.Address,
				pe->qwBaseAddress + dwExportRVA
			);

			if( PE_IS_VALID_ADDRESS( pe->ExportTable.Address ) )
			{
				pe->ExportTable.Address.Size = (uint64_t)dwExportSize;

				PIMAGE_EXPORT_DIRECTORY pExportDirectory = (PIMAGE_EXPORT_DIRECTORY)pe->ExportTable.Address.Data;

				uint32_t *pdwFunctions,
						 *pdwFunctionNames;
				uint16_t *pwOrdinals;
				uint32_t  dwCurrent = 0;

				pdwFunctions     = (uint32_t *)PE_GET_POINTER( pe, pExportDirectory->AddressOfFunctions );
				pwOrdinals       = (uint16_t *)PE_GET_POINTER( pe, pExportDirectory->AddressOfNameOrdinals );
				pdwFunctionNames = (uint32_t *)PE_GET_POINTER( pe, pExportDirectory->AddressOfNames );

				ll_init( &pe->ExportTable.Symbols );

				pe->ExportTable.ByAddress = HT_CREATE_BY_QWORD();
				pe->ExportTable.ByOrdinal = HT_CREATE_BY_WORD();
				pe->ExportTable.ByName	  = HT_CREATE_BY_ISTRING();

#define SHOULD_APPEND_SYMBOL( SYM ) \
	( PE_IS_VALID_ADDRESS( (SYM)->Address ) && ht_get( pe->ExportTable.ByAddress, (void *)(SYM)->Address.VA ) == NULL )

#define APPEND_SYMBOL( SYM ) \
	if( (SYM)->Name[0] != 0x00 ){ \
		ht_add( pe->ExportTable.ByName, (SYM)->Name, (SYM) ); \
	} \
	ll_append( &pe->ExportTable.Symbols, (SYM) ); \
	ht_add( pe->ExportTable.ByAddress, (void *)(SYM)->Address.VA, (SYM) ); \
	ht_add( pe->ExportTable.ByOrdinal, (void *)(SYM)->Ordinal, (SYM) )

				dwMaxExports = min( dwMaxExports, pExportDirectory->NumberOfNames + pExportDirectory->NumberOfFunctions );

#pragma region Loop by Name

				PE_SYMBOL *pSymbol = NULL;

				for( uint32_t i = 0; i < dwMaxExports; ++i )
				{
					pSymbol = (PE_SYMBOL *)calloc( 1, sizeof(PE_SYMBOL) );

					__try
					{
						PE_ADDRESS_FROM_VA
						(
							pe,
							pSymbol->Address,
							pe->qwBaseAddress + pdwFunctions[ pwOrdinals[ i ] ]
						);

						if( SHOULD_APPEND_SYMBOL( pSymbol ) )
						{
							uint64_t qwNameRaw = peRawOffsetByVA( pe, pe->qwBaseAddress + pdwFunctionNames[ i ] );

							if( qwNameRaw != PE_INVALID_OFFSET )
							{
								peCopyString( pSymbol->Name, (char *)pe->pData + qwNameRaw, 0xFE );
							}

							pSymbol->Ordinal = pwOrdinals[i] + 1;

							APPEND_SYMBOL( pSymbol );
						}
						else
						{
							free( pSymbol );
						}
					}
					__except(EXCEPTION_EXECUTE_HANDLER)
					{
						free( pSymbol );
					}
				}

#pragma endregion

#pragma region Loop by Ordinal

				uint32_t dwMaxOrdinals = min( pExportDirectory->NumberOfFunctions, dwMaxExports );

				for( uint32_t i = 0; i < dwMaxOrdinals; i++ )
				{
					pSymbol = (PE_SYMBOL *)calloc( 1, sizeof(PE_SYMBOL) );

					__try
					{
						PE_ADDRESS_FROM_VA
						(
							pe,
							pSymbol->Address,
							pe->qwBaseAddress + pdwFunctions[ i ]
						);

						if( SHOULD_APPEND_SYMBOL( pSymbol ) )
						{
							pSymbol->Ordinal = (uint16_t)( pExportDirectory->Base + i );

							APPEND_SYMBOL( pSymbol );
						}
						else
						{
							free( pSymbol );
						}
					}
					__except(EXCEPTION_EXECUTE_HANDLER)
					{
						free( pSymbol );
					}

					if( pe->ExportTable.Symbols.elements == dwMaxExports )
					{
						break;
					}
				}

#pragma endregion

#undef APPEND_SYMBOL
#undef SHOULD_APPEND_SYMBOL

				if( PE_MASK_HAS_BIT( dwOptions, PE_EXPORT_OPT_DEMANGLE_NAMES ) )
				{
					PE_FOREACH_EXPORTED_SYMBOL( pe, pSymbol )
					{
						if( pSymbol->Name[0] != 0 )
						{
							char szUndecoratedBuffer[0xFF] = {0};

							if( UnDecorateSymbolName( pSymbol->Name, szUndecoratedBuffer, 0xFE, UNDNAME_COMPLETE ) )
							{
								strncpy_s( pSymbol->Name, szUndecoratedBuffer, 0xFE );
							}
						}
					}
				}
			}
			else
			{
				status = PE_DIRECTORY_NOT_FOUND;
			}
		}

		PE_SET_PARSED( pe, EXPORTS );
	}

	return status;
}

PE_STATUS peGetExportedSymbolByName( PE *pe, const char *pszName, PE_SYMBOL **ppSymbol )
{
	PE_STATUS status = PE_NOT_PARSED;

	*ppSymbol = NULL;

	if( PE_IS_PARSED( pe, EXPORTS ) == TRUE )
	{
		*ppSymbol = (PE_SYMBOL *)ht_get( pe->ExportTable.ByName, (void *)pszName  );

		status = *ppSymbol == NULL ? PE_NOT_FOUND : PE_SUCCESS;
	}

	return status;
}

PE_STATUS peGetExportedSymbolByAddress( PE *pe, uint64_t qwAddress, PE_SYMBOL **ppSymbol )
{
	PE_STATUS status = PE_NOT_PARSED;

	*ppSymbol = NULL;

	if( PE_IS_PARSED( pe, EXPORTS ) == TRUE )
	{
		*ppSymbol = (PE_SYMBOL *)ht_get( pe->ExportTable.ByAddress, (void *)qwAddress  );

		status = *ppSymbol == NULL ? PE_NOT_FOUND : PE_SUCCESS;
	}

	return status;
}

PE_STATUS peGetExportedSymbolByOrdinal( PE *pe, uint16_t wOrdinal, PE_SYMBOL **ppSymbol )
{
	PE_STATUS status = PE_NOT_PARSED;

	*ppSymbol = NULL;

	if( PE_IS_PARSED( pe, EXPORTS ) == TRUE )
	{
		*ppSymbol = (PE_SYMBOL *)ht_get( pe->ExportTable.ByOrdinal, (void *)wOrdinal );

		status = *ppSymbol == NULL ? PE_NOT_FOUND : PE_SUCCESS;
	}

	return status;
}

PE_STATUS peParseImportTable( PE *pe, uint32_t dwOptions /* = PE_IMPORT_OPT_DEFAULT */ )
{
	PE_STATUS status = PE_SUCCESS;

	if( PE_IS_PARSED( pe, IMPORTS ) == FALSE )
	{
		uint32_t dwImportRVA  = PE_HEADERS_OPT_FIELD( pe, DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].VirtualAddress ),
			  dwImportSize = PE_HEADERS_OPT_FIELD( pe, DataDirectory[IMAGE_DIRECTORY_ENTRY_IMPORT].Size );

		if( dwImportRVA != 0 && dwImportSize != 0 )
		{
			PE_ADDRESS_FROM_VA
			(
				pe,
				pe->ImportTable.Address,
				pe->qwBaseAddress + dwImportRVA
			);

			if( PE_IS_VALID_ADDRESS( pe->ImportTable.Address ) )
			{
				pe->ImportTable.Address.Size = (uint64_t)dwImportSize;

				PIMAGE_IMPORT_DESCRIPTOR pDescriptor = (PIMAGE_IMPORT_DESCRIPTOR)pe->ImportTable.Address.Data,
										 pImport = NULL;
				uint32_t dwImportCount = 0,
					  dwThunkRVA;
					 // dwThunkRaw,
					 // dwRealThunkRaw;

				uint64_t qwRealThunkRaw,
					     qwThunkRaw;

				ll_init( &pe->ImportTable.Modules );

				pe->ImportTable.ByName = HT_CREATE_BY_ISTRING();

				for( pImport = pDescriptor, dwImportCount = 0; dwImportCount <= 96; ++pImport, ++dwImportCount )
				{
					if( peIsValidImportDescriptor( pImport ) == false )
					{
						break;
					}

					dwThunkRVA = (uint32_t)( pe->qwBaseAddress + pImport->FirstThunk );
					qwThunkRaw = peRawOffsetByVA( pe, dwThunkRVA );
					
					if( qwThunkRaw == PE_INVALID_OFFSET )
						continue;

					char *pszDllName = (char *)PE_GET_POINTER( pe, pImport->Name ),
						 *pszSymbolName = NULL;

					if( IsBadReadPtr( pszDllName, 1 ) )
						continue;

					PE_IMPORT_MODULE *pModule = (PE_IMPORT_MODULE *)ht_get( pe->ImportTable.ByName, (void *)pszDllName );

					if( pModule == NULL )
					{
						pModule = (PE_IMPORT_MODULE *)calloc( 1, sizeof(PE_IMPORT_MODULE) );

						PE_INIT_MODULE( pModule, pszDllName );

						PE_ADD_IMPORTED_MODULE( pe, pModule );
					}

					if( pImport->Characteristics == 0 )
						/* Borland compilers don't produce Hint Table */
						qwRealThunkRaw = qwThunkRaw;
					else
						/* Hint Table */
						qwRealThunkRaw = peRawOffsetByVA( pe, pe->qwBaseAddress + pImport->Characteristics );
					
					if( qwRealThunkRaw == PE_INVALID_OFFSET )
						continue;

					uint32_t dwThunkCount = 0;

#pragma region PE32


					if( pe->Headers.Plus == FALSE )
					{
						PIMAGE_THUNK_DATA32 pAddressThunk;
						PIMAGE_THUNK_DATA32 pNameThunk;

						pAddressThunk = (PIMAGE_THUNK_DATA32)( pe->pData + qwThunkRaw );
						pNameThunk    = (PIMAGE_THUNK_DATA32)( pe->pData + (uint32_t)qwRealThunkRaw );

						while( pNameThunk && pNameThunk->u1.AddressOfData )
						{
							uint64_t qwNameRaw;
							PIMAGE_IMPORT_BY_NAME pImportByName;
							PE_SYMBOL *pSymbol = (PE_SYMBOL *)calloc( 1, sizeof(PE_SYMBOL) );

							pSymbol->Address.VA = dwThunkRVA + sizeof(IMAGE_THUNK_DATA32) * dwThunkCount++;

							if( pNameThunk->u1.Ordinal & IMAGE_ORDINAL_FLAG )
							{
								pSymbol->Ordinal = IMAGE_ORDINAL32(pNameThunk->u1.Ordinal);
							}

							qwNameRaw = peRawOffsetByVA( pe, pe->qwBaseAddress + pNameThunk->u1.AddressOfData );

							if( qwNameRaw != PE_INVALID_OFFSET )
							{
								pImportByName = (PIMAGE_IMPORT_BY_NAME)( pe->pData + qwNameRaw );
								pszSymbolName = (char *)&pImportByName->Name[0];

								peCopyString( pSymbol->Name, pszSymbolName, 0xFE );

								ht_add( pModule->ByName, (void *)pSymbol->Name, pSymbol );
							}

							ll_append( &pModule->Symbols, pSymbol );

							++pNameThunk;
							++pAddressThunk;
						}
					}
#pragma endregion

#pragma region PE32+
					else
					{
						PIMAGE_THUNK_DATA64 pAddressThunk;
						PIMAGE_THUNK_DATA64 pNameThunk;

						pAddressThunk = (PIMAGE_THUNK_DATA64)( pe->pData + qwThunkRaw );
						pNameThunk    = (PIMAGE_THUNK_DATA64)( pe->pData + qwRealThunkRaw );

						while( pNameThunk && pNameThunk->u1.AddressOfData )
						{
							uint64_t qwNameRaw;
							PIMAGE_IMPORT_BY_NAME pImportByName;
							PE_SYMBOL *pSymbol = (PE_SYMBOL *)calloc( 1, sizeof(PE_SYMBOL) );

							pSymbol->Address.VA = dwThunkRVA + sizeof(PIMAGE_THUNK_DATA64) * dwThunkCount++;

							if( pNameThunk->u1.Ordinal & IMAGE_ORDINAL_FLAG )
							{
								pSymbol->Ordinal = IMAGE_ORDINAL64(pNameThunk->u1.Ordinal);
							}

							qwNameRaw = peRawOffsetByVA( pe, pe->qwBaseAddress + pNameThunk->u1.AddressOfData );

							if( qwNameRaw != PE_INVALID_OFFSET )
							{
								pImportByName = (PIMAGE_IMPORT_BY_NAME)( pe->pData + qwNameRaw );
								pszSymbolName = (char *)&pImportByName->Name[0];

								peCopyString( pSymbol->Name, pszSymbolName, 0xFE );

								ht_add( pModule->ByName, (void *)pSymbol->Name, pSymbol );
							}

							ll_append( &pModule->Symbols, pSymbol );

							++pNameThunk;
							++pAddressThunk;
						}
					}
#pragma endregion
				}
			}
			else
			{
				status = PE_DIRECTORY_NOT_FOUND;
			}

#pragma region Options

			if( PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_RESOLVE_ORDINALS ) ||
				PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_RENAME_ORDINALS )  ||
				PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_DEMANGLE_NAMES ) )
			{
				PE_FOREACH_IMPORTED_MODULE( pe, pModule )
				{
					PE peModule = {0};
					char szModuleName[MAX_PATH + 1] = {0};

					PE_FOREACH_MODULE_SYMBOL( pModule, pSymbol )
					{
						PE_SYMBOL *pResolved = NULL;

						if( pSymbol->Name[0] == 0 )
						{
							if( PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_RESOLVE_ORDINALS ) )
							{
								if( peModule.dwParseState == PE_NONE_PARSED )
								{
									if( peLocateModule( pe, pModule, &peModule ) != ERROR_SUCCESS ||
										peParseExportTable( &peModule, 0xFFFF ) != ERROR_SUCCESS )
									{
										goto next_module;
									}
								}

								if( peGetExportedSymbolByOrdinal( &peModule, pSymbol->Ordinal, &pResolved ) == ERROR_SUCCESS )
								{
									strncpy_s( pSymbol->Name, pResolved->Name, 0xFE );
								}
							}
							else if( PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_RENAME_ORDINALS ) )
							{
								if( szModuleName[0] == 0 )
								{
									strncpy_s( szModuleName, pModule->Name, MAX_PATH );
									char *p = strrchr( szModuleName, '.' );
									if( p )
										*p = '\0';
								}

								sprintf_s( pSymbol->Name, "%s@%u", szModuleName, pSymbol->Ordinal );
							}
						}

						if( PE_MASK_HAS_BIT( dwOptions, PE_IMPORT_OPT_DEMANGLE_NAMES ) )
						{
							if( pSymbol->Name[0] != 0 )
							{
								char szUndecoratedBuffer[0xFF] = {0};

								if( UnDecorateSymbolName( pSymbol->Name, szUndecoratedBuffer, 0xFE, UNDNAME_COMPLETE ) )
								{
									strncpy_s( pSymbol->Name, szUndecoratedBuffer, 0xFE );
								}
							}
						}
					}

next_module:

					peClose( &peModule );
				}
			}

#pragma endregion

		}

		PE_SET_PARSED( pe, IMPORTS );
	}

	return status;
}

PE_STATUS peGetImportedModuleByName( PE *pe, const char *pszName, PE_IMPORT_MODULE **ppModule )
{
	PE_STATUS status = PE_NOT_PARSED;

	if( PE_IS_PARSED( pe, IMPORTS ) == TRUE )
	{
		if( ppModule != NULL )
		{
			*ppModule = (PE_IMPORT_MODULE *)ht_get( pe->ImportTable.ByName, (void *)pszName  );

			status = *ppModule == NULL ? PE_NOT_FOUND : PE_SUCCESS;
		}
		else
		{
			status = ht_get( pe->ImportTable.ByName, (void *)pszName  ) == NULL ? PE_NOT_FOUND : PE_SUCCESS;
		}
	}
	else if( ppModule != NULL )
	{
		*ppModule = NULL;
	}

	return status;
}

PE_STATUS peGetImportedSymbolByName( PE_IMPORT_MODULE *pModule, const char *pszName, PE_SYMBOL **ppSymbol )
{
	if( ppSymbol )
	{
		*ppSymbol = (PE_SYMBOL *)ht_get( pModule->ByName, (void *)pszName );

		return *ppSymbol == NULL ? PE_NOT_FOUND : PE_SUCCESS;
	}
	else
	{
		return ht_get( pModule->ByName, (void *)pszName ) == NULL ? PE_NOT_FOUND : PE_SUCCESS;
	}
}

uint32_t peExtractStrings( PE *pe, uint32_t dwMinLength, bool bFullEncoding )
{
	if( PE_IS_PARSED( pe, STRINGS ) == FALSE )
	{
		pe->Strings.AsciiTable   = HT_CREATE_BY_STRING();
		pe->Strings.UnicodeTable = HT_CREATE_BY_WSTRING();

		ll_init( &pe->Strings.List );

		peExtractStrings<char>( pe, dwMinLength, Ascii, (PE_STRING_DUP)_strdup, pe->Strings.AsciiTable );

		if( bFullEncoding )
		{
			peExtractStrings<wchar_t>( pe, dwMinLength, Unicode, (PE_STRING_DUP)_wcsdup, pe->Strings.UnicodeTable );
		}

		PE_SET_PARSED( pe, STRINGS );
	}

	return pe->Strings.List.elements;
}

void peClose( PE *pe )
{
	if( pe->hFile && pe->hFile != INVALID_HANDLE_VALUE )
    {
        CloseHandle( pe->hFile );
    }

	if( pe->pData != NULL && pe->hMap != NULL )
    {
		UnmapViewOfFile( pe->pData );
    }

    if( pe->hMap != NULL )
    {
        CloseHandle( pe->hMap );
    }

	// free the export table
	if( pe->ExportTable.ByAddress )
	{
		ht_destroy( pe->ExportTable.ByAddress );
	}

	if( pe->ExportTable.ByName )
	{
		ht_destroy( pe->ExportTable.ByName );
	}

	if( pe->ExportTable.ByOrdinal )
	{
		ht_destroy( pe->ExportTable.ByOrdinal );
	}

	if( pe->ExportTable.Symbols.elements )
	{
		ll_destroy( &pe->ExportTable.Symbols, free );
	}

	// free the import table
	if( pe->ImportTable.ByName )
	{
		ht_destroy( pe->ImportTable.ByName );
	}

	if( pe->ImportTable.Modules.elements )
	{
		PE_FOREACH_IMPORTED_MODULE( pe, pModule )
		{
			ll_destroy( &pModule->Symbols, free );
			ht_destroy( pModule->ByName );
		}

		ll_destroy( &pe->ImportTable.Modules, free );
	}

	// free strings
	if( pe->Strings.List.elements )
	{
		ll_foreach_data( &pe->Strings.List, lli, PE_STRING, pString )
		{
			if( pString )
			{
				if( pString->Data != NULL )
				{
					free( pString->Data );
				}

				free( pString );
			}
		}

		ll_destroy( &pe->Strings.List, NULL );

		if( pe->Strings.AsciiTable )
		{
			ht_destroy( pe->Strings.AsciiTable );
		}

		if( pe->Strings.UnicodeTable )
		{
			ht_destroy( pe->Strings.UnicodeTable );
		}
	}

	ZeroMemory( pe, sizeof(PE) );
}

#pragma endregion