/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

UndoManager {
	
	var <history, <current = 0;
	var <>max = 50;
	var <>verbose = false;
	
	add { |obj, msg|
		history = [ [ obj.deepCopy, msg, Date.localtime ] ] ++ (history ? [])[current..];
		current = 0;
		if( history.size > max ) { history = history[..max+1] };
		this.changed( \add );
		if(  this.verbose  ) {
			"% :: added state\n\tnew size: %)\n\tmessage: %\n".postf( 
				thisMethod.asString, 
				history.size,
				msg 
			);
		};
	}
		
	undo { |num = 1|
		var obj;
		if( (num + current).exclusivelyBetween( -1, history.size ) ) { 
			current = num + current;
			obj = history[current][0].copy;
			this.changed( \undo );
			if(  this.verbose  ) {
				"% :: % ".postf( 
					thisMethod.asString, 
					num
				);
			};
			^obj;
		} { 
			if( this.verbose ) {
				"% :: minimum or maximum reached\n".postf( thisMethod.asString );
			};
			^nil;
		};
	}
	
	previous { ^this.undo(1); }
	next { ^this.undo(-1); }
	
	at { |index = 0|
		^history[ index ] !? { history[ index ][ 0 ] };
	}
	
	clear {
		history = [];
		current = 0;
		this.changed( \clear );
		if(  this.verbose  ) {
			"% :: cleared".postf( thisMethod.asString );
		};
	}
	
	post {
		"UndoManager history:".postln;
		 history.reverseDo({ |item, i|
			var size;
			size = history.size;
			"\t%: % (%)\n".postf( size - i, (item[1] ? "").asString, item[2].hourStamp[..7] );
		});
	}
	
}