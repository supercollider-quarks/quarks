// By Jascha Narveson and Charles CŽleste Hutchins

Scala : Tuning {
/*@
shortDesc: open Scala files
longDesc: Reads Scala files and creates a Tuning basedon them.  It can also generate a Scale that includes every interval in the Tuning.  See Tuning for the principle methods.

More information about the Scala file format, and a link to the scale library, can be found at: 
http://www.huygens-fokker.org/scala/scl_format.html
@*/
//

	var <pitchesPerOctave;

	
	*new { arg path;
		/*@
		desc: open a Scala file
		path: path to the Scala file
		ex: 
		a = Scala("slendro.scl");
		@*/
	
		^super.new.initOpen(pathname: path);
	
	}

	*open { arg path;
		/*@
		desc: a more intiutive syntax for opening a Scala file
		@*/
		
		^super.new.initOpen(pathname: path); 
	}
	
	
	
	initOpen{ arg pathname;
	
		var file,lines,line, ratios, num;
		
		tuning = [];

		// read the file
		// ok to read all the data into memory because it's small
		file = File.open(pathname,"r"); // open the .scl file
		lines = [];	// start an array for the relevant lines of the file
		line = 0;
		while ({line != nil},
			{
				line=file.getLine; 
				if(line.isNil==false,
					{
						if(line.contains("!").not, 
								// ignore commented .scl file lines (starting with "!")
							{
								lines = lines.add(line);
								//line.postln;
							} // look at non-commented lines 
						);
					}
				);
			}		
		);
		file.close;
		
		
		// parse the results
		name = lines.removeAt(0);
		name = name.asString; // the first line will the the name
		pitchesPerOctave = lines.removeAt(0).asInteger;
		lines.do({|line|  // each scale pitch will be either in ratio or cents notation
			//line.post;
			(line.contains(".").not).if({
			
					// the ratio case
					num = line.interpret.ratiomidi;
					tuning = tuning ++ num;
				},
				{// the cents case
					//"as float: ".post; i.asFloat.postln;
					num = line.asFloat;
					num = num / 100;
					tuning = tuning ++ num;
			});
		});

		tuning = tuning.addFirst(0); // the interval 1/1 is not explicitly stated in the .scl file
		octaveRatio = tuning.pop.midiratio;
		
	}
	
	
	scale {
	
		/*@
		desc: Generates a Scale which contains as a degree every step in the tuning
		ex:
		a = Scala("slendro.scl");
		b = a.scale;
		@*/
		
		var degrees;
		
		degrees = Array.series(pitchesPerOctave, 0, 1);
		^Scale(degrees, pitchesPerOctave, this, name);
	}

}


/*
 The scale archive, and rules for formatting .scl files, are described at
 <http://www.xs4all.nl/~huygensf/scala/scl_format.html> 
 as of Nov.11, 2005:
 
	¥ 	The files are human readable ASCII or 8-bit character text-files.
	¥ 	The file type is .scl .
	¥ 	There is one scale per file.
	¥ 	Lines beginning with an exclamation mark are regarded as comments and are to be ignored.
	¥ 	The first (non comment) line contains a short description of the scale, preferably not exceeding 80 characters, but longer lines are possible and should not give a read error. The description is only one line. If there is no description, there should be an empty line.
	¥ 	The second line contains the number of notes. This number indicates the number of lines with pitch values that follow. In principle there is no upper limit to this, but it is allowed to reject files exceeding a certain size. The lower limit is 0, which is possible since degree 0 of 1/1 is implicit. Spaces before or after the number are allowed.
	¥ 	After that come the pitch values, each on a separate line, either as a ratio or as a value in cents. If the value contains a period, it is a cents value, otherwise a ratio. Ratios are written with a slash, and only one. Integer values with no period or slash should be regarded as such, for example "2" should be taken as "2/1". The highest allowed numerator or denominator is 231-1 = 2147483647. Anything after a valid pitch value should be ignored. Space or horizontal tab characters are allowed and should be ignored. Negative ratios are meaningless and should give a read error. For a description of cents, go here.
	¥ 	The first note of 1/1 or 0.0 cents is implicit and not in the files.
	¥ 	Files for which Scala gives Error in file format are incorrectly formatted. They should give a read error and be rejected.

 So these lines are all valid pitch lines:

81/64
408.0
408.
5
-5.0
10/20
100.0 cents
 100.0 C#
 5/4   E\

 Here is an example of a valid file:

! meanquar.scl
!
1/4-comma meantone scale. Pietro Aaron's temperament (1523)
 12
!
 76.04900
 193.15686
 310.26471
 5/4
 503.42157
 579.47057
 696.57843
 25/16
 889.73529
 1006.84314
 1082.89214
 2/1

 An advise for writing a scale file: put the filename on the first line behind an exclamation mark. Then someone receiving the file and reading it knows a name under which to save it.
	*/

