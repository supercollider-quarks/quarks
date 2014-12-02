/*
QLButtonSynthDef
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonSynthDef : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			var window;
			SynthDescLib.global.read;
			SynthDescLib.global.browse;
			Window.allWindows.do{|win|
				if(win.name == "SynthDef browser",
					{window = win}
				)
			};
			window
		};

		bounds = bounds ? Point(717, 805);

		^super
			.newCopyArgs("SynthDef", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}