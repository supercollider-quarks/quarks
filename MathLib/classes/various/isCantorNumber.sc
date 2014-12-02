+ Integer {

	isCantorNumber {
		var rest = this;
		while { rest > 0 } 
		{
				if(rest mod: 3 == 1) { ^false };
				rest = rest div: 3;
		};
		^true
	}
	mapToCantor {
		^this.isCantorNumber.binaryValue
	}
}

+ AbstractFunction {
	mapToCantor {
		^this.collect(_.mapToCantor)
	}
}

+ Collection {
	mapToCantor {
		^this.collect(_.mapToCantor)
	}
}