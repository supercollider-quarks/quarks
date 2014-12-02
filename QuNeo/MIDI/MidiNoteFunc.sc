/*
MidiNoteFunc by Jonathan Siemasko - Email: schemawound@yahoo.com - Web: http://schemawound.com/

This control registers a function for note on and note off message
*/

MidiNoteFunc{
	var <noteNum, <channel, <scInPort, <scOutPort, <name;
	var displayDebugInfo, <>onFunc, <>offFunc;

	*new{|noteNum, channel = 0, scInPort, scOutPort, name|
		^super
			.newCopyArgs(noteNum, channel, scInPort, scOutPort, name)
			.init()
	}

	init{
		if(noteNum.notNil,
			{
				//Register On Function
				onFunc = {};
				if(noteNum.notNil, {
					MIDIdef.noteOn(name ++ \On,
						{|val, num, chan, src|
							if (chan == channel, {
								onFunc.value(val, num, chan, src, name, displayDebugInfo); //velocity and midi note are passed to the function
								if(displayDebugInfo == true, {(name ++ \On ++ ': NoteNum: ' ++ num ++ ' - Vel: ' ++ val).postln;});
							});
						},
						noteNum: noteNum,
						chan: channel
						//, srcID: scInPort
					).permanent = true;
				});

				//Register Off Function
				offFunc = {};
				if(noteNum.notNil, {
					MIDIdef.noteOff(name ++ \Off,
						{|val, num, chan, src|
							if (chan == channel, {
								offFunc.value(val, num); //velocity and midi note are passed to the function
								if(displayDebugInfo == true, {(name ++ \Off ++ ': NoteNum: ' ++ num ++ ' - Vel: ' ++ val).postln;});
							});
						},
						noteNum: noteNum,
						chan: channel
						//, srcID: scInPort
					).permanent = true;
				});
			}
		);
	}

	//Set to display debug values to the console
	debug{|setDebug = true| displayDebugInfo = setDebug;}
}