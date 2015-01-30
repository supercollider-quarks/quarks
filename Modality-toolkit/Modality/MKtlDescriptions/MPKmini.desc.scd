// AKAI professional MPKmini Laptop Production Controller specs
// This desc file is made for the Factory Preset: Preset3-MPC,
// downloadable from "http://www.akaipro.com/product/mpkmini#downloads"
// made by Joost van Dalen

(
device: "MPK mini",
protocol: \midi,
infoMessage: "\n\t MPK mini: This desc file is made for the Factory Preset: Preset3-MPC,
\t downloadable from http://www.akaipro.com/product/mpkmini#downloads \n",
description: (
	\kn: (13..20).collect{ |midiNum|
		(\midiMsgType: \cc, \type: \knob, \midiChan: 0, \midiNum: midiNum, \spec: \midiCC)
	},
	\padOn: #[[37,36,42,82,40,38,46,44],[48,47,45,43,49,55,51,53]].collect({ |bank|
		bank.collect{ |note|
			(\midiMsgType: \noteOn, \type: \pad, \midiChan: 9, \midiNum:  note, \spec: \midiNote)
	}}),
	\padOff: #[[37,36,42,82,40,38,46,44],[48,47,45,43,49,55,51,53]].collect({ |bank|
		bank.collect{ |note|
			(\midiMsgType: \noteOff, \type: \pad, \midiChan: 9, \midiNum:  note, \spec: \midiNote)
	}}),

	\padCC: [(1..9),(10..17)].collect({ |bank|
		bank.collect{ |cc|
			(\midiMsgType: \cc, \type: \pad, \midiChan: 9, \midiNum:  cc, \spec: \midiCC)
	}}),

	\bt: (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum:  64, \spec: \midiCC),

	\keyOn: (0..120).collect{ |note|
		(\midiMsgType: \noteOn, \type: \key, \midiChan: 0, \midiNum:  note, \spec: \midiNote)
	},

	\keyOff: (0..120).collect{ |note|
		(\midiMsgType: \noteOff, \type: \key, \midiChan: 0, \midiNum:  note, \spec: \midiNote)
	}
)
)
