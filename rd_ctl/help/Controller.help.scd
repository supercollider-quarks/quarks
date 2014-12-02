// Controller.help.sc - (c) rohan drape, 2004-2007

// Initialise as required, ie. boot
// scsynth & interface engine.

s.boot;
SwingOSC.default.initTree;

// Create a new <Controller> value, and create
// a window to monitor and edit <Ctl> status.
// A <Ctl> interface has three components, a
// NameView, a DialView and a ValueView.

(
c = Controller.new(s, 256) ;
c.makeInterface(rows: 4, columns: 3, offset: 0);
)

// There is a trivial midi initializer to set
// up some standard midi mappings, if it does
// not do what you want you will need to do
// this yourself - if you are not using midi
// skip this...

c.midiInitialize;

// Setup the first <Ctl> with a non-zero depth
// value.  Editing the DialView modifies the
// <Ctl> value.  Pressing the NameView
// increments the <Ctl> state, using the shift
// modifier decrements.  Note that modifying
// the state modifies the value, and also the
// reverse.  The display method prints the
// <Ctl> status.

(
c[0].setup("Frequency", ControlSpec.new(220, 440, ExponentialWarp), 330, 4);
c[0].display;
c[0].addRecv({
	arg index, spec, value, state;
	[index, spec, value, state].postln;});
)

// Make the state modification follow a
// constrained map.  The array is given in
// 'value' space and mapped immediately to
// 'internal' space, subsequent changes to the
// <Ctl> spec will keep the same 'shape'
// stateMap.  Note that the map is only
// consulted when the state is modified,
// changing the value modifies the state in
// the same unit linear manner.

c[0].stateMap = [330, 360];

// The state modification map need not be
// linear.

c[0].stateMap = [220, 221, 400];

// If the state modification map has the same
// number of elements as the depth of the
// <Ctl> the values are located exactly.

c[0].depth = 4;
c[0].stateMap = [220, 230, 240, 420];
c[0].state = 1;
c[0].value;            // => 230
c[0].increment(-2);
c[0].value;            // => 420

// Assigning nil as the map reinstates the
// default linear map.

c[0].stateMap = nil;

// The <Ctl> value can be updated from the
// server.  The ValueView at a <Ctl> interface
// is a button that can be pressed to update
// that value in this manner.

(
s.sendMsg("/c_set", 0, 345);
c[0].update;
c[0].value;
)

// Make an interface with a color scheme.  The
// colorChooser argument should be a procedure
// that returns a <Color> value based on an
// <Integer> index.

(
c.makeInterface(colorChooser: {
	arg index;
	[Color.yellow, Color.white].wrapAt(index);});
)

// Replace the default midi controller map.
// This map makes the following sixteen
// controller arrangement:

//         .---.---.---.---.---.---.---.---.
//         | 0 | 8 | 1 | 9 | 2 | 10| 3 | 11|
//         .---.---.---.---.---.---.---.---.
//         | 4 | 12| 5 | 13| 6 | 14| 7 | 15|
//         .---.---.---.---.---.---.---.---.

(
MIDIIn.control = {
	arg source, channel, number, value;
	var index ;
	index = [(0..7),(8..15)].lace(16).at(number);
	c[index].internal = value / 127.0;
};
)

// Make two Sine oscillators.

(
c[0].setup("Frequency", ControlSpec.new(220, 240, ExponentialWarp), 221);
c[1].setup("Gain", ControlSpec.new(0, 1/5, LinearWarp), 1/7);
c[8].setup("Frequency", ControlSpec.new(220, 240, ExponentialWarp), 229);
c[9].setup("Gain", ControlSpec.new(0, 1/5, LinearWarp), 1/9);
{SinOsc.ar(In.kr([0, 8]), 0, In.kr([1, 9]))}.play
)

// A test instrument.

(
SynthDef.new("ping", {
	arg out = 0, freq = 440, pan = 0, gain = 1;
	Out.ar(out, Pan2.ar(
		SinOsc.ar(freq, 0,
			EnvGen.kr(envelope: (Env.perc), doneAction: 2)),
		pan, gain));}).send(s);
)

// Use <Ctl> values for scheduling nodes
// (pings).

(
c[0].setup("DelayTime", ControlSpec.new(0.05, 0.75, LinearWarp), 0.18);
c[1].setup("FreqScalar", ControlSpec.new(0.5, 2, LinearWarp), 1.75);
c[2].setup("PanLocation", ControlSpec.new(-1, 1, LinearWarp), 0);
c[3].setup("BasicGain", ControlSpec.new(0, 1, LinearWarp), 0.1);
c[4].setup("Scale", ControlSpec.new(0, 1, LinearWarp), 0.0, 3);
Routine.new({
	var d;
	d = [[0, 2, 4, 5, 7, 9, 11],
		[0, 2, 3, 5, 6, 8, 9, 11],
		[0, 2, 4, 6, 8, 10]];
	inf.do({
		arg n;
		var f;
		f = (d[c[4].state].choose + 48 + [-12,0,12].choose).midicps;
		[n,f].postln;
		s.sendMsg("/s_new", "ping", -1, 0, 0,
			"freq", f * c[1].value,
			"pan", c[2].value,
			"gain", c[3].value + 0.25.rand);
		c[0].value.yield;
		c[0].value.postln;
	});
}).play(SystemClock);
)

// Add a language level oscillator to the pan
// <Ctl>.  This runs at the same rate as the
// node instantiation routine, though it is
// not synchronized.  Note that the interface
// reflects changes at the <Ctl>.

Routine.new({inf.do({c[2].value = 2.0.rand - 1.0; c[0].value.yield;})}).play;

// Setup midi NoteOn messages to increment and
// decrement state as if Midi Controllers
// (ie. 7bit signal).

(
c[0].depth = 127;
MIDIIn.noteOn = {
	arg src, chan, num, vel;
	var step;
	if(num==60, {step = -1}, {step = 1});
	c[0].increment(step);
};
)

// A single sawtooth oscillator with NoteOn
// and NoteOff messages setting C values.

(
c[16].setup("Note", ControlSpec.new(0, 127, LinearWarp));
c[17].setup("Gain", ControlSpec.new(0, 0.1, LinearWarp));
MIDIIn.noteOn = {
	arg src, chan, num, vel;
	c[16].internal = num/127.0;
	c[17].internal = vel/127.0;
};
MIDIIn.noteOff = {
	arg src, chan, num, vel;
	c[17].internal = 0.0;
};
{LFSaw.ar(In.kr(16).midicps, 0, In.kr(17))}.play
)

// Find out where the audio input bus is.

s.options.dump;

// Two comb filters, one tuned using a delay
// time, the other a note input.

(
c[0].setup("DelayTime", ControlSpec.new(0.001, 0.18, LinearWarp));
c[1].setup("DecayTime", ControlSpec.new(0.01, 9.0, LinearWarp));
c[2].setup("Gain", ControlSpec.new(0, 1, LinearWarp));
c[3].setup("Note", ControlSpec.new(0, 127, LinearWarp));
c[4].setup("DecayTime", ControlSpec.new(0.01, 9.0, LinearWarp));
c[5].setup("Gain", ControlSpec.new(0, 1, LinearWarp));
{[
 CombC.ar(In.ar(8), 0.2, In.kr(0), In.kr(1), In.kr(2)),
  CombC.ar(In.ar(8), 0.2, 1.0 / (In.kr(3).floor.midicps), In.kr(4), In.kr(5))
];}.play;
)

// Make an interface with non default numbers
// of rows and columns and name each <ctl>
// according to index.

(
n = 36;
c = Controller.new(s, n);
c.makeInterface(6, 6);
n.do({
	arg i;
	c[i].setup("Ctl:" ++ i.asString);
});
)

// Dump information about a <Ctl>

c[0].dump;

// <Controller> implements the copySeries
// method, allowing expressions like.

c[10..13].collect(_.value).sum

// There is a snapshot mechanism that collects
// the current values.

~preset = c.snapshot(0,15);

// Scramble the values just stored...

c[0..15].do(_.internal = 1.0.rand);

// Restore values from snapshot.

c.restore(~preset);

// Non-default interfaces.  Make a trivial
// multi-slider to set <Ctl> values.

(
w = GUI.window.new("c.Sliders", Rect(200 , 200, 200, 200)).front;
s = GUI.multiSliderView.new(w, Rect(0, 0, 200, 200));
s.action = {arg e; c[e.index].value = e.currentvalue;};
)
