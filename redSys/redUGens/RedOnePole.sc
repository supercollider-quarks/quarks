RedOnePole {
	var <>up, <>down, prev= 0;
	*new {|up= 0.99, down|
		^super.newCopyArgs(up, down?up);
	}
	onepole {|in|
		var res;
		if(in>prev, {
			res= in+(up*(prev-in));
		}, {
			res= in+(down*(prev-in));
		});
		prev= res;
		^res;
	}
	
	//--ugen
	*ar {|in, up= 0.99, down|
		var buf, prev, res;
		down= down?up;
		buf= LocalBuf(1).clear;
		prev= Dbufrd(buf);
		res= if(in>prev, in+(up*(prev-in)), in+(down*(prev-in)));
		^Duty.ar(SampleDur.ir, 0, Dbufwr(res, buf));
	}
	*kr {|in, up= 0.99, down|
		var buf, prev, res;
		down= down?up;
		buf= LocalBuf(1).clear;
		prev= Dbufrd(buf);
		res= if(in>prev, in+(up*(prev-in)), in+(down*(prev-in)));
		^Duty.kr(ControlDur.ir, 0, Dbufwr(res, buf));
	}
}