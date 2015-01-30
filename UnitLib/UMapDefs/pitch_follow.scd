/*
\pitch_follow

Creates an UMap that listens to an audio bus from a previous unit in the chain, and follow it's pitch.

bus: the number of the bus (usually 0)
ampTreshold: an amplitude threshold above which pitch detection is performed
median: number of frames of built-in median filter
pitchRange: the range of pitches to detect (will never go out of range)
toRange: the range to which the pitchRange is scaled
invert: if true, the toRange is inverted (i.e. low values become high and v.v.)
curve: a curvature value for toRange. If curve == 0, the range is linear.

The 'toRange' arg range is mapped to that of the parameter to which the UMap is connected

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \pitch_follow, { |bus = 0,
	ampThreshold = 0.02, median = 7,
	pitchRange = #[60, 4000], toRange = #[0.0,1.0], 
	invert = 0, curve = 0|
	var sig, unranged;
	sig = In.ar(bus + UIn.firstPrivateBus);
	sig = Pitch.kr( sig, 440, pitchRange[0], pitchRange[1], 
		ampThreshold: ampThreshold, median: median 
	)[0];
	sig = sig.explin(*pitchRange ++ [0,1,\none]);
	sig = if( invert, 1-sig, sig );
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(0,1,0,1, curve ),
		sig
	]);
	sig = sig.linlin(0,1,*toRange ++ [ \none ]);
	UMapOut.kr(sig);
})
	.setSpec( \ampThreshold, \amp.asSpec )
	.setSpec( \median, [1,31,\lin,1,7].asSpec )
	.setSpec( \pitchRange, FreqSpec().asRangeSpec )
	.setSpec( \invert, BoolSpec(false) )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \bus, PositiveIntegerSpec(0,0,32) )
	.mappedArgs_([ \toRange ])
	.category_(\input)

