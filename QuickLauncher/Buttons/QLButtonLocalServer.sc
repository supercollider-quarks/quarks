/*
QLButtonLocalServer
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonLocalServer : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			Server.local.makeWindow();
			Server.local.window;
		};
		
		bounds = bounds ? Point(300, 100);

		^super
			.newCopyArgs("Local Server", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}