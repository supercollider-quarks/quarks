/**
2006  Till Bovermann (IEM)
*/

JInT_PocketControl : JInT {
	var knobCCResponder;
	var midiSrc;
	var notInitialized = true;
	
	/** 
	 * @todo 
	 * @todo 
	 */
	*new {|server, src=2094993222|	// src of my PocketCtl
		^super.new.initPC(server, src)
	}
	initPC {|server, src|
		midiSrc = src;
		////////////
		controllers = [
	/*  0 */	JInTC_Knob.new("Knob  1", server, [0, 127, \linear, 0].asSpec).short_(\c1),
	/*  1 */	JInTC_Knob.new("Knob  2", server, [0, 127, \linear, 0].asSpec).short_(\c2),
	/*  2 */	JInTC_Knob.new("Knob  3", server, [0, 127, \linear, 0].asSpec).short_(\c3),
	/*  3 */	JInTC_Knob.new("Knob  4", server, [0, 127, \linear, 0].asSpec).short_(\c4),
	/*  4 */	JInTC_Knob.new("Knob  5", server, [0, 127, \linear, 0].asSpec).short_(\c5),
	/*  5 */	JInTC_Knob.new("Knob  6", server, [0, 127, \linear, 0].asSpec).short_(\c6),
	/*  6 */	JInTC_Knob.new("Knob  7", server, [0, 127, \linear, 0].asSpec).short_(\c7),
	/*  7 */	JInTC_Knob.new("Knob  8", server, [0, 127, \linear, 0].asSpec).short_(\c8),
	/*  8 */	JInTC_Knob.new("Knob  9", server, [0, 127, \linear, 0].asSpec).short_(\c9),
	/*  9 */	JInTC_Knob.new("Knob 10", server, [0, 127, \linear, 0].asSpec).short_(\c10),
	/* 10 */	JInTC_Knob.new("Knob 11", server, [0, 127, \linear, 0].asSpec).short_(\c11),
	/* 11 */	JInTC_Knob.new("Knob 12", server, [0, 127, \linear, 0].asSpec).short_(\c12),
	/* 12 */	JInTC_Knob.new("Knob 13", server, [0, 127, \linear, 0].asSpec).short_(\c13),
	/* 13 */	JInTC_Knob.new("Knob 14", server, [0, 127, \linear, 0].asSpec).short_(\c14),
	/* 14 */	JInTC_Knob.new("Knob 15", server, [0, 127, \linear, 0].asSpec).short_(\c15),
	/* 15 */	JInTC_Knob.new("Knob 16", server, [0, 127, \linear, 0].asSpec).short_(\c16),
		];
	} // end initPC

	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		
		knobCCResponder.init(true);
	} // end startCustom
	stopCustom {
			knobCCResponder.remove;
	}
	
	initialize {
		knobCCResponder = CCResponder(
			{ |src, chan, num, vel| 
				(midiSrc == src).if {
					controllers[num].set(0, vel);
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