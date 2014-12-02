

// remove some
Instr("bassEfx.brf",{  arg audio=0.0,ffreq=200,rq8=0.8;
	BRF.ar(audio ,ffreq,rq8 )
},nil,\audio);




// awesome
Instr("bassEfx.slewd2",{  arg audio=0.0,slewRise=300,slewFall;

	Slew.ar(audio,slewRise,slewFall)
	
},[
	\audio,
	[50,3000],
	[50,3000]
	
],\audio);


// simpler awesome
Instr("bassEfx.slewd",{  arg audio=0.0,slew=300;

	Slew.ar(audio,slew,slew)
	
},[
	\audio,
	[50,3000]

],
	\audio
);



// a channel hardShaped
Instr("bassEfx.hardGainChan",{ arg audio=0.0,freq=200,rq8=1.0,drive=1.0;

	var chan,rest,mix;

	chan = BPF.ar( audio, freq,rq8);
	chan = HardShaper.ar(chan,drive);
	// phasey ness is nice actually
	rest = BRF.ar( audio, freq, rq8);
	
	mix=Mix.new([chan,rest ]);

},#[
	\audio,
	[60,10000,\exponential],
	[0.0,1.5],
	[0,20]

],
	\audio
);


// two channels. pumper
Instr("bassEfx.deucePump",{ arg audio=0.0,freq=200, lo=1.0,hi=1.0;
						
	Mix.new([
		 LPF.ar(audio,freq,lo).softclip,
		 HardShaper.ar( HPF.ar(audio,freq),hi)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[0.0,8.0],
	[0,10]

],
	\audio
);

// two channels. balance pumper
Instr("bassEfx.deucePump2",{ arg audio=0.0,freq=500, drive=1,balance=0.5;
	
	var lo,hi;
	/* # lo, hi = LinPan2.kr(drive,balance);			*/	lo = drive * (1.0 - balance);
	hi = drive * balance;

	Mix.new([
		 LPF.ar(audio,freq,lo * 9.0).softclip,
		 HardShaper.ar( HPF.ar(audio,freq),hi * 6.0)
	])

//	XFader.ar(
//		 LPF.ar(audio,freq,lo * 9.0).softclip,
//		 HardShaper.ar( HPF.ar(audio,freq),hi * 6.0),
//		 balance
//	) * drive

},#[
	\audio,
	[60,10000,\exponential],
	[0,1.5],
	\pan
],
	\audio
);

// two channels. balance pumper.  xover
Instr("bassEfx.deucePump2b",{ arg audio=0.0,freq=500, drive=1,balance=0.5;
	
//	var lo,hi,loa,hia;
//	# lo, hi = LinPan2.kr(drive,balance);					
//	# loa , hia = Instr.ar([\splits,\xover],[ audio,freq,lo * 9.0,hi * 6.0]);
//	Mix.new([
//		 loa.softclip,
//		 HardShaper.ar( hia)
//	])
	var lo,hi,loa,hia;
	//# lo, hi = LinPan2.kr(drive,balance);					
	lo = drive * (1.0 - balance);
	hi = drive * balance;
	
	# loa , hia = Instr.ar([\splits,\xover],[ audio,freq,lo * 9.0,hi * 6.0]);
	Mix.new([
		 loa.softclip,
		 HardShaper.ar( hia)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[0,1.5],
	\pan
],
	\audio
);

// two channels. balance pumper
Instr("bassEfx.deucePumpFlat",{ arg audio=0.0,freq=500, drive=1,balance=0.5;
	
	
	var lo,hi,loa,hia;
	lo = drive * (1.0 - balance);
	hi = drive * balance;

	# loa , hia = Instr.ar([\splits,\xover],[ audio,freq,lo * 9.0,hi * 6.0]);
	Mix.new([
		 loa,
		 hia
	])

},#[
	\audio,
	[60,10000,\exponential],
	[0,1.5],
	\pan
],
	\audio
);


// two channels. balance pumper  softclip on end.slight wander on freq
Instr("bassEfx.deucePumpHalfFlat",{ arg audio=0.0,freq=500, drive=1,balance=0.5,max=2.5;
	
	var lo,hi,loa,hia;
	lo = drive * (1.0 - balance);
	hi = drive * balance;

	# loa , hia = Instr.ar([\splits,\xover],[ audio,(freq + LFNoise1.kr(0.2,200)).max(60),lo * max,hi * max]);
	Mix.new([
		 loa,
		 hia
	]).softclip
},#[
	\audio,
	[60,10000,\exponential],
	[0,5.5],
	\pan,
	[0.00001,5.0,\exponential]

],
	\audio
);



// split not limited, added noise wander
Instr("bassEfx.deucePumpHalfFlatSlew",{ arg audio=0.0,freq=500, drive=1,balance=0.5,max=3.0,slew=12000;
	
	var lo,hi,loa,hia;
	lo = drive * (1.0 - balance);
	hi = drive * balance;

	# loa , hia = Instr.ar([\splits,\xover],[ audio,(freq + LFNoise1.kr(0.2,200)).max(60),lo * max,hi * max]);
	
	Slew.ar(
		Mix.new([
			 loa,
			 hia
		]).softclip
	,slew,slew)
},#[
	\audio,
	[60,10000,\exponential],
	[0,5.5],
	\pan,
	[0.01,5.0,\exponential],
	[19000,100,\exponential]

],
	\audio
);



Instr("bassEfx.rlpf",{  arg audio=0.0,ffreq=6000;
	RLPF.ar(audio,ffreq,0.5)
},[
	\audio,
	[50,16000]

],
	\audio
);



// has a nice phaseyness.  mids overlap
Instr("bassEfx.threePump",{ arg audio=0.0,freq=2000, halfwidth=500,mid=0.8,lo=0.8,hi=0.8,sweepfreq=1000;
	
	freq = max(freq,halfwidth + 100);
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		 HardShaper.ar( BPF.ar(audio,sweepfreq, 0.5 ) ,mid * 6.0),
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[10,3000],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,10000,\exponential]

	
],
	\audio
);



// interesting balances
Instr("bassEfx.threePumpResonz",{ arg audio=0.0,freq=2000, halfwidth=500,mid=0.8,lo=0.8,hi=0.8,sweepfreq=1000;
	
	freq = max(freq,halfwidth + 100);
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		 Resonz.ar(audio,sweepfreq, halfwidth * 2 / sweepfreq ,mid) ,
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[10,3000],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,10000,\exponential]

	
],
	\audio
);





// formlet 
Instr("bassEfx.threePumpFormlet",{ arg audio=0.0,freq=2000, halfwidth=500,mid=0.8,lo=0.8,hi=0.8,sweepfreq=1000,decay=0.01;
	
	freq = max(freq,halfwidth + 100);
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		 Formlet.ar(audio,sweepfreq, 0.0005,decay ,mid) ,
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[10,3000],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,3000,\exponential],
	[0.001,0.1,\exponential]

],
	\audio
);





// auto wah 
Instr("bassEfx.autoWah",{ arg audio=0.0,freq=2000, halfwidth=500,mid=0.8,lo=0.8,hi=0.8,sweepfreq=1000,decay=0.01,bounce=1000
			,attack=0.01,release=0.3;
	var freqmod;
	freq = max(freq,halfwidth + 100);
	freqmod=Amplitude.kr(Mono(audio),attack,release, bounce,sweepfreq);
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		 Formlet.ar(audio,freqmod, 0.0005,decay ,mid) ,
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[10,3000],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,3000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]

	
],
	\audio
);



// godlike 
Instr("bassEfx.autoWahWobble",{ arg audio=0.0,freq=2000, halfwidth=500,mid=0.8,lo=0.8,hi=0.8,wobbleWidth=1000,decay=0.01,bounce=1000
			,attack=0.01,release=0.3;
	var freqmod;
	freq = max(freq,halfwidth + 100);
	freqmod=Amplitude.kr(Mono(audio),attack,release, bounce,freq + LFNoise1.kr(0.1,wobbleWidth));
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		 Formlet.ar(audio,freqmod, 0.0005,decay ,mid) ,
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[10,3000],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,3000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]

],
	\audio
);




Instr("bassEfx.autoWahWobbleDLY",{ arg audio=0.0,freq=2000, mid=0.8,lo=0.8,hi=0.8,wobbleWidth=1000,decay=0.01,bounce=1000
			,release=0.3,feedback=0.3;

	var freqmod,halfwidth = 1000;
	freq = max(freq,halfwidth + 100);
	freqmod=Amplitude.kr(Mono(audio),0.1,release, bounce,freq + LFNoise1.kr(0.1,wobbleWidth));
	audio = NumChannels.ar(audio,2);
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.0005,decay ,mid) ,
			0.375,feedback,0.05,tempo:Tempo.kr
		),
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,3000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]

	
],
	\audio
);





Instr("bassEfx.autoWahWobbleDLYdust",{ arg audio=0.0,freq=2000, mid=0.8,lo=0.8,hi=0.8,
			wobbleWidth=1000,decay=0.01,bounce=1000,
			release=0.3,density=0.5;

	var freqmod,halfwidth = 1000;
	var feedback;
	
	freq = max(freq,halfwidth + 100);
	freqmod=Amplitude.kr(Mono(audio),0.1,release, bounce,freq + LFNoise1.kr(0.1,wobbleWidth));
	feedback=
		Decay2.kr(
			Dust.kr(density),
			0.1,density.reciprocal * 3);
	
	audio = NumChannels.ar(audio,2);
			
	Mix.new([
		 LPF.ar(audio,freq - halfwidth,lo * 9.0).softclip,
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.0005,decay ,mid) ,
			 0.375,feedback,0.05,tempo: Tempo.kr
		),
		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
	])

},#[
	\audio,
	[60,10000,\exponential],
	[0,1.5],
	[0,1.5],
	[0,1.5],
	[60,3000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]

	
],
	\audio
);




//
//Instr([\bassEfx,\autoWahWobbleDLYdust2],{ arg audio=0.0,freq=2000, mix,wobbleWidth=1000,decay=0.01,bounce=1000
//			,release=0.3,density=0.5;
//
//	var freqmod,halfwidth = 1000;
//	var feedback;
//	var mids;
//	
//	freq = max(freq,halfwidth + 100);
//	freqmod=Amplitude.kr(Mono(audio),0.1,release, bounce,freq + LFNoise1.kr(0.1,wobbleWidth));
//	feedback=
//		Decay2.kr(
//			Dust.kr(density),
//			0.1,density.reciprocal * 3);
//	mids = 
//	Mix.new([
//		 LPF.ar(audio,freq - halfwidth,lo * 9.0),
//		PingPongT.ar(
//			 Formlet.ar(audio,freqmod, 0.0005,decay ,mid) ,
//			 0.4,0.375,feedback,0.05
//		),
//		 HardShaper.ar( HPF.ar(audio,freq + halfwidth),hi * 6.0)
//	])
//
//},#[
//	\audio,
//	[60,10000,\exponential],
//	[0,1.5],
//	[0,1.5],
//	[0,1.5],
//	[60,3000,\exponential],
//	[0.001,0.1,\exponential],
//	[0,7000]

//]);






Instr("bassEfx.aBasicChannel",{ arg audio=0.0,
			lo=0.8,	lofreq,
			mid=0.8,	midfreq=1000,
			hi=0.8,	hifreq=9000,
			wobbleWidth=1000,
			decay=0.01,
			bounce=1000
			,feedbackDensity=0.5,tempo=1.0;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq  ;//Amplitude.kr(Mono(audio),0.1,release, bounce,midfreq + wobble);
	resmod = Amplitude.kr(Mono(audio),0.05,0.1);
	feedback=
		Decay2.kr(
			Dust.kr(feedbackDensity),
			0.1,feedbackDensity.reciprocal * 3);
	
	audio = NumChannels.ar(audio,2);
		
	Mix.new([
		 RLPF.ar(audio,Clip.kr(lofmod,60,9000),resmod,lo * 9.0).softclip,
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.005,decay ,mid) ,
			 0.375,feedback,0.05,1, tempo: tempo
		),
		 HardShaper.ar( HPF.ar(audio,hifreq),hi * 6.0)
	])

},#[
	\audio,
	[0,1.5],
	[300,4000,\exponential],
	[0,1.5],
	[60,6000,\exponential],
	[0,1.5],
	[500,10000,\exponential],
	[60,2000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]
	
],
	\audio
);



Instr("bassEfx.aBasicChannelFlat",{ arg audio=0.0,
			lo=0.8,	lofreq,
			mid=0.8,	midfreq=1000,
			hi=0.8,	hifreq=9000,
			wobbleWidth=1000,
			decay=0.01,
			bounce=1000
			,feedbackDensity=0.5,tempo=1.0;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq  ;//Amplitude.kr(Mono(audio),0.1,release, bounce,midfreq + wobble);
	resmod = Amplitude.kr(Mono(audio),0.05,0.1);
	feedback=
		Decay2.kr(
			Dust.kr(feedbackDensity),
			0.1,feedbackDensity.reciprocal * 3);
	
	audio = NumChannels.ar(audio,2);
			
	Mix.new([
		 RLPF.ar(audio,Clip.kr(lofmod,60,9000),resmod,lo * 9.0),
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.005,decay ,mid) ,
			 0.375,feedback,0.05,1,tempo: tempo
		),
		  HPF.ar(audio,hifreq,hi * 6.0)
	])

},#[
	\audio,
	[0,1.5],
	[300,4000,\exponential],
	[0,1.5],
	[60,6000,\exponential],
	[0,1.5],
	[500,10000,\exponential],
	[60,2000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]
	
],
	\audio
);




// clearer, but no crunch
// screwed up
Instr("bassEfx.aBasicChannel2",{ arg audio=0.0,
										lo=0.8,	lofreq,
										mid=0.8,	midfreq=1000,
										hi=0.8,	hifreq=9000,
										wobbleWidth=1000,
										bounce=1000,
										feedbackDensity=0.5,
										tempo=1.0;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble;
	var loa,mida,hia;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq ;
	feedback=
		Decay2.kr(
			Dust.kr(feedbackDensity),
			0.1,feedbackDensity.reciprocal * 3);
			
	# loa , mida, hia = Instr.ar([\splits,\xover3,\lp],[ audio, Clip.kr(lofmod,60,9000),hifreq ]);
	
	Mix.new([
		loa * lo ,
		PingPongT.ar(
			nil,
			 NumChannels.ar(mida * mid,2) ,
			 0.375,feedback,0.05,1,tempo: tempo
		),
		  hia * hi
	])

},#[
	\audio,
	[0,1.5],
	[300,4000,\exponential],
	[0,1.5],
	[60,6000,\exponential],
	[0,1.5],
	[500,10000,\exponential],
	[60,2000,\exponential],
	[0,7000]
	
],
	\audio
);



// clearer, but no crunch
// hilo balance
Instr("bassEfx.aBasicChannel3",{ arg audio=0.0,
									lofreq=300,
									hifreq=9000,
									lohidrive = 1.0,
									lohi = 0.0,
									mid = 1.0,
									wobbleWidth=1000,
									bounce=1000,
									feedbackDensity=0.5,
									tempo=1.0;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble;
	var loa,mida,hia;
	var lo,hi;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio).max(0.0001),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	feedback=
		Decay2.kr(
			Dust.kr(feedbackDensity),
			0.1,feedbackDensity.reciprocal * 3);
			
	# loa , mida, hia = Instr.ar([\splits,\xover3,\lp],[ audio, Clip.kr(lofmod,60,9000),hifreq ]);
	//# lo,hi = LinPan2.kr(lohidrive,lohi);
	lo = lohidrive * (1.0 - lohi);
	hi = lohidrive * lohi;

	
	
	Mix.new([
		loa * lo ,
		PingPongT.ar(
			nil,
			 NumChannels.ar(mida * mid,2) ,
			 0.375,feedback,0.05,1,tempo: tempo
		),
		  hia * hi
	])

},#[
	\audio,
	[400,1000,\exponential],
	[1200,10000,\exponential],
	[0,4],
	\mix,
	[0,4],
	[60,2000,\exponential],
	[0,7000]
	
],
	\audio
);



