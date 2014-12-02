// wslib 2006

// osx only

// using .systemCmd and .pathMatch
// don't use wild cards ( *.* etc ) unless you know very well what you're doing

+ Nil { 
	isValidExtension { ^false }
	standardizePath { ^nil } 
	}

+ Symbol { isValidExtension { ^this.asString.isValidExtension } }

+ SequenceableCollection { 

	flatNoString { // flat everything but the Strings
		var list;	
		list = this.species.new;
		this.do({ arg item, i;
			if ( item.respondsTo('flatNoString'), {
				list = list.addAll( item.flatNoString );
			},{
				list = list.add( item );
			});
		});
		^list
		}
		
	}

+ String {
	
	flatNoString { ^[ this ] }

	folderContents { |levels = inf| // all folder contents
		var out = [];
		out = (this.standardizePath ++ "/*").pathMatch;
		if( levels != 0 )
			{ out = out.collect({ |item|
			  if( item.last == $/ )
				{ item.folderContents( levels - 1 );  }
				{ item; }; });
			};
		^out.flatNoString;
		}

	postlnIfTrue { |bool = true| if( bool ) { ^this.postln }; }

	isFile { ^PathName( this ).isFile; }
	
	isSoundFile { var sf;
		if( this.isFile )
			{ sf = SoundFile.new;
				if( sf.openRead( this.standardizePath ) )
					{ sf.close; 
						^true }
					{ ^false };
			}
			{ ^false  }
		}
	
	
	isFolder { ^PathName( this ).isFolder; } // can also be bundle
	
	isBundlePath { ^( this.last == $/ ) && { this.splitext.last.isValidExtension }; }
		
	isBundle {  // such as .app, .wdgt, .component, .bundle etc
		var path;
		path = this.pathMatch;
		^if(path.notEmpty, { path[0].isBundlePath; }, { false });
		}
		
	isValidExtension {
		if( (this.size == 0) or: 
				{ this.includes( $ ) or: this.first.isAlpha.not } ) 
			{ ^false } { ^true }; 
		}
	
	pad { |char = $', amt = 1|
		var padString;
		padString = String.fill( amt, { char } );
		^padString ++ this ++ padString;
		}
		
	removeFwdSlash { if( this.last == $/ ) { ^this[0..(this.size-2)] } { ^this } }
		
	extension { var ext; // returns only if valid
		ext = this.splitext.last;
		if( ext.isValidExtension )
			{ ^ext.removeFwdSlash } // removes fwdSlash from bundle paths
			{ ^nil }
		}
	
	removeExtension { var ext;
		ext = this.extension;
		^if(ext.notNil)
			{ this.splitext.first }
			{ this }	
		}
	
	replaceExtension { |newExt|
		if( newExt.notNil)
			{ ^this.standardizePath.removeExtension ++ "." ++ newExt }
		}
		
	hasExtension { ^this.extension != nil }
	
	absolutePath2 { |base| // crude..
		if( base.isNil )
			{ ^this.basename }
			{ ^this[base.size..] }
		}
	
	pathExists { |showType = true| // types are : \bundle, \folder and \file
		var path;
		path = this.pathMatch;
		^if( showType )
			{ if(path.size == 1)
				{ if( path.at(0).last == $/ )
					{ if( path.at(0).isBundlePath ) { \bundle } { \folder } }
					{ \file };
				}	{ false };
			} { path.size == 1 }
		}

	 makeDir { |silent = false|
 		// auto create directories when they're not there - be carefull with this one
 		var dname;
 		dname = this.standardizePath;
		if( dname.pathMatch == []; )
				{ ("mkdir -p" + dname.quote).systemCmd;
				("created dir:" + dname ).postlnIfTrue( silent.not );  }
		}
	
	//// copy, move and rename
		
	renameTo { |newName| // only the file's / folder's name is used
		var result;
		if( newName != nil )
			{ if( this.isFile or: this.isFolder )
				{ 	newName = this.standardizePath.dirname ++ "/" ++ newName.basename;
					result = ("mv" + this.standardizePath.quote + newName.quote ).systemCmd;
					if( result == 0 )
						{ ("String-renameTo:\n\trenamed '" ++ this.basename ++ 
								"' to '" ++ newName.basename ++ "'").postln; }
						{ ("String-renameTo: failed (result:" + result ++ ")").postln; };
								
					^newName; }
				{ ("String-renameTo: '" ++ this ++ "' doesn't exist").postln; 
					^this };
			}
		}
		
	copyTo { |newDir = "", newName, overwrite = false, createIfNotThere = true, silent = false | 
	
		// if newName == nil the original name is used
		
		var from, result, to, exists = false;
		from = this.standardizePath;
		to = newDir.standardizePath ++ "/" ++ ( newName ? from.basename );
		exists = to.isFile or: to.isFolder;
		if( exists.not or: overwrite )
			{	if( createIfNotThere ) { to.dirname.makeDir( silent ) };
				result = ("cp -R" + from.quote + to.quote).systemCmd;
				if( result == 0 )
					{ ("String-copyPath:\n\tcopied  '" ++ 
		     			from ++ "'\n\tto      '" ++ to ++ "'" ++ 
		     				 ( if(exists) {" (overwritten)" } {""} ) )
		     				 .postlnIfTrue( silent.not ); }
					{ ("String-copyTo: failed (result:" + result ++ ")")
						.postlnIfTrue( silent.not ); };
					
			}
			{ ("String-copyTo: failed\n\t'" ++ to ++ "' already exists" )
				.postlnIfTrue( silent.not ); }
		^result == 0; // returns true when succeeded
		}
		
	copyFile { |newPath, overwrite = false, createIfNotThere = true, silent = false |
		// full path as input
		// still also copies folders
		newPath = newPath.standardizePath;
		^this.copyTo( newPath.dirname, newPath.basename, overwrite, createIfNotThere, silent );
		} 
	
	copyToDir { |newDir = "", overwrite = false, createIfNotThere = true, silent = false |
		// same as copyTo, but doesn't specify new name
		^this.copyTo( newDir, nil, overwrite, createIfNotThere, silent );
		}
	
	copyRename { |newDir = "", newName, createIfNotThere = true, silent = false |
	
		// auto rename if exists
		
		var result, to, exists = false;
		to = newDir.standardizePath ++ "/" ++ (newName ? this.basename);
		if(  to.pathExists( false ) )
			{ ^this.copyRename( newDir, PathName( to.basename ).realNextName, 
				createIfNotThere, silent );  }
			{ if( this.copyTo( newDir, newName, createIfNotThere: createIfNotThere, 
				silent: silent ) )
				{ ^to } 
				{ ^false };
			};
		}
		
	copyReplace { |newDir = "", newName, createIfNotThere = true, 
		toTrash = true, ask = true, silent = false|
		( newDir.standardizePath ++ "/" ++ ( newName ? this.basename ) )
			.removeFile( toTrash, ask, silent );
		^this.copyTo( newDir, newName, false, createIfNotThere, silent );
		}
		
		
	replaceWith { |inPath, createIfNotThere = true| // copyTo vice versa
		^inPath.copyTo( this.standardizePath.dirname, this.standardizePath.basename, 
			true, createIfNotThere );
		}
		
	copyToDesktop { ^this.copyTo( "~/Desktop" ); }
	create_scwork { "~/scwork".makeDir; }
	copyTo_scwork { ^this.copyTo( "~/scwork", nil, false, true ); } // creates if not there
	
	moveToDir { |to, createIfNotThere = false, silent = false| // move anything to a dir 
		// doesn't overwrite (result:256)
		var result, exists;
		exists = this.pathExists;
		if( to.notNil && { exists != false } )
			{ 	to = to.standardizePath;
				if( createIfNotThere ) { to.makeDir( silent ) };
				if( to.isFolder )
					{ 
					result = ("mv" + this.standardizePath.quote + to.quote).systemCmd; 
					
					if( result == 0 )
						{ ("String-moveToDir:\n\tmoved  '" ++ 
		     					this.standardizePath ++ "' (" ++ exists ++
		     					 ")\n\tto     '" ++ to ++ "/'").postlnIfTrue( silent.not );
		     					 
		     			^to ++ "/" ++ this.standardizePath.basename; // return new path
		     			} 
		     			{ ("String-moveToDir: failed (result:" + result ++ ")")
		     					.postlnIfTrue( silent.not ); 
		     			};
		     		}
					{ "String-moveToDir: folder or file doesn't exist"
						.postlnIfTrue( silent.not );
					};
			};
		}
		
	moveTo { |newDir = "", newName, createIfNotThere = true, silent = false | 
	
		// if newName == nil the original name is used
		
		// does not overwrite
		
		var from, result, to, exists = false;
		from = this.standardizePath;
		to = newDir.standardizePath ++ "/" ++ ( newName ? from.basename );
		exists = to.isFile or: to.isFolder;
		if( exists.not )
			{	if( createIfNotThere ) { to.dirname.makeDir( silent ) };
				result = ("mv" + from.quote + to.quote).systemCmd;
				if( result == 0 )
					{ ("String-moveTo:\n\tmoved  '" ++ 
		     			from ++ "'\n\tto      '" ++ to ++ "'" )
		     				.postlnIfTrue( silent.not ); }
					{ ("String-moveTo: failed (result:" + result ++ ")")
						.postlnIfTrue( silent.not ); };
					
			}
			{ ("String-moveTo: failed\n\t'" ++ to ++ 
				"' already exists or original was not found" )
				.postlnIfTrue( silent.not );
				}
		^result == 0; // returns true when succeeded
		
		}
		
	moveRename { |newDir = "", newName, createIfNotThere = true, silent = false |
		var result, to, exists = false;
		to = newDir.standardizePath ++ "/" ++ (newName ? this.basename);
		if(  to.pathExists( false ) )
			{ ^this.moveRename( newDir, PathName( to.basename ).realNextName, 
				createIfNotThere, silent );  }
			{ if( this.moveTo( newDir, newName, createIfNotThere: createIfNotThere, silent: silent ) )
				{ ^to } 
				{ ^false };
			};
		}
		
	moveReplace { |newDir = "", newName, createIfNotThere = true, 
		toTrash = true, ask = true, silent = false|
		( newDir.standardizePath ++ "/" ++ ( newName ? this.basename ) )
			.removeFile( toTrash, ask, silent );
		^this.moveTo( newDir, newName, createIfNotThere, silent );
		}
	
		
	removeFile { |toTrash = true, ask = true, silent = false|
		// by default only moves to trash
		// watch out with this one..
		// also removes folders...
		// does not ask when moving to trash
		
		var result, exists, rmFunc;
		exists = this.pathExists;
		rmFunc = { result = ("rm -R" + this.standardizePath.quote).systemCmd; 
						("String-removeFile: removed file" + this.basename.quote)
						.postlnIfTrue( (result == 0) && silent.not );
				 };
	
		if(  exists != false )
			{ if( toTrash )
				{ ^this.moveRename( "~/.Trash", silent: silent );  }
				{ if( ask )
					{ SCAlert( "delete" + this.basename.quote ++ "?",
						[ "cancel", "ok" ], [nil, rmFunc]);
						} { rmFunc.value };
				};
			};
		}
		
		
	//// file compression and archiving
		
	zip { |newPath, includeInvisible = false, tmp = "/tmp"| 
		// makes zip archive (leaves original intact)
		var oldPath, exists, result;
		oldPath = this.standardizePath;
		newPath = newPath.standardizePath ? oldPath.removeExtension;
		exists = this.pathExists;
		if( exists != false )
			{ result = 
				( "cd" + oldPath.dirname.quote + "\n"
				"zip -b" + tmp +
			 	(if( [\folder, \bundle].includes(exists) ) { "-r " } {""}) ++
			 	//"-j" +  // -j is for losing the absolute path data
				(if( includeInvisible ) { "-S " } {""}) ++
				newPath.quote + oldPath.basename.quote ).systemCmd;
			if( result == 0 )
				{ ("String-zip:\n\tcreated zip file  '" ++ newPath ++ ".zip'").postln; 
					^newPath ++ ".zip"; }
				{ ("String-zip: failed (result:" ++ result ++ ")").postln; };
			}
			{ "String-zip: original path not found".postln; };
		}
		
	unzip { |to|
		var oldPath, exists, result;
		oldPath = this.standardizePath.replaceExtension( "zip" );
		exists = oldPath.pathExists;
		to = to.standardizePath ? oldPath.dirname;
		if( exists != false )
			{ result = ("unzip" + oldPath.quote + "-d" + to.quote ).systemCmd;
			  if(result == 0)
				{ ("String-unzip:\n\tunzipped file" + oldPath.pad($') ).postln; }
				{ ("String-unzip: failed (result:" ++ result ++ ")").postln; };
				}
			{ "String-zip:" + oldPath.pad($') + "not found".postln; };
		}
		
	// the following methods don't use internal check; systemCmd will fail when files or
	// folders don't exist:
	
	tar { |newPath|
		var location, original, result;
		original = this.standardizePath;
		location = (newPath ? original).standardizePath;
		if(location.extension != "tar" ) { location = location ++ ".tar"; };
		
		if( ("cd" + original.dirname.quote 
		+ "\n" + "tar cvf" + location.quote + original.basename.quote
			).systemCmd != 0 )
			{ "String-tar: failed - might still have created an empty tar file".postln; }
			{ ( "String-tar:\n\tcreated tar file" + location ).postln; };
		
		^location;
		}
		
	untar { var location, result;
		location = this.standardizePath;
		if( ("cd" + location.dirname.quote 
		+ "\n" + "tar xvf" + location.basename.quote
			).systemCmd != 0 )
			{ "String-untar: failed".postln; }
			{ ( "String-untar:\n\textracted tar file" + location.pad($') ).postln; };
		^location.removeExtension; // could be wrong if filename != stored folder/file name
		}
	
	gz { |speed = 6| // 1 = fast, 9 = best (don't expect very large differences here)
		
		//  use with care: deletes original
		// for folders first use .tar
		
		if ( ("gzip" + "-" ++ (speed.max(1).min(9).round(1)) +
				this.standardizePath.quote).systemCmd != 0 )
			{ "String-gz: failed".postln }
			{ ( "String-gz:\n\tcreated gzipped file" + 
				( this.standardizePath ++ ".gz").pad($') ).postln; };
		^this ++ ".gz";
		}
		
	ungz {
		if ( ("gunzip" + this.standardizePath.quote).systemCmd != 0 )
			{ "String-ungz: failed".postln }
			{ ( "String-ungz:\n\gunzipped file" + this.standardizePath.pad($') ).postln; };
		^this.removeExtension;
		}
		
	targz {  |newPath, speed = 6| 
		var tar;
		tar = this.tar(newPath);
		^tar.gz(speed);
		}
		
	tgz { |newPath, speed = 6| 
		var tar;
		tar = this.tar(newPath);
		^tar.gz(speed).renameTo( tar.basename.removeExtension.replaceExtension( "tgz" ) );
		}
		
	untgz { arg deleteTar=false;
	     var out; 
		var tar;
		tar = this.ungz;
		if( tar.extension != "tar" ) { tar = tar ++ ".tar"; }; // .tar.gz support
		out = tar.untar;
		if( deleteTar ) { tar.removeFile( true, false ) };
		^out
		}
		
	flac { |level = 5, newDir, deleteInputFile = false, flacLocation = "/usr/local/bin/" | // 0-8
		// needs flac to be installed 
		var result, thisStd;
				
		thisStd = this.standardizePath;
		
		result = "%flac% -%  %".format( 
				flacLocation,
				(if( deleteInputFile ) { " --delete-input-file" } { "" }),
				level,
				thisStd.quote
			).systemCmd;
				
		if( result != 0 ) 
			{ "String-flac: failed".postln; ^thisStd; } 
			{  ("String-flac: created\n\t" ++ 
				thisStd.replaceExtension( "flac" ) ).postln;
			if( newDir.notNil )
				{ if( thisStd.replaceExtension( "flac" ).moveTo( newDir ) )
						{  ^(newDir.standardizePath ++ "/" ++ 
							thisStd.basename.replaceExtension( "flac" ) ); }
						{  ^thisStd.replaceExtension( "flac" ); }; }
				{ ^thisStd.replaceExtension( "flac" ); }; 
			};
		}
		
	unflac { |format = 'wav', newDir, deleteInputFile = false, flacLocation = "/usr/local/bin/" | 
	
		// format: 'wav' or 'aiff'
		// needs flac to be installed
		
		var result, thisStd;
				
		thisStd = this.standardizePath;
		
		result = "%flac -d % %".format(
			flacLocation,
			(if( format === 'aiff' ) 
				{ "--force-aiff-format" } 
				{""} ), 
			thisStd.quote ).systemCmd;
				
		if( result != 0 ) 
			{ "String-unflac: failed".postln; }					{ ("String-unflac: created\n\t" ++ 
				thisStd.replaceExtension( format.asString )).postln;
			 if( newDir.notNil )
				{ if( thisStd.replaceExtension( format.asString ).moveTo( newDir ) )
						{  ^(newDir.standardizePath ++ "/" ++ 
							thisStd.basename.replaceExtension( format.asString ) ); }
						{  ^thisStd.replaceExtension( format.asString ); }; }
				{ ^thisStd.replaceExtension( format.asString ); };
			};
		}
		
	flacAll { |level = 5, newDir, extensions, deleteInputFiles = false,  
			flacLocation = "/usr/local/bin/" |
		var files, tdn, dirName, out = [];
		
		extensions = extensions ? [ 'wav', 'aif', 'aiff', 'sd2' ];
		dirName = this.standardizePath;
		
		files = dirName.folderContents.select({ |item|
			extensions.includes( item.extension.asSymbol );
			});
			
		files = files.collect({ |item|
			item[ (dirName.size + 1) ..];
			});
		
		files.do({ |item|
			if( newDir.notNil )
				{ tdn = item.dirname;
				  if( tdn.asSymbol === '.' ) { tdn = "" };
				 
				out = out.add( (dirName ++ "/" ++ item).flac( level, 
					newDir ++ "/" ++ tdn,
					deleteInputFile: deleteInputFiles,
					flacLocation: flacLocation ) ); }
					
				{ out = out.add( (dirName ++ "/" ++ item).flac( level, 
					deleteInputFile: deleteInputFiles,
					flacLocation: flacLocation ) ); };
			});
		
		^out;
		}
		
	unflacAll { |format = 'wav', newDir, deleteInputFiles = false,  
			flacLocation = "/usr/local/bin/" |
		var files, tdn, dirName, out = [];
		
		dirName = this.standardizePath;
		
		files = dirName.folderContents.select({ |item|
			item.extension.asSymbol === 'flac';
			});
			
		files = files.collect({ |item|
			item[ (dirName.size + 1) ..];
			});
		
		files.do({ |item|
			if( newDir.notNil )
				{ tdn = item.dirname;
				  if( tdn.asSymbol === '.' ) { tdn = "" };
				 
				out = out.add( (dirName ++ "/" ++ item).unflac( format, 
					newDir ++ "/" ++ tdn,
					deleteInputFile: deleteInputFiles,
					flacLocation: flacLocation ) ); }
					
				{ out = out.add( (dirName ++ "/" ++ item).unflac( format, 
					deleteInputFile: deleteInputFiles,
					flacLocation: flacLocation ) ); };
			});
		
		^out;
		}
		
	openWith { |appName = "SuperCollider"|
		("open -a" + appName.asString.quote + this.standardizePath.quote ).systemCmd;
		}
		
	openWithID { |id = "com.apple.safari"|
		("open -b" + id + this.standardizePath.quote ).systemCmd;
		}
		
	openInFinder { ("open" + this.standardizePath.quote ).systemCmd; }
	
	showInFinder { ("open" + this.standardizePath.dirname.quote ).systemCmd; }
	
	downloadURL { |to| // input should be valid web adress -- not finished
		if( to.isNil )
			{ };
		}
		
	getHostIP {
		var out;
		if ( (out = { this.gethostbyname.asIPString }.try).isNil )
			{ "String-getHostIP: host '%' not found\n".postf( this );
				^out }
			{ ^out };
		}
		
	afp { |login, password, volume|
		var loginPassword, server;
		
		if( password.notNil )
			{ loginPassword = ":" ++ password.findReplaceAll( " ", "%20"); };
			
		if( login.notNil )
			{ loginPassword = login.findReplaceAll( " ", "%20") 
				++ ( loginPassword ? "" ) 
				++ "@"; }
			{ loginPassword = "" };
		
		if( volume.notNil )
			{ volume = "/" ++ volume.findReplaceAll( " ", "%20"); }
			{ volume = "" };
			
		server = this.findReplaceAll( " ", "%20");
			
		"open afp://%%%"
			.format( loginPassword, server, volume )
			.systemCmd;
	 }
	
	openServer { |login, password, volume| // by default opens volume with same name as server
		^( this.findReplaceAll( " ", "-" ) ++ ".local" )
			.afp( login, password, volume ? this );
		  }
		
	// old
	
	downloadCVSSource { | login = "anonymous", password = "", server = "cvs.sourceforge.net", 
			root = "/cvsroot/supercollider", repository = "SuperCollider3"| 
		var dest;
		if( this == "" )
			{ dest = "~/dev"; }
			{ dest = this; };
		dest.makeDir;
		("cd" + dest.quote ++ 
			"\n" ++
			"cvs -d:pserver:" ++ login ++ "@" ++ server ++ ":" ++ root + "login" ++ 
			"\n" ++
			password ++ "\n" ++  // enter for password
			"cvs -z3 -d:pserver:" ++ login ++ "@" ++ server ++ ":" ++ root + "co" + repository ).unixCmd;
		}
	
	updateCVSSource {  // works?
		var dest;
		if( this == "" )
			{ dest = "~/dev/SuperCollider3/"; }
			{ dest = this; };
		dest.makeDir;
		("cd" + dest.quote ++ "\n" ++ "cvs -z3 update -dP" ++ "\n" ).unixCmd;
		}
		
	// new	 
	
	downloadSVNSource { |folderName = "SuperCollider3",
		repos = "https://svn.sourceforge.net/svnroot/supercollider/trunk"|
		var dest;
		if( this == "" )
			{ dest = "~/dev"; }
			{ dest = this; };
		dest.makeDir;
		( "cd" + dest.quote ++ 
			"\n" ++
		  "svn co" + repos + folderName ).unixCMD;
		}

	}