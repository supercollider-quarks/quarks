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

PartConvBuffer : AbstractRichBuffer {

	classvar <>fftSize = 2048; // changing this requires all data files to be rewritten
	var <path;
		
	*new{ |path, numFrames|
		^super.new(numFrames).path_( path, true ); // update from path
	}
	
	*newBasic { |path, numFrames|
		^super.new(numFrames).path_( path ); // don't update from path
	}

	shallowCopy{
        ^this.class.new(path, numFrames, fftSize);
	}

	fromFile { |soundfile|
		if( this.prReadFromFile( soundfile ).not ) { 
			"%:initFromFile - could not open file '%'\n".postf( this.class, path.basename ) 
		}
	}
	
	prReadFromFile { |soundfile|
		var test = true;
		if( soundfile.isNil or: { soundfile.isOpen.not and: { path.notNil }}) {
			soundfile = soundfile ?? { SoundFile.new }; 
			if( path.notNil ) {
				test = soundfile.openRead( path.getGPath.asPathFromServer );
				soundfile.close; // close if it wasn't open
			} {
				test = false;
			};
		};
		if( test ) {	
			this.numFrames = soundfile.numFrames;
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
	
	makeBuffer { |server, startPos = 0, action, bufnum|
		var buf;
		if( path.notNil ) {
			buf = Buffer.read( server, path.getGPath, 0, -1, action, bufnum );
			this.addBuffer( buf );
			^buf;
		} {
			action.value;
			^nil;
		};
	}
	
	
	*convertIRFile { |inPath, outPath, server, action, channel = 0|
		var i = 0;
		server = (server ? Server.default).asCollection;
		if( inPath.isString.not ) { 
			outPath = outPath ?? { 
				"~/Desktop/%.partconv".format( Date.localtime.stamp ).getGPath
			};
		} {
			outPath = (outPath ?? { inPath.replaceExtension( "partconv" ) }).getGPath;		};
		server.do({ |srv|
			var buf, irbuf, bufsize, cond;
			{
				if( inPath.isString ) {
					buf = Buffer.readChannel( srv, inPath.getGPath, channels: [channel] );
					srv.sync;
				} {
					cond = Condition(false);
					buf = Buffer.sendCollection( srv, inPath, action: { 
						cond.test = true;
						cond.signal;
					});
					cond.wait;
				};

				bufsize = PartConv.calcBufSize(fftSize, buf);
				irbuf = Buffer.alloc(srv, bufsize, 1);
				irbuf.preparePartConv( buf, fftSize );
				srv.sync;
				buf.free;
				irbuf.write( outPath, "aiff", "float" );
				srv.sync;
				irbuf.free;
				i=i+1;
				if( server.size == i ) { action.value( outPath ) };
			}.fork;
		});
		^outPath;
	}
	
	*generateDanStowelIR { |dur = 1.3, outPath| // write to local file if outPath.notNil
		var ir, sf;
		//synthesise the honourable 'Dan Stowell' impulse response
		ir = ((0..dur*44100).linlin(0,dur*44100,1,0.125).collect({ |f| 
				f = f.squared.squared; 
				f = if( f.coin ) { 0 } { f.squared }; 
				f = if( 0.5.coin ) { 0-f } { f }; 
			})
		) * (-27.dbamp);
		if( outPath.isNil ) {
			^ir
		} {
			ir = ir.as(FloatArray);
			sf = SoundFile.new;
			outPath = outPath.getGPath;
			if(sf.openWrite(outPath), {
				sf.writeData(ir);
				sf.close;
				^outPath;
			}, {
				"%:generateDanStowelIR : Failed to write data".format( this ).warn; 
				^nil
			}
			);
		};
	}
	
	framesToSeconds { |frames = 0|  
		^frames !? { (frames / (sampleRate ? 44100)) / 2 } // overlap = 2
	}
	secondsToFrames { |seconds = 0| 
		^seconds !? { seconds * 2 * (sampleRate ? 44100) } 
	}
	
	duration { ^this.framesToSeconds( numFrames ) }
	
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
	
	// utilities
	
	plot { this.asSoundFile.plot; } // plots the raw data (a sequence of fft frames)
	
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
	
	asPartConvBuffer { ^this }
	
    printOn { arg stream;
		stream << this.class.name << "(" <<* [
		    	path, numFrames
		]  <<")"
	}

    storeOn { arg stream;
		stream << this.class.name << ".newBasic(" <<* [ // use newBasic to prevent file reading
		    path.formatGPath !? _.quote, numFrames
		]  <<")"
	}
}


+ Object {
	
	asPartConvBuffer { 
		^PartConvBuffer.newBasic(nil, 0)
	}
}

+ String {
	
	asPartConvBuffer { 
		^PartConvBuffer.new( this )
	}
}