
EZFancySlider : EZSlider {


	init { arg parentView, bounds, label, argControlSpec, argAction, initVal,
			initAction, labelWidth, argNumberWidth,argUnitWidth,
			labelHeight, argLayout, argGap, argMargin;

		var labelBounds, numBounds, unitBounds,sliderBounds;
		var numberStep;
		//try{SwingGUI.put(\fancySlider, JSCFancySlider);};
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

		// calculate bounds of all subviews
		# labelBounds,numBounds,sliderBounds, unitBounds
				= this.prSubViewBounds(innerBounds, label.notNil, unitWidth>0);

		// instert the views
		label.notNil.if{ //only add a label if desired
			labelView = GUI.staticText.new(view, labelBounds);
			labelView.string = label;
		};

		(unitWidth>0).if{ //only add a unitLabel if desired
			unitView = GUI.staticText.new(view, unitBounds);
		};
		sliderView = FancySlider.new(view, sliderBounds);
		numberView = GUI.numberBox.new(view, numBounds);

		// set view parameters and actions

		controlSpec = argControlSpec.asSpec;
		controlSpec.addDependant(this);
		this.onClose = { controlSpec.removeDependant(this) };
		(unitWidth>0).if{unitView.string = " "++controlSpec.units.asString};
		initVal = initVal ? controlSpec.default;
		action = argAction;

		sliderView.action = {
			this.valueAction_(controlSpec.map(sliderView.value));
		};

		sliderView.receiveDragHandler = { arg slider;
			sliderView.valueAction = controlSpec.unmap(GUI.view.currentDrag);
		};

		sliderView.beginDragAction = { arg slider;
			controlSpec.map(sliderView.value);
		};

		numberView.action = { this.valueAction_(numberView.value) };

		numberStep = controlSpec.step;
		if (numberStep == 0) {
			numberStep = controlSpec.guessNumberStep
		}{
			// controlSpec wants a step, so zooming in with alt is disabled.
			numberView.alt_scale = 1.0;
			sliderView.alt_scale = 1.0;
		};

		numberView.step = numberStep;
		numberView.scroll_step = numberStep;
		//numberView.scroll=true;

		if (initAction) {
			this.valueAction_(initVal);
		}{
			this.value_(initVal);
		};
		
		if (labelView.notNil) {
			labelView.mouseDownAction = {|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {this.editSpec});
			}
		};
		
		this.prSetViewParams;

	}
	setColors{arg stringBackground,stringColor,sliderBackground,numBackground,
		numStringColor,numNormalColor,numTypingColor,knobColor,background;

			stringBackground.notNil.if{
				labelView.notNil.if{labelView.background_(stringBackground)};
				unitView.notNil.if{unitView.background_(stringBackground)};};
			stringColor.notNil.if{
				labelView.notNil.if{labelView.stringColor_(stringColor)};
				unitView.notNil.if{unitView.stringColor_(stringColor)};};
			numBackground.notNil.if{
				numberView.background_(numBackground);};
			numNormalColor.notNil.if{
				numberView.normalColor_(numNormalColor);};
			numTypingColor.notNil.if{
				numberView.typingColor_(numTypingColor);};
			numStringColor.notNil.if{
				numberView.stringColor_(numStringColor);};
			sliderBackground.notNil.if{
				sliderView.background_(sliderBackground);};
			knobColor.notNil.if{
				sliderView.knobColor_(knobColor);
			};
			background.notNil.if{
				view.background=background;};
			numberView.refresh;
	}
	
}
