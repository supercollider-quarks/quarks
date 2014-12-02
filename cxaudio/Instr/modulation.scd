
Instr([\modulation,\phaser],{ arg audio=0.0,depth=1.0,decay=0.5,lfo=0.5,min=440,max=1600;

	var phased,xine;
	var dmin,dmax;
	phased = audio;
	dmin = min / (44100 / 2.0);
	dmax = max / (44100 / 2.0);
	
	xine = SinOsc.kr(lfo,0.0,dmax - dmin, dmin);
	6.do({
		phased = AllpassL.ar( phased, 10000 / (44100 / 2.0) + 0.1,xine, decay);
	});

	//audio + (phased * depth)
	phased
},#[
	\audio,
	[0.0,1.0],
	[0.0,10.0],
	[0.0,30],
	\ffreq,
	\ffreq
],
	\audio
);
		




Instr([\modulation, \SinChorus],{ arg audio=0.0,qnty=4,maxDelay=0.05,speed=0.1;	

		SinChorus.ar(audio,qnty,maxDelay,speed)

},[
	\audio,
	\qnty,
	ScalarSpec(0.001,0.1),
	[0.01,2.0,\exponential]
],
	\audio
);


Instr([\modulation, \NoiseChorus],{ arg audio=0.0,qnty=4,maxDelay=0.05,speed=0.1;
				
		StereoNoiseChorus.ar(audio,qnty,maxDelay,speed)
		
},[
	\audio,
	\qnty,
	ScalarSpec(0.001,0.1),
	[0.01,2.0,\exponential]
],\audio);


Instr([\modulation, \CombAChorus],{ arg audio=0.0,qnty=4,maxDelay=0.05,speed=0.1,bidecay=0.1,height=1.0;
				
		CombAChorus.ar(audio,qnty,maxDelay,speed,bidecay)
		
},[
	\audio,
	\qnty,
	ScalarSpec(0.001,0.1),
	[0.01,2.0,\exponential]
],\audio);



Instr([\modulation, \CombNChorus],{ arg audio=0.0,qnty=4,maxDelay=0.05,speed=0.1,bidecay=0.1,height=1.0;
				
		CombNChorus.ar(audio,qnty,maxDelay,speed,bidecay)
		
},[
	\audio,
	\qnty,
	ScalarSpec(0.001,0.1),
	[0.01,2.0,\exponential]
],\audio);





