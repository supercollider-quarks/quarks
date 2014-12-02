VerbosityAllGui : JITGui {

	// TODO: add button for posting type, and timestamp yes/no
	
	var <global;
	var <names, <keysRotation=0;
	var <edits;

	*new { |numItems = 16, parent, bounds, makeSkip = true, options = #[]| 
		^super.new( Verbosity.all, numItems, parent, bounds, makeSkip, options );
	}
		
	// options could include a TdefGui with EnvirGui ...
	makeViews { |options|
		var lineheight = 30;
		//max(	skin.buttonHeight * numItems + skin.headHeight, zone.bounds.height)  - (skin.margin.y * 2);

		if (parent.isKindOf(Window.implClass)) { 
			parent.name = Verbosity.name ++ ".all";
		};

		global = EZSlider( zone,
			Rect(0,0, bounds.width, lineheight),
			\global, \verbosity.asSpec, { |ez| Verbosity.globalLevel = ez.value; }, labelWidth: 120, numberWidth: 30, unitWidth: 15, gap: 4@4, margin: 4@4 )
		.font_(font);

		edits = Array.fill(numItems, { 
			VerbosityGui.new(
				numItems: 0, 
				parent: zone, 
				bounds: Rect(0,0, zone.bounds.width - 16, skin.buttonHeight), 
				makeSkip: false
			) 
		});
		
		parent.view.decorator.left_(zone.bounds.right - 12)
			.top_(zone.bounds.top + skin.headHeight);
		
		scroller = EZScroller(parent,
			Rect(0, 0, 12, numItems * skin.buttonHeight),
			numItems, numItems,
			{ |sc| keysRotation = sc.value.asInteger.max(0) }
		).visible_(false);
		
		scroller.slider.resize_(3);	
	}
	
	checkUpdate {
		var overflow, tooMany;

		names = object.keys.as(Array);
		
		try { names.sort };
		overflow = (names.size - numItems).max(0);
		if (overflow > 0) {
			scroller.visible_(true);
			scroller.numItems_(names.size);
			scroller.value_(keysRotation ? overflow);
			names = names.drop(keysRotation).keep(numItems);
		} {
			scroller.visible_(false);
		};
		global.value = Verbosity.globalLevel;
		edits.do { |edit, i| 
			edit.object_(object[ names[i] ]);
			edit.zone.visible_( object[ names[i] ].notNil );
		};
		//		if (vbGui.notNil) { vbGui.checkUpdate };
	}


}

VerbosityGui : JITGui {

	var <levelView;
	
	makeViews { 
		var lineheight = max(
			skin.buttonHeight * numItems + skin.headHeight, 
			zone.bounds.height)  - (skin.margin.y * 2); 
		
		nameView = DragBoth(zone, Rect(0,0, 120, lineheight))
			.font_(font)
			.align_(\center)
			.receiveDragHandler_({ arg obj; this.object = View.currentDrag });

		levelView = EZSlider( zone,
			Rect(0,0, bounds.width - 125, lineheight),
			nil, \verbosity.asSpec, { |ez| object.level = ez.value; }, numberWidth: 30, unitWidth: 15 )
			.font_(font);
	}

	checkUpdate {
		var newState = this.getState; 
		
		// compare newState and prevState, update gui items as needed
		if (newState == prevState) { ^this };
		
		if (newState[\object] != prevState[\object]) { 
			this.name_(this.getName);
		};
		if (newState[\level] != prevState[\level]) { 
			levelView.value_( newState[\level] );
		};
		prevState = newState;	
	} 

	getState { 
		// get all the state I need to know of the object I am watching
		^(object: object, level: try{ object.level } ) 
	}

}