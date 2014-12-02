// wslib 2007 
// generate compact code (only for Points so far;  Point(1,2).asShortCS -> "1@2" )

+ Point {
	asShortCS {
		if( (x.size == 0) && { x.rate == \scalar })
			{ ^"%@%".format( x, 
				if( y.sign == -1 ) 
					{ "(" ++ y ++ ")" } 
					{ y } )
			}
			{ ^this.asCompileString }
		}
	}
	
+ Object { 
	asShortCS { ^this.asCompileString }
	}