
// the .max(0.0001) was need in SC2 to stop it from blowing up
// I don't know if its needed now.  I think it still is.

Instr.orc(\dynamics,[

	\compressor, { arg audio=0.0,threshold=0.5,slope=0.5,clampTime=0.1,relaxTime=0.3;
		// auto gain would be nice
			Compander.ar( audio, Mono(audio).max(0.0001), threshold, 1.0 , slope,clampTime,relaxTime )
	},
	\upwardLim,{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,relaxTime=0.3,drive=1.0;
	
		Limiter.ar(
			Compander.ar( audio, Mono(audio)//.max(0.0001)
					, threshold, boost , 1.0 ,clampTime,relaxTime ) * drive,
			0.999)
				
	},
	\limiter,{ arg audio=0.0;
	
		Limiter.ar(
			audio ,
			0.999)
				
	},
	\upward,{ arg audio=0.0,threshold=0.5,boost=0.3,clampTime=0.1,relaxTime=0.3;
			Compander.ar( audio, Mono(audio).max(0.0001), threshold, boost , 1.0 ,clampTime,relaxTime )
	},
	
	\compLim,{ arg audio=0.0,threshold=0.7,slope=0.7,clampTime=0.1,relaxTime=0.3;
	
		Limiter.ar(
			Compander.ar( audio, Mono(audio).max(0.0001), threshold, 1.0 , slope ,clampTime,relaxTime ),
			0.999)
				
	}
	
	,\dualFreqSlew,{ arg audio,
							ffreq=90, rq=0.5,
							freq=700,keyrq=0.3,
							 drive=2,decay=0.3,
							 refresh=0.248,lift=0.744,
							 slewRise=2550,
							 space = 1.0,
							 freq2=100;
	
		var lo,hi,guide,bass,mono;
		var slewed;
		
		
		mono = NumChannels.ar(audio,1,false); //Mix.new(audio ?? { [0,0] });
		lo = BPF.ar( mono, ffreq,rq);
		hi = BRF.ar( audio, ffreq,rq);
		hi = HPF.ar( hi, freq2);
		
		guide = Amplitude.kr(BPF.ar(mono, freq,keyrq),0.01,decay,drive);
		bass = lo * guide;
		bass = Compander.ar( bass,bass.max(0), 0.7, 1, 0.4,0.1,0.2);
		bass = Slew.ar( bass, 800,800,1.2);
		
		
		slewed = Slew.ar( hi, slewRise,slewRise);
		
		hi = hi * Amplitude.kr( Slope.ar(hi, mul: 0.0002) , 0.01,0.3,mul: refresh, add: lift);	
		hi = Compander.ar( hi, hi, 0.7,1,0.5,0.05,0.2);
		
		hi = Mix.new([slewed * space,hi]);
		
		Mix.new([[bass,bass],  hi]) 
	
	}
],
	\stereoEffect
);




Instr("dynamics.dualFreq",{ arg audio,
						ffreq=90, rq=0.5,
						freq=700,keyrq=0.3,
						 drive=2,decay=0.3,
						 refresh=0.248,lift=0.744,
						 slewRise=2550,
						 space = 1.0,
						 freq2=100;

	var lo,hi,guide,bass,mono;
	
	mono = NumChannels.ar(audio,1,false); //Mix.new(audio ?? { [0,0] });
	lo = BPF.ar( mono, ffreq,rq);
	hi = BRF.ar( audio, ffreq,rq);
	hi = HPF.ar( hi, freq2);
	
	guide = Amplitude.kr(BPF.ar(mono, freq,keyrq),0.01,decay,drive);
	bass = lo * guide;
	bass = Compander.ar( bass,bass.max(0), 0.7, 1, 0.4,0.1,0.2);
	bass = Slew.ar( bass, 800,800,1.2);
	
	hi = hi * Amplitude.kr( Slope.ar(hi, mul: 0.0002) , 0.01,0.3,mul: refresh, add: lift);	
	hi = Compander.ar( hi, hi, 0.7,1,0.5,0.05,0.2);
		
	Mix.new([[bass,bass],  hi]) 

},
	nil,
	\audio
);



// smear and gate/compress lowend,  
Instr("dynamics.smearRefresh",{ arg audio,
						ffreq=90, rq=0.5,
						freq=700,keyrq=0.3,
						 drive=2,decay=0.3,
						 refresh=0.248,lift=0.744,
						 freq2=100;

	var lo,hi,guide,bass,mono;
		
	mono = NumChannels.ar(audio,1,false); //Mix.new(audio ?? { [0,0] });
	lo = BPF.ar( audio, ffreq,rq);
	
	hi = BRF.ar( audio, ffreq,rq);
	hi = HPF.ar( hi, freq2);
	
	// expand out quiet part
	guide = Amplitude.kr(BPF.ar(mono, freq,keyrq),0.01,decay,drive);
	bass = lo * guide;

	// slew blur
	bass = Slew.ar( bass, 800,800,1.2);

	// compress
	bass = Compander.ar( bass,bass.max(0), 0.7, 1, 0.4,0.1,0.2);
	
	// driven by amplitide of transients
	hi = hi * Amplitude.kr( Slope.ar(hi, mul: 0.0002) , 0.01,0.3,mul: refresh, add: lift);	
	// compressed
	hi = Compander.ar( hi, hi, 0.7,1,0.5,0.05,0.2);
		
	Mix.new([bass,  hi]) 

},
	nil,
	\audio
);


// smear and gate/compress lowend,  
Instr("dynamics.loHiCompTranny",{ arg audio,
						ffreq=90, rq=0.5,
						freq=700,
						 drive=2,
						 refresh=0.248,lift=0.744,
						 threshold=0.7;

	var lo,hi,guide,bass,mono;
		
	mono = NumChannels.ar(audio,1,false); //Mix.new(audio ?? { [0,0] });
	lo = RLPF.ar( audio, ffreq,rq);
	
	hi = HPF.ar( audio, ffreq);
	
	// expand out quiet part
	guide = Amplitude.kr(BPF.ar(mono, freq,0.5),0.01,0.3,drive);
	bass = lo * guide;

	// slew blur
	bass = Slew.ar( bass, 800,800,1.2);

	// compress
	bass = Compander.ar( bass,Mono(bass).max(0.001), threshold, 1, 0.4,0.1,0.2);
	
	// driven by amplitide of transients
	hi = hi * Amplitude.kr( Slope.ar(hi, mul: 0.0002) , 0.01,0.3,mul: refresh, add: lift);	
	// compressed
	hi = Compander.ar( hi, hi, 0.7,1,0.5,0.05,0.2);
		
	Mix.new([bass,  hi]) 

},
	nil,
	\audio
);





/*  pass in table as param
Instr([\dynamics,\shaper],{ arg audio=0.0,attack=0.05,release=0.2;

	var table,key;
	table =
	Signal
	.newClear(2048)
	.fill(0.5)
	.addSine(0.5, 0.5, -0.5pi)
	.distort
	.distort
	.distort
	.distort
	.normalize
	.asWavetable;
	
	key = Mono(audio).max(0.0001);
	
	Shaper.kr(table,
			Amplitude.kr(key,attack,release)
		)
		
		-	Amplitude.kr(key,0.01,0.05)
 	* audio	
});


Instr([\dynamics,\variShaper],{ arg audio=0.0,attack=0.05,release=0.2,flatness=1.0;

	var table,key;
	table =
	Signal
	.newClear(2048)
	.fill(0.5)
	.addSine(0.5, 0.5, -0.5pi)
	.distort
	.distort
	.distort
	.distort
	.normalize
	.asWavetable;
	
	key = Mono(audio).max(0.0001);

	LinXFade2.kr(
		Shaper.kr(table,Amplitude.kr(key,attack,release))
		
		-	Amplitude.kr(key,0.01,0.05)
		,
		1.0,
		flatness
		
	)
 	* audio	
});

*/


Instr([\dynamics,\level3band],{ arg audio=0.0,lo=1.0,mid=1.0,hi=1.0, 						attack=0.9,xover1=400,xover2=6000;

	var bands,levels;
	bands =	Instr.ar([\splits,\xover3,\bpf],[audio,xover1,xover2]);
	levels = [lo,mid,hi];
	bands = bands.collect({ arg b,i;
		Compander.ar(b,Mono(b).max(0.0001),0.5,0.4,1.0,attack,attack,levels.at(i))
	});
	Mix.new(bands);
},#[
	\audio,
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[0.01,5.0],
	[100,5000,\exponential],
	[6000,20000,\exponential]

],
	\audio
);



/*
Instr([\dynamics,\shape3band],{ arg audio=0.0,lo=1.0,mid=1.0,hi=1.0,attack=0.2,release=0.5,xover1=400,xover2=6000;

	var bands,levels;
	bands =	Instr.ar([\splits,\xover3,\bpf],[audio,xover1,xover2]);
	levels = [lo,mid,hi];
	bands = bands.collect({ arg b,i;
		Instr.ar([\dynamics,\shaper],[b,attack,release]) * levels.at(i)
	});
	Mix.new(bands);
},#[
	\audio,
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[0.01,5.0],
	[0.01,5.0],
	[100,5000,\exponential],
	[6000,20000,\exponential]

]);
*/


Instr([\dynamics,\deess],{ arg audio=0.0,threshold=0.5,slope=0.5,ffreq=6000,rq8=0.5,
							clampTime=0.01,relaxTime=0.3;

	var key;
	key = BPF.ar(Mono(audio),ffreq,rq8).max(0.0001);
	Compander.ar( audio, key, threshold, 1.0 , slope,clampTime,relaxTime );
},
	nil,
	\audio
);



Instr([\dynamics,\gatecomp],{ arg audio=0.0,
							gthreshold=0.1,gslope=0.5,
							cthreshold=0.7,cslope=0.5,
							clampTime=0.1,relaxTime=0.3;

	var key,gated;
	key = Mono(audio).max(0.0001);
	gated=Compander.ar( audio, key, gthreshold,gslope,1.0,  clampTime,relaxTime);
		 Compander.ar( gated, key, cthreshold, 1.0 , cslope,clampTime,relaxTime);
},#[
	\audio,
	[0.001,1.0],
	[0.1,100],
	[0.001,1.0],
	[0.05,1.0],
	[0.01,0.5],
	[0.01,1.0]

],
	\audio
);



Instr([\dynamics,\gate],{ arg audio=0.0,
							gthreshold=0.1,gslope=0.5,
							clampTime=0.1,relaxTime=0.05;

	var key,gated;
	key = Mono(audio).max(0.0001);
	gated=Compander.ar( audio, key, gthreshold,gslope,1.0,  clampTime,relaxTime);
},#[
	\audio,
	[0.0001,1.0,\exponential],
	[0.01,20],
	[0.01,0.5],
	[0.01,1.0]

],
	\audio
);

/*
agh.. this one sounded good
but AvgAbsAmp is gone

Instr([\dynamics,\RMScompressor],{ arg audio,coef=0.5,threshold=0.2,slope=0.3,clampTime=0.01,relaxTime=0.1;
	var key;
	key = AvgAbsAmp.kr(Mono(audio),coef);
	Compander.ar( audio, key, threshold, 1.0 , slope,clampTime,relaxTime )
});
*/



