/*
Possibly : Maybe {
	var <>conditions;
	
	source_ { arg obj;
		
		this.value = obj;
	}
	reduceFuncProxy { arg args, protect=true;
		var val = super.reduceFuncProxy(args, protect);
		conditions.do { |x| 
			var res = x.value(val, this);
			res !? { ^res }; 
		};
		^val
	}

	
	addCondition { arg func;
		conditions = conditions.add(func);
	}
}


// dependency scheme. experimental

Perhaps : Maybe {

	var <>connected, <>action;
	
	
	// find better name.
	glue { 
		current !? {
			this.addDependant(current);
			current.addConnection(this);
		}
	}
	
	releaseGlued {
		connected !? { 
			connected.do { |x|
				x.removeDependant(this);
			};
			connected.makeEmpty;
		}
	}
	
	addConnection { arg obj;
		if(connected.isNil) { connected = IdentitySet.new };
		connected.add(obj);
	}
	
	value_ { arg obj;
		this.releaseGlued;
		value = obj;
		this.catchRecursion {
			this.update(this, obj);
			this.changed(obj);
		};
		
	}
	
	update { arg who, what;
		if(who !== this) {
			"updated % %\n".postf(who, what);
			this.doAction(who, what)
		} {
			"didn't do it.".postln;
		};	
	}
	
	doAction { arg who, what;
		action.value(this, what);
	}
}
*/