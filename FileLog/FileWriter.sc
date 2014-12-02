/*

	Copyright 2009 (c) - Marije Baalman (nescivi)
	Part of the FileLog quark

	Released under the GNU/GPL license, version 2 or higher

*/

FileWriter : File {

	classvar <delim = $ ;
	
	var <>delimiter;
	var <>timeStamp = false;

	var <>timebase = \local; // other option \gm

	var <>stringMethod = \asString;

	
	*new { arg pathName, mode="w", stamp = false, del; 
		^super.new(pathName, mode).timeStamp_( stamp ).delimiter_( del ? this.delim );
	}
	
	/*
	*new { arg pathName, mode="w"; 
		^super.new(pathName, mode);
	}
	*/

	writeLine{ |array|
		if ( timeStamp ){
			if ( timebase == \local ){
				this.write( Date.localtime.stamp.perform( stringMethod ) );
			}{ // gm
				this.write( Date.gmtime.stamp.perform( stringMethod ) );
			};
			this.write( delimiter );
		};
		array.do{ |it,i|
			this.write( it.perform( stringMethod ) );
			if ( i < ( array.size - 1) ){
				this.write( delimiter );
			};
		};
		this.write("\n");
	}

}

TabFileWriter : FileWriter { 
	classvar <delim = $\t;
}

CSVFileWriter : FileWriter { 
	classvar <delim = $,;
}

SemiColonFileWriter : FileWriter { 
	classvar <delim = $;;
}