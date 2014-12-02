/*
QLButtonProxyMixer
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonProxyMixer : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			ProxyMixer.new(nil, 25).parent
		};
		
		bounds = bounds ? Point(684, 506);

		^super
			.newCopyArgs("Proxy Mixer", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}