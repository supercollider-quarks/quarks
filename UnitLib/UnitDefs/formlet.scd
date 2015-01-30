/*
\formlet

An FOF-like formant filter.

freq: frequency.
attackTime: attack time in seconds
decayTime: decay time in seconds
dry: level of unprocessed sound

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \formlet, { |freq = 440, attackTime = 0.05, decayTime = 0.1, dry = 0|
	var in;
	in = UIn.ar(0,1);
	in = Formlet.ar( in, freq.clip(20,20000), attackTime, decayTime ) + (in * dry);
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \freq, FreqSpec( 20, 20000 ))
	.setSpec( \attackTime, [ 0.01, 1, \exp, 0, 0.1 ].asSpec )
	.setSpec( \decayTime, [ 0.01, 1, \exp, 0, 0.1 ].asSpec )
	.setSpec( \dry, \amp.asSpec );