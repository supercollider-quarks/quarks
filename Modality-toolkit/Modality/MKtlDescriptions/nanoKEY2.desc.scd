(
device: "nanoKEY2",
protocol: 'midi',
description: (
	'keyOn':
	(0..120).collect{ arg i;
		('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote')
	},
	'keyOff':
	(0..120).collect{ arg i;
		('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 0, 'midiNum':  i, 'spec': 'midiNote')
	},
	// ------ cc -------------
	'mod': ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':  1,'spec': 'midiCC'),
	'sustain': ('midiMsgType': 'cc', 'type': 'button', 'midiChan': 0, 'midiNum':  64,'spec': 'midiCC'),

	// ------- bend ------------ these are actually two buttons!
	'pitch': ('midiMsgType': 'bend', 'type': 'bender', 'midiChan': 0, 'midiNum':  0,'spec': 'midiBend'),
)
);
