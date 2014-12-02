// rohrhuber 10/2005
+ Bag {
	
	// information theory
	uncertainty {
		var sum = 0, size = this.size;
		contents.keysValuesDo {|obj, n|
			var p = n / size;
			sum = sum + (p * log2(p))
		};
		^sum.neg
	}
	
	probabilityOf { arg obj;
		var n = contents.at(obj) ? 0;
		^n / this.size;
	}
	
	uncertaintyOf { arg obj;
		var p = this.probabilityOf(obj);
		^neg( p * log2(p) )
	}
	
	asWeights {
		var values, weights, size = this.size;
		contents.keysValuesDo {|obj, n|
			values = values.add(obj);
			weights = weights.add(n / size)
		};
		^[values, weights]
	}
}
