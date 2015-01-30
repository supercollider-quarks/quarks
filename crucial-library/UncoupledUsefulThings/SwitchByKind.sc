

/*
	k = SwitchByKind( 
		SomeClass , {  },
		AnotherClass, { },
		YetAnotherClass, { },
		Object, { }
		);
		
	o = YetAnotherClass.new;
	// finds the handler for the class or searches superclasses
	k.value(o)
	
*/

SwitchByKind {
	
	var handlers;
	
	*new { arg ... pairs;
		^super.new.init(pairs)
	}
	init { arg pairs;
		handlers = IdentityDictionary.new;
		pairs.pairsDo { arg klass,func;
			handlers[klass] = func
		}
	}
	value { arg object ... args;
		object.class.superclassesDo { arg klass;
			var h;
			h = handlers.at(klass);
			if(h.notNil,{
				^h.valueArray([object] ++ args)
			})
		}
		^nil
	}
}
