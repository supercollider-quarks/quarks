+ String {

	// using sips (unix application built into MacOSX)
	
	// convert your web images right from SC..
	
	resampleImage { |newMaxSize = 600, newName = \auto|
		var path, result;
		path = this.standardizePath;
		
		case { newName == \auto }
			{  newName = path.removeExtension ++ "_" ++ newMaxSize ++ "." ++ path.extension }
			{ newName == \self }
			{ newName = path }
			{ true }
			{ newName = newName.standardizePath };
			
		result = ("sips -Z " ++ newMaxSize ++ " " ++ path.quote ++ " --out " ++ newName.quote)
			.systemCmd;
		if( result != 0 )
			{ "String-resampleImage: failed".postln; ^path }
			{ ("String-resampleImage: created file:\n\t" ++ newName).postln; ^newName };
			
		}
	
	convertImage { |newFormat = "jpg", newName = \auto|
		var path, result;
		path = this.standardizePath;
				
		case { [\auto, \self].includes( newName )  }
			{  newName =  path.removeExtension ++ "." ++ newFormat;  }
			{ true }
			{ newName = newName.standardizePath };
			
		result = ("sips -s format " ++ newFormat ++ " " ++ 
				path.quote ++ " --out " ++ newName.quote)
			.systemCmd;
		if( result != 0 )
			{ "String-convertImage: failed".postln; ^path }
			{ ("String-convertImage: created file:\n\t" ++ newName).postln; ^newName };
		}
		
	rotateImage { |degreesCW = 90, newName = \auto|
		var path, result;
		path = this.standardizePath;
			 
		case { newName === \auto }
			{  newName = path.removeExtension ++ "_r" ++ degreesCW ++ "." ++ path.extension; }
			{ newName === \self }
			{ newName = path }
			{ true }
			{ newName = newName.standardizePath };
			
		result = ("sips -r " ++ degreesCW ++ " " ++ 
				path.quote ++ " --out " ++ newName.quote)
			.systemCmd;
		if( result != 0 )
			{ "String-rotateImage: failed (%)\n".postf( result ); ^path }
			{ ("String-rotateImage: created file:\n\t" ++ newName).postln; ^newName };
		}
	
	
	}