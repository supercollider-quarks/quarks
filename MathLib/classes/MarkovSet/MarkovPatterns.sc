
Pfsm2 : Pattern {
	var <>markovset, <>repeats;
	
	*new { arg markovset, repeats=1;
		^super.newCopyArgs(markovset, repeats).minit;
	}
	
	minit { 
		if(markovset.size == 0, {"no elements contained in this MarkovSet".postln; ^nil});
		if(markovset.seeds.isEmpty, { markovset.makeSeeds });
	}
	
	embedInStream { arg inval;
		^markovset.embedInStream(inval, repeats) // responsibility stays with markov set
	}
	

}


//uses a pattern of indices to walk through a MarkovDict. uses wrapped indexing.

Pmswitch : Pfsm2 {
	var <>which;
	*new { arg markovset, which, repeats=1;
		^super.new(markovset, repeats).which_(which)
	}
	embedInStream { arg inval;
		var item, bag, inStream, index; 
		if(markovset.order > 1, { "not yet implemented".postln; ^nil });
				inStream = which.asStream;
          		repeats.do {
                		item = markovset.seeds.wrapAt(inStream.next);
                	
                		while {  
                			bag = markovset.dict.at(item); //.postln;
                			index = inStream.next; //.postln;
                			index.notNil
                		} {
                		
                			item = if(bag.isNil) {nil}
                			{
                				item = bag.wrapAt(index.asInteger)
                			};
              		item.embedInStream(inval);
                		}
                	};
       
	}

}

Pspy : FilterPattern { 
	var <>markovset, <>repeats;
	
	*new { arg markovset, pattern, repeats = inf;
			^super.newCopyArgs(pattern, markovset, repeats)
		
	}
	embedInStream { arg inval;
		^markovset.embedSpyInStream(pattern, repeats, inval) // spy responsibility stays with set
	}
}





/*
Pfsm3 : Pfsm2 {
	// passes an array of data each time it is called. [item, weight, number of items] (see WeighBag::infoChoose) 
	
	asStream {
		if(markovset.order > 1, { "not yet implemented".postln; ^nil });
		^Routine.new({ arg inval;
				var items, bag;
            		items = [markovset.seeds.choose, 1, 1];
                	length.do({ 
                		if(items.notNil, { bag = markovset.dict.at(items.at(0)) });
                		items = if( bag.notNil, { bag.infoChoose }, { nil });
              			items.embedInStream(inval);
                	})
             })
	}

}

*/

