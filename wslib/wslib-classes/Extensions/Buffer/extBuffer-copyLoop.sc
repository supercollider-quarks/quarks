// wslib 2007 - copy a part of a buffer to another buffer, loop if index out of range

+ Buffer {
	copyLoop { arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1;
		var srcPointer, dstPointer, numSamplesLeft, numSamplesThisTime;
		if( numSamples.isNegative ) { numSamples = buf.numFrames - dstStartAt };
		srcStartAt = srcStartAt.wrap(0, numFrames );
		numSamplesLeft = numSamples;
		srcPointer = srcStartAt;
		dstPointer = dstStartAt;
		while { numSamplesLeft > 0 } { 	
			numSamplesThisTime = numSamplesLeft.min( numFrames - srcPointer );
			this.copyData( buf, dstPointer, srcPointer, numSamplesThisTime );
			//"copied: %,%,%\n".postf( dstPointer, srcPointer, numSamplesThisTime );
			dstPointer =  dstPointer + numSamplesThisTime;
			numSamplesLeft = numSamplesLeft - numSamplesThisTime;
			srcPointer = 0;
		};
	}
	
	copyLoopTo { arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1, action;
		{	
			this.server.sync( nil, 
				this.server.makeBundle( false, {
					this.copyLoop( buf, dstStartAt, srcStartAt, numSamples );
				})
			);
			action.value( this );
		}.forkIfNeeded;
	}
	
	copyLoopFrom { arg buf, dstStartAt = 0, srcStartAt = 0, numSamples = -1, action;
		buf.copyLoopTo( this, dstStartAt, srcStartAt, numSamples, action ); 
	}
	
	
	// create a crossfade at the end of the buffer for a seemless loop
	// this takes apx. as long as the duration of the crossfade (0.25s default)
	
	copyLoopCF {	arg buf, startFrom = 0, fadeTime = 0.25, numSamples = -1, action;
		fadeTime = fadeTime.min( buf.duration );
		this.copyLoopTo( buf, 0, startFrom + (this.sampleRate * fadeTime), action: {
			{	
				var numChannels;
				numChannels = buf.numChannels;	
				
				this.server.sync( nil, 
					this.server.makeBundle( false, {
						SynthDef( "wslib_cf_buffer_%".format(numChannels), { |dur = 0.25, 
								tobuf = 0, frombuf = 1, toStartframe = 0, fromStartframe = 0|
							var mix;
							mix = Line.ar( 0, 0.5pi, dur, doneAction:2 );
							RecordBuf.ar( 
								PlayBuf.ar( numChannels, frombuf, 
									startPos: fromStartframe, loop:1 ),
								tobuf, toStartframe, mix.sin, mix.cos );
						}).send(this.server);
					} ) 
				);
				
				Synth( "wslib_cf_buffer_%".format(numChannels), 
						[ \dur, fadeTime, \tobuf, buf, \frombuf, this, 
						  \toStartframe, buf.numFrames - (44100 * fadeTime),
						  \fromStartframe, startFrom
						], this.server )
					.freeAction_({ action.value });
			}.fork;
		} );
	}
	
}