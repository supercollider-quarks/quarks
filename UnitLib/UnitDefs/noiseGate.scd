/*
\noiseGate

A noise gate.

thresh: threshold in dB
att: attack time
hold: hold time
rel: release time
hysteresis: hysteresis applied to threshold (0-1). A hysteresis value 1 (maximum) will make the gate never close agian after opening.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \noiseGate, { |thresh = -10, att = 0.01, hold = 0.0, rel = 0.5, hysteresis = 0.0|
	var in, sig, test;
	in = UIn.ar(0,1);
	thresh = thresh.dbamp;
	sig = Schmidt.ar( Amplitude.ar( in ), thresh * (1 - hysteresis), thresh );
	sig = Slew.ar( sig, inf, 1/hold ) > 0;
	sig = LagUD.ar( sig, att, rel );
	UOut.ar( 0, in * sig );
})
	.category_( \dynamics )
	.setSpec( \att, [0,1,4,0,0.01].asSpec )
	.setSpec( \rel, [0,1,4,0,0.5].asSpec )
	.setSpec( \thresh, [ -90, 0, \lin, 1, -10].asSpec );