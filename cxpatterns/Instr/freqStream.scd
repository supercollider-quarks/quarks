
/*
	taking degrees, frequencies, durations, scales in various combinations
	and returning objects that supply frequency
*/


Spec.specs.addAll([
	\freqStream -> StreamSpec(\freq.asSpec.as(StaticSpec)),

	\scale -> ScaleSpec(#[0,2,4,5,7,9,11]),
	// for modal jazz
	\scaleStream -> StreamSpec(\scale),
	\rootStream -> StreamSpec(\root.asSpec.as(StaticIntegerSpec)),
	\cycleLength -> StaticIntegerSpec(2,1024,default:16)
	]);



// given degrees and scales, return frequencies
Instr([\freqStream,\streamByDegree],{ arg degreeStream,scaleStream,octave=4;
	(degreeStream + (octave * 12)).degreeToKey(scaleStream).midicps

},	[
	\degreeStream,
	\scaleStream,
	\octaveStream
],
	\freqStream
);


Instr([\freqStream,\divStutter],{ arg root=0,scale,cycleLength=16,octave=4;

	var list;
	cycleLength = min(4,cycleLength);
	(cycleLength).do({ arg i;
		i = i % 4;
		(cycleLength div: 4).do({
			list = list.add( i + 1 );
		})
	});
	//[root,octave,scale,(root + (octave*12) + scale).midicps].debug;
	PdurStutter(
		PseqLive(list.copyRange(0,cycleLength-1),0,5,1),
		Pseq((root + (octave*12) + scale).midicps ,inf)
	)
},[
	\root.asSpec.as(StaticSpec),
	\scale,
	StaticIntegerSpec(4,1024,default:16), // MUST be at least 4
	\octave.asSpec.as(StaticSpec)
], \freqStream );


