/*
\bandPass

A band-pass filter

freq: center frequency.
bw: the bandwidth in octaves between -3 dB frequencies.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \bandPass, { |freq = 440, bw = 1|
	var in;
	in = UIn.ar(0,1);
	in = BBandPass.ar( in, freq.clip(20,20000), bw );
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \freq, \freq.asSpec )
	.setSpec( \bw, [ 0.01, 10, \exp, 0, 1 ].asSpec )