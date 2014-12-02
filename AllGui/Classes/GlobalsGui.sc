GlobalsGui : JITGui {
	var <textViews, <cmdLineView, <codeDumpView;
	var <editKeys = #[], keysRotation = 0;

	classvar <names = #[
		\a, \b, \c, \d, \e, \f, \g,
		\h, \i, \j, \k, \l, \m, \n,
		\o, \p, \q, \r, \s, \t, \u,
		\v, \w, \x, \y, \z, \cmdLine, \codeDump ];

	*new { |numItems = 12, parent, bounds|
			// numItems not supported yet, should do scrolling
			// ... for small screens ...
		^super.new(thisProcess.interpreter, numItems, parent, bounds);
	}

		// these methods should be overridden in subclasses:
	setDefaults { |options|
		if (parent.isNil) {
			defPos = 10@260
		} {
			defPos = skin.margin;
		};
		minSize = 200 @ (numItems + 2 * skin.buttonHeight + 4);
	}

	makeViews {
		var textwidth = zone.bounds.width - 20;
		var textheight = skin.buttonHeight;

		cmdLineView = EZText(zone, textwidth + 16 @ textheight, 'cmdLine', labelWidth: 64)
			.enabled_(false);
		codeDumpView = EZText(zone, textwidth + 16 @ textheight, 'codeDump', labelWidth: 64)
			.enabled_(false);

		cmdLineView.labelView.align_(\center);
		cmdLineView.view.resize_(2);

		textViews = numItems.collect { |i|
			var text, labelWidth = 15, canEval = true;

			text = EZText(zone, textwidth @ textheight, "",
				{ |tx|
					object.perform(
						text.labelView.string.asSymbol.asSetter,
						tx.textField.string.interpret);
				},
				labelWidth: labelWidth
			);
			text.visible_(false);
			text.view.resize_(2);
			text.labelView.align_(\center);
			text;
		};

		textViews = textViews;

		/// make a scroller
		zone.decorator.reset.shift(zone.bounds.width - 16, textheight * 2);
		scroller = EZScroller(zone,
			Rect(0, 0, 12, numItems * textheight),
			numItems, numItems,
			{ |sc| keysRotation = sc.value.asInteger.max(0); }
		).visible_(false);

		scroller.slider.resize_(3);

		this.name_(this.getName);
	}

	getState {
		var state = ();
		names.do { |globvar|
			state.put(globvar, object.instVarAt(globvar))
		};
		^state;
	}

	getName { ^"Global_Vars" }
	winName { ^this.getName }

	checkUpdate {
		var newKeys;
		var newState = this.getState;
		var allNewKeys = newState.keys.remove(\cmdLine).asArray.sort;
		var overflow = (allNewKeys.size - numItems).max(0);

		keysRotation = keysRotation.clip(0, overflow);

		newKeys = allNewKeys.drop(keysRotation).keep(numItems);

		if (prevState[\cmdLine] != newState[\cmdLine]) {
			cmdLineView.value_(newState[\cmdLine]);
		};
		if (prevState[\codeDump] != newState[\codeDump]) {
			cmdLineView.value_(newState[\codeDump]);
		};

		scroller
			.numItems_(allNewKeys.size)
			.visible_(overflow > 0)
			.value_(keysRotation);

		textViews.do { |textView, i|
			var oldKey = editKeys[i];
			var oldObj = textView.value;
			var newKey = newKeys [i];
			var newObj = newState[newKey];

			if (newKey != oldKey) {
				textView.visible_(newKey.notNil);
				textView.labelView.string_(newKey.asString);
			};
			if (newObj != oldObj) {
				textView.value_(newObj ? "");
			};
		};

		prevState = newState;
		editKeys = newKeys;
	}
}
