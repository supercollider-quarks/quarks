/*
QuickLauncherEQDef
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonEQ : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			MasterEQ(2).window
		};
		
		bounds = bounds ? Point(305, 220);

		^super
			.newCopyArgs("EQ", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}