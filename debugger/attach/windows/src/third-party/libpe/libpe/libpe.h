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
#pragma once

#include <Windows.h>
#include "llist.h"
#include "htable.h"

//! Initial state, nothing was parsed.
#define PE_NONE_PARSED     0x00000000
//! Section headers were parsed.
#define PE_SECTIONS_PARSED (1 << 0)
//! Entry point address was parsed.
#define PE_ENTRY_PARSED	   (1 << 1)
//! Export directory and symbols were parsed.
#define PE_EXPORTS_PARSED  (1 << 2)
//! Import directory was parsed.
#define PE_IMPORTS_PARSED  (1 << 3)
//! Unicode and Ascii strings were extracted.
#define PE_STRINGS_PARSED  (1 << 4)

//! No extra action, parse import table as it is.
#define PE_IMPORT_OPT_DEFAULT          0x00000000
//! Resolve imports by ordinal into imports by name.
#define PE_IMPORT_OPT_RESOLVE_ORDINALS (1 << 0)
//! Rename imports by ordinal into "library.dll@ordinal"
#define PE_IMPORT_OPT_RENAME_ORDINALS  (1 << 1)
//! Demangle imported names
#define PE_IMPORT_OPT_DEMANGLE_NAMES   (1 << 2)

//! No extra action, parse export table as it is.
#define PE_EXPORT_OPT_DEFAULT          0x00000000
//! Demangle exported names.
#define PE_EXPORT_OPT_DEMANGLE_NAMES   (1 << 0)

//! Macro which check if a PE_SYMBOL pointer has the Name field filled.
#define PE_SYMBOL_HAS_NAME( SYM ) \
	( (SYM)->Name[0] != 0 )

//! Macro to check if the PE has the given table or not.
#define PE_HAS_TABLE( PE, TABLE ) \
	( (PE)->TABLE.Address.VA != 0 )

//! Macro to easily loop PE sections.
#define PE_FOREACH_SECTION( PE, SEC_VAR_NAME ) \
	PIMAGE_SECTION_HEADER SEC_VAR_NAME = NULL; \
	uint32_t __peForeachSectionIndex = 0; \
	for( __peForeachSectionIndex = 0, SEC_VAR_NAME = &(PE)->Sections.pHeaders[0]; \
		 __peForeachSectionIndex < (PE)->Sections.dwNumber && ( SEC_VAR_NAME = &(PE)->Sections.pHeaders[__peForeachSectionIndex] ); \
		 ++__peForeachSectionIndex )

//! Macro to easily loop exported symbols inside a PE.
#define PE_FOREACH_EXPORTED_SYMBOL( PE, SYM_VAR_NAME ) \
	ll_foreach_data( &(PE)->ExportTable.Symbols, ll_symbol, PE_SYMBOL, SYM_VAR_NAME )

//! Macro to easily loop imported modules of a PE.
#define PE_FOREACH_IMPORTED_MODULE( PE, MOD_VAR_NAME ) \
	ll_foreach_data( &(PE)->ImportTable.Modules, ll_module, PE_IMPORT_MODULE, MOD_VAR_NAME )

//! Macro to easily loop symbols inside a PE_IMPORT_MODULE structure pointer.
#define PE_FOREACH_MODULE_SYMBOL( MODULE, SYM_VAR_NAME ) \
	ll_foreach_data( &(MODULE)->Symbols, ll_mod_symbol, PE_SYMBOL, SYM_VAR_NAME )

//! Macro to easily loop strings.
#define PE_FOREACH_STRING( PE, STR_VAR_NAME ) \
	ll_foreach_data( &(PE)->Strings.List, lli, PE_STRING, STR_VAR_NAME )

/*
 * Return codes enumeration.
 */
typedef enum
{
	//! Operation succesfully completed.
	PE_SUCCESS,
	//! Unexpected end of file while parsing it.
	PE_UNEXPECTED_EOF,
	//! The file is not a PE32 or PE32+ file.
	PE_NOT_PE,
	//! File or object not found.
	PE_NOT_FOUND,
	//! Failed to open file.
	PE_OPEN_FAILED,
	//! Failed to memory map the file.
	PE_MMAP_FAILED,
	//! Error while parsing the specified directory, not found.
	PE_DIRECTORY_NOT_FOUND,
	//! A specific part of the PE should be parsed before this operation can be executed.
	PE_NOT_PARSED
}
PE_STATUS;

/*
 * A structure representing an address inside the PE.
 */
typedef struct
{
	//! Virtual address ( base + RVA )
	uint64_t VA;
	//! Absolute raw offset inside the file.
	uint64_t Offset;
	//! Pointer to data inside the file.
	uint8_t *Data;
	//! Size of the area this address points to if applicable.
	uint64_t Size;
}
PE_ADDRESS;

/*
 * Section headers container structure.
 */
typedef struct
{
	//! Pointer to the first section header.
	PIMAGE_SECTION_HEADER pHeaders;
	//! Number of section headers.
	uint32_t			  dwNumber;
}
PE_SECTIONS;

/*
 * API container structure ( used both for imports and exports ).
 */
typedef struct
{
	//! Name of the symbol.
	char	   Name[0xFF];
	//! Ordinal number.
	uint16_t   Ordinal;
	//! Address of the symbol.
	PE_ADDRESS Address;
}
PE_SYMBOL;

/*
 * PE export table container structure.
 */
typedef struct
{
	//! Address of the export table.
	PE_ADDRESS	Address;
	//! List of exported symbols.
	ll_t	    Symbols;
	//! Lookup by virtual address table.
	ht_t	   *ByAddress;
	//! Lookup by name table.
	ht_t	   *ByName;
	//! Lookup by ordinal table.
	ht_t	   *ByOrdinal;
}
PE_EXPORT_TABLE;

/*
 * Imported module container structure.
 */
typedef struct
{
	//! Name of the module.
	char  Name[MAX_PATH + 1];
	//! List of symbols imported from this module.
	ll_t  Symbols;
	//! Lookup by name table.
	ht_t *ByName;
}
PE_IMPORT_MODULE;

/*
 * PE import table container structure.
 */
typedef struct
{
	//! Address of the import table.
	PE_ADDRESS Address;
	//! List of imported modules.
	ll_t	   Modules;
	//! Lookup by name table.
	ht_t	  *ByName;
}
PE_IMPORT_TABLE;

/*
 * Main headers container structure.
 */
typedef struct
{
	//! TRUE if PE32+, else FALSE:
	BOOL			  Plus;
	//! NT hader.
	PIMAGE_NT_HEADERS NT;
	//! DOS header.
    PIMAGE_DOS_HEADER DOS;
	//! Optional header PE32/PE32+ union.
	union 
	{
		PIMAGE_OPTIONAL_HEADER32 p32;
		PIMAGE_OPTIONAL_HEADER64 p64;
	}
	OPT;
}
PE_HEADERS;

/*
 * String encoding.
 */
typedef enum
{
	Ascii,
	Unicode
}
PE_STRING_ENCODING;

/*
 * Structure representing a string.
 */
typedef struct
{
	//! Buffer of the string.
	uint8_t *Data;
	//! Length in bytes of the string ( > CharLength for Unicode ).
	uint32_t ByteLength;
	//! Length in characters of the string.
	uint32_t CharLength;
	//! Absolute raw offset inside the file.
	uint32_t Offset;
	//! Encoding of the string.
	PE_STRING_ENCODING Encoding;
}
PE_STRING;

/*
 * Container structure of strings extracted from the file.
 */
typedef struct
{
	//! Fast Ascii encoded string lookup table.
	ht_t *AsciiTable;
	//! Fast Unicode encoded string lookup table.
	ht_t *UnicodeTable;
	//! Global list of strings.
	ll_t List;
}
PE_STRINGS;

/*
 * PE main container structure.
 */
typedef struct 
{
	//! Current parsing state of this PE, a mask of PE_*_PARSED flags.
	uint32_t dwParseState;
	//! File name of the PE or "[ADDRESS]" if peOpenMemory was used.
	char szFileName[MAX_PATH + 1];
	//! Size of the file/buffer.
	uint32_t dwFileSize;
	//! Handle to the file, NULL if peOpenMemory was used.
	HANDLE hFile;
	//! Handle to the memory map, NULL if peOpenMemory was used.
	HANDLE hMap;
	//! Memory buffer of the file.
	uint8_t *pData;
	//! Main headers.
	PE_HEADERS Headers;
    //! Image base address.
	uint64_t qwBaseAddress;
	//! Image size in memory ( might be different to dwFileSize due to alignment ).
	uint32_t dwImageSize;
	//! PE section headers.
	PE_SECTIONS Sections;
	//! Entry point address.
	PE_ADDRESS EntryPoint;
	//! Export table.
	PE_EXPORT_TABLE ExportTable;
	//! Import table.
	PE_IMPORT_TABLE ImportTable;
	//! Strings
	PE_STRINGS Strings;
}
PE;

extern "C"
{

//! Open a file and parse its basic structure.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param pszFileName name of the file to be parsed.
//!
//! @return PE_SUCCESS if the file was succesfully recognized and parsed as a PE,
//!			otherwise one of the PE_STATUS enumeration values.
//!
//! @remarks This function will set the PE_SECTIONS_PARSED and PE_ENTRY_PARSED parse 
//!			 status flags.
PE_STATUS peOpenFile( PE *pe, const char *pszFileName );

//! Parse a memory buffer.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param pData a pointer to the buffer to be parsed.
//! @param dwSize size of the buffer.
//!
//! @return PE_SUCCESS if the buffer was succesfully recognized and parsed as a PE,
//!			otherwise one of the PE_STATUS enumeration values.
//!
//! @remarks This function will set the PE_SECTIONS_PARSED and PE_ENTRY_PARSED parse 
//!			 status flags.
PE_STATUS peOpenBuffer( PE *pe, uint8_t * pData, uint32_t dwSize );

//! Translate a virtual address inside the PE into a PE_ADDRESS structure.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param qwVirtualAddress the virtual address to translate, it must be absolute ( base + RVA ).
//! @param pAddress the PE_ADDRESS structure pointer that will be filled.
//!
//! @return TRUE if the translation was successfull, otherwise FALSE. 
BOOL peResolveVirtualAddress( PE *pe, uint64_t qwVirtualAddress, PE_ADDRESS *pAddress );

//! Get the PE_ADDRESS structure of the given section.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param pSection the section header to be resolved.
//! @param pAddress the PE_ADDRESS structure pointer that will be filled.
//!
//! @return TRUE if the translation was successfull, otherwise FALSE. 
BOOL peResolveSectionAddress( PE *pe, PIMAGE_SECTION_HEADER pSection, PE_ADDRESS *pAddress );

//! Get a section header pointer given the section name.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param pszName the name of the section to search.
//!
//! @return A pointer to the section header if the section was found, otherwise NULL.
PIMAGE_SECTION_HEADER peGetSectionByName( PE *pe, const char *pszName );

//! Get pointer to the section header containing the given address.
//!
//! @param pe pointer to a PE structure to be filled with file data.
//! @param qwVirtualAddress the virtual address to search inside sections, it must be absolute ( base + RVA )..
//!
//! @return A pointer to the section header if the address was found inside a valid section, otherwise NULL.
PIMAGE_SECTION_HEADER peGetSectionByAddress( PE *pe, uint64_t qwVirtualAddress );

//! Parse the export table of the PE.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param dwMaxExports maximum number of exported symbols to parse.
//! @param dwOptions optional options mask composed by PE_EXPORT_OPT_* flags.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
//!
//! @remarks This function will set the PE_EXPORTS_PARSED parse status flags.
PE_STATUS peParseExportTable( PE *pe, uint32_t dwMaxExports, uint32_t dwOptions = PE_EXPORT_OPT_DEFAULT );

//! Search an exported symbol given its name.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param pszName name of the symbol to search.
//! @param ppSymbol pointer to a pointer that will be set to the symbol found.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
PE_STATUS peGetExportedSymbolByName( PE *pe, const char *pszName, PE_SYMBOL **ppSymbol );

//! Search an exported symbol given its virtual address.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param dwAddress address of the symbol to search.
//! @param ppSymbol pointer to a pointer that will be set to the symbol found.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
//!
//! @remarks The address must be in its absolute form ( base + rva ).
PE_STATUS peGetExportedSymbolByAddress( PE *pe, uint64_t qwAddress, PE_SYMBOL **ppSymbol );

//! Search an exported symbol given its ordinal.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param wOrdinal ordinal of the symbol to search.
//! @param ppSymbol pointer to a pointer that will be set to the symbol found.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
PE_STATUS peGetExportedSymbolByOrdinal( PE *pe, uint16_t wOrdinal, PE_SYMBOL **ppSymbol );

//! Parse the import table of the PE.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param dwOptions optional options mask composed by PE_IMPORT_OPT_* flags.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
//!
//! @remarks This function will set the PE_IMPORTS_PARSED parse status flags.
PE_STATUS peParseImportTable( PE *pe, uint32_t dwOptions = PE_IMPORT_OPT_DEFAULT );

//! Search an imported module given its name.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param pszName name of the module to search.
//! @param ppModule pointer to a pointer that will be set to the module found.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
PE_STATUS peGetImportedModuleByName( PE *pe, const char *pszName, PE_IMPORT_MODULE **ppModule );

//! Search an imported symbol given its name.
//!
//! @param pModule the imported module to search the symbol in.
//! @param pszName name of the symbol to search.
//! @param ppSymbol pointer to a pointer that will be set to the symbol found.
//!
//! @return PE_SUCCESS on success or one of the PE_STATUS enumeration values.
PE_STATUS peGetImportedSymbolByName( PE_IMPORT_MODULE *pModule, const char *pszName, PE_SYMBOL **ppSymbol );

//! Extract printable strings from the PE file.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
//! @param dwMinLength minimum length of a string to be extracted.
//! @param bFullEncoding set to true to extract both Ascii and Unicode strings, 
//!						 false to extract only Ascii strings.
//!
//! @return The total number of succesfully extracted strings.
uint32_t peExtractStrings( PE *pe, uint32_t dwMinLength, bool bFullEncoding );

//! Close handles and free resources of the PE structure.
//!
//! @param pe pointer to a PE structure initialized with peOpen(File|Buffer).
void peClose( PE *pe );

}