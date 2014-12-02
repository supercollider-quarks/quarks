/*
QuNeoGridPad by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/
*/

QuNeoGridPad{
	var <name, <channel, <scInPort, <scOutPort, <press, <noteNum;

	*new{|noteNums, pressCCs, name = \gridPad, channel = 0, scInPort, scOutPort|
		^super
			.newCopyArgs(name, channel, scInPort, scOutPort)
			.init(noteNums, pressCCs)
	}

	init{|noteNums, pressCCs|
		var noteName1 = name ++ ': ' ++ \note1;
		var noteName2 = name ++ ': ' ++ \note2;
		var noteName3 = name ++ ': ' ++ \note3;
		var noteName4 = name ++ ': ' ++ \note4;
		var pressName1 = name ++ ': ' ++ \press1;
		var pressName2 = name ++ ': ' ++ \press2;
		var pressName3 = name ++ ': ' ++ \press3;
		var pressName4 = name ++ ': ' ++ \press4;
		noteNum = [
			MidiNoteFunc(noteNums[0], channel, scInPort, scOutPort, pressName1),
			MidiNoteFunc(noteNums[1], channel, scInPort, scOutPort, pressName2),
			MidiNoteFunc(noteNums[2], channel, scInPort, scOutPort, pressName3),
			MidiNoteFunc(noteNums[3], channel, scInPort, scOutPort, pressName4),
		];
		press = [
			MidiCcBus(pressCCs[0], channel, scInPort, scOutPort, pressName1),
			MidiCcBus(pressCCs[1], channel, scInPort, scOutPort, pressName2),
			MidiCcBus(pressCCs[2], channel, scInPort, scOutPort, pressName3),
			MidiCcBus(pressCCs[3], channel, scInPort, scOutPort, pressName4),
		];
	}

	//Set debug values for all underlying controls
	debug{|setDebug = true|
		noteNum.do{|thisNote| thisNote.debug(setDebug)};
		press.do{|thisPress| thisPress.debug(setDebug)};
	}
}