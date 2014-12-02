/*
MiniIDENodeTree
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDENodeTree : MiniIDETab{
	classvar <tabLabel = \NodeTree;

	*new{|bounds|
		^super.new(bounds)
			.init();
	}

	init{
		//NODETREEVIEW
		Server.default.plotTree;
		Window.allWindows.do{|win|
			if((win.name == "internal Node Tree") || (win.name == "localhost Node Tree"),
				{view = win.view}
			)
		};
		//LAYOUT
		layout = VLayout(view);
	}
}