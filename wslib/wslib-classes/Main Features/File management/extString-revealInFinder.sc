+ String {

	revealInFinder {
		var path;
		path = this.standardizePath;
		
		if( path[0] != $/ ) { path = String.scDir +/+ path };
		
		"tell application \"Finder\"
			activate
			reveal POSIX file %
		end tell".format( path.quote ).appleScript; //.asAppleScriptCmd.postln.unixCmd;
		}
	
	}