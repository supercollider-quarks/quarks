/*
\tanhDistort

Distorts the sound via a hyperbolic tangent function.
inGain: input gain (dB).outGain:output gain (dB).

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \tanhDistort, { |inGain = 0, outGain = 0|
	UOut.ar( 0, (UIn.ar( 0 ) * inGain.dbamp).tanh * outGain.dbamp )
} )
	.category_( \distortion )
	.setSpec( \inGain, [0, 60].asSpec )
	.setSpec( \outGain, [-60, 0].asSpec );