
/*
	keeping the synth running
	trig input triggers the sample playback
	2 separate play bufs alternate so that the envelope release overlaps

	the level of the trig is used for the amp
	
*/

Spec.add(\env3, EnvSpec(Env.new([0,1,1,0],[0,1,0]).releaseNode_(2)));


//var trigf;
//trigf = 	{ arg t; Trig1.kr(t,0.4); };
//trigf = { arg t; 1.0 - t };

Instr("sampler.play1ByPitch",{ arg sample,trig,envperc,pchRatio=1.0,fStart=0.0;

	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
	pchRatio = sample.bufRateScaleIr * pchRatio;
	fStart = fStart * sample.bufFramesIr;
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			pchRatio,
			a,
			fStart,
			0.0)

		* EnvGen.kr(envperc, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			pchRatio,
			b,
			fStart,
			0.0)

		* EnvGen.kr(envperc, b, amp )
)


},nil,\audio);



Instr([\sampler,\looped,\beatLock],{ arg sample,trig,env3sustain,pchRatio=1.0,beatStart=0.0;

	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
	pchRatio = pchRatio * sample.pchRatioKr;
	beatStart = beatStart * sample.beatsizeIr;
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			pchRatio,
			a,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			pchRatio,
			b,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, b, amp )
)


/*
		ReTrigger2.ar({
			PlayBuf.ar(
	 		 	sample.signal, 
	 		  	sample.sampleRate, 
	 		  	// pitch
	 		  	pchRatio ,
	 		  	// offset
	 		  	beatStart * sample.beatsize,
	 		  	0, sample.end //loop points
	 		  )
	 	},trig,env3sustain,nil,sample.numChannels)
*/
	 	
},nil,\audio);


Instr([\sampler,\looped,\origTempo] ,{  arg sample,trig,env3sustain,pchRatio=1.0,beatStart=0.0;
				
	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
	pchRatio = pchRatio * sample.bufRateScaleIr;
	beatStart = beatStart * sample.beatsizeIr;
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			a,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			b,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, b, amp )
)
/*
		pchRatio = pchRatio * sample.bufRateScaleIr;
		
		PlayBuf.ar(sample.numChannels,
				sample.bufnumIr,
				pchRatio,
				trig,
				beatStart * sample.beatsize * sample.bufFramesIr,
				1.0)
				
* EnvGen.kr(env3sustain, trigf.value(trig),Latch.kr(trig,trig) )
*/
},nil,\audio);



Instr([\sampler,\oneShot,\beatLock],{ arg sample,trig,env3sustain,pchRatio=1.0,beatStart=0.0;


	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
	pchRatio = pchRatio * sample.pchRatioKr;
	beatStart = beatStart * sample.beatsizeIr;
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			a,
			beatStart,
			0.0)

		* EnvGen.kr(env3sustain, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			b,
			beatStart,
			0.0)

		* EnvGen.kr(env3sustain, b, amp )
)


/*
		// check for 1.0
		pchRatio = pchRatio * sample.pchRatioKr(tempo);
		
		PlayBuf.ar(sample.numChannels,
				sample.bufnumIr,
				pchRatio,
				trig,
				beatStart * sample.beatsize * sample.bufFramesIr,
				0.0)

* EnvGen.kr(env3sustain, trigf.value(trig),Latch.kr(trig,trig) )
*/
	
},nil,\audio);

Instr([\sampler,\oneShot,\origTempo],{ arg sample,trig,env3sustain,pchRatio=1.0,beatStart=0.0;

	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
	pchRatio = pchRatio * sample.bufRateScaleIr;
	beatStart = beatStart * sample.beatsizeIr;
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			a,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			b,
			beatStart,
			1.0)

		* EnvGen.kr(env3sustain, b, amp )
)

/*
		pchRatio = pchRatio * sample.bufRateScaleIr;			PlayBuf.ar(sample.numChannels,				
				sample.bufnumIr,				
				pchRatio,				
				trig,				
				beatStart * sample.beatsize * sample.bufFramesIr,				0.0)

* EnvGen.kr(env3sustain, trigf.value(trig),Latch.kr(trig,trig) )
*/

},nil,\audio);


// single version
//Instr([\sampler,\oneShot,\cheap],{ arg sample,trig,env;
//
//	PlayBuf.ar(sample.numChannels,
//			sample.bufnumIr,
//			sample.bufRateScaleIr,
//			trig,
//			0.0,
//			0.0)
//
//* EnvGen.kr(env, trigf.value(trig),Latch.kr(trig,trig) )
//			
//});
//

Instr([\sampler,\oneShot,\cheap],{ arg sample,trig,env;

	var a,b,amp;
	a = ToggleFF.kr(trig);
	b = 1.0 - a;
	amp = Latch.kr(trig,trig);
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			a,
			0.0,
			0.0)

		* EnvGen.kr(env, a,amp )
)
+
(
	PlayBuf.ar(sample.numChannels,
			sample.bufnumIr,
			sample.bufRateScaleIr,
			b,
			0.0,
			0.0)

		* EnvGen.kr(env, b, amp )
)
			
},nil,\audio);





// record


// LPF RLPF HPF RHPF with fenv sweeps

// integrator with huff
// slew it out
// continous loop on sample, mod loop points
	// better to spawn over
// continous then enveloped

