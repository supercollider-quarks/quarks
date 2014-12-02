//redFrik

//see RedGUI help for examples

RedGUI {
	*initClass {
		StartUp.add({
			GUI.skins.put(\redFrik, (
				background: Color.red.alpha_(0.8),
				foreground: Color.black,
				selection: Color.grey,
				unfocus: 0.9,
				fontSpecs: ["Monaco", 9],
				offset: 0@0
			));
		});
	}
}
Red2DSlider {
	*new {|parent, bounds|
		bounds= bounds ?? {100@100};
		^Slider2D(parent, bounds)
			.knobColor_(GUI.skins.redFrik.foreground)
			.background_(GUI.skins.redFrik.background);
	}
}
RedButton {
	*new {|parent, bounds ...str|
		var fnt= RedFont.new;
		if(str.isEmpty, {str= [""]});
		bounds= bounds ?? {(str.maxValue{|x| x.bounds(fnt).width}+14)@14};
		^Button(parent, bounds)
			.states_(
				str.collect{|x, i|
					var cols= [GUI.skins.redFrik.foreground, GUI.skins.redFrik.background];
					[x, cols.wrapAt(i), cols.wrapAt(i+1)];
				}
			)
			.font_(fnt);
	}
}
RedFont {
	*new {
		^Font(*GUI.skins.redFrik.fontSpecs);
	}
}
RedKnob {
	*new {|parent, bounds|
		bounds= bounds ?? {48@48};
		^Knob(parent, bounds)
			.color_([
				GUI.skins.redFrik.background,
				GUI.skins.redFrik.foreground,
				GUI.skins.redFrik.background,
				GUI.skins.redFrik.foreground
			]);
	}
}
RedLevelIndicator {
	*new {|parent, bounds|
		bounds= bounds ?? {100@14};
		^LevelIndicator(parent, bounds)
			.style_(1)
	}
}
RedMultiSliderView {
	*new {|parent, bounds|
		bounds= bounds ?? {100@14};
		^MultiSliderView(parent, bounds)
			.background_(GUI.skins.redFrik.background)
			.strokeColor_(GUI.skins.redFrik.foreground)
			.fillColor_(GUI.skins.redFrik.foreground);
	}
}
RedNumberBox {
	*new {|parent, bounds|
		bounds= bounds ?? {36@14};
		^NumberBox(parent, bounds)
			.value_(0)
			.background_(GUI.skins.redFrik.background)
			.typingColor_(GUI.skins.redFrik.foreground)
			.font_(RedFont.new);
	}
}
RedPopUpMenu {
	*new {|parent, bounds|
		bounds= bounds ?? {74@14};
		^PopUpMenu(parent, bounds)
			.background_(GUI.skins.redFrik.background)
			.stringColor_(GUI.skins.redFrik.foreground)
			.font_(RedFont.new);
	}
}
RedPopUpTreeMenu {
	*new {|parent, bounds|
		bounds= bounds ?? {74@14};
		^PopUpTreeMenu(parent, bounds)
			.background_(GUI.skins.redFrik.background)
			.stringColor_(GUI.skins.redFrik.foreground)
			.font_(RedFont.new);
	}
}
RedSlider {
	*new {|parent, bounds|
		bounds= bounds ?? {100@14};
		^Slider(parent, bounds)
			.knobColor_(GUI.skins.redFrik.foreground)
			.background_(GUI.skins.redFrik.background);
	}
}
RedStaticText {
	*new {|parent, string, bounds|
		var fnt= RedFont.new;
		bounds= bounds ?? {string.bounds(fnt)};
		^StaticText(parent, bounds)
			.string_(string)
			.stringColor_(GUI.skins.redFrik.foreground)
			.font_(fnt);
	}
}
RedTextView {
	*new {|parent, bounds|
		bounds= bounds ?? {100@14};
		^TextView(parent, bounds)
			.background_(GUI.skins.redFrik.background)
			.stringColor_(GUI.skins.redFrik.foreground)
			.font_(RedFont.new);
	}
}
