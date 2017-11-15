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
#include "llist.h"

#define LL_ITEM_INIT( ITEM ) \
	(ITEM)->data = \
	(ITEM)->next = NULL

#define LL_ITEM_CREATE( DATA ) \
	ll_item_t *item = (ll_item_t *)calloc( 1, sizeof(ll_item_t) ); \
	item->next = NULL; \
	item->data = DATA

void ll_init( ll_t *ll )
{
	ll->head = NULL;
	ll->tail = NULL;
	ll->elements = 0;
}

void ll_append( ll_t *ll, void *data )
{
	LL_ITEM_CREATE( data );

	if( ll->head == NULL )
	{
		ll->head = item;
	}
	else
	{
		ll->tail->next = item;
	}

	ll->tail = item;
	++ll->elements;
}

void ll_destroy( ll_t *ll, ll_data_free_t data_free )
{
	if( ll->elements )
	{
		for( ll_item_t *entry = ll->head; entry != NULL; )
		{
			ll_item_t *next = entry->next;
		
			if( data_free != NULL && entry->data != NULL )
			{
				data_free( entry->data );
			}

			free( entry );

			entry = next;
		}
	}
}