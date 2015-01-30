/*
a = MKtl('pcmn0');

a.elementAt(\sl).action_({ |elem| [elem.name, elem.value].postln; });
a.elementAt(\keyOn).action_({ |elem| [\ON, elem.name, elem.value].postln; });
a.elementAt(\keyOff).action_({ |elem| [\OFF, elem.name, elem.value].postln; });
*/
(
device: "APC MINI",
protocol: 'midi',
description: (

'keyOn': [(0..63),(64..71),(82..89),[98]].collect { |xs| xs.collect({ |midinote|  ('midiMsgType': 'noteOn', 'type': 'key', 'midiChan': 0, 'midiNum':  midinote, 'spec': 'midiNote') }) },

'keyOff': [(0..63),(64..71),(82..89),[98]].collect { |xs| xs.collect({ |midinote|  ('midiMsgType': 'noteOff', 'type': 'key', 'midiChan': 0, 'midiNum':  midinote, 'spec': 'midiNote') }) },

\sl: (48..56).collect { |midinote|  ('midiMsgType': 'cc', 'type': 'slider', 'midiChan': 0, 'midiNum':  midinote, 'spec': 'midiCC') }

)
)
