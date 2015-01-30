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

BufferCenter {
	classvar <>all, <>defaults;
	
	var <server, <>dict;
	
	/*
	dict:
		(
		Buffer: (
			\synths: [ ... ], // synths depending upon buffer 
			\startFrame: xxx, // start frame of sound file (default: 0)
			\numFrames: xxx,  // numFrames of sound file (default: -1)
			\channels: []    // default nil
			\cue: bool )	   // true if DiskIn cue buffer (default: false)
		)
	*/
	
	*forServer { |server| // use this instead of *new
		server = server ? Server.default;
		^all.detect({ |item| item.server == server }) ?? { this.new( server ) }	}
	
	*new { |server| ^super.newCopyArgs( server ? Server.default ).init }
	
	*initClass {
		defaults = IdentityDictionary[		
			\startFrame -> 0,
			\numFrames -> -1,
			\cue -> false
		];
	}
	
	init {
		dict = IdentityDictionary();
		all = all.add( this );
	}

	*bufferPerform { |selector ...args|
		var buf;
		buf = Buffer.perform( selector, *args );
		this.addBuffer( buf );
		^buf;
	}
	
	// doubles for Meta_Buffer methods
	*alloc { |server, numFrames, numChannels = 1, completionMessage, bufnum|
		^this.bufferPerform( \alloc, server, numFrames, numChannels, completionMessage, bufnum )
		}
	
	addBuffer { |buffer ...args| // args: arg pairs
		if( buffer.server == server )
			{ dict.put( buffer, IdentityDictionary().proto_( defaults ).putPairs( *args ) ); }
			{ this.class.addBuffer( buffer ); }
	}
	
	removeBuffer { |buffer| 
		if( buffer.server == server )
			{ dict.removeAt( buffer ); }
			{ this.class.removeBuffer( buffer ); }
	}
	
	*addBuffer { |buffer|
		this.forServer( buffer.server ).addBuffer( buffer );
	}
	
	*removeBuffer { |buffer|
		this.forServer( buffer.server ).removeBuffer( buffer );
	}
	
	
	
	
}