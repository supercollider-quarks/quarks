+ Score { 
	// wslib 2007 : systemCmd version of recortnrt
	// returns result instead of itself, interrupts processes (not async like unixCmd)
	// Use this if you want SC to perform actions after the file is written.
	// - Will cause a "spinning beachball" during processing.
	// - doesn't post things in the post window
	
	recordNRTs { arg oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100, headerFormat =
		"AIFF", sampleFormat = "int16", options, completionString="", duration = nil;
		this.writeOSCFile(oscFilePath, 0, duration);
		^systemCmd(Server.program + " -N" + oscFilePath + (inputFilePath ? "_") + 
			"\""++outputFilePath++"\""
		 	+ sampleRate + headerFormat + sampleFormat +
			(options ? Score.options).asOptionsString
			+ completionString);
	}
	}