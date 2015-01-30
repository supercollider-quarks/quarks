// Sherman Filterbank 2 Descriptionfile
(
device: "Sherman FilterBank 2",
protocol: \midi,
description:

(
// MIDI coming in from the Sherman Filterbank
'tr': [
	('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': 15, 'midiNum':  78, 'spec': 'midiNote'),
	('midiMsgType': 'noteOnOff', 'type': 'button', 'midiChan': 15, 'midiNum':  82, 'spec': 'midiNote')
],

// MIDI being send to the Sherman Filterbank
'cutFil1': ('midiMsgType': 'bend', 'type': 'knob', 'midiChan': 15, 'spec': 'midiBend', 'ioType': 'out'),
'resFil1': ('midiMsgType': 'touch', 'type': 'knob', 'midiChan': 15, 'spec': 'midiTouch', 'ioType': 'out'),
'cutFil2': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 1, 'spec': 'midiCC', 'ioType': 'out'),
'resFil2': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 2, 'spec': 'midiCC', 'ioType': 'out'),
'fmDepth': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 4, 'spec': 'midiCC', 'ioType': 'out'),
'vcaBias': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 7, 'spec': 'midiCC', 'ioType': 'out'),
'amDepth': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 11, 'spec': 'midiCC', 'ioType': 'out'),
'attADSR': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 5, 'spec': 'midiCC', 'ioType': 'out'),
'decADSR': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 16, 'spec': 'midiCC', 'ioType': 'out'),
'relADSR': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 17, 'spec': 'midiCC', 'ioType': 'out'),
'attAR': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 18, 'spec': 'midiCC', 'ioType': 'out'),
'relAR': ('midiMsgType': 'cc', 'type': 'knob', 'midiChan': 15, 'midiNum': 19, 'spec': 'midiCC', 'ioType': 'out'),
);
);
