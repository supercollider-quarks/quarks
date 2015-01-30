///// nanoPAD2 desc file

/*
MIDIMKtl.find;
k = MIDIMKtl('nnpd20');
*/

(
device: "nanoPAD2",
protocol: \midi,
description: (
	\padOn: (36..99).collect{ |note|
		(\midiMsgType: \noteOn, \type: \pad, \midiChan: 0, \midiNum:  note, \spec: \midiVel)
	},
	\padOff: (36..99).collect{ |note|
		(\midiMsgType: \noteOff, \type: \pad, \midiChan: 0, \midiNum:  note, \spec: \midiVel)
	},
	'touchX': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  1,'spec': 'midiCC'),
	'touchY': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  2,'spec': 'midiCC'),
	'touchInUse': ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  16,'spec': 'midiCC'),
)
)