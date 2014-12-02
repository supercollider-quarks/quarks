/*

	Copyright 2009 (c) - Marije Baalman (nescivi)
	Part of the FileLog quark

	Released under the GNU/GPL license, version 2 or higher

*/

// writes multiple files from disk, as created by MultiFileWriter,
// uses gzip and tar for unzipping and unbundling

MultiFilePlayer : MultiFileReader{

	var <lineMap;
	var <>headerSize = 0;
	
	*new{ |fn,fc|
		^super.new(fn, fc ? TabFilePlayer ).myInit;
	}

	myInit{
		lineMap = Order.new;
		lineMap.put( 0, 0 );
	}

	fileClass_{ |fc|
		//	fc.postln;
		if ( fc.asClass.isKindOfClass( FilePlayer ) ){
			fileClass = fc;
		}{
			"fileClass must be a (subclass of) FilePlayer".warn;
		}
	}

	currentLine{
		if ( curFile.notNil and: (curid > -1 )){
			if ( lineMap.indexOf( curid ).notNil ){
				^(lineMap.indexOf(curid) + curFile.currentLine - headerSize);
			};
		}
		^(-1)
	}

	goToLine{ |lineid|
		var fid, lineinfile;

		if ( lineMap.indexOf( curid ).isNil ){
			// ensure that the first file is open:
			this.openFile( 0 );
		};

		//	corlineid = lineid; // + ( headerSize * (curid+1) );

		// This bit opens the file that is closest to the lineid we want
		fid = lineMap.slotFor( lineid );

		//	[lineid,lineinfile,fid].postln;

		if ( lineMap.indices.at( fid ) > lineid  ){
			// lineid is below that slot
			this.openFile( lineMap.array.at(fid) - 1 );
		}{
			// lineid is above that slot
			this.openFile( lineMap.array.at(fid) );
		};
		
		if ( curid != - 1 ){
			lineinfile = lineid - lineMap.indexOf( curid ) + headerSize;
			// this skips to the right line in the file, and skips to next file if necessary
			//	lineinfile.postln;
			if ( curFile.goToLine( lineinfile ).not ){
				if ( this.skipToNextFile.not ){
					// there are no more files.
					("Line"+lineid+"does not exist").warn;
					^false;
				};
				if ( curid != -1 ){
					//		("skipped to next file, now going to line again").postln;
					^this.goToLine( lineid );
				}{
					^false;
				};
			}{
				^true;
			};
		};
		^false;
	}

	readHeader{ |fileid,hs|
		headerSize = hs ? headerSize;
		fileid = fileid ? curid;
		if ( fileid == - 1 ){ fileid = 0 };
		this.openFile( fileid );
		^curFile.readHeader(headerSize);
		//	^headerSize.collect{ |it| this.next };
	}

	// called from next, and goToLine;
	skipToNextFile{
		//	[curid, lineMap.at( lineMap.lastIndex) ].postln;
		if ( lineMap.at( lineMap.lastIndex ) == curid ){
			//	[curFile.lineMap.indices,curFile.lineMap.array].flop.postln;
			//	[lineMap.indices,lineMap.array].flop.postln;

			// in case we jumped to the current file,
			// and did not know the starting line number
			// don't write into the lineMap for the next file
			// since the line number will be unknown.
			//	("writing in lineMap"+((curFile.lineMap.lastIndex - headerSize) + lineMap.indexOf(curid)) + (curid+1) ).postln;
			lineMap.put( (curFile.lineMap.lastIndex - headerSize) + lineMap.indexOf(curid), curid + 1 );
		};
		//	[lineMap.indices,lineMap.array].flop.postln;
		//	("skip to next file"+(curid+1)).postln;
		this.openFile( curid + 1 );
		if ( curFile.notNil ){
			// this puts the current pos at the beginning of the data;
			this.readHeader;
			^true;
		};
		^false
	}

	readAtLine{ |line|
		this.goToLine( line );
		^this.next;
	}

	readAtLineInterpret{ |line|
		this.goToLine( line );
		^this.nextInterpret;
	}

	readAt{ |line,fileid|
		if ( fileid.isNil ){
			^this.readAtLine(line);
		};
		if ( curid != fileid ){
			if ( this.openFile( fileid ).isNil ){
				^nil;
			};
		};
		^curFile.readAt( line );
	}

	readAtInterpret{ |line,fileid|
		if ( fileid.isNil ){
			^this.readAtLineInterpret(line);
		};
		if ( curid != fileid ){
			if ( this.openFile( fileid ).isNil ){
				^nil;
			};
		};
		^curFile.readAtInterpret( line );
	}

	makeGui{ |parent|
		^MultiFilePlayerGui.new( this, parent );
	}
}