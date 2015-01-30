//	// Teenage Engineering OP-1
//	// cc messages from four knobs,
// and lots of buttons (mostly with icons)

(
device: "OP-1 Midi Device",
protocol: \midi,
description: (
	\key: (
		\on: (5..124).collect{ |num|
			(\midiMsgType: \noteOn , \type: \key, \midiChan: 0, \midiNum: num, \spec: \midiNote)
		},
		\off: (5..124).collect{ |num|
			(\midiMsgType: \noteOff, \type: \key, \midiChan: 0, \midiNum: num, \spec: \midiNote)
		}
	),

	\kn: (
		\turn: [(1..4), [\blue, \green, \white, \red]].flop.collect { |desc, i|
			var num, label;
			#num, label = desc;
			(\midiMsgType: \cc, \type: \knob, \label: label, \midiChan: 0, \midiNum: num, \spec: \midiCC)
		},
		\hat:  [(64..67), [\blue, \green, \white, \red]].flop.collect { |desc, i|
			var num, label;
			#num, label = desc;
			(midiMsgType: \cc, type: \button, \label: label, \midiChan: 0, \midiNum: num, \spec: \midiBut, \mode: \push)
		}
	),
	\bt: (11..14).collect { |num|
		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: num, \spec: \midiBut)
	},
	\btPreset: [50, 51, 52, 21, 22, 23, 24, 25].collect { |num|
		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: num, \spec: \midiBut)
	},

	\btBubble: 	(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 5, \spec: \midiBut, \mode: \push),
	\btMetro: 	(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 6, \spec: \midiBut, \mode: \push),

	//
	\btSynth: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 7, \spec: \midiBut, \mode: \push),
	\btDrums: 	(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 8, \spec: \midiBut, \mode: \push),
	\btTape: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 9, \spec: \midiBut, \mode: \push),
	\btMix: 	(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 10, \spec: \midiBut, \mode: \push),


	// upper right
	\btSeq: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 26, \spec: \midiBut, \mode: \push),
	\btCom: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 49, \spec: \midiBut, \mode: \push),
	\btMic: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 48, \spec: \midiBut, \mode: \push),

	// red labels
	\btLift: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 15, \spec: \midiBut, \mode: \push),
	\btPaste: 	(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 16, \spec: \midiBut, \mode: \push),
	\btCut: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 17, \spec: \midiBut, \mode: \push),

	\btRec: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 38, \spec: \midiBut, \mode: \push),
	\btPlay: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 39, \spec: \midiBut, \mode: \push),
	\btStop: 		(\midiMsgType: \cc, \type: \button, \midiChan: 0, \midiNum: 40, \spec: \midiBut, \mode: \push),

	'bend': 		('midiMsgType': 'bend', 'type': 'bender', 'midiChan': 0, 'midiNum':  0,'spec': 'midiBend')
)
)

/* test that this works:

MIDIMKtl.find;
m = MIDIMKtl('pmdd10');
m.verbose = true;

m.elementAt(\bt, 4).addAction({ |el| "but 4: %\n".postf(el.value); });
m.elementAt(\btJoin).action_({ |el| "btJoin: %\n".postf(el.value); });
m.elementAt(\keyOn, 1).action_({ |el| "keyOn: %\n".postf(el.value); });
m.elementAt(\keyOff, 1).action_({ |el| "keyOff: %\n".postf(el.value); });

m.elementAt(\kn, 1).action_({ |el| "el %: %\n".postf(el.name, el.value); });

*/
