
// for Pbind or InstrSpawner


//InstrSpawner : can pass a freq or a stream into it
	// 
	
// amp or amplitudeStream
	// for now just amplitudeStream
	// a wave might be better
	
Instr([\spawnEvents,\dyeeer],{ arg freq=440,rq=0.1,width=0.5,fenv,fenvmod=1.0,envperc;

	RLPF.ar(
		Pulse.ar(
			freq,
			width
		),
		EnvGen.kr(fenv,levelScale: fenvmod),
		rq)
		  * EnvGen.kr(envperc, doneAction: 2)

},nil,\audioEvent);


Instr([\spawnEvents,\PlayBuf,\varistart],{ arg sample,unipolarStream=0,pchRatioStream=1.0,env;

	PlayBuf.ar( sample.numChannels, sample.bufnumIr,pchRatioStream,1,unipolarStream * sample.bufFramesKr,1 )
		 * EnvGen.kr(env,doneAction: 2)

},nil,\audioEvent);


Instr([\spawnEvents,\PlayBuf,\oneShot],{ arg sample,pchRatioStream=1.0,env;

	PlayBuf.ar( sample.numChannels, sample.bufnumIr,pchRatioStream,1,0,0 )
		 * EnvGen.kr(env,doneAction: 2)

},nil,\audioEvent);

Instr([\spawnEvents,\PlayBuf,\oneShotLinen],{ arg sample,pchRatioStream=1.0,attackTime=0.01,
			susLevel=0.7,releaseTime=0.05;

	PlayBuf.ar( sample.numChannels, sample.bufnumIr,pchRatioStream,1,0,0 )
		 * Linen.kr(Impulse.kr(0),attackTime,susLevel,releaseTime,doneAction: 2)

},nil,\audioEvent);




[SinOsc,LFCub,LFPar,LFTri,Impulse,LFSaw].do({ arg ugen;

	Instr([\spawnEvents,ugen.asSymbol,\EnvGen],{ arg freq=1000,amp=0.1,env;

			ugen.ar(freq)
				* EnvGen.kr(env,1.0,amp,doneAction: 2)

	},nil,\audioEvent);
		
	Instr([\spawnEvents,ugen.asSymbol,\Linen],{ arg freq=1000,attackTime=0.1,susLevel=0.1,releaseTime=0.4;

			ugen.ar(freq)
				* Linen.kr(Impulse.kr(0),attackTime,susLevel,releaseTime,doneAction: 2)

	},nil,\audioEvent);


	Instr([\spawnEvents,ugen.asSymbol,\modulated,\Linen],{ arg freq=1000,attackTime=0.1,
				susLevel=0.1,releaseTime=0.4;

			ugen.ar(ugen.kr(ugen.kr(0.2,0,8,10),0, freq * 0.5,freq),0,0.1)
				* Linen.kr(Impulse.kr(0),attackTime,susLevel,releaseTime,doneAction: 2)

	},nil,\audioEvent);

});

[LFPulse,VarSaw].do({ arg ugen;

	Instr([\spawnEvents,ugen.asSymbol,\EnvGen],{ arg freq=1000,width=0.5,amp=0.1,env;

			ugen.ar(freq,width: width)
				* EnvGen.kr(env,1.0,amp,doneAction: 2)

	},nil,\audioEvent);
		
	Instr([\spawnEvents,ugen.asSymbol,\Linen],{ arg freq=1000,width=0.5,attackTime=0.1,
				susLevel=0.1,releaseTime=0.4;

			ugen.ar(freq,width: width)
				* Linen.kr(Impulse.kr(0),attackTime,susLevel,releaseTime,doneAction: 2)

	},nil,\audioEvent);

	Instr([\spawnEvents,ugen.asSymbol,\LFO],{ arg freq=1000,lfo=0.5,attackTime=0.1,susLevel=0.1,releaseTime=0.4;
			ugen.ar(freq,width: FSinOsc.kr(lfo).range(0,1))
				* Linen.kr(Impulse.kr(0),attackTime,susLevel,releaseTime,doneAction: 2)
	},nil,\audioEvent);

});


Instr("spawnEvents.EnvGen",{ arg audio, env,amp;

	audio * EnvGen.kr(env,levelScale:amp,doneAction:2)

},[
	\audio,
	EnvSpec(Env.linen),
	\amp
],
	'audioEvent'
);



