/*
\leakDC

Removes DC offset from signal.

cutFreq: frequencies below the cutFreq (Hz) will be reduced or removed. A lower cutFreq will make the DC offset removal less effective, a higher cutFreq will result in better offset removal but also in audible loss of bass frequencies in the sound

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \leakDC, { |cutFreq = 20|
	var in;
	in = UIn.ar(0,1);
	cutFreq = cutFreq.clip(1,200);
	in = LeakDC.ar( in, (-2pi * (cutFreq/SampleRate.ir)).exp );
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \cutFreq, [1,200,\exp,1,20].asSpec);