/*
MiniIDGlobals
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDEGlobals : MiniIDETab{
	classvar <tabLabel = \Globals;

	*new{|bounds|
		^super
			.new(bounds)
			.init();
	}

	init{
		layout = VLayout(
			GlobalsGui(40, bounds: 490@100).parent.view
		);
	}
}