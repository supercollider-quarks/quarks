/*
\impulse

A non-bandlimited impulse generator. Outputs single sample impulses.

freq: frequency (Hz)
scale: a multiplier for the frequency (if scale is 0, only a single impulse is produced)
numSamples: width of the impulse in number of samples (1-100)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \impulse, { |freq = 440, scale = 1, numSamples = 1, amp = 0.1|
	var sig;
	sig = Impulse.ar( freq * scale ) * amp;
	sig = Trig1.ar( sig, numSamples / SampleRate.ir );
	UMixOut.ar( 0, sig, 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpec( \scale, [0,2,\lin,0.125,1].asSpec )
	.setSpec( \numSamples, [1,100,\lin,1,1].asSpec );