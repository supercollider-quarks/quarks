

HardShaper  {

	*ar { arg audio=0.0,drive=1;
		var ab;
		ab = abs(audio);
		^audio*(ab + drive)/(audio ** 2 + (drive - 1) * ab + 1) 
	}
	*kr { arg input=0.0,drive=1;
		^this.ar(input,drive)
	}

}


// PingPong specified in beats
PingPongT {

	//your buffer should be nil or the same numChannels as your inputs
	*ar { arg  bufnum,  inputs, beats, feedback=0.7, wobble=0.0, rotate=1,tempo=1.0;
	
		var delayedSignals, outputs,offset;
		bufnum = bufnum ?? {LocalBuf( 44100, inputs.numChannels)};
		delayedSignals = PlayBuf.ar(inputs.numChannels,bufnum,1.0,1.0,0.0,1.0);
		
		if(delayedSignals.numChannels > 1,{
			outputs = delayedSignals.rotate(rotate) * feedback + inputs;
		},{
			outputs = delayedSignals * feedback + inputs;
		});
		if(wobble != 0.0,{ wobble = LFNoise1.kr(0.1,wobble); });
		offset = (tempo.reciprocal * beats + wobble) * 44100;

		// feedback to buffers		
		RecordBuf.ar(outputs,bufnum,offset,1.0,0.0,1.0,1.0,1.0);
		
		^outputs
	}
}


PingPongTP  {
    
	*ar { arg input,process, maxBeats, beats,feedback,wobble,tempo=1.0,bufnum;
	
		var actDelayTime;
		var output,delayedSignal;
		
		bufnum = bufnum ?? {LocalBuf( 44100, input.numChannels)};
		
		actDelayTime = 
			if(wobble.notNil,{
				beats * tempo.reciprocal + LFNoise1.kr(0.3,wobble)
			},{
				beats * tempo.reciprocal
			});
		

		delayedSignal = Tap.ar( bufnum, 2 , actDelayTime);
//		PlayBuf.ar( 2, bufnum, 1.0,1.0,
//			actDelayTime * BufSampleRate.kr(bufnum) ,1.0);
		
		input = NumChannels.ar(input,2); // force stereo
		// mix the delayed signal with the input
		output = process.value(delayedSignal) + input;
		
		RecordBuf.ar(output, bufnum,recLevel: feedback)
		^output
	}
}





// delay specified in beats
DelayT {
	// wobble in seconds
	*ar { arg input, maxBeats, beats,wobble, tempo;
		^DelayL.ar(input,
			40.reciprocal * maxBeats,// can't go below 40 bpm
			if(wobble.notNil,{
				beats * tempo.reciprocal + LFNoise1.kr(0.3,wobble)
			},{
				beats * tempo.reciprocal
			})
		)
	}
	*kr { arg input, maxBeats, beats,wobble,tempo;
	
		^DelayL.kr(input,
			40.reciprocal * maxBeats,// can't go below 40 bpm
			if(wobble.notNil,{
				beats * tempo.reciprocal + LFNoise1.kr(0.3,wobble)
			},{
				beats * tempo.reciprocal
			})
		)
	}

}




SinChorus { // different if odd or even voices!

	*ar { arg input,voices=4,maxDelay=0.01,speed=0.1;
		var mul1,mul2,add,vol;
		add = maxDelay / 2;
		mul1 = add - 0.0001;
		mul2 = mul1.neg;
		vol = voices.reciprocal;
		^Mix.fill(voices,{arg i;
			DelayL.ar(input,maxDelay,[
								SinOsc.kr(speed,2pi / voices * i ,mul1,add ),
								SinOsc.kr(speed,2pi / voices * i ,mul2,add )
				],
				vol )
		})
	}

}


NoiseChorus {

	*ar { arg input,voices=8,maxDelay=0.1,speed=0.5;
		var vol;
		vol = voices.reciprocal;
		^Mix.fill(voices,{arg i;
			DelayL.ar(input,maxDelay,LFNoise2.kr(speed,maxDelay / 2 - 0.0001,maxDelay / 2 , vol) )
		})
	}
}


StereoNoiseChorus {

	*ar { arg input,voices=8,maxDelay=0.1,speed=0.5;
		var vol;
		vol = voices.reciprocal;
		^Mix.fill(voices,{arg i;
			DelayL.ar(input,maxDelay,[LFNoise2.kr(speed,maxDelay / 2 - 0.0001,maxDelay / 2 ) ,
								LFNoise2.kr(speed,maxDelay / 2 - 0.0001,maxDelay / 2 ) ],
								vol)
		})
	}
}



CombAChorus { // different if odd or even voices!

	*ar { arg input,voices=4,maxDelay=0.01,speed=0.1,bidecay=0.1,height=1.0;
		var mul1,mul2,add,vol;
		add = maxDelay / 2;
		mul1 = add - 0.0001 * height;
		mul2 = mul1.neg;
		vol = voices.reciprocal;
		^Mix.fill(voices,{arg i;
			CombL.ar(input,maxDelay,[
								SinOsc.kr(speed,2pi / voices * i ,mul1,add ),
								SinOsc.kr(speed,2pi / voices * i ,mul2,add )
				],
				bidecay,
				vol )
		})
	}
}



CombNChorus { // different if odd or even voices!

	*ar { arg input,voices=4,maxDelay=0.01,speed=0.1,bidecay=0.1,height=1.0;
		var mul1,mul2,add,vol;
		add = maxDelay / 2;
		mul1 = add - 0.0001 * height;
		mul2 = mul1.neg;
		vol = voices.reciprocal;
		^Mix.fill(voices,{arg i;
			CombL.ar(input,maxDelay,[
								SinOsc.kr(speed,2pi / voices * i ,mul1,add ),
								SinOsc.kr(speed,2pi / voices * i ,mul2,add )
				],
				bidecay,
				vol )
		})
	}
}

// ideas:
// height that works better
// different waves for modulator
// shepard tone combs

/* 

MultiPitchShift { //wrong!
			// you want the qnty'th root of pchRatio
			
	*ar { arg qnty=2, in = 0.0, winSize = 0.2, pchRatio = 1.0, 
				pchDispersion = 0.0, timeDispersion = 0.0;
		
		qnty.do({
		 	in =	PitchShift.ar(in,winSize,pchRatio / qnty,pchDispersion,timeDispersion)
		});
		^in
	}

}
*/




Surf {

	*kr {  arg min=0.0,max=1.0,tideFreq=0.01,waveFreq = 0.05, tideEffect=0.5;

		var wave,tide,halfrange;
		halfrange = max - min * 0.5;
		tide = FSinOsc.kr(tideFreq,0,halfrange,halfrange + min) * tideEffect;
		wave = FSinOsc.kr(waveFreq,0,halfrange, halfrange + min) * (1 - tideEffect);
		^(wave + tide)
	}
	*ar {  arg min=0.0,max=1.0,tideFreq=0.1,waveFreq = 440.0, tideEffect=0.5;

		var wave,tide,halfrange;
		halfrange = max - min * 0.5;
		tide = FSinOsc.ar(tideFreq,0,halfrange,halfrange + min) * tideEffect;
		wave = FSinOsc.ar(waveFreq,0,halfrange, halfrange + min) * (1 - tideEffect);
		^(wave + tide)
	}
}
// n Turf ?


Reverberator3 {
    
    // basic configurable reverberator 
    
	*ar {
		 arg input, //stereo or mono
		 	revBalance=0.0,// 0..1
		 	
		 	revTime=4,
		 	taps=10,
		 	combs=6,
		 	allpasses=4,
		 	tapsMin=0.01,
		 	tapsMax=0.07,
		 	combsMin=0.5,
		 	combsMax=0.3,
		 	allpassMin=0.01,
		 	allpassMax=0.05,
		 	allpassDecay=1.0;
		
		var 	tapsOut,out,tapsLevelMax,combinput,combLevelMax;

		if(taps > 0,{
			tapsLevelMax = taps.reciprocal;
	
			tapsOut = 
				Mix.fill(taps,{
					var delays;
					delays = rrand(tapsMin,tapsMax);
					DelayN.ar(input,delays,delays,tapsLevelMax.rand)
				}) + input
		},{
			tapsOut = input;
		});
		
		
		// 0 possible
		combs.do({ arg ci;
			var times;
			times = rrand(combsMin,combsMax);
			if(combinput.isNil, { //first time thru
				combLevelMax = combs.reciprocal; // first time, else possible div by 0
				combinput = tapsOut;
				out = CombC.ar(  
							combinput,times,times,
							revTime,combLevelMax.rand)
			},{
				out = out + CombC.ar( 
							combinput,times,times,
							revTime,combLevelMax.rand)
			})
		});
		
		// 0 possible
		allpasses.do({ arg i;
			var times;
			times = 	[rrand(allpassMin,allpassMax),rrand(allpassMin,allpassMax)];

			if(out.notNil,{
		 		out = AllpassC.ar(
			 			out,
					 	// first time thru its possibly still mono, 
		 				// then it expands to stereo and each subsequent is paired up to that expansion
		
			 			times,
			 			times,
			 			allpassDecay) 
			
		 	},{ // no taps or combs
		 		out = AllpassC.ar(
		 			input, 
					 	// first time thru its possibly still mono, 
		 				// then it expands to stereo and each subsequent is paired up to that expansion
		 			times,
			 		times,
		 			allpassDecay) 
		 	})
		 });
		^XFader.ar(input,out,revBalance)
	}
	
}



+ UGen {
    // doesn't work for demand rate
    blend { arg that, blendFrac = 0.5;
        // blendFrac should be from zero to one
        ^this + (blendFrac * (that - this));
    }
}




