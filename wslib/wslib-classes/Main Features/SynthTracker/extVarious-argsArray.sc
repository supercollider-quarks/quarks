// methods for conversion of dictionaries to arrays that can be used
// as args for Synth's and server messages
//
// part of wslib 2005
//
// ( freq: 440, gate: 1 ).asArgsArray
// --> [\freq, 440, \gate, 1]
// and vice versa of course..

// there are other methods in SC to do similar things, these are used by a lot
// of wslib classes

+ SequenceableCollection {
	
	isArgsDict { ^false }
	isArgsArray { ^this.clump(2).flop[0].every({ |item| item.class == Symbol })
		&& this.size.even } // check if it's valid
	
				
	asArgsArray { |check = false| 
		if( check && { this.isArgsArray.not } ) 
			{  "SequenceableCollection-asArgsArray:\ninput is not a valid argsArray".warn; };
		^this }
		
	asArgsDict { |check = false|
		var dict, thisClumped;
		if( check && { this.isArgsArray.not } ) 
			{  "SequenceableCollection-asArgsDict:\ninput is not a valid argsArray".warn; };
		thisClumped = this.clump(2);
		dict = (); // preferred Class for argsDict is Event
		this.clump(2).do({ |item| dict.put( item[0], item[1]) });
		^dict;
		}
		
	asArgsString { arg delim = ",", equalSign = "=", prepend = "args ", append = ";";
		^this.asArgsDict.asArgsString(delim, equalSign);
		}
		
	flattenArgsArray { |check = false|
		var newArray;
		if( check && { this.isArgsArray.not } ) 
			{  "SequenceableCollection-asArgsDict:\ninput is not a valid argsArray".warn; };
		^this.asArgsDict.flattenArgsDict.asArgsArray;
		}
	}

+ Dictionary {
		
	isArgsArray { ^false } 
	isArgsDict { ^this.keys.every({ |item| item.class == Symbol }); }
	
	asArgsDict { |check = false|
		if( check && { this.isArgsDict.not } )
			{ "Dictionary-asArgsDict:\ninput is not a valid argsDict".warn };
		if(this.class != Event) { ^().putAll(this) } { ^this } }
		
	asArgsArray { |check = false|
		var argsArray = this.asKeyValuePairs;
		//this.keysValuesDo( { |key, value| argsArray = argsArray ++ [key, value] });
		if( check && { argsArray.isArgsArray.not } ) 
			{  "Dictionary-asArgsArray:\ninput is not a valid argsDict".warn; };
		^argsArray;
		}
	
	asSortedArgsArray { |check = false|
		var argsArray = [];
		this.sortedKeysValuesDo( { |key, value| argsArray = argsArray ++ [key, value] });
		if( check && { argsArray.isArgsArray.not } ) 
			{  "Dictionary-asArgsArray:\ninput is not a valid argsDict".warn; };
		^argsArray;
		}
		
	asArgsString { arg delim = ",", equalSign = "=", prepend = "arg ", append = ";"; 
		// just for posting or export: no backwards version
		var string = "";
		this.keysValuesDo({ |key, value, i|
			string = string ++ key + equalSign + value;
			if(i < (this.keys.size - 1)) {string = string ++ delim ++ " "; } });
		^prepend ++ string ++ append;
		}
	
	postSorted { 
		"(".post;
		this.sortedKeysValuesDo({ |key, value, i|
			(" " ++ key ++ ": " ++ value ++ ", ").post;
			if((i%4) == 3) { "\n ".post; };
			});
		")".postln;
		}		
		
	
	flattenArgsDict { |check = false|
		var newDict = ();
		if( check && { this.isArgsDict.not } )
			{ "Dictionary-asArgsDict:\ninput is not a valid argsDict".warn };
		this.keysValuesDo({ |key, value|
			var count=1;
			if(value.size != 0)
				{ if(key.asString.last.isDecDigit)
						{ count = key.asString.last.asString.interpret;
							key = key.asString[0..(key.asString.size -2)]; };
					value.do({ |item, i|
						newDict.put((key ++ (i+count)).asSymbol,
							item);
					}) }
				{ newDict.put(key, value); };
			});
		^newDict;
		}

	}
	
+ Function {

	valueArgsArray { arg array; // array can also be Dict or regular array for valueArray
		if(array.isArgsArray or: array.isArgsDict) 
			{ ^this.valueWithEnvir(array.asArgsDict);}
			{ ^this.valueArray(array); }
		}
		
	valueArgsDict { arg event; ^this.valueWithEnvir(event.asArgsDict); }
	
	}
	
+ Nil { 
	// strange but true..
	asArgsArray { ^this } asArgsDict { ^this } 
	isArgsDict { ^true } isArgsArray { ^true } 
	}
