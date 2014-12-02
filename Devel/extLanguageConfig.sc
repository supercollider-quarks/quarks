


+ LanguageConfig {

	*clear {
		this.includePaths.do(this.removeIncludePath(_));
		this.excludePaths.do(this.removeExcludePath(_));
	}

	*add {  |configFileName, dir|
		var configFilePath = (dir ? Platform.userConfigDir) +/+ "sclang_conf_%.yaml".format(configFileName);
		if(File.exists(configFilePath)) { ("Did not add configuration, it exists already in this path:" + configFilePath).warn; ^this };
		LanguageConfig.store(configFilePath);
		"\nNew config file has been written to:\n%\n".postf(configFilePath);
	}

	*switchDir { |folderName, excludeDir, includeDir|
		var toInclude = includeDir +/+ folderName;
		var toExclude =  excludeDir +/+ folderName;
		if(pathMatch(toInclude).isEmpty) {
			Error("no directory found to which to link (% doesn't exist".format(toInclude)).throw;
		} {
			if(pathMatch(toExclude).isEmpty) {
				Error("no directory found to which to link (% doesn't exist)".format(toExclude)).throw;
			} {
				this.addIncludePath(toInclude);
				"excluded:\n%\n".postf(toExclude);
				this.addExcludePath(toExclude);
				"included:\n%\n".postf(toInclude);
			}
		}

	}

	*buildDir {
		var i = Platform.classLibraryDir.find("/build/Install/");
		if(i.isNil) {
			Error("no build directory found in path: %)".format(Platform.classLibraryDir)).throw;
		};
		^Platform.classLibraryDir.keep(i);
	}

	*makeDevelConfig { |configFileName|
		var buildDir = this.buildDir;
		this.switchDir("SCClassLibrary", Platform.resourceDir, buildDir);
		this.switchDir("HelpSource", Platform.resourceDir, buildDir);
		this.add(configFileName, Platform.userConfigDir)
	}


	/*
	*makeStandaloneConfig { |configFileName|
		this.clear;
		this.addExcludePath(Platform.userExtensionDir);
		this.addExcludePath(Platform.systemExtensionDir);
		this.add(configFileName, Platform.resourceDir);
		// s.options.ugenPluginsPath = Platform.resourceDir; // works only once: this needs to be written into startup file.
	}
	*/
	/*
	LanguageConfig.makeStandaloneConfig("standaloneX");

	*/



}
