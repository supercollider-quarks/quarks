
// K2A
Instr([\klankperc,\k2a],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,foldAt=0.1;
	Klank.ar(
				`[
					FloatArray[ 87.041, 198.607 ],
					nil,
					FloatArray[ 0.165394, 0.15595 ]
				],
				K2A.ar(trig),
				sfreqScale,sfreqOffset,stimeScale
			).squared.fold2(foldAt)		
},[
	nil,
	[0.01,100],
	[0,10000],
	[0.01,100]

],\audio);





Instr([\klankperc,\impulse,\thwick],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,foldAt=0.1;
	
	Klank.ar(`[
				FloatArray[ 237.225, 53.8729, 741.223, 76.144, 142.979, 172.487 ],
				nil,
				FloatArray[ 0.0310065, 0.130285, 0.0594931, 0.0716388, 0.157874, 0.114807 ]

			],
			Impulsar.ar(trig),
			sfreqScale,sfreqOffset,stimeScale
		).squared.fold2(foldAt)		
},[
	nil,
	[0.01,100,\exponential],
	[0.001,10000,\exponential],
	[0.01,100]

],\audio);



Instr([\klankperc,\impulse,\thum2],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,foldAt=0.1;

	Klank.ar(`[
					FloatArray[ 87.041, 198.607 ],
					nil,
					FloatArray[ 0.165394, 0.15595 ]
				],
			Impulsar.ar(trig),
			sfreqScale,sfreqOffset,stimeScale
		).squared.fold2(foldAt)		
},[
	nil,
	[0.01,100],
	[0,10000],
	[0.01,100]

],\audio);


Instr([\klankperc,\impulse,\cring],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,foldAt=0.1;
	
	Klank.ar(`[
					FloatArray[ 142.848, 347.225, 411.233 ],
					nil,
					FloatArray[ 0.203269, 0.636002, 0.49101 ]
				],
			Impulsar.ar(trig),
			sfreqScale,sfreqOffset,stimeScale
		).squared.fold2(foldAt)		
},[
	nil,
	[0.01,100],
	[0,10000],
	[0.01,100]

],\audio);


//
////
////Instr([\klankperc,\creator],{ arg trig=0.0;
////
////	Klank.ar(
////				`[
//					FloatArray.fill(3, { exprand(50.0,800.0) }).postln,
//					nil,
//					FloatArray.fill(3, { 1.0.rand }).postln
//				],
////				ImpulseSequencer.ar([1.0],K2A.ar(trig))
////			).squared.fold2(0.1) //* [-1,1].choose
////			
////}).test
////
////
////



// can be a bit murky
Instr([\klankperc,\blip,\fold],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,numHarms=2.0,foldAt=0.1,freq=10,gain=1.0;
	Klank.ar(
				`[
					FloatArray[ 251.55, 459.601, 106.222, 152.87, 409.43, 186.177 ],
					nil,
					FloatArray[ 0.372221, 0.227528, 0.477731, 0.318256, 0.121477, 0.449826 ]
				],
				Enveloper2.ar( Blip.ar(freq,numHarms, 0.006), trig),
				sfreqScale,sfreqOffset,stimeScale
			).squared.fold2(foldAt)
			* gain		
},[
	nil,
	[0.01,10],
	[0,300],
	[0.01,100],
	[1.0,100.0]

],\audio);





//ISSUE not modulateable any more
Instr([\klankperc,\blip,\flat],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,numHarms=2.0,freq=10,gain=1.0;
	Klank.ar(
				`[
					FloatArray[ 251.55, 459.601, 106.222, 152.87, 409.43, 186.177 ],
					nil,
					FloatArray[ 0.372221, 0.227528, 0.477731, 0.318256, 0.121477, 0.449826 ]
				],
				Enveloper2.ar( Blip.ar(freq,numHarms, 0.006), trig),
				//ReTrigger.ar({  Blip.ar(freq,numHarms, 0.006)   }, trig),
				sfreqScale,sfreqOffset,stimeScale
			)
			* gain		
},[
	nil,
	[0.01,10],
	[0,300],
	[0.01,100],
	[1.0,100.0]

],\audio);

// do a pitch dive one


Instr([\klankperc,\blip,\dive,\flat],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,
			stimeScale=1.0,numHarms=2.0,freq=10,gain=1.0,decay=0.1;
			
	Klank.ar(
				`[
					FloatArray[ 664.454, 464.166, 487.716, 278.493, 64.3552, 344.767 ],
					nil,
					FloatArray[ 0.00877374, 0.0359073, 0.0696307, 0.00696222, 0.0180813, 0.0432172 ]
				],
				Enveloper2.ar(  
					Blip.ar(
						EnvGen.kr(Env.new([1.0,0.1],[1.0],\exp),trig,freq,0,decay)
						//XLine.kr(freq,10,decay)
							,numHarms, 0.006)   , trig),
				sfreqScale,sfreqOffset,stimeScale
			)
			* gain		
},[
	nil,
	[0.01,10],
	[0,300],
	[0.01,100],
	[1.0,100.0]

],\audio);


Instr([\klankperc,\blip,\dive,\crunch],{ arg trig=0.0,sfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,
			numHarms=2.0,freq=10,gain=1.0,decay=0.1,foldAt=0.1;
			
	Klank.ar(
				`[
					FloatArray[ 664.454, 464.166, 487.716, 278.493, 64.3552, 344.767 ],
					nil,
					FloatArray[ 0.00877374, 0.0359073, 0.0696307, 0.00696222, 0.0180813, 0.0432172 ]
				],
				Enveloper2.ar( Blip.ar(XLine.kr(freq,10,decay),numHarms, 0.06)    , trig),
				sfreqScale,sfreqOffset,stimeScale
			).squared.fold2(foldAt)
			* gain		
},[
	nil,
	[0.01,10],
	[0,300],
	[0.01,100],
	[1.0,100.0]

],\audio);


Instr([\klankperc,\blip,\dive,\crunch2],{ arg trig=0.0,ssfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,
				numHarms=2.0,freq=10,gain=1.0,decay=0.1,foldAt=0.1;
	// the decay is a pitch decay down to 10
	// to excite the Klank
	var xline;
	xline = EnvGen.kr( Env.perc(0.0,1.0,1.0,-8) ,trig,levelScale: freq - 10, levelBias: 10, timeScale: decay);
	Klank.ar(
		`[
			FloatArray[ 250.631, 102.046, 362.43 ],
			nil,
			FloatArray[ 0.554637, 0.451605, 0.521619 ]
		],
		Enveloper2.ar(  Blip.ar(xline,numHarms, 0.06)   , trig),
		ssfreqScale,sfreqOffset,stimeScale
	).squared.fold2(foldAt)
		* gain		
},[
	nil,
	StaticSpec(0.01,10),
	StaticSpec(0,300),
	StaticSpec(0.01,100),
	[1.0,100.0],
	StaticSpec(10,8000,\exponential),
	\gain,
	StaticSpec(0.001,1.0),
	nil
],\audio);




/*
triggable XLine

range = Latch.kr( end - start,trig);
xline = EnvGen.kr( Env([1.0,0.001],[1],'exp') ,trig,levelScale: range ,levelBias; Latch.kr(end,trig), timeScale: dur, doneAction:doneAction)

*/

Instr([\klankperc,\blip,\dive,\crunch3],{ arg trig=0.0,ssfreqScale=1.0,sfreqOffset=0.0,stimeScale=1.0,numHarms=2.0,freq=10,gain=1.0,decay=0.1,foldAt=0.1;

	// the decay is a pitch decay down to 10
	// to excite the Klank
	var xline;
	xline = EnvGen.kr( Env.perc(0.0,1.0,1.0,-8) ,trig,levelScale: freq - 10, levelBias: 10, timeScale: decay);

	Klank.ar(
				`[
					FloatArray[ 68.1516, 64.1059 ],
					nil,
					FloatArray[ 0.87422, 0.521266 ]
				],
				Enveloper2.ar( Blip.ar(xline,numHarms, 0.06)   , trig),
				ssfreqScale,sfreqOffset,stimeScale
			).squared.fold2(foldAt)
			* gain		
},[
	nil,
	[0.01,10],
	[0,300],
	[0.01,100],
	[1.0,100.0]

],\audio);



