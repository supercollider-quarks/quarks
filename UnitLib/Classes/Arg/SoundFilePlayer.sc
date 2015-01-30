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


// a pseudo-ugen to play a BufSndFile from inside a Unit.
// auto-creates a control with the name <key> (default: bufSoundFile), format:
//	[ bufnum, rate, loop ]

BufSndFilePlayer {
	
	// *getArgs may also be used to feed other buffer playing UGens
	//	the list is formed according to PlayBuf arguments
	
	*getArgs { |numChannels = 1, key, trigger = 1, startPos, ugenRate = \audio| // user may override startPos
		var bufnum, rate, loop, startFrame;
		key = key ? 'soundFile';
		#bufnum, rate, loop = key.asSymbol.kr( [ 0, 1, 0 ] );
		if( startPos.isNil ) { startPos = 'u_startPos'.kr(0); }; // for use inside a U or UChain
		startFrame = ((startPos * BufSampleRate.kr( bufnum )) * rate.abs.max(1.0e-12));
		if( ugenRate == \control ) { startFrame = startFrame / (SampleRate.ir / ControlRate.ir); };
		^[ numChannels, bufnum, BufRateScale.kr( bufnum ) * rate, trigger, startFrame, loop ];
	}
	
	*ar { |numChannels = 1, key, trigger = 1, startPos, doneAction = 0|
		Udef.addBuildSpec(ArgSpec(key ? 'soundFile', nil, BufSndFileSpec(nil) ) );
		^PlayBuf.ar( *this.getArgs( numChannels, key, trigger, startPos ) ++ [ doneAction ] );
	}
	
	*kr { |numChannels = 1, key, trigger = 1, startPos, doneAction = 0|
		Udef.addBuildSpec(ArgSpec(key ? 'soundFile', nil, BufSndFileSpec(nil) ) );
		^PlayBuf.kr( *this.getArgs( numChannels, key, trigger, startPos, \control ) ++ [ doneAction ]  );
	}
	
}


// a pseudo-ugen to play a DiskSndFile from inside a Unit.
// auto-creates a control with the name <key> (default: diskSoundFile), format:
//	[ bufnum, rate, loop ]

DiskSndFilePlayer {
	
	*ar { |numChannels = 1, key|
		var bufnum, rate, loop;
		key = key ? 'soundFile';
		#bufnum, rate, loop = key.asSymbol.kr( [ 0, 1, 0 ] );
		Udef.addBuildSpec(ArgSpec(key ? 'soundFile', nil, DiskSndFileSpec(nil) ) );
		^VDiskIn.ar( numChannels, bufnum, BufRateScale.kr( bufnum ) * rate, loop );
	}
	
}