// all mono
	
// ?	class Out has bad inputs: [ an OutputProxy, a BinaryOpUGen ].

Instr.orc(\oscillOrcKr,[
	\sin,{arg lfo=500,phase=0,amp=1.0;  
			SinOsc.kr(lfo,phase,amp);  
	},
	
	\syncsaw,{ arg lfo=400,freq=1300,amp=1.0;
		 SyncSaw.kr(freq, lfo, amp); 
	},
	\syncsawAdd,{ arg lfo=400,freqOffset=1300,amp=1.0;
		 SyncSaw.kr(lfo + freqOffset, lfo, amp); 
	},
	\syncsawMul,{ arg lfo=400,freqOffset=1.0,amp=1.0;
		 SyncSaw.kr(lfo * freqOffset, lfo, amp); 
	},
	\pmosc,{ arg lfo=400, freq=500,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.kr(lfo,freq,pmindex,phasemod,amp)
	},
	\pmoscAdd,{ arg lfo=400, freqOffset=500,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.kr(lfo,lfo + freqOffset,pmindex,phasemod,amp)
	},
	\pmoscMull,{ arg lfo=400, freqOffset=1.1,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.kr(lfo,freqOffset,pmindex,phasemod,amp)
	},

	\lfsaw,{ arg lfo=440,amp=1.0,iPhase=0.0;
		LFSaw.kr(lfo,iPhase,amp)
	},
	\lfpar,{ arg lfo=440,iPhase=0.0;
		LFSaw.kr(lfo,iPhase)
	},
	\lfcub,{ arg lfo=440,iPhase=0.0;
		LFSaw.kr(lfo,iPhase)
	},
	\lftri,{ arg lfo=440,amp=1.0,iPhase=0.0;
		LFTri.kr(lfo,iPhase,amp)
	},
	\lfpulse,{ arg lfo=440,amp=1.0,iPhase=0.0;
		LFPulse.kr(lfo,iPhase,amp)
	},
	\lfvarsaw,{ arg lfo=1.0,iphase=0.0,width=0.5;
		VarSaw.kr(lfo,iphase,width)
	},
	\white,{ arg amp=1;
		WhiteNoise.kr(amp)
	},
	\brown,{ arg amp=1;
		BrownNoise.kr(amp)
	},
	/*
	\pinker,{ arg amp=1;
		PinkerNoise.kr(amp)
	},
	*/
	\clipn,{ arg amp=1;
		LFClipNoise.kr(amp)
	},
	\gray,{arg amp=1;
		GrayNoise.kr(amp)
	},
	\crackle,{ arg chaos,amp=1;
		Crackle.kr(chaos,amp)
	},
	/*
	\rossler,{ arg chaosParam=1.5,dt=0.04,amp=1;
		Rossler.kr(chaosParam,dt,amp)
	},
	\LinCong,{arg iseed=100000,lfo=800,freqOffset=1000,imod=1000000,amp=1;
		LinCong.kr(iseed,freqOffset,lfo,imod,amp)
	},
	*/

	\LFNoise0,{ arg lfo,amp=1;
		LFNoise0.kr(lfo,amp)
	},
	\LFNoise1,{ arg lfo,amp=1;
		LFNoise1.kr(lfo,amp)
	},
	\LFNoise2,{ arg lfo,amp=1;
		LFNoise2.kr(lfo,amp)
	},
	\whiteResonz,{	arg 	lfo=500,bw=0.008;
		Resonz.kr(WhiteNoise.kr(4), 500.value, bw)
	},
	\ZeroCrossing,{ arg audio;
		ZeroCrossing.kr(A2K.kr(audio))
	},
	
],\bipolar);

	
Instr.orc(\oscillOrcKr,[
	\Amplitude,{ arg audio,attack=0.05,release=0.2,mul=1.0,add=0.0;
		Amplitude.kr(audio,attack,release,mul,add)
	},
	\PeakFollower,{ arg audio=0.0,coef=0.999;
		PeakFollower.kr(audio,coef)
	},
	\Slope,{  arg audio,mul=1.0,add=0.0;
		Slope.kr(A2K.kr(audio),mul,add)
	},
	\PulseCount,{ arg trig,reset=0.0;
		PulseCount.kr(trig,reset)
	}
],
	\unipolar
); // well not really...
	
	
	
Instr.orc(\oscillOrcKr,[	
	
	\impulse,{ arg lfo=440,amp=1.0;
		Impulse.kr(lfo,amp)
	},
//	\TPulse,{ arg trig=0.0,lfo=0.0,width=0.5;
//		TPulse.kr(trig,lfo,width)
//	},

	\Dust,{ arg density,amp=1;
		Dust.kr(amp)
	},
	\Dust2,{ arg density,amp=1;
		Dust2.kr(density,amp)
	},
	
//	\Pitch,{  arg audio = 0.0;
//		var lfo,hasFreq;
//		#lfo,hasFreq = Pitch.kr(audio);
//		lfo
//	},


	\PulseDivider,{ arg trig,div=2.0;
		PulseDivider.kr(trig,div)
	},


	\TDelay,{ arg trig=0.0,delay=0.1;
		TDelay.kr(trig,delay)
	},
//	\TDelayBeat,{ arg trig=0.0,beat=0.5;
//		TDelay.kr( trig, Tempo.beats2secsKr(beat))
//	},


],\trig);


Instr([\oscillOrcKr,
	\ExtendTrig],{ arg trig=0.0,duration=0.25;
		Trig.kr(trig,duration)
	},nil,\gate);

Instr([\oscillOrcKr,
	\Gate],{ arg bipolar,gate=0.0;
		Gate.kr(bipolar,gate)
	},nil,\gate);

Instr([\oscillOrcKr,	
	\Latch],{ arg bipolar=0.0,trig=0.0;
		Latch.kr(bipolar,trig)
	},nil,\bipolar);



