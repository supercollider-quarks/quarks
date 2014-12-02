+ SpatialRender {

	initSpatDifLogger { |mySpatDifLoggerPath|

		// place here the desired spatdif meta info

		var fileName;
		var extensions;
		var author,host,date,session,location,annotation;

		fileName = "TimeFileLog" ++ "_" ++ Date.localtime.stamp ++ ".txt";

		extensions = ["jack","distance-cues"];

		// author =
		host = "SCLiss:SpatialRender";
		date = Date.getDate.format("%Y-%m-%d");
		// session =
		// location =
		// annotation =


		// cross-os compatibility
		spatDifLoggerPath = mySpatDifLoggerPath ?? {
			var string = thisProcess.platform.userExtensionDir;
			string[..string.findBackwards("/")]++"SpatialRender"
		};

		("mkdir "++ spatDifLoggerPath).systemCmd; //just in case

		spatDifLogger = SpatDifLogger.new(fileName,spatDifLoggerPath,extensions,author,host,date,session,location,annotation);
		spatDifLogger.log_(logOSC);

		// create a routine that auto-saves the spatdif logger in a regular way
		autosaveTask = Task({
			inf.do({
				spatDifLogger.save;
				autosavePeriod.wait;
			})
		});
		if (autosaveOSC) {autosaveTask.start};
	}


}