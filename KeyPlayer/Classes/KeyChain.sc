KeyChain {
	classvar <all, <>verbose = false; 
	var <key, <>list, <>index = 0, <nextIndex = 0, <nextKey, <>repeats = 1; 
	
	*initClass { all = () }
	
	*new { |key, list, index|
		var res = all[key]; 
		if (res.isNil) { 
			res = super.newCopyArgs(key, list ?? { [] }, index ? 0);
			all.put(key, res);
		} { 
			if (list.notNil) { res.list_(list) };
			if (index.notNil) { res.index_(index) };
		};
		
		^res
	}
	
	loop { repeats = inf }

	value { |...args| this.next(*args) }

	next { |...args|
		var which; 
		index = nextIndex;
		which = list.wrapAt(index);
		this.findNext(which.to);
		
		if (which.notNil) { which.act(*args) }; 
		
		 if (verbose) { 
			 "KeyChain(%) did % at %.\n".postf(key, which.key, index);
			 "	up next: % at %.\n".postf(nextKey, nextIndex);
		};
	}

	reset { index = 0 }
	
	findNext { |nextInfo| 
		if (nextInfo.isKindOf(Symbol)) { 
			nextIndex = list.detectIndex { |dict| dict.key == nextInfo };
			if (nextIndex.notNil) { nextKey = nextInfo };
		} {
			nextIndex = nextInfo ?? { index + 1 } % list.size; 
			nextKey = list[ nextIndex ].key;
		}
	}
	
	chooseNext { ^list.choose.to }
	
	act { |keys| 
		keys.do { |key| 
			var which;
			which = if (key.isNumber) { 
				list.wrapAt(key); 
			} { 
				list.detect { |it| it.key == key };
			};
			which !? { which.act };
		};	
	}
}