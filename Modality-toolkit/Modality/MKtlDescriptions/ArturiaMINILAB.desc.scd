/*
MKtl.find;

k = MIDIMKtl('rtrm0');

Ndef(\test, { |amp = 0.2, cutoff = 1000, reso = 5|
	RLPF.ar(Saw.ar(100, amp ! 2), cutoff, 1/reso);
}).play;
Ndef(\test).addSpec(\cutoff, [800, 10000, \exp]);
Ndef(\test).addSpec(\reso, [0.5, 100, \exp]);

k.elements[\kn][0][0].action = { |el| Ndef(\test).setUni(\amp, el.value.postln) };
k.elements[\kn][0][1].action = { |el| Ndef(\test).setUni(\cutoff, el.value.postln) };
k.elements[\kn][0][2].action = { |el| Ndef(\test).setUni(\reso, el.value.postln) };
*/

(
device: "Arturia MINILAB",
protocol: \midi,
infoMessage: "\n\tArturia minilab, made for absolute knob mode for now.\n",
description: (
// ------ noteOn -------------
	keyOn: (36..72).collect { |i|
		('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote');
	},
	keyOff: (36..72).collect { |i|
		('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote');
	},
	padOn: (36..43).collect { |i|
		('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote');
	},
	padOff: (36..43).collect { |i|
		('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote');
	},
	// pads bank 2: (22..29)

// ------ cc -------------
	kn: [
		[7, 74, 71, 76, 77, 93, 73, 75],
		[114, 18, 19, 16, 17, 91, 79, 72]
	].collect { |line|

		line.collect { |ccnum|
			('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  ccnum,'spec': 'midiCC')
		}
	},
	mod: 	('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  1,'spec': 'midiCC'),

// ------- bend ------------
'bend': ('midiMsgType': 'bend', 'type': 'bender', 'midiChan': 0, 'midiNum':  0,'spec': 'midiBend'),
 )
)
