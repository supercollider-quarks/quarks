+ SequenceableCollection {

// accepts an array of intervals in the form [ [p1, q1], [p2, q2], ... [pn, qn] ]
	specificHarmonicity {
		var indigestibleSum = 0, intraScale = [], n = this.size;
		this.do{|i,k|
			this[k..].do{|j,l|
				if (i != j) {
					intraScale = intraScale.add(j.ratioDiv(i));
				};
			};
		};
		intraScale = intraScale.removeDuplicates;
		intraScale.do{|x| var h;
			h = x[0].indigestibility + x[1].indigestibility;
			indigestibleSum = indigestibleSum + h;
		};
		^(n * (n - 1 )) / (2 * indigestibleSum)
	}


}