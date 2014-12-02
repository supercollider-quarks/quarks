// wslib 2005

+ Collection {
	asPerformDict { arg ... methods;
		var dict;
		// all objects in the collection should be members of the same class
		// this is not checked internally
		dict = ();
		dict.put((this.first.class.asString.firstToLower ++ "s").asSymbol,
			this);
		methods.do({ |method|
			dict.put(method.asSymbol, this.collect({ |object|
				object.perform(method.asSymbol) }) );
			});
		^dict;
		}
}

