VarProxy {

	classvar dict;

	*initClass {
		dict = IdentityDictionary.new;
	}

	*asENInput{ |key, value|
		var v = Var(value);
		if(dict.at(key).notNil){
			"overwritting current Var at %".format(key).warn
		};
		dict.put(key,v);
		^Writer( v, Tuple3([],[],[]) )
	}

	*enIn { |key, value|
		^ENDef.appendToResult( this.asENInput(key, value) )
	}

	*new{ |key,value|
		if( dict.at(key).notNil ) {
			dict.at(key).value_(value)
		} {
			"VarProxy: no Var at %.".format(key).postln;
		}
	}

}


/*

(

//network
~networkDescription = ENDef({
    //inputs
    var x = VarProxy.enIn(\x, 5);
    x.enDebug(\x)

});
//compile network
~network = EventNetwork(~networkDescription, true);

//start network
~network.start;

)

VarProxy(\x, 24)
VarProxy(\y, 24)

*/

ENdef  {

	classvar <>all;
	var <key;
	var <eventNetwork;

	*initClass { all = IdentityDictionary.new }

	*new { | key, f | //object is a function
		var check = this.checkArgs(\ENdef, \new, [key, f], [Symbol, [Function,Nil]]);

		var en = all.at(key);
		en = en ?? {
			var x = this.basicNew( key, nil);
			all.put(key, x);
			x
		};
		f !? { en.setSource(f) };
		^en
	}

	*basicNew{ |key, en|
		^super.newCopyArgs(key, en)
	}

	start{
		eventNetwork !? _.start
	}

	stop{
		eventNetwork !? _.stop
	}

	clear{
		if(eventNetwork.notNil and: {eventNetwork.active}) {
			eventNetwork.stop;
			eventNetwork = nil;
		}
	}

	setSource { |f|
		var d = "setSource %".format(f).postln;
		var active = false;
		if(eventNetwork.notNil and: {eventNetwork.active}) {
			eventNetwork.stop;
			active = true;
		};
		if( f.notNil ) {
			eventNetwork = EventNetwork(ENDef(f));
			if(active) { eventNetwork.start }
		} {
			eventNetwork = nil
		}
	}

}
		