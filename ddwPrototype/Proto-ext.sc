
+ Function {
	// environment safety: asynchronous functions don't remember
	// the environment - scheduled funcs, OSCresponders, etc.
	// this is an easy way to make an environment-safe function
	e { ^this.inEnvir }
}

	// in Proto, parent should contain all functions; all data should go in main dict
	// some data maybe should go in parent: provide a collection of keys
+ IdentityDictionary {
	moveFunctionsToParent { |keysToMove|
		var	movedKeys;	// because you shouldn't delete things from a collection under iteration
		movedKeys = IdentitySet.new;
		parent.isNil.if({ parent = this.class.new });
			// kvDo iterates only over main dict, not parent
		this.keysValuesDo({ |key, val|
			(val.isFunction or: { keysToMove.notNil and: { keysToMove.includes(key) } }).if({
				movedKeys.add(key);
				parent.put(key, val);
			});
		});
		movedKeys.do({ |key|
			this.removeAt(key);
		});
	}
}

// for importing methods

+ Dictionary {
	asProtoImportable {}
}

+ Ref {
	asProtoImportable { ^this.value.asProtoImportable }
}
