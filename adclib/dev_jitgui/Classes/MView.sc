/* TODO MView

// 1. editStr action runs twice - why?
x = 0;
j = JITView(123);
// now type "x = x + 1" // -> 2

// 2. Window resize by code loops when userview with drawfunc is present.
m = MView(123);
m.drawFunc.disable(\all);
m.parent.bounds_(Rect(100, 100, 150, 24)); // crash
m.parent.bounds_(m.parent.bounds.moveTo(100, 100)); // fine, no resize

// later:

* soft-switch between vertical or horizontal display of values?
* support navig arrows for increment?

* draw method for Knob? Concentric multiknobs?
* draw method for XY, xys display?
--- could be nice for MTP/Influx

*/

MView : JITView {

	*initClass {
		Class.initClassTree(this.superclass);
		styleDict.putAll((
			\indexCol: Color.grey(0.0, 0.5),
			\indexFont: Font("Monaco", 12),
			\round: 0.0001,
			\shiftMode: \stop,
			\knobCol: Color.grey(0.0, 0.25),
			\knobCCol: Color.grey(1.0, 0.25),
			\ghostCol: Color.black
		));
	}

	init { |invalue, inparent, inbounds, inoptions|
		^super.init(invalue, inparent, inbounds, inoptions).mode_(\number);
	}

	makeDrawFunc {
		super.makeDrawFunc;
		drawFunc.add(\number, { this.drawNumber }, active: false);
		drawFunc.add(\ghost, { this.drawGhost }, active: false);
		drawFunc.modes.put(\code, (on: \code, off: \number));
		drawFunc.modes.put(\number, (on: [\number, \code]));
	}

	doEnter { |uv, mod = 0|
		// overrides return in keyDownFuncs
		var newVal = try { dict[\editStr].interpret };
		if (newVal.notNil) {
			var spec = dict[\myspec];
			if (spec.notNil and: { this.checkNum(newVal) > 0}) {
				newVal = spec.constrain(newVal);
			} {
				"JITView - no spec for val, thus unconstrained: %\n"
				.postf(newVal);
			};

			this.valueAction_(newVal);
			dict.put(\editStr, nil);
		};
	}


	makeKeyDownActions {
		super.makeKeyDownActions;
		dict.put(\keyNumFuncs, (
			$x: { |uv, mod| this.shiftRange(-1.0, \stop).doAction },
			$X: { |uv, mod| this.setUni(nil, this.makeVals(0.0)).doAction },

			$m: { |uv, mod| this.shiftRange(1.0, \stop).doAction },
			$M: { |uv, mod| this.setUni(nil, this.makeVals(1.0)).doAction },

			$c: { |uv, mod|
				var currVals = this.getUni.asArray;
				var currCenter = mean([currVals.minItem, currVals.maxItem]);
				this.shiftRange(0.5 - currCenter, \stop).doAction },
			$C: { |uv, mod| this.setUni(nil, this.makeVals(0.5)).doAction },

			$r: { |uv, mod|
				var currVals = this.getUni, jump;
				if (currVals.size < 1) {
					this.setUni(nil, 1.0.rand);
				} {
					jump = rrand(currVals.minItem.neg, 1-currVals.maxItem);
					this.shiftRange(jump, \stop)
				};
				this.doAction
			},
			$R: { |uv, mod|
				this.setUni(nil, this.makeVals({ 1.0.rand; })).doAction
			}
		));

		// number mode with alt-n
		dict[\keyDownAltFuncs].put($n, { |uv, key|
			this.mode_(\number.postcs) });

		dict[\keyNumFuncs].parent_(dict[\keyDownFuncs]);

		// simple 1, 2 or more number(s), like slider+numbox is now.
		uv.keyDownAction.add(\number, { |uv, key, mod|
			mod = mod ? 0;
			uv.focus(true);

			if (mod.isAlt) {
				dict[\keyDownAltFuncs][key].value;
			} {

				if ("[]1234567890.e+-,".includes(key)) {
					dict.put(\editStr, dict[\editStr] ? "" ++ key);
				} {
					dict[\keyNumFuncs][key].value(uv, mod);
				};
			};
			uv.refresh;

		}, active: false);

		uv.keyDownAction.modes.put(\number, (on: \number, off: \code));
	}
	// paramName is needed for use with SoftSet,
	// could be used for checking spec.
	setUni { |paramName, normVal|
		var spec;
		if (normVal.isNil) { ^this };
		spec = dict[\myspec].value.asSpec;
		if (spec.notNil) { this.value_(spec.map(normVal)); this.refresh };
	}

	getUni {
		var spec = dict[\myspec].value.asSpec;
		^if (spec.notNil and: { this.checkNum(value) > 0 }) { (spec.unmap(value)) };
	}

	get { ^value }
	set { |val| this.value_(val) }

	cannotDrawNumber {
		"MView cannot display as number(s): %\n".postf(value);
	}

	drawGhost {
		drawFunc.add(\ghost, { |uv|
			var bounds = dict[\bounds];
			var height = dict[\height], width = dict[\width];
			var ghostPos = dict[\ghostPos];
			var ghostLabel = dict[\ghostLabel];
			var ghostBounds, ghostWidthHalf, xpos, centPt;

			if (ghostPos.notNil) {
				xpos = ghostPos * width;
				centPt = xpos@(height*0.5);
				Pen.color_(dict[\ghostCol]);
				Pen.width_(1);
				Pen.addRoundedRect(Rect.aboutPoint(centPt, 8, height*0.5 - 2), 5, 5);
				Pen.addArc(centPt, 5, 0, 2pi);
				Pen.stroke;
			};

			if (ghostLabel.notNil) {
				Pen.color_(dict[\ghostCol]);
				xpos ?? {
					xpos = (0.25 * width);
					centPt = xpos@(height*0.5);
				};
				ghostLabel = ghostLabel.asString;
				ghostBounds = ghostLabel.bounds(dict[\hiFont]);
				ghostWidthHalf = ghostBounds.width;
				ghostBounds.left = xpos - ghostWidthHalf.clip(0, dict[\width]);
				ghostLabel.drawCenteredIn(
					Rect.aboutPoint(centPt, ghostWidthHalf, dict[\height] *0.5),
				dict[\hiFont]);
			};
		});
	}

	drawNumber {
		switch(this.checkNum(value),
			1, {
				^this.drawSingleNumber },
			2, { ^this.drawMultiNumber },
			0, { ^this.cannotDrawNumber }
		);
	}

	drawSingleNumber {

		var normval = this.getUni;
		var xval, dot, height;

		normval = this.getUni;
		if (normval.isNil) { ^this.cannotDrawNumber };

		xval = normval * dict[\width];
		height = dict[\height];
		dot = xval@(height*0.5);

		Pen.color_(dict[\knobCol]);
		Pen.addRoundedRect(Rect(xval - 8, 2, 16, height - 4), 5, 5);
		Pen.fill;

		// paint dot
		Pen.color_(dict[\knobCCol]);
		Pen.width_(2);
		Pen.addArc(dot, 6, 0, 2pi).fill;
	}

	drawMultiNumber {
		var normvals, xvals, valsRect, dotStep, dots;
		var leftEnd, rightEnd, rangeWidth, height;

		normvals = this.getUni;
		if (normvals.isNil or: { normvals.isEmpty }) {
			^this.cannotDrawNumber
		};

		xvals = normvals * dict[\width];

		leftEnd = xvals.minItem;
		rightEnd = xvals.maxItem;
		rangeWidth = rightEnd - leftEnd;
		height = dict[\height];

		valsRect = Rect(leftEnd, 2, rangeWidth, height - 4);

		dotStep = valsRect.height / (xvals.size);
		dots = xvals.collect { |val, i| val @ (i + 0.5 * dotStep + 2) };

		dict.put(\xvals, xvals);
		dict.put(\dots, dots);

		// paint range area as block
		Pen.color_(dict[\knobCol]);
		Pen.fillRect(valsRect);

		// paint left and right bars
		Pen.color_(dict[\knobCol]);
		[leftEnd, rightEnd].do { |xpos|
			Pen.addRoundedRect(
				Rect(xpos-8, 2, 16, height - 4),
				5, 5
			);
		};
		Pen.fill;

		// paint horiz layer strips for the dots
		dots.do { |dot, i|
			if (i.even) {
				Pen.fillRect(
					Rect(leftEnd, dotStep * i + 2, rangeWidth, dotStep)
				);
			};
		};

		// paint the dots
		Pen.color_(dict[\knobCCol]);
		Pen.width_(2);

		// depending on movemode, hilite
		// dot, bar, or whole knob

		dots.do { |dot, i|
			var nextdot = dots[i+1];
			Pen.addArc(dot, 6, 0, 2pi).fill;
			if (dots.size > 1) {
				Pen.stringAtPoint(i.asString, dot,
				dict[\indexFont], dict[\indexCol]);
			};
			// lines between dots are off for now
			// if (nextdot.notNil) {
			// 	Pen.line(dot, nextdot).stroke;
			// };
		};
	}

	mouseDownNumber { |uv, x, y|
		var xy = x@y;
		var foundIndex;

		if (this.checkNum(value) < 1) { ^this.cannotDrawNumber };

		dict[\mousexy] = xy;
		dict[\normx] = x / dict[\width];
		dict[\moveMode] = \noMove;

		if (value.isKindOf(SimpleNumber)) {
			dict[\moveMode] = \number;
		} {
			foundIndex = dict[\dots].detectIndex { |dot|
				dot.dist(xy) < 6 };
			dict[\moveMode] = \shiftRange; // default
			dict[\foundIndex] = foundIndex; // index or nil

			// find selected dot:
			if (foundIndex.notNil) {
				//	"foundIndex: %\n".postf(foundIndex);
				dict[\moveMode] = \single;
			} {
				// no dot index found, so try borders
				if (x.absdif(dict[\xvals].minItem) < 8) {
					dict[\moveMode] = \scaleMin;
				} {
					if (x.absdif(dict[\xvals].maxItem) < 8) {
						dict[\moveMode] = \scaleMax;
					} {
						if (x.inclusivelyBetween(
							dict[\xvals].minItem,
							dict[\xvals].maxItem)) {
							dict[\moveMode] = \shiftRange;
		}; }; }; } };
	}

	mouseMoveNumber { |uv, x, y, mod|
		var normX = (x / dict[\width]);
		var xy = x@y;
		// "mouseMove: % x: % y: % normval: % \n".postf(dict[\moveMode], x, y, normX);
		(
			\number: { this.setUni(nil, normX) },
			\shiftRange: {
				this.shiftRange(normX - dict[\normx], dict[\shiftMode]);
			},
			\scaleMin: { this.scaleMin(normX) },
			\scaleMax: { this.scaleMax(normX) },
			\single: {
				this.setNormNumByIndex(dict[\foundIndex], normX);
			}
		)[dict[\moveMode]].value;
		// cache these after doing the actions:
		dict[\mousexy] = xy;
		dict[\normx] = normX;
		this.doAction;
		this.refresh;
	}

	// methods when value is an array of numbers
	scaleMin { |normVal|
		var currVals = this.getUni;
		var currmin = currVals.minItem;
		var currmax = currVals.maxItem;
		this.setUni(nil, currVals.linlin(
			currmin, currmax, normVal, currmax));
	}

	scaleMax { |normVal|
		var currVals = this.getUni;
		var currmin = currVals.minItem;
		var currmax = currVals.maxItem;
		this.setUni(currVals.linlin(
			currmin, currmax, currmin, normVal));
	}

	shiftRange { |normDiff = 0, mode = \stop|
		// modes can be \stop or \clip
		var currVals = this.getUni;
		if (currVals.size < 1) {
			^this.setUni(nil, currVals + normDiff)
		};

		if (mode == \stop) {
			if (normDiff > 0) {
				normDiff = min(normDiff, 1 - currVals.maxItem);
			} {
				normDiff = max(normDiff, currVals.minItem.neg);
			};
		};
		this.setUni(nil, this.getUni + normDiff)
	}

	setNormNumByIndex { |index, normval|
		value.put(index, dict[\myspec].map(normval));
	}


	makeMouseActions {
		super.makeMouseActions;

		uv.mouseDownAction.modes.put(\number, (on: \number, off: \code));
		uv.mouseMoveAction.modes.put(\number, (on: \number, off: \code));
		uv.mouseUpAction.modes.put(\number, (on: \number, off: \code));

		uv.mouseDownAction.modes.put(\code, (on: \code, off: \number));
		uv.mouseMoveAction.modes.put(\code, (on: \code, off: \number));
		uv.mouseUpAction.modes.put(\code, (on: \code, off: \number));

		uv.mouseDownAction.add(\number, { |uv, x, y, mod|
			this.mouseDownNumber(uv, x, y, mod);
		}, active: false);

		uv.mouseMoveAction.add(\number, { |uv, x, y, mod|
		this.mouseMoveNumber(uv, x, y, mod);
	});
	}

	number { this.mode_(\number).refresh; }
}
