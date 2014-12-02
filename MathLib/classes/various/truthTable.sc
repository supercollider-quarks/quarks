+ Function {
	// generates a truth table string for n-ary boolean function
	// e. g. { |a, b, c| a and: b xor: c }.truthTable
	
	truthTable {
		var numArgs = this.def.argNames.size;
		var res = "", fstr = "%\t".dup(numArgs + 1).join ++ "\n";
		[true, false].dup(numArgs).allTuples.do { |tup|
			tup = tup ++ this.(*tup);
			tup = tup.collect( if(_) { "T" } { "F" } );
			res = res ++ format(fstr, *tup)
		};
		^res
	}

}