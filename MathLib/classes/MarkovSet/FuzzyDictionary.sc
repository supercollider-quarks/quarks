/* dictionary that keeps objects with the same key in a list and returns randomly one of them */



FuzzyDictionary : IdentityDictionary {	put { arg key, item;		super.put(key, super.at(key).add(item))	}		at { arg key;		var list = super.at(key);		list !? { ^list.choose };
		^nil	}		removeAt { arg key, item;		var list = super.at(key);
		list !? { 
				if(item.isNil) { 
					item = list.removeAt(list.size.rand) 
				} {
					list.remove(item);
				};
				if(list.isEmpty) { this.keyPut(key, nil) };
				^item 
		};		^nil	}	choose {		var list = super.choose;		list !? { ^list.choose };
		^nil	}
	
	/*
	
	// problem:
	// collect, select, reject should iterate over the objects
	
	
	keysValuesDo { arg function;
		this.keysValuesArrayDo(array, { |key, val, i|
			val.do { |el, j| function.(key, el, i, j) }
		})
	}
	
	prKeysValuesDo {
		this.keysValuesArrayDo(array, { |key, val, i|
			function.(key, val, i)
		})
	}

	
	associationsDo { arg function;
		super.associationsDo(function)
	}
	*/		doAt { arg key, function;		super.at(key).do(function);	}		keyAt { arg key;		^super.at(key)	}
	
	keyPut { arg key, item;
		item ?? { super.removeAt(key); ^this };
		super.put(key, item)
	}	}


FuzzySet : FuzzyDictionary {
	var <reverseLookup;
	
	*new { arg size=8;
		^super.new(size).initLookup;
	}
	
	initLookup {
		reverseLookup = IdentityDictionary.new;
	}

	put {arg key, item; // any identical item is removed first
		var set;
		var oldkey = reverseLookup.removeAt(item);
		oldkey !? { this.removeAt(oldkey, item) };
		
		set = this.keyAt(key);
		if(set.isNil) {
			set = IdentitySet.new; 
			this.keyPut(key, set) 
		};
		set.add(item);
		reverseLookup.put(item, key);
	}
	
	remove { arg item;
		var oldkey;
		item ?? {^this };
		oldkey = reverseLookup.removeAt(item);
		oldkey !? { this.removeAt(oldkey, item) };
		^nil
	}
	
	replace { arg item, by;
		var oldkey = reverseLookup.removeAt(item);
		oldkey !? {
			this.removeAt(oldkey, item);
			this.put(oldkey, by)
		};
		
	}
	
	removeAt { arg key, item;
		var res = super.removeAt(key, item);
		item !? { reverseLookup.removeAt(item) };
		^res
	}
}


