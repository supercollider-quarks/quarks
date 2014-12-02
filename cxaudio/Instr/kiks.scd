
// not exactly timbaland

Instr([\kiks,	\hardshape,\xline],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,drive=1.0,amp=1.0;
		
		var finger,out;
		
		finger = Decay2.ar(				
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(BrownNoise.ar(noise) + (1.0 - noise)) 		
			).fold2(0.5);
	
		out=HardShaper.ar(
			Enveloper2.ar(
				finger + SinOsc.ar(
					//XLine.kr(freq,10,decay)
					Linen.kr(trig,0.0,freq,decay) + 10.0
					)
			,trig)
			,drive);
			
		if(amp==1.0,{out},{out * amp})
},nil,\audio);




Instr([\kiks,	\hardshape, \line],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,drive=1.0,amp=1.0;
		
		var finger,out;
		
		finger = Decay2.ar(				
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(BrownNoise.ar(noise) + (1.0 - noise)) 		
			).fold2(0.5);
	
		out=HardShaper.ar(
			Enveloper2.ar(
				finger + SinOsc.ar(
					Linen.kr(trig,0.0,freq,decay) + 10.0

					//Line.kr(freq,10,decay)
					)
			,trig),drive);
			
		if(amp==1.0,{out},{out * amp})
},nil,\audio);





Instr([\kiks,	\nodist,\xline],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,amp=1.0;
		
		var finger,out;
		
		finger = Decay2.ar(				
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(BrownNoise.ar(noise) + (1.0 - noise)) 		
			).fold2(0.5);
	
			out=Enveloper2.ar(
				finger + SinOsc.ar(
						Linen.kr(trig,0.0,freq,decay) + 10.0
					)
			,trig);
			
		if(amp==1.0,{out},{out * amp})
},nil,\audio);



Instr([\kiks,	\nodist, \line],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,amp=1.0;
		
		var finger,out;
		
		finger = Decay2.ar(				
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(BrownNoise.ar(noise) + (1.0 - noise)) 		
			).fold2(0.5);
	
		out=Enveloper2.ar(
				finger + SinOsc.ar(
						Linen.kr(trig,0.0,freq,decay) + 10.0
					)
			,trig);
			
		if(amp==1.0,{out},{out * amp})
},nil,\audio);



	
Instr([\kiks,	\anakik,\flat],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0;
		var finger;
		finger = Decay2.ar(			 
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(PinkNoise.ar(noise)
				 + (1 - noise)) 		
			).fold2(0.5);
	
			//ReTrigger.ar({
				finger + SinOsc.ar(
						EnvGen.kr( Env.perc(level: 10), trig ,timeScale: decay)
						//XLine.kr(freq,10,decay)
					)
			//},trig)
			
	},nil,\audio);//.test
	
	

Instr([\kiks,	\anakik,\soft],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0;
		var finger;
		finger = Decay2.ar(			 
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(PinkNoise.ar(noise)
				 + (1 - noise)) 		
			).fold2(0.5);
	
			//ReTrigger.ar({
				(finger + SinOsc.ar(
						EnvGen.kr( Env.perc(level: 10), trig ,timeScale: decay)
					)).softclip
			//},trig).softclip
			
	},nil,\audio);
	
Instr([\kiks,	\anakik,\hard],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,drive=1.0;
		var finger;
		finger = Decay2.ar(				
				Impulsar.ar(trig), 
				0.001, stickDecay, 		
				(PinkNoise.ar(noise)
				 + (1 - noise)) 		
			).fold2(0.5);
	
			HardShaper.ar(
			
					finger + SinOsc.ar(
						EnvGen.kr( Env.perc(level: 10), trig ,timeScale: decay)
						//XLine.kr(freq,10,decay)
					)
			,drive)
	},nil,\audio);


Instr([\kiks,	\anakik,\pulse],{arg trig=0.0,freq=100,decay=0.3,stickDecay=0.01, noise=1.0,drive=1.0;
	var finger;
	finger = Decay2.ar(			 
			Impulsar.ar(trig), 
			0.001, stickDecay, 		
			(PinkNoise.ar(noise)
			 + (1 - noise)) 		
		).fold2(0.5);

	finger + Pulse.ar(
		EnvGen.kr( Env.perc(level: 10), trig ,timeScale: decay)
		//XLine.kr(freq,10,decay)
	)
					
},nil,\audio);
	
	


