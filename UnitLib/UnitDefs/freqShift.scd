/*
\freqShift

freqShift implements single sideband amplitude modulation, also known as frequency shifting, but not to be confused with pitch shifting. Frequency shifting moves all the components of a signal by a fixed amount but does not preserve the original harmonic relationships.

freq: the amount of shifting (Hz)
phase: phase of the freq shift (radians)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \freqShift, { |freq = 0, phase = 0|
	var sig;
	sig = UIn.ar( 0, 1 );
	sig = FreqShift.ar( sig, freq, phase );
	UMixOut.ar(0, sig );
})	
	.category_( \effect )
	.setSpec( \freq, [-22050,22050,\lin,0,0].asSpec )
	.setSpec( \phase, AngleSpec() );