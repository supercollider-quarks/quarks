/*
\gain

A simple gain stage. Can set a gain in dB and inverse phase.

gain: added gain (dB)
inverse: inverse phase (false/true) 
numChannels: number of channels

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \gain, { |gain = 0, inverse = 0|
	var in;
	in = UIn.ar(0, Udef.numChannels );
	in = in * gain.dbamp * inverse.linlin(0,1,1,-1);
	UMixOut.ar( 0, in );
})
	.category_( \utility )
	.setSpec( \gain, [-96,24,\lin,0,0].asSpec )
	.setSpec( \inverse, BoolSpec(false) );