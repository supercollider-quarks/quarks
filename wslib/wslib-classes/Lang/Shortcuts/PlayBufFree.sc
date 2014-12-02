// wslib 2005
// convenience classes for PlayBuf

// obsolete in SC3.3, but still here for backward compat

PlayBufFree {
	// simple shortcut:
	// play a buffer once and then free synth
	// loop = 1 will keep the Synth running. When loop is set to 0 afterwards the 
	// buffer will play to the end and then the synth will be free-ed.
	
	*ar { arg numChannels, bufnum=0, rate=1.0, trigger=1.0, startPos=0.0, loop = 0.0;
		var out;
		this.deprecated( thisMethod, Meta_PlayBuf.findMethod(\ar) );
		out = PlayBuf.ar( numChannels, bufnum, rate, trigger, startPos, loop);
		FreeSelfWhenDone.kr( out );
		^out;
		}
		
	*generateSynthDef { arg numCha = 1; // fast synthdef for instant buffer playback
		^SynthDef("PlayBufFree", { arg bufnum = 0, pan=0, level=1, rate=1, gate=0;  //gate = loop
			Out.ar(0, 
				Pan2.ar(
					PlayBufFree.ar(numCha, bufnum, rate, loop: gate) 
				* level, pan) ); 
			}).load(Server.default);
	}
}

PlayBufLoop {
	// dirty way to make a buffer loop between two points using a trigger
	// when endPos < startPos the buffer will play backwards
	
	*ar {  arg numChannels, bufnum=0, rate=1.0, startPos=0.0, endPos=1.0, loop = 0.0, keepLoop = 0;
		var out, trigger;
		trigger = Impulse.ar( rate.abs * ( (1 / (BufDur.kr(bufnum) * (endPos - startPos)
			.abs.max(0.000000001))) 
			).min(20000) * loop.round(1) );
		^PlayBuf.ar( numChannels, bufnum, rate * (endPos - startPos).sign, 
			trigger, startPos * BufFrames.kr(bufnum), keepLoop);
		}
	}
		
	