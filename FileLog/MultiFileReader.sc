/*

	Copyright 2009 (c) - Marije Baalman (nescivi)
	Part of the FileLog quark

	Released under the GNU/GPL license, version 2 or higher

*/

// writes multiple files from disk, as created by MultiFileWriter,
// uses gzip and tar for unzipping and unbundling

MultiFileReader{

	var <indexfn;
	var <indexFile;

	var <>fileClass;

	var <pathDir;
	var <fileName;
	//	var <extension;

	var <tarBundle;
	var <zipSingle;

	var <curFile;
	var <curid = -1;

	*new{ |fn,fc|
		^super.new.init(fn).fileClass_( fc ? TabFileReader );
	}

	init{ |fn|
		var path = PathName(fn);
		fileName = path.fileNameWithoutExtension;
		tarBundle = (path.extension == "tar");
		pathDir = PathName(path.asAbsolutePath).pathOnly;
		//		pathDir = pathDir ;
		if ( tarBundle ){
			// not sure whether fileName is the right thing to do?
			indexfn = fileName +/+ fileName ++ "_index";
			("tar -f" + fn + "-x" + indexfn ).systemCmd;
			this.openIndexFile;
			//.unixCmdThen( {this.openIndexFile} );
		}{
			if ( path.isFolder ){
				indexfn = pathDir +/+ fileName +/+ fileName ++ "_index";
			}{
				indexfn = pathDir +/+ fileName ++ "_index";
			};
			this.openIndexFile;
		};
	}

	openIndexFile{
		var line;
		indexFile = TabFilePlayer.new( indexfn );

		// read the first line:
		line = indexFile.next;
		indexFile.reset; // reset the file again.
		zipSingle = (PathName(line.last).extension == "gz");
		//	extension = PathName(line.last).fileNameWithoutExtension
	}

	readIndexLine{ |ind|
		ind = ind ? curid;
		^indexFile.readAt( ind );
	}

	reset{
		this.openFile( 0 );
		curFile.reset;
	}

	openFile{ |ind|
		var line, path;

		//	("opening file"+ind+",curid"+curid).postln;

		if ( curid == ind ){
			^curFile;
		}{
			this.closeFile;
		};

		line = this.readIndexLine( ind );

		//		"line".postln;
		//		line.postcs;
		//		line.size.postln;

		if ( line.isNil ){
			("File with id"+ind+"does not exist").warn;
			curid = -1;
			curFile = nil;
			^nil;
		};

		if ( line.size < 2 ){
			("File with id"+ind+"does not exist").warn;
			curid = -1;
			curFile = nil;
			^nil;
		};

		path = line.last;

		if ( tarBundle ){
			this.extractFromTar( path );
			//	("tar -f" + pathDir +/+ fileName ++ ".tar" + "-x" + fileName +/+ path ).systemCmd;
		};
		if ( zipSingle ){
			("gzip -d" + fileName +/+ path ).systemCmd;
			path = PathName( line.last ).fileNameWithoutExtension;
		};

		//[ pathDir, fileName, path ].postln;
		curFile = fileClass.new( pathDir +/+ fileName +/+ path );
		//		curFile.postln;
		if ( curFile.isNil ){
			("File with id"+ind+"does not exist").warn;
			curid = -1;
		}{
			curid = ind;
		};
		//	[curid,curFile].postln;
		^curFile;
	}

	extractFromTar{ |path|
			("tar -f" + pathDir +/+ fileName ++ ".tar" + "-x" + fileName +/+ path ).systemCmd;
	}

	closeFile{
		if ( curFile.notNil ){
			curFile.close;
			curid = -1;
		};
	}

	close{
		indexFile.close;
		this.closeFile;
	}

	skipToNextFile{
		this.openFile( curid + 1 );
		// return true if file exists.
		^curFile.notNil;
	}

	next{
		var res;
		if ( curFile.isNil ){
			this.openFile( 0 );
		};
		res = curFile.next;
		if ( res.isNil ){
			this.skipToNextFile;
			res = curFile.next;
		};
		^res;
	}

	nextInterpret{
		^this.next.collect( _.interpret );
	}

}