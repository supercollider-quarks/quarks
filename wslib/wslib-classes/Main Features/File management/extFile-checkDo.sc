 + File {
	
	*makeDir { |dir = ""| ^dir.makeDir; }
 
 	*checkDo { 
 		// open file to write to, evaluate function, and close it again
 		// - everything you need in one method 
 		// - asks for options when ask == true and file exists
 		// - generates new name when overwrite == false and file exists
 		// - closes the file afterwards
 		// - function can also be a string
 		
 		arg pathName, function, overwrite = false, ask = true, mode = "w", doneAction;
 		
 		var theFile;
 		var renameFunc, writeFunc, newNameFunc, savePanelFunc;
 		
		pathName = pathName.standardizePath;
 		
 		renameFunc = {
 			File.checkDo( 
 				PathName( pathName ).realNextName,
 				function, overwrite, false, mode, doneAction )
 			};
 			
 		writeFunc = {
 			if( PathName( pathName.dirname ).isFolder.not )
 				{ File.makeDir( pathName.dirname ) };
 			theFile = File( pathName , mode );
 			if( function.class == String )
 				{ theFile.putString( function ) }
 				{ function.value( theFile, pathName ); };
 			theFile.close; 
 			("saved file: " ++ pathName).postln;
 			doneAction.value( theFile, pathName );
 			};
 		
 		newNameFunc = {
 			SCRequestString( 
 				PathName(pathName).realNextName.basename, 
 				"Please enter a new name:",
 				{ |newName| 
 				File.checkDo( 
 					PathName(pathName.dirname ++ "/" ++ newName)
 						.extension_( PathName(pathName).extension ).fullPath,
 						function, overwrite, ask, mode, doneAction );
 				});
 			};
 			
 		savePanelFunc = {
 			CocoaDialog.savePanel( 
 				{ |newPath| 
 				File.checkDo( 
 					PathName(newPath).extension_( PathName(pathName).extension ).fullPath
 					, function, overwrite, ask, mode, doneAction );
 				} ) };
 		
 		if( overwrite or: File.exists( pathName ).not )
 			{ writeFunc.value }
 			{ if( ask )
 				{ ( "File-checkDo: The file '" ++ pathName.basename ++ "' already exists" ).postln;
 					"\tcheck the alert box on your screen".postln;
 				 SCAlert( "The file '" ++ pathName.basename ++ "' already exists",
 					[ "cancel", "overwrite", "auto rename", "new name", "browse"],
 					[ nil, writeFunc, renameFunc, newNameFunc, savePanelFunc ] );  }
 				{ renameFunc.value };
 			}
 		
 		}
 		
 	}