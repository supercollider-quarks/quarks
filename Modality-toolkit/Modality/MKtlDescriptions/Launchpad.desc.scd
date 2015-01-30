// Novation Launchpad specs:
// Mostly NoteOn / NoteOff, some cc messages,
// no hardware scenes, flattened out with underscores

(
device: "Launchpad",
protocol: \midi,
description: (
	\up:      (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 104, \spec: \midiBut, \mode: \push),
	\down:    (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 105, \spec: \midiBut, \mode: \push),
	\left:    (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 106, \spec: \midiBut, \mode: \push),
	\right:   (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 107, \spec: \midiBut, \mode: \push),
	\session: (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 108, \spec: \midiBut, \mode: \push),
	\user1:   (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 109, \spec: \midiBut, \mode: \push),
	\user2:   (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 110, \spec: \midiBut, \mode: \push),
	\mixer:   (\midiType: \cc, \type: \button, \midiChan: 0, \midiNum: 111, \spec: \midiBut, \mode: \push),

	bt: ((0, 16 .. 112) +.t (0..7)).flat.collect{|num, i|
		(\midiType: \note, \type: \button, \midiChan: 0, \midiNote: num, \spec: \midiBut, \mode: \push)
	},
	ar: ((0, 16 .. 112) + 8).flat.collect{|num, i|
		(\midiType: \note, \type: \button, \midiChan: 0, \midiNote: num, \spec: \midiBut, \mode: \push)
	}
)
)
