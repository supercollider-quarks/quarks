// Discs (c) 2008-2011 Tom Hall. 
// "DIatonic Set ClasseS
// reg /*at*/ ludions /*dot*/ com 
// GNU licence, http://gnu.org/copyleft/
// Latest version available at www.ludions.com/sc/
// version 2009-04-11 


Discs {

	var <sharpsArr, <flatsArr, <c7Arr, <sharpsC7, <flatsC7, <d3rds;
	
	*new { ^super.new.makeLists }
	
	makeLists {
		sharpsArr = ["C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"];
		flatsArr = ["C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"]; 
		c7Arr = (0..(11)).collect({|el| if(el.odd, {(el+6)%12}, {el})});
		sharpsC7 = c7Arr.collect({|el, i| sharpsArr[el] });
		flatsC7 = c7Arr.collect({|el, i| flatsArr[el] });
	}
	
	indexOfC7 {|int|
		^c7Arr.detectIndex({|el| el==int})
	}
	
	nameNonClem { |tonic|
		^[0, 7, 2, 9, 4, 11].includes(tonic) // sharp keys, else flat
	}
	
	namesC7 {|tonic=0, card=7|
		var notes;
		notes = if(this.nameNonClem(tonic), {sharpsC7}, {flatsC7});
		if(tonic==6, {notes.put(5, "Cb")}); // hack to have correct note names for Gb maj
		^notes.rotate(13-this.indexOfC7(tonic)).keep(card)
	}
	
	intsC7 {|tonic=0, card=7|
		^c7Arr.rotate(13-this.indexOfC7(tonic)).keep(card)
	}
	
	// stepwise
	intsSw {|tonic=0, card=7|
		var ints, index;
		ints = this.intsC7(tonic, card).sort;
		index = ints.detectIndex({|el| el==tonic});
		^ints.rotate(index.neg);
	}
	
	ints3rds { |tonic=0, card=7|
		var arr, rtnArr = []; 
		arr = this.intsC7(tonic, card);
		arr = arr.rotate(-1).extend(12,  nil).clump(4);
		arr[0].do{|i, j| rtnArr = rtnArr ++ [i, arr[1][j], arr[2][j] ] };
		^rtnArr = rtnArr.reject({|i| i.isNil});	
	}
	
	names3rds {|tonic=0, card=7|
		var notes;
		notes = this.noteNames(tonic, card);
		^this.ints3rds(tonic, card).collect({|el, i| notes[el]})
	}
	
	noteNames { |tonic=0, card=7|
		var	 notes = if(this.nameNonClem(tonic), {sharpsArr}, {flatsArr});
		if(tonic==6, {notes.put(11, "Cb")}); // hack to have correct note names
		^notes
	}
	
	namesSw {|tonic=0, card=7|
		var notes;
		notes = this.noteNames(tonic, card);
		^this.intsSw(tonic, card).collect({|el, i| notes[el]})
	}
	
		
	sortC7 { |arr|
		^arr.sort({ arg a, b; 
			c7Arr.detectIndex({|el| el==a}) <= c7Arr.detectIndex({|el| el==b}) 
		});
	}

	// superset is the reference set in 3rds against which to sort
	sort3rds { | arr, superset | 
		^arr.sort({ arg a, b; 
			superset.detectIndex({|el| el==a}) <= superset.detectIndex({|el| el==b}) });
	}

	complement { |aSet, superTonic=0, superCard=12|
		if(aSet.any({|i| i.isString}), {
			"Set must be in integer format".error; // for now			^nil	
		});
		^symmetricDifference(aSet, this.intsC7(superTonic, superCard))
	}

	findIntervals { |subset, superset|
		^subset.collect({|pc| 
				superset.detectIndex({|el| el==pc}) 
		});
	}

	checkSubset { |subset, superset|
		if(subset.isSubsetOf(superset), {
			^true
		}, {
			format("Subset element(s) not included in superset: %", subset.difference(superset)).error;
			^false;
		});
	}
	
}


/*
a = Discs.new

a.namesC7(0, 12);
a.namesC7(0, 10);

a.namesC7(6, 12);
a.namesSw(6, 9)
a.intsC7(3)
a.intsC7(4)
a.intsSw(3)
a.namesSw(3, 7, false)
a.namesSw(4, 8)
a.namesC7(4, 8);
a.namesSw(11);
a.intsC7(11);
b = a.intsSw(11);
// puts a set into c7 order
a.sortC7([2, 7, 0, 3]) // (may be not rotated to match d)

b = a.intsC7(3)
a.sortC7(b)
a.complement(b)
h = a.namesSw(3, 7) //names
a.complement(h) // error warning
h = a.intsSw(3)
a.complement(h) //OK

a= Discs.new
a.checkSubset([0, 1], Set[0, 2, 4, 6])
a.namesC7(0, card:7);
b = a.intsC7(0, card:7); // integers
a.findIntervals([0, 1, 11], b)

a.findIntervals([0, 2, 11], b) // => [1, 3, 6]
// ie [0, 2, 11] have positions [1, 3, 6] in [ 5, 0, 7, 2, 9, 4, 11 ]

a.c7Arr
a.namesC7(0, 12);
a.namesC7(0, 10);

a = Discs.new
a.ints3rds(0, 7)
a.names3rds(0, 7)
a.names3rds(0, 8) // 3rds not really possible

*/