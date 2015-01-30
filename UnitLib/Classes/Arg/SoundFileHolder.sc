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

AbstractRichBuffer {
	
	classvar <>allBuffers;
	classvar <>allUnits;
	
	classvar <>useCheckFree = true; // safer in high-traffic situations, but not for NRT
	
    var <numFrames, <numChannels, <sampleRate;
    var <>unitArgName;

	// var <>buffers; // holder for all buffers
    // var <unit, <unitArgName;
    
    *initClass { 
	    allBuffers = IdentityDictionary();
	    allUnits = IdentityDictionary();
	}

    *new{ |numFrames, numChannels = 1, sampleRate = 44100|
        ^super.newCopyArgs(numFrames, numChannels, sampleRate)
    }

    shallowCopy{
        ^this.class.new(numFrames, numChannels, sampleRate)
	}
		
	== { |that| // use === for identity
		^this.compareObject(that);
	}

    numFrames_ { |new|
		numFrames = new;
		this.changed( \numFrames, numFrames );
	}

	numChannels_ { |new|
		numChannels = new;
		this.changed( \numChannels, numChannels );
	}

	sampleRate_ { |new|
		sampleRate = new ? 44100;
		this.changed( \sampleRate, sampleRate );
	}
	
	buffers { ^allBuffers[ this ] }
	buffers_ { |buffers| allBuffers[ this ] = buffers; }

    //BUFFER MANAGEMENT
	addBuffer { |buf|
		this.buffers = this.buffers.add( buf );
		this.changed( \buffers, this.buffers );
	}

	removeBuffer { |buf|
		this.buffers.remove( buf );
		if( this.buffers.size == 0 ) { this.buffers = nil };
		this.changed( \buffers, this.buffers );
	}

	currentBuffers { |server| // returns all buffers if server == nil
		if( server.notNil ) {
			^this.buffers.select({ |item| item.server == server }) ? #[];
		};
		^this.buffers;
	}

	currentBuffer { |server|
		var buf;
	    buf = this.currentBuffers(server).last;
	    if( buf.notNil ) { 
		    ^buf;
	    } {
		    "%:currentBuffer - no buffer loaded for %"
		    		.format( this.class, server ).warn;
		    	^buf
	    };   
	}
	
	freeBuffer { |buf, action|
		if( buf.notNil ) {
			if( UEvent.nrtMode != true ) {
				buf.checkFree( action );
			} {
				buf.free;
				action.value( buf );
			};
			this.removeBuffer( buf );
		} {
			 "%:freeBuffer - no buffer to be freed"
		    		.format( this.class ).warn;
		    	action.value;
		};
	}
	
	freeAllBuffers { |server|
	    if( server.notNil ) {
		    this.currentBuffers( server ).do( this.freeBuffer(_) )
		}{
		    this.buffers.copy.do( this.freeBuffer(_) )
		}
	}
	
	freeBufferFor { |server|
		if( server.notNil ) {
			this.freeBuffer( this.currentBuffers(server).first );
		};
	}

	resetBuffers { |server|
		this.currentBuffers( server ).do({ |buf|
			this.removeBuffer( buf );
		});
	}

	prepare { |servers, startPos = 0, action|
	    //this.resetBuffers;
	    action = MultiActionFunc( action );
	    servers.do({ |server| this.makeBuffer(server, startPos, action: action.getAction) })
	}

	dispose {
       this.freeAllBuffers;
       this.unit = nil;
	}

	disposeFor { |server|
        this.freeBufferFor(server);
        if( this.buffers.size == 0 ) {
	        this.unit = nil;
        };
	}

	asControlInputFor { |server| ^this.currentBuffer(server) }
	
	u_waitTime { ^1 }
	
	asUnitArg { |unit, key|
		this.unitArgName = key;
	}

	unit_ { |aUnit|
		if( aUnit.notNil ) {
			case { this.unit == aUnit } {
				// do nothing
			} { allUnits[ this ].isNil } {
				allUnits[ this ] = [ aUnit, nil ];
			} {
				"Warning: unit_ \n%\nis already being used by\n%\n".postf(
					this.class,
					this.asCompileString, 
					this.unit 
				);
			};
		} {
			allUnits[ this ] = nil; // forget unit
		};
		
		/*
	    case { unit == aUnit } { 
		    // do nothing
		} {
		    unit.isNil 
		} {
	        unit = aUnit;
	        unitArgName = nil;
	    } {
	        "Warning:".postln;
	        this.postcs;
	        "\nis already being used by".postln;
	        unit.postln;
	    };
	    */
	}
	
	unit { ^allUnits[ this ] !? { allUnits[ this ][0] }; }
	
	unitSet { // sets this object in the unit to enforce setting of the synths
		if( this.unit.notNil ) {	
			if( this.unitArgName.notNil ) {
				this.unit.set( this.unitArgName, this );
			};
		};
	}
}

RichBuffer : AbstractRichBuffer {

    *new{ |numFrames, numChannels = 1, sampleRate = 44100|
        ^super.new(numFrames, numChannels, sampleRate)
    }

	makeBuffer { |server, startPos = 0, action, bufnum|
	    var buf;
		buf = Buffer.alloc(server, numFrames, numChannels, nil, bufnum );
		OSCresponderNode( server.addr, '/done', { |time, resp, msg, addr|
			if( msg == [ '/done', '/b_alloc', buf.bufnum ] ) {
				resp.remove;
				action.value( buf );
			};
		}).add;
		this.addBuffer( buf );
		^buf;
	}

	printOn { arg stream;
		stream << "a " << this.class.name << "(" <<* [
		    numFrames, numChannels, sampleRate]
		<<")"
	}

    storeOn { arg stream;
		stream << this.class.name << "(" <<* [
		    numFrames, numChannels, sampleRate]
		<<")"
	}
}

AbstractSndFile : AbstractRichBuffer {
	
	// points to a Sndfile and holds its specs, similar to SoundFile
	// aditional parameters for Buffer loading and playback settings
	// fully MVC aware
	
	var <path;
	var <startFrame = 0, endFrame;  // for buffer loading
	var <rate = 1;
	var <loop = false;
		
	*newBasic{ |path, numFrames, numChannels, sampleRate = 44100, startFrame = 0, endFrame, 
		rate = 1, loop = false |
		^super.new(numFrames, numChannels, sampleRate)
		    .initAbstractSndFile( path, startFrame, endFrame, rate, loop );
	}

	shallowCopy{
        ^this.class.newBasic(path, numFrames, numChannels, sampleRate, startFrame, endFrame, rate, 
        		loop);
	}

	initAbstractSndFile { |inPath, inStartFrame, inEndFrame, inRate, inLoop|
		 path = inPath.formatGPath;
		 startFrame = inStartFrame;
		 endFrame = inEndFrame;
		 rate = inRate;
		 loop = inLoop;
	}
	
	exists { ^File.exists(  path.getGPath.asPathFromServer ) }

	*buf{ ^BufSndFile }
	*disk{ ^DiskSndFile }
	*fromType{ |type|
	    ^switch(type)
	    {\buf}{BufSndFile}
	    {\disk}{DiskSndFile}
	}

	fromFile { |soundfile|
		if( this.prReadFromFile( soundfile ).not ) { 
			"%:fromFile - could not open file '%'\n".postf( this.class, path.basename ) 
		}
	}
	
	prReadFromFile { |soundfile|
		var test = true;
		if( soundfile.isNil or: { soundfile.isOpen.not } ) {
			soundfile = soundfile ?? { SoundFile.new }; 
			test = soundfile.openRead( path.getGPath.asPathFromServer );
			soundfile.close; // close if it wasn't open
		};
		if( test ) {	
			this.numFrames = soundfile.numFrames;
			this.numChannels = soundfile.numChannels;
			this.sampleRate = soundfile.sampleRate;
			^true;
		} { 
			^false 
		};
	}
	
	asSoundFile { // convert to normal soundfile
		^SoundFile( path.getGPath.asPathFromServer )
			//.numFrames_( numFrames ? 0 )
			.instVarPut( \numFrames,  numFrames ? 0 )
			.numChannels_( numChannels ? 1 )
			.sampleRate_( sampleRate ? 44100 );
	} 
	
	// mvc aware setters
	
	path_ { |new, update = false|
		path = (new ? path).formatGPath;
		this.changed( \path, path );
		if( update == true ) { this.prReadFromFile; };
	}
	
	basename { ^path !? { path.basename } }
	basename_ { |basename| 
		if( path.isNil ) {
			this.path = basename;
		} {
			this.path = path.dirname +/+ basename;
		};
	}
	
	dirname {  ^path !? { path.dirname } }
	dirname_ { |dirname| 
		if( path.isNil ) {
			this.path = dirname;
		} {
			this.path = dirname +/+ path.basename;
		};
	}

	startFrame_ { |new|
		startFrame = (new ? 0).max(0);
		this.changed( \startFrame, startFrame );
	}
	
	endFrame_ { |new|
		endFrame = new.min(numFrames);
		this.changed( \endFrame, endFrame );
	}
	
	start { ^this.startFrame }
	start_ { |frame| this.startFrame = frame }
	
	end { ^this.endFrame }
	end_ { |frame| this.endFrame = frame }
	
	
	cutStart { |time = 0|
		this.startSecond = this.startSecond + time;
	}

	rate_ { |new|
		rate = new ? 1;
		this.changed( \rate, rate );
		this.unitSet;
	}
	
	loop_ { |new|
		loop = new ? false;
		this.changed( \loop, loop );
		this.unitSet;
	}
	
	endFrame { if( numFrames.notNil ) { 
			^(endFrame ? numFrames) % (numFrames+1) 
		} { 
			^endFrame 
		};
	}
	
	// pseudo getter/setters
		 
	usedFrames { ^(this.endFrame ?? { startFrame - 1 }) - startFrame } // -1 if unknown or to end
	usedFrames_ { |frames = (-1)| // -1 means from startFrame to end 
		if( [-1, nil].includes(frames.asInt) ) { 
			this.endFrame = nil; 
		} { 
			this.endFrame = frames + startFrame 
		};
	}

	framesToSeconds { |frames = 0|  ^frames !? { (frames / (sampleRate ? 44100)) / rate } }
	secondsToFrames { |seconds = 0| ^seconds !? { seconds * rate * (sampleRate ? 44100) } }

	startSecond 	{ ^this.framesToSeconds( this.startFrame ); }
	endSecond 	{ ^this.framesToSeconds(this.endFrame); }
	duration 		{ ^this.framesToSeconds(this.usedFrames); } // negative if unknown
	fileDuration 	{ ^this.framesToSeconds(this.numFrames); }
	eventDuration { ^if(loop){ inf }{ this.duration } }
	
	startSecond_ { |startSecond = 0| this.startFrame = this.secondsToFrames( startSecond ); }
	endSecond_ { |endSecond = 0| this.endFrame = this.secondsToFrames( endSecond ); }
	duration_ { |duration| this.usedFrames = this.secondsToFrames( duration ) }
	fileDuration_ { |duration| this.numFrames = this.secondsToFrames( duration ); }
	
	// utilities
	
	plot { this.asSoundFile.plot; }
	
	checkDo { |action|
		var test = true;
		if( numFrames.isNil ) { 
			test = this.prReadFromFile; // get numFrames etc.
		};
		if( test ) { 
			^action.value 
		} {
			"%: file % not found".format( this.class, path.quote ).warn;
			^false;
		};
	}
	
	splice { |seconds|
		^this.checkDo({
			this.spliceFrames( this.secondsToFrames( seconds ) );
		});
	}
	
	spliceFrames { |frame = 0| // split into segments based on frame or frames relative to startFrame
		^this.checkDo({
			var positions, segments, usedFrames;
			usedFrames = this.usedFrames;
			positions = [ 0, usedFrames];
			frame.asCollection.do({ |item|
				if( positions.includes( item ).not && { item < usedFrames } ) {
					positions = positions.add( item );
				};
			});
			positions.sort;
			positions.doAdjacentPairs({ |a,b| segments = segments.add( [a,b] ); });
			if( segments.size > 1 ) {
				segments.collect({ |segment|
					this.copy
						.startFrame_( startFrame + segment[0] )
						.endFrame_( startFrame + segment[1] )
				});
			} { 
				[ this ];
			}
		});
	}
	
	makeUnit { 
		^U( this.unitNamePrefix++"SoundFile", [\soundFile, this ] );
	}
	
	asUnit {
		^this.makeUnit;
	}

	makeUChain { |outputUnit = \output|
		var chain;
		this.unit = nil; // forget old unit
		chain = UChain( this.makeUnit, outputUnit);
		if( loop.not ) { chain.dur_( this.eventDuration ) };
		^chain;
	}

	play{ |target, startPos = 0|
	    var chain = this.makeUChain;
	    chain.prepareAndStart(target ? Server.default, startPos);
	    ^chain
	}

    printOn { arg stream;
		stream << this.class.name << "(" <<* [
		    	path, numFrames, numChannels, sampleRate,
            	startFrame, endFrame, rate, loop
		]  <<")"
	}

    storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath.quote, numFrames, numChannels, sampleRate,
             startFrame, endFrame, rate, loop
		]  <<")"
	}
	
}

BufSndFile : AbstractSndFile {

    var <useChannels, <>useStartPosForBuf = false;

	*new{ |path, startFrame = 0, endFrame, rate = 1, loop = false, useChannels | 
		// path of existing file or SoundFile
		if( path.class == SoundFile ) {
			^this.newBasic( path.path, nil, nil, nil, startFrame, endFrame, rate, loop )
				.useChannels_(useChannels).fromFile( path );
		} {
			^this.newBasic( path, nil, nil, nil, startFrame, endFrame, rate, loop )
				.useChannels_(useChannels).fromFile;
		};
	}
	
	asBufSndFile { ^this }
	
	asMonoBufSndFile { 
		^MonoBufSndFile.newCopyVars( this ); 
	}
	
	asDiskSndFile { 
		var unit; // pass unit on to new object
		unit = this.unit;
		this.unit = nil;
		^DiskSndFile.newCopyVars( this ).unit_( unit ); 
	}

    useChannels_ { |new|
        useChannels = new;
        this.changed( \useChannels, useChannels );
    }
    
    numChannelsForPlayBuf {
	    ^if( useChannels.isNil ) { ^numChannels } { ^this.useChannels.size };
    }
    
    asControlInputFor { |server, startPos = 0| 
	    ^[ this.currentBuffer(server, startPos), rate, loop.binaryValue ] 
	   }
	   
	// not used by Unit:
	asControlInput { 
		^this.asControlInputFor( Server.default ); // assume default server
	}
	
	asOSCArgEmbeddedArray { | array| ^this.asControlInput.asOSCArgEmbeddedArray(array) }

    makeBuffer { |server, startPos = 0, action, bufnum|
		var buf, addStartFrame = 0, localUsedFrames;
		
		localUsedFrames = this.usedFrames;
		
		if( useStartPosForBuf && { startPos != 0 } ) { 
			addStartFrame = this.secondsToFrames( startPos );
			if( localUsedFrames != -1 ) { 
				localUsedFrames - addStartFrame;
			};
		};
		
		if( this.useChannels.notNil ) {
			buf = Buffer.readChannel( server, path.getGPath,
					startFrame + addStartFrame, localUsedFrames, this.useChannels, action, bufnum );
		} {
			buf = Buffer.read( server, path.getGPath,
					startFrame + addStartFrame, localUsedFrames, action, bufnum );
		};
		this.addBuffer( buf );
		^buf;
	}

    unitNamePrefix{ ^"buf" }
    
    u_waitTime { ^5 }
    
    storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath.quote, numFrames, numChannels, sampleRate,
             startFrame, endFrame, rate, loop
		]  << ")" << if( this.useChannels.notNil ) { ".useChannels_(%)".format( useChannels ) } { "" };
	}
	
	cutStart { |time=0|
		if( loop.not ) { 
			if( time < this.endSecond ) {
				this.startSecond = this.startSecond + time;
			};
		};
	}

	//a pseudo-ugen for UGens for allocating a buffer and reading a file into it.
	*kr{ |numChannels = 1, key|
		var bufnum, rate, loop;
		key = key ? 'buffer';
		#bufnum, rate, loop = key.asSymbol.kr( [ 0, 1, 0 ] );
		Udef.addBuildSpec( ArgSpec(key, nil, BufSndFileSpec(numChannels) ) );
		^bufnum
	}

}

MonoBufSndFile : BufSndFile {
	
	var <channel = 0;
	
	useChannels { ^[ channel ] }
	useChannels_ { |new| 
		this.channel = new.asCollection.first ? 0;
		this.changed( \useChannels, useChannels );
	}
	
	channel_ { |new = 0|
		channel = new.min( numChannels );
		this.changed( \useChannels, useChannels );
	}
	
	asMonoBufSndFile { 
		^this 
	}
	
	asBufSndFile {
		^BufSndFile.newCopyVars( this );
	}
	
	numChannels { ^1 }
	
	storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath.quote, numFrames, numChannels, sampleRate,
             startFrame, endFrame, rate, loop
		]  << ")" << if( this.channel != 0 ) { ".channel_(%)".format( this.channel ) } { "" };
	}
	
}

DiskSndFile : AbstractSndFile {
	
	var <>diskBufferSize = 32768;

	*new{ |path, startFrame = 0, endFrame, rate = 1, loop = false| 
		// path of existing file or SoundFile
		if( path.class == SoundFile ) {
			^this.newBasic( path.path, nil, nil, nil, startFrame, endFrame, rate, loop )
				.fromFile( path );
		} {
			^this.newBasic( path, nil, nil, nil, startFrame, endFrame, rate, loop).fromFile;
		};
	}
	
	asBufSndFile { 
		var unit; // pass unit on to new object
		unit = this.unit;
		this.unit = nil;
		^BufSndFile.newCopyVars( this ).unit_( unit ); 
	}
	
	asMonoBufSndFile {
		^MonoBufSndFile.newCopyVars( this );
	}
	
	asDiskSndFile { ^this }
	
	asControlInputFor { |server, startPos = 0| 
		^[ this.currentBuffer(server, startPos), rate, loop.binaryValue ] 
	}
	
	makeBuffer {  |server, startPos = 0, action, bufnum|  // startOffset in seconds
	    //endFrame not used
		var test = true;
		var buf, addStartFrame = 0;
		var actualStartFrame;
		
		if( numChannels.isNil ) { 
			test = this.prReadFromFile; // get numchannels etc.
		};
		
		if( test ) {
			
			if( startPos != 0 ) { addStartFrame = this.secondsToFrames( startPos ) };
			if( loop ) {
				 actualStartFrame = (startFrame + addStartFrame).wrap( 0, this.endFrame );
			} {
				actualStartFrame = startFrame + addStartFrame;
			};
			buf = Buffer.alloc(server, diskBufferSize.asInt, numChannels, { arg buffer;
				buffer.readMsg(path.getGPath, actualStartFrame.asInt,
					diskBufferSize.asInt, 0, true, {|buf|
						["/b_query", buf.bufnum]
					}
				);
			}).doOnInfo_(action).cache;
			this.addBuffer( buf );
			^buf;
		} {
			"DiskSndFile:prReadBuffer : file not found".warn;
		};
	}
	
	 freeBuffer { |buf, action|
		 if( UEvent.nrtMode != true ) {
			buf.checkCloseFree( action );
		 } {
			 buf.close;
			 buf.free;
			 action.value( buf );
		 };
		this.removeBuffer( buf );
	}

    unitNamePrefix{ ^"disk" }
    
    u_waitTime { ^1 }
	
}

+ Object {
	*newCopyVars { |obj, exclude|  // assumes object with similar keys, a subclass etc
		var correspondingVars, new;
		exclude = exclude.asCollection;
		new = this.newCopyArgs(); // set nothing, init nothing
		this.instVarNames.select({ |item| 
			exclude.includes( item ).not && {
				obj.class.instVarNames.includes( item );
			};
		}).do({ |item|
			new.instVarPut( item, obj.instVarAt( item ).copy );
		});
		^new;		
	}
}

+ Object {
	
	asBufSndFile { 
		^BufSndFile.newBasic(String.scDir +/+ "sounds/a11wlk01-44_1.aiff", 107520, 1, 44100, 0, nil, 1, false)
	}
	
	asMonoBufSndFile { 
		^MonoBufSndFile.newBasic(String.scDir +/+ "sounds/a11wlk01-44_1.aiff", 107520, 1, 44100, 0, nil, 1, false)
	}
	
	asDiskSndFile {
		^DiskSndFile.newBasic(String.scDir +/+ "sounds/a11wlk01-44_1.aiff", 107520, 1, 44100, 0, nil, 1, false)
	}
}


/*

(
Udef(\test, {
	Out.ar(0, PlayBuf.ar(1, (String.scDir +/+ "sounds/a11wlk01-44_1.aiff").asBufKr ) )
})
)

(
Udef(\test, {
	var buf = (String.scDir +/+ "sounds/a11wlk01-44_1.aiff").asBufKr(key: \buf1, numChannels:1, startFrame:0, endFrame:44100 );
	Out.ar(0, PlayBuf.ar(1, buf ) )
})
)

UChain(\test).gui
*/

+ String {
	asUnitArg { |unit, key|
		case { unit.getSpec( key ).isKindOf( BufSndFile ) } {
			^this.asBufSndFile.asUnitArg(unit, key)
		} { unit.getSpec( key ).isKindOf( DiskSndFile ) } {
			^this.asDiskSndFile.asUnitArg(unit, key)
		} {
			^this;
		};
	}
	
	asUnit {
		^this.asBufSndFile.asUnit;
	}
	
	asBufSndFile { 
		^BufSndFile( this );
	}
	
	asDiskSndFile {
		^DiskSndFile( this );
	}
	
	asPathFromServer { 
		
		// a relative path on the server is not the same as on the lang for standalone apps.
		// this method always returns a path that points to same file on both server and lang

		var scdir;
		if( [ $/, $~ ].includes( this[0] ).not ) {
			scdir = String.scDir;
			if( scdir != File.getcwd ) { // <- means this is a standalone app
				^scdir +/+ this;	
			} {
				^this
			};
		} {
			^this;
		}	
	}

	asBufKr{ |key, numChannels = 1, startFrame = 0, endFrame|
		key = key ? \buf;
		^key.ubufkr( this, startFrame = 0, endFrame, numChannels:numChannels)[0]
	}
}

/*


(
Udef(\test, {
	Out.ar(0, PlayBuf.ar(1, \sndfile.ubufkr()[0] ) )
})
)


(
Udef(\test, {
	var buf, rate, loop;
	#buf, rate, loop = \sndfile.ubufkr();
	Out.ar(0, PlayBuf.ar(1, buf, BufRateScale.kr( buf ) * rate, loop:loop ) )
})
)

(
Udef(\test, {
	var buf, rate, loop;
	#buf, rate, loop = \sndfile.ubufkr(String.scDir +/+ "sounds/a11wlk01-44_1.aiff", 0, 44100*2, 0.5, true);
	Out.ar(0, PlayBuf.ar(1, buf, BufRateScale.kr( buf ) * rate, loop:loop ) )
})
)

UChain(\test).gui



(
Udef(\test, {
	Out.ar(0, DiskIn.ar(1, \sndfile.udiskkr()[0] ) )
})
)


(
Udef(\test, {
	var buf, rate, loop;
	#buf, rate, loop = \sndfile.udiskkr();
	Out.ar(0, VDiskIn.ar(1, buf, BufRateScale.kr( buf ) * rate, loop ) )
})
)

(
Udef(\test, {
	var buf, rate, loop;
	#buf, rate, loop = \sndfile.udiskkr(String.scDir +/+ "sounds/a11wlk01-44_1.aiff", 0, 44100*2, 0.5, true);
	Out.ar(0, VDiskIn.ar(1, buf,  BufRateScale.kr( buf ) * rate, loop ) )
})
)

UChain(\test).gui

*/

+ Symbol {

	ubufkr{ |path, startFrame = 0, endFrame, rate = 1, loop = false,
		useChannels, numChannels = 1|

		var default = path !? { |p| BufSndFile(path, startFrame, endFrame,
			rate, loop, useChannels, numChannels) };
		var spec = BufSndFileSpec( numChannels );

		Udef.addBuildSpec( ArgSpec(this, default, spec ) );

		//bufnum, rate, loop
        ^this.kr([ 0, 1, 0 ])
	}


	udiskkr{ |path, startFrame = 0, endFrame, rate = 1, loop = false,
		useChannels, numChannels = 1|

		var specDefault = path !? { |p| DiskSndFile(path, startFrame, endFrame,
			rate, loop) };
		var spec = DiskSndFileSpec( numChannels );

		//bufnum, rate, loop
        ^this.ukrArgSpec([ 0, 1, 0 ], specDefault, spec )
	}

}