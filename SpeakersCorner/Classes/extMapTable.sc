+ SequenceableCollection {	 

	asMapTable { |sizePow2=8| 

		var insize = this.size, maxval = this.last;
		var tabsize = (2 ** sizePow2).asInteger; 
		var numvals = tabsize / (insize - 1);
		var outTable = Signal.newClear(tabsize); 
		
		this.doAdjacentPairs { |a, b, i| 
			var startIndex = (i * numvals).round(1);
			var endIndex = (i + 1 * numvals).round(1);
			var incr = b - a / (endIndex - startIndex); 
			(startIndex..endIndex - 1).do { |j| 
				outTable.put(j, a + (j - startIndex * incr));
			};
		};
		^outTable;
	}
}

/*inpos -> outpos
0.0 -> 0.0
0.3 -> 0.4
0.7 -> 0.8
1.0 -> 1.2
1.5 -> 1.6
2.0 -> 2.0 (same as 0.0)

a = [0, 0.3, 0.7, 1, 1.5, 2.0].asMapTable; 
a.plot;
s.boot; 

b = Buffer.sendCollection(s, a.asWavetable, 1);
b.getn(0, 255, { |a| a.postln });

c = { |inpos=0.0| 
	Shaper.kr(b.bufnum, inpos.wrap(0, 2)).poll;
}.play;

c.set(\inpos, -1);
c.set(\inpos, -0.99);
c.set(\inpos, -0.6);
c.set(\inpos, -0.2);
c.set(\inpos, 0.2);
c.set(\inpos, 0.6);
c.set(\inpos, 0.99);
c.set(\inpos, 1.0);
*/

