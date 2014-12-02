//f.olofsson & j.liljedahl 2010-2011

//todo:	legato in pbind not working properly - why? (is this still the case??)

//edits by jonatan liljedahl:
// - works for multiple routines using the same clock
// - restart routines at t_start 0, so we get the behaviour of oldschool midi-synced hardware boxes:
//   1 press start on master, slaves starts at zero
//   2 press stop at master, slaves stop
//   3 press start again, slaves starts again at zero
//   pausing should work out of the box..
// - fractional tick scheduling should work, even though the precision isn't perfect
//   (sometimes it lags one tick behind)
// - got rid of waitForStart
// - added default clock
// - added permanent flag, beatsPerBar, start and stop actions.

AudioMulchClock {
	var	<running = false, <tick = 0, <>shift = 0, <>beatsPerBar= 4, <>permanent= false,
		<tempo = 1, <>startAction = nil, <>stopAction = nil, avg, lastTime = 0,
		queue, start, stop, pulse;
	classvar defaultClock = nil;
	*new {
		^super.new.initAudioMulchClock;
	}
	*default {
		^defaultClock ?? {
			(defaultClock = AudioMulchClock.new).permanent = true;
		};
	}
	initAudioMulchClock {
		queue= PriorityQueue.new;
		avg= FloatArray.newClear(10);
		start= OSCresponderNode(nil, \t_start, {|t, r, m|
			var items = [];
			if(m[1]!=tick, {
				(this.class.name++": start "++m[1]).postln;
				if(PriorityQueue.findMethod(\do).notNil) { // 3.5 support
					queue.do {|item| items = items.add(item)};
				} {
					queue.array.pairsDo{|time, item| items = items.add(item)};
				};
				queue.clear;
				items.do {|item| item.reset; this.schedAbs(m[1].roundUp(beatsPerBar*24), item)};
			}, {
				(this.class.name++": resumed "++m[1]).postln;
			});
			lastTime = Main.elapsedTime;
			running= true;
			this.startAction.value;
			this.doPulse(t,r,m);
		}).add;
		stop= OSCresponderNode(nil, \t_stop, {|t, r, m|
			if(running, {
				(this.class.name++": stop").postln;
				running= false;
				stopAction.value;
			});
		}).add;
		pulse= OSCresponderNode(nil, \t_pulse, {|t, r, m|
			this.doPulse(t,r,m);
		}).add;
		
		CmdPeriod.doOnce {this.clear};
	}
	doPulse {|t,r,m|
		var time, avgs;
		tick = m[1]-shift;
		avg.put(tick%10, Main.elapsedTime-lastTime);
		lastTime = Main.elapsedTime;
		avgs = avg.sum;
		tempo = 10/(avgs*24);
		while({time = queue.topPriority; time.notNil and:{time.floor<=tick}}, { 
			this.doSched(time-tick, queue.pop, avgs/tick.clip(1,10));
			//note: avg.sum won't be correct before the first 10 ticks.. probably not a big deal in most cases.
		});
		running= true;
	}
	doSched {|ofs, item, tickdur|
		var delta;
		//("doSched tickdur: "++tickdur++" ofs: "++ofs++" tick: "++tick).postln;
		SystemClock.sched(ofs * tickdur, {
			delta = item.awake(tick.asFloat, Main.elapsedTime, this);
			if(delta.isNumber, {
				this.sched(delta+(ofs/24), item);
			});
			nil;
		});
	}
	play {|task, quant= 1|
		this.schedAbs(this.nextTimeOnGrid(quant), task);
	}
	beatDur {
		^1/tempo;
	}
	beats {
		^tick/24;
	}
	sched {|delta, item|
		queue.put(tick+(delta*24), item);
	}
	schedAbs {|tick, item|
		queue.put(tick, item);
	}
	nextTimeOnGrid {|quant= 1, phase= 0|
		if(quant.isNumber.not, {
			quant= quant.quant;
		});
		if(quant==0, {^tick+(phase*24)});
		if(tick==0, {^phase*24});
		^tick+((24*quant)-(tick%(24*quant)))+(phase%quant*24);
	}
	clear {
		(this.class.name++": clear").postln;
		if(PriorityQueue.findMethod(\do).notNil) { // 3.5 support
			queue.do {|item| item.removedFromScheduler};
		} {
			queue.array.pairsDo{|time, item| item.removedFromScheduler};
		};
		queue.clear;
		if(permanent.not, {
			start.remove;
			stop.remove;
			pulse.remove;
		},{
			CmdPeriod.doOnce {this.clear};
		});
		running= false;
	}
}
