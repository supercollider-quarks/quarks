//f.olofsson & j.liljedahl 2010-2011

//todo: use SkipJack for permanent clocks or just recreate the routine at cmdperiod?

ClockAudioMulch : TempoClock {
	var <>addr, <>tick= 0, <>shift= 0, task;
	*new {|tempo, beats, seconds, queueSize= 256, addr|
		^super.new.initClockAudioMulch(tempo, beats, seconds, queueSize, addr);
	}
	start {|startTick=0|
		tick = startTick;
		task= Routine({
			inf.do{
				addr.sendMsg( if(tick==startTick, \t_start, \t_pulse), (tick+shift).asInteger, 0.0, 0.0);
				(this.beatDur/24.0).wait;
				tick= tick+1;
			};
		}).play(this);
	}
	initClockAudioMulch {|tempo, beats, seconds, queueSize, argAddr|
		addr= argAddr ?? {NetAddr("127.0.0.1", 7000)};
		this.start(0);
		^super.init(tempo, beats, seconds, queueSize);
	}
	stop {
		addr.sendMsg(\t_stop, (tick+shift).asInteger, 0.0, 0.0);
		task.stop;
		super.stop;
	}
}
