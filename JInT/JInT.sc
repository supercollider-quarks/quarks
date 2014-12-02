/**
	2006  		Till Bovermann (IEM)
	2007, 2008  	Till Bovermann (Uni Bielefeld)

	base class for Just InTerface 
*/
JInT {
	var <controllers; // a dict of controller representations (JInTController)
	var cMap;
	
	*new {
		^super.new.initJInT;
	}
	initJInT {
		cMap = TwoWayIdentityDictionary.new;
		controllers = [];
	}
	startCustom {"JInT-start: abstract method".warn}
	stopCustom {"JInT-stop: abstract method".warn}
	start {
		// setup controller-map and start controllers
		controllers.do{|cont, i| cMap[cont.short] = i; cont.start};
		// run subclass start method
		this.startCustom;
	}
	stop {
		controllers.do(_.stop);
		// run subclass stop method
		this.stopCustom;
	}
	at {|keys = 0|
		var res;
		try {
			res = keys.isKindOf(Collection).not.if({
				this.basicAt(keys);
			}, {
				keys.collect{|key| this.basicAt(key)};
			});
		} {
			(format("JInT-at: malformed argument (%). has to be either a valid key, an index, or a collection of these.\n", keys)).warn;
		};
		^res
	}
	basicAt {|key|
		key.isKindOf(Symbol).if({
			^controllers[cMap[key]];
		}, {
			^controllers[key];
		});
	}
	info {
		postf("%\n", this.class);
		controllers.do{|cont, i|
			postf("\t  '%' (%) - \"%\" (% DOF) \n\t\t-> %\n", cont.short, i, cont.description, cont.numDOF, cont.semantics.flatten.asSet.asArray);
		}
	}
}
