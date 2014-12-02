

// GPL : from examples
Instr([\oscillOrc2,\tabla],{arg midinote=40,  mod1=0.5,mod2=0.5;

		var finger, tabla, 				// vars for a finger and drum sound generators
		klankSpecs, 						// specifications of the resonator.
		longestRingTime, repeatTime,		// longest rt of a resonance, repeatTime of the stroke.
		impulseToNoise,fingerToDrum;
		
		impulseToNoise=mod1;
		fingerToDrum=mod2;
		
		finger = Decay2.ar(				// create decay2 envelope, 
			Impulse.ar(0, 1), 				// retriggered by an Impulse with 1 Hz and amp. 1.
			0.001, 0.01, 					// attack time = 1 msec, decay time = 10 msec.
			(PinkNoise.ar(impulseToNoise)	// PinkNoise with an amplitude depending on MouseX
			 + (1 - impulseToNoise)) 		// + a number: this adds the Decay curve as an audio signal,
		);								// close Decay2 argument list.
				
		klankSpecs = 	#[ [565, 855, 1139, 1440, 1735], 	// frequencies
					nil, 							// amplitudes all default to 1 if not given.
					[ 2.625, 0.9, 1.2, 0.375, 0.45 ] ];	// ring times; scaling them by 1.5 sounds 
		
		tabla = Klank.ar(`klankSpecs, finger,midinote/50 ,0,1,0.05);  
			
		 (finger * (1 - fingerToDrum)) +  	// (change '+' to ',' for finger/tabla in L/R)
			(tabla * fingerToDrum) ;		// * 100 is for (estimated) equal peak amplitude
			
			
},nil,\audio);


Instr([\oscillOrc2,\randKlang ],{arg freq=500,qnty=10,amp; 
	Klang.ar(`[ Array.rand(qnty.asInteger, 600.0, 1000.0), nil, nil ], freq/2000,0,qnty.reciprocal * amp) ;	
},nil,\audio);


Instr([\oscillOrc2,\geomKlang],{arg freq=500,freqOffset=500,freqScale=1.0,qnty=10,amp=1.0; 	
	Klang.ar(`[ Array.geom(qnty.asInteger, 1, freqOffset), nil, nil ], freqScale,freq,qnty.reciprocal * amp) ;	
},[
	\freq,
	StaticSpec(1.0,5.0)
],\audio);


Instr([\oscillOrc2,	\seriesKlang],{arg freq=500,freqOffset=500,freqScale=1.0,qnty=10,amp; 
	 Klang.ar(`[ Array.series(qnty.asInteger, 0, freqOffset), nil, nil ], freqScale,freq,qnty.reciprocal * amp) ;	
},[
	\freq,
	StaticSpec(1.0,5.0)
],\audio);


// static spec prob ??
// GPL : from examples
Instr([\oscillOrc2,	\monoClav],{arg qnty=3,freq2=3000,decay=6,mod1=0.5,midinote=50;
	
		var delayTime, hammerEnv;
		var freqOffsets,myDetune;
		
		 // excitation envelope
		hammerEnv = Decay2.ar(Impulse.ar(0,0.9), 0.08, 0.04);
		myDetune=(mod1 * 24).max(0.0005);
	
		freqOffsets=Interval(myDetune.neg, myDetune, myDetune * 2 / qnty);
		
		//freqOffsets.inspect;
		
		
			Mix.fill(qnty, { arg i;	
				delayTime = (midinote + freqOffsets.at(i)).midicps.reciprocal;
				
				// used as a string resonator
				CombL.ar(
					LFNoise2.ar(freq2, hammerEnv),		
					delayTime, 		// max delay time
					delayTime,			// actual delay time
					decay) 				// decay time of string
			});
			
},[
	\qnty,
	\freq,
	\decay,
	StaticSpec(0,1,\linear,0),
	\midinote
],\audio);

