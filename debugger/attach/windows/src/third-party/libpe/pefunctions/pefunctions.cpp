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
#include <libpe.h>
#include <stdio.h>
#include "distorm\distorm.h"
#include "distorm\mnemonics.h"

// Disasm blocks of 15 instructions each
#define MAX_INSTRUCTIONS 15

// Callback called for each disassembled instruction.
typedef bool (*peDisasmCallback)( _DInst *inst, void *data );

// This structure will contain each sub routine data.
typedef struct
{
	char				  Name[0xFF];
	PE_ADDRESS			  Address;
	PIMAGE_SECTION_HEADER Section;
}
function_t;

// Helper structure to be used as disasm callback context data.
typedef struct
{
	PE		   *pe;
	ll_t	   *llFunctions;
	ht_t	   *htFunctions;
	function_t *pFunction;
}
dasm_callback_data_t;

// Disasm given data at the given address and call the callback for each instruction.
void peDisasm( uint64_t qwAddress, BYTE *pData, uint64_t qwSize, _DecodeType decode, peDisasmCallback callback, void *data )
{
	_DecodeResult res;
	_DInst decodedInstructions[MAX_INSTRUCTIONS];
	unsigned int decodedInstructionsCount = 0, i, next;
	_OffsetType offset = 0;

	unsigned char *buf = pData;
	int len = qwSize;

	while( len > 0 ) 
	{
		_CodeInfo code = {0};

		code.codeOffset = qwAddress + offset;
		code.code		= buf;
		code.codeLen    = len;
		code.dt			= decode;
		code.features   = DF_MAXIMUM_ADDR32;

		int totsize = 0;

		res = distorm_decompose64( &code, decodedInstructions, MAX_INSTRUCTIONS, &decodedInstructionsCount );
		if( res == DECRES_INPUTERR ) 
		{
			printf( "! DECRES_INPUTERR\n" );
			break;
		}
		else if( decodedInstructionsCount == 0 ) 
		{
			break;
		}

		for( i = 0; i < decodedInstructionsCount; i++ ) 
		{
			_DInst *inst = &decodedInstructions[i];

			if( callback( inst, data ) == false )
			{
				totsize = len;
				break;
			}

			totsize += inst->size;
		}

		buf    += totsize;
		len    -= totsize;
		offset += totsize;
	}
}

// This callback will search for CALLs to subroutines.
bool peSearchSubroutinesCallback( _DInst *inst, void *data )
{
	dasm_callback_data_t *cdata = (dasm_callback_data_t *)data;

	// Is this instruction a suitable call ?
	if( ( inst->opcode == I_CALL || inst->opcode == I_CALL_FAR ) && inst->ops[0].type == O_PC )
	{
		uint32_t dwFunctionAddress = inst->addr + inst->imm.sdword + inst->size;
		function_t *pFunction = NULL;
		PE_ADDRESS FunctionAddress = {0};

		// Are we already aware of this function?
		pFunction = (function_t *)ht_get( cdata->htFunctions, (void *)dwFunctionAddress );

		if( pFunction == NULL )
		{
			// Make sure this call references an executable section address.
			PIMAGE_SECTION_HEADER pFunctionSection = peGetSectionByAddress( cdata->pe, (uint64_t)dwFunctionAddress );

			if( pFunctionSection && ( ( pFunctionSection->Characteristics & IMAGE_SCN_CNT_CODE ) || ( pFunctionSection->Characteristics & IMAGE_SCN_MEM_EXECUTE ) ) )
			{
				if( peResolveVirtualAddress( cdata->pe, (uint64_t)dwFunctionAddress, &FunctionAddress ) )
				{
					// Create and save the function structure.
					pFunction = (function_t *)calloc( 1, sizeof(function_t) );

					pFunction->Section = pFunctionSection;
					memcpy( &pFunction->Address, &FunctionAddress, sizeof(PE_ADDRESS) );
					sprintf( pFunction->Name, "sub_%08X", dwFunctionAddress );

					ll_append( cdata->llFunctions, pFunction );
					ht_add( cdata->htFunctions, (void *)dwFunctionAddress, pFunction );
				}
			}
		}
	}

	return true;
}

// This callback will search for the first RET instruction inside the subroutine.
bool peAnalyzeSubroutinesCallback( _DInst *inst, void *data )
{
	dasm_callback_data_t *cdata = (dasm_callback_data_t *)data;

	if( inst->opcode == I_RET || inst->opcode == I_RETF )
	{
		cdata->pFunction->Address.Size = inst->addr - cdata->pFunction->Address.VA;

		return false;
	}

	return true;
}

int main(int argc, char* argv[])
{
	PE pe = {0}; 
	PE_STATUS status;
	dasm_callback_data_t cdata = {0};
	ll_t  llFunctions;
	ht_t *htFunctions;

	if( argc < 2 )
	{
		printf( "Usage: %s <pe-filename>\n", argv[0] );
		exit(1);
	}

	status = peOpenFile( &pe, argv[1] );
	if( status != PE_SUCCESS )
	{
		printf( "Open error: %08X\n", status );
		goto done;
	}

	// Initialize callback data.
	ll_init( &llFunctions );
	htFunctions = HT_CREATE_BY_DWORD();

	cdata.llFunctions = &llFunctions;
	cdata.htFunctions = htFunctions;
	cdata.pe = &pe;

	// Search each suitable section.
	PE_FOREACH_SECTION( &pe, pSection )
	{
		// skip empty sections
		if( pSection->SizeOfRawData == 0 )
			continue;
		// skip non executable or non code sections
		else if( !( pSection->Characteristics & IMAGE_SCN_CNT_CODE ) && !( pSection->Characteristics & IMAGE_SCN_MEM_EXECUTE ) )
			continue;

		printf
		( 
			"Analyzing section '%s' ( %08llX - %08llX ) ...\n", 
			pSection->Name, 
			pe.qwBaseAddress + pSection->VirtualAddress, 
			pe.qwBaseAddress + pSection->VirtualAddress + pSection->SizeOfRawData 
		);
		
		PE_ADDRESS SectionAddress = {0};

		peResolveSectionAddress( &pe, pSection, &SectionAddress );

		// Check for CALLs instruction into this section.
		peDisasm
		( 
			SectionAddress.VA, 
			SectionAddress.Data, 
			SectionAddress.Size, 
			pe.Headers.Plus ? Decode64Bits : Decode32Bits, 
			peSearchSubroutinesCallback, 
			&cdata 
		);
	}

	ll_foreach_data( &llFunctions, lli, function_t, pFunction )
	{
		cdata.pFunction = pFunction;
		// Find first RET instruction inside the function body.
		peDisasm
		( 
			pFunction->Address.VA, 
			pFunction->Address.Data, 
			pFunction->Address.Size, 
			pe.Headers.Plus ? Decode64Bits : Decode32Bits, 
			peAnalyzeSubroutinesCallback, 
			&cdata 
		);

		printf( "[%08llX] %s ( %I64d bytes )\n", pFunction->Address.VA, pFunction->Name, pFunction->Address.Size );
	}

	ll_destroy( &llFunctions, free );
	ht_destroy( htFunctions );

done:

	peClose(&pe);

	return 0;
}

