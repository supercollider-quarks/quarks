// wslib 2005
//
// improved version of Cocoa-getPathsInDirectory *with* extension matching
// changed to use of pathMatch
// added find functionality -- watch out with big search tasks; these are recursive functions
// so they might freeze your system for a while. But they will always work (tested with up to
// apx. 70000 files at once
// and Buffer: read directory

+ String {

	dirLevel { ^this.occurencesOf($/) } // obsolete?
	
	getPathsInDirectory { |extension, nLevels, includeInPath = false|
	
		// recursive pathmatch
		
		var path = this.standardizePath.withTrailingSlash;
		var out, folders;
		path = path[..(path.size-2)]; // remove trailing slash
		nLevels = nLevels ? inf;
		if( extension.notNil ) 
			{ out = (path ++ "/*." ++ extension).pathMatch; } 
			{ out = (path ++ "/*").pathMatch.select({ |item|
				item.last != $/ ; }) };
		if( nLevels != 0 ) {
			folders = (path ++ "/*/").pathMatch.collect( { |folder|
				folder.getPathsInDirectory( extension, nLevels - 1, true )
				} );
			out = out ++ folders.flatten(1);
			};
		if( includeInPath.not )
			{ out = out.collect({ |item| item[ (path.size + 1) .. ] }); }
		^out;
	}
	
	findPathsInDirectory { |wordToFind = "", extension, nLevels, includeInPath = false|
	
		// recursive pathmatch -- wordToFind is case-sensitive
		
		var path = this.standardizePath;
		var out, folders;
		nLevels = nLevels ? inf;
		if( extension.notNil ) 
			{ out = (path ++ "/*" ++ wordToFind ++ "*." ++ extension).pathMatch; } 
			{ out = (path ++ "/*" ++ wordToFind ++ "*").pathMatch.select({ |item|
				item.last != $/ ; }) };
		if( nLevels != 0 ) {
			folders = (path ++ "/*/").pathMatch.collect( { |folder|
				folder.findPathsInDirectory( wordToFind, extension, nLevels - 1, true )
				} );
			out = out ++ folders.flatten(1);
			};
		if( includeInPath.not )
			{ out = out.collect({ |item| item[ (path.size + 1) .. ] }); }
		^out;
		}
	
	getSubDirectories { | nLevels, includeInPath = false|
	
		// Variation on above
		
		var path = this.standardizePath;
		var out, folders;
		nLevels = nLevels ? inf;
		out = (path ++ "/*/").pathMatch;
		if( nLevels != -1 ) {
			folders = out.collect( { |folder|
				folder.getSubDirectories( nLevels - 1, true )
				} );
			out = out ++ folders.flatten(1);
			};
		if( includeInPath.not )
			{ out = out.collect({ |item| item[ (path.size + 1) .. ] }); }
		^out;
		}
		
	findSubDirectories { | wordToFind = "", nLevels, includeInPath = false|
	
		// Variation on above
		
		var path = this.standardizePath;
		var out, folders;
		nLevels = nLevels ? inf;
		out = (path ++ "/*" ++ wordToFind ++ "*/").pathMatch;
		if( nLevels != -1 ) {
			folders =  (path ++ "/*/").pathMatch.collect( { |folder|
				folder.findSubDirectories( wordToFind, nLevels - 1, true )
				} );
			out = out ++ folders.flatten(1);
			};
		if( includeInPath.not )
			{ out = out.collect({ |item| item[ (path.size + 1) .. ] }); }
		^out;
		}
	
	
		
}			