// M-audio Trigger finger - MKtl

(
device: "USB Trigger Finger",
protocol: \midi,
description: (
	pad: ((0..15)+35).collect{ |num|
		( type: \pad, spec: \midiVel, \midiMsgType: \note, \midiChan: 0, \midiNum: num)
	},
	sl: #[7, 1, 71, 74].collect{ |num|
		( type: \slider, spec: \midiCC, \midiMsgType: \cc, \midiChan: 0, \midiNum: num )
	},
	kn: #[10, 91, 12, 93, 5, 73, 84, 72].collect{ |num|
		( type: \knob, spec: \midiCC, \midiMsgType: \cc, \midiChan: 0, \midiNum: num )
	}
)
)

// pads * 16
// [35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51]

// sliders * 4
// [7, 1, 71, 74]

// knobs * 8
// [10, 91, 12, 93, 5, 73, 84, 72]

