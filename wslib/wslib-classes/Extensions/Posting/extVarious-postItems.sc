// wslib 2005

// way of posting all items of an array with formatting

+ SequenceableCollection {
	postItems { |nPerLine, delim = ", ", startString = "\n", endString = "\n", newLinePre = "\t" |
		if(nPerLine.isNil) 
			{nPerLine = this.size};
		this.do({ |item, i|
		 	if( (i%nPerLine) == 0 )
		 		{ if(i == 0)
		 			{ startString.post; }
		 			{ ("\n" ++ newLinePre).post; }; };
			item.asCompileString.post;
			if(i < (this.size - 1)) 
				{ delim.post; }
				{ endString.post; };
			
			});
		^this;
		}
	
	}

+ Dictionary {
	
	postItems { |nPerLine, keyDelim = " : ", delim = "", newLinePre = "\t" |
		if(nPerLine.isNil) 
			{nPerLine = 1};
		this.keysValuesDo({ |key, item, i|
		 	if( (i%nPerLine) == 0 )
		 		{ ("\n" ++ newLinePre).post; };
			(key ++ keyDelim ++ item).post;
			if(i < (this.size - 1)) { delim.post; };
			});
		"\n".post;
		^this;
		}
			
}
