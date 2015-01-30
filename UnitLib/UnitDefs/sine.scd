/*
\sine

A sine wave oscillator.
http://en.wikipedia.org/wiki/Sine_wave

freq: frequency (Hz)
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \sine, { |freq = 440, amp = 0.1|
	UMixOut.ar( 0, SinOsc.ar( freq, 0, amp ), 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpecMode( \amp, \normal );