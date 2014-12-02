
ToggleTextField : SCViewHolder {
	// text field that is programmatically switchable on or off
	// when on, keyboard input goes into the string and is "trapped"
	// (no key bubbling)
	// when off, keyboard input bubbles up without going into the string
	// a button enables/disables -- green light, type your string

	var	<enabledButton, <textField, <>action,
		<buttonWidth = 15,
		<bounds,
		
		<>clearOnEnable = true;		// if true, string will be cleared when enable button is hit
	
	*new { arg parent, bounds;
		^super.new.init(parent, bounds)
	}

	init { arg argParent, argBounds;
		view = GUI.compositeView.new(argParent, argBounds);
//		try { view.relativeOrigin_(argParent.relativeOrigin) };
		bounds = view.bounds;
		enabledButton = GUI.button.new(view, this.buttonBounds)
			.states_([
				[" ", Color.black, Color.clear],
				[" ", Color.black, Color.green]
			])
			.action_({ |b|
				(b.value > 0).if({ this.enable(false) }, { this.disable(false) });
			});
		textField = GUI.textField.new(view, this.textBounds)
			.action_({ |txt|
				this.disable;
				action.value(txt);
			});
		this.disable;
	}
	
	remove {
		view.remove;
		view.parent.refresh;	// otherwise button doesn't disappear
	}
	
	string { ^textField.string }
	string_ { |str| textField.value_(str) }
	value { ^textField.value }
	value_ { arg val;
		textField.value_(val);
	}	
	valueAction_ { arg val;
		textField.value_(val);
		this.doAction;
	}
	
	focus { arg flag = true;
		flag.if({ this.enable }, { this.disable });
	}

	enable { |updateGUI = true|	// allow string input, no key bubbling
		textField.keyDownAction = { |v, key, modifiers, unicode|
			textField.defaultKeyDownAction(key, modifiers, unicode);
		};
		{	clearOnEnable.if({ textField.value_("") });
			textField.focus }.defer;
		updateGUI.if({ { enabledButton.value = 1 }.defer; });
	}
	
	disable { |updateGUI = true|
			// to get bubbling AND bypass the defaultKeyDownAction,
			// keyDownAction must be non-nil (a function) but RETURN nil when called
		textField.keyDownAction = { nil };
		updateGUI.if({ { enabledButton.value = 0 }.defer; });
	}
	
	toggle {
		(enabledButton.value == 0).if({ this.enable }, { this.disable });
	}
	
	buttonWidth_ { |width|
		var	widthDiff;
		widthDiff = width - buttonWidth;		// needed for view bounds adjustment
		buttonWidth = width;
		{	enabledButton.bounds = enabledButton.bounds.resizeBy(widthDiff, 0);
			textField.bounds = textField.bounds.moveBy(widthDiff, 0)
				.resizeBy(widthDiff.neg, 0);
		}.defer;
	}
	
	bounds_ { |b|
		bounds = b;
		{	view.bounds = bounds;
			enabledButton.bounds = this.buttonBounds;
			textField.bounds = this.textBounds;
		}.defer;
	}
	
	buttonBounds {
//		if(view.tryPerform(\relativeOrigin) ? false) {
			^Rect(0, 0, buttonWidth, bounds.height)
//		} {
//			Rect(bounds.left, bounds.top, buttonWidth, bounds.height)
//		}
	}
	
	textBounds {
//		if(view.tryPerform(\relativeOrigin) ? false) {
			^Rect(buttonWidth + 5, 0, bounds.width - buttonWidth - 5, bounds.height)
//		} {
//			Rect(bounds.left + buttonWidth + 5, bounds.top,
//				bounds.width - buttonWidth - 5, bounds.height)
//		}
	}
}
