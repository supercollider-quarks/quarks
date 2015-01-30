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

ObjectWithArgs {
	
	classvar <>verbose = false;
	
	var <args;
	
	// args are an array of key, value pairs: [ key, value, key, value ...etc ]
	
	keys { ^(args ? [])[0,2..] }
	argNames { ^this.keys }
	
	values { ^(args ? [])[1,3..] }
	values_ { |newValues|  
		var keys = this.keys;
		newValues[..keys.size-1].do({ |val, i|
			this.setArg( keys[i], val )
		});
	}

	setArg { |key, value|
		var index;
		index = this.keys.indexOf( key );
		if( index.notNil ) { 
			args[ (index * 2) + 1 ] = value;
			this.changed( key, value );
		} {
			if( verbose ) {
				"%:% arg % not found".format( this.class, thisMethod.name, key ).warn;
			};
		};	
	}
	
	getArg { |key|
		var index;
		index = this.keys.indexOf( key );
		if( index.notNil ) { 
			^args[ (index * 2) + 1 ] 
		} { 
			if( verbose ) {
				"%:% arg % not found".format( this.class, thisMethod.name, key ).warn;
			};
			^nil 
		};
	}
	
	findKeyForValue { |val|
		var index;
		index = this.values.indexOf( val );
		^index !? { this.keys[ index ] }
	}
	
	at { |key| ^this.getArg( key ) }
	put { |key| ^this.setArg( key ) }

	doesNotUnderstand { |selector ...args| 
		// bypasses errors; warning if arg not found
		if( selector.isSetter ) { 
			this.setArg( selector.asGetter, *args ) 
		} {
			^this.getArg( selector );
		};	
	}
	
    archiveAsCompileString { ^true }

}

+ SequenceableCollection {
	
	pairsAt { |key| // could be optimized
		var index = this[0,2..].indexOf( key );
		if( index.notNil ) { 
			^this.at( (index * 2) + 1 );
		} {
			^nil 
		};
	}
	
	pairsPut { |key, value| // only puts if key is found
		var index = this[0,2..].indexOf( key );
		if( index.notNil ) { 
			this.put((index * 2) + 1, value);
		};
	}
}
