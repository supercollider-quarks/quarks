//redFrik

RedDirection {

	//--lang
	var <>last;
	*new {|start= 0|
		^super.new.last_(start);
	}
	direction {|val|
		var dir;
		dir= if(val>last, {1}, {if(val<last, {-1}, {0})});
		last= val;
		^dir;
	}
	
	//--ugen
	*ar {|in|
		var dir;
		dir= HPZ1.ar(in);
		^(dir>0)+(dir<0).neg
	}
	*kr {|in|
		var dir;
		dir= HPZ1.kr(in);
		^(dir>0)+(dir<0).neg
	}
}
