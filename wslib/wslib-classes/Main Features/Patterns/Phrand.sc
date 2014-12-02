// wslib 2011
// history aware random
// chooses item from a list without repeating
// histSize sets the number of items to be remembered for repetition
// setting histSize to the size of the list - 1 results in a repetitive pattern
/*

x = Phrand( (..10), 10, inf ).asStream;
x.nextN(100).plot2;

x = Phrand( (..10), 9, inf ).asStream; // slowly changing
x.nextN(100).plot2;

// sound example:
Pbind( \note, Phrand( (..12), 11, inf ), \dur, 1/8 ).play;

// proof of concept
x = Phrand( (..10), 1, inf ).asStream; 

(
y = x.value;
100.do({
	var val;
	val = x.value;
	if( y == val ) { "repetition occurred! (%)\n".postf(val) };
	y = val;
});
)

// now try again with:
x = Phrand( (..10), 0, inf ).asStream; // histsize == 0: allow repetitions

// or:
x = Prand( (..10), inf ).asStream;

*/

Phrand : ListPattern {
	var <>histSize;
	
	*new { arg list, histSize=2, repeats=1;
		^super.new(list, repeats).histSize_(histSize)
	}
	
	embedInStream { arg inval;
		var item, size, history = [], histSizeStr = histSize.asStream;
		var index;
		repeats.value(inval).do({ arg i;
			var count = 0;
			size = list.size;
			index = size.rand;
			while { history.includes( index ) && { count < 1000 }; } // prevent inf loop
				{ index = size.rand; count = count + 1 };
			item = list.at(index);
			history = ([ index ] ++ history)[..(histSizeStr.next(inval))-1];
			inval = item.embedInStream(inval);
		});
		^inval;
	}
}