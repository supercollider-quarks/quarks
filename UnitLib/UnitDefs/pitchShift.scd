/*
\pitchShift

A time domain granular pitch shifter. Grains have a triangular amplitude envelope and an overlap of 4:1.

semitones: the number of semitones to shift the pitch with.pitchDisp: the maximum random deviation of the pitch.timeDisp: a random offset from 0 to the number of seconds set with timeDisp that is added to the delay of the grain.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \pitchShift, { |semitones = 0, pitchDisp = 0.0, timeDisp = 0.0|
	var sig;
	URandSeed.ir(12345);
	sig = UIn.ar( 0, 1 );
	sig = PitchShift.ar( sig, \windowSize.ir(0.2), semitones.midiratio, pitchDisp * 4, timeDisp * 0.2 );
	UOut.ar(0, sig );
})	
	.category_( \effect )
	.setSpec( \semitones, [ -24, 24, \lin, 0.01, 0 ].asSpec )
	.setSpec( \windowSize, [0.01,0.5,\exp, 0, 0.2 ].asSpec, \init ) 