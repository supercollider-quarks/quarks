/*
QuNeoDrumPad by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeoDrumPad{
	var <noteNum, pressCC, xCC, yCC, <name, <channel, <scInPort, <scOutPort;
	var <note, <press, <x, <y;

	*new{|noteNum, pressCC, xCC, yCC, name = \drumPad, channel = 0, scInPort, scOutPort|
		^super
			.newCopyArgs(noteNum, pressCC, xCC, yCC, name, channel, scInPort, scOutPort)
			.init()
	}

	init{
		var notename = name ++ ': ' ++ \note;
		var pressName = name ++ ': ' ++ \press;
		var xName = name ++ ': ' ++ \x;
		var yName = name ++ ': ' ++ \y;
		note = MidiNoteFunc(noteNum, channel, scInPort, scOutPort, notename);
		press = MidiCcBus(pressCC, channel, scInPort, scOutPort, pressName);
		x = MidiCcBus(xCC, channel, scInPort, scOutPort, xName);
		y = MidiCcBus(yCC, channel, scInPort, scOutPort, yName);
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		note.debug(setDebug);
		press.debug(setDebug);
		x.debug(setDebug);
		y.debug(setDebug);
	}
}