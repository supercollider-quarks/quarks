
// some of these are from jmc examples
// and are thus GPL

	
Instr([\percOrc4,	\tabla],{arg trig=0.0,  impulseToNoise=0.5,fingerToDrum=0.5,freqScale=1.0,freqOffset=0.0,timeScale=1.0;
	
		var finger, tabla;
		
		finger = Decay2.ar(			 
			Impulsar.ar(trig), 
			0.001, 0.01, 		
			(PinkNoise.ar(impulseToNoise)
			 + (1 - impulseToNoise)) 		
		);
		
		tabla = Klank.ar(`(#[ [565, 855, 1139, 1440, 1735], 				// frequencies
						nil, 								// amplitudes all default to 1 if not given.
					[ 2.625, 0.9, 1.2, 0.375, 0.45 ] ])				// ring times; scaling them by 1.5 sounds 
					,
					 finger,freqScale,freqOffset,timeScale);  
			
		LinXFade2.ar(finger,tabla *  5.reciprocal,fingerToDrum)
		
},[
	nil,
	[0,1],
	\mix
],\audio);


Instr([\percOrc4,	\partchDiamond],{ arg trig=0.0,  qnty=4, decay=1,rq=0.4,ffreq=400;
	var  y;
	
		y = Impulsar.ar(trig);
		y = Resonz.ar(y, ffreq, rq);
		y = Integrator.ar(y, 0.99);
		
		y = Mix.fill(qnty, {
			CombL.ar(
				y, 
				0.1, 
				(7.rand + 1) / (8.rand + 1) * 0.001, // Partch tonality diamond about 1000Hz
				5
			);
		});
},[],\audio);



Instr([\percOrc4,	\monoClav],{arg trig=0.0,qnty=3,freqOffset=3000,decay=6,myDetune=6,midinote=30;
	
		var delayTime, myfreqOffset, strike, hammerEnv, hammer;
		var freqOffsets;
		
		strike = Impulsar.ar(trig);
			
		hammerEnv = Decay2.ar(strike, 0.008, 0.04); // excitation envelope
	
			
		freqOffsets=Interval(myDetune.neg, myDetune, myDetune * 2 / qnty);
		Mix.new(Array.fill(qnty, { arg i;	
				delayTime = 1 / (midinote.value + freqOffsets.at(i)).midicps;
				hammer = LFNoise2.ar(freqOffset.abs*3, hammerEnv); // 3000 Hz was chosen by ear..
				CombL.ar(hammer,		// used as a string resonator
					delayTime, 		// max delay time
					delayTime,			// actual delay time
					decay) 				// decay time of string
			}));

},[
	nil,nil,nil,nil,[StaticSpec(0.005,12)]
],\audio);

	
// following are CC

// stick
Instr([\percOrc4,	\clong],{ arg trig=0.0, qnty=10,freqOffset=500;
	var imp;
	imp=(Impulsar.ar(trig) * 50).distort;
	Mix.fill(qnty, { arg i;									//bwr
			Resonz.ar(imp, 200 + (i*freqOffset.rand.abs*2),qnty.reciprocal);				
		}).softclip;
},[
	nil,nil,[StaticSpec(0,500)]
],\audio);


Instr([\percOrc4,	\clongkik],{ arg trig=0.0, qnty=10,freqOffset=500,freq=130,decay=0.2;
	var imp,stick;
	imp=(Impulsar.ar(trig) * 50).distort;
	stick=Mix.fill(qnty, { arg i;									//bwr
			Resonz.ar(imp, 200 + (i*freqOffset.rand.abs*2),qnty.reciprocal);				
		}).softclip;
	
		SinOsc.ar(
				EnvGen.kr( Env.perc(releaseTime:0.5,level: 10), trig ,timeScale: decay),
			0,1.0,stick)
			
},[
	nil,nil,[StaticSpec(0,500)]
],\audio);




Instr([\percOrc4,	\clongkik3],{ arg trig=0.0, qnty=10,freqOffset=500,freq=130,decay=0.2,mix;
	var imp,stick;
	imp=(Impulsar.ar(trig) * 50).distort;
	stick=Mix.fill(qnty, { arg i;									//bwr
			Resonz.ar(imp, 200 + (i*freqOffset.rand.abs*2),qnty.reciprocal);				
		}).softclip;
	
	//ReTrigger.ar({

		LinXFade2.ar(
			 SinOsc.ar(
				EnvGen.kr( Env.perc(releaseTime:0.5,level: 10), trig ,timeScale: decay),
			 	0,1.0).softclip,
			 stick,
			 mix
			 )
			 
	//},trig,0.5)
					
},[
	nil,nil,[StaticSpec(0,500)]
],\audio);

	
					
					
Instr([\percOrc4,	\klank808],{ // a Klank is more efficient, but no bwr tune
		arg trig=0.0,qnty=10,ifreqOffset=500,freqScale=1,freqOffset=0,
			timeScale=0.15,ffreq=440,bwr=1.0;
					
		Klank.ar(`[Array.series(qnty, 200, ifreqOffset),nil,[1.0]],
			Resonz.ar(
					//EnvGen.ar(Env.perc(0,0.01),gate: trig)
					Impulsar.ar(trig),ffreq,bwr
				)
		,freqScale,freqOffset,timeScale
		).softclip;
				
},[
	nil,nil,[StaticSpec(0,300)]
],\audio);

					
Instr([\percOrc4,	\klank808b],{ // a Klank is more efficient, but no bwr tune
		arg trig=0.0,qnty=10,ifreqOffset=500,freqScale=1,freqOffset=0,
			timeScale=0.15,ffreq=440,bwr=1.0,freq=100,decay=0.3,drive=1.0;
			
		var click;
					
		click=Klank.ar(`[Array.series(qnty, 200, ifreqOffset),nil,[1.0]],
			Resonz.ar(
					//EnvGen.ar(Env.perc(0,0.01),gate: trig)
					Impulsar.ar(trig),ffreq,bwr
				)
		,freqScale,freqOffset,timeScale
		).softclip;
		
		SinOsc.ar(
			EnvGen.kr( Env.perc(releaseTime:0.3,level: 10), trig ,timeScale: decay),
			0,1.0,click * drive).softclip
				
},[
	nil,nil,[StaticSpec(0,300)]
],\audio);



	

Instr([\percOrc4,	\shaker],{ arg trig=0.0,freqOffset=300,dur=0.3;
		var sh;
		sh= HPZ2.ar(Dust2.ar( Lag.kr( LFPulse.kr(2, 0.12, 10000), 0.1), 2 )) ;
		Resonz.ar( // shaker
			//ReTrigger.ar({ HPZ2.ar(Dust2.ar( Lag.kr( LFPulse.kr(0, 0.12, 10000), 0.1), 2 )) },trig),
			Enveloper2.ar(sh,trig,Env.perc(0.0,dur)),
			SinOsc.kr(2, 0, freqOffset, 4000), 0.2
		);
},[
	\trig,
	[\freqOffset],
	[StaticSpec(0.01,1.0)]
],\audio
);


