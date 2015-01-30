/*
TODO:

QuNexus has four modes:
	Preset A uses just noteOn/Off and channel bend
	Preset B adds channel pressure
	Preset C allocates notes to 10 rotating midi chans,
		and sends poly bend and pressure mod (cc1) on those chans
	Preset D is for drums (uses chan 10) and clips


// mini code example for global note resp + voicer:
z = MIDIMKtl('qnxs0');
z.verbose = true;
z.verbose = false;
// make an NPVoicer, use with \default synthdef:
s.boot; s.latency = nil;
v = NPVoicer(Ndef(\piano)).play;
v.prime(\default);

// add a global noteOn function
z.global.put(\noteOn, MFunc([ \debug, { |...args| [\noteOnGlob, args].postln }]));
z.global[\noteOn].add(\note1, { |chan, note, vel|
	v.put(note, [\freq, note.midicps, \amp, (vel/127) ].postln);
});
// and a global noteOff function
z.global[\noteOff] = MFunc();
z.global[\noteOff].add(\note1, { |chan, note| v.release(note); });


Preset C, poly bend and pressure:
Rather than using multiple bend and cc elements for each channel,
use global responders, and use channel as info for an NPVoicer.

// same as above, switch to Preset C
// works in principle,
// but better write a synthdef that has nice responses.

q = q ? (); q.chanNotes =  q.chanNotes ? ();

z.global[\noteOn].add(\note1, { |chan, note, vel|
	q.chanNotes.put(chan, note);
	v.put(chan, [\freq, note.midicps, \amp, (vel/127) ].postln);
});
z.global[\noteOff].add(\note1, { |chan, note|
	v.release(chan);
	q.chanNotes.removeAt(chan);
});

z.global[\cc] = MFunc();
z.global[\cc].add(\mod, { |chan, ccnum, val|
	if (ccnum == 1) { v.setAt(chan, \amp, val/127) };
});
z.global[\bend] = MFunc();
z.global[\bend].add(\pitch, { |chan, val|
	var bend = (val - 8192).linlin(-1500, 1500, -2, 2).postln;
	v.setAt(chan, \freq, (q.chanNotes[chan] + bend).postln.midicps.postln);
});

*/



(
device: "QuNexus",
protocol: \midi,
description: (

	// Preset A mode has these:
// ------ noteOn -------------

	keyOn: (24..108).collect { |midinote|
	('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  midinote, 'spec': 'midiNote') },
	// ---------noteOff ----------
	keyOff: (24..108).collect { |midinote|
		('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 0, 'midiNum':  midinote, 'spec': 'midiNote')
	},

// ------- bend ------------
	'bend': ('midiMsgType': 'bend', 'type': 'bender', 'midiChan': 0, 'midiNum':  0,'spec': 'midiBend'),

// Preset B mode adds single channel pressure from all keys :

// ------- cc ------------
'monotouch': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  1,'spec': 'midiCC'),
)

// Preset C mode adds Poly Pitch Bend on up to 10 channels rotating,
// plus pressure on cc1 (also rotating?)

// Preset D mode for drums and clips

);
