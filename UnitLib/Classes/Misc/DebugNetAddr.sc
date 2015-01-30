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

DebugNetAddr : NetAddr {
	
	classvar <>debug = true;
	classvar <>postStatus = false;
	
	sendRaw { arg rawArray;
		if( debug ) { rawArray.postln };
		^super.sendRaw( rawArray );
	}
	sendMsg { arg ... args;
		if( debug && { postStatus or: {args[0].asSymbol !== '/status' } } ) { args.postln };
		^super.sendMsg( *args );
	}
	// warning: this primitive will fail to send if the bundle size is too large
	// but it will not throw an error.  this needs to be fixed
	sendBundle { arg time ... args;
		if( debug ) { ([ time ] ++ args).postln };
		^super.sendBundle( time, *args );
	}

	sendPosBundle { arg position ... args; // a 64 bit (double) position value
		if( debug ) { ([ position ] ++ args).postln };
		^super.sendPosBundle( position, *args );
	}
}