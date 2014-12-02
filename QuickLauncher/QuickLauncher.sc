/*
QuickLauncher
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QuickLauncher {
	var bounds, userButtonsDef, colSize, <window, <layout, userButtons;

	*new{|bounds, userButtonsDef, colSize = 3|
		var window, layout, userButtons;
		bounds = bounds ? Point(200, 100);
		userButtonsDef = userButtonsDef ? [QLButtonLocalServer(), QLButtonInternalServer(), QLButtonEQ(), QLButtonClassBrowser(), QLButtonSynthDef(), QLButtonNdefMixer(), QLButtonProxyMixer(), QLButtonAppSupportDir(), QLButtonClassLibraryDir()];
		userButtons = userButtonsDef collect: _.button;
		layout = GridLayout();
		window = Window(bounds: bounds)
					.onClose_({userButtonsDef do: _.closeWindow })
					.layout_(layout);
		layout.add()
		^super
			.newCopyArgs(bounds, userButtonsDef, colSize, window, layout, userButtons)
			.fillLayout();
	}

	fillLayout{
		var rowSize;
		rowSize = (userButtons.size / colSize).ceil; //Round up to avoid truncation
		for(0, rowSize - 1, {|rowCount|
			for (0, colSize - 1, {|colCount|
				layout.add(userButtons[(rowCount * colSize) + colCount], rowCount, colCount);
			});

		});
	}

	front{window.front}
}