/**
2006  Till Bovermann (IEM)
*/

JInT_TriggerFinger : JInT {
	var 
		padNoteOnResponder, 
		padNoteOffResponder, 
		padCCResponder, 
		faderCCResponder, 
		knobCCResponder;
	var midiSrc;
	var notInitialized = true;
	
	/** 
	 * @todo 
	 * @todo 
	 */
	*new {|server, src=1946237828|	// src of my Triggerfinger
		^super.new.initTF(server, src)
	}
	initTF {|server, src|
		midiSrc = src;
		////////////
		controllers = [
	/*  0 */	JInTC_Fader("Fader F1", server, [0, 127, \linear, 0].asSpec).short_(\f1),
	/*  1 */	JInTC_Fader("Fader F2", server, [0, 127, \linear, 0].asSpec).short_(\f2),
	/*  2 */	JInTC_Fader("Fader F3", server, [0, 127, \linear, 0].asSpec).short_(\f3),
	/*  3 */	JInTC_Fader("Fader F4", server, [0, 127, \linear, 0].asSpec).short_(\f4),

	/*  4 */	JInTC_Knob.new("Knob  C1", server, [0, 127, \linear, 0].asSpec).short_(\c1),
	/*  5 */	JInTC_Knob.new("Knob  C2", server, [0, 127, \linear, 0].asSpec).short_(\c2),
	/*  6 */	JInTC_Knob.new("Knob  C3", server, [0, 127, \linear, 0].asSpec).short_(\c3),
	/*  7 */	JInTC_Knob.new("Knob  C4", server, [0, 127, \linear, 0].asSpec).short_(\c4),
	/*  8 */	JInTC_Knob.new("Knob  C5", server, [0, 127, \linear, 0].asSpec).short_(\c5),
	/*  9 */	JInTC_Knob.new("Knob  C6", server, [0, 127, \linear, 0].asSpec).short_(\c6),
	/* 10 */	JInTC_Knob.new("Knob  C7", server, [0, 127, \linear, 0].asSpec).short_(\c7),
	/* 11 */	JInTC_Knob.new("Knob  C8", server, [0, 127, \linear, 0].asSpec).short_(\c8),

	/* 12 */	JInTC_PPad.new("Pressure sensitive pad  1", server, [0, 127, \linear, 0].asSpec!2).short_(\pad1),
	/* 13 */	JInTC_PPad.new("Pressure sensitive pad  2", server, [0, 127, \linear, 0].asSpec!2).short_(\pad2),
	/* 14 */	JInTC_PPad.new("Pressure sensitive pad  3", server, [0, 127, \linear, 0].asSpec!2).short_(\pad3),
	/* 15 */	JInTC_PPad.new("Pressure sensitive pad  4", server, [0, 127, \linear, 0].asSpec!2).short_(\pad4),
	/* 16 */	JInTC_PPad.new("Pressure sensitive pad  5", server, [0, 127, \linear, 0].asSpec!2).short_(\pad5),
	/* 17 */	JInTC_PPad.new("Pressure sensitive pad  6", server, [0, 127, \linear, 0].asSpec!2).short_(\pad6),
	/* 18 */	JInTC_PPad.new("Pressure sensitive pad  7", server, [0, 127, \linear, 0].asSpec!2).short_(\pad7),
	/* 19 */	JInTC_PPad.new("Pressure sensitive pad  8", server, [0, 127, \linear, 0].asSpec!2).short_(\pad8),
	/* 20 */	JInTC_PPad.new("Pressure sensitive pad  9", server, [0, 127, \linear, 0].asSpec!2).short_(\pad9),
	/* 21 */	JInTC_PPad.new("Pressure sensitive pad 10", server, [0, 127, \linear, 0].asSpec!2).short_(\pad10),
	/* 22 */	JInTC_PPad.new("Pressure sensitive pad 11", server, [0, 127, \linear, 0].asSpec!2).short_(\pad11),
	/* 23 */	JInTC_PPad.new("Pressure sensitive pad 12", server, [0, 127, \linear, 0].asSpec!2).short_(\pad12),
	/* 24 */	JInTC_PPad.new("Pressure sensitive pad 13", server, [0, 127, \linear, 0].asSpec!2).short_(\pad13),
	/* 25 */	JInTC_PPad.new("Pressure sensitive pad 14", server, [0, 127, \linear, 0].asSpec!2).short_(\pad14),
	/* 26 */	JInTC_PPad.new("Pressure sensitive pad 15", server, [0, 127, \linear, 0].asSpec!2).short_(\pad15),
	/* 27 */	JInTC_PPad.new("Pressure sensitive pad 16", server, [0, 127, \linear, 0].asSpec!2).short_(\pad16),
		];
	} // end initTF
	
	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		
		[
			padNoteOnResponder, 
			padNoteOffResponder, 
			padCCResponder, 
			faderCCResponder, 
			knobCCResponder
		].do{|resp| resp.init(true)};
	} // end startCustom
	
	stopCustom {
		[
			padNoteOnResponder, 
			padNoteOffResponder, 
			padCCResponder, 
			faderCCResponder, 
			knobCCResponder
		].do{|resp| resp.remove};
	}
	initialize {
		padNoteOnResponder = NoteOnResponder(
			{ |src, chan, num, vel| 
				(midiSrc == src).if {
					controllers[num+11].beginCont(0, vel);
//					{gui.padNoteOnValue_(num, vel)}.defer;
				}
			}, 
			nil, 
			2, 
			install: false
		);
		padNoteOffResponder = NoteOffResponder(
			{ |src, chan, num, vel| 
				(midiSrc == src).if {
					controllers[num+11].endCont(0, 0);
//					{gui.padNoteOnValue_(num, 0)}.defer;
				}
			}, 
			nil,
			2,
			install: false
		);
		padCCResponder =  CCResponder(
			{ |src, chan, num, vel|
				(midiSrc == src).if {
					controllers[num+11].set(1, vel);
//					{gui.padCCValue_(num, vel)}.defer;
				}
			}, 
			nil,
			3,
			install: false
		);
		faderCCResponder = CCResponder(
			{ |src, chan, num, vel|
				(midiSrc == src).if {
					controllers[num-1].set(0, vel);
//					{gui.faderValue_(num, vel)}.defer;
				}
			},
			nil, 
			0,
			install: false
		);
		knobCCResponder = CCResponder(
			{ |src, chan, num, vel| 
				(midiSrc == src).if {
					controllers[num+3].set(0, vel);
//					{gui.knobValue_(num, vel)}.defer;
				}
			},
			nil, 
			1,
			install: false
		);
		notInitialized = false;
	} 
}