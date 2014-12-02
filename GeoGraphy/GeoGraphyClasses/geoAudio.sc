// GeoAudio is an abstract class to be subclassed
// in order to have an audio device for GeoGraphy

GeoAudio {

// CORE
//////////////////////////////////////////////////////////////////	// a geographic sequencer
	var <>runner, <>server ;
	
	// needs a graph and a runner
	*new { arg runner ; 
		^super.new.initGeoAudio(runner) ; 
	}

	initGeoAudio { arg aRunner ;
		runner = aRunner ;
		runner.addDependant(this) ;
		server = Server.local.boot ;
		server.doWhenBooted({
			this.sendDef ;
			})
	}


	update { arg theChanged, theChanger, more;
		if (more[0] == \actant, 
			 { this.play( more[1..] ) }) // useless to pass the msgID
	}

//////////////////////////////////////////////////////////////////

// dummy methods to be overwritten
	initAudio { "init done".postln }

// here you associate to aDef a synthDef
	sendDef { var aDef ; aDef.send(server) ;
	}

// here some mappings starting from more array
	play { arg more ;
		more.postln ;
	}

}



//  Examples

// 1. An impulsive sinusoidal generator
// label are used to extract pitch value
// use format like "s60" (pitch --> 60, 60 is extracted)

Sinusoider : GeoAudio {
	
	// initAudio is not necessary here
	
	// we assume pitch as midi notation
	sendDef {
		//the synthDef being sent
	 	var aDef  = SynthDef(\Sinusoider, { arg pitch, amp = 0.1, out = 0 ;
	 		var dur = 30/pitch ;
			Out.ar(out, Pan2.ar(
				EnvGen.kr(Env.perc(dur*0.01, dur, 1, -8), 1.0, doneAction:2) 
				*
				SinOsc.ar(pitch.midicps),
				LFNoise1.kr(1/dur),
				amp
				)
			)}) ;
		aDef.send(server) ;
	}


	play { arg message ; 
		var label, weight, offsetWeight, amp ;
		label 	= message[1][3].asString ;
		weight 	= message[5] ;
		offsetWeight = message[6] ; 
		amp = (weight+offsetWeight).thresh(0) ;
		if ( label[0].asSymbol == \s, {
			label = label[1..].asFloat ;
			 Synth.new(\Sinusoider, [\pitch, label, \amp, amp]) 
		})
	}

}



//  Examples

// 1b. The same, just for comparison, using a square
// It reacts to a different format: e.g. "q60" (pitch --> 60, 60 is extracted)

Squarer : GeoAudio {
	
	// initAudio is not necessary here
	
	// we assume pitch as midi notation
	sendDef {
		//the synthDef being sent
	 	var aDef  = SynthDef(\Squarer, { arg pitch, amp = 0.1, out = 0 ;
	 		var dur = 30/pitch ;
			Out.ar(out, Pan2.ar(
				EnvGen.kr(Env.perc(dur*0.01, dur, 1, -8), 1.0, doneAction:2) 
				*
				Pulse.ar(pitch.midicps, 0.5),
				LFNoise1.kr(1/dur),
				amp
				)
			)}) ;
		aDef.send(server) ;
	}


	play { arg message ; 
		var label, weight, offsetWeight, amp ;
		label 	= message[1][3].asString ;
		weight 	= message[5] ; 
		offsetWeight = message[6] ;
		amp = (weight+offsetWeight).thresh(0) ; 
		if ( label[0].asSymbol == \q, {
			label = label[1..].asFloat ;
			 Synth.new(\Squarer, [\pitch, label, \amp, amp]) 
		})
	}

}


// 2. a sampler
// create a Dict of buffers loading all the audio files in samplesPath and in its subdirs
// so that they can be referred via labels
// a label "hihat" means: play hihat.* file

GraphSampler : GeoAudio  {

	var <>bufDict, <>samplesPath ;

	initAudio { arg aSamplesPath ;
		var p, l, name ;
		samplesPath = aSamplesPath ;
		bufDict = IdentityDictionary.new ;	
		server.doWhenBooted({
			p = Pipe.new("ls -R" + samplesPath, "r") ;				l = p.getLine ;
			while({l.notNil}, {
				case 
				{ l.contains("/") }
					{ samplesPath = l.split($:)[0] }
				{ (l.contains("/").not).and(l.contains(".")) }
					{ name = (l.split($.)[0].asSymbol).postln ;
					bufDict.add(name 
						-> Buffer.read(server, samplesPath++"/"++l))} ;
				l = p.getLine; 
				}) ;	
			p.close ;
			})
	}
	
	sendDef {
		//the synthDef being sent
	 	var aDef = SynthDef(\graphSampler, { arg bufnum, amp = 1, out = 0, dur = 1 ;
	 		
			Out.ar(out, Pan2.ar(
				Line.kr(amp, amp, dur, doneAction:2) 
				*
				PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), loop: 0),
				LFNoise1.kr(1/dur)
				)
			)}) ;
		aDef.send(server) ;
	}

	play { arg message ;
		var label, weight, offsetWeight, dur, amp ;
		label = message[1][3] ;
		weight = message[5] ; 
		offsetWeight = message[6] ;		
		amp = (weight+offsetWeight).thresh(0) ; 
		if (bufDict[label] != nil,  
			{ 	dur = bufDict[label].numFrames/server.sampleRate ;
				Synth.new(\graphSampler, 
				[\bufnum, bufDict[label].bufnum, \amp, amp, \dur, dur]) }
			// ,{ "NO SAMPLE WITH THIS NAME".postln }
			 )
		}
}

// 2b. a slice player
SlicePlayer : GeoAudio  {

	var <>buf ;

	initAudio { arg aSamplePath = "sounds/a11wlk01.wav" ;
		server.doWhenBooted({
			buf = Buffer.read(server, aSamplePath) 
		}) 
	}
	
	sendDef {
		//the synthDef being sent
	 	var aDef = SynthDef(\slicePlayer, 
	 		{ arg bufnum, amp = 1, out = 0, dur = 1, startPos = 0, rate = 0 ;
			Out.ar(out, Pan2.ar(
				Line.kr(amp, amp, dur, doneAction:2) 
				*
				PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum), 
							rate: rate, startPos: startPos, loop: 0),
				LFNoise1.kr(1/dur)
				)
			)}) ;
		aDef.send(server) ;
	}
	
	play { arg message ;
		var vID, x, y, label, weight, offsetWeight, amp, dur, startPos, rate ;
		vID = message[0] ;
		x = message[1][0] ;
		y = message[1][1] ; 
		label = message[1][3].asString ;
		weight = message[5] ; 
		offsetWeight = message[6] ;		
		amp = (weight+offsetWeight).thresh(0) ;	 	
		startPos = 0 ;
		rate = 1 ;
		if (label[0].asSymbol == \p,  
			{ 	dur = vID * 0.015 ; // the more the vertices the longest the dur
				// spatial mapping
				startPos = buf.numFrames*0.00125 * y  ; // means: /800
				rate = x/1200*2 ; 
				Synth.new(\slicePlayer, 
				[\bufnum, buf.bufnum, \amp, amp, \dur, dur, \startPos, startPos, \rate, rate]) }
			// ,{ "NO SAMPLE WITH THIS NAME".postln }
			 )
		}
	
	
}
