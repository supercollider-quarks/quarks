
XiiTestMIDI {

	*new{
	var document;
	var inPorts = 2;
	var outPorts = 2;
	MIDIClient.init(inPorts,outPorts);			// explicitly intialize the client
	MIDIIn.noteOff = { arg src, chan, num, vel; 	[chan,num,vel / 127].postln; };
	MIDIIn.noteOn = { arg src, chan, num, vel; 	[chan,num,vel / 127].postln; };
	MIDIIn.polytouch = { arg src, chan, num, vel; 	[chan,num,vel / 127].postln; };
	MIDIIn.control = { arg src, chan, num, val; 	[chan,num,val].postln; };
	MIDIIn.program = { arg src, chan, prog; 		[chan,prog].postln; };
	MIDIIn.touch = { arg src, chan, pressure; 	[chan,pressure].postln; };
	MIDIIn.bend = { arg src, chan, bend; 			[chan,bend - 8192].postln; };
	MIDIIn.sysex = { arg src, sysex; 			sysex.postln; };
	MIDIIn.sysrt = { arg src, chan, val; 			[chan,val].postln; };
	MIDIIn.smpte = { arg src, chan, val; 			[chan,val].postln; };

	inPorts.do({ arg i; 
		MIDIIn.connect(i, MIDIClient.sources.at(i));
	});

	document = Document.new("MIDI info", "// the following is your midi info", true);
	}
}