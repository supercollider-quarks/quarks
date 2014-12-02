// this file is part of redUniverse /redFrik

//--todo:
//make into pattern

RedLSystem {
	var <>axiom, <rules, <production, <generation;
	*new {|axiom, rules|
		^super.new.axiom_(axiom).rules_(rules).reset
	}
	next {
		production= this.prRewrite(production);
		generation= generation+1;
	}
	reset {
		production= axiom.as(Array);
		generation= 0;
	}
	rules_ {|dict|
		rules= dict.keysValuesChange{|k, v| v.as(Array)};
	}
	asString {
		^production.flat.join;
	}
	
	//--private
	prRewrite {|x|
		if(x.size==0, {
			^rules[x] ? x;
		}, {
			^x.collect{|y| this.prRewrite(y)};
		});
	}
}
