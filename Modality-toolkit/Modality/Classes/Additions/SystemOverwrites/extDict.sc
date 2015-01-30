+ Dictionary {
	
	swapKeys {|aKey, bKey|
		var aSrc, bSrc;
		
		// find name clashes in dict and swap keys if there are. Otherwise "rename" key.
		aSrc = this[aKey];
		bSrc = this[bKey];
		
		this[aKey] = bSrc;
		this[bKey] = aSrc;
	}
	
	changeKeyForValue{|newKey, val|
		var oldName, otherVal;
		
		// find name clashes in dict and swap keys if there are. Otherwise "rename" key.
		oldName = this.findKeyForValue(val);
		otherVal = this[newKey];
		
		this[oldName] = otherVal;
		this[newKey] = val;
	}
}
	// move up to dictionary? 
	// dictionary key could also be e.g. a string
+ IdentityDictionary {
	atKeys { |keys|
		if (keys.isKindOf(Collection)) { 
			^keys.collect (this.at(_))
		};
		^this.at(keys);
	}
}
