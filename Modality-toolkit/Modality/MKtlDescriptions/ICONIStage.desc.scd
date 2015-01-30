// ICON I-Stage specs:
// Mostly NoteOn / NoteOff, some cc messages,
// hardware scenes: DJ, Effector and Drummer seems to work, flattened out with underscores.

(
device: "iCON iStage V1.00",
protocol: \midi,

description: (
	\cf: (\midiType: \cc, \type: \xfade,   \midiChan: 0, \midiNum: 7, \spec: \midiCC),
	\bt: (0..19).collect{|num| (0..11).collect{|chan|
		(\midiType: \noteOnOff, \type: \button, \midiChan: chan, \midiNote: num, spec: \midiBut, \mode: \push)
}},
	\play: (0..19).collect{|num|
		(\midiType: \noteOnOff, \type: \button, \midiChan: 12, \midiNote: num, spec: \midiBut, \mode: \push)
	},
	\stop: (0..12).collect{|chan|
		(\midiType: \note, \type: \button, \midiChan: chan, \midiNote: 103, spec: \midiBut, \mode: \push)
	},
	// drummer
	bluL: (60..75).collect{|num|
		(\midiType: \note, \type: \button, \midiChan: 9, \midiNote: num, spec: \midiVel, \mode: \push)
	},
// duplication in here (42)!
	red: #[35, 48, 37, 36, 49, 47, 40, 38, 50, 45, 42, 42, 51, 43, 46, 44].collect{|num, i|
		(\midiType: \note, \type: \button, \midiChan: 9, \midiNote: num, spec: \midiVel, \mode: \push)
	}
/*++
// left blue is duplicate of red
#[35, 48, 37, 36, 49, 47, 40, 38, 50, 45, 42, 42, 51, 43, 46, 44].collect{|num, i|
	[("red" ++ i.asStringToBase(10, 2)).asSymbol, (\midiType: \note, \type: \button, \midiChan: 9, \midiNote: num, spec: \midiBut, \mode: \push)]
}.flat
*/

)
)