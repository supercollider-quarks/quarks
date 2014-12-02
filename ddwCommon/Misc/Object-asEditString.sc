
+ Object {
	asEditString { // arg cacheKey;
		// like compilestring, but stores objects that can't be represented as strings
		// (like functions) and substitutes a reference for that object in the global Library
		
		^String.streamContents({ arg stream; 
			this.storeEditOn(stream, this.cacheKey);
		});
	}
	
	storeEditOn { arg stream, cacheKey;
		var	thisCompStr,	// try for simple compilestring from primitive
			index,		// object index
			args,		// holder for storeArgs
			branch;		// branch of Library for this object
		
		thisCompStr = this.pr_asCompileString;	// try to get compile string
		
			// if primitive returns this (not a string), not a simple class; duplicate storeOn
		thisCompStr.isString.not.if({
			(args = this.storeArgs).notEmpty.if({
				stream << this.class.name << ".new(";
				args.storeEditItemsOn(stream, cacheKey);
				stream << ")";
			}, {
					// get object index:
				(branch = Library.at(\objectCache, cacheKey)).isNil.if({
					index = 0;	// 0 if no subobjects have been cached
				}, {
						  // 1 + max if others exist
					index = branch.keys.asArray.maxItem.asInteger + 1;
				});
				Library.put(\objectCache, cacheKey, index, this);
				stream << ("Library.at(\\objectCache, '" ++ cacheKey
					++ "', " ++ index ++ ")")
			});
		}, {
			stream << thisCompStr
		});
	}	
	
	purgeObjectCache {
		Library.global.removeAt(\objectCache, this.cacheKey)
	}
	
		// this should be unique per master parent object, for easy cleanup
	cacheKey { ^(this.class.name ++ this.hash).asSymbol }
	
	pr_asCompileString { _ObjectCompileString }	// call prim but don't do storeOn
}

+ Collection {
	storeEditOn { | stream, cacheKey |
		if (stream.atLimit) { ^this };
		stream << this.class.name << "[ " ;
		this.storeEditItemsOn(stream, cacheKey);
		stream << " ]" ;
	}
	storeEditItemsOn { | stream, cacheKey |
		this.do { | item, i |
			if (stream.atLimit) { ^this };
			if (i != 0) { stream.comma.space; };
			item.storeEditOn(stream, cacheKey);	// here's the recursion
		};
	}
}

+ Dictionary {
	storeEditItemsOn { arg stream, cacheKey, itemsPerLine = 5;
		var last, itemsPerLinem1;
		itemsPerLinem1 = itemsPerLine - 1;
		last = this.size - 1;
		this.associationsDo({ arg item, i;
			item.storeEditOn(stream, cacheKey);
			if (i < last, { stream.comma.space;
				if (i % itemsPerLine == itemsPerLinem1, { stream.nl.space.space });
			});
		});
	}
}

+ String {
	storeEditOn { arg stream;
		stream.putAll(this.asCompileString);
	}
}

+ Array {
	storeEditOn { | stream, cacheKey |
		if (stream.atLimit) { ^this };
		stream << "[ " ;
		this.storeEditItemsOn(stream, cacheKey);
		stream << " ]" ;
	}
}
		
+ Array2D {
	storeEditOn { | stream, cacheKey |
		stream << this.class.name << ".fromArray(";
		[rows,cols,this.asArray].storeEditItemsOn(stream, cacheKey);
		stream << ")";
	}
}

+ Association {
	storeEditOn { | stream, cacheKey |
		stream << "(";
		key.storeEditOn(stream, cacheKey);
		stream << " -> ";
		value.storeEditOn(stream, cacheKey);
		stream << ")";
	}
}

+ Boolean {
	storeEditOn { | stream, cacheKey |
		stream.putAll(this.asCompileString);
	}
}

+ Char {
	storeEditOn { | stream, cacheKey |
		stream.putAll(this.asCompileString);
	}
}

+ Class {
	storeEditOn { | stream, cacheKey |
		stream << name;
	}
}

+ Complex {
	storeEditOn { | stream, cacheKey |
		stream << "Complex(";
		real.storeEditOn(stream, cacheKey);
		stream << ", ";
		imag.storeEditOn(stream, cacheKey);
		stream << ")";
	}
}

+ Editor {
	storeEditOn { | stream, cacheKey |
		value.storeOn(stream);
	}
}

+ Nil {
	storeEditOn { | stream, cacheKey |
		stream.putAll(this.asCompileString);
	}
}

+ Point {
	storeEditOn { | stream, cacheKey |
		stream << "Complex(";
		x.storeEditOn(stream, cacheKey);
		stream << ", ";
		y.storeEditOn(stream, cacheKey);
		stream << ")";
	}
}

+ Ref {
	storeEditOn { | stream, cacheKey |
		stream << "`(";
		value.storeEditOn(stream, cacheKey);
		stream << ")";
	}
}

+ SimpleNumber {
	storeEditOn { | stream, cacheKey |
		stream.putAll(this.asString);
	}
}

+ Symbol {
	storeEditOn { | stream, cacheKey |
		stream.putAll(this.asCompileString);
	}
}
