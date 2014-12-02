Preduce : Pattern {
	var <>selectorPattern, <>list;
	*new { arg selectorPattern ... patterns;
		^super.new.list_(patterns).selectorPattern_(selectorPattern)
	}
	embedInStream { arg inval;
		var selstr, selector, streams, outval, values;
		selstr = selectorPattern.asStream;
		streams = list.collect(_.asStream);
		while {
			selector = selstr.next(inval);
			selector.notNil
		} {
			values = streams.collect { |x| 
				var z = x.value(inval); 
				if(z.isNil) {
					^inval
				};
				z
			};
			outval = values.reduce(selector);
			inval = outval.yield;
		};
		^inval
	}	
}

Pperform : Pattern {
	var <>selectorPattern, <>arguments;
	*new { arg selectorPattern ... arguments;
		^super.new.arguments_(arguments).selectorPattern_(selectorPattern)
	}
	embedInStream { arg inval;
		var selstr, selector, streams, outval, values, receiver;
		selstr = selectorPattern.asStream;
		streams = arguments.collect(_.asStream);
		while {
			selector = selstr.next(inval);
			selector.notNil
		} {
			values = streams.collect { |x| 
				var z = x.value(inval); 
				if(z.isNil) {
					^inval
				};
				z
			};
			receiver = values.removeAt(0);
			outval = receiver.performList(selector, values);
			inval = outval.yield;
		};
		^inval
	}	
}