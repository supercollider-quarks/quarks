/* 	Simple reverb class, based on MoorerLoyReverb as given in Pope, Sc1 Tutorial.
	input is converted to mono and filtered,
	dense reverb is done with a bank of comb filters with prime ratio delaytimes;
	hfDamping uses side CombL side effect for freq dependent decay.
	to do: get a good list of primes in sequence together and hardcode them...
*/
AdCVerb {

	classvar <>verbose = false, <>maxTime = 0.2;

	*ar { arg in, revTime = 3, hfDamping = 0.1, nOuts = 2, predelay = 0.02,
		numCombs = 8, numAllpasses = 4, inFilter = 0.6, leakCoeff = 0.995,
		combScale = 1, apScale = 1, allpassPrimes;

		var timeOneSample;	// used for comb average-filtering;
		var primeRange;

		var combTimes,	// Table of combtimes
		allpassTimes,		// delayTimes for allpasses
		combsOut, apDecay;

		timeOneSample = SampleDur.ir;

	// Initialize comb table for longer reverberations
	//	 "// combs: ".post;
		  		// try creating good prime number based delayTimes with e.g. :
	//	combTimes = ({ rrand(100, 400).nthPrime } ! numCombs).sort.postln / 40000;

		combTimes = [
			0.0797949, 			// new prime Numbers
			0.060825,
			0.0475902,
			0.0854197,
			0.0486931,
			0.0654572,
			0.0717437,
			0.0826624,
			0.0707511,
			0.0579574,
			0.0634719,
			0.0662292
		];

		combTimes = combTimes.copyRange(0, numCombs - 1);
	//	combTimes.postln;
	// Initialize allpass delay times:
	//	 "// allpasses: ".post;

		allpassPrimes = allpassPrimes ?? {
			primeRange = 250 div: numAllpasses;
			{
				{ |i| rrand(i + 1 * primeRange, i + 2 * primeRange).nthPrime } ! numAllpasses
			} ! nOuts
		};

		allpassTimes = allpassPrimes * (1/44100); // scale into a good time range.

		if (verbose) {
			"// AdCVerb - allpassPrimes are: \n    %\n\n".postf(allpassPrimes);
		};

	// mix input down to mono if needed, block DC, round off and pre-delay reverb input.
		in = DelayN.ar(
			OnePole.ar(
				LeakDC.ar(in.asArray.sum, leakCoeff),
				inFilter
			),
			maxTime,
			predelay
		);

	// Create an array of combs, with a special trick to make treble decay faster than lows:
		if (numCombs > 0) {
			 combsOut = CombL.ar(in, maxTime,

			 	(combTimes * combScale)
			 		.round(timeOneSample)	// round delay times to integer samples
			 	+ 						// and add up to half a sample to them:
			 							// linear interpolation between samples loses
			 							// high freq energy, with the maximum at 0.5.
			 	(timeOneSample * 0.5 * hfDamping),
			 	revTime
			 ).sum
		 } { combsOut = 0 };

	// allpass decay always is shorter than combs decay
		apDecay = 1.min(revTime * 0.6);

	// Put the output through nOuts parallel chains of allpass delays
		^allpassTimes.collect({ |timesCh| var out;
			out = combsOut + in;
			timesCh.do { |time| out = AllpassN.ar(out, maxTime, time * apScale, apDecay) };
			out;
		});
	 }
}

/// same as AdCVerb, but can have random animation on the combTimes - animRate, animDepth.

AdCVerb2 {

	classvar <>verbose = false, <>maxTime = 0.2;

	*ar { arg in, revTime = 3, animRate = 0.1, animDepth = 0.3,
		hfDamping = 0.1, nOuts = 2, predelay = 0.02,
		numCombs = 8, numAllpasses = 4, inFilter = 0.6, leakCoeff = 0.995,
		combScale = 1, apScale = 1, allpassPrimes;

		var timeOneSample;	// used for comb average-filtering;
		var primeRange;

		var combTimes,	// Table of combtimes
		allpassTimes,		// delayTimes for allpasses
		combsOut, apDecay;

		var combDrifts;

		timeOneSample = SampleDur.ir;

	// Initialize comb table for longer reverberations
	//	 "// combs: ".post;
		  		// try creating good prime number based delayTimes with e.g. :
	//	combTimes = ({ rrand(100, 400).nthPrime } ! numCombs).sort.postln / 40000;

		combTimes = [
			0.0797949, 			// new prime Numbers
			0.060825,
			0.0475902,
			0.0854197,
			0.0486931,
			0.0654572,
			0.0717437,
			0.0826624,
			0.0707511,
			0.0579574,
			0.0634719,
			0.0662292
		];

		combTimes = combTimes.copyRange(0, numCombs - 1);

		combDrifts = LFDNoise3.kr(animRate ! numCombs)
		.range(combTimes.minItem, combTimes.maxItem);
		combTimes = combTimes.blend(combDrifts, animDepth);

	//	combTimes.postln;
	// Initialize allpass delay times:
	//	 "// allpasses: ".post;

		allpassPrimes = allpassPrimes ?? {
			primeRange = 250 div: numAllpasses;
			{
				{ |i| rrand(i + 1 * primeRange, i + 2 * primeRange).nthPrime } ! numAllpasses
			} ! nOuts
		};

		allpassTimes = allpassPrimes * (1/44100); // scale into a good time range.

		if (verbose) {
			"// AdCVerb - allpassPrimes are: \n    %\n\n".postf(allpassPrimes);
		};

	// mix input down to mono if needed, block DC, round off and pre-delay reverb input.
		in = DelayN.ar(
			OnePole.ar(
				LeakDC.ar(in.asArray.sum, leakCoeff),
				inFilter
			),
			maxTime,
			predelay
		);

	// Create an array of combs, with a special trick to make treble decay faster than lows:
		if (numCombs > 0) {
			 combsOut = CombL.ar(in, maxTime,

			 	(combTimes * combScale)
			 		.round(timeOneSample)	// round delay times to integer samples
			 	+ 						// and add up to half a sample to them:
			 							// linear interpolation between samples loses
			 							// high freq energy, with the maximum at 0.5.
			 	(timeOneSample * 0.5 * hfDamping),
			 	revTime
			 ).sum
		 } { combsOut = 0 };

	// allpass decay always is shorter than combs decay
		apDecay = 1.min(revTime * 0.6);

	// Put the output through nOuts parallel chains of allpass delays
		^allpassTimes.collect({ |timesCh| var out;
			out = combsOut + in;
			timesCh.do { |time| out = AllpassN.ar(out, maxTime, time * apScale, apDecay) };
			out;
		});
	 }
}
