/*
	This was the first draft of DrawGrid/Grid
	the line algo comes from Plotter
	
	it is provided here so that SplineGui can have a fallback grid lines 
	implementation if DrawGrid/Grid is not present
	which is the case in SC 3.4
	
	will be removed
*/

GridLines0 {
	
	var <>userView,<>bounds,<>spec,<>domainSpec,<>gridOnX,<>gridOnY;
	var <>gridColorX, <>gridColorY,<>font,<>fontColor;
	var	<>gridLinePattern, <>gridLineSmoothing;
	var <>labelX,<>labelY;
	
	*new { arg userView,bounds,spec,domainSpec,gridOnX=true,gridOnY=true;
		^super.newCopyArgs(userView,bounds,spec,domainSpec,gridOnX,gridOnY).init
	}
	
	init {
		var w;
		userView = userView ?? {
			UserView(w = Window.new.front,bounds ?? {w.bounds.moveTo(0,0)})
				.resize_(5)
				.drawFunc_({ arg v;
					this.bounds = v.bounds;
					this.draw
				});
		};
		bounds = bounds ?? {userView.bounds};
		spec = spec ?? {ControlSpec(0, 1, 'linear', 0, 0.5, "")};
		domainSpec = domainSpec ?? {ControlSpec(0, 1, 'linear', 0, 0.5, "")};
		GUI.skin.at(\plot).use {
			font = ~gridFont ?? { Font.default };
			if(font.class != GUI.font) { font = Font(font.name, font.size) };
			gridColorX = ~gridColorX;
			gridColorY = ~gridColorY;
			fontColor = ~fontColor;
			gridLineSmoothing = ~gridLineSmoothing;
			gridLinePattern = ~gridLinePattern.as(FloatArray);
			labelX = ~labelX; // huh ? in the skin ?
			labelY = ~labelY;
		};
	}
	
	draw {
		Pen.addRect(bounds);
		Pen.fillColor = Color.white;
		Pen.fill;
		
		if(gridOnX) { this.drawGridX; this.drawNumbersX; };
		if(gridOnY) { this.drawGridY; this.drawNumbersY; };
		this.drawLabels;
	}

	drawGridX {
		var top = bounds.top;
		var base = bounds.bottom;

		this.drawOnGridX { |hpos|
			Pen.moveTo(hpos @ base);
			Pen.lineTo(hpos @ top);
		};

		Pen.strokeColor = gridColorX;
		this.prStrokeGrid;
	}

	drawGridY {
		var left = bounds.left;
		var right = bounds.right;

		this.drawOnGridY { |vpos|
			Pen.moveTo(left @ vpos);
			Pen.lineTo(right @ vpos);
		};

		Pen.strokeColor = gridColorY;
		this.prStrokeGrid;
	}

	drawNumbersX {
		var top = bounds.top;
		var base = bounds.bottom - 10;
		if(base > top,{
			Pen.fillColor = fontColor;
			this.drawOnGridX { |hpos, val, i|
				var string = val.asStringPrec(5) ++ domainSpec.units;
				Pen.font = font;
				Pen.stringAtPoint(string, hpos @ base );
				Pen.stroke;
			};
			Pen.stroke;
		})
	}

	drawNumbersY {
		var left = bounds.left;
		var right = bounds.right;
		Pen.fillColor = fontColor;

		this.drawOnGridY { |vpos, val, i|
			var string = val.asStringPrec(5).asString ++ spec.units;
			if(gridOnX.not or: { i > 0 }) {
				Pen.font = font;
				Pen.stringAtPoint(string, left @ vpos);
			}
		};

		Pen.stroke;
	}

	drawOnGridX { |func|
		var width = bounds.width;
		var left = bounds.left;
		var n, gridValues;
		/*if(this.hasSteplikeDisplay) {
			// special treatment of special case: lines need more space
			xspec = xspec.copy.maxval_(xspec.maxval * value.size / (value.size - 1))
		};*/
		// can calc these only on resize
		n = (bounds.width / 64).round(2);
		if(domainSpec.hasZeroCrossing) { n = n + 1 };

		gridValues = domainSpec.gridValues(n);
		if(gridOnY) { gridValues = gridValues.drop(1) };
		gridValues = gridValues.drop(-1);

		gridValues.do { |val, i|
			var hpos = left + (domainSpec.unmap(val) * width);
			func.value(hpos, val, i);
		};
	}

	drawOnGridY { |func|
		var base = bounds.bottom;
		var height = bounds.height.neg; // measures from top left
		var n, gridValues;

		n = (bounds.height / 32).round(2);
		if(spec.hasZeroCrossing) { n = n + 1 };
		gridValues = spec.gridValues(n);

		gridValues.do { |val, i|
			var vpos = base + (spec.unmap(val) * height);
			func.value(vpos, val, i);
		};
	}

	drawLabels {
		var sbounds;
		if(gridOnX and: { labelX.notNil }) {
			sbounds = try { labelX.bounds(font) } ? 0;
			Pen.font = font;
			Pen.strokeColor = fontColor;
			Pen.stringAtPoint(labelX,
				bounds.right - sbounds.width @ bounds.bottom
			)
		};
		if(gridOnY and: { labelY.notNil }) {
			sbounds = try { labelY.bounds(font) } ? 0;
			Pen.font = font;
			Pen.strokeColor = fontColor;
			Pen.stringAtPoint(labelY,
				bounds.left - sbounds.width - 3 @ bounds.top
			)
		};
	}

	x { ^this }
	setZoom { arg from,to;
		domainSpec.minval = from;
		domainSpec.maxval = to;
	}

	prStrokeGrid {
		Pen.width = 1;

		try {
			Pen.smoothing_(gridLineSmoothing);
			Pen.lineDash_(gridLinePattern);
		};

		Pen.stroke;

		try {
			Pen.smoothing_(true);
			Pen.lineDash_(FloatArray[1, 0])
		};
	}
}


