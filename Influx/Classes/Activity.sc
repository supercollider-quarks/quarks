Activity {
	var <>decaytime, <>dt, <level = 0, <floor = 0.0001, <skip, <>action;

	*new { |decaytime = 10, dt = 0.02|
		^super.newCopyArgs(decaytime, dt).init;
	}

	init { skip = SkipJack({ this.decay }, { dt }); }

	add { |val=0| level = (level + (val / max(1, level))) }

	decay {
		level = level * (0.001 ** (dt / decaytime));
		if (level < floor) { level = 0 };
		action.value(this);
	}

	play { skip.play }
	stop { skip.stop }
}