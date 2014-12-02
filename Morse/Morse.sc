
Morse { 
	classvar <>verbose = true; 
	classvar <>dot = 0.1, <>dash = 0.3, <>intra = 0.1, <>short = 0.3, <>medium = 0.7; 
	
	*timesFor { arg char = $x; 
		var code, times;
		code = MorseDict.at(char);
	
		if (code.notNil, { 
			times = code.collect({ arg code, i; 
				[ [ dot, dash, medium ] @ code, intra ];
			}).flat; 
			times.putLast(short);

		}, { if (verbose) { ("Morse found nothing for key: " + char).inform }; [] });
		
		^times
	}
	
	*word { arg word; 
		var times = word.as(Array).collect { |char| this.timesFor(char) }.reject(_.isEmpty);
		var lastword = times.last; 
		if (lastword.notNil) { lastword.putLast(medium) };
		^times
	}
	
	*new { arg text = "Morse Code";
		^text.split($ ).collect { |word| this.word(word) }.reject(_.isEmpty);
	}
	
	*signs { arg text; ^MorseDict.wordSigns(text) }
	
}
