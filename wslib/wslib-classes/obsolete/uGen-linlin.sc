// wslib 2006

+ UGen {
	
	// scaling support for UGens
	
	// mostly obsolete with new version, except for clipMinMax? But what's the use..
	
	
	clipMinMax { |outMin, outMax, clip = \minmax| // -> replaced by UGen:prune
		case {clip == \minmax }
		 	{ ^this.clip( outMin, outMax ); }
			{clip == \min }
		 	{ ^this.max( outMin ); }
			{clip == \max }
		 	{ ^this.min( outMax ); }
		 	{ true }
		 	{ ^this };
		}
	
	/*
	
	linlin { |inMin, inMax, outMin, outMax, clip=\minmax|
		 ^( (this-inMin)/(inMax-inMin) * (outMax-outMin) + outMin )
		 		.clipMinMax( outMin, outMax, clip );
		 }
	
	linexp { |inMin, inMax, outMin, outMax, clip=\minmax|
		 ^( pow(outMax/outMin, (this-inMin)/(inMax-inMin)) * outMin)
		 		.clipMinMax( outMin, outMax, clip );
		 }
		 
	explin { |inMin, inMax, outMin, outMax, clip=\minmax|
		 ^((log(this/inMin)) / (log(inMax/inMin)) * (outMax-outMin) + outMin)
		 		.clipMinMax( outMin, outMax, clip );
		 }
		 
	expexp { |inMin, inMax, outMin, outMax, clip=\minmax|
		 ^(pow(outMax/outMin, log(this/inMin) / log(inMax/inMin)) * outMin)
		 		.clipMinMax( outMin, outMax, clip );
		 }
		
	*/
		  
	}