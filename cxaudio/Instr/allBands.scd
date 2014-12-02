
/*
	split audio input into frequency bands
	treat in various ways
	remix and 
	return
*/

// mids all dust gated
Instr("allBands.threes-gated",{ arg audio=0.0,
					lo,lofreq,midDensity=0.5,midfreq=1000,hi,hifreq=9000,
					wobbleWidth=1000,decay=0.01,bounce=1000,tempo;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod,mid;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq  ;
	resmod = Amplitude.kr(Mono(audio),0.05,0.1);
	mid=
		Decay2.kr(
			Dust.kr(midDensity),
			0.1,midDensity.reciprocal * 1);

	feedback = 0.8; 
	
	audio = NumChannels.ar(audio,2);
				
	Mix.new([
		RLPF.ar(audio,Clip.kr(lofmod,60,9000),resmod,lo * 9.0).softclip,
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.005,decay ,mid) ,
			 0.375,feedback,0.04,1,tempo
		),
		 HardShaper.ar( HPF.ar(audio,hifreq),hi * 6.0)
	])

},#[
	\stereo,
	[0,1.5],
	[300,4000,\exponential],
	\unipolar,
	[60,6000,\exponential],
	[0,1.5],
	[500,10000,\exponential],
	[60,2000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]
	
],
	\stereoEffect
);





// all three all dust gated
Instr("allBands.threes-alldusted",{ arg audio=0.0,
					lo=0.5,lofreq=800,lodensity=0.5,
					mid=0.5,midfreq=1000,middensity=0.5,
					hi=0.5,hifreq=9000,hidensity=0.5,
					wobbleWidth=1000,decay=0.01,bounce=1000,tempo;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq  ;
	resmod = Amplitude.kr(Mono(audio),0.05,0.1);
	
	lo = 	
		Decay2.kr(
			Dust.kr(lodensity),
			0.1,lodensity.reciprocal,lo );
	mid=
		Decay2.kr(
			Dust.kr(middensity),
			0.1,middensity.reciprocal,mid );

	feedback = 0.8; 
		
	hi =
		Decay2.kr(
			Dust.kr(hidensity),
			0.1,hidensity.reciprocal,hi );
	
	audio = NumChannels.ar(audio,2);
		
	Mix.new([
	   RLPF.ar(audio,Clip.kr(lofmod,60,9000),resmod,lo * 9.0).softclip,
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.005,decay ,mid) ,
			 0.375,feedback,0.03,1,tempo
		),
		 HardShaper.ar( HPF.ar(audio,hifreq),hi * 6.0)
	])

},#[
	\audio,

	[0,1.5],
	[300,4000,\exponential],
	\unipolar,
	[0,1.5],
	[60,6000,\exponential],
	\unipolar,
	[0,1.5],
	[500,10000,\exponential],
	\unipolar,

	[60,2000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000]
	
],
	\stereoEffect
);





// all three all dust gated
// rlpf does res mod
Instr("allBands.threes-lowAnd2Delays",{ 
		arg audio=0.0,
			lo=0.5,lofreq=800,lodensity=0.5,
			mid=0.5,midfreq=1000,middensity=0.5,
			hi=0.5,hifreq=9000,hidensity=0.5,
			wobbleWidth=1000,decay=0.01,bounce=1000,tempo;

	var freqmod;
	var feedback;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	lofmod = ampl   + lofreq ;
	freqmod= ampl +  midfreq  ;
	resmod = Amplitude.kr(Mono(audio),0.05,0.1);
	
	lo = 	
		Decay2.kr(
			Dust.kr(lodensity),
			0.1,lodensity.reciprocal * 3,lo );
	mid=
		Decay2.kr(
			Dust.kr(middensity),
			0.1,middensity.reciprocal,mid );

	feedback = 0.8; 
		
	hi =
		Decay2.kr(
			Dust.kr(hidensity),
			0.1,hidensity.reciprocal,hi );
	
	audio = NumChannels.ar(audio,2);
		
	Mix.new([
	   RLPF.ar(audio,Clip.kr(lofmod,60,9000),resmod,lo * 9.0),
		PingPongT.ar(nil,
			 Formlet.ar(audio,freqmod, 0.005,decay ,mid) ,
			0.375,feedback,0.03,1,tempo
		),
		PingPongT.ar(nil,
			 HardShaper.ar( HPF.ar(audio,hifreq),hi * 6.0),
			 0.25,feedback,0.03,1,tempo
		)
	])

},#[
	\audio,

	[0,1.5],
	[300,4000,\exponential],
	\unipolar,
	[0,1.5],
	[60,6000,\exponential],
	\unipolar,
	[0,1.5],
	[500,10000,\exponential],
	\unipolar,

	[60,2000,\exponential],
	[0.001,0.1,\exponential],
	[0,7000],
	\tempo
],
	\stereoEffect
);






// 2 channels only
//  all dust gated
Instr("allBands.twos.bothDusted",{ 
		arg audio=0.0,
			lo=0.5,lofreq=800,
			hi=0.5,hifreq=9000,
			density=0.5,overlap=0.3,
			wobbleWidth=1000,decay=0.01,bounce=1000,tempo;

	var freqmod;
	var feedback=0.8;
	var sensitivity = 2;
	var lofmod;
	var ampl,wobble,resmod;
	
	wobble = LFNoise1.kr(0.1,wobbleWidth);
	ampl = Amplitude.kr(Mono(audio),0.1,0.3,bounce,wobble);
	overlap = density.reciprocal * overlap;
	
	lo = 	
		Decay2.kr(
			Dust.kr(density),
			0.1, overlap,lo );
		
	hi =
		Decay2.kr(
			Dust.kr(density),
			0.1, overlap,hi );
	
	audio = NumChannels.ar(audio,2);
	//audio = NumChannels.ar(audio,buffer.numChannels);

	Mix.new([
		PingPongT.ar(nil,
			 Formlet.ar(audio,ampl + lofreq, 0.005,decay ,lo) ,
			 0.375,feedback,0.03,1,tempo
		),
		PingPongT.ar(nil,
		 	Formlet.ar(audio,ampl + hifreq, 0.005,decay ,hi) ,
				 0.25,feedback,0.03,1,tempo
		)
	])

},#[
	\audio,

	[0,1.5],
	[300,4000,\exponential],

	[0,1.5],
	[60,6000,\exponential],

	\unipolar,
	\unipolar,

	[5,2000,\exponential],
	[0.001,0.1,\exponential],
	[-7000,7000],
	\tempo
],
	\stereoEffect
);



// compressed bass
//
// rhythmically intelligent gating
//	// or amp sensitive threshold for dust
//
// two or three audio inputs each bpf to an area
//	// dusted
//	// trigged


Instr([\allBands,\slewlock],{ arg audio,xover1=1200,xover2=7000,slope=500,indrive=100,inputGate= -1,
								feedback=0.8,windowsize=0.1,overlap=3.0,lov=1.0,midv=1.0,hiv=1.0;
								
	var lo,mid,hi;
	
	# lo, mid, hi = Instr.ar([\splits,\xover3,\lp],[ audio,xover1,xover2 ]);
	
	mid = Instr.ar([\slews,\backup2],[mid,slope,indrive]);
	
	Mix.new([ lo * lov, mid * midv,hi * hiv ])
	
},#[
	\audio,
	[50,2000,\exponential],
	[3000,10000,\exponential],
	[10,4000,\exponential],
	[1,4000],
	\mix,
	\feedback,
	[0.01,0.4],
	[0.1,8.0],
	[0.0,3.0],
	[0.0,3.0],
	[0.0,3.0]

],
	\stereoEffect
);




