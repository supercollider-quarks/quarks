
	// I often need descriptive information about an object for drag-n-drop purposes
	// This stores a ref to an object with a name and a "type" symbol

NamedRef : Ref {
	var	<>name, <>type, <>properties;
	
	*new { arg thing, name, type, properties;
		^super.new(thing)
			.name_(name)
			.type_(type)
			.properties_(properties ?? { IdentityDictionary.new })
	}
	
	setProperty { |key, value|
		properties.put(key, value)
	}
	
	getProperty { |key| ^properties[key] }
	
		// a shortcut for getting a property
		// only works if the property name is something that is not already a method of
		// Ref or Object
	doesNotUnderstand { |selector ... args|
		properties.keys.includes(selector).if({
			^properties[selector]
		}, {
			super.doesNotUnderstand(selector, args)	// if not a valid property, die
		});
	}
	
	tryPerform { |selector ... args|
		properties.keys.includes(selector).if({
			^properties[selector]
		}, {
			this.respondsTo(selector).if({
				^this.performList(selector,args)
			}, { ^nil })
		})
	}
	
		// need more information than Ref-printOn provides
	printOn { arg stream;
		stream << "NamedRef(" << value << ", " << name << ", " << type << ")"
	}
	
	storeEditOn { | stream, cacheKey |
		stream << "NamedRef(";
		value.storeEditOn(stream, cacheKey);
		stream << ", ";
		name.storeEditOn(stream, cacheKey);
		stream << ", ";
		type.storeEditOn(stream, cacheKey);
		stream << ", ";
		properties.storeEditOn(stream, cacheKey);
		stream << ")";
	}
	
	asString { ^name.asString }		// for display
}
