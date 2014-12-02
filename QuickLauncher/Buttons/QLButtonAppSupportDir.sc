/*
QLButtonAppSupportDir
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButtonAppSupportDir : QLButton {

	*new{
		var showButtonFunc = {
			Platform.userAppSupportDir.openOS;
		};

		^super
			.newCopyArgs("App Support Dir", false, 0@0, showButtonFunc)
			.createButton();
	}

	//Since this button does not open a supercollider window override logic so this is not a toggle button
	createButton{
		var	states = [[name, Color.white, Color.grey]];
		var action = {|butt| showButtonFunc.value()};
		button = Button()
			.states_(states)
			.action_(action);
	}
}