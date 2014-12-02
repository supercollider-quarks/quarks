/*
QuNeoButton by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeoButton{
	var <noteNum, pressCC, <name, <channel, <scInPort, <scOutPort;
	var <note, <press;

	*new{|noteNum, pressCC, name = \button, channel = 0, scInPort, scOutPort|
		^super
			.newCopyArgs(noteNum, pressCC, name, channel, scInPort, scOutPort)
			.init()
	}

	init{
		var notename = name ++ ': ' ++ \note;
		var pressName = name ++ ': ' ++ \press;
		note = MidiNoteFunc(noteNum, channel, scInPort, scOutPort, notename);
		press = MidiCcBus(pressCC, channel, scInPort, scOutPort, pressName);
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		note.debug(setDebug);
		press.debug(setDebug);
	}
}