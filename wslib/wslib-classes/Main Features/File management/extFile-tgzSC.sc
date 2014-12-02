+ File {
	*tgzSC { // make tgz archive of currently running version of sc, with build date for backup
		var cwd, date;
		this.deprecated( thisMethod );
		cwd = File.getcwd;
		date = File.getBuildDate; // only for wesleyan build
		date = date.replace(".", "_");
		if( date.size != 0 ) { date = "_" ++ date };
		^cwd.tgz( cwd ++ date );
		}
	}