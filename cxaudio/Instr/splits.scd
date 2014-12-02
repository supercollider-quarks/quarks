
// these return more than one track
// they take a stereo in and return an array of stereo outs

Instr("splits.two",{ arg audio=0.0,lpf=200, lprq=0.5,hipass=100;

	var lo,hi;

	lo = RLPF.ar( audio, lpf,lprq);
	hi = HPF.ar( audio, hipass);

	[ lo, hi]

},[
	\audio,
	[50,10000,\exponential],
	[0.1,0.9],
	[100,7000,\exponential]

],
	MultiTrackAudioSpec(2,2)
);




Instr("splits.xover",{ arg audio=0.0,xover=200,logain=1.0,higain=1.0;
						
	var lo,hi;
		
	lo = LPF.ar( audio, xover);
	hi = audio - lo;
	if(logain != 1.0,{ lo = lo * logain });
	if(higain != 1.0,{ hi = hi * higain });
	
	[lo,hi]

},[
	\audio,
	\freq,
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(2,2)
);


Instr("splits.rxover",{ arg audio=0.0,xover=200,rq=0.5,logain=1.0,higain=1.0;
	var lo;
	lo = RLPF.ar( audio, xover,rq);
	[ lo * logain, audio - lo * higain]

},[
	\audio,
	\ffreq,
	\rq,
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(2,2)
);
/*
Instr([\splits,\r4xover],{ arg audio=0.0,xover=0.5,rq=0.5,logain=1.0,higain=1.0;
	var lo;
	lo = RLPF4.ar( audio, xover,rq);
	[ lo * logain, audio - lo * higain]

},[
	\audio,
	\unipolar,
	\rq,
	[0.0,2.0],
	[0.0,2.0]

]);
*/

Instr("splits.bpf",{ arg audio=0.0,ffreq=200,rq8=1.0;
						
	var chan;
	chan = BPF.ar( audio, ffreq,rq8);
	[chan,audio - chan]
},
	nil,
	MultiTrackAudioSpec(2,2)
);


Instr([\splits,\xover3,\lp],{ arg audio=0.0,xover1=200,xover2=4000,logain=1.0,midgain=1.0,higain=1.0;
		
		var lo,mid,hi,rest;
		lo = RLPF.ar(audio,xover1,0.4);// cmon you know you want it
		rest = audio - lo;
		mid = LPF.ar(rest,xover2);
		hi = rest - mid;
		
		lo = if(logain==1.0,{lo},{lo*logain});
		mid = if(midgain==1.0,{mid},{mid*midgain});
		hi = if(higain==1.0,{hi},{hi*higain});
		
	[ lo, mid, hi ]
},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);


Instr([\splits,\xover3,\slew],{ arg audio=0.0,slope=200,xover2=4000,logain=1.0,midgain=1.0,higain=1.0;
		
		var lo,mid,hi,rest;
		lo = Slew.ar(audio,slope,slope);
		rest = audio - lo;
		mid = LPF.ar(rest,xover2);
		hi = rest - mid;
		
		lo = if(logain==1.0,{lo},{lo*logain});
		mid = if(midgain==1.0,{mid},{mid*midgain});
		hi = if(higain==1.0,{hi},{hi*higain});
		
	[ lo, mid, hi ]
},[
	\audio,
	[10,1200,\exponential],
	[3000,9000,\exponential],
	
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);


// loses a lot of delicacy
Instr([\splits,\xover3,\bpf],{ arg audio=0.0,xover1=200,xover2=4000,logain=1.0,midgain=1.0,higain=1.0;
		
		var lo,mid,hi,rest;
		mid =BPFbyFreq.ar( audio, xover1,xover2);
		rest = audio - mid;
		hi = HPF.ar(rest,xover2,higain);
		lo = LPF.ar( rest,xover1,logain);				
	
	[ 	lo
		, mid * midgain
		, hi
	]
},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);

// xtract mid by resonz
Instr([\splits,\xovermid,\resonz],{ arg audio=0.0,xover1=200,xover2=4000;
						
	var lo,hi,mid,rest;
	
	mid = ResonzByFreq.ar( audio, xover1,xover2);
	rest = audio - mid;
	
	[ mid,  rest]

},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(2,2)
);

Instr([\splits,\xover3,\resonz],{ arg audio=0.0,xover1=200,xover2=4000;
						
	var lo,hi,mid,rest;
	
	mid = ResonzByFreq.ar( audio, xover1,xover2);
	rest = audio - mid;
	lo = LPF.ar( rest, xover1);
	hi = rest - lo;
	
	[ lo,mid,  hi]

},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);



Instr([\splits,\xover3,\rbpf],{ arg audio=0.0,xover1=200,xover2=4000,rq=0.5,logain=1.0,midgain=1.0,higain=1.0;
						
	var lo,hi,mid;
	
	lo = RLPF.ar( audio, xover1,rq,logain);
	mid = RBPF.ar( audio, xover1,xover2,rq, midgain);
	hi = HPF.ar( audio, xover2,higain);
	
	[ lo,mid,  hi]

},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	\rq,
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);


// cant modulate the low band without ugly little noise
Instr([\splits,\xover3,\rrbpf],{ arg audio=0.0,xover1=200,xover2=4000,rq=0.5,logain=1.0,midgain=1.0,higain=1.0;
						
	var lo,hi,mid;
	
	lo = RLPF.ar( audio, xover1,rq,logain);
	mid = RRBPF.ar( audio, xover1,xover2,rq, 0.3,midgain);
	hi = HPF.ar( audio, xover2,higain);
	
	[ lo,mid,  hi]

},[
	\audio,
	[200,3000,\exponential],
	[3000,9000,\exponential],
	
	\rq,
	[0.0,2.0],
	[0.0,2.0],
	[0.0,2.0]

],
	MultiTrackAudioSpec(3,2)
);





Instr("splits.xoverBalancer",{ arg audio,xover=500,lo=1,hi=1;
	var loa;
	loa = LPF.ar(audio,xover);
	Mix.new([loa * lo, audio - loa * hi ])
},#[
	\audio,
	[100,12000,\exponential]

],
	\audio
);


// same as \lp
//Instr([\splits,\xover3,\xover],{ arg audio=0.0,xover1=400,xover2=5000;
//						
//	var lo,hi,mid,midhi;
//	
//	lo = LPF.ar(audio,xover1);
//	midhi = audio - lo;
//	mid = LPF.ar(midhi,xover2);
//	hi = midhi - mid;
//	
//	[ lo,mid,  hi]
//
//},[
//	\audio,
//	\ffreq,
//	\ffreq,
//	
//	\rq,
//	\rq
//]);

/** minuser ?

Instr([\splits,\xover3,\xoverBalancer],{ arg audio=0.0,xover1=400,xover2=5000,lo=1,mid=1,hi=1;
	var split;
	split = Instr.ar([\splits,\xover3,\minuser],[audio,xover1,xover2]);
	Mix.new([
			split.at(0) * lo,
			split.at(1) * mid,
			split.at(2) * hi
		])
},#[
	\audio,
	\ffreq,
	\ffreq,
	
	\rq,
	\rq,
	[0.0,1.5],
	[0.0,1.5],
	[0.0,1.5]

],MultiTrackAudioSpec(3,2));

**/


Instr([\splits,\xover3,\resMinuser],{ arg audio=0.0,xover1=400,xover2=5000,rq1=0.5,rq2=0.5;
						
	var lo,hi,mid,midhi;
	
	lo = RLPF.ar(audio,xover1,rq1);
	midhi = audio - lo;
	mid = RLPF.ar(midhi,xover2,rq2);
	hi = mid - midhi;
	
	[ lo,mid,  hi]

},[
	\audio,
	\ffreq,
	\ffreq,
	
	\rq,
	\rq
],MultiTrackAudioSpec(3,2)
);


// mixes it back together
Instr([\splits,\xover3,\resMinuserBalancer],{ arg audio=0.0,xover1=400,xover2=5000,rq1=0.5,rq2=0.5,lo=1,mid=1,hi=1;
	var split;
	split = Instr.ar([\splits,\xover3,\resMinuser],[audio,xover1,xover2,rq1,rq2]);
	Mix.new([
			split.at(0) * lo,
			split.at(1) * mid,
			split.at(2) * hi
		])
},#[
	\audio,
	\ffreq,
	\ffreq,
	
	\rq,
	\rq,
	[0.0,1.5],
	[0.0,1.5],
	[0.0,1.5]

],
	\audio
);


/*
Instr([\splits,\xover3,\res4Minuser],{ arg audio=0.0,xover1=0.3,xover2=0.7,rq1=0.5,rq2=0.5;
						
	var lo,hi,mid,midhi;
	lo = RLPF4.ar(audio,xover1,rq1);
	midhi = audio - lo;
	mid = RLPF4.ar(midhi,xover2,rq2);
	hi = mid - midhi;
	[ lo,mid,  hi]
},[
	\audio,
	\unipolar,
	\unipolar,
	
	\rq,
	\rq
]);



Instr([\splits,\xover3,\res4MinuserBalancer],{ arg audio=0.0,xover1=0.3,xover2=0.7,rq1=0.5,rq2=0.5,lo=1,mid=1,hi=1;
	var split;
	split = Instr.ar([\splits,\xover3,\res4Minuser],[audio,xover1,xover2,rq1,rq2]);
	Mix.new([
			split.at(0) * lo,
			split.at(1) * mid,
			split.at(2) * hi
		])
},#[
	\audio,
	\unipolar,
	\unipolar,
	
	\rq,
	\rq,
	[0.0,1.5],
	[0.0,1.5],
	[0.0,1.5]

]);

*/




Instr([\splits,\xover3,\resMinuserBouncer],{ arg audio=0.0,xover1=400,xover2=5000,rq1=0.5,rq2=0.5,lobounce=500;
						
	var lo,hi,mid,midhi,bounce;
	bounce = Amplitude.kr(audio,0.05,0.2,mul: lobounce);
	lo = RLPF.ar(audio,xover1 + bounce,rq1);
	midhi = audio - lo;
	mid = RLPF.ar(midhi,xover2,rq2);
	hi = mid - midhi;
	
	[ lo,mid,  hi]

},[
	\audio,
	\ffreq,
	\ffreq,
	
	\rq,
	\rq
],
	MultiTrackAudioSpec(3,2)

);




Instr([\splits,\xover3,\resMinuserBouncerBalancer],{ arg audio=0.0,xover1=400,xover2=5000,rq1=0.5,rq2=0.5,lo=1,mid=1,hi=1,bounce=500;
	var split;
	split = Instr.ar([\splits,\xover3,\resMinuserBouncer],[audio,xover1,xover2,rq1,rq2,bounce]);
	Mix.new([
			split.at(0) * lo,
			split.at(1) * mid,
			split.at(2) * hi
		])
},#[
	\audio,
	\ffreq,
	\ffreq,
	
	\rq,
	\rq,
	[0.0,1.5],
	[0.0,1.5],
	[0.0,1.5],
	[0.1,6000,\exponential]

],
	\audio
);


/*

Instr([\splits,\fft],{ arg audio=0.0;

	var fft,r,i;	
	fft =	FFT.ar(1024,0,nil,nil,nil,audio);
	r = fft.collect({ arg c; c.real });
	i = fft.collect({ arg c; c.imag }); 
[
	IFFT.ar(256,0,nil,nil,nil,r.copyRange(0,255),i.copyRange(0,255)),
	IFFT.ar(256,0,nil,nil,nil,r.copyRange(256,511),i.copyRange(256,511)),
	IFFT.ar(256,0,nil,nil,nil,r.copyRange(512,767),i.copyRange(512,767)),
	IFFT.ar(256,0,nil,nil,nil,r.copyRange(768,1023),i.copyRange(768,1023))
]	

});
*/

//
//
//
//Instr([\splits,\fftBalance],{ arg audio=0.0,lo=1,lomid=1,himid=1,hi=1;
//
//	var fft,r,i;	
//	fft =	FFT.ar(1024,0,nil,nil,nil,audio);
//	r = fft.collect({ arg c; c.real });
//	i = fft.collect({ arg c; c.imag }); 
//Mix.new([
//	IFFT.ar(256,0,nil,nil,nil,r.copyRange(0,255),i.copyRange(0,255)) * lo,
//	IFFT.ar(256,0,nil,nil,nil,r.copyRange(256,511),i.copyRange(256,511)) * lomid,
//	IFFT.ar(256,0,nil,nil,nil,r.copyRange(512,767),i.copyRange(512,767)) * himid,
//	IFFT.ar(256,0,nil,nil,nil,r.copyRange(768,1023),i.copyRange(768,1023)) * hi
//])	
//
//});


// yeah !!!!!! viscious !
Instr("splits.bySlew",{ arg audio=0.0,freq=300;

	var slewed,rest;
	slewed = Slew.ar(audio,freq,freq);
	rest = audio - slewed;
	
	[ rest,slewed]
},#[
	\audio,
	\freq
],
	MultiTrackAudioSpec(2)
);

