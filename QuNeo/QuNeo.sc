/*
QuNeo by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeo{
	var <defaultChannel, <scInPort, <scOutPort, <pads, <hSliders, <vSliders, <lSliders, <buttons, <circles;

	*new{|defaultChannel = 0, scInPort, scOutPort|
		//If scInPort and scOutPort are left blank they will be detected during init.
		^super
			.newCopyArgs(defaultChannel, scInPort, scOutPort)
		    .init()
	}

	init{
		//Connect to MIDI sources if noone has bothered to do it yet.
		if(MIDIClient.sources.isNil, {MIDIIn.connect});
		//Find QUNEO in and out ports if not specified during creation.
		if(scInPort.isNil, {scInPort = this.detectInPort});
		if(scOutPort.isNil, {scOutPort = this.detectOutPort});
		//Create control arrays
		pads = Array.newClear(16);
		hSliders = Array.newClear(16);
		vSliders = Array.newClear(16);
		lSliders = Array.newClear(4);
		buttons = Array.newClear(16);
		circles = Array.newClear(8);
		this.postInfo;
	}

	postInfo{
		''.postln;
		('QuNeo: In - ' ++ scInPort ++ ' / Out - ' ++ scOutPort).postln;
		(Char.tab ++ 'Pads - ' ++ pads.size).postln;
		(Char.tab ++ 'Horizontal Sliders - ' ++ hSliders.size).postln;
		(Char.tab ++ 'Vertical Sliders - ' ++ vSliders.size).postln;
		(Char.tab ++ 'Long Sliders - ' ++ lSliders.size).postln;
		(Char.tab ++ 'Buttons - ' ++ buttons.size).postln;
		(Char.tab ++ 'Circles - ' ++ circles.size).postln;
		''.postln;
	}

	detectInPort{^(MIDIClient.sources.detect({|item| item.device.find("QUNEO").notNil}) !? _.uid ? 0);}

	detectOutPort{^(MIDIClient.destinations.detect({|item| item.device.find("QUNEO").notNil}) !? _.uid ? 0);}

	createDrumPad{|index, noteNum, pressCC, xCC, yCC, channel|
		var name = \drumPad ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		pads = pads.put(index, QuNeoDrumPad(noteNum, pressCC, xCC, yCC, name, channel, scInPort, scOutPort));
	}

	createHSlider{|index, noteNum, pressCC, locCC, channel = 0, scInPort, scOutPort|
		var name = \hSlider ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		hSliders = hSliders.put(index, QuNeoSlider(noteNum, pressCC, locCC, name, channel, scInPort, scOutPort));
	}

	createVSlider{|index, noteNum, pressCC, locCC, channel = 0, scInPort, scOutPort|
		var name = \vSlider ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		vSliders = vSliders.put(index, QuNeoSlider(noteNum, pressCC, locCC, name, channel, scInPort, scOutPort));
	}

	createLSlider{|index, noteNum, pressCC, locCC, widthCC, channel = 0, scInPort, scOutPort|
		var name = \longSlider ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		lSliders = lSliders.put(index, QuNeoLongSlider(noteNum, pressCC, locCC, widthCC, name, channel, scInPort, scOutPort));
	}

	createButton{|index, noteNum, pressCC, channel = 0, scInPort, scOutPort|
		var name = \button ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		buttons = buttons.put(index, QuNeoButton(noteNum, pressCC, name, channel, scInPort, scOutPort));
	}

	createCircle{|index, noteNum, pressCC, locCC, channel = 0, scInPort, scOutPort|
		var name = \circle ++ '_' ++ index;
		if(channel.isNil, {channel = defaultChannel});
		circles = circles.put(index, QuNeoCircleControl(noteNum, pressCC, locCC, name, channel, scInPort, scOutPort));
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		pads.do{|pad| if(pad.notNil, {pad.debug(setDebug)})};
		hSliders.do{|slider| if(slider.notNil, {slider.debug(setDebug)})};
		vSliders.do{|slider| if(slider.notNil, {slider.debug(setDebug)})};
		lSliders.do{|slider| if(slider.notNil, {slider.debug(setDebug)})};
		buttons.do{|button| if(button.notNil, {button.debug(setDebug)})};
		circles.do{|circle| if(circle.notNil, {circle.debug(setDebug)})};
	}
}