// by Martin Marier

ArrayWarp : LinearWarp {
	var <>array, <>interpolate=false, <w, step, <ms;
	*initClass {
		Warp.warps.put(\array, ArrayWarp)
	}
	*new { arg spec, array;
		^super.new(spec.asSpec).init(array);
	}
	init { arg argArray;
		array = argArray ? [0.0,1.0];
		array = array.normalize;
		step = spec.step/spec.range;
	}
	step_ {
		step = spec.step/spec.range;
		try {ms.step_(step)};		
	}
	map { arg value;
		if (interpolate) {
			^super.map(array.blendAt(value * (array.size-1)));
		}{
			^super.map(array.at(value * (array.size-1)));
		}
	}
	unmap { arg value;
		^array.indexInBetween(
			super.unmap(value)
		) / (array.size-1);
	}
	makeWindow { arg x=30, y=300, action, name="ArrayWarp";
		var display;
		w = Window(name , Rect(x, y, 350, 400))
			.alwaysOnTop_(true);
		w.addFlowLayout( 10@10, 5@2 );
		ms = MultiSliderView(w, 330@330)
			.resize_(1)
			.elasticMode_(1)
			.indexThumbSize_(50)
			.valueThumbSize_(2)
			.value_(array)
			.step_(step)
			.action_({ |a|
				array = a.value;
				display.string_(
					super.map(ms.currentvalue).asString
				);
				action.value;
			});
		StaticText(w, 30@15)
			.string_("size")
			.resize_(7)
			.align_(\right);
		NumberBox(w, 40@15)
			.value_(array.size)
			.resize_(7)
			.action_({|j|
				array = Array.fill(j.value,{|i| array[i] ? 0});
				ms.value_(array);
				action.value;
			});
		StaticText(w, 200@15)
			.string_("interpolation")
			.resize_(9)
			.align_(\right);
		Button(w, 40@20)
			.states_([["off"],["on"]])
			.value_(interpolate.binaryValue)
			.resize_(9)
			.action_({ |butt|
				interpolate = butt.value.booleanValue;
				action.value;
			});
		display=StaticText(w, 330@15)
			.string_("")
			.resize_(8)
			.align_(\center);
		w.front;
	}
}

+ Array {
	asWarp { arg spec;
		^ArrayWarp.new(spec, this)
	}
}

//method that increase a value according to a spec.
//currentVal in value to be incremented.
//increment is the number of steps the value should be incremented.
//numSteps is how many steps there are in spec.range (spec.range/spec.step).
//numSteps parameter is used only if spec.step == 0
//(calculated numSteps would be inf).

+ Warp {
	increase { arg currentVal, increment, numSteps=1000;
		^if (spec.step == 0) {
			spec.map(spec.unmap(currentVal) + (increment / numSteps))
		}{
		 	currentVal + (spec.step * increment);
		}
	}
	//increment is the same but for values between 0 and 1 (unmapped).
	increment { arg currentVal, increment, numSteps=1000;
		^if (spec.step == 0) {
			currentVal + (increment / numSteps)
		}{
		 	currentVal + (increment * spec.step / spec.range );
		}
	}
}

+ ControlSpec {
	makeWindow { arg x=30, y=900, action, name="Control Spec";
		var w, return, widgets, curve;
		w = Window(name , Rect(x, y, 290, 70), false)
			.alwaysOnTop_(true);
		w.addFlowLayout( 10@10, 5@2 );
		[\minval,\maxval,\warp,\step,\default,\units].do{ |i|
			StaticText(w, 40@15).string_(i.asString).align_(\center);
		};
		//return = this.deepCopy;
		widgets = [
			NumberBox(w, 40@18).value_(this.minval)
				.action_({|i|
					this.minval_(i.value);
					this.init;
					this.warp.class.switch(
						CurveWarp, { this.warp.init(curve.value) }
					);
					action.value(this);
				}),
			NumberBox(w, 40@18).value_(this.maxval)
				.action_({|i|
					this.maxval_(i.value);
					this.init;
					this.warp.class.switch(
						CurveWarp, { this.warp.init(curve.value) }
					);
					action.value(this);
				}),
			PopUpMenu(w, 40@18).items_(Warp.warps.keys.asArray ++ [\curve])
				.value_(
					if (this.warp.class == CurveWarp) {
						Warp.warps.keys.asArray.size;
					}{
						Warp.warps.keys.asArray.indexOf(this.warp.asSpecifier)
					}
				)
				.action_({|i|
					try {this.warp.w.close};
					if (i.item == \curve) {
						curve.enabled_(true);
						this.warp_(curve.value.asWarp(this));
						this.init;
						this.warp.init(curve.value);
					}{
						curve.enabled_(false);
						this.warp_(i.item.asWarp(this));
						this.init;
						if (i.item == \array) {
							this.init;
							this.warp.init;
							this.warp.makeWindow(
								w.bounds.left + 290,
								w.bounds.top,
								action,
								name
							);
						}
					};
					action.value(this);
				}),
			NumberBox(w, 40@18).value_(this.step)
				.action_({|i|
					this.step_(i.value);
					this.warp.class.switch(
						ArrayWarp, {this.warp.step_}
					);
					action.value(this);
				}),
			NumberBox(w, 40@18).value_(this.default)
				.action_({|i|
					this.default_(i.value);
					action.value(this);
				}),
			TextField(w, 40@18).value_(this.units)
				.action_({|i|
					this.units_(i.value.asString);
					action.value(this);
				})
		];
		StaticText(w, 40@18);
		StaticText(w, 40@18);
		curve = NumberBox(w, 40@18).value_(2)
			.action_({|i|
				this.warp_(i.value.asWarp(this));
				this.init;
				this.warp.init(i.value);
				action.value(this);
			});
		this.warp.class.switch(
			CurveWarp, {
				curve.enabled_(true);
				curve.value_(this.warp.curve);
			},
			ArrayWarp, {
				curve.enabled_(false);
				this.warp.makeWindow(
						w.bounds.left + 290,
						w.bounds.top,
						action,
						name
					)
			},
			{curve.enabled_(false);}			
		);

		StaticText(w, 40@18);
		PopUpMenu(w, 85@18).items_(
			[\presets] ++ Spec.specs.select({|i|
				(i.class == ControlSpec)
			}).keys.asArray.sort
		)
			.value_(0)
			.action_({|i|
				var args;
				try {this.warp.w.close};
				if (i.value != 0) {
					// Default step = 0 is unfortunate, would be better 1
					args = i.item.asSymbol.asSpec.storeArgs;
					widgets.do({ |j,k|
						if (k == 2) {
							j.value_(
								Warp.warps.keys.asArray.indexOf(
									args[k]
								);
							);
							this.warp_(args[k].asWarp(this));
						}{
							j.valueAction_(args[k]);
						}
					});
				};
				action.value;
			});
		w.front;
		w.onClose_({
			try {this.warp.w.close};
		});
		^w;
	}
	constrain { arg value;
		^value.asFloat.round(step).clip(clipLo, clipHi)
	}
	map { arg value;
		// maps a value from [0..1] to spec range
		^warp.map(value.clip(0.0, 1.0)).round(step).clip(clipLo, clipHi);
	}
	increase { arg currentVal, increment, numSteps=1000;
		^warp.increase(
			currentVal, increment, numSteps
		).round(step).clip(clipLo, clipHi);
	}
	increment { arg currentVal, increment=1, numSteps=1000;
		^warp.increment(
			currentVal, increment, numSteps
		).round(step/this.range).clip(0.0, 1.0);
	}
}