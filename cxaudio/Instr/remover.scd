
// removes (filters away) 

// drive by amplitude : a crossfade  of slew limited vs. original
Instr("remover.bySlew",{ arg audio=0.0,sensitivity=0.0,slope=500;

	var slewed,mix,add;
	
	slewed = Slew.ar(audio,slope,slope);
	add = sensitivity.round(1.0).neg;  // 1 0 -1
	mix = Amplitude.kr(audio,mul: (sensitivity * 30),add: add);
	XFader.ar(audio,slewed,mix.max(-1).min(1))

},#[
	\audio,
	[-1,1],
	[10,12000,\exponential]

],
	\audio
);


Instr("remover.bySlew2",{ arg audio=0.0,sensitivity=0.0,slope=500;

	var slewed,sense;
	
	sense = Amplitude.kr(audio,mul: sensitivity) * slope;
	slewed = Slew.ar(audio,slope,slope);

},#[
	\audio,
	[10,100],
	[10,12000,\exponential]

],
	\audio
);



