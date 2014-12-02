
// see sampler
// synth stays open and retrigs the sample multiple times

Spec.add(\rqenv, EnvSpec(  Env.new([ 0.194444, 0.0810185, 0.0648148, 0.444444 ], [ 0.01, 0.111111, 0.0833333 ], [ -0.583333, 3.33333, 1.66667 ], nil, nil)));

Spec.add(\envpercshort, EnvSpec(Env.new([ 0, 1, 1, 0.444444, 0 ], [ 0.166667, 1, 0.805556, 0.777778 ], [ -7.16667, -2, 2, -2 ], nil, nil)));



Instr([\samplerf,\rlpf,\rq],{ arg sample,trig,envpercshort,fenv,rq=0.5,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;

		Enveloper2.ar(

			RLPF.ar(
				PlayBuf.ar(
					sample.numChannels,
					sample.bufnumKr,
					sample.bufRateScaleKr * pchRatio,
		 		  	trig,
		 		  	// offset
		 		  	beatStart * sample.beatsizeIr,
		 		  	0.0
		 		  ),
		 		  EnvGen.kr(fenv,trig,ffreqMul,ffreqAdd),
		 		  rq)

	 	,trig,envpercshort)

},nil,\audio);


Instr([\samplerf,\rlpf,\rqenv],{ arg sample,trig,envpercshort,fenv,rqenv,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;

		Enveloper2.ar(
			RLPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(fenv,trig,ffreqMul,ffreqAdd),
		 		  EnvGen.kr(rqenv,trig)
	 		 )
	 	,trig,envpercshort)
},nil,\audio);



Instr([\samplerf,\rlpf4,\rq],{ arg sample,trig,envpercshort,envlinen,rq=0.5,pchRatio=1.0,
		beatStart=0.0,ffreq4Mul=0.8,ffreq4Add=0.0;

		Enveloper2.ar(
			RLPF.ar(
			PlayBuf.ar(
	 		 	sample.numChannels,
	 		  	sample.bufnumKr,
	 		  	// pitch
				sample.bufRateScaleKr * pchRatio,
				trig,
	 		  	// offset
	 		  	beatStart * sample.beatsize,
				0.0 // no loop
	 		  ),
	 		  EnvGen.kr(envlinen,trig,\freq.asSpec.map(ffreq4Mul),\freq.asSpec.map(ffreq4Add)),
	 		  rq)
	 	,trig,envpercshort)

},[
//	nil,nil,nil,nil,nil,nil,nil,
//	StaticSpec(0.0,1.0),
//	StaticSpec(0.0,1.0)
],\audio);

Instr([\samplerf,\rlpf4,\rqenv],{ arg sample,trig,envpercshort,envlinen,rqenv,pchRatio=1.0,
		beatStart=0.0,ffreq4Mul=0.8,ffreq4Add=0.0;

		Enveloper2.ar(
			RLPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(envlinen,trig,\freq.asSpec.map(ffreq4Mul),
		 		  							\freq.asSpec.map(ffreq4Add)),
		 		  EnvGen.kr(rqenv,trig)
	 		 )
	 	,trig,envpercshort)
},[
//	nil,nil,nil,nil,nil,nil,nil,
//	StaticSpec(0.0,1.0),
//	StaticSpec(0.0,1.0)
],\audio);





Instr([\samplerf,\lpf],{ arg sample,trig,envpercshort,fenv,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;

		Enveloper2.ar(
			LPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(fenv,trig, ffreqMul,ffreqAdd)
		 	),
		trig,envpercshort)

},nil,\audio);



Instr([\samplerf,\hpf],{ arg sample,trig,envpercshort,fenv,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;

		Enveloper2.ar(
			HPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(fenv,trig, ffreqMul,ffreqAdd)
		 	)
	 	,trig,envpercshort)

},nil,\audio);

Instr([\samplerf,\rhpf,\rq],{ arg sample,trig,envpercshort,fenv,rq=0.5,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;


		Enveloper2.ar(
			RHPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(fenv,trig, ffreqMul,ffreqAdd),
		 		  rq
		 	)
	 	,trig,envpercshort)

},nil,\audio);



Instr([\samplerf,\rhpf,\rqenv],{ arg sample,trig,envpercshort,fenv,rqenv,pchRatio=1.0,
		beatStart=0.0,ffreqMul=2000.0,ffreqAdd=40.0;

		Enveloper2.ar(
			RHPF.ar(
				PlayBuf.ar(
		 		 	sample.numChannels,
		 		  	sample.bufnumKr,
		 		  	// pitch
					sample.bufRateScaleKr * pchRatio,
					trig,
		 		  	// offset
		 		  	beatStart * sample.beatsize,
					0.0 // no loop
		 		  ),
		 		  EnvGen.kr(fenv,trig, ffreqMul,ffreqAdd),
		 		  EnvGen.kr(rqenv,trig)
		 	)
	 	,trig,envpercshort)

},nil,\audio);


