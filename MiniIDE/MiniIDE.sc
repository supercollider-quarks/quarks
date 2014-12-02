/*
MiniIDE
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDE{
	var userButtonsDef, showPositionButton, showGlobalButtons, <window, <view, point;

	*new{|tabs, userButtonsDef, bounds, alwaysOnTop = true, showPositionButton = true, showGlobalButtons = true|
		//userButtonsDef expects an array of MiniIDELauncherButtonDef that will be passed to the MiniIDELauncher
		^super
			.newCopyArgs(userButtonsDef, showPositionButton, showGlobalButtons)
			.init(tabs, bounds, alwaysOnTop);
	}

	init{|tabs, bounds, alwaysOnTop|
		//If no bounds are passed then set default
		//Get a point copy of the bounds to pass to the tabs
		var point;
		if(tabs == nil, {tabs = [MiniIDEScope, MiniIDENodeTree, MiniIDEGlobals, MiniIDEEnvironment, MiniIDEHistory, MiniIDEJIT, MiniIDELauncher, MiniIDEHelp]});
		if(bounds == nil, {bounds = Rect(0,0,512,805)});
		if(bounds.class == Point, {bounds = bounds.asRect});  //Convert Point to Rect
		point = Point(bounds.width, bounds.height);
		//WINDOW
		window = Window.new(
			name: "MiniIDE",
			bounds: bounds,
			resizable: false,
			border: true,
			scroll: false
		).alwaysOnTop_(alwaysOnTop);
		//TABS
		view = TabbedView
			.newColor(
				parent:window,
				bounds:point,
				labels:tabs.collect{|tab| tab.tabLabel},
			    name:"MiniIDE",
				scroll:false)
			.tabPosition_(\bottom)
		    .stringColor_(Color.black)
		    .stringFocusedColor_(Color.grey)
		    .labelPadding_(12);
		tabs.do{|tabType, tabNum|
			this.setTabType(tabNum, tabType)
		};
	}

	setTabType{|tabNum, tabType|
		view.views[tabNum].layout_(tabType.new(bounds: point).layout);
	}

	front{window.front}
}