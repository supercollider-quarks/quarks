
// dependency scheme. experimental

Perhaps : Maybe {

	var <>connected, <>action; // later instead of action can be added others.
	var <>name; // for now.
		
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
		"added connection".postln;
		if(connected.isNil) { connected = IdentitySet.new };
		connected.add(obj);
	}
	
	value_ { arg obj;
		this.releaseGlued;
		value = obj;
		"set value".postln;
		this.catchRecursion {
			this.update(this, obj); // placeholder also forwards change to itself.
			this.changed(obj);
		};
		
	}
	
	update { arg who, what;
		"updated % %\n".postf(who.name, what);
		this.doAction(who, what)	
	}
	
	doAction { arg who, what;
		action.value(this, what);
	}
}

Contract : Maybe {
	var <>condition;
	test {
		^condition.value(value)
	}

}
