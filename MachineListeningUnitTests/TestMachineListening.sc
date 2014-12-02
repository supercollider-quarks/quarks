/**
* Unit tests for Machine Listening UGens, (c) 2010 Dan Stowell, published under the GPLv2+.
* These tests check that temporal/spectral features correlate,
* or don't correlate, with some basic signal modulations. 
* There's a probabilistic aspect meaning some tests may very rarely fail by chance.
*/
// s.boot;
//TestMachineListening().run;

TestMachineListening : UnitTest {
		
	// array of tests and their expected correlation bounds.
	// [label, func, min corr cutoff, max corr cutoff, min corr amp, max corr amp]
	classvar <corrtesters;
	
	*initClass{
		corrtesters = [
		["SpecCentroid", {|a,c| SpecCentroid.kr(c)}          , 0.95, 1, -0.09, 0.09],
		["FFTPower",     {|a,c| FFTPower.kr(c) }             , 0.85, 1,  0.92, 1],
		["Loudness",     {|a,c| Loudness.kr(c) }             , 0.85, 1,  0.92, 1],
		["SpecPcile 95", {|a,c| SpecPcile.kr(c, 0.95) }      , 0.93, 1, -0.05, 0.05],
		["SpecPcile 50", {|a,c| SpecPcile.kr(c, 0.50) }      , 0.93, 1, -0.11, 0.11],
		["SpecPcile 25", {|a,c| SpecPcile.kr(c, 0.25) }      , 0.93, 1, -0.13, 0.13],
		["FFTSubbandPower 1600--6400", {|a,c| FFTSubbandPower.kr(c, #[1600, 6400])[1] }, 0.87, 1, 0.9, 1],
		["SpecFlatness", {|a,c| SpecFlatness.kr(c) }         , 0.90, 1, -0.05, 0.05],
		["FFTPower",     {|a,c| FFTPower.kr(c) }             , 0.85, 1,  0.92, 1],
//		["", {|a,c| }, 0.9, 1],
		["ZeroCrossing", {|a,c| A2K.kr(ZeroCrossing.ar(a)) } , 0.8,  1, -0.08, 0.08]
		];
	}

	/**
	* Test correlations against varying cutoff of filtered noise.
	*/
	test_corr_cutoff 
	{
		var genfunc = {
				var input = XLine.kr(5000, 100, 10 );
				var son  = MoogFF.ar(WhiteNoise.ar, input);
				[input, son]
			};
		this.doCorrelTest(genfunc, 2, 3);
	}
	
	/**
	* Test correlations against varying amplitude of modulated noise
	* - often negative tests in that we expect near-zero correlation, various measures are meant to be amplitude-invariant.
	*/
	test_corr_amp {
		var genfunc = {
				var input = XLine.kr(0.001, 1, 10 );
				var son  = WhiteNoise.ar * input;
				[input, son]
			};		
		this.doCorrelTest(genfunc, 4, 5);
	}
	
	doCorrelTest { |genfunc, indexmin, indexmax|
		var testsIncomplete = 1;
		var func = {
				var input, son, chain, ana;
				# input, son = genfunc.value;
				chain = FFT(LocalBuf(1024), son);
				ana = this.class.corrtesters.collect{|tester|
						tester[1].value(son, chain)
					};
				Out.ar(0, son * 0.1);
				[input] ++ ana
			};
		func.loadToFloatArray(10, Server.default, { |data|
			var in, outs, corr;
			data = data.clump(this.class.corrtesters.size + 1).flop;
			in = data[0];
			outs = data[1..];
			corr = outs.collect{|anout| corr(in, anout)};
			this.class.corrtesters.do{|tester, index|
				//"%, corr is %".format(tester[0], corr[index]).postln;
				this.assert((corr[index] >= tester[indexmin]) and: {corr[index] <= tester[indexmax]},
					"for % should lie within [%, %], is %"
						.format(tester[0], tester[indexmin], tester[indexmax], corr[index])
					)
			};
			testsIncomplete = testsIncomplete - 1;
		});
		this.wait{testsIncomplete==0};
	} // doCorrelTest
}
