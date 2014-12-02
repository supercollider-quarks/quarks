
Crucial {

	*initClass {
		Class.initClassTree(GUI);
		// this skin is cleaner and more basic than the default
		// GUI.setSkin(\crucial) in your startup
		GUI.skins.put(\crucial,
			(
				fontSpecs: 	["Helvetica", 11.0],
				fontColor: 	Color.black,
				background: 	Color(1.0, 1.0, 1.0, 0.80597014925373),
				foreground:	Color.grey(0.95),
				onColor:		Color.new255(255, 250, 250),
				offColor:		Color(0.77, 0.77, 0.77, 0.6),
				gap:			4 @ 4,
				margin: 		2@0,
				buttonHeight:	17,
				focusColor: Color(1.0, 0.98507462686567,0)
			));
	}

	*initSpecs {
		// optional
		
		Class.initClassTree(Warp);
		Class.initClassTree(Spec);

		Spec.specs.putAll(
		  IdentityDictionary[
			'audio'->AudioSpec.new,
			'input'->AudioSpec.new,
			'unipolar'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'bipolar'->ControlSpec.new(-1, 1, 'lin', 0, 0),

			//'lofreq'->ControlSpec.new(0.1, 100, 'exp', 0, 6),
			//'rq'->ControlSpec.new(0.001, 2, 'exp', 0, 0.707),
			//'boostcut'->ControlSpec.new(-20, 20, 'lin', 0, 0),
			'bw'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'octave'->ControlSpec.new(-6, 10, 'lin', 1, 2),
			'sampleStart'->ControlSpec.new(0, 4, 'lin', 0.25, 2),
			'degree'->ControlSpec.new(0, 11, 'lin', 1, 6),
			//'ffreq4'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'sdetune'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'slewRise'->ControlSpec.new(10, 10000, 'lin', 0, 5005),
			'slewFall'->ControlSpec.new(10, 10000, 'lin', 0, 5005),
			'pchRatio'->ControlSpec.new(-4.0, 4.0, 'lin', 0, 2),
			'pitch'->ControlSpec.new(-4, 4, 'lin', 0, 0),

			'trig'->TrigSpec.new(0, 1, 'lin', 0, 0.0),
			'trigger'->TrigSpec.new(0, 1, 'lin', 0, 0.0),
			'gate'-> ControlSpec(0, 1, 'lin', 0, 0.0),

			'legato'->StaticSpec.new(0.01, 4, 'lin', 0, 0.9),
			'release'->ControlSpec.new(0, 16, 'lin', 0, 0.5),
			'bicoef'->ControlSpec.new(-1, 1, 'lin', 0, 0.2),
			'freq2'->ControlSpec.new(40, 5000, 'exp', 0, 447.214),
			// goes to 8
			'rq8'->ControlSpec.new(0.0001, 8, 'exp', 0, 1.0),
			'safeffreq'->ControlSpec.new(200, 16000, 'exp', 0, 1788.85),
			'saferq'->ControlSpec.new(0.001, 0.7, 'lin', 0, 0.3505),
			'chaos'->ControlSpec.new(0, 5, 'lin', 0, 2.5),
			'freqOffset'->ControlSpec.new(-4000, 4000, 'lin', 0, 0),
			'bwr'->ControlSpec.new(0, 1, 'lin', 0, 0.5),//bandwithratio
			'delayDecay'->ControlSpec.new(-2, 2, 'lin', 0, 0),
			'binstretch'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'binshift'->ControlSpec.new(0, 16, 'lin', 0, 8),
			'lfo'->ControlSpec.new(0, 3000, 'lin', 0, 1500),
			'width'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'radians'->ControlSpec.new(0, 6.28319, 'lin', 0, 3.14159),
			'numChannels'->StaticSpec.new(1, 8, 'lin', 1, 2),
			'freqScale'->ControlSpec.new(0.01, 10, 'lin', 0, 1.0),
			'qnty0'->StaticIntegerSpec.new(0, 20, 10),
			'ffreqMul'->ControlSpec.new(0.1, 16000, 'exp', 0, 2),
			//'freq'->ControlSpec.new(20, 20000, 'exp', 0, 440),
			//'phase'->ControlSpec.new(0, 6.28319, 'lin', 0, 3.14159),
			'ffreqAdd'->ControlSpec.new(0.1, 16000, 'exp', 0, 40),
			'ffreq'->ControlSpec.new(60, 20000, 'exp', 0, 1095.45),
			'velocity'->ControlSpec.new(0, 127, 'lin', 0, 63.5),
			'vibRate'->ControlSpec.new(0, 20, 'lin', 0, 10),
			'vibDepth'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'tremRate'->ControlSpec.new(0, 20, 'lin', 0, 10),
			'tremDepth'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'panRate'->ControlSpec.new(0, 20, 'lin', 0, 10),
			'panDepth'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'thru'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'off'->NoLagControlSpec.new(0, 1, 'lin', 0, 0.5),
			'revTime'->ControlSpec.new(0, 16, 'lin', 0, 8),
			'taps'->StaticIntegerSpec.new(1, 10, 6),
			'combs'->StaticIntegerSpec.new(1, 10, 6),
			'microDelay'->ControlSpec.new(0.0001, 0.05, 'lin', 0, 0.02505),
			'microAttack'->ControlSpec.new(0.0001, 0.2, 'lin', 0, 0.10005),
			'microDecay'->ControlSpec.new(0.0001, 0.2, 'lin', 0, 0.10005),
			'combSelect'->StaticIntegerSpec.new(0, 5, 3),
			'medianLength'->StaticIntegerSpec.new(0, 15, 8),
			'uzi'->StaticIntegerSpec.new(1, 16, 9),
			'numharms'->ControlSpec.new(1, 100, 'lin', 0, 50.5),
			'sustain'->ControlSpec.new(0, 16, 'lin', 0, 8),
			'sensitivity'->ControlSpec.new(0, 12, 'lin', 0, 6),
			'gain'->ControlSpec.new(0.000001, 4, 'exp', 0, 2),
			'dur'->ControlSpec.new(0, 16, 'lin', 0, 1),
			'density'->ControlSpec.new(0, 30, 'lin', 0, 1.0),
			'qnty'->StaticIntegerSpec.new(1, 24, 4),
			'winSize'->StaticSpec.new(0.01, 4, 'lin', 0, 0.2),
			'pchDispersion'->ControlSpec.new(0, 4, 'lin', 0, 0.05),
			'timeDispersion'->ControlSpec.new(0, 3, 'lin', 0, 0.05),
			'maxBeats'->StaticSpec.new(0.125, 8, 'lin', 0, 4),
			'drive'->ControlSpec.new(0, 10, 'lin', 0, 1.5),
			'feedback'->ControlSpec.new(0, 1, 'lin', 0, 0.7),
			'overlap'->ControlSpec.new(0, 12, 'lin', 0, 6),
			'maxDelay'->StaticSpec.new(0.005, 1, 'lin', 0, 0.5),
			'speed'->ControlSpec.new(0.001, 8, 'lin', 0, 4.0),
			'root'->ControlSpec.new(0, 64, 'lin', 1, 0),
			'bidecay'->ControlSpec.new(-10, 10, 'lin', 0, 0.05),
			'midinote'->ControlSpec.new(0, 127, 'lin', 1, 64),
			'note'->ControlSpec.new(0, 11, 'lin', 1, 0),
			'timeScale'->ControlSpec.new(0.1, 10, 'lin', 0, 1.0),
			'pmindex'->ControlSpec.new(0, 20, 'lin', 0, 10),
			//'rate'->ControlSpec.new(0.125, 8, 'exp', 0, 1),
			'beat'->ControlSpec.new(0.001, 1, 'lin', 0.125, 0.5),
			'decay'->ControlSpec.new(0, 16, 'lin', 0, 1.5),
			'mix'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'post'->ControlSpec.new(0, 1, 'lin', 0, 1.0),
			'lag'->ControlSpec.new(0, 1, 'lin', 0, 0.1),
			'stretch'->ControlSpec.new(0.0125, 4, 'lin', 0, 1.0),
			'tempoFactor'->ControlSpec.new(0.125, 4, 'lin', 0.125, 1.0),
			'coef'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'widefreq'->ControlSpec.new(0.1, 20000, 'exp', 0, 440),
			'attack'->ControlSpec.new(0, 16, 'lin', 0, 0.1),
			'chaosParam'->ControlSpec.new(1, 30, 'lin', 0, 2.0),
			'dt'->ControlSpec.new(0.01, 0.04, 'lin', 0, 0.025),
			'iseed'->StaticSpec.new(1, 1000000, 'lin', 1, 500001),
			'midi'->ControlSpec.new(0, 127, 'lin', 0, 64),
			'imod'->StaticSpec.new(1, 1000000, 'lin', 1, 500001),
			'fdrive'->ControlSpec.new(0, 20, 'lin', 0, 10),
			'audio1'->AudioSpec.new,
			'pre'->ControlSpec.new(0, 1, 'lin', 0, 0.5),
			'audio2'->AudioSpec.new,
			'midivelocity'->ControlSpec.new(1, 127, 'lin', 0, 64),
			//'delay'->ControlSpec.new(0.005, 1, 'lin', 0, 0.5025),
			\in->AudioSpec.new,
			\k->ControlSpec(-6.0,6.0),
			\stepsPerOctave->ControlSpec(1.0,128.0,\lin,1.0,12.0),
			\mul -> ControlSpec(0.0,1.0,\lin,0,1.0),
			\add -> ControlSpec(0.0,1.0,\lin,0.0,0.0),
			'bufnum' -> StaticIntegerSpec(0,100,0),
			'sndbuf' -> StaticIntegerSpec(0,100,0),
			\instr -> InstrNameSpec()
			
		  ]
		);
	}
}
