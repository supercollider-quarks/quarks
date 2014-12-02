IntervalTable {

/*
	 There are two kinds of interval tables: one obtained from the Huygens-Fokker Foundation list:
	 http://www.huygens-fokker.org/docs/intervals.html (consulted 03/10/07) containing 356 	 intervals inside an octave, or a table generated with the program JST for the Atari ST by 	 Clarence Barlow (see http://www.musikwissenschaft.uni-mainz.de/Autobusk) for which there are
	 several lists, each filtered by a minimum harmonicity of the ratios and encompassing 9 octaves 	 above and below unison (+/- 10800 cents)
	 method *loadTable is used to load them with arguments \huygens or \JST, if \JST is the case,
	 an additional argument for minimum harmonicity should be provided (\030, \035, \040, \045 or
	 \050)
	 
*/	
	classvar <table, <>tableType, <>tableMin;
	
	*initClass {
		IntervalTable.loadTable;
	}
	
	*loadTable {|type = \JST, min = \030, path |
		var ark, filename;
		path = path ?? {Platform.userAppSupportDir ++ "/quarks/DissonanceLib/"};
		tableType = type;
		if (type == \huygens) {tableMin = nil} {tableMin = min};
		type.switch(
			\huygens, {filename = "huygensList.int" },
			\JST, { filename = "JSTList" ++ min ++ ".int" },
			{^"Invalid type"}
		);
		ark = ZArchive.read(path ++ filename);
		table = ark.readItem;
		ark.close;	
		^"table " ++ filename ++ " loaded...";
	}
	
	
	*classify {|centsList, tolerance = 6| // in cents
		var result = Array.newClear(centsList.size), key;
		key = tableType.switch(\JST, \harmon, \huygens, \names);
		centsList.do{|cent, i|
			table.cents.do{|x,j|
				if ( (cent - x).abs <= tolerance ){
					result[i] = result[i].add([table.ratios[j], table.cents[j], table[key][j]])
				}
			};
		};
		^result;
	}

}

/*(c) 2008 jsl*/