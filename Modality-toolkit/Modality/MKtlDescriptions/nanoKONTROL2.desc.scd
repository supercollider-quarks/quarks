//	// KORG nanoKONTROL2 specs:
//	// only cc messages,
(
device: "nanoKONTROL2",
protocol: \midi,
description: [
	\rew -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 43, \spec: \midiBut, \mode: \push),
	\fwd -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 44, \spec: \midiBut, \mode: \push),
	\stop -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 42, \spec: \midiBut, \mode: \push),
	\play -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 41, \spec: \midiBut, \mode: \push),
	\rec -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 45, \spec: \midiBut, \mode: \push),
	\cycle -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 46, \spec: \midiBut, \mode: \push),
	\mset -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 60, \spec: \midiBut, \mode: \push),
	\mleft -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 61, \spec: \midiBut, \mode: \push),
	\mright -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 62, \spec: \midiBut, \mode: \push),
	\tleft -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 58, \spec: \midiBut, \mode: \push),
	\tright -> (\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 59, \spec: \midiBut, \mode: \push),

	// scene:
	\sl -> (0..7).collect{ |i|
		(\midiMsgType: \cc, \type: \slider, \midiChan: 0, \midiNum: i, \spec: \midiCC)
	},

	\kn -> (16..23).collect{ |i|
		(\midiMsgType: \cc, \type: \knob, \midiChan: 0, \midiNum: i, \spec: \midiCC)
	},

	\bt -> [(32..39),(48..55),(64..71)].collect{ |xs|
		xs.collect{ |i|
			(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum:  i, \spec: \midiCC)
		}
	}
]
)