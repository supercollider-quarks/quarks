Siren {

	// emulate the waveform of a siren

	*ar { |freq, sharpness = 2, iphase = 0, modPhase = 0, noiseLevel = 0, mul = 1, add = 0|
		var wave;
		wave = SinOsc.ar( 0, MulAdd( LFSaw.ar( freq, iphase/pi )**sharpness, pi, modPhase ) );
		if( noiseLevel == 0 )
			{ ^wave.madd( mul, add ) }
			{ ^((wave * (1-noiseLevel)) + (PinkNoise.ar( noiseLevel )*wave.range(0,1)))
				.madd( mul, add );}
		}
		
	*kr { |freq, sharpness = 2, iphase = 0, modPhase = 0, mul = 1, add = 0| // no noise here
		^SinOsc.kr( 0, MulAdd( LFSaw.kr( freq, iphase/pi )**sharpness, 
			pi, modPhase ) ).madd(mul,add); 
		}
	
	}