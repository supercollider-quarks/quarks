// wslib 2012
//
// returns a sorted array of random values
// *new can also be used in language
// trig:	(ar and kr versions) trigger new values
// n:      number of items
// minVal: minimum value
// maxVal: maximum value
// warp:   a distribution warp (as seen in ControlSpec)
// spread: equality of distribution; 
// 		0 = full linear (no random)
//		1 = equal distribution ( all steps < (2*n) )
//		2 = similar to ({0.0 rrand 1.0}!n).sort; total random
//		3 and up = exaggerated spread

SortedRand {
	
	*new { |n = 10, minVal = 0, maxVal = 1, warp = \lin, spread = 1|
		if( minVal.isNumber && { maxVal.isNumber } ) {
			^this.prMapValues( {0.0 rrand: 1.0}!(n+1), minVal, maxVal, warp, spread );
		} {
			^this.prMapValues( Rand(0.dup(n+1),1), minVal, maxVal, warp, spread );
		};
	}
	
	*ir { |n = 10, minVal = 0, maxVal = 1, warp = \lin, spread = 1|
		^this.prMapValues( Rand(0.dup(n+1),1), minVal, maxVal, warp, spread );
	}
	
	*kr { |trig = 0, n = 10, minVal = 0, maxVal = 1, warp = \lin, spread = 1|
		^this.prMapValues( TRand.kr(0.dup(n+1),1, trig), minVal, maxVal, warp, spread );
	}
	
	*ar {  |trig = 0, n = 10, minVal = 0, maxVal = 1, warp = \lin, spread = 1|
		^this.prMapValues( TRand.ar(0.dup(n+1),1, trig), minVal, maxVal, warp, spread );
	}
	
	*prMapValues { |values, minVal = 0, maxVal = 1, warp = \lin, spread = 1|
		var spec;
		spec = [minVal, maxVal, warp].asSpec;
		values = values.pow(spread).integrate;
		values = values / (values.last); // normalize
		values.pop; // remove last item
		^spec.map( values );
	}
	
}

Scramble {
	
	// scramble an array
	// if trig == 1, scrambling will be performed once at init
	// trig can not be audio rate
	
	// this Pseudo-UGen can easily use up a lot of UGens. At array sizes > 12
	// the ugen graph will become too large to send (must load in such cases).
	// Arrays of size > 100 will crash the Server
	
	// functionality like this could probably be made much more efficient as a real UGen in c++
	
	*ar { |array, trig = 1|
		^Select.ar( this.indices( array.size, trig ), array );
	}
	
	*kr { |array, trig = 1|
		^Select.kr( this.indices( array.size, trig ), array );
	}
	
	*indices { |size = 10, trig = 1|
		var weights;
		weights = DC.kr(1.dup(size));
		^size.collect({ |i|
			var val;
			val = TWindex.kr( trig, weights, 1 );
			weights = weights.collect({ |item, i|
				(item - InRange.kr( val, i-0.5,i+0.5 )).max(0);
			});
			val;
		});
	}
	
}