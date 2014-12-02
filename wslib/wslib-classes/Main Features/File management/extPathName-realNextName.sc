// wslib 2006

// better version of nextName

+ PathName {

	noRealEndNumbers { |withExtension = true|
		// support for files with extension
		var count = 0; 
		var extension;
		var fileName;
		
		//pathOnly = this.pathOnly;
		fileName = fullPath.removeExtension;
		extension = fullPath.extension;
		if( withExtension.not ) { extension = ""; };
		extension = extension ? "";
		if( extension != "" ) { extension = "." ++ extension };
		
		while { 	count = count + 1;
				fileName.at(fileName.size - 1).isDecDigit; }
			{ fileName = fileName.copyRange(0,  fileName.size - 2) };
			
		^fileName ++ extension;
	}
	
	extension_ { |extension = "", warn| // warn is a func evaluated when extension is changed
		if( warn.notNil ) { if( this.fullPath.extension != extension )
			{ warn.value } };
		fullPath = fullPath.replaceExtension( extension );
		^this;
		}
		
	realEndNumber {	// turn consecutive digits at the end of fullPath into a number.
	
		// support for files with extension
	
		var numString = "";
		var count = 0, char, number;
		var fileName;
		
		fileName = fullPath.removeExtension;
		
		while
			{ 	count = count + 1;
				char = fileName.at(fileName.size - count);
				char.isDecDigit; } 
			{ numString = char ++ numString };

		number = numString.interpret ? 0;
		^number
	}
	
	realEndNumber_ { |number = 0|
		// support for files with extension
		var count = 0; 
		var pathOnly, extension;
		var fileName;
		
		if(number == 0) { number = "" };
		
		fileName = this.noRealEndNumbers;
		#fileName, extension = fileName.splitext;
		extension = extension ? "";
		if( extension != "" ) { extension = "." ++ extension };
	
		fullPath = fileName ++ number ++ extension;
		^this;
	}

	realNextPathName {  |increaseBy = 1|
		^this.copy.realEndNumber_( this.realEndNumber + increaseBy );
		}
		
	realNextName { |increaseBy = 1|
		^this.realNextPathName.fullPath;
		}	
}

+ String {
	asPathName { ^PathName( this ) }
	realNextName { ^this.asPathName.realNextName; }
	
	}