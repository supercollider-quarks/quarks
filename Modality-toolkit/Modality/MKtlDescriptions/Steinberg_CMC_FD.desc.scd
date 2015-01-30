/*
todo
*/
(
device:"Steinberg CMC-FD",
protocol: \midi,
description:
(

// ------ noteOnOff -------------

// the buttons
'bankLeft': ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  46, 'spec': 'midiNote'),
'bankRight': ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  47, 'spec': 'midiNote'),
'chanLeft': ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  48, 'spec': 'midiNote'),
'chanRight': ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  49, 'spec': 'midiNote'),

'shift': ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  70, 'spec': 'midiNote'),

// fader touched or not
'faderTouch': (104..107).collect { |noteNum| ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  noteNum, 'spec': 'midiNote') },

// fader touched when shift is down sends channel mute
'faderMute': (8..11).collect { |noteNum| ('midiMsgType': 'noteOnOff', 'type': 'key', 'midiChan': 0, 'midiNum':  noteNum, 'spec': 'midiNote') },

// ------- bend ------------
// high res fader values (10 bit )
'fader': (0..3).collect { |chan| ('midiMsgType': 'bend', 'type': 'bender', 'midiChan': 0, 'midiNum':  0,'spec': 'midiBend') }
);
)