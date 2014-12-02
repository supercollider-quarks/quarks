/*
QLButtonInternalServer
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonInternalServer : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			Server.internal.makeWindow();
			Server.internal.window;
		};
		
		bounds = bounds ? Point(300, 100);

		^super
			.newCopyArgs("Internal Server", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}