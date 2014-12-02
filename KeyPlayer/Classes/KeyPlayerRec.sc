 /*** could be unified with CtLoop, maybe with a superclass EventLoop. ****/

// maybe do automatic recording always?
// if so, problem: we must know which keys to record,
// and which not (e.g. rec on/off controls).
// good enough for playing with it now, extend later...

// only record keys in use?
// declare nonRec keys in player or in rec?
/* show rec/play state on gui:
    recBut, playBut, forkBut,
    recOn, playOn, speed, loop,
    numLists, select curr list,
*/

KeyPlayerRec {
	classvar <>verbose = false;

	var <>player, <>list, <>lists;
	var <>isOn=false, <>loop = true, <>speed = 1;
	var then, playOnceFunc, task;

	*new { |player|
		^super.new.player_(player).init;
	}

	init {
		lists = List[];

		playOnceFunc = {
		if (verbose) { "KeyPlayRec starts!".postln; };
		list.do { |trip|
			var time, char, type, unicode;
			#time, char, type = trip;
			unicode = char.asUnicode;
			(time / speed.value).wait;
			player.keyAction(char, which: type);
		};
		if (verbose) { "KeyPlayRec done.".postln; };
		};

		task = TaskProxy({
			this.stopRec;
			playOnceFunc.value;
			if (loop) {
				task.play
			} {
				("done: kp" + player.key).postln;
			}
		});
	}

	startRec {
		if (verbose) { ("recording keys: kp" + player.key).postln; };
		then = nil;
		isOn = true;
		list = List.new;
	}

	stopRec {
		if (isOn) {
			if (verbose) { ("stopRec: kp" + player.key).postln };
			// record final wait time
			this.recordEvent(\end, \down);
			isOn = false;
			lists.add(list);
		};
	}

	recordEvent { |char, type=\down|
		var now = thisThread.seconds;
		var event;
		if (isOn) {
			then = then ? now;
			event = [now - then, char, type];
			list.add(event);
			then = now;
			if (verbose) {
				"KPR % : recording %.\n".postf(player.key, event);
			};
		}
	}

	play { task.play; }

	playOnce { this.stopRec; Task({ playOnceFunc.value; }).play; }

	toggleRec { if (isOn) { this.stopRec } { this.startRec } }

	togglePlay {
		if (task.isActive) { task.stop } { task.play };
	}

	changeSpeed { |factor=1| speed = speed * factor }
}
