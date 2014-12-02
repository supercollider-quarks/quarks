
Preference {

	classvar <>fileNames, <>startupFilePath, <>repositoryDirPath, <current;
	classvar <>openFileAtStartup = false, <>examplesFolder;
	
	*initClass {	
		
		
		StartUp.add {
		
			if([\osx, \linux].includes(thisProcess.platform.name).not) {
				"sorry, preferences currently work only with OS X.".postln;
				^this
			};
			
			// for now, user dir only
			startupFilePath = startupFilePath ?? { thisProcess.platform.startupFiles.last };			repositoryDirPath = repositoryDirPath ?? {
				(startupFilePath.dirname +/+ "startupfiles");
			};
			repositoryDirPath = repositoryDirPath.escapeChar($ );
			examplesFolder = examplesFolder ?? { 
				this.filenameSymbol.asString.dirname +/+ "startup_examples/";
			}; 
			//startupFilePath = startupFilePath.escapeChar($ );
			if(this.safeToCreateSymlink.not) { this.postInitWarning };
			
			if(pathMatch(repositoryDirPath).isEmpty) {
				systemCmd(postln("mkdir -p" + repositoryDirPath));
			};
			
			if(pathMatch(repositoryDirPath +/+ "*").isEmpty) {
				this.copyExamplesFromQuark;
			};
			
			this.initFilePaths;
			this.findCurrentStartup;
			this.initMenu;
			if(openFileAtStartup) { this.openStartupFile };
			
		
		};
	}
	
		
	*initFilePaths {
		var filePaths = pathMatch(repositoryDirPath +/+ "*startup*");
		fileNames = ();
		filePaths.do { |path|
			var key = path.basename.asSymbol;
			fileNames[key] = path;
		}
	}
	
	*findCurrentStartup {
		var startupPointsTo, stat, index, path;
		// the current startup file is the one symlinked to the extensions folder
		path = startupFilePath.escapeChar($ );
		if(pathMatch(path).isEmpty) { current = \none; ^this };
		stat = unixCmdGetStdOut("stat -F" + path);
		index = stat.find("->");
		if(index.isNil) {
			current = \default;
		} {
			startupPointsTo = stat[index + 2 ..];
			if(startupPointsTo.last == Char.nl) {
				startupPointsTo = startupPointsTo.drop(-1)
			};
			current = startupPointsTo.basename.asSymbol;
		};
		"Current startup file: %\n".postf(current);
	}
	
	
	
	*setToDefault {
		// look for a simple startup.rtf or similar
		fileNames.keysValuesDo { |name, val|
			var str = name.asString;
			if(str.splitext.first == "startup") {
				this.setPreference(name);
				this.initMenu;
				^this
			};
		};
		this.reset;
	}
	
	
	
	*setPreference { |which|
			var path = fileNames.at(which.asSymbol);
			var symlinkPath = startupFilePath.escapeChar($ );
			("\n\nSwitching to startup file:" + which).postln;
			if(this.safeToCreateSymlink) {
				if(path.notNil) {
					systemCmd("ln -s -F " ++ path.escapeChar($ ) + symlinkPath);
					current = which;
					this.initMenu;
					this.executePreload(path);
				} {
				 	"Preference: no file of this name found: %\n".postf(which) 
				};
			} {
				this.postInitWarning;

			}
	}

	
	*reset {
		if(pathMatch(startupFilePath.escapeChar($ )).notEmpty) { 
				if(isSymLink(startupFilePath.escapeChar($ ))) {
					systemCmd("rm" + startupFilePath.escapeChar($ ));
					current = \none;
				} {
					"Preference: Please remove your startup file manually".postln;
				}
		};
		this.initMenu;
	}
	

	*executePreload { |path|
		var i, k, string, code;		
		File.use(path, "r") { |f| string = f.readAllString };
		// check if any preferences are declared in startup file
		// and which need to be executed before recompile (usually Language Config and quarks)
		i = string.find("/*\nPREFERENCES:");
		if(i.notNil) {
			i = string.find("\n", offset: i + 10);
			k = string.find("*/", offset: i);
			if(k.notNil) {
				code = string[i+1..k-1];
				"Preference switch - running the following code:".postln;
				code.postln.interpret;
			}	
		}		
	}
		
	*openRepository {
		var str = "open";
		str = str + repositoryDirPath;
		systemCmd(str)
	}
	
	*openStartupFile { |path|
		var name;
		path = path ?? startupFilePath;
		name = startupFilePath.basename.asSymbol;
		Document.allDocuments.do { |doc| 
			if(doc.title == name) {
				doc.front;
				^this
			};
		};
		systemCmd("open" + path.escapeChar($ ));
	}
	
	*postInstalledQuarks {
		"\n\n".post;
		Quarks.installed.do { |quark|
			"'%'.include;\n".postf(quark.name);	
		};
	}
	
	*safeToCreateSymlink {
		var symlinkPath = startupFilePath.escapeChar($ );
		^pathMatch(symlinkPath).isEmpty or: { isSymLink(symlinkPath) }
	}
	
	
	*initMenu {
		
		Platform.case(\osx, {
			try { // make sure that nothing can block other method calls
				var names = fileNames.keys.asArray.sort;
				var parent = CocoaMenuItem.default.findByName("startup");
				var isDefault;
				
				if (parent.notNil) { parent.remove; };
				
				isDefault = (current == \default) or: { current == 'startup.scd' };
				
				CocoaMenuItem.add(["startup", "default"], {
					this.setToDefault;
				}).enabled_(isDefault.not);
				
				CocoaMenuItem.add(["startup", "none"], { 
					this.reset; 
					this.initMenu;
				}).enabled_(current != \none);
				
				parent = CocoaMenuItem.default.findByName("startup");
				
				SCMenuSeparator(parent, 2);

				CocoaMenuItem.add(["startup", "Open current startup"], { 
					this.openStartupFile; 
				}).enabled_(current != \none);		

				CocoaMenuItem.add(["startup", "Open startup folder"], { 
					this.openRepository; 
				});

				SCMenuSeparator(parent, 5);

				names.do { |name|
					var menuName = name.asString;
					var item = CocoaMenuItem.add(["startup", menuName], { this.setPreference(name) });
					item.enabled = (current != name);
				};
				
				SCMenuSeparator(parent, names.size + 6);
				
				CocoaMenuItem.add(["startup", "Open quarks window"], { 
					Quarks.gui;
				});
				
				CocoaMenuItem.add(["startup", "Post installed Quarks"], { 
					this.postInstalledQuarks; 
				});
								
				CocoaMenuItem.add(["startup", "init", "Copy examples from quark"], { 
					this.copyExamplesFromQuark;
					this.initFilePaths; 
					this.initMenu;
				});
				
				CocoaMenuItem.add(["startup", "init", "Refresh this menu"], { 
					this.initFilePaths; 
					this.initMenu;
				});				
			};
		})
	}
	
	*postInitWarning {
		"************************************************************************\n"
		"Preference: in order to use preference switching, move your startup file "
		"to the startupfiles folder first, and recompile.\n"
		"Preference.openRepository;\n"
		"************************************************************************".postln	
	}
	
	*copyExamplesFromQuark {
			var filePaths = pathMatch(this.filenameSymbol.asString.dirname 
												+/+ "example_setups/*");
			filePaths.do { |path|
				path = path.escapeChar($ );
				// cp -n: copy, do not overwrite existing file
				systemCmd("cp -n % %"
					.format(path, repositoryDirPath +/+ path.basename)
				);
			};
	}
	

}

+ String {
	isSymLink { 
		^unixCmdGetStdOut("ls -la" + this).at(0) == $l 
	}
}

