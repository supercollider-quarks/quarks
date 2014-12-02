

Instr.orc(\timeDomainEfx,[

	\PitchShift,{
		arg audio,pchDispersion=1.01,timeDispersion=1.01,pchRatio=0.5,winSize=0.2;
		PitchShift.ar(
			audio,
			winSize,
			pchRatio,
			pchDispersion,
			timeDispersion
		)
	},

	\qAllpassA,{	arg audio,decay=1,maxDelay=0.3,qnty=4,chanDiff=0.1;

			(qnty.asInteger).do({
			 		var x;
			 		audio =
	 				AllpassL.ar(audio, maxDelay,
		 				[rrand(0.01,maxDelay),rrand(0.01,maxDelay)],
		 				decay)
		 		 });
			audio
	},

	\qAllpassL,{	arg audio,decay=1,maxDelay=0.3,qnty=4;

			(qnty.asInteger).do({
			 		var x;
			 		audio =
	 				AllpassL.ar(audio, maxDelay,
		 				[rrand(0.01,maxDelay),rrand(0.01,maxDelay)],
		 				decay)
		 		 });
			audio
	}

//	\fftcomber,{
//		arg audio,delay,decay;
//
//		FFTfilter.filter(audio,// mono ?
//		{ arg in, mindelay,maxdelay;
//
//			//mindelay=0.1;
//			//maxdelay=0.9;
//			CombN.ar(in, maxdelay, delay * (maxdelay - mindelay) + mindelay,  decay * 8 - 4, 1, in);
//		 });
//	},
//


],
	\stereoEffect
);


Instr([\timeDomainEfx,\CombsAllPasses],{arg audio,delay=0.1,decay=15,combs=7,
				maxDelay=0.5,allpasses=4,allpassDecay=1;

	var y;
	y = Mix.fill(combs,{ CombL.ar(audio, maxDelay,
						delay,
						decay)
			});
	allpasses.do({ y = AllpassL.ar(y, 0.05, [0.05.rand, 0.05.rand], allpassDecay) });
	y
},#[
	\audio,
	\delay,
	\decay,
	\qnty,
	\maxDelay,
	\qnty,
	\decay
],
	\stereoEffect
);

// something is wrong here
// it attacks
/*
Instr([\timeDomainEfx,\plate],{ arg audio,feedback=0.8,maxDelay=0.3,
				qnty=4,bicoef=0.0;

	// no sound yet ?

	var  buffer,tapTimes, delayedSignals, filteredDelays, output;
	var input;
	buffer = LocalBuf(0.5*44100,2);
	// assign random tap times
	tapTimes = Array.rand(qnty, 0.015, maxDelay);

	delayedSignals = Mix.fill(qnty,{ arg i;
		PlayBuf.ar(2,
				buffer,
				startPos: tapTimes.at(i) * 44100.0)
	});

	// filter taps
	filteredDelays =
				OnePole.ar(
					delayedSignals * feedback
					,bicoef
				);

	input = filteredDelays + audio;
	input = NumChannels.ar(input,2);

	// write to delay lines
	RecordBuf.ar(filteredDelays + audio,buffer);

	filteredDelays

},[
	\audio,
	\unipolar,
	\maxDelay,
	\qnty,
	\bipolar
],
	\stereoEffect
);
*/
/*.test(
	Patch({
		Decay2.ar(Impulse.ar([0.6,0.5], 0.25), 0.01, 0.25,
		SinOsc.ar(SinOsc.kr(3.7,0,200,500)));
	})
)*/


Instr.orc(\timeDomainEfx,[

	// based on a wrong explanation of aphex exciter !
	// sounds bright though
	\exciterN,{ arg audio,microDelay,maxDelay=0.2,ffreq=60,drive=1.0,thru=0.0;
		if(thru != 0.0,{ thru = audio * thru });
		DelayN.ar( HPF.ar(audio,ffreq,drive), maxDelay ,microDelay, 1.0, thru)
	},
	\exciterL,{ arg audio,microDelay,maxDelay=0.2,ffreq=60,drive=1.0,thru=0.0;
		if(thru != 0.0,{ thru = audio * thru });
		DelayL.ar( HPF.ar(audio,ffreq,drive), maxDelay ,microDelay , 1.0, thru)
	},
	\stereoExc,{ arg audio,microDelay,maxDelay=0.2,ffreq=4000,drive=5.0,panDepth,thru=0.0;
		if(thru != 0.0,{ thru = audio * thru });
		DelayL.ar( HPF.ar(audio,ffreq,drive), maxDelay ,[microDelay,(microDelay - panDepth).max(0.001)] ,
			1.0, thru)
	},

	\stereo2Exc,{ arg audio,microDelay,maxDelay=0.2,ffreq=1000,drive=5.0,panDepth=0.05,lfo=1.0,decay=0.1,thru=0.0;
		var panner;
		if(thru != 0.0,{ thru = audio * thru });

		panner=FSinOsc.kr(lfo,panDepth);
		microDelay = microDelay;

		CombL.ar(
			HPF.ar(audio,ffreq,drive).softclip,
			 maxDelay ,
			 [max(0.001,microDelay + panner),max(0.001,microDelay - panner)] ,
			 decay,
			 1.0,
			  thru
		)

	}
],
	\stereoEffect
);



Instr.orc(\timeDomainEfx,[

	\ptchDustFlip,{
		arg audio,
			pchRatio=0.5,density=5,winSize=0.2,pchDispersion=0.01,lag=0.01;

		var  pitched,flippy;

		flippy=Lag.kr(ToggleFF.kr(Dust.kr(density)) ,lag,pchRatio,0.5);

		pitched = PitchShift.ar(audio, winSize, flippy, pchDispersion);
		pitched
	},
	\ptchToggFlip,{
		arg audio,
			pchRatio=0.5,threshold=0.8,winSize=0.2,pchDispersion=0.01,lag=0.01;

		var  pitched,flippy;

		flippy=Lag.kr(ToggleFF.kr( Amplitude.kr(Mono(audio)) > threshold) ,lag,pchRatio,0.5);

		pitched = PitchShift.ar(audio, winSize, flippy, pchDispersion);
		pitched
	},
	\ptchThreshFlip,{
		arg audio,
			pchRatio=0.5,threshold=0.8,winSize=0.2,pchDispersion=0.01,lag=0.01;

		var  pitched,flippy;

		flippy=Lag.kr(Amplitude.kr(Mono(audio)) > threshold ,lag,pchRatio,0.5);

		pitched = PitchShift.ar(audio, winSize, flippy, pchDispersion);

		pitched
	}
],
	\stereoEffect
);


Instr([\timeDomainEfx, \geomMultiTap],{ arg audio=0.0,qnty=4,tdelay=1.0,tdecay=0.7,adecay=0.8,buffer;

    buffer = buffer ?? {LocalBuf(44100,2)};
	audio = NumChannels.ar(audio, 2);
	MultiTap.ar((Array.geom(qnty,tdelay,tdecay).max(0.00290249)),
				(Array.geom(qnty,1.0,adecay)),
				audio,
				1,
				0,
				buffer.bufnumIr)

},[
	\audio,
	StaticIntegerSpec(2,10),
	StaticSpec(0.05,2.0),
	StaticSpec(0.001,2.0),
	StaticSpec(0.01,2.0)
],
	\stereoEffect
);



Instr([\timeDomainEfx,\tempoPing],{ arg audio, feedback,tempoFactor,buffer,tempo;
    // buffer optional
	var out;
	audio = NumChannels.ar(audio,2);

	out=PingPongT.ar(buffer !? {buffer.bufnumIr},audio, 2, feedback, 0.0, 1,tempo);
	out
},
	nil,// arg names are good enough
	\stereoEffect
);



Instr([\timeDomainEfx, \PingPongT],{ arg audio=0.0 ,maxBeats=0.5, beats=0.375,feedback=0.4,wobble=0.05,tempo,buffer;

		audio = NumChannels.ar(audio,2);
		PingPongT.ar(buffer !? {buffer.bufnumIr},audio,beats,feedback,wobble,1,tempo)
},[
	\audio,
	StaticSpec(0.125,1.0),
	[0.0125,1.0],
	[0,1],
	[0,0.25]
],\stereoEffect);



Instr([\timeDomainEfx, \DelayT],{ arg audio=0.0 ,maxBeats=0.5, beats=0.375,wobble=0.05,tempo=1.0;

		DelayT.ar(audio,maxBeats,beats,wobble,tempo)
},[
	\audio,
	StaticSpec(0.125,1.0),
	[0.0125,1.0],
	[0,0.25]
],\stereoEffect);


/*
	// forces mono, splays to stereo
	Instr([\efxTimeDomain3,\multiGranny],{ arg audio,
			speed=1.0,wobble=0.1,pchRatio=1.0,bleed=0.5,qnty=2,halfmedian=0,maxDelay;

		var buffer,size,out,halfsize;

		audio = NumChannels.ar(audio,1);
		buffer = Buffer.for(audio,maxDelay);
		size = maxDelay * 44100; // buffer.first.size - 2 div: 2 ;

		wobble = size * wobble;
		halfsize = size * 0.5;

		RecordBuf.ar(buffer,audio,1 - bleed,bleed,0.0,1,1.0);

		out=Splay.arFill(qnty,{
			PlayBuf.ar(buffer,44100,
				pchRatio, LFNoise1.kr(speed,wobble,halfsize)// wobble of zero sits in the middle
			)
		});

		Median.ar(halfmedian * 2 + 1,out); // cheaply remove the glitches
	},[
		//arg audio,speed=1.0,wobble=0.1,pchRatio=1.0,bleed=0.5,qnty=2,halfmedian=0,maxDelay;
			nil,   nil,	nil,		nil,			nil,	   nil,  StaticSpec(0,11,\linear,1)

	]);

*/


//	Instr([\timeDomainEfx,\multFeedRLPF],{arg audio,
//		drive=0.4,ffreq=3000,rq=0.5,qnty=4,freqOffset=33,delay=0.25;
//
//		var buffer;
//		buffer = Buffer.for(audio,0.5);
//		MultiTapDelayProcess.ar(
//			Array.fill(qnty,{arg i; {arg in; RLPF.ar(in,LFNoise1.kr(0.3,freqOffset,ffreq),rq) }}),audio,nil,
//			Array.fill(qnty,{arg i; (i * delay * thisSynth.tempo.reciprocal).wrap(0.05,1.9) }),
//			LFNoise1.kr(0.2,Amplitude.kr(audio,mul: - 1)),
//			buffer
//		)
//	},[
//	//arg audio,drive=0.4,ffreq=3000,rq=0.5,qnty=4,freqOffset=33,delay=0.25;
//		nil,nil,nil,			nil,		nil,	nil,	StaticSpec(0.0,0.5)
//	]);
//
//	Instr([\timeDomainEfx,\multFeedBPF],{arg audio,
//		drive=0.4,ffreq=3000,rq=0.5,qnty=4,freqOffset=33,delay=0.25;
//
//		var buffer;
//		buffer = Buffer.for(audio,0.5);
//		MultiTapDelayProcess.ar(
//			Array.fill(qnty,{arg i; {arg in; BPF.ar(in,LFNoise1.kr(0.3,freqOffset,ffreq),rq) }}),audio,nil,
//			Array.fill(qnty,{arg i; (i * delay * thisSynth.tempo.reciprocal).wrap(0.05,1.9) }),
//			LFNoise1.kr(0.2,Amplitude.kr(audio,mul: - 1)),
//			buffer
//		)
//	},[
//	//arg audio,drive=0.4,ffreq=3000,rq=0.5,qnty=4,freqOffset=33,delay=0.25;
//		nil,nil,nil,			nil,		nil,	nil,	StaticSpec(0.0,0.5)
//	]);
//
//


//Instr([	\timeDomainEfx, \Reverberator3],{
//			arg audio,mix,revTime=1,taps=2,combs=4,allpasses=3,tapsMin=0.01,tapsMax=0.05,combsMin=0.05,combsMax=0.1,
//				allpassMin=0.01,allpassMax=0.05,decay=1.0;
//
//			Reverberator3.ar(audio,mix,revTime,taps.asInteger,combs.asInteger,allpasses.asInteger,
//				tapsMin,tapsMax,combsMin,combsMax,allpassMin,allpassMax,decay)
//	},[
//		nil,nil,nil,[\qnty0],[\qnty0],[\qnty0],[StaticSpec(0,1.0)],[StaticSpec(0,1.0)],[StaticSpec(0,1.0)],[StaticSpec(0,1.0)],
//			[StaticSpec(0,1.0)],[StaticSpec(0,1.0)],\decay
//	]);
//

