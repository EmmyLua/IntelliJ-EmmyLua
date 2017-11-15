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
#include <stdint.h>
#include <stdlib.h>

#define HT_N_BUCKETS 65535

typedef unsigned short hash_t;

#pragma pack( push )
#pragma pack( 1 )

//! hashtable bucket entry list item
typedef struct ht_entry
{
    void  *key;
    void  *value;

    struct ht_entry *next;
}
ht_entry_t;

#pragma pack( pop )

typedef struct
{
	hash_t		h;
	ht_entry_t *e;
}
ht_iterator_t;

//! protypes used for function pointers
typedef void    *(* ht_copy_t)( void *k );
typedef int32_t  (* ht_cmp_t)( void *a, void *b );
typedef uint32_t (* ht_hash_t)( void *k );
typedef void     (* ht_free_t)( void *k );

//! the main hashtable structure
typedef struct
{
	// a static array of buckets, this hashtable implementation
	// does not support reashing
    ht_entry_t *buckets[HT_N_BUCKETS];
    
	// key copy function pointer ( optional )
    ht_copy_t key_copy;
	// key compare function pointer
    ht_cmp_t  key_cmp;
	// key hashing function pointer ( optional )
    ht_hash_t key_hash;
	// key free function pointer ( optional )
    ht_free_t key_free;
	// value copy function pointer ( optional )
    ht_copy_t val_copy;
	// value free function pointer ( optional )
    ht_free_t val_free;
}
ht_t;

//! Create a hashtable object.
//! 
//! @param key_copy a ht_copy_t type function pointer, if specified each key will be cloned with it.
//! @param key_cmp a ht_cmp_t type function pointer which will be used to compare keys, must return 0 if keys are equal.
//! @param key_hash a ht_hash_t type function pointer which will be used to create the hash for a key, if not specified the hash will be key % HT_N_BUCKETS.
//! @param key_free a ht_free_t type function pointer which will be used to free keys if key_copy was specified.
//! @param val_copy a ht_copy_t type function pointer, if specified each value will be cloned with it.
//! @param val_free a ht_free_t type function pointer which will be used to free values if val_copy was specified.
//!
//! @return a pointer to a ht_t structure or NULL if allocation failed.
ht_t *ht_create( ht_copy_t key_copy, ht_cmp_t key_cmp, ht_hash_t key_hash, ht_free_t key_free, ht_copy_t val_copy, ht_free_t val_free );

//! Add an object to the hashtable.
//! 
//! @param ht the ht_t pointer created with ht_create.
//! @param key the key of the object.
//! @param value the object.
//!
//! @return a pointer the previous object with the given key if any, otherwise NULL.
void *ht_add( ht_t *ht, void *key, void *value );

bool ht_first( ht_t *ht, ht_iterator_t *iter );
bool ht_next( ht_t *ht, ht_iterator_t *iter );

void ht_remove( ht_t *ht, void *key );

//! Get an object from the hashtable.
//! 
//! @param ht the ht_t pointer created with ht_create.
//! @param key the key of the object to be retrieved.
//!
//! @return a pointer the object with the given key if any, otherwise NULL.
void *ht_get( ht_t *ht, void *key );

//! Destroy the hashtable.
//! 
//! @param ht the ht_t pointer created with ht_create.
void  ht_destroy( ht_t *ht );

int32_t  ht_qword_cmp( void *a, void *b );
int32_t  ht_dword_cmp( void *a, void *b );
int32_t  ht_word_cmp( void *a, void *b );
uint32_t ht_str_ihash( void *k );
uint32_t ht_str_hash( void *k );
uint32_t ht_wstr_hash( void *k );

#define HT_CREATE_BY_QWORD() \
	ht_create( NULL, ht_qword_cmp, NULL, NULL, NULL, NULL )

#define HT_CREATE_BY_DWORD() \
	ht_create( NULL, ht_dword_cmp, NULL, NULL, NULL, NULL )

#define HT_CREATE_BY_WORD() \
	ht_create( NULL, ht_word_cmp, NULL, NULL, NULL, NULL )

#define HT_CREATE_BY_STRING() \
	ht_create( NULL, (ht_cmp_t)strcmp, ht_str_hash, NULL, NULL, NULL )

#define HT_CREATE_BY_ISTRING() \
	ht_create( NULL, (ht_cmp_t)_stricmp, ht_str_ihash, NULL, NULL, NULL )

#define HT_CREATE_BY_WSTRING() \
	ht_create( NULL, (ht_cmp_t)wcscmp, ht_wstr_hash, NULL, NULL, NULL )