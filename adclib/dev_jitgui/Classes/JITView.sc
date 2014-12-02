/* TODO:

// BUGS/glitches

// 1 * click and arrow into editstring for easier editing

*** unify with JITGui:
* put into a zone with its own layout
* make sure it works with direct placement, FlowLayout or new Layout schemes
* know its own preferred size
* know how to place itself in a Layout

* flexify getVal function,
* so PdefnGui can just have a changed function

* Copy/paste into/from MGui?
* Drag from lang, drop to lang?

* ChoiceSpec for eg env types, popup/listgui

*/

JITView {
	classvar <styleDict;

	var <parent, <bounds;
	var <value, <action;
	var <uv, <drawFunc, <dict;

	*initClass {
		styleDict = (
			\backCol: Color.grey(0.90),
			\focusCol: Color.grey(1.0),
			\labelCol: Color.blue(0.8, 0.7),
			\valCol: Color.green(0.6, 0.8),
			\editCol: Color.red(0.6, 0.6),
			\hiCol: Color.green(1.0, 0.21),
			\hiFontCol: Color.magenta(1.0, 0.3),
			\font: Font("Monaco", 16),
			\editFont: Font("Monaco", 24),
			\hiFont: Font("Monaco", 20),
			\lineHeight: 24,
			\hiLabel: "X"
		);
	}

	*new { |value, parent, bounds, options|
		^super.new.init(value, parent, bounds, options);
	}

	init { |invalue, inparent, inbounds, inoptions|
		dict = ().parent_(styleDict);
		action = MFunc();
		this.makeBounds(inbounds)
		.makeParent(inparent)
		.makeViews(inoptions)
		.makeDrawFunc
		.makeMouseActions
		.makeKeyDownActions
		.value_(invalue);
	}

	label { ^dict[\label] }
	label_ { |val| this.putDict(\label, val).refresh }

	mode { ^drawFunc.mode }

	funcs { ^[uv.drawFunc,
		uv.keyDownAction,
		uv.mouseDownAction,
		uv.mouseMoveAction]
	}

	mode_ { |name|
		this.funcs.do(_.mode_(name));
		this.refresh;
	}

	//	mode_ { |val| this.putDict(\mode, val) }

	refresh { defer { uv.refresh } }

	doAction { action.value(this) }

	// should be in Object?
	checkNum { |val|
		if (val.isKindOf(Number)) { ^1 };
		if (val.isKindOf(SequenceableCollection) and:
			{ val.every(_.isKindOf(SimpleNumber)) })
		{ ^2 };
		^0; // not a numerically displayable value
	}

	makeBounds { |inbounds|
		bounds = inbounds ?? { Rect(0, 0, 200, 24) };
	}
	makeParent { |inparent, inname|
		parent = inparent ?? {
			inparent = Window(inname ? this.class.name, bounds).front;
			// when in its own window, move to top left
			bounds = bounds.moveTo(0,0);
			inparent;
		};
	}

	makeViews {
		uv = UserView(parent, bounds);
		uv.background = dict[\backCol];
		uv.focusGainedAction = { uv.background_(dict[\focusCol]); };
		uv.focusLostAction = { uv.background_(dict[\backCol]); };

		uv.resize_(5); // elastic h+v
	}

	prepDraw {
		dict[\bounds] = uv.bounds;
		dict[\height] = dict[\bounds].height;
		dict[\width] = dict[\bounds].width;

		dict[\bounds00] = dict[\bounds].moveTo(0,0);
		dict[\boundsLabel] = dict[\bounds00].copy.height_(24);
		dict[\boundsValues] = dict[\boundsLabel].copy
		.moveTo(0, dict[\height] - 24);
		dict[\hiRect] = dict[\bounds00].copy
		.width_(dict[\width] * 0.5);
	}

	drawLabel {
		Pen.stringLeftJustIn(
			dict[\label],
			dict[\boundsLabel],
			dict[\font],
			dict[\labelCol]
		);
	}

	adjustFont { |str, font, rect|
		var drawSize = QtGUI.stringBounds(str, font);
		var scaler, drawFont = font;
		if (drawSize.width > rect.width) {
			scaler = rect.width / drawSize.width;
			drawFont = font.copy
			.size_(font.size * scaler);
		};
		^drawFont
	}

	drawValueAsCode {
		var drawFont, roundedVal;
		var currValStr, currVal = value;
		if (value.isNil) { currValStr = "-" } {
			if (this.checkNum(value) > 0) {
				currVal = value.round(dict[\round]);
				// keep orig number(s) if rounding has no effect,
				// so Integers remain Integers
				if (currVal == value) { currVal = value };
			};
			currValStr = currVal.asCompileString;
		};

		drawFont = this.adjustFont(
			currValStr,
			dict[\font],
			dict[\boundsValues]);

		Pen.stringRightJustIn(
			currValStr,
			dict[\boundsValues],
			drawFont,
			dict[\valCol]
		);
	}

	drawEditStr {
		var drawFont = this.adjustFont(
			dict[\editStr],
			dict[\editFont],
			dict[\bounds00]);

		Pen.stringCenteredIn(
			dict[\editStr],
			dict[\bounds00],
			drawFont,
			dict[\editCol]
		);
	}

	drawHilite {
		Pen.push;
		Pen.color_(dict[\hiCol]);
		Pen.fillRect(dict[\bounds00]);
		Pen.pop;
		Pen.stringCenteredIn(
			dict[\hiLabel], dict[\hiRect],
			dict[\hiFont], dict[\hiFontCol]
		);
	}

	// make drawFunc with all the options first,
	// then enable the ones you need as presets
	makeDrawFunc {
		uv.drawFunc = drawFunc = MFunc.new;

		drawFunc.add(\prep, { this.prepDraw });
		drawFunc.add(\label, { this.drawLabel });
		drawFunc.add(\code, { this.drawValueAsCode });
		drawFunc.add(\editStr, { this.drawEditStr });
		drawFunc.add(\hilite, { this.drawHilite }, active: false);

		// overwrite these in subclasses,
		// so name-only mode turns off code display, etc.
		drawFunc.modes.put(\code, (on: \code));
		drawFunc.modes.put(\nameOnly, (on: \label, off: \code));
	}


	value_ { |obj|
		value = obj;
		this.refresh;
	}

	valueAction_ { |obj|
		value = obj;
		action.value(this);
		this.refresh;
	}

	makeVals { |func, n|
		n = n ?? { value.size };
		if (n < 1) { ^func.value };
		^func.dup(n);
	}

	doEnter { |uv, mod = 0|			// return
		var valStr = dict[\editStr], newVal;
		if (valStr.isNil) {
			^this.doAction;
		};

		newVal = valStr.interpret;
		if (newVal.notNil or:
			{ newVal.isNil and: (valStr == "nil") }) {
			this.valueAction_(newVal);
			dict.put(\editStr, nil);
		} {
			"JITView: could not interpret % !\n"
			.postf(valStr.asCompileString)
		};
	}

	// basic keyDown first keyDown, like slider+numbox is now.

	makeKeyDownActions {
		uv.keyDownAction = MFunc.new;

		// put this in for debugging?
		// uv.keyDownAction.add(\post, { |uv, key, mod| [key, mod].postcs }, false);

		dict[\keyDownAltFuncs] = ($c: { this.mode_(\code.postcs) });

		dict[\keyDownFuncs] = ()
		.put(8.asAscii,  { |uv, mod = 0| // backspace by one, shift to clear
			if (mod.isShift) {
				dict.put(\editStr, "");
			} {
				dict.put(\editStr, dict[\editStr].drop(-1));
			};
		})
		.put($\t, { |uv, mod = 0|		// tab puts value in editString
			dict.put(\editStr, value.asCompileString);
		})
		.put($\r, {	|uv, mod = 0|			// return
			this.doEnter(uv, mod);
		});

		uv.keyDownAction.modes.put(\code, (on: \code));

		uv.keyDownAction.add(\code, { |uv, key, mod|
			var valStr = dict[\editStr] ? "";
			// [key, mod].postcs;
			var func;
			if (mod.isAlt) {
				func = dict[\keyDownAltFuncs][key]
			} {
				func = dict[\keyDownFuncs][key]
			};

			uv.focus(true);

			if (func.notNil) {
				func.value(uv, mod);
			} {
				// all other cases
				dict.put(\editStr, valStr ++ key);
			};
			uv.refresh;
		}, active: true);

	}

	putDict { |... pairs| dict.putPairs(pairs); }

	code { this.mode_(\code).refresh; }

	hilite { |hiLabel, hiColor|
		dict[\hiLabel] = hiLabel ? dict[\hiLabel];
		dict[\hiCol] = hiColor ? dict[\hiCol];
		uv.drawFunc.enable(\hilite);
		this.refresh;
	}
	nohilite {
		uv.drawFunc.disable(\hilite);
		this.refresh;
	}

	makeMouseActions {
		// prepare basic mouse actions
		uv.mouseDownAction = MFunc([\code, { "...insert cursor".postln }]);
		uv.mouseMoveAction = MFunc([\code, { "...text range".postln }]);
		uv.mouseUpAction = MFunc([\code, { }]);

		uv.mouseDownAction.modes.put(\code, (on: \code));
		uv.mouseDownAction.modes.put(\nameOnly, (off: \code));

		uv.mouseMoveAction.modes.put(\code, (on: \code));
		uv.mouseMoveAction.modes.put(\nameOnly, (off: \code));

		uv.mouseUpAction.modes.put(\code, (on: \code));
		uv.mouseUpAction.modes.put(\nameOnly, (off: \code));

	}
}

