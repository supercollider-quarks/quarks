// addition to Note.sc
// part of wslib

+ String {
	asNote { |cents|	^Note(this, cents:cents) }
}

+ SimpleNumber {
	asNote {
		^if(this < 128)
		 	{Note(midi:this)} 
		 	{Note(freq:this)}
		}
}

+ Symbol {
	asNote { |cents|	^Note(this.asString, cents:cents) }
}

+ Array {
	
	asNote { |cents|	
		^this.collect({ |item|
			item.asNote(cents) });
	}
}
