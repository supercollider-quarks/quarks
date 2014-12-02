// wslib 2009 
// roman literals
/*

Date.getDate.year.collect({ |i| (i+1).digit2roman; }).clump(5).collect( _.join(", ") ).join(",\n");

"1, 2, 3, 4, 5".digit2roman;

2.8.digit2roman

"MCMLXXVII".roman2digit;

"IM".roman2digit; // illegal syntax -> wrong calculation (only C can preceed M)
999.digit2roman; // this is how it should be written

http://www.novaroma.org/via_romana/numbers.html

*/

+ String {
	roman2digit { 
		var y;
		var a = 0;
		var x = ( I: 1, V: 5, X: 10, L: 50, C: 100, D: 500, M: 1000 );
		var z = ( I: [ \V, \X ], X: [ \L, \C ], C: [ \D, \M ] );
		var i = 0;
		
		y = this.toUpper.as( Array ).collect(_.asSymbol)
			.select({ |item| x.keys.includes( item ) });
		
		while { i < y.size }
			{
			if( z[ y[i] ].asCollection.includes( y[i+1] ) )
				{ a = a + ( x[y[i+1]] - x[y[i]] ); i = i+2; }
				{ a = a + x[y[i]]; i = i+1; }
			  };
		^a;
		}
	
	digit2roman { ^this.doToNumbers( _.digit2roman ); }
	
	}
	
+ Number {

	digit2roman {
	
		var a = this.round(1);
		var y = "";
		var x = [ [I: 1], [V: 5], [X: 10], [L: 50], [C: 100], [D: 500], [M: 1000] ];
		var z = ( M: \C, D: \C, C: \X, L: \X, X: \I, V: \I );
		
		x.reverseDo({ |item, i|
			var key, val, sub;
			#key, val = item;
			
			while { (a - val) >= 0 }
				 { y = y ++ key; a = a - val; }; 
				 
			sub = x.detect({ |it| it[0] == z.at(key) });
			
			if( sub.notNil && { (a - (val - sub[1])) >= 0 } )
				{ a = a - (val - sub[1]); y = y ++ sub[0] ++ key; };
			});
		^y;
		
		}
	
	
	}
	