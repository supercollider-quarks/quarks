

Midi2Freq : KrPlayer {
	// players or floats will work

	*new { arg note,octave=5.0;
		^Patch(UGenInstr(Midi2FreqUGen,\kr),[ note,octave ])
	}
	// storeArgs { ^[note,octave] }
	
	//TODO
	//asStream
	//guiClass { ^Midi2FreqGui }
}


/* 
Midi2FreqGui : KrPlayerGui {

	guiBody { arg layout;
		CXLabel(layout.startRow,"note:");
		model.note.gui(layout);
		CXLabel(layout.startRow,"octave:");
		model.octave.gui(layout);
	}

}

*/

