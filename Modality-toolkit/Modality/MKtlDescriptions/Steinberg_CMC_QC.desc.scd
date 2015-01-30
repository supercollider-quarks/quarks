(
device:"Steinberg CMC-FD",
protocol: \midi,
description:
(

// ------ noteOnOff -------------

// buttons by labels on them:

'butQ': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  42, 'spec': 'midiNote'),
'butEQ': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  44, 'spec': 'midiNote'),

'chanLeft': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  48, 'spec': 'midiNote'),
'chanRight': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  49, 'spec': 'midiNote'),

'butLearn': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  50, 'spec': 'midiNote'),

'butF1': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  54, 'spec': 'midiNote'),
'butF2': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  55, 'spec': 'midiNote'),
'butF3': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  56, 'spec': 'midiNote'),
'butF4': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  57, 'spec': 'midiNote'),

'shift': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  70, 'spec': 'midiNote'),
'butW': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  78, 'spec': 'midiNote'),
'butR': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  81, 'spec': 'midiNote'),
'butMIDI': ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  116, 'spec': 'midiNote'),

// ------ cc -------------
//
'kn': (0..7).collect { |chan| ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': chan, 'midiNum':  74,'spec': 'midiCC') },

'knRel': (16..23).collect { |ccnum| ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  ccnum,'spec': 'midiCC') }
)
);