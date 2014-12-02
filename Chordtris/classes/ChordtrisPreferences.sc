ChordtrisPreferences {
	
	// Dictionary containing symbols as keys and instances of ChordtrisPreferenceSetting as values
	classvar preferences;
	
	// the Chordtris library folder
	classvar preferenceFolder;
	
	// the path to the file
	classvar preferenceFile;
	
	*initClass {
		//libraryFolder = "~/Library/Application\ Support/Chordtris".standardizePath;
		preferenceFolder = Platform.userAppSupportDir;
		preferenceFile = preferenceFolder +/+ "Chordtris.preferences";
	}
		
	*getPreferences {
		if(preferences.isNil)
		{
			preferences = this.loadPreferences;
		};
		
		^preferences;
	}
	
	*loadPreferences {
		var file;
		var fileContents;
		var loadedPreferences = this.getDefaultPreferences;
		var valueMap;
		
		// check if the application support folder exists
		if(File.exists(preferenceFolder).not) {
			^loadedPreferences;
		};
		
		// check if the preferences file exists
		if(File.exists(preferenceFile).not) {
			^loadedPreferences;
		};

		// read the preferences file
		file = File.open(preferenceFile, "r");
		fileContents = file.readAllString;
		file.close;
		
		valueMap = fileContents.interpret;
		
		// overwrite actual values in the default map
		valueMap.keysValuesDo { |key, val|
			loadedPreferences.at(key).value = val;
		}
		
		^loadedPreferences;
				
	}
	
	*savePreferences {
		var file;
		var mapToSave = Dictionary.new;
		if(File.exists(preferenceFolder).not) {
			File.mkdir(preferenceFolder);
		};
		
		file = File.open(preferenceFile, "w");
		
		this.getPreferences.keysValuesDo { |key, setting|
			mapToSave.put(key, setting.value);
		};
		file.write(mapToSave.asCompileString.asSymbol);
		file.close;
	}
	
	*getDefaultPreferences {
		var p = Dictionary.new;
		
		p.put(\chordNameLanguage, ChordtrisPreferenceSetting("Chord Name Language", \string, "English", ["English", "Deutsch"]));
		p.put(\musicVolume, ChordtrisPreferenceSetting("Music Volume", \float, 0.4, [0, 1], 0.1));
		p.put(\soundVolume, ChordtrisPreferenceSetting("Sound Volume", \float, 0.3, [0, 1], 0.1));
		p.put(\keyboardVolume, ChordtrisPreferenceSetting("Keyboard Volume", \float, 0.5, [0, 1], 0.1));
		p.put(\chordDetectionTime, ChordtrisPreferenceSetting("Chord Detection Time", \float, 0.05, [0.01, 0.1], 0.01));
		
		^p;
	}
}