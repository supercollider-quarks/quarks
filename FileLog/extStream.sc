+ Stream{

	// skips a number of lines and returns the result of the last line.
	skipNextN{ |n,inval|
		var res;
		n.do{ res = this.next(inval) };
		^res;
	}

}