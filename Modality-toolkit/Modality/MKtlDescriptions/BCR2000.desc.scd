// Behringer BCR2000 specs:
/*

pr_u_l
pr_u_r
pr_d_l
pr_d_r
tr[page][row][column]
knUp[page][row][column]
bt[row][column]
kn[row][column]
MIDIMKtl('bcr20000').explore
MIDIMKtl('bcr20000').verbose = true
MIDIMKtl('bcr20000').verbose = false
MIDIMKtl('bcr20000').elements.kn[0][0].value_(0.5)

*/


(
device: "BCR2000",
protocol: \midi,
description: (
	//buttons at bottom right
	\pr: [
		[
			(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 105, \spec: \midiBut, \mode: \toggle, \ioType: \inout),
			(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 106, \spec: \midiBut, \mode: \toggle, \ioType: \inout)
		],
		[
			(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 107, \spec: \midiBut, \mode: \toggle, \ioType: \inout),
			(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 108, \spec: \midiBut, \mode: \toggle, \ioType: \inout)
		]
	],

	//upper row of encoders when pressed
	\tr:4.collect{ |j|
		8.collect{ |i|
			// top knob push mode
			(\midiMsgType: \cc, \type: \encoder, \midiChan: 0, \midiNum: 33+(8*j)+i, \spec: \midiCC, \ioType: \in)
		}
	},

	//upper row of encoders
	\knUp:4.collect{ |j|
		8.collect{ |i|
			// top knob push mode
			(\midiMsgType: \cc, \type: \encoder, \midiChan: 0, \midiNum: 1+(8*j)+i, \spec: \midiCC, \ioType: \inout)
		}
	},

	//buttons
	\bt:[65,73].collect{ |ccStart|
		8.collect{ |i|
			(\midiMsgType: \cc, \type: \midiBut, \midiChan: 0, \midiNum: ccStart+i, \spec: \midiCC, \ioType: \inout)
		}
	},

	//other encoders
	\kn:[81,89,97].collect{ |ccStart|
		8.collect{ |i|
			(\midiMsgType: \cc, \type: \encoder, \midiChan: 0, \midiNum: ccStart+i, \spec: \midiCC, \ioType: \inout)
		}
	}
)
)