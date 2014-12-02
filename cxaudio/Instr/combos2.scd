

// 2 inputs
//  all dust gated delayed
// dust seems to be working odd.

Instr("combos2.dustedDelayed",{ 
		arg audio1=0.0, audio2=0.0,
			onefreq=1000,	twofreq=2000,
			wobbleWidth=1000,
			density1=0.5,	density2=0.5,	overlap=0.3,tempo;

	var wobble1,wobble2,feedback=0.8;
	var one,two,input1,input2;
	
	wobble1 = LFNoise1.kr(0.1,wobbleWidth);
	wobble2 = LFNoise1.kr(0.1,wobbleWidth);
	
	one = 	
		Decay2.kr(
			Dust.kr(density1),
			0.1, density1.reciprocal * overlap );
		
	two =
		Decay2.kr(
			Dust.kr(density2),
			0.1, density2.reciprocal * overlap );
	
	input1 = 	Clip.ar( RBPF.ar(audio1,(wobble1 + onefreq), 40,19000));
	input2 = Clip.ar( RBPF.ar(audio2,(wobble2 + twofreq), 40,19000));
	
	Mix.new([
		PingPongT.ar(nil,
			NumChannels.ar(input1, 2),
			 0.375,feedback,0.03,tempo: tempo
		),
		PingPongT.ar(nil,
			NumChannels.ar(input2, 2),
			0.25,feedback,0.03, tempo: tempo
		)
	])

},#[
	\audio,
	\audio,
	\ffreq,
	\ffreq,
	[0,4000],
	\unipolar,
	\unipolar,
	\unipolar,
	\tempo
	
],
	\dualStereoEffect
);


/*
	XY is for use with tablets or joysticks.
	2 controls
	xy may be labelled as : ab / hilo
*/

	
// xy intended xfade
Instr("combos2.xy",{ 
		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 3000;
	
	var hi,lo,lomix,himix;
	
	# lo, hi = LinPan2.ar(K2A.ar(drive),hilo);
	
	lomix = XFader.ar( 
			LPF.ar(audio1,freq,lo * 9.0).softclip, 
			LPF.ar(audio2,freq,lo * 9.0).softclip, 
			ab);
	
	himix = XFader.ar( 
			 HardShaper.ar( HPF.ar(audio1,freq),hi * 6.0),
			 HardShaper.ar( HPF.ar(audio2,freq),hi * 6.0),
			 ab);

	Mix.new([
		lomix,himix
	])
	

},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	\ffreq	
],
	\dualStereoEffect
);




// xy intended xfade // NOT FLAT
Instr("combos2.xyFlat",{ 
		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 3000;
	
	var hi,lo,lomix,himix;
	var lo1,lo2;
	# lo, hi = LinPan2.ar(K2A.ar(drive),hilo);
	
	lo1=LPF.ar(audio1,freq); 
	lo2=LPF.ar(audio2,freq );
	
	lomix = XFader.ar( 
			lo1,lo2,
			ab);
	
	himix = XFader.ar( 
			audio1 - lo1 ,
			  audio2 - lo2 ,
			 ab);

	Mix.new([
		lomix * lo,himix * hi
	])

},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	\ffreq	
],
	\dualStereoEffect
);




// xy intended xfade
Instr("combos2.xyFlatRev",{		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 3000;
	
	var hi,lo,lomix,himix;
	var lo1,lo2,hi1,hi2;
	// #  hi , lo = LinPan2.ar(drive,hilo);
	
	lo1=LPF.ar(audio1,freq); 
	lo2=LPF.ar(audio2,freq );
	hi1 = audio1  - lo1;
	hi2 = audio2 - lo2;
	
	lomix = LinXFade2.ar( 
			lo1,hi2,
			ab);
	
	himix = LinXFade2.ar( 
			hi1 ,lo2,
			 hilo);

	Mix.new([
		lomix,himix
	])

},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	\ffreq	
],
	\dualStereoEffect
);




// the best !
Instr("combos2.xy4Flat",{ 
		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 3000;
	
	var lo1,lo2,hi1,hi2;
	var lo1vol,lo2vol,hi1vol,hi2vol;
		var iab,ihilo,driveab,driveiab;

	//var a,b,c,d;
	//#  hi1vol ,  lo1vol,hi2vol, lo2vol = Pan4.ar( drive,ab,hilo);

	ab = ab * 0.5 + 0.5;
	iab = 1 - ab;
	hilo = hilo * 0.5 + 0.5;
	ihilo = 1 - hilo;
	driveab = drive * ab;
	driveiab = drive * iab;
	hi1vol = driveiab * (ihilo);
	lo1vol = driveab * (ihilo);
	hi2vol = driveiab * hilo;
	lo2vol = driveab * hilo;
	
		
	lo1=LPF.ar(audio1,freq ); 
	lo2=LPF.ar(audio2,freq ); 
			
	Mix.new([
		lo1 * lo1vol, 
		lo2 * lo2vol,
		 audio1 - lo1 *  hi1vol ,
		 audio2 - lo2 * hi2vol 
	]);


},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	\ffreq	
],
	\dualStereoEffect
);



// the best !
Instr("combos2.xy4Slew",{ 
		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 500;
	
	var lo1,lo2,hi1,hi2;
	var lo1vol,lo2vol,hi1vol,hi2vol;
	
	//var a,b,c,d;
	//#  hi1vol ,  lo1vol,hi2vol, lo2vol = Pan4.ar( drive,ab,hilo);
	var iab,ihilo,driveab,driveiab;

	ab = ab * 0.5 + 0.5;
	iab = 1 - ab;
	hilo = hilo * 0.5 + 0.5;
	ihilo = 1 - hilo;
	driveab = drive * ab;
	driveiab = drive * iab;
	hi1vol = driveiab * (ihilo);
	lo1vol = driveab * (ihilo);
	hi2vol = driveiab * hilo;
	lo2vol = driveab * hilo;
		
	lo1=Slew.ar(audio1,freq,freq ); 
	lo2=Slew.ar(audio2,freq,freq ); 
			
	Mix.new([
		lo1 * lo1vol, 
		lo2 * lo2vol,
		 audio1 - lo1 *  hi1vol ,
		 audio2 - lo2 * hi2vol 
	]);


},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	[1,1000,\exponential]
	
],
	\dualStereoEffect
);



Instr("combos2.xy4rev",{ 
		arg audio1=0.0,		audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 3000;
	
	var lo1,lo2,hi1,hi2;
	var lo1vol,lo2vol,hi1vol,hi2vol;
	//var a,b,c,d;
	//#  hi1vol ,  lo1vol,hi2vol, lo2vol = Pan4.ar( drive,ab,hilo);
	var iab,ihilo,driveab,driveiab;

	ab = ab * 0.5 + 0.5;
	iab = 1 - ab;
	hilo = hilo * 0.5 + 0.5;
	ihilo = 1 - hilo;
	driveab = drive * ab;
	driveiab = drive * iab;
	hi1vol = driveiab * (ihilo);
	lo1vol = driveab * (ihilo);
	hi2vol = driveiab * hilo;
	lo2vol = driveab * hilo;

	lo1=LPF.ar(audio1,freq ); 
	lo2=LPF.ar(audio2,freq ); 
			
	Mix.new([
		lo1 * lo1vol, 
		lo2 * lo2vol,
		 audio1 - lo1 *  hi1vol ,
		 audio2 - lo2 * hi2vol 
	]);

},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,2.0],
	\ffreq	
],
	\dualStereoEffect
);



// the best !
Instr("combos2.xy4SlewRev",{ 
		arg audio1=0.0, audio2=0.0,
			ab = -1, hilo = -1,
			drive = 0.5, freq = 500;
	
	var lo1,lo2,hi1,hi2;
	var a,b,c,d;
	var iab,ihilo,driveab,driveiab;
	
	//#  a ,b, c, d = Pan4.ar( drive,ab,hilo);
//	# a, b, c, d = Pan4.ar( K2A.ar(drive), ab, hilo );
	ab = ab * 0.5 + 0.5;
	iab = 1 - ab;
	hilo = hilo * 0.5 + 0.5;
	ihilo = 1 - hilo;
	driveab = drive * ab;
	driveiab = drive * iab;
	a = driveiab * (ihilo);
	b = driveab * (ihilo);
	c = driveiab * hilo;
	d = driveab * hilo;
	
	lo1=Slew.ar(audio1,freq,freq ); 
	lo2=Slew.ar(audio2,freq,freq ); 

			
	Mix.new([
		lo1 * c,
		lo2 * b,
		 audio1 - lo1 *  a ,
		 audio2 - lo2 * d 
	]);
	lo1
},#[
	\audio,
	\audio,
	\bipolar,
	\bipolar,
	[0.0,5.0],
	[1,1000,\exponential]
	
],
	\dualStereoEffect
);






// 
Instr("combos2.xy4SlewRev2",{ 
		arg audio1=0.0,		audio2=0.0,
			x = 0, y = 0,
			drive = 0.5, freq = 500;
	
	var lo1,lo2,hi1,hi2;
	
	lo1=Slew.ar(audio1,freq,freq ); 
	lo2=Slew.ar(audio2,freq,freq ); 
	hi1 = audio1 - lo1;
	hi2 = audio2 - lo2;

	Mix.new([
		lo1 * y, 
		lo2 * (1.0 - y),
		 hi1 *  ( 1.0 - x) ,
		 hi2 * x 
	]);


},#[
	\audio,
	\audio,
	\unipolar,
	\unipolar,
	[0.0,10.0],
	[1,1000,\exponential]
	
],
	\dualStereoEffect
);



