/*
\moogVCF

A Robert Moog style low-pass filter, modulated with an LFO
freq: cutoff frequencies between which the modulation takes place.modSpeed: speed of modulation (cycles per second).modShape: shape of LFO; 0: square wave, 1: sine wave.res: resonance of the filter (0-1)
--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef('moogVCF', { |freq #[ 20, 20000 ], modSpeed = 1, modShape = 1, res = 0.5|
	var in;
	in = UIn.ar(0,1);
	freq = freq.clip(20,20000);
	in = MoogVCF.ar( in, SinOsc.ar( modSpeed ).pow( modShape ).exprange( *freq ), res );
	UOut.ar( 0, in );
}, [ 
	ArgSpec('freq', [ 20.0, 20000.0 ], 
		RangeSpec(20, 20000, 0, inf, 'exp', 0, [20.0,20000.0 ], " Hz") 
	), 
	ArgSpec( 'modSpeed', 1, [0.1, 20, 'exp', 0, 1].asSpec ),
	ArgSpec( 'modShape', 1, [0, 1, 'lin', 0, 1].asSpec ),
	ArgSpec('res', 0.5, [0, 1, 'linear', 0, 0.1].asSpec )
], 'filter')