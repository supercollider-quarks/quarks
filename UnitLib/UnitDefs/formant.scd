/*
\formant

A formant generator. Generates a set of harmonics around a formant frequency at a given fundamental frequency.

freq: frequency (Hz)
fromant: frequency of the formant (Hz)
bw: bandwidth of the formant as multiplier of the freq
amp: amplitude (0-1)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \formant, { |freq = 440, formant = 1760, bw = 2, amp = 0.1|
	var sig;
	sig = Formant.ar( freq, formant, (bw * freq), amp );
	UMixOut.ar( 0, sig, 0, true ) 
} ).category_( \oscillator )
	.setSpec( \freq, FreqSpec( 2, 20000 ) )
	.setSpec( \formant, FreqSpec( 2, 20000 ) )
	.setSpec( \bw, [1, 20, \exp ].asSpec )
	.setSpec( \lag, [0,1,\lin].asSpec );