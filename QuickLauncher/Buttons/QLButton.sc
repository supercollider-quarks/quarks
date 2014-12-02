/*
QLButton
by Jonathan Siemasko
Contact: schemawound@yahoo.com
Homepage: http://schemawound.com/
*/

QLButton{
	var <>name, <>alwaysOnTop, <>bounds, <>showButtonFunc, <button, <window;

	*new{|name  = "Button", alwaysOnTop = false, bounds, showButtonFunc|
		bounds = bounds ? Point(50, 20);

		^super
			.newCopyArgs(name, alwaysOnTop, bounds, showButtonFunc)
			.createButton();
	}

	closeWindow{if (window != nil, {button.value = 0; window.close; window = nil})}

	setWindowOptions{
		//Some windows have some onclose logic that causes problems if it gets overwritten.  Appending my function to the end of the current function.
		var currOnCloseFunction = window.onClose;
		window
			.bounds_(bounds)
			.alwaysOnTop_(alwaysOnTop)
			.onClose_({currOnCloseFunction.value(); this.closeWindow()}); 
	}

	createButton{
		var	states = [[name, Color.white, Color.grey],[name, Color.white, Color.red]];
		var action = {|butt| if(butt.value == 1,
			{
				window = showButtonFunc.value();
				this.setWindowOptions();
			},
			{this.closeWindow()}
		)};
		button = Button()
			.states_(states)
			.action_(action);
	}
}