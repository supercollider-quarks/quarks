ConductorSettingsGUI {
	var <>conductor;
//	var <>path;
	
	*new { | conductor | ^super.newCopyArgs(conductor) }
	
//	getFile { arg argPath; var file, contents;
//		if (File.exists(argPath)) {
//			path = argPath;
//			file = File(path,"r"); 
//			contents = file.readAllStringRTF;
//			file.close;
//			^contents;
//		} {
//			(argPath + "not found").postln;
//			^nil
//		}
//	}
//	
//	putFile { | vals, argPath | 
//		path = argPath ? path;
//		File(path,"w").putAll(vals).close;
//	}
//
//	load { | argPath |
//		var v;
//		if (argPath.isNil) {
//			File.openDialog(nil, { arg path; 
//				v = this.getFile(path);
//				conductor.value_(v.interpret)
//			});
//		} {
//			v = this.getFile(argPath);
//			conductor.value_(v.interpret)
//		};
//	}
//	
//	save { | path |
//		if (path.isNil) {
//			File.saveDialog(nil, nil, { arg path; 
//				this.putFile(conductor.value.asCompileString, path)
//			});
//		} {
//			this.putFile(conductor.value.asCompileString, path)
//		};
//
//	}

	draw { |win, name =">"| ~settingsGUI.value(win, name, conductor) }
	
}