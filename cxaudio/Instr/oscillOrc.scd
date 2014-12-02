// all mono
	
Instr.orc(\oscillOrc,[

	\sin,{arg freq=500,phase=0,amp=1.0;  
		SinOsc.ar(freq,phase,amp);  
	},
	
	\formant,{ arg freq=400,freq2=1300,bw=400,amp=1.0;
		Formant.ar(freq,freq2,bw,amp);
	},
	\formantAdd,{ arg freq=400,freqOffset=100,bw=400,amp=1.0;
		Formant.ar(freq,freq + freqOffset,bw,amp);
	},
	\formantMul,{ arg freq=400,freqOffset=100,bw=400,amp=1.0;
		Formant.ar(freq,freq * freqOffset,bw,amp);
	},
	
	\syncsaw,{ arg freq=400,freq2=1300,amp=1.0;
		 SyncSaw.ar(freq2, freq, amp); 
	},
	\syncsawAdd,{ arg freq=400,freqOffset=1300,amp=1.0;
		 SyncSaw.ar(freq + freqOffset, freq, amp); 
	},
	\syncsawMul,{ arg freq=400,freqOffset=1.0,amp=1.0;
		 SyncSaw.ar(freq * freqOffset, freq, amp); 
	},
	// won't modulate ? why ?
	\pmosc,{ arg freq=400, freq2=500,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.ar(freq,freq2,pmindex,phasemod,amp)
	},
	\pmoscAdd,{ arg freq=400, freqOffset=500,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.ar(freq,freq + freqOffset,pmindex,phasemod,amp)
	},
	\pmoscMull,{ arg freq=400, freqOffset=1.1,pmindex=0,phasemod=0.0,amp=1.0;
		PMOsc.ar(freq,freq * freqOffset,pmindex,phasemod,amp)
	},
/*
	\phasor,{ arg freq=440,amp=1.0;
		Phasor.ar(freq,amp)
	},
*/
	\lfsaw,{ arg freq=440,amp=1.0,iphase=0.0;
		LFSaw.ar(freq,iphase,amp)
	},
	\lftri,{ arg freq=440,amp=1.0,iphase=0.0;
		LFTri.ar(freq,iphase,amp)
	},
	\lfpulse,{ arg freq=440,amp=1.0,iphase=0.0,width=0.5;
		LFPulse.ar(freq,iphase,width,amp)
	},
	\impulse,{ arg freq=440,amp=1.0,iphase=0.0;
		Impulse.ar(freq,iphase,amp)
	},
	\varsaw,{ arg freq=440,iphase=0.0,width=0.5,amp=1.0;
		VarSaw.ar(freq,iphase,width,amp)
	},

	
	\blip,{ arg freq=440,numharms=4,amp=1.0;
		Blip.ar(freq,numharms,amp)
	},
	
	\saw, { arg freq=440,amp=1.0;
		Saw.ar(freq,amp)
	},
	\pulse,{ arg freq=440,width=0.5,amp=1.0;
		Pulse.ar(freq,width,amp)
	},
	
	
	
	
	
	
	
	\white,{ arg amp=1;
		WhiteNoise.ar(amp)
	},
	\brown,{ arg amp=1;
		BrownNoise.ar(amp)
	},
	\pink,{ arg amp=1;
		PinkNoise.ar(amp)
	},
	\ClipNoise,{ arg amp;
		ClipNoise.ar(amp)
	},
	
	\gray,{arg amp=1;
		GrayNoise.ar(amp)
	},
	\crackle,{ arg chaos,amp=1;
		Crackle.ar(chaos,amp)
	},
	/*
	\rossler,{ arg chaosParam=1.5,dt=0.04,amp=1;
		Rossler.ar(chaosParam,dt,amp)
	},
	\LinCong,{arg iseed=100000,freq=800,freqOffset=1000,imod=1000000,amp=1;
		LinCong.ar(iseed,freqOffset,freq,imod,amp)
	},
	*/
	\LFNoise0,{ arg freq,amp=1;
		LFNoise0.ar(freq,amp)
	},
	\LFNoise1,{ arg freq,amp=1;
		LFNoise1.ar(freq,amp)
	},
	\LFNoise2,{ arg freq,amp=1;
		LFNoise2.ar(freq,amp)
	},
	\Dust,{ arg density,amp=1;
		Dust.ar(amp)
	},
	\Dust2,{ arg density,amp=1;
		Dust2.ar(density,amp)
	},
	/*
	\audioin, { arg amp=1;
		AudioIn.ar(	[1,2],amp)
	},
	*/

	\whiteResonz,{	arg 	freq=500,bw=0.008;
		
		Resonz.ar(WhiteNoise.ar(4), 500.value, bw)
				
	}


],\mono);






