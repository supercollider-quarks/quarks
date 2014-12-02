EZSmoothRanger : EZRanger {
	
		var action;
	
		sliderClass { ^SmoothRangeSlider }
		numberBoxClass { ^SmoothNumberBox }
		staticTextClass { ^StaticText }
	
		init { arg parentView, bounds, label, argControlSpec, argAction, initVal,
			initAction, labelWidth, argNumberWidth,argUnitWidth,
			labelHeight, argLayout, argGap,argMargin;

		var labelBounds, hiBounds,loBounds, unitBounds,rangerBounds;
		var numberStep;

		// Set Margin and Gap
		this.prMakeMarginGap(parentView, argMargin, argGap);

		unitWidth = argUnitWidth;
		numberWidth = argNumberWidth;
		layout=argLayout;

		bounds.isNil.if{bounds = 350@20};

		// if no parent, then pop up window
		# view,bounds = this.prMakeView( parentView,bounds);

		labelSize=labelWidth@labelHeight;
		numSize = numberWidth@labelHeight;

		// calcualate bounds
		# labelBounds,hiBounds,loBounds,rangerBounds, unitBounds
				= this.prSubViewBounds(innerBounds, label.notNil, unitWidth>0);

		label.notNil.if{ //only add a label if desired
			labelView = this.staticTextClass.new(view, labelBounds);
			labelView.string = label;
		};

		(unitWidth>0).if{ //only add a unitLabel if desired
			unitView = this.staticTextClass.new(view, unitBounds);
		};


		loBox = this.numberBoxClass.new(view, loBounds);
		rangeSlider = this.sliderClass.new(view, rangerBounds);
		hiBox = this.numberBoxClass.new(view, hiBounds);

		controlSpec = argControlSpec.asSpec;
		(unitWidth>0).if{unitView.string = " "++controlSpec.units.asString};
		action = argAction;

		loBox.action_({ |box| this.lo_(box.value).doAction; });
		rangeSlider.action_({ |sl|
				this.lo_(controlSpec.map(sl.lo));
				this.hi_(controlSpec.map(sl.hi));
				this.doAction;
			});
		hiBox.action_({ |box| this.hi_(box.value).doAction; });

		if (initVal.notNil) { this.value_(initVal) };
		if (initAction) { this.doAction };


//		if (controlSpec.step != 0) {
//			rangeSlider.step = (controlSpec.step / (controlSpec.maxval - controlSpec.minval));
//		};

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep;
		}{
			// controlSpec wants a step, so zooming in with alt is disabled.
			hiBox.alt_scale = 1.0;
			loBox.alt_scale = 1.0;
			rangeSlider.alt_scale = 1.0;
		};

		hiBox.step=numberStep;
		loBox.step=numberStep;
		hiBox.scroll_step=numberStep;
		loBox.scroll_step=numberStep;

		rangeSlider.receiveDragHandler = { arg slider;
			slider.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		rangeSlider.beginDragAction = { arg slider;
			controlSpec.map(slider.value)
		};

		this.prSetViewParams;
		
				
		labelView.applySkin( RoundView.skin );
		unitView.applySkin( RoundView.skin );
		this.applySkin( RoundView.skin );

	}
	
	setColors{arg stringBackground, stringColor, sliderColor,  numBackground,numStringColor,
			 numNormalColor, numTypingColor, knobColor,background, hiliteColor ;

			stringBackground.notNil.if{
				labelView.notNil.if{labelView.background_(stringBackground)};
				unitView.notNil.if{unitView.background_(stringBackground)};};
			stringColor.notNil.if{
				labelView.notNil.if{labelView.stringColor_(stringColor)};
				unitView.notNil.if{unitView.stringColor_(stringColor)};};
			sliderColor.notNil.if{
				rangeSlider.background_(sliderColor);};
			numBackground.notNil.if{
				hiBox.background_(numBackground);
				loBox.background_(numBackground);};
			numNormalColor.notNil.if{
				hiBox.normalColor_(numNormalColor);
				loBox.normalColor_(numNormalColor);};
			numTypingColor.notNil.if{
				hiBox.typingColor_(numTypingColor);
				loBox.typingColor_(numTypingColor);};
			numStringColor.notNil.if{
				hiBox.stringColor_(numStringColor);
				loBox.stringColor_(numStringColor);};
			knobColor.notNil.if{
				rangeSlider.knobColor_(knobColor);};
			background.notNil.if{
				view.background=background;};
			hiliteColor.notNil.if{	
				rangeSlider.hiliteColor=hiliteColor;};
			hiBox.refresh;
	}
		
	setSliderProperty { |key ...value| rangeSlider.perform( (key ++ "_").asSymbol, *value ); }
	
// 	doAction { action.value(this); } // why not just action.value(this); ?

	action { ^action }
	action_ { |func| action = func }
	
	bounds { ^view.bounds }
	bounds_ { |bounds| view.bounds = bounds }
	
	labelWidth { ^labelView !? { labelView.bounds.width } ? 0 }
	labelWidth_ { |width = 60|
		var delta;
		if( layout === \horz && { labelView.notNil } ) { // only for horizontal sliders
			delta = labelView.bounds.width - width;
			labelView.bounds = labelView.bounds.width_( width );
			loBox !? { loBox.bounds = loBox.bounds.left_( loBox.bounds.left - delta ) };
			rangeSlider.bounds = rangeSlider.bounds
				.width_( rangeSlider.bounds.width + delta )
				.left_( rangeSlider.bounds.left - delta );
		};
	}
	
	numberWidth { ^loBox !? { loBox.bounds.width } ? 0 }
	
	numberWidth_ { |width = 45|
		// TODO
	}
}

EZRoundRanger : EZSmoothRanger {

	sliderClass { ^RoundRangeSlider }
	numberBoxClass { ^RoundNumberBox }
	
	}