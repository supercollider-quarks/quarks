FileSystemType {

	classvar <>default;
	var <>subFolders;
	
	*new { |subFolders|
		subFolders = subFolders ? [];
		^super.newCopyArgs( subFolders );
		}
		
	*fromPath { |path|
		this.deprecated( thisMethod );
		path = (path ? "~/scwork/FileSystem").standardizePath;
		if( path.isFolder )
			{ ^FileSystemType( 
				(path ++ "/*").pathMatch.select( _.isFolder ).collect({ 
					|item| item.standardizePath.absolutePath( path ++ "/" )
					})
					); }
			{ ("Path" + path.pad($') + "doesn't exist (yet)").postln;
				^FileSystemType.new }
		}
	
	*initClass {
		default = FileSystemType( [ "Sounds", "Dev" ] ); 
		}
	}

FileSystem {
	var <location, <projectName, <fileSystemType;
	
	*new { |location, projectName, fileSystemType, check = true|
		this.deprecated( thisMethod );
		location = (location ? "~/scwork/").standardizePath;
		projectName = projectName ? "FileSystem";
		fileSystemType = fileSystemType ? FileSystemType.default;
		if( check )
			{ if( (location ++ "/" ++ projectName ).pathExists != false)
				{ ("FileSystem: " + location ++ "/" ++ projectName +
					"already exists").warn };
			};
		^super.newCopyArgs( location, projectName, fileSystemType );
		}
		
	makeDir {  fileSystemType.subFolders.do({ |item|
			[location,projectName,item].join( $/ ).makeDir });
		}
	}
		
		
		
		
	
	
	