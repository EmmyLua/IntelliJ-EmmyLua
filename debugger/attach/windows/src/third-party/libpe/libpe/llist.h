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

#include <stdint.h>
#include <stdlib.h>

typedef struct _ll_item
{
	void     *data;
	_ll_item *next;
}
ll_item_t;

typedef struct
{
	ll_item_t *head;
	ll_item_t *tail;
	uint32_t   elements;
}
ll_t;

typedef void ( *ll_data_free_t)( void *data );

#define ll_foreach( ll, item ) for( ll_item_t *item = (ll)->head; item != NULL; item = item->next )

#define ll_foreach_data( ll, item, type, name ) \
	type *name = NULL; \
	ll_item_t *item = NULL; \
	for( item = (ll)->head, name = item ? (type *)item->data : NULL; item != NULL && ( name = item ? (type *)item->data : NULL ); item = item->next )

void ll_init( ll_t *ll );
void ll_append( ll_t *ll, void *data );
void ll_destroy( ll_t *ll, ll_data_free_t data_free );