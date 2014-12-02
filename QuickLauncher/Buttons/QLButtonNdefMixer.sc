/*
QLButtonNdefMixer
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonNdefMixer : QLButton {
	
	*new{|alwaysOnTop = false, bounds|
		var showButtonFunc = {
			NdefMixer.new(Ndef.all.choose, 25).parent
		};
		
		bounds = bounds ? Point(684, 506);

		^super
			.newCopyArgs("Ndef Mixer", alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}
}