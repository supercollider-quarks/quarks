/**
2006  Till Bovermann (IEM)
*/

JInT_PocketFader : JInT {
	var faderCCResponder;
	var midiSrc;
	var midiChan;
	var notInitialized = true;
	
	/** 
	 * @todo 
	 * @todo 
	 */
	*new {|server, src=2094993222, chan = 3|	// src of my PocketCtl
		^super.new.initPF(server, src, chan)
	}
	initPF {|server, src, chan|
		midiSrc  = src;
		midiChan = chan;
		////////////
		controllers = [
	/*  0 */	JInTC_Fader.new("Fader  1", server, [0, 127, \linear, 0].asSpec).short_(\f1),
	/*  1 */	JInTC_Fader.new("Fader  2", server, [0, 127, \linear, 0].asSpec).short_(\f2),
	/*  2 */	JInTC_Fader.new("Fader  3", server, [0, 127, \linear, 0].asSpec).short_(\f3),
	/*  3 */	JInTC_Fader.new("Fader  4", server, [0, 127, \linear, 0].asSpec).short_(\f4),
	/*  4 */	JInTC_Fader.new("Fader  5", server, [0, 127, \linear, 0].asSpec).short_(\f5),
	/*  5 */	JInTC_Fader.new("Fader  6", server, [0, 127, \linear, 0].asSpec).short_(\f6),
	/*  6 */	JInTC_Fader.new("Fader  7", server, [0, 127, \linear, 0].asSpec).short_(\f7),
	/*  7 */	JInTC_Fader.new("Fader  8", server, [0, 127, \linear, 0].asSpec).short_(\f8),
	/*  8 */	JInTC_Fader.new("Fader  9", server, [0, 127, \linear, 0].asSpec).short_(\f9),
	/*  9 */	JInTC_Fader.new("Fader 10", server, [0, 127, \linear, 0].asSpec).short_(\f10),
	/* 10 */	JInTC_Fader.new("Fader 11", server, [0, 127, \linear, 0].asSpec).short_(\f11),
	/* 11 */	JInTC_Fader.new("Fader 12", server, [0, 127, \linear, 0].asSpec).short_(\f12),
	/* 12 */	JInTC_Fader.new("Fader 13", server, [0, 127, \linear, 0].asSpec).short_(\f13),
	/* 13 */	JInTC_Fader.new("Fader 14", server, [0, 127, \linear, 0].asSpec).short_(\f14),
	/* 14 */	JInTC_Fader.new("Fader 15", server, [0, 127, \linear, 0].asSpec).short_(\f15),
	/* 15 */	JInTC_Fader.new("Fader 16", server, [0, 127, \linear, 0].asSpec).short_(\f16),
		];
	} // end initPF

	startCustom {
		if (notInitialized) {
			this.initialize;
		}; // fi
		
		faderCCResponder.init(true);
	} // end startCustom
	stopCustom {
			faderCCResponder.remove;
	}
	
	initialize {
		faderCCResponder = CCResponder(
			{ |src, chan, num, vel| 
//				[src, chan].postln;
				(midiSrc == src).if {
					controllers[num].set(0, vel);
//					{gui.faderValue_(num, vel)}.defer;
				}
			},
			nil,
			midiChan,
			install: false
		);
		notInitialized = false;
	} 
}