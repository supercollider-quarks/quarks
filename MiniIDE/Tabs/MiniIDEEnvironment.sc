/*
MiniIDEEnvironment
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

MiniIDEEnvironment : MiniIDETab{
	classvar <tabLabel = \Environment;

	*new{|bounds|
		^super
			.new(bounds)
			.init();
	}

	init{
		layout = VLayout(
			EnvirGui(currentEnvironment, 42, bounds: 490@100).parent.view
		);
	}
}