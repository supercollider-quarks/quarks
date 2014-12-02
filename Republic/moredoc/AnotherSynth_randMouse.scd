	// a SynthDef that has mouse controlled modulation;
	// will sound different based on mousepos at server!
(
SynthDef(\xxy, {|out, sustain = 1, freq = 440|
	var env, son;
	env = Line.ar(1, 0, sustain, doneAction: 2);
	son = SinOsc.ar(
		freq * [1, 1.2, 1.5, 1.7] + (env % (1 - env *0.1) * 1000), 
		SinOsc.ar(40) * MouseX.kr(0, 40) * (1 - env)
	).sum;
	Out.ar(out, son * env * 0.1);
}).share;
)

	// repeat it 
(
Tdef(\x, { 
	loop { 
		0.5.wait; 
		(instrument: \xxy, freq: 600, server: r.servers.choose).play
	}
}).play
)

	// get a mouse value from sending machine, 
	// and use it to control freq - other mousecontrol
	// is still measured at receiving server
(
Tdef(\x, { 
	var getmouse = GetMouseX(200, 10000, \exponential, server: r.servers[\adc]);
	loop { 
		
		0.5.wait; 
		(instrument: \xxy, freq: getmouse.value.postln, server: r.servers.choose).play
	}
}).play
)

	// make sound modulation more drastic
	// can do this while task runs!
(
SynthDef(\xxy, {|out, sustain = 1, freq = 440|
	var env, son;
	env = Line.ar(1, 0, sustain, doneAction: 2);
	son = SinOsc.ar(freq * [1, 1.2, 1.5, 1.7] + (env % (1 - env *0.1) * 1000), SinOsc.ar(40) * 	ExpRand(1, 100) * (1 - env)).sum;
	Out.ar(out, son * env * 0.1);
}).share;
)


