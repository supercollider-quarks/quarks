// W. Snoei 2005

+ Object {
	asCode { ^this.asCompileString; }
	}
	
+ FlowLayout { // special version for FlowLayout
	asCode { ^(this.class.asString ++ "(" + [bounds, margin, gap].join( ", ") + ")" ); }
	}
