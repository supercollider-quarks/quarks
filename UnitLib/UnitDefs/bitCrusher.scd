/*
\bitCrusher

Introduces quantization noise to a signal by downsampling and bit-rate rounding

rate: fraction of the original sampling rate. If fs == 44100; rate == 0.25 means fs becomes 11025
bits: virtual number of bits (1-24), can be fractional

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \bitCrusher, { |rate = 1.0, bits = 24|
	var sig;
	sig = UIn.ar( 0 );
	sig = Gate.ar( sig, Impulse.ar( rate * SampleRate.ir ) );
	sig = sig.linlin(-1,1,0,1,\none).round( 2** (bits-1).neg ).linlin(0,1,-1,1,\none);
	UOut.ar( 0, sig )
} )
	.category_( \distortion )
	.setSpec( \rate, [0, 1, 2, 0, 1].asSpec )
	.setSpec( \bits, [1,24,\lin,0,24].asSpec );