/*
MiniIDETab
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDETab{
	var <bounds, <>window, <view, layout;
	classvar <tabLabel = \Tab;

	*new{|bounds|
		var newTab;

		//Default bounds and convert to rect if it is a point
		if(bounds == nil,
			{bounds = Rect(0,0,512,805)}
		);
		if(bounds.class == Point,
			{bounds = bounds.asRect}
		);

		newTab = super.newCopyArgs(bounds);
		newTab.window = Window.new(bounds: bounds);

		^newTab;
	}

	layout {
		^layout
	}
	layout_ {|value|
		layout = value;
		window.layout_(value);
	}

}