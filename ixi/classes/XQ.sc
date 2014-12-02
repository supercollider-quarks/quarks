
// XQ is the storage for all environmental variables for ixiQuarks

/*
Testing:
XQ.globalBufferDict
XQ.globalWidgetList

// get all active pools
XQ.poolNames

// buffers
XQ.buffers(XQ.poolNames[0]) 	// you need to know the poolname
// selections (contains [selStart, numFrames])
XQ.selections(XQ.poolNames[0]) 	// you need to know the poolname
// buffers and selections
XQ.bufferList(XQ.poolNames[0]) 	// you need to know the poolname
// example: the bufnum of the first buffer in the first bufferpool (in alphabetic order)
XQ.buffers(XQ.poolNames[0])[0].bufnum

XQ.bufferPoolNum
XQ.pref
XQ.pref.midi
*/

XQ {
	classvar <>globalWidgetList, <>globalBufferDict, <>bufferPoolNum;
	classvar <>pref;
	
	*new{
		^super.new.initXQ;
	}
	
	initXQ{
		var soundsfolderpath, preferencesfolderpath;
		globalWidgetList = List.new; // keep track of active widgets
		// (contains [List [buffers], [selstart, sellength]])
		globalBufferDict = (); 
		bufferPoolNum = -1;
		// check if there is a sounds folder
		soundsfolderpath = String.scDir++"/sounds/ixiquarks";
		if(soundsfolderpath.pathMatch==[], {
			("mkdir -p" + soundsfolderpath.quote).unixCmd;
			"ixi-NOTE: an ixiquarks soundfolder was not found, it was created in sounds".postln;
		});
		preferencesfolderpath = String.scDir++"/ixiquarks/preferences";
		if(preferencesfolderpath.pathMatch==[], {
			("mkdir -p" + preferencesfolderpath.quote).unixCmd;
			"ixi-NOTE: an ixiquarks preferences folder was not found, it was created".postln;
		});
	}

	// methods for accessing poolnames, buffers and selections. good for live-coding
	// available pools
	*poolNames {
		^globalBufferDict.keys.asArray.sort;
	}

	// bufferlist with buffers AND selections
	*bufferList {arg poolname;
		if(poolname.isString, {poolname = poolname.asSymbol});
		if(globalBufferDict.at(poolname).notNil, {
			^globalBufferDict.at(poolname);
		},{
			"no bufferpool with that name (bufferList)".warn;
			^[];
		});
	}
	
	// buffers only (not selections)
	*buffers {arg poolname;
		if(poolname.isString, {poolname = poolname.asSymbol});
		if(globalBufferDict.at(poolname).notNil, {
			^globalBufferDict.at(poolname)[0];
		},{
			"no bufferpool with that name (buffer)".warn;
			^[];
		});
	}

	// selections only (not buffers)
	*selections {arg poolname;
		if(poolname.isString, {poolname = poolname.asSymbol});
		if(globalBufferDict.at(poolname).notNil, {
			^globalBufferDict.at(poolname)[1];
		},{
			"no bufferpool with that name (selections)".warn;
			^[];
		});
	}

	// the basenames of the buffers in the bufferlist
	*bufferNames {arg poolname;
		if(poolname.isString, {poolname = poolname.asSymbol});
		if(globalBufferDict.at(poolname).notNil, {
			^globalBufferDict.at(poolname)[0].collect({arg buffer; buffer.path.basename});
		},{
			"no bufferpool with that name (bufferNames)".warn;
			^[];
		});
	}
	
	*widgetsToFront {
		globalWidgetList.do({|widget| widget.win.front;});
	}

	// read in the preference file and if it's not there, use default preferences
	*preferences {
		var prefFile, preferences;
		try{
			prefFile = File("ixiquarks/preferences/preferences.ixi", "r");
			preferences = prefFile.readAllString;
			preferences.interpret;
		} {

		"ixi-NOTE: you don't have the preferences.ixi file installed! Check ixiQuarks help".postln;
			this.pref = ()
				.emailSent_(true) // change to true when you have sent ixi an email
				// .sampleRate_(44100) // gets from server
				.bitDepth_("int16") // possible: "int16", "int24", "int32"
				.numberOfChannels_(52) // number of audio channels used
				.polyMachineTracks_(6) // how many tracks in polymachine (4 is default)
				.bufferPlayerTracks_(16) // tracks in BufferPlayer (8, 16, 24 and 32 being ideal)
				.midi_(false) // using midi or not?
				.midiControllerNumbers_( [73, 72, 91, 93, 74, 71, 5, 84, 7] ) // evolution mk-449c
				.midiControllerNumbers_( [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14] ) // doepfer pocket 
				.midiRotateWindowChannel_(15) // the controller number to switch between windows
				.midiInPorts_( 2 ) // how many inports you are using
				.midiOutPorts_( 2 ); // how many outports - not used really yet
		}
	}

}