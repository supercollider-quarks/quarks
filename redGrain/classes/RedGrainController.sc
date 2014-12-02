//redFrik 050331-050607

RedGrainController {
	var <>delta= 0.05, >grainBuf, >grainRate, >positionStart, >positionSpeed,
		>grainDur, >grainPan, >grainAmp, <>gain, >density,
		redGrain, task;
	*new {|redGrain|
		^super.new.initRedGrainController(redGrain);
	}
	initRedGrainController {|argRedGrain|
		redGrain= argRedGrain;
		grainBuf= redGrain.buf;
		grainRate= redGrain.rate;
		positionStart= {|i| i};
		positionSpeed= 1;
		grainDur= redGrain.dur;
		grainPan= redGrain.pan;
		grainAmp= redGrain.amp;
		gain= ();
		density= 1;
	}
	start {
		task= Task({
			inf.do{|i|
				redGrain.pos= this.prFuncPosition(redGrain.pos, i);
				redGrain.mute= true;
				if(density.value(redGrain.pos, i).coin, {redGrain.mute= false});
				redGrain.buf= grainBuf.value(redGrain.buf, redGrain.pos, i);
				redGrain.rate= grainRate.value(redGrain.rate, redGrain.pos, i);
				redGrain.dur= grainDur.value(redGrain.dur, redGrain.pos, i);
				redGrain.pan= grainPan.value(redGrain.pan, redGrain.pos, i);
				redGrain.amp= this.prFuncAmplitude(redGrain.pos, i);
				delta.wait;
			};
		}).play;
	}
	stop {task.stop}
	pause {task.pause}
	resume {task.resume}
	
	//--private
	prFuncPosition {|pos, i|
		var buf, step;
		buf= redGrain.buf;
		step= delta/(buf.numFrames/buf.sampleRate)/buf.numChannels*positionSpeed.value(pos, i);
		^positionStart.value(pos+step, i)%1.0;
	}
	prFuncAmplitude {|pos, i|
		var bufnum, amp, boost;
		bufnum= redGrain.buf.bufnum;
		amp= grainAmp.value(redGrain.amp, pos, i);
		boost= gain[bufnum] ? 1.0;
		^amp*boost;
	}
}
