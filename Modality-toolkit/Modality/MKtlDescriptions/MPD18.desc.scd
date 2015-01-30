/*

MPD18

one slider, sends on chan 1, ccnum, 1, [0, 127, \lin, 1, 0]

channel aftertouch,sends on cahn 0, [0, 127, \lin, 1, 0]

16 pads x 3 banks, pad 1, ... pad 16

/****** TESTING ******/

MIDIMKtl.find;
m = MIDIMKtl('kmpd180');  // Akai MPD18
// slider
m.elements[\sl].addAction({ |...args| args.post; " slider YO!".postln; });
// touch on any pad
m.elements[\touch_0].addAction({ |...args| args.post; " touch YO !".postln; });

// pad1 noteOn direct indexing
m.elements[\padOn][0][0].addAction({ |...args| args.post; "pad1 sc1 on YO !".postln; });
// pad16 scene B noteOff
m.elements[\padOn][1][15].addAction({ |...args| args.post; "pad16 sc2 off YO !".postln; });

// use nicer lookup
m.elementAt(\padOn, 1, 2).addAction({ |...args| args.post; "pad2 sc1 on YO !".postln; });
m.elementAt(\padOff, 1, 2).addAction({ |...args| args.post; "pad2 sc1 off YO !".postln; });


/****** END TESTs ******/
*/
(
device: "Akai MPD18",
protocol: \midi,
description: (
	\sl: (\midiMsgType: \cc, \type: \slider, \midiChan: 0, \midiNum:  1, \spec: \midiCC),

	// // ------- touch ------------
	'touch_0': ('midiMsgType': 'touch', 'type': 'chantouch', 'midiChan': 0, 'midiNum':  0,'spec': 'midiTouch'),

	\padOn: (36..83).clump(16).collect{ |padNums|
		padNums.collect {|padNum| (\midiMsgType: \noteOn, \type: \pad, midiChan: 0,
			\midiNum: padNum, \spec: \midiNote) }
	},
	\padOff: (36..83).clump(16).collect{ |padNums|
		padNums.collect {|padNum| (\midiMsgType: \noteOff, \type: \pad, midiChan: 0,
			\midiNum: padNum, \spec: \midiNote) }
	}
)
)