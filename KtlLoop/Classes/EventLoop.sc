// replaces TimeLoop and descendants

EventLoop {

	// for now, let all EventLoops live in one dict?
	// subclasses can redirect to their own this.all
	classvar <allEls;

	var <key, <func;
	var <list, <task, <isRecording = false;
	var recStartTime, then;
	var <>keysToRecord;

	var <verbosity = 1;

	var <lists, <currIndex = 0, <numLists = 0;

	*initClass { allEls = () }

	*all { ^allEls }

	*at { |key| ^this.all[key] }

	*new { arg key, func;
		var res = this.at(key);
		if(res.isNil) {
			res = super.newCopyArgs(key, func).init.prAdd(key);
		} {
			// do we want to use the interface
			// EventLoop(\x, {}) to change the playback func?
			if (func.notNil) { res.func_(func) };
		}
		^res
	}

	prAdd { arg argKey;
		key = argKey;
		this.class.all.put(argKey, this);
	}

	// backwards compat in KeyPlayer
	isOn { ^isRecording }

	storeArgs { ^[key] }

	printOn { |stream| ^this.storeOn(stream) }

	init { |argFunc|
		func = func ?? { "defaultFunc!".postln; this.defaultFunc };
		lists = EventList[];

		this.initTask;
		this.prepRec;
	}

	func_ { |inFunc|
		func = inFunc;
		task.set(\func, func);
	}

	defaultFunc { ^{ |ev| ev.postln.copy.play; } }

	// check that it is an EventList?
	list_ { |inList|
		list = inList;
		task.set(\list, list);
	}

	verbosity_ { |num|
		verbosity = num;
		task.set(\verbosity, num);
	}

	looped { ^task.get(\looped) }
	looped_ { |val| task.set(\looped, val) }
	toggleLooped { this.looped_(this.looped.not) }

	tempo { ^task.get(\tempo) }
	tempo_ { |val| task.set(\tempo, val) }

	step { ^task.get(\step) }
	step_ { |val| task.set(\step, val) }

	jitter { ^task.get(\jitter) }
	jitter_ { |val| task.set(\jitter, val) }

	lpStart { ^task.get(\lpStart) }
	lpStart_ { |val| task.set(\lpStart, val) }

	range { ^task.get(\range) }
	range_ { |val| task.set(\range, val) }

	initTask {

		task = TaskProxy({ |envir|
			var event, absTime, relDur;
			var index = 0, indexOffset = 0, indexPlusOff;
			var maxIndex, minIndex, indexRange, indexInRange = true;

			var calcRange = {
				var lastIndex =(envir[\list].lastIndex ? -1);
				minIndex = (envir[\lpStart] * lastIndex).round.asInteger;
				indexRange = (envir[\range] * lastIndex).round.asInteger;
				maxIndex = minIndex + indexRange;
				// [minIndex, maxIndex, indexRange].postln;
			};
			var calcIndexInRange = {
				indexInRange = (index >= minIndex) and: { index <= maxIndex };
			};

			if (envir.verbosity > 0) {
				(envir[\postname] + "plays list of % events and % secs.")
				.format(list.size, list.totalDur.round(0.01)).postln;
			};

			calcRange.value;
			index = if (envir[\step] > 0, minIndex, maxIndex);
			calcIndexInRange.value;

			while { envir[\looped] or: indexInRange } {

				indexOffset = (envir[\jitter].bilinrand * indexRange).round.asInteger;
				indexPlusOff = (index + indexOffset).round.asInteger.wrap(minIndex, maxIndex);
				// [index, indexOffset, indexPlusOff].postln;

				event = envir[\list].wrapAt(indexPlusOff);
				if (event.isNil) {
					0.1.wait;
					// early exit here? e.g. set loop false?
				}{
					event[\type].switch(
						\start, { "startfunc?"; },
						\end, { "endfunc?"; },
						{ envir[\func].value(event); }
					);

					if (envir.verbosity > 1) {
						String.fill(indexPlusOff, $-).post;
						"i: % - ev: %".format(indexPlusOff, event).postln;
					};

					(event[\playDur] / envir[\tempo]).wait;

					index = (index + envir[\step]);
					calcRange.value;
					calcIndexInRange.value;
					if (envir[\looped] and: { indexInRange.not }) {
						index = index.wrap(minIndex, maxIndex);
					};
				};
			};

			if (envir.verbosity > 0) { (envir[\postname] + "ends.").postln; };

		});

		task.set(\postname, this.asString);
		task.set(\verbosity, 1);
		task.set(\looped, false, \step, 1, \tempo, 1);
		task.set(\lpStart, 0, \range, 1, \jitter, 0);

		task.addSpec(\tempo, [0.1, 10, \exp, 0.001]);
		task.addSpec(\lpStart, [0, 1]);
		task.addSpec(\range, [0, 1]);
		task.addSpec(\jitter, [0, 1, \amp]);
		task.addSpec(\step, [-1, 1, \lin, 1]);
		task.addHalo(\orderedNames, [\tempo, \lpStart, \range, \jitter]);

		task.set(\func, func);
	}

	// recording events:

	startRec { |instant = false|

		if (isRecording) { ^this };

		isRecording = true;
		this.prepRec;
		task.stop;
		if (verbosity > 0) {
			"  %.startRec; // recording list[%].\n".postf(this, list.size);
		};
		if (instant) { list.start(this.getAbsTime); };
	}

	recordEvent { |event|
		var recEvent;
		if (isRecording) {
			// autostart at 0
			if (list.size == 0) { list.start(this.getAbsTime); };
			recEvent = this.getTimes;
			event.keysValuesDo { |key, val|
				if (key === \absTime) {
					warn("" + thisMethod ++ ": can't use 'absTime' as key in event: %!"
						.format(event));
				} {
					recEvent.put(key, val);
				};
			};
			list.addEvent(recEvent);
			if (verbosity > 1) {
				("//" + this.asString + "rec: ").post; recEvent.postln;
			};
		}
	}

	stopRec {
		if (isRecording.not) { ^this };

		isRecording = false;
		list.finish(this.getAbsTime);
		this.addList;
		recStartTime = nil;

		if (verbosity > 0) {
			"// % stopRec; // recorded list[%] with % events.\n".postf(
				this, lists.lastIndex, lists.last.size)
		};
	}

	toggleRec { |instant=false|
		if (isRecording, { this.stopRec }, { this.startRec(instant) });
	}

	getAbsTime {
		var now = thisThread.seconds;
		recStartTime = recStartTime ? now;
		^now - recStartTime;
	}

	getTimes {
		var absTime, relDur;
		var now = thisThread.seconds;
		if (then.isNil) {
			then = now;
			recStartTime = now;
		};
		relDur = now - then;
		absTime = now - recStartTime;
		then = now;
		^(absTime: absTime, relDur: relDur);
	}

	prepRec {
		this.addList;
		this.list_(EventList[]);
		then = recStartTime = nil;
		this.resetLoop;
	}
	// support simple pattern recording
	next { |inval|
		this.recordEvent(inval);
		^this.isRecording.binaryValue
	}

	// taskproxy for playback interface

	play {
		this.stopRec;
		if (verbosity > 0) { "  %.play;\n".postf(this) };
		task.stop.play;
	}

	togglePlay { if (task.isPlaying, { this.stop }, { this.play }); }

	stop {
		if (verbosity > 0) { "  %.stop;\n".postf(this) };
		task.stop;
	}

	pause { task.pause; }
	resume { task.resume; }
	isPlaying { ^task.isPlaying; }

	// could be more flexible
	playOnce {
		task.fork(event: (task.envir.copy).put(\looped, false));
	}

	resetLoop { task.set(\lpStart, 0, \range, 1, \step, 1, \tempo, 1, \jitter, 0) }

	isReversed { ^this.step == -1 }
	reverse { this.step_(-1) }
	forward { this.step_(1) }
	flip { this.step_(this.step.neg) }


		// handling the lists

	addList {
		if (list.notNil and: { list.notEmpty and: { lists.first !== list } }) {
			lists.addFirst(list);
			numLists = lists.size;
		}
	}

	listInfo { ^lists.collect { |l, i| [i, l.size] } }

	printLists {|round = 0.01|
		this.post; this.asString + "- lists: ".postln;
		this.listInfo.postln;
		lists.do (_.print);
	}

	setList { |index = 0|
		var newList = lists[index];
		if (newList.isNil) {
			this.post; ": no list at index %.".postf(index);
			^this
		};
		this.list_(newList);
		currIndex = index;
	}

	listDur { ^list.last.keep(2).sum; }

	quantize { |quant = 0.25, fullDur|
		list.quantizeDurs(quant, fullDur);
	}

	unquantize { list.restoreDurs; }

}

KeyLoop : EventLoop {
	var <>actionDict;

	// assume single-depth key dict by default:
	defaultFunc { ^{ |ev| actionDict[ev[\key]].value.postln }; }

	// or prepare for lookup in an existing KeyPlayer,
	// which has both down and up key dicts:
	*keyPlayerFunc { |player|
		^{ |ev|
			// ev.postcs;
			if ([\start, \end].includes(ev[\type]).not) {
				player.keyAction(ev[\unicode], which: ev[\type]);
			};
		};
	}

	// by default, one would record key and type
	// this is kept as is for backwards compat,
	// maybe unify later.
	recordEvent { |key, type|
		if (key.notNil) { key = key.asUnicode };
		super.recordEvent((unicode: key, type: type));
	}
}
