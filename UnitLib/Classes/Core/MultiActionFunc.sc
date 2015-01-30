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

// MultiActionFunc remembers how many times an action is added,
// and fires only if the action was called as many times
// example:

/*

(
// "done" is posted only once after all buffers are loaded
a = MultiActionFunc( { |...args| args.postln; nil; } );

5.do({
	Buffer.alloc( s, 44100, 1 ). a.getAction );
});

)



*/


MultiActionFunc {
	var <>action, <>routine, <>i = 0, <>n = 0;
	
	*new { |action| 
		^super.newCopyArgs( action ); 
	}
	
	getAction {
		n = n + 1;
		^{ |...args|
			if( i < (n-1) ) {
				i = i+1; nil;
			} {
				action.value( *args );
			};
		};
	}
	
}
