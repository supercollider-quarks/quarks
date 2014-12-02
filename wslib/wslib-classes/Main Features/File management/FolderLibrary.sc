// wslib 2006
//
// a library for folder structures
// works the same way as osx folder structures
//
// 
//
// use: design and manipulate a folder structure and call mkdir for all at once
//  will only create new folders when they are not there yet
// - be careful with the makeDir method; cannot be undone.


FolderLibraryElement {
	
	var <>name = "New";
	var <>contents; // should only be other FolderLibraryElements
	var <>type = 'folder'; // the only type for now
	
	*new { |name = "New", contents, type = 'folder'|
		this.deprecated( thisMethod );
		^super.newCopyArgs( name.asString, contents ? [], type );
		}
		
	*newFromPath { |path = "New/New"|
		var newElement;
		this.deprecated( thisMethod );
		path = path.standardizePath.split( $/ );
		newElement = FolderLibraryElement( path.last, [], 'folder');
		path.pop;
		path.reverseDo( { |item|
			newElement = FolderLibraryElement( item, [ newElement ], 'folder' );
			} );
		^newElement;
		}
		
	at { |key|
		^contents.select( { |item| item.name.asSymbol == key.asSymbol } ).first  }
	
	put { |key, anElement|
			if( anElement.class == String )
				{ anElement = FolderLibraryElement.newFromPath( anElement.asString ); };
			if( this.at( key ) == nil )
				{ contents = contents.add( 
					FolderLibraryElement( key.asString, 
						anElement.asCollection, 'folder' ) ).sort; }
				{ 	if( anElement.notNil )
						{ anElement.asCollection.do( { |item|
							this.at( key ).put(item.name, item.contents)
								} ) 
						}
					};
		}
	
	add { |anElement|
			if( anElement.class != FolderLibraryElement )
				{ anElement = FolderLibraryElement.newFromPath( anElement.asString ); };
			^this.copy.put( anElement.contents.first.name, anElement.contents.first.contents )
		}
		
	
	<= { |anElement| ^name <= anElement.name; }
		
	value { ^name }
	
	postTree { 
		var func;
		func = { |array, level = 0|
			array.do({ |item|
				 (String.fill( level, $\t ) ++
				 item.name).postln; 
				 func.value( item.contents, level + 1 );
				 });
			};
		name.postln;
		func.value( contents, 1 );
		}
	
	asPaths { |allPaths = false|
		var func;
		var outArray = [];
		
		func = { |array, baseName = ""|
			array.do({ |item|
				if( item.contents.first.isNil or: allPaths )
					{ outArray = outArray.add( baseName ++ "/" ++ item.name ); };
				 func.value( item.contents, baseName ++ "/" ++ item.name );
				 });
			};
		func.value( contents, name.asString );
		^outArray;
		}			
	
	}

FolderLibrary : FolderLibraryElement {
	
	var <name;
	var <>contents;
	
	*new { |baseName = "~/scwork", contents|
		this.deprecated( thisMethod );
		if( contents.isString )
			{ contents = [ FolderLibraryElement.newFromPath( contents ) ] };
		^super.newCopyArgs( baseName.standardizePath, contents ? [] );
		}

	baseName_ { |newBaseName| name = newBaseName.standardizePath; }
	
	baseName { ^name }
	
	makeDir { this.asPaths.do( { |item| File.makeDir( item ) } ); }
	
	}