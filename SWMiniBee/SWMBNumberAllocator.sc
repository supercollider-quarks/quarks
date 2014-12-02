/* $Id: MotionTracker.sc 54 2009-02-06 14:54:20Z nescivi $ 
 *
 * Copyright (C) 2009, Marije Baalman <nescivi _at_ gmail.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 */

SWMBNumberAllocator
{
	var lo, hi, <freeList, next;
	var <allocList;
	
	*new { arg lo, hi;
		^super.newCopyArgs(lo, hi).init
	}
	init {
		next = lo - 1;
		freeList = List.new;
		allocList = List.new;
	}

	allocID{ |id|
		freeList.remove( id );
		this.addToAllocList( id ); 
	}

	alloc {
		var id;
		if (freeList.size > 0, { 
			id = freeList.removeAt(0); 
			this.addToAllocList( id ); 
			^id }
		);
		if (next < hi, { 
			next = next + 1;
			if ( allocList.includes( next ) ){
				^this.alloc;
			}{
				id = next;
				this.addToAllocList( id );
				^id;
			}
		});
		^nil
	}
	addToAllocList{ |id|
		allocList = allocList.add(id).sort;
	}
	free { arg inIndex;
		allocList.remove( inIndex );
		freeList = freeList.add(inIndex).sort; 
	}
}
