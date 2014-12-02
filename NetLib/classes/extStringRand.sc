
+String {
	// a markov set would maybe be better
	
	*rand { arg length = 8, nCapitals = 0, pairProbability = 0.2;
		var consonants = "bcdfghjklmnpqrstvwxz";
		var vowels = "aeiouy";
		var cweight = #[ 0.07, 0.03, 0.07, 0.06, 0.07, 0.03, 0.01, 0.07, 0.07, 0.06, 0.07, 0.06, 
								0.01, 0.06, 0.07, 0.07, 0.01, 0.03, 0.01, 0.04 ];
		var vweigth = #[ 0.19, 0.19, 0.19, 0.19, 0.19, 0.07 ];
		var lastWasVowel = false;
		var last, res, ci, breakCluster=false;
		res = this.fill(length, { |i|
						var vowel = if(breakCluster.not and: {pairProbability.coin}) 
									{ÊbreakCluster = true; lastWasVowel.not } 
									{ breakCluster = false; lastWasVowel };
						if(vowel) {
							lastWasVowel = false;
							last = vowels.wchoose(vweigth)
						} { 
							lastWasVowel = true;
							last = if(last == $q) { $u } { 
								consonants.wchoose(cweight)
							};
						};
		});
		if(nCapitals > 0) {
			ci = [0] ++ (2..length-2).scramble.keep(nCapitals - 1);
			if(ci.size < nCapitals) { ci = ci.add(length-1) };
			if(ci.size < nCapitals) { ci = ci.add(1) };
			ci.do {|i|
				res[i] = res[i].toUpper;
			};
		};
		^res
	}
}

+Symbol {
	*rand { arg length=8, nCapitals=0;
		^String.rand(length, nCapitals).asSymbol
	}
}

