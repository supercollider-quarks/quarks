//redFrik

RedInterpolator {
	var <value, <>tick, <>clock, <>action;
	var task;
	*new {|value= 0, tick= (1/60), clock|
		clock= clock ? SystemClock;
		^super.newCopyArgs(value, tick, clock);
	}
	goto {|target= 1, dur= 0, curve= 0|
		task.stop;
		task= Routine({
			var steps= (dur/tick).round.asInteger;
			var source= value;
			if(steps==0 or:{target==source}, {
				value= target;
				action.value(value, 1, 0, 0);
			}, {
				steps.do{|i|
					var r= (i+1)/steps;//ramp 0-1
					value= RedTween.value(r, source, target, curve);
					action.value(value, r, i, steps);
					tick.wait;
				};
			});
		}).play(clock);
	}
	value_ {|val|
		if(task.isPlaying, {
			task.stop;
		});
		value= val;
		action.value(value, 1, 0, 0);
	}
}
