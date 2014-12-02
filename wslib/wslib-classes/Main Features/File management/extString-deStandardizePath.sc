// wslib 06

+ String {
	deStandardizePath { // replaces user folder by "~"
		var userFolder;
		userFolder = "~/".standardizePath;
		if( userFolder.asSymbol === this[..( userFolder.size - 1 ) ].asSymbol )
			{ ^"~/" ++ this[ userFolder.size .. ];  }
			{ ^this; }
		}
	}