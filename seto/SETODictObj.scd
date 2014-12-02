// SETODictObj is an  object where you can set custom functions for each classID:
SETODictObj.setAction(1, 
	{|me| "%: Hello, I'm a first class object\n".postf(me)}
);

// It is also possible to set the same function for a range of classIDs
SETODictObj.setAction([23, 42], 
	{|me| "%: I'm related to an important number\n".postf(me)}
);


// If you want to track an object and also do something when it gets invisible, simply set the releaseFunction as well
SETODictObj.setAction(0, 
	{|me| "%: Here!\n".postf(me)},
	{|me| "%: \tGone!\n".postf(me)}
);

// By overwriting a function you can change behavior.
// Note that you always have to define both action and releaseAction.
// Accessing object parameters is easy, simply call their name:
SETODictObj.setAction(0, 
	{|me| "%: Here!\n\t%\n".postf(me, me.pos)},
	{|me| "%: \tGone!\n".postf(me)}
);


a = SETO_OSCServer('2Dobj', nil, SETODictObj)
a.start
a.stop


// if you're curious where your objects are, have a look at them:

SETOgui(a)



s.boot

SynthDef(\whoa, {|freq = 100|
	Out.ar(0, SinOsc.ar(freq)!2 * EnvGen.ar(Env([0, 1, 0], [0.11, 0.01]), levelScale: 0.051, doneAction: 2))}
).add;

SynthDef(\wuha, {|freq = 100|
	Out.ar(0, LPF.ar(Pulse.ar(freq))!2 * EnvGen.ar(Env([0, 1, 0], [0.11, 0.01]), levelScale: 0.051, doneAction: 2))}
).add;

Synth.grain(\whoa)


SETODictObj.setAction(0, {|me|
	var freq = me.pos.first.linexp(0, 1, 100, 500);
	
	
	Synth.grain(\whoa, [0, freq])
	
})

SETODictObj.setAction(9, {|me|
	var freq = me.pos.first.linexp(0, 1, 200, 1000);
	Synth.grain(\wuha, [0, freq])
})


SETObject