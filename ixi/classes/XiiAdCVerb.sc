/* 	Simple reverb class, based on MoorerLoyReverb as given in Pope, Sc1 Tutorial. 
	input is converted to mono and filtered, 
	dense reverb is done with a bank of comb filters with prime ratio delaytimes;
	hfDamping uses side CombL side effect for freq dependent decay. 
	to do: get a good list of primes in sequence together and hardcode them...
	
	thor: I made this an ixiQuarks class as I always forgot to include it in binaries
	and I changed the name as the reverb class itself had the AdC name after its author.
*/
XiiAdCreVerb { 	

	*ar { arg in, revTime = 3, hfDamping = 0.1, nOuts = 2, predelay = 0.02,
		numCombs = 8, numAllpasses = 4, inFilter = 0.6, combScale = 1, apScale = 1;
	
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
		combTimes.postln;
	// Initialize allpass delay times:		  
		 "// allpasses: ".post;
		primeRange = 250 div: numAllpasses; 
		
		allpassTimes = ({ 
			{ |i| rrand(i + 1 * primeRange, i + 2 * primeRange).nthPrime } ! numAllpasses 
		} ! nOuts).postln / 44000; 
	
	// mix input down to mono if needed, round off and pre-delay reverb input.
		in = DelayN.ar(OnePole.ar(in.asArray.sum, inFilter), 0.2, predelay);  
		
	// Create an array of combs:
		if (numCombs > 0) { 
			 combsOut = CombL.ar(in, 0.2, 
			 	(combTimes * combScale).round(timeOneSample)
			 	+ (timeOneSample * 0.5 * hfDamping), 
			 	revTime
			 ).sum 
		 } { combsOut = 0 };
	
	// Put the output through two parallel chains of allpass delays
		apDecay = 1.min(revTime * 0.6);
		
		^allpassTimes.collect({ |timesCh| var out;
			out = combsOut + in;
			timesCh.do { |time| out = AllpassN.ar(out, 0.2, time * apScale, apDecay) };
			out;
		});
	 }
}