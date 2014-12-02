+ String {

wordBins {|minLengthOfWords = 2, stopList|
	var index = 0, size, bj = 0, raw, strings, numAppearances;
	
	minLengthOfWords.postln;
	stopList = stopList ? #[];
	raw = this
		.tr($\t, $ ).tr($\n, $ )	// replace \n, \t with blank
		.tr($(, $ ).tr($), $ ).tr(${, $ ).tr($}, $ ).tr($[, $ ).tr($], $ ) // remove paren
		.tr($*, $ ).tr($:, $ ).tr($,, $ ).tr($., $ ).tr($?, $ ) // remove *:,.?
		.tr($/, $ ).tr($|, $ ).tr($\ , $ ).tr($", $ ).tr($$, $ )
		.split($ )				// from upto here -> array
//		.reject{|str| str.size < minLengthOfWords} // reject words < minLengthOfWords chars (including " ")
		.sort;
	
	raw = raw.reject{|word, i|  stopList.any(_ == word) || {word.size < minLengthOfWords}};
	
	size = raw.size;

	// count words
	strings = Array.fill(size, "");
	numAppearances = Array.fill(size, 1);
	while({index < (size-1)}, {
		strings[bj] = raw[index];
		((raw[index].toLower) == (raw[index+1].toLower)).if({
			numAppearances[bj] = numAppearances[bj] + 1;
		}, {
			bj = bj + 1;
		});
		index = index+1;
	});

	// return sorted List of [numAppearance, string]; empty strings are rejected.
	^([numAppearances, strings].flop.reject{|it| it.last == ""}.sort({|a, b| a.first < b.first}));
}
}



+ Document {
	*wordBins {|docs, minLengthOfWords = 2, stopList|
		var str = "";
		
		// concatenate all string from all documents
		Document.allDocuments[0..4].do{|doc|
			str = str + doc.string.stripRTF
		};
		^str.wordBins(minLengthOfWords, stopList);
	}
}
