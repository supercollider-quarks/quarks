


Instr([\surf,\slew] ,{ arg audio,tideFreq=0.05,waveFreq = 0.1, min=40,max=12000,tideEffect=0.5;

	var slew;
	slew = Surf.kr(min,max,tideFreq,waveFreq,tideEffect);
	Slew.ar( audio,
			slew,
			slew
		)
},[
	\audio,
	[0,0.2],
	[0,0.5],
	\freq,
	\freq,
	\linear
],
	\audio
);



Instr([\surf,\rlpf] ,{ arg audio,tideFreq=0.05,waveFreq = 0.1, min=100,max=12000,tideEffect=0.5,rq=0.2;

	var slew;
	slew = Surf.kr(min,max,tideFreq,waveFreq,tideEffect);
	RLPF.ar( audio,
			slew + 10,
			rq
		)
},[
	\audio,
	[0,0.2],
	[0,0.5],
	\freq,
	\freq,
	\linear
],
	\audio
);


Instr([\surf,\rez] ,{ arg audio,tideFreq=0.05,waveFreq = 0.1, min=0.02,max=1.4,tideEffect=0.5,freq=5000;

	var slew;
	slew = Surf.kr(min,max,tideFreq,waveFreq,tideEffect);
	Resonz.ar( audio,
			freq,
			slew
		)
},[
	\audio,
	[0,0.2],
	[0,0.5],
	\rq,\rq
],
	\audio
);



Instr([\surf,\bpf] ,{ arg audio,tideFreq=0.05,waveFreq = 0.1, min=0.02,max=1.4,tideEffect=0.5,freq=5000;

	var slew;
	slew = Surf.kr(min,max,tideFreq,waveFreq,tideEffect);
	BPF.ar( audio,
			freq,
			slew
		)
},[
	\audio,
	[0,0.2],
	[0,0.5],
	\rq,\rq
],
	\audio
);


/* this blows up
Instr([\surf,\RHPF] ,{ arg audio,tideFreq=0.05,waveFreq = 0.1, min=100,max=4000,tideEffect=0.5,rq=0.2;

	var slew;
	slew = Surf.kr(min,max,tideFreq,waveFreq,tideEffect);
	RHPF.ar( audio,
			slew,
			rq
		)
},[
	\audio,
	[0,0.2],
	[0,0.5],
	\ffreq,\ffreq
],\audio);

*/



//Instr.leaves(\atmospherics).do({ arg instr;
//	Patch(instr.name,[nil]).topGui;
//});
//




/*


(
{ arg synth,tideFreq=0.01,waveFreq = 0.1, min=40,max=12000,tideEffect=0.5;

	var wave,tide,halfrange;
	halfrange = max - min * 0.5;
	tide = SinOsc.ar(tideFreq,0,halfrange,halfrange + min) * tideEffect;
	wave = SinOsc.ar(waveFreq,0,halfrange, halfrange + min) * (1 - tideEffect);
	(wave + tide) / 12000
	//tide
}.plot(120.0)
)

*/

//Instr([\surf,\wave] ,{ arg tideFreq=0.01,waveFreq = 0.1, min=40,max=12000,tideEffect=0.5;
//
//	var wave,tide,halfrange;
//	halfrange = max - min * 0.5;
//	tide = SinOsc.ar(tideFreq,0,halfrange,halfrange + min) * tideEffect;
//	wave = SinOsc.ar(waveFreq,0,halfrange, halfrange + min) * (1 - tideEffect);
//	(wave + tide)
//},[
//	[0,0.2],
//	[0,0.5],
//	\freq,
//	\freq,
//	\linear
//]);
//





