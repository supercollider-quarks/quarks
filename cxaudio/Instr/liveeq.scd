
/*
	final eqs designed for live shows
	tweaking the sound with a few intuitive controls
*/

Instr("liveeq.hiloAndMidPush",{ arg audio=0.0,hilo=0,level=1.0,midlevel=1.0,
					xover=500,xover2=7000;
	
	var lo,hi,loa,hia;
	var lov,hiv,mida;
	
	//xover = xover + LFNoise1.kr(0.2,200).max(60);
	// some wobble
	xover2 = xover2 + LFNoise1.kr(0.2,200).max(60);
	
	//# lov, hiv = LinPan2.kr(level,hilo);
	lov = level * (1.0 - hilo);
	hiv = level * hilo;

	# loa , mida, hia = Instr.ar([\splits,\xover3,\lp],[ audio,xover,xover2,lov,midlevel * level,hiv]);
	
	// mida = Instr.ar([\eq,\bpfGain],[mida,midfreq,midrq,midlevel]);
	// let something else limit it
	Mix.new([
		 loa,
		 mida,
		 hia
	])
	
},#[
	\audio,
	\mix,
	[0.0,3.5],
	[0.4,1.0],
	[200,2000,\exponential],
	[4000,10000,\exponential]

],
	\audio
);




Instr("liveeq.hiloAndMidPushSlew",{ arg audio=0.0,hilo=0,level=1.0,midlevel=1.0,
					slope=500,xover2=7000;
	
	var lo,hi,loa,hia;
	var lov,hiv,mida;
	
	xover2 = xover2 + LFNoise1.kr(0.2,200).max(60);
	
	//# lov, hiv = LinPan2.kr(level,hilo);
	lov = level * (1.0 - hilo);
	hiv = level * hilo;

	# loa , mida, hia = Instr.ar([\splits,\xover3,\slew],[ audio,slope,xover2,lov,midlevel * level,hiv]);
	
	Mix.new([
		 loa,
		 mida,
		 hia
	])
	
},#[
	\audio,
	\mix,
	[0.0,5.5],
	[0,1.0],
	[200,2000,\exponential],
	[4000,10000,\exponential]

],
	\audio
);




Instr("liveeq.leakBPF",{ arg audio=0.0,coef=0.8,ffreq=1000,rq=0.5,gain=0.2;

	audio = LeakDC.ar(audio,coef);
	audio + BPF.ar(audio,ffreq,rq,gain)
			
},[
	\audio,
	\coef,
	\ffreq,
	\rq,
	\gain
],
	\audio
);


Instr("liveeq.upwardLimHPmids",{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,
									relaxTime=0.3,drive=1.0,coef=0.99,ffreq=1000,rq=0.5,fboost=0.2;

	var audioOut;
	audio = LeakDC.ar(audio,coef);
	audioOut = Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime ,drive);
	Limiter.ar(
		(audioOut + BPF.ar(audioOut,ffreq,rq,fboost)),
		0.999)
			
},
	nil,
	\audio
);

// dc trap removed
Instr("liveeq.upwardLimHPmids2",{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,
										relaxTime=0.3,drive=1.0,ffreq=1000,rq=0.5,fboost=0.2;

	var audioOut;
	audioOut = Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime ,drive);
	Limiter.ar(
		(audioOut + BPF.ar(audioOut,ffreq,rq,fboost)),
		0.999)
			
},
	nil,
	\audio
);

// no bass trap, always a high boost
Instr("liveeq.upwardLimHPmidsHi",{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,
										relaxTime=0.3,drive=1.0,ffreq=1000,rq=0.5,fboost=0.2,hiboost=3.0;

	var audioOut;
	audioOut = Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime ,drive);
	Limiter.ar(
		(audioOut + BPF.ar(audioOut,ffreq,rq,fboost) + BPF.ar(audioOut,12000,0.5,hiboost)),
		0.9999)
			
},
	nil,
	\audio
);

Instr("liveeq.LeakLimBPF",{ arg audio=0.0,coef=0.8,ffreq=1000,rq=0.5,fboost=0.2;

	audio = LeakDC.ar(audio,coef);
	
	Limiter.ar(
		(audio + BPF.ar(audio,ffreq,rq,fboost)),
	0.999)
	
},
	nil,
	\audio
);

//Instr([\liveeq,\LeakCompLimBPF],{ arg audio=0.0,coef=0.8,ffreq=1000,rq=0.5,fboost=0.2,threshold=0.18,slope=0.3;
//
//	audio = LeakDC.ar(audio,coef);
//	audio = audio + BPF.ar(audio,ffreq,rq,fboost);
//	audio = Instr.ar([\dynamics,\RMScompressor],[ audio,0.5,threshold,slope]);
//	
//	Limiter.ar(
//		audio,
//	0.999)
//	
//});

/*
Instr("liveeq.LeakDeesCompLimBPF",{ arg audio=0.0,coef=0.8,ffreq=1000,rq=0.5,fboost=0.2,threshold=0.18,slope=0.2;

	var key;

	audio = LeakDC.ar(audio,coef);
	audio = audio + BPF.ar(audio,ffreq,rq,fboost);
	audio = Instr.ar([\dynamics,\RMScompressor],[ audio,0.5,threshold,slope]);
	
	key = BPF.ar( AvgAbsAmp.kr(Mono(audio),coef),3000,0.3);
	audio = Compander.ar( audio, key, threshold, 1.0 , slope,0.01,0.1 );

	Limiter.ar(
		audio,
	0.999)
	
});
*/

Instr("liveeq.upwardLimHP8k",{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,relaxTime=0.3,drive=1.0,coef=0.99,ffreq=8000,rq=0.5,gain=0.2;

	var audioOut;
	audio = LeakDC.ar(audio,coef);
	audioOut = Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime ,drive);
	Limiter.ar(
		Instr.ar("toneknob.bpfGain",[   audioOut,ffreq,rq,gain ]),
		0.999)		
},
	nil,
	\audio
);


Instr("liveeq.upwardLimPeaking",{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,relaxTime=0.3,drive=1.0,coef=0.99,ffreq=8000,rq=0.5,gain=0.2;

	var audioOut;
	audio = LeakDC.ar(audio,coef);
	audioOut = Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime ,drive);
	Limiter.ar(
		// RBJeq.sc
		PeakingEQ.ar(audioOut,ffreq,rq,gain),
		0.999)
			
},[
	nil,nil,nil,nil,nil,nil,nil,nil,nil,[[0.01,6.0]]
],
	\audio
);

