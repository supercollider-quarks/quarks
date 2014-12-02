+ Object {
	atKey { |key| ^nil }
}

+ Pbind {
	atKey { |key|
		patternpairs.pairsDo { |k, val|
			if(k === key) { ^val }
		};
		^nil
	}
}

+ Pchain {
	atKey { |key|
		patterns.do { |p|
			if ( p.class == Pbind ){ ^p.atKey( key )};
		};
		^nil
	}
}

+ PatternProxy{
	atKey{ |key|
		^source.atKey( key )
	}
}

/*
+ Pdef{

	at{ |key|
		var ret;
		try{
			ret = source.patternpairs.clump(2).detect({ |it| it[0] == key })[1]
		}{ ret = nil };
		^ret;
	}

}
*/