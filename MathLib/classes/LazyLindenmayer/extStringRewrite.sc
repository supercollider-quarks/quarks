// stream implementation of lindenmeyer systems, depth first traversal
// a stream of strings, symbols or characters is recursively rewrittenten 
// into a stream of characters

// version 2.0 03/2006

// context-free as well as context-sensitive generative grammars.

// the rules can use strings, strings, chars or functions as values


+ Stream {
	
	// a stream of strings or characters
	
	rewriteString { arg rules, level=1, contextSize, parseRules=true, contextFree;
		var routine;
		
		if(level <= 0) { ^this };
			contextFree = contextFree ? true;
			if(parseRules) {
				rules = rules.collect { arg assoc;
					assoc = assoc.copy;
					assoc.key = assoc.key.asRewritingRule;
					contextFree = contextFree and: { assoc.key.rewritingContextFree };
					assoc
				};
				parseRules = false;
			};
			if(contextSize.isNil) {	
				contextSize = 1;
				rules.do { |assoc| 
					contextSize = max(contextSize, assoc.key.rewritingRuleSize);
				};
			};
		//	 "contextFree: % \n".postf(contextFree);
			if(contextFree) {
		
			// for context-free generative grammar

				routine = Routine { arg inval;
					var outval, dropSize, current, rewritten, count=0;
				 	
					current = "";
					
					loop {
						outval = this.next(inval);
						count = count + 1;
						if(outval.notNil) {
							current = current ++ outval;
						} {
							
							if(current.size == 0) { inval = nil.yield };
						};
						
						if( current.size >= contextSize or: { outval.isNil } ) {
							#rewritten, dropSize 
								= current.matchRules(rules, current, level, rules, count);
							if(rewritten.notNil) {
								rewritten.do { |el| inval = el.yield };
								current = current.drop(dropSize);
	
							} {
								if(current.size == 0) { 
									inval = nil.yield 
								} {
									inval = current.removeAt(0).yield
								}
							}
						};
					};
					
				}
			
			
			} {
				// for context-sensitive generative grammar
				
				routine = Routine { arg inval;
					
					var outval;
					var keySize, count=0, offset=0;
				 	var current="", rewritten, last;
					
					loop {
						outval = this.next(inval); 
						count = count + 1;
						
						if(outval.notNil) {
							current = current ++ outval;
						} {
							if(current.size == 0) { inval = nil.yield };
						};
						if( current.size >= contextSize or: { outval.isNil } ) {
						
							#rewritten, keySize 
								= current.matchRules(rules, count, level, rules);
							
							if(rewritten.isNil) {
								if(offset == 0) {
									inval = current[0].yield;
								}
							} {
								rewritten.drop(offset).do { |el| inval = el.yield };
								offset = 0;
							};
							
							offset = offset + keySize;
							
							current = current.drop(1);
							offset = max(0, offset - 1);
						};
					};
					
				};
		};
		^routine.rewriteString(rules, level - 1, contextSize, parseRules, contextFree)
	
	}
}



+ String {
	
	matchRules { arg rules ... args; // returns [value, dropSize] pair
	
			rules.do { |assoc|
						var res = assoc.key.matchForRewriting(this, assoc.value, args);
						if(res.notNil) {
							^res
						}
			}
			^[nil, 0]
	}
	
	matchForRewriting { arg string, value, args;
		^if(string.beginsWith(this)) {
			[value.valueArray(args), this.size] // 'this' is key
		} {
			nil
		}
	}
	
	rewriteString { arg rules, level=1, contextSize; // the whole string is rewrittenten at once
		^all(this.iter.rewriteString(rules, level, contextSize)).join
	}
	
	asRewritingRule {
		var x, a, b, key, rewriteMask, dropSize;
		a = this.indexOf($<);
		b = this.indexOf($>);
		
		if(a.isNil and: { b.isNil }) { ^this }; // contextFree		
		x = this.delimit { |x| x === $< or: {x === $> } };
		if(a.isNil) { // only >
		 	rewriteMask = "%";
		 	key = x.join;
		 	dropSize = b;
		}{
			if(b.isNil) { // only <
				rewriteMask = x[0]++ "%";
				key = x.join;
				dropSize = key.size;
			}{
				if(b < a) { 
					Error("illegal syntax. \"<\" must precede \">\".").throw 
				} {	// both < >
					rewriteMask = x[0] ++ "%" ++ x[2];
					key = x.join;
					dropSize = b - 1; // because one preceeding char (<) has been removed
				}
			}
		};
		^[key, rewriteMask, dropSize] // for format method
		
	}
	
	rewritingRuleSize { ^this.size }
	rewritingContextFree {^true }
	
}

+ Array { // array is an object with a context. 
	
	matchForRewriting { arg string, value, args;
		var key = this.first;
		var res;
		^if(string.beginsWith(key)) {
			res = this[1].format(value.valueArray(args));			[res, this[2]] 
		} { 
			nil 
		};
	}
	asRewritingRule { ^this }

	rewritingRuleSize { ^this.first.size }
	rewritingContextFree {^false }
}


