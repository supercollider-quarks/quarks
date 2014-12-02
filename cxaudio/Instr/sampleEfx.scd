
Instr([\sampleEfx,\Warp1],{ 
	// from Warp1 by Chad Kirby <ckirby@u.washington.edu>
	// inspired by sndwarp by Richard Karpen
	// optimised in SC2 by felix
	// SC3 version by felix
	
	 arg sample, pointer, pchRatio=1.0, windowSize=0.1, overlaps=10,
			windowRandRatio = 0.1, windowEnv,mul=1.0;
		
		var  sampleRate, bufSize,amp,bufBySR;
		var offset,rateScale,timeScale;

		sampleRate = sample.sampleRateKr;
		bufSize = sample.bufSamplesKr;
		rateScale = sample.bufRateScaleKr;

		windowEnv = windowEnv ?? { Env.linen( 0.5, 1.0, 0.5, 1, \welch ) };

		// try to keep the amplitude constant regardless of overlaps
		amp = (mul/overlaps)**0.5;

		// randomize the playbuf offset
		offset = pointer * bufSize;
		if(windowRandRatio != 0.0,{
			offset = offset + (WhiteNoise.kr.range(0.0,windowRandRatio * windowSize * sampleRate));
		});
		
		// as window size gets smaller, you used to get more overlaps
		// eg. 10 / 0.1 == 100 spawns a second

		pchRatio = pchRatio * rateScale;
		timeScale = min( windowSize, bufSize / sampleRate );

		Mix.fill(overlaps,{ arg i;
			var trig;
			// trigs offset equally
			trig = LFPulse.kr( windowSize.reciprocal , 1.0 / overlaps * i //* 2pi
												, 1.0 / overlaps);

			PlayBuf.ar( sample.numChannels,
					sample.bufnumKr, 
					Latch.kr(pchRatio,trig), 
					trig,
					offset, // latched by the trig
					1.0) // looping
				
			* EnvGen.kr(
				windowEnv,
				trig,
				timeScale: timeScale 
			)

		}).madd(amp);
},[
	\sample,
	\unipolar,
	\pchRatio,
	[0.02,1.0],
	\qnty, // static
	\unipolar,
	EnvSpec( Env.perc( 0.4, 0.6, 1.0,  \welch ) ),
	\mul
],\audio);






//
//Instr([\sampleEfx,\Warp1],{ 
//	// from Warp1
//	// by Chad Kirby <ckirby@u.washington.edu>
//	// inspired by sndwarp by Richard Karpen
//	// optimised by cx
//
//	 arg sample, pointer, pchRatio=1.0, windowSize=0.1, overlaps=10,
//			windowRandRatio = 0.1, windowEnv,mul=1.0;
//		
//		var  buffer,sampleRate, bufSize,amp,bufBySR;
//
//		buffer = sample.signal;
//		sampleRate = sample.sampleRate;
//		windowEnv = windowEnv ?? { Env.linen( 0.5, 0.0, 0.5, 1, \welch ) };
//		bufSize = sample.size;
//		// try to keep the amplitude constant regardless of overlaps
//		amp = (mul/overlaps)**0.5;
//		bufBySR = bufSize / sampleRate;
//		
//		TSpawn.ar( { arg spawm, i, synth;
//
//			var offset;
//		
//			// randomize the playbuf offset
//			offset = (pointer.abs * bufSize  + ( windowRandRatio.rand * windowSize * sampleRate ) );
//		
//		
//			PlayBuf.ar( buffer, sampleRate, pchRatio, offset, 0,
//					bufSize, 
//					EnvGen.kr( windowEnv, amp, 0, 1, 0, min( windowSize, bufBySR ) ) 
//			)
//		
//		}, sample.numChannels, nil, Impulse.kr( overlaps/windowSize ) );
//	
//},[
//	\sample,
//	\unipolar,
//	\pchRatio,
//	\window,
//	\qnty,
//	\unipolar,
//	EnvSpec( Env.linen( 0.5, 0.0, 0.5, 1, \welch ) ),
//	\mul
//]);


Instr([\sampleEfx,\Warp2],{ arg sample, pointer, pchRatio=1.0, windowSize=0.1, 
	 overlaps=10,windowRandRatio = 0.1;
		
		var  sampleRate, bufSize,amp;
		var offset,rateScale,timeScale;

		sampleRate = sample.sampleRateKr;
		bufSize = sample.bufSamplesKr;
		rateScale = sample.bufRateScaleKr;

		// randomize the playbuf offset
		offset = pointer * bufSize;
		if(windowRandRatio != 0.0,{
			offset = Wrap.kr( 
					offset + (WhiteNoise.kr.range(0.0,windowRandRatio * windowSize * sampleRate)),
					0.0,
					bufSize);
		});
		
		pchRatio = pchRatio * rateScale;
		//timeScale = min( windowSize, bufSize / sampleRate );

		Mix.fill(overlaps,{ arg i;
			var trig,tap;
			// trigs offset equally
			trig = LFPulse.kr( windowSize.reciprocal , 1.0 / overlaps * i //* 2pi
												, 1.0 / overlaps);

			tap = PlayBuf.ar( sample.numChannels,
					sample.bufnumKr, 
					Latch.kr(pchRatio,trig), 
					trig,
					offset, // latched by the trig
					1.0); // looping
			
			// use a wave instead	
			tap * FSinOsc.kr(windowSize.reciprocal * 0.5, 
						
						(1.0 / overlaps * i ) * 2pi
						).abs;
						
		}).madd(overlaps.reciprocal);
},[
	\sample,
	\unipolar,
	\pchRatio,
	[0.02,1.0],
	\qnty, // static
	\unipolar,
	\mul
],\audio);










Instr([\sampleEfx,\riddimWarp],{  arg sample, pointer, trig, pchRatio=1.0, wobble=0.1, windowEnv;
		var offset;
		// old style
		windowEnv = windowEnv ?? { Env.linen( 0.5, 0.0, 0.5, 1, \welch ) };
	// wobble is wrong
		offset = pointer + wobble * sample.bufSamplesKr;
		pchRatio = pchRatio * sample.bufRateScaleKr;
		
		// add delays layer
		PlayBuf.ar( 
			sample.numChannels,
			sample.bufnumKr, 
			Latch.kr(pchRatio,trig), 
			trig,
			offset, // latched by the trig
			1.0) // looping on
			
		* EnvGen.kr(
			windowEnv,
			trig
		)

/*		
		SC2 version:
		
		var  buffer,sampleRate, bufSize;
		
		// prevent messaging inside the tspawn
		buffer = sample.signal;
		sampleRate = sample.sampleRate;
		bufSize = sample.size;
		
		windowEnv = windowEnv ?? { Env.linen( 0.5, 0.0, 0.5, 1, \welch ) };
		
		TSpawn.ar( { arg spawm, i, synth;

			PlayBuf.ar( buffer, sampleRate, pchRatio, 
			pointer + wobble * bufSize, 0, sample.end , EnvGen.kr(windowEnv) )

		}, sample.numChannels, nil, trigger );
*/	
		
},[
	\sample,
	\unipolar,
	\trig,
	\pchRatio,
	\wobble,
	EnvSpec( Env.linen( 0.5, 0.0, 0.5, 1, \welch ) )
],\audio);


Instr([\sampleEfx,\riddimWarp2],{  arg sample, pointer, trig, pchRatio=1.0, wobble=0.1, windowEnv;
		var offset;
		// old style
		windowEnv = windowEnv ?? { Env.linen( 0.5, 0.0, 0.5, 1, \welch ) };
		offset = Wrap.kr(pointer + WhiteNoise.kr(wobble),0.0,1.0) * sample.bufSamplesKr;
		pchRatio = pchRatio * sample.bufRateScaleKr;
		
		PlayBuf.ar( 
			sample.numChannels,
			sample.bufnumKr, 
			Latch.kr(pchRatio,trig), 
			trig,
			offset, // latched by the trig
		1.0) // looping off
			
		* EnvGen.kr(
			windowEnv,
			trig
		)
		
},[
	\sample,
	\unipolar,
	\trig,
	\pchRatio,
	\wobble,
	EnvSpec( Env.linen( 0.5, 0.0, 0.5, 1, \welch ) )
],\audio);







