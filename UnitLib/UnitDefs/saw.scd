/*
\saw

A band-limited sawtooth wave generator.
http://en.wikipedia.org/wiki/Sawtooth_wave

freq: frequency (Hz)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \saw, { |freq = 440, amp = 0.1|
	UMixOut.ar( 0, Saw.ar( freq, amp ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) );