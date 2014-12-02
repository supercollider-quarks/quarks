 
	
	
Instr([\percOrc5,	\choochoo],{ arg trig=0.0,decay=0.2,noise=0.07,freq=6000,freqRange=1000;
	Decay.ar(Impulsar.ar(trig), decay, LFNoise0.ar(LFNoise1.kr(0.3,freqRange,freq + freqRange),noise));
},[
	\trig,
	\decay,
	[0.001,0.25],
	[1,12000],
	[1,5000]
],\audio
);


Instr([\percOrc5,	\mridangam],{ arg trig=0.0,decay=0.2,ffreq=261.626,bwr=0.02,amp=0.3;
		
	Resonz.ar(
		WhiteNoise.ar(Decay2.kr(trig,0.002, decay,70)) 
	, ffreq
	, bwr
	, 4)
	.distort
	* amp

},[
	\trig,
	[0.0,0.3]
],\audio
);




Instr([\percOrc5,	\blips,\one],{ arg trig=0.0,freq=8000,decay = 0.3;

	trig = A2K.kr(Impulsar.ar(trig));
		FSinOsc.ar(freq, 
					Decay2.kr(trig, 0.01, decay,1.0))
						
},[
	\trig
],\audio
);





Instr([\percOrc5,	\blips,\techsyn],{ arg trig=0.0,droneLevel=0.0022,ffreq=8000,rq=0.2,decay = 0.3;
 
	 RLPF.ar(
	       Klank.ar(
	                `[ 
	                    Array.linrand(4, 50.0, 2000.0), 
	                    nil, 
	                    Array.rand(4, 0.2, 12.0) 
	                ],
	                //drones
	                PinkNoise.ar(LFNoise1.kr(3.0, 0.0008, droneLevel))
	            ).max(0) ,
	            Decay2.kr(trig, 0.004, decay, ffreq,  100),
	            rq
	       )
						
},[
	\trig,
	[0.0,0.017]
],\audio
);




Instr([\percOrc5,	\blips,\techsyn2],{ arg trig=0.0,droneLevel=0.0022,ffreq=8000,rq=0.2,decay = 0.3;
 
	 RLPF.ar(
	       Klank.ar(
	                `[ 
	                    Array.linrand(4, 50.0, 2000.0), 
	                    nil, 
	                    Array.rand(4, 0.2, 12.0) 
	                ],
	                //drones
	                PinkNoise.ar(LFNoise1.kr(3.0, 0.0008, droneLevel))
	            ).max(0) ,
	            Decay2.kr(trig, 0.004, decay, ffreq,  100),
	            rq
	       )
						
},[
	\trig,
	[0.0,0.017]
],\audio
);



Instr([\percOrc5,	\blips,\verbclik],{ arg trig=0.0,ffreq=8000,detune=1000,bwr = 0.003,decay=2.0,
								percolators = 10,combs=7,allpasses=4;
 
	var t,s,y;
		
		t = Impulsar.ar(trig,50);
			// sine percolation sound :
		s = Mix.new(Array.fill(percolators, { Resonz.ar(t, ffreq + Rand(0.0,detune), bwr )}) );
		
		y = Mix.new(CombL.ar(s, 0.1, LFNoise1.kr( Array.fill(combs,{Rand(0.005,0.1)}), 0.04, 0.05), decay)); 
		
		allpasses.do({ y = AllpassN.ar(y, 0.050, [Rand(0.005,0.05),Rand(0.005,0.05)], 1) });
		
		s+(0.2*y)
	
},[
	\trig,
	\ffreq,
	StaticSpec(0,1000),
	[0.0001,0.1,\exponential],
	\decay,
	\qnty,
	\qnty,
	\qnty
],\audio
);

/*
Instr([\percOrc5,	\blips,\metalplate],{ arg trig=0.0,freq=8000, ffreq=8000,feedback=0.8,qnty=4;

	var  buffers, tapTimes, excitation, delayedSignals, filteredDelays;
	
	// allocate buffers for the delay lines	
	buffers = Array.fill(qnty, { Signal.new(44100 * 0.03); });	
	
	// assign random tap times
	tapTimes = Array.rand(qnty, 0.015, 0.03);
	
	// excitation
	excitation = Decay2.ar(Impulsar.ar(trig,0.2), 0.01, 0.2, LFNoise2.ar(freq));	
	
	// tap the delay lines
	delayedSignals = TapN.ar(buffers, tapTimes);	
	
	// filter the taps //delayedSignals; //	
	filteredDelays = LPF.ar(delayedSignals, ffreq, feedback);
	
	// write to delay lines
	DelayWr.ar(buffers, filteredDelays + excitation);	// write the mixed signal to the delay line
	
	Mix.new(filteredDelays);	// mix 5 channels to one
			
},[
	\trig
],\audio);

*/

/*
Instr([\percOrc5,	\blips,\garynewman],{ arg trig=0.0,freq=8000,freq2=8000, ffreq=8000,feedback=0.8;

	var   buffer, excitation, delayedSignals, filteredDelays;
	
	excitation = Decay2.ar(Impulsar.ar(trig,0.2), 0.01, 0.2, LFNoise2.ar(freq2));	
	buffer =Signal.new(44100 * 0.03);
	delayedSignals = TapN.ar(buffer, freq.reciprocal);	
	
	filteredDelays = LPF.ar(delayedSignals, ffreq, feedback);
	
	DelayWr.ar(buffer, filteredDelays + excitation);
	
	filteredDelays
			
},[
	\trig
]);

*/


Instr([\percOrc5, \kik, \brush],{ arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,sine=1.0,env;

	var finger,sh;

	sh= HPZ2.ar(Dust2.ar(4000)) ;

	finger = Decay2.ar(			 
				Impulsar.ar(trig), 
			0.001, stickDecay, 		
			((sh * noise) + (1 - noise)) 		
		).fold2(0.5);

		SinOsc.ar(
			Linen.kr(trig,0,freq,decay) + 40
		).madd( 
			EnvGen.kr(env,trig,Latch.kr(sine,trig))
		, finger)
},[

],
\audio);
	
/*
"/Volumes/Macintosh HD/Users/cruxxial/Documents/SC3docs/Music/Patches/percy3/hardisco".loadDocument.gui

*/


Instr([\percOrc5,	\snare,\snapzzz],{arg trig=0.0,chaosParam=1.0,envadsr,qnty,freqOffset=0;

	var finger,sh,imp;
	
	sh = Crackle.ar(chaosParam);

	imp=(Impulsar.ar(trig) * 50).distort;
	
	finger=Mix.fill(qnty, { arg i;
			Resonz.ar(imp, 200 + (i*freqOffset),qnty);				
		}).softclip;

	Enveloper2.ar(
		sh
		,trig,envadsr)

	+ finger
	
},[
	\trig,
	[1.0,2.0],
	\envadsr,
	\qnty,
	[1,200]
],\audio);



	
	
Instr([\percOrc5,	\snare, \filtenv,\dry],{ arg trig=0.0,decay=0.2,fdecay=0.3,ffreq=261.626,rq=0.2,amp=0.3;
		
	RLPF.ar(
		WhiteNoise.ar(
			Decay2.kr(trig,0.002, decay,70)
			) 
	, Decay.kr(trig,fdecay, ffreq)
	, rq
	, 4)
	.distort
	* amp
			
},[
	\trig,
	[0.0,0.3],
	[0.0,1.0]	
],\audio);


	
Instr([\percOrc5,	\snare, \filtenv,\allpas],{ arg trig=0.0,decay=0.2,fdecay=0.3,vdecay=0.5,ffreq=261.626,rq=0.2,amp=0.3;

	var y;	
	y=RLPF.ar(
		WhiteNoise.ar(
			Decay2.kr(trig,0.002, decay,70)
			) 
	, Decay.kr(trig,fdecay, ffreq)
	, rq
	, 4)
	.distort
	* amp;

	Splay.arFill(4,{
		AllpassN.ar(y,0.05,0.05.rand,vdecay)
	});
		
},[
	\trig,
	[0.0,0.3],
	[0.0,1.0],
	\decay
],\audio);



	
Instr([\percOrc5, \snare, \filtenv,\comb],{ arg trig=0.0,decay=0.2,fdecay=0.3,vdecay=0.5,ffreq=261.626,rq=0.2,amp=0.3,maxDelay=0.1;

	var y,mul,add;	
	y=RLPF.ar(
		WhiteNoise.ar(
			Decay2.kr(trig,0.002, decay,70)
			) 
	, Decay.kr(trig,fdecay, ffreq)
	, rq
	, 4)
	.distort
	* amp;

	mul = maxDelay / 2;
	add = mul * 0.9;
	Splay.arFill(4,{
			CombL.ar(y, maxDelay, LFNoise1.kr(0.1.rand, mul, add), vdecay)
		});

},[
	\trig,
	[0.0,0.3],
	[0.0,1.0],
	\decay
],\audio);


/*
no RLPF4

Instr([\percOrc5, \snare, \filtenv,\combf4],{ arg trig=0.0,decay=0.2,fdecay=0.3,vdecay=0.5,rq=0.2,amp=0.3,maxDelay=0.1;

	var y,mul,add;	
	y=RLPF4.ar(
		WhiteNoise.ar(
			Decay2.kr(trig,0.002, decay,70)
			) 
	, Decay.kr(trig,fdecay, 0.8,0.1)
	, rq
	, 4)
	.distort
	* amp;

	mul = maxDelay / 2;
	add = mul * 0.9;
	Splay.arFill(4,{
			CombL.ar(y, maxDelay, LFNoise1.kr(0.1.rand, mul, add), vdecay)
		});

},[
	\trig,
	[0.0,0.3],
	[0.0,1.0],
	\decay
]);


*/




