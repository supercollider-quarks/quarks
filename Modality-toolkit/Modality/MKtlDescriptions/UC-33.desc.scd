(
device: "UC-33 USB MIDI Controller",
protocol: \midi,
// preset 3
description: (
	sl: #[105, 106, 107, 108, 110, 111, 112, 116, 26].collect {|num, i|
		(\midiType: \cc, \type: \slider, \midiChan: 0, \midiNum: num, \spec: \midiCC, \ioType: \in)
	},

	kn: #[[45, 46, 47, 5, 80, 81, 82, 83], [50, 51, 55, 87, 75, 76, 77, 78], [20, 21, 40, 43, 70, 71, 72, 73]].collect{|row| row.collect {|num, i|
		(\midiType: \cc, \type: \knob, \midiChan: 0, \midiNum: num, \spec: \midiCC, \ioType: \in)
	}},
	bt: #[57, 52, 53, 54, 41, 42, 44, 22, 23, 24].collect {|num, i|
		(\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: num, \spec: \midiBut, \mode: \toggle, \ioType: \in)
	}

)
)