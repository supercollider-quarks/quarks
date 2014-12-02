

Instr("mastering.limiter",{ arg audio=0.0,drive=1.0;
	Limiter.ar(
		audio * drive
	)
},
	nil,
	\audio
);






Instr("mastering.unslewlimiter",{ arg audio=0.0,drive=1.0,unslewv=1.5,unslewf=400;

	audio = Instr.ar([\slews,\balance],[audio,unslewf,unslewv,1.0]);

	Limiter.ar(
		audio * drive
	)
},[
	\audio,
	\drive,
	\gain,
	\ffreq
],
	\audio
);



// mids drive the highs

Instr("mastering.midTrans2High",{ arg audio=0.0,xover1=400,xover2=8000, sensitivity = 0.1,logain=1.0,midgain=1.0,higain=1.0;

	var lo,mid,hi;
	
	# lo, mid, hi = Instr.ar("splits.xover3.lp",[audio,xover1,xover2]);
	
	hi =	Instr.ar([\dyna,\crossDriveTrans],[hi,mid,sensitivity]);
	
	Limiter.ar(
		Mix.new([ lo * logain, mid * midgain,hi * higain ])
	)
	
},#[
	\audio,
	\freq,
	\freq,
	[0.0,0.4],
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

	
],
	\audio
);




// + special case remove clicks
// mids drive the highs

Instr("mastering.midTrans2HighMedian",{ arg audio=0.0,xover1=400,xover2=8000, sensitivity = 0.1,logain=1.0,midgain=1.0,higain=1.0;

	var lo,mid,hi;
	
	# lo, mid, hi = Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2]);
	
	hi = Median.ar(7,hi);
	
	hi =	Instr.ar([\dyna,\crossDriveTrans],[hi,mid,sensitivity]);
	
	Limiter.ar(
		Mix.new([ lo * logain, mid * midgain,hi * higain ])
	)
	
},#[
	\audio,
	\freq,
	\freq,
	[0.0,0.4],
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	\audio
);



/**
	 + special case remove clicks from hi
	 mids drive the highs by normal level
  ( not transient driven )
**/

//Instr("mastering.mid2HighMedian",{ arg audio=0.0,xover1=900,xover2=7139, slewv = 1.0,
//					logain=1.4,midgain=1.0,higain=1.0,
//					attack=0.01,release=0.1,stereo=2.0,
//					midthresh=0.4,midslope=0.3;
//
//	var lo,mid,hi;
//	
//	# lo, mid, hi = Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2]);
//	
//	lo = Instr.ar([\dynamics,\shaper],[lo]);
//	lo = (lo * logain).softclip;
//	
//	mid = Median.ar(3,mid);
//	mid = Instr.ar([\stereo,\bal2CompSides],[mid,1.0,1.3,0.3,0.3]);
//	//mid = Instr.ar([\dynamics,\shaper],[mid]);
//	mid = Compander.ar(mid,mid.max(0.0001),midthresh,1,midslope,0.01,0.2);
//	
//	hi = Median.ar(3,hi);
//	hi = Instr.ar([\stereo,\verbSides],[hi,1.0,stereo,0.05]);
//	// squash
//	hi = Compander.ar(hi,hi.max(0.0001),0.4,1,0.4,0.01,0.2);
//	//hi = Mix.new([ PitchShift.ar( hi,0.02,2,0.3),hi]);
//	// and drive off mid 5k
//	//hi =	Instr.ar([\dyna,\audioByKey],[hi,HPF.ar(mid,1000),sensitivity,attack,release]);
//	hi = Instr.ar([\slews,\balance],[ hi,300,slewv,1.0]);
//	
//	Limiter.ar(
//		 Mix.new([ lo , mid * midgain,hi * higain ])
//	)
//	
//},#[
//	\audio,
//	\freq,
//	\freq,
//	[0.0,15.0],
//	[0.0,4.0],
//	[0.0,2.0],
//	[0.0,4.0],
//	\attack,
//	\release,
//	\drive
//],
//	\audio
//);


Instr("mastering.medianFinalizer",{ arg audio=0.0,xover1=900,xover2=7139, slewv = 1.0,
					logain=1.4,midgain=1.0,higain=1.0,
					attack=0.01,release=0.1,stereo=2.0,
					midthresh=0.4,midslope=0.3,
					lgthresh,lgslope,lcthresh,lcslope,
					hgthresh,hgslope,hcthresh,hcslope;

	var lo,mid,hi;
	
	audio = Median.ar(3,audio);
	
	# lo, mid, hi = Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2]);
	
	
	lo = Instr.ar([\dynamics,\gatecomp],[lo,lgthresh,lgslope,lcthresh,lcslope]);
	lo = (lo * logain).softclip;
	
	mid = Instr.ar([\stereo,\bal2CompSides],[mid,1.0,1.3,0.3,0.3]);
	mid = Instr.ar([\dynamics,\gatecomp],[mid,0.1,1,midthresh,midslope]);
	
	hi = Instr.ar([\dynamics,\gatecomp],[hi,hgthresh,hgslope,hcthresh,hcslope]);
	hi = Instr.ar([\stereo,\verbSides],[hi,1.0,stereo,0.05]);
	hi = Instr.ar([\slews,\balance],[ hi,300,slewv,1.0]);
	
	
	Limiter.ar(
		 Mix.new([ lo , mid * midgain,hi * higain ])
	)
	
},#[
	\audio,
	\freq,
	\freq,
	[0.0,15.0],
	[0.0,4.0],
	[0.0,2.0],
	[0.0,4.0],
	\attack,
	\release,
	\drive,
	
	//m
	[0.001,1.0],
	[0.01,1.0],
	
	//l
	[0.001,1.0],
	[0.1,100],
	[0.001,1.0],
	[0.05,1.0],
	// h
	[0.001,1.0],
	[0.1,100],
	[0.001,1.0],
	[0.05,1.0]



],
	\audio
);




//// slow leveling
//Instr([\mastering,\level],{ arg audio=0.0,xover1=400,xover2=8000,attack=0.5,release=1.0,logain=1.0,midgain=1.0,higain=1.0;
//
//	var lo,mid,hi;
//	
//	# lo, mid, hi = Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2]);
//	
//	hi =	Instr.ar([\dyna,\crossDriveTrans],[hi,mid,sensitivity]);
//	
//	Limiter.ar(
//		Mix.new([ lo * logain, mid * midgain,hi * higain ])
//	)
//},#[
//	\audio,
//	\freq,
//	\freq,
//	[0.0,0.4],
//	[0.0,2.0],
//	[0.0,2.0],
//	[0.0,2.0]

//	
//]);
//


// slew and compress bass
// compress mids

Instr("mastering.soak",{ arg audio,slope=500,lov=1.0,midv=1.0,hiv=1.4;
	
	var lo,mid,hi;
	
	# lo , mid, hi = Instr.ar([\splits,\xover3,\lp],[audio,400,7000]);
	
	lo = Instr.ar([\dynamics,\compressor],[lo,0.5,0.4]);
	lo = Slew.ar(lo,slope,slope,lov);
	mid = Instr.ar([\dynamics,\compressor],[mid,0.5,0.5]);
	
	Limiter.ar(Mix.new([lo ,mid,hi * hiv]))  
},#[
	\audio,
	[0.1,500,\exponential],
	[0.0,3.0],
	[0.0,3.0],
	[0.0,3.0]


],
	\audio
);


Instr("mastering.simple3Chan",{arg audio=0.0,xover1=400,xover2=9000,lov=1.0,midv=1.0,hiv=1.0,
									threshold=0.5,slope=0.5,drive=1.0;

	var lo,mid,hi;
	#lo,mid,hi=Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2, lov,midv,hiv]);
	
	audio=Mix.new([lo,mid,hi]);
	
	audio=Instr.ar([\dynamics,\compressor],[audio,threshold,slope]);
	
	Limiter.ar(audio * drive)

},#[
	\audio,
	[40,3000,\exponential],
	[4000,16000,\exponential],
	[0.0,4.0],
	[0.0,4.0],
	[0.0,4.0]

],
	\audio
);





Instr("mastering.compcompunslew",{ arg audio=0.0,xover1=400,xover2=9000,lov=1.0,midv=1.0,hiv=1.0,
											threshold=0.5,slope=0.5,unslewf=400,unslewv=1.0,drive=1.0;

	var lo,mid,hi;
	var lomid;
	audio = Instr.ar([\slews,\balance],[audio,unslewf,unslewv,1.0]);
	
	#lo,mid,hi=Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2,lov,midv]);
	
	lomid=Instr.ar([\dynamics,\compressor],[Mix.new([lo,mid]),threshold,slope]);
	
	audio = Mix.new([lomid,hi * hiv]);
	
	Limiter.ar(audio * drive)

},#[
	\audio,
	[40,3000,\exponential],
	[4000,16000,\exponential],
	[0.0,4.0],
	[0.0,4.0],
	[0.0,4.0],
	\threshold,
	\slope,
	\freq,
	\gain
],
	\audio
);




Instr("mastering.Mediansimple3Chan",{ arg audio=0.0,xover1=400,xover2=9000,lov=1.0,midv=1.0,hiv=1.0,
												threshold=0.5,slope=0.5,drive=1.0;

	var lo,mid,hi;
	audio = Median.ar(3,audio);
	#lo,mid,hi=Instr.ar([\splits,\xover3,\lp],[audio,xover1,xover2, lov,midv,hiv]);
	
	audio=Mix.new([lo,mid,hi]);
	
	audio=Instr.ar([\dynamics,\compressor],[audio,threshold,slope]);
	
	Limiter.ar(audio * drive)

},#[
	\audio,
	[40,3000,\exponential],
	[4000,16000,\exponential],
	[0.0,4.0],
	[0.0,4.0],
	[0.0,4.0]

],
	\audio
);



