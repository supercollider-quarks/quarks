//wslib 2005
// ++ support; not in place

+ Dictionary {
	++ { arg aDict;
		var newDict;
		newDict = this.copy;
		^newDict.putAll(aDict);
		}
	}