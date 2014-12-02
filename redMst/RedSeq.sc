//redFrik - released under gnu gpl license

RedSeq {
	var task;
	*new {|indices, beats|
		^super.new.initRedSeq([indices, beats].flop);
	}
	*newArray {|array|
		^super.new.initRedSeq(array);
	}
	initRedSeq {|array|
		task= Task({
			array.do{|x|							//x should be [index, beat]
				RedMst.goto(x[0]);
				x[1].wait;
			};
			RedMst.stop;
			(this.class.name++": sequence finished").postln;
		});
	}
	play {
		task.reset;
		task.play(RedMst.clock);
	}
	stop {
		task.stop;
		RedMst.stop;
	}
	pause {
		task.pause;
	}
	resume {
		task.resume;
	}
}
