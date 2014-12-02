/*
QuNeoLongSlider by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeoLongSlider{
	var <noteNum, pressCC, locCC, widthCC, <name, <channel, <scInPort, <scOutPort;
	var <note, <press, <loc, <width;

	*new{|noteNum, pressCC, locCC, widthCC, name = \longSlider, channel = 0, scInPort, scOutPort|
		^super
			.newCopyArgs(noteNum, pressCC, locCC, widthCC, name, channel, scInPort, scOutPort)
			.init()
	}

	init{
		var notename = name ++ ': ' ++ \note;
		var pressName = name ++ ': ' ++ \press;
		var locName = name ++ ': ' ++ \loc;
		var widthName = name ++ ': ' ++ \width;
		note = MidiNoteFunc(noteNum, channel, scInPort, scOutPort, notename);
		press = MidiCcBus(pressCC, channel, scInPort, scOutPort, pressName);
		loc = MidiCcBus(locCC, channel, scInPort, scOutPort, locName);
		width = MidiCcBus(locCC, channel, scInPort, scOutPort, widthName);
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		note.debug(setDebug);
		press.debug(setDebug);
		loc.debug(setDebug);
		width.debug(setDebug);
	}
}