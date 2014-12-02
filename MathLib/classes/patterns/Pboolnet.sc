/*
 	
 	an adaption of Sekhar C. Ramakrishnan boolean network pattern.
 	(inspired by http://farben.latrobe.edu.au/mikropol/volume6/dorin_a/dorin_a.html)
 	rewritten to make shorter and more efficient Julian Rohrhuber 2005
 	


*/

Pboolnet : Pattern {
	var <>nodes, <>repeats, <>loops;
	
	*new { arg nodes, repeats=1, loops=1;
		if(nodes.size.odd) { Error("nodes must be pairs of values").throw };
		^super.newCopyArgs(nodes, repeats, loops)
	}
	
	embedInStream { arg inval;
		var bools, newBools, selectorStreams, loopStream;
		
		nodes.pairsDo { |val, selector|
			inval = val.yield;
			bools = bools.add(val == 1);
			selectorStreams = selectorStreams.add(selector.asStream);
		};
		
		newBools = bools.copy;
		loopStream = loops.asStream;
		
		inval = this.embedLoopInStream(inval, bools, loopStream.value - 1);

		repeats.value.do {
			
			selectorStreams.do {|selector, i|	
				var bool;
				selector = selector.value;
				if(selector.isNil) { ^inval };
				bool = if(selector === 'not') {
					bools[i].not
				}{
					perform(bools @@  (i-1), selector, bools @@ (i+1))
				};
				newBools[i] = bool;
				inval = bool.binaryValue.yield;
				
			};
			
			inval = this.embedLoopInStream(inval, bools, loopStream.value - 1);
			
			bools = newBools.copy;
		
		}
		^inval

	}
	
	embedLoopInStream { arg inval, bools, n;
			(n * bools.size).do { |i| 
		 		inval = (bools @@ i).binaryValue.yield 
			}
			^inval
	
	}

}





