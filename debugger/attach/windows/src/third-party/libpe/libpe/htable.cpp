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
#include "htable.h"
#include <string.h>
#include <ctype.h>

inline ht_entry_t *ht_make_entry( void *key, void *value )
{
	ht_entry_t *entry = (ht_entry_t *)malloc( sizeof(ht_entry_t) );

    entry->key   = key;
    entry->value = value;
    entry->next  = NULL;

	return entry;
}

ht_t *ht_create( ht_copy_t key_copy, ht_cmp_t key_cmp, ht_hash_t key_hash, ht_free_t key_free, ht_copy_t val_copy, ht_free_t val_free )
{
    ht_t *ht = (ht_t *)calloc( 1, sizeof(ht_t) );

    ht->key_copy = key_copy;
    ht->key_cmp  = key_cmp;
    ht->key_hash = key_hash;
    ht->key_free = key_free;
    ht->val_copy = val_copy;
    ht->val_free = val_free;

    return ht;
}

void *ht_add( ht_t *ht, void *key, void *value )
{
    void *old = NULL;
    hash_t hash = ( ht->key_hash ? ht->key_hash( key ) % HT_N_BUCKETS : (unsigned long)key % HT_N_BUCKETS );
    ht_entry_t *entry = NULL,
               *tail = NULL,
               *bucket = NULL;

	bucket = ht->buckets[ hash ];

    void *v = ht->val_copy ? ht->val_copy(value) : value;

    // new bucket
    if( bucket == NULL )
    {
        ht->buckets[ hash ] = ht_make_entry( ht->key_copy ? ht->key_copy(key) : key, v );
    }
    else
    {
        for( entry = bucket; entry; entry = entry->next )
        {
            tail = entry;

            // existing key, replace old value
            if( ht->key_cmp( key, entry->key ) == 0 )
            {
                old = entry->value;
		        entry->value = v;
                break;
            }
        }

        // nothing found, append new entry
        if( entry == NULL )
        {
            tail->next = ht_make_entry( ht->key_copy ? ht->key_copy(key) : key, v );
        }
    }

    return old;
}

void *ht_get( ht_t *ht, void *key )
{
	if( ht && key )
	{
		hash_t hash = ( ht->key_hash ? ht->key_hash( key ) % HT_N_BUCKETS : (uint32_t)key % HT_N_BUCKETS );
		ht_entry_t *entry = NULL,
				   *bucket = NULL;

		bucket = ht->buckets[ hash ];

		for( entry = bucket; entry; entry = entry->next )
		{
			if( ht->key_cmp( key, entry->key ) == 0 )
			{
				return entry->value;
			}
		}
	}

    return NULL;
}

bool ht_first( ht_t *ht, ht_iterator_t *iter )
{
    for( iter->h = 0; iter->h < HT_N_BUCKETS; ++iter->h )
    {
        iter->e = ht->buckets[ iter->h ];
        if( iter->e )
		{
			return true;
		}
	}

	return false;
}

bool ht_next( ht_t *ht, ht_iterator_t *iter )
{
	if( iter->h == HT_N_BUCKETS )
	{
		return false;
	}

	iter->e = iter->e->next;

	if( iter->e == NULL )
	{
		do
		{
			iter->e = ht->buckets[ ++iter->h ];
		}
		while( iter->e == NULL && iter->h < HT_N_BUCKETS );
		
		return ( iter->e && iter->h < HT_N_BUCKETS ? true : false );
	}

	return true;
}

void ht_remove( ht_t *ht, void *key )
{
    hash_t hash = ( ht->key_hash ? ht->key_hash( key ) % HT_N_BUCKETS : (unsigned long)key % HT_N_BUCKETS );
    ht_entry_t *entry = NULL,
               *prev = NULL,
               *bucket = NULL;

	bucket = ht->buckets[ hash ];

    // new bucket
    if( bucket != NULL )
    {
        for( entry = bucket; entry; entry = entry->next )
        {
            if( ht->key_cmp( key, entry->key ) == 0 )
            {
                break;
            }

			prev = entry;
        }

        // nothing found, append new entry
        if( entry != NULL )
        {
			if( ht->key_free )
			{
				ht->key_free( entry->key );
			}

			if( ht->val_free )
			{
				ht->val_free( entry->value );
			}

			// first element of the bucket
			if( prev == NULL )
			{
				ht->buckets[ hash ] = entry->next;
			}
			else
			{
				prev->next = entry->next;
			}

			free( entry );
        }
    }
}

void ht_destroy( ht_t *ht )
{
    ht_entry_t *entry = NULL,
               *bucket = NULL;
    hash_t hash = 0;

    for( hash = 0; hash < HT_N_BUCKETS; ++hash )
    {
        entry = ht->buckets[ hash ];
        while( entry )
        {
            ht_entry_t *next = entry->next;

            if( ht->key_free )
                ht->key_free( entry->key );

            if( ht->val_free )
                ht->val_free( entry->value );

            free( entry );

            entry = next;
        }
    }

    free( ht );
}

int32_t ht_qword_cmp( void *a, void *b )
{
	return (uint64_t)a == (uint64_t)b ? 0 : 1;
}

int32_t ht_dword_cmp( void *a, void *b )
{
	return (uint32_t)a == (uint32_t)b ? 0 : 1;
}

int32_t ht_word_cmp( void *a, void *b )
{
	return (uint16_t)a == (uint16_t)b ? 0 : 1;
}

uint32_t ht_str_ihash( void *k )
{
	uint32_t hash = (uint32_t)5381;
	const char *p = (const char *)k;
	size_t len = strlen( p );

    while( len-- )
	{
        hash = ((hash << 5) + hash) + (tolower(*p++));
	}

    return hash;
}

uint32_t ht_str_hash( void *k )
{
	uint32_t hash = (uint32_t)5381;
	const char *p = (const char *)k;
	size_t len = strlen( p );

    while( len-- )
	{
        hash = ((hash << 5) + hash) + (*p++);
	}

    return hash;
}

uint32_t ht_wstr_hash( void *k )
{
	uint32_t hash = (uint32_t)5381;
	const wchar_t *p = (const wchar_t *)k;
	size_t len = wcslen( p );

    while( len-- )
	{
        hash = ((hash << 5) + hash) + (*p++);
	}

    return hash;
}