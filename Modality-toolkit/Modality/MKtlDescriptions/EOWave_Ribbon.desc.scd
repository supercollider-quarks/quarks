(
device: "The Ribbon",
protocol: \midi,
description: (
	// ribbon
	// [ noteOn, val, 127, num, 78, chan, 0, src, -1477523613 ]
	// [ noteOff, val, 0, num, 78, chan, 0, src, -1477523613 ]
	ribbon: (
		\touch: (
			on:  (36..82).collect{|num|
				(\midiMsgType:  \noteOn, \type: \slider, \midiChan: 0, \midiNum: num, \spec: \midiBut)
			},
			off:  (36..82).collect{|num|
				 (\midiMsgType: \noteOff, \type: \slider, \midiChan: 0, \midiNum: num, \spec: \midiBut)
			}
		),
		\bend: (\midiMsgType: \bend, \type: \slider, \midiChan: 0, \midiNum: 0, \spec: \midiBend),

		// [ cc, val, 82, num, 7, chan, 0, src, -1477523613 ]
		\pressure: (\midiMsgType: \cc, \type: \slider, \midiChan: 0, \midiNum: 7, \spec: \midiCC)
	),
		// expressionPad
// [ cc, val, 0, num, 1, chan, 0, src, -1477523613 ]
	\pad: (\midiMsgType: \cc, \type: \pad, \midiChan: 0, \midiNum: 1, \spec: \midiCC)
)
)


