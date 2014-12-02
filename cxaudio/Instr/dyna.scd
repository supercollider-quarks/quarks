
// strange dynamic treatments

Instr([\dyna,\transDriven],{ arg audio=0.0,sensitivity=0.01,attack=0.01,release=0.1;

	// detect the slope (sensitive to transients) and use that to spike the amplitude
	// with variable attack and release.
	// you can do strange audio-refresh effects
	
	Amplitude.kr(
		Slope.kr(A2K.kr(audio),sensitivity),
		attack,
		release)
	
		* audio
},[
	\audio,
	[0.0,0.1]

],
	\audio
);

Instr([\dyna,\crossDriveTrans],{ arg audio=0.0,key=0.0,sensitivity=0.01,attack=0.01,release=0.1;

	// detect the transients on the key input
	// and spkie the amplitude of the treated channel
	
	Amplitude.kr(
		Slope.kr(A2K.kr(key),sensitivity),
		attack,
		release)
		
		* audio
},[
	\audio,
	\audio,
	[0.0,0.1]

],
	\audio
);



Instr([\dyna,\audioByKey],{ arg audio=0.0,key=0.0,sensitivity=1.0,attack=0.01,release=0.1;

	// drive the audio with the normal audio level of the key channel

	Amplitude.kr(
		key,
		attack,
		release,
		sensitivity)
		
		* audio
},[
	\audio,
	\audio,
	[0.0,10.0]

],
	\audio
);





//// an actual LPF gating upwards
//Instr([\dyna,\bss],{ arg audio=0.0,keyfreq=6000,basefreq=6000,sensitivity=1.0,attack=0.01,release=0.1;
//
//	var freq;
//	freq=Amplitude.kr(
//		key,
//		attack,
//		release,
//		sensitivity,
//		basefreq);
//		
//		LPF.ar(audio,freq);
//},[
//	\audio,
//	\audio,
//	[0.0,10.0]

//]);
//





