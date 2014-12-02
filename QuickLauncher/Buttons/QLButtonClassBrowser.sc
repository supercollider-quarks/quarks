/*
QLButtonClassBrowser
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonClassBrowser : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			var window;
			Object.browse;
			Window.allWindows.do{|win|
				if(win.name == "class browser",
					{window = win}
				)
			};
			window
		};
		
		bounds = bounds ? Point(717, 805);

		^super
			.newCopyArgs("Class", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}