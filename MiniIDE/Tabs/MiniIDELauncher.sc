/*
MiniIDELauncher
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDELauncher : MiniIDETab{
	classvar
	    <tabLabel = \Launcher;
		
	*new{|bounds, userButtonsDef, colSize = 2|
		^super
			.new(bounds)
			.init(userButtonsDef, colSize)
	}

	init{|userButtonsDef, colSize|
		var ql;
		ql = QuickLauncher(bounds: bounds, colSize: colSize);
		layout = ql.layout;
	}
}