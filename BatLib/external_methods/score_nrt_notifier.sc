+ Score
{
	recordNRTnotify
	{ 
		arg oscFilePath, outputFilePath, inputFilePath, sampleRate = 44100, headerFormat =
		"AIFF", sampleFormat = "int16", options, completionString="", duration = nil, notifyFunc;
		
		this.writeOSCFile(oscFilePath, 0, duration);
		
		(program + " -N" + oscFilePath + (inputFilePath ? "_") + "\""++outputFilePath++"\""
		 	+ sampleRate + headerFormat + sampleFormat +
			(options ? Score.options).asOptionsString
			+ completionString).unixCmd(notifyFunc);
	}
}