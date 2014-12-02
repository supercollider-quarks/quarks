TextFieldOld : SCViewHolder {
	var text, <string, keyString, <stringColor, <>typingColor, <background, drawColor, <>action, innerBounds;

	*new { |parent, bounds|
		^super.new.init(parent, bounds)
	}

	init { |parent, bounds|
		// UserView can focus; CompositeView can't
		bounds = bounds.asRect;
		innerBounds = bounds.insetBy(1, 1).moveTo(1, 1);
		background = Color.white;
		this.view = UserView(parent, bounds).background_(Color.black).canFocus_(true)
		.drawFunc_({
			Pen.color_(background).fillRect(innerBounds)
			.color_(drawColor)
			.stringAtPoint(string, Point(1, 1))
		});
		// text = StaticText(view, bounds.insetBy(1, 1).moveTo(1, 1)).background_(Color.white);
		view.keyDownAction = { |view, char, modifiers, unicode|
			this.doKey(char, modifiers, unicode);
			true  // required for QT
		};
		typingColor = Color.red(0.7);
		this.stringColor = Color.black;
		string = String.new;
	}

	// needed?
	// viewDidClose {
	// 	text.remove;
	// }

	// background { ^text.background }
	background_ { |color|
		// text.background = color;
		background = color;
		view.refresh;
	}
	borderColor { ^view.background }
	borderColor_ { |color| view.background = color }
	stringColor_ { |color|
		stringColor = color;
		drawColor = color;
	}

	bounds_ { |rect|
		rect = rect.asRect;
		innerBounds = rect.insetBy(1, 1).moveTo(1, 1);
		view.bounds = rect;
		// text.bounds = rect.insetBy(1, 1).moveTo(1, 1);
	}

	string_ { |str|
		string = str;
		view.refresh;
		// text.string = str;
	}

	value_ { |str|
		drawColor = stringColor;
		this.string = str;
	}
	valueAction_ { |str|
		drawColor = stringColor;
		this.string = str;
		action.value(this, str);
	}

	doKey { arg key, modifiers, unicode;
		if(unicode == 0,{ ^this });
		// standard keydown
		if ((key == 3.asAscii) || (key == $\r) || (key == $\n), { // enter key
			if (keyString.notNil,{ // no error on repeated enter
				this.valueAction_(string);
				keyString = nil;// restart editing
			});
			^this
		});
		if (key == 127.asAscii or: { unicode == 8 }, { // delete key
			if(keyString.notNil,{
				if(keyString.size > 1,{
					keyString = keyString.copyRange(0,keyString.size - 2);
				},{
					keyString = String.new;
				});
				drawColor = typingColor;
				this.string = keyString;
			},{
				keyString = String.new;
				drawColor = typingColor;
				this.string = keyString;
			});
			^this
		});
		if (keyString.isNil, {
			drawColor = typingColor;
			keyString = this.string;
		});
		keyString = keyString.add(key);
		this.string = keyString;
	}

	// defaultGetDrag {
	// 	^this.string
	// }
	// defaultCanReceiveDrag {
	// 	^currentDrag.respondsTo(\asString)
	// }
	// defaultReceiveDrag {
	// 	this.valueAction = currentDrag;
	// }
}

