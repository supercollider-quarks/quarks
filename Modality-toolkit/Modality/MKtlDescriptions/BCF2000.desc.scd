// Behringer BCF2000 specs:
/*

pr_u_l
pr_u_r
pr_d_l
pr_d_r
tr[page][row][column]
kn[page][row][column]
bt[row][column]
sl[row][column]


*/


(
device: "BCF2000",
protocol: \midi,
description: (
	//buttons at bottom right
	\pr_u_l: (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 89, \spec: \midiBut, \mode: \toggle, \ioType: \inout),
	\pr_u_r: (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 90, \spec: \midiBut, \mode: \toggle, \ioType: \inout),
	\pr_d_l: (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 91, \spec: \midiBut, \mode: \toggle, \ioType: \inout),
	\pr_d_r: (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 92, \spec: \midiBut, \mode: \toggle, \ioType: \inout),

	//upper row of encoders when pressed
	\tr:4.collect{ |j|
		8.collect{ |i|
			// top knob knpush mode
			(\midiType: \cc, \type: \encoder, \midiChan: 0, \midiNum: 33+(8*j)+i, \spec: \midiCC, \ioType: \in)
		}
	},

	//upper row of encoders
	\kn:4.collect{ |j|
		8.collect{ |i|
			// top knob push mode
			(\midiType: \cc, \type: \encoder, \midiChan: 0, \midiNum: 1+(8*j)+i, \spec: \midiCC, \ioType: \inout)
		}
	},

	//buttons
	\bt:[65,73].collect{ |ccStart|
		8.collect{ |i|
			(\midiType: \cc, \type: \midiBut, \midiChan: 0, \midiNum: ccStart+i, \spec: \midiCC, \ioType: \inout)
		}
	},

	//sliders
	sl:8.collect{ |i|
		(\midiType: \cc, \type: \encoder, \midiChan: 0, \midiNum: 81+i, \spec: \midiCC, \ioType: \inout)
	}
)
)