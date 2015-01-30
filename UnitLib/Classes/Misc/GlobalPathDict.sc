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

GlobalPathDict {
	
	classvar <>dict, <>replaceChar = $@;
	classvar <>relativePath;
	
	*initClass { 
		dict = IdentityDictionary(); 
		GlobalPathDict.put( \resources, String.scDir );
	}
	
	*put { |key, path|
		dict.put( key, path );
	}
	
	*at { |key|
		^dict.at( key );
	}
	
	*getPath { |path|
		var index, key;
		if( path[0] == replaceChar ) {
			index = path.indexOf( $/ ) ?? { path.size};
			key = path[1..index-1].asSymbol;
			path = path[index+1..];
			if( this.at( key ).isNil) {
				"%:getPath - % not found"
					.format( this, key )
					.warn;
				^path.standardizePath;
			} {
				^(this.at( key ).withoutTrailingSlash +/+ path).standardizePath;
			};
		} {
			^path.standardizePath;
		};
	}
	
	*formatPath { |path|
		var stPath, array = [], key, i = 0;
		
		this.put( '_relative', relativePath ?? { thisProcess.nowExecutingPath !? _.dirname } ); 
		
		dict.keysValuesDo({ |key, value|
			array = array.add( [ value.standardizePath.withTrailingSlash, key ] );
		});
		
		array = array.sort({ |a,b|
			(a[0].size < b[0].size) or: { a[0].size == b[0].size && { a[1] <= b[1] } }
		}).reverse;
		
		stPath = this.getPath( path );
		
		while { key.isNil && (i < array.size) } {
			if( stPath.find( array[i][0] ) == 0 ) {
				key = array[i][1];
				if( (key === '_relative') && { relativePath.isNil }) {
					path = stPath;
					key = nil;
					i = i+1;
				};
			} {
				i = i+1;
			};
		};
		
		if( key.notNil ) {
			^stPath.findReplace( array[i][0], replaceChar ++ key ++ "/" );
		} {
			^path;
		};
	}
}

+ String {
	
	getGPath {
		^GlobalPathDict.getPath( this );
	}
	
	formatGPath {
		^GlobalPathDict.formatPath( this );
	}
	
	putGPath { |key = \default|
		GlobalPathDict.put( key, this );
	}
	
}

+ Nil {
	
	putGPath { |key = \default|
		GlobalPathDict.put( key, this );
	}
	
	getGPath { ^this }
	
	formatGPath { ^this }

}