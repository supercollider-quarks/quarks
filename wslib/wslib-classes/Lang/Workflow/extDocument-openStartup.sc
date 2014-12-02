+ Document {

	*openStartup { ^thisProcess.platform.startupFiles // open all existing startup files
			.collect( _.pathMatch ).flatten(1)
			.collect( Document.open(_) ); 
			}
	}