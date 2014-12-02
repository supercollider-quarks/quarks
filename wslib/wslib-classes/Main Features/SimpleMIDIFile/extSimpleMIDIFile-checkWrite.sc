// write file using the wslib file management options

+ SimpleMIDIFile {
	checkWrite { arg newFileName, overwrite= false, ask= true;
		var theFile;
		
		newFileName = newFileName ?? pathName;
		newFileName = newFileName.standardizePath;
		
		newFileName = PathName(newFileName).extension_("mid").fullPath; //force mid extension
		
		File.checkDo( newFileName, 
				{ |f| this.writeFile( f ); }, overwrite, ask, "wb+") 
					// output to file with default formatting
				
		}
	}
