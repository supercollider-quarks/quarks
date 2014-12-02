/**
2006  Till Bovermann (IEM)
*/

JInT_PocketDial : JInT {
	var knobCCResponder;
	var midiSrc;
	var midiChan;
	var notInitialized = true;
	var <>step = 0.01;

	/** 
	 * @todo 
	 */
	*new {|server, src = -1758497667, chan = 1|
		^super.new.initPC(server, src, chan)
	}
	initPC {|server, src|
		midiSrc = src;
		////////////
		controllers = [
	/*  0 */	JInTC_EndlessKnob.new("Endless Knob  1", server, [0, 1, \linear, 0, step].asSpec).short_(\c1),
	/*  1 */	JInTC_EndlessKnob.new("Endless Knob  2", server, [0, 1, \linear, 0, step].asSpec).short_(\c2),
	/*  2 */	JInTC_EndlessKnob.new("Endless Knob  3", server, [0, 1, \linear, 0, step].asSpec).short_(\c3),
	/*  3 */	JInTC_EndlessKnob.new("Endless Knob  4", server, [0, 1, \linear, 0, step].asSpec).short_(\c4),
	/*  4 */	JInTC_EndlessKnob.new("Endless Knob  5", server, [0, 1, \linear, 0, step].asSpec).short_(\c5),
	/*  5 */	JInTC_EndlessKnob.new("Endless Knob  6", server, [0, 1, \linear, 0, step].asSpec).short_(\c6),
	/*  6 */	JInTC_EndlessKnob.new("Endless Knob  7", server, [0, 1, \linear, 0, step].asSpec).short_(\c7),
	/*  7 */	JInTC_EndlessKnob.new("Endless Knob  8", server, [0, 1, \linear, 0, step].asSpec).short_(\c8),
	/*  8 */	JInTC_EndlessKnob.new("Endless Knob  9", server, [0, 1, \linear, 0, step].asSpec).short_(\c9),
	/*  9 */	JInTC_EndlessKnob.new("Endless Knob 10", server, [0, 1, \linear, 0, step].asSpec).short_(\c10),
	/* 10 */	JInTC_EndlessKnob.new("Endless Knob 11", server, [0, 1, \linear, 0, step].asSpec).short_(\c11),
	/* 11 */	JInTC_EndlessKnob.new("Endless Knob 12", server, [0, 1, \linear, 0, step].asSpec).short_(\c12),
	/* 12 */	JInTC_EndlessKnob.new("Endless Knob 13", server, [0, 1, \linear, 0, step].asSpec).short_(\c13),
	/* 13 */	JInTC_EndlessKnob.new("Endless Knob 14", server, [0, 1, \linear, 0, step].asSpec).short_(\c14),
	/* 14 */	JInTC_EndlessKnob.new("Endless Knob 15", server, [0, 1, \linear, 0, step].asSpec).short_(\c15),
	/* 15 */	JInTC_EndlessKnob.new("Endless Knob 16", server, [0, 1, \linear, 0, step].asSpec).short_(\c16),
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
			{ |src, chan, num, val| 
				(midiSrc == src).if {
					controllers[val].set(
						0, 
						controllers[val].rawVals.first + 
							(num == 96).if({step}, {step.neg}) 
							% controllers[val].specs.first.maxval
					);

	//					{gui.knobValue_(num, val)}.defer;
				}
			},
			nil, 
			1,
			install: false
		);
		notInitialized = false;
	} 
}