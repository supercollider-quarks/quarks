

Instr([\minimoog,\minimoog],{ arg freq=440,int1=5,int2 = -5,width1=0.1,width2=0.1,width3=0.1,
			ffreqInterval=0,rq=0.4;
	var p;
	
	p=Pulse.ar([ freq  * int1.midiratio, freq, freq * int2.midiratio],
			[ width1,width2,width3],0.3);
			
	RLPF.ar(Mix.new(p),freq * ffreqInterval.midiratio,rq)
			
},[
	nil,
	ControlSpec(-48,48,\linear,1,0),
	ControlSpec(-48,48,\linear,1,0)

	,nil,nil,nil,
	ControlSpec(-48,48,\linear,1,0)

],\audio);


Instr([\minimoog,\loose],{ arg freq=440,int1=5,int2 = -5,
			ffreqInterval=0,rq=0.4;
	var p,c,d,f;
	c=LFNoise1.kr(0.1,0.45,0.55);
	d=LFNoise1.kr(0.1,0.45,0.55);
	f=LFNoise1.kr(0.1,2);
	p=Pulse.ar([ freq  * int1.midiratio + f , freq, freq * int2.midiratio - f],
			[c,d,c],0.2);
			
	RLPF.ar(Mix.new(p),freq * ffreqInterval.midiratio,rq)
			
},[
	nil,
	ControlSpec(-48,48,\linear,1,0),
	ControlSpec(-48,48,\linear,1,0),
	ControlSpec(-48,48,\linear,1,0)

],\audio);

// has one long ffreq env
// what's the point of that one ?
Instr([\minimoog,\detune],{ arg freq=440,detune = -4,ffreqInterval=0,rq=0.4,freqEnvSpeed=1.0;
	var p;
	var f,g,h,c,env;
	c=LFNoise1.kr(0.1,0.45,0.55);
	f=LFNoise1.kr(0.1,detune);
	g=LFNoise1.kr(0.1,detune);
	
	env=Env.adsr(0.1, 0.2, 0.25, 0.3, 1, -4);
	
	p=Pulse.ar([ freq  + f , freq + g, freq  -f],
			[c,c.neg,c],0.2);
					
	RLPF.ar(Mix.new(p),
		EnvGen.kr(env,1,freq * ffreqInterval.midiratio,0.0,freqEnvSpeed)
		,rq)
			
},[
	nil,
	[-20,20],
	ControlSpec(-48,48,\linear,1,0),
	\rq,
	[0.01,100]

],\audio);
//.test







