/*
MiniIDEHistory
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDEHistory : MiniIDETab{
	classvar <tabLabel = \History;

	*new{|bounds|
		^super
			.new(bounds)
			.init();
	}

	init{
		var historyWindow, historyEditBox, historyListBox, historyWidthOffset = 20, historyHeightOffset = 200;
		//WINDOW
		historyWindow = History
			.start
			.makeWin.w.view;
		view = window;
		historyWindow.bounds = bounds;
		//Resize history window pieces in order to fill tab
		historyEditBox = historyWindow.children[0];
		historyEditBox.bounds_(
			Rect(
				historyEditBox.bounds.left,
				historyEditBox.bounds.top,
				bounds.width - historyWidthOffset,
				historyEditBox.bounds.height
			)
		);
		historyListBox = historyWindow.children[8];
		historyListBox.bounds_(
			Rect(
				historyListBox.bounds.left,
				historyListBox.bounds.top,
				bounds.width - historyWidthOffset,
				bounds.height - historyHeightOffset
			)
		);
		layout  = VLayout(historyWindow);
	}
}