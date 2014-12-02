/*
QuNeoSlider by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeoSlider{
	var <noteNum, pressCC, locCC, <name, <channel, <scInPort, <scOutPort;
	var <note, <press, <loc;

	*new{|noteNum, pressCC, locCC, name = \slider, channel = 0, scInPort, scOutPort|
		^super
			.newCopyArgs(noteNum, pressCC, locCC, name, channel, scInPort, scOutPort)
			.init()
	}

	init{
		var notename = name ++ ': ' ++ \note;
		var pressName = name ++ ': ' ++ \press;
		var locName = name ++ ': ' ++ \loc;
		note = MidiNoteFunc(noteNum, channel, scInPort, scOutPort, notename);
		press = MidiCcBus(pressCC, channel, scInPort, scOutPort, pressName);
		loc = MidiCcBus(locCC, channel, scInPort, scOutPort, locName);
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		note.debug(setDebug);
		press.debug(setDebug);
		loc.debug(setDebug);
	}
}