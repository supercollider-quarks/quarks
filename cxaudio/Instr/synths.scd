
/*
	these all have trigs or gates but the synth keeps playing
	mono analog style
*/

Instr([\synths,\Pulse],{ arg trig=1.0, freq=440,
			ffreqInterval=0,rq=0.4,env,qnty=4,detune=2;
	var p,width;
	
	p = Splay.arFill(qnty,{ arg i;
		Pulse.ar(freq + Rand(detune.neg,detune), LFNoise1.kr(0.1,0.45,0.55) )
	});

	p=RLPF.ar(p,freq * ffreqInterval.midiratio,rq);

	Enveloper2.ar(p,trig,env)

},[
	nil,
	nil,
	ControlSpec(-48,48,\linear,1,0),
	nil,
	\envperc,
	\qnty,
	StaticSpec(0,20)
],\audio);

Instr([\synths,\PulseFix],{ arg trig=1.0, freq=440,
			ffreqInterval=0,rq=0.4,env,qnty=4,detune=2;
	var p,width;
	
	p = Splay.arFill(qnty,{ arg i;
		Pulse.ar(freq + Rand(detune.neg,detune) )
	});

	p=RLPF.ar(p,freq * ffreqInterval.midiratio,rq);

	Enveloper2.ar(p,trig,env)

},[
	nil,
	nil,
	ControlSpec(-48,48,\linear,1,0),
	nil,
	\envperc,
	\qnty,
	StaticSpec(0,20)
],\audio);





Instr([\synths,\PMOsc,\rlpf],{ arg gate=0.0, freq=440,freq2=440,index=1.0,
			ffreqInterval=0,rq=0.4,env;
	var p,width;
	
	p=PMOsc.ar(freq,freq2,index,0);

	p=RLPF.ar(p,freq * ffreqInterval.midiratio,rq);
	
	Enveloper2.ar(p,gate,env)
			
},[
	nil,
	nil,
	[100,5000,\exponential],
	[0.0,10.0],
	ControlSpec(-48,48,\linear,1,0),
	nil,
	\envadsr
],\audio);




// fenv + rq env
Instr([\synths,\PMOsc,\FQenv],{ arg gate=0.0, freq=440,freq2=440,index=1.0,
			ffreqRange=400,ffreqBase=100,rq=0.4,rqRange= - 0.1, env,fattack=0.1,fdecay=0.3;
	var p,width,f;
	
	p=PMOsc.ar(freq,freq2,index,0);

	f = Decay2.kr(gate,fattack,fdecay,ffreqRange,ffreqBase);
	rq = Decay2.kr(gate,fattack,fdecay,rqRange,rq);
	p=RLPF.ar(p,f,rq);
	
	Enveloper2.ar(p,gate,env)
	
},[
	\gate,
	nil,
	[100,5000,\exponential],
	[0.0,10.0],
	[1,5000,\exponential],
	[40,800,\exponential],
	nil,
	[-0.8,0.8],
	\envadsr,
	[0.01,5],
	[0.01,60]
],\audio);






Instr([\synths,\PMOsc,\indexEnv],{ arg gate=0.0, freq=440,freq2=440,index=1.0,indexRange=1.0,
			ffreqRange=400,ffreqBase=100,rq=0.4, env,attack=0.1,decay=0.3;
	var p,width,f;
	
	index = Decay2.kr(gate,attack,decay,indexRange,index);
	p=PMOsc.ar(freq,freq2,index,0);

	f = Decay2.kr(gate,attack,decay,ffreqRange,ffreqBase);
	p=RLPF.ar(p,f,rq);
	
	Enveloper2.ar(p,gate,env)
	
},[
	\gate,
	nil,
	[100,5000,\exponential],
	[0.0,10.0],
	[-4,4],
	[1,5000,\exponential],
	[40,800,\exponential],
	nil,
	\envadsr
],\audio);





// fenv + rq env
Instr([\synths,\PMOsc,\freq2Env],{ arg gate=0.0, freq=440,freq2=440,freqRange=1.0,index=1.0,
			ffreqRange=400,ffreqBase=100,rq=0.4, env,attack=0.1,decay=0.3;
	var p,width,f;
	
	freq2 = Decay2.kr(gate,attack,decay,freqRange,freq2);
	p=PMOsc.ar(freq,freq2,index,0);

	f = Decay2.kr(gate,attack,decay,ffreqRange,ffreqBase);
	p=RLPF.ar(p,f,rq);
	
	Enveloper2.ar(p,gate,env)
	
},[
	nil,
	nil,
	[100,5000,\exponential],
	[-2000,2000],
	[0.0,10.0],
	[1,5000,\exponential],
	[40,800,\exponential],
	nil,
	\envadsr
],\audio);




//
//
//Instr([\synths,\Saw],{ arg gate=0.0, freq=440,
//			ffreq4=0.5,rq=0.4,env,qnty=4,detune=2;
//	var p,width;
//	
//	p = Splay.arFill(qnty,{ arg i;
//			Saw.ar(freq + (rrand(detune / 2, detune) * [-1,1].choose))
//		});
//	p=RLPF4.ar(p,ffreq4 ,rq);
//
//	Enveloper2.ar(p,gate,env)
//						
//},[
//	nil,
//	nil,
//	[0.1,0.9],
//	nil,
//	\envadsr,
//	\qnty,
//	StaticSpec(0,20)
//]);




//Instr([\synths,\SyncSaw],{ arg gate=0.0, freq=440,freq2,
//			ffreq4=0.5,rq=0.4,env,qnty=4,detune=2;
//	var p;
//
//	p = Splay.arFill(qnty,{ arg i;
//			SyncSaw.ar(freq + (Rand(detune / 2, detune) * [-1,1].choose),
//					freq2)
//		});
//		
//	p=RLPF4.ar(p,ffreq4 ,rq);
//
//	Enveloper2.ar(p,gate,env,2,2)
//						
//},[
//	[nil],
//	[nil],
//	[nil],
//	[0.1,0.9],
//	[nil],
//	\envadsr,
//	\qnty,
//	[StaticSpec(0,20)]
//]);



//Instr([\synths,\stereo,\SyncSaw],{ arg gate=0.0, freq=440,freq2,
//			ffreq4=0.5,rq=0.4,env,qnty=4,detune=2;
//	var p;
//	
//	p = Splay.arFill(qnty,{ arg i;
//			SyncSaw.ar(freq + (rrand(detune / 2, detune) * [-1,1].choose),
//					freq2)
//		});
//		
//	p=RLPF4.ar(p,ffreq4 ,rq);
//
//	Enveloper2.ar(p,gate,env,2,2)
//						
//},[
//	nil,nil,nil,
//	[0.1,0.9],
//	nil,
//	\envadsr,
//	\qnty,
//	[StaticSpec(0,20)]
//]);




Instr([\synths,\stereo,\SinOsc,\clear],{ arg gate=0.0, freq=440,env,qnty=4,detune=2;
	var p;
	
	p = Splay.arFill(qnty,{ arg i;
			SinOsc.ar(freq + (detune  * [-1,1].choose))
		});

	Enveloper2.ar(p,gate,env)
						
},[
	nil,nil,\envadsr
],\audio);



Instr([\synths,\stereo,\SinOsc,\pmod],{ arg gate=0.0, freq=440,freq2=20,env,qnty=4,detune=2;
	var p;
	
	p = Splay.arFill(qnty,{ arg i;
			SinOsc.ar(freq + (detune  * [-1,1].choose),
				SinOsc.ar(freq2,0,2pi))
		});

	Enveloper2.ar(p,gate,env)
						
},[
	nil,nil,nil,\envadsr
],\audio);





Instr([\synths,\mono,\SinOsc],{ arg gate=0.0, freq=440,env,qnty=4,detune=2;
	var p;
	
	p = Mix.fill(qnty.asInteger,{ arg i;
			SinOsc.ar(freq + (detune  * [-1,1].choose))
		}) * qnty.reciprocal;

	Enveloper2.ar(p,gate,env,2,1)
						
},[
	nil,nil,\envadsr
],\audio);




