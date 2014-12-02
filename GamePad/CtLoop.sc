	// record events in time, so they can be replayed.

	// record controller values, from gamepads and such. adc, haho, 2005-2008
CtLoop : EventLoop {
	classvar <>minTime = 0.001, <>verbose=false;

	var <key, <>ctlMap,
	<list, <task, <isRecording=false, then,
	<>tempo=1, <>start=0, <>length=1, <>step=1, <>jitter=0.0,
	<>scaler=1, <>shift=0.0,	// assumes normalized values.
	<>rescaled=false, <nonRescalableCtls, inverter=1;

	var <loops;

	*new { arg key, ctlMap;
		^super.newCopyArgs(key, ctlMap).init;
	}

	init {
		loops = List[];
		list = List[];

		nonRescalableCtls = Set[];

		task = TaskProxy({
			var dt, index, event, ctlID, val, listSize, intro = true;
			index = (start * list.size).round.round.asInteger;

			"CtLoop task starts.".postln;
			loop({
				listSize = list.size;
				if (list.isEmpty) {
					0.1.wait;
				} {
					event = list.wrapAt(index);

					if (verbose) { event.round(0.001).postcs };

					#dt, ctlID, val = event;

					if (intro.not) {
							// jump over steps if speed is too high?
						max((dt / tempo), minTime).wait;
					};
					intro = false;

					if (rescaled and: { nonRescalableCtls.includes(ctlID).not })
						{ val = this.scaleVal(val); };
					 ctlMap[ctlID].value(val);

					index = (index + step +
						(jitter.asFloat.squared.bilinrand * listSize).round.asInteger					)
						.wrap(
							(start * listSize).round.asInteger,
							(start + length.abs * listSize).round.asInteger - 1
						);

					};
			});
		});
		task.clock_(SystemClock).quant_(0);
	}

	recordEvent { arg ctlID, val;
		var now, delta, event;
		if (isRecording) {
			now = thisThread.seconds;
			delta = now - (then ? now);
			then = now;
			event = [ delta, ctlID, val ];
			if (verbose) { event.round(0.001).postcs };
			list.add(event);
		}
	}

	postRecFix {
 		var now, delta;
		now = thisThread.seconds;
		delta = now - (then ? now);
		try { list.first.put(0, list.first[0] + delta) };
	}

	clear {
		if (loops.last !== list) { loops.add(list) };
		list = List[];
		then = nil;
		this.resetLoop.resetScaling;
	}

	startRec { |instant = false|
		isRecording = true;
		this.clear; task.stop;
		"\n  %(%).startRec;\n".postf(this.class, key);
		if (instant) { then = thisThread.seconds };
	}

	stopRec {
		isRecording = false;
		loops.add(list);
		"\n  %(%).stopRec;\n".postf(this.class, key);
		this.postRecFix;
	}

	toggleRec { |instant=false| if (isRecording, { this.stopRec }, { this.startRec(instant) }); }

	play {
		"\n  %(%).play;\n".postf(this.class, key);
		isRecording = false;
		task.stop.play;
	}

	togglePlay { if (task.isPlaying, { this.stop }, { this.play }); }

	stop {
		"\n  %(%).stop;\n".postf(this.class, key);
 		task.stop;
 	}

	pause { task.pause; }
	resume { task.resume; }
	isPlaying { ^task.isPlaying; }

	resetLoop { start = 0; length = 1; step = 1; tempo = 1; }

	isReversed { ^step == -1 }
	reverse { step = -1 }
	forward { step = 1 }
	flip { step = step.neg }

	scaleVal { arg val, clip=true;
		var scaledVal;
		scaledVal = val - 0.5 * scaler * inverter + 0.5 + shift;
		^if (clip, { scaledVal.clip(0, 1) }, scaledVal);
	}

	isInverse { ^inverter == -1 }
	invert { inverter = -1 }
	up { inverter = 1 }
	flipInv { inverter = inverter.neg }

	dontRescale { arg ... ids; nonRescalableCtls.addAll(ids.flat); }
	doRescale { arg ... ids; nonRescalableCtls.removeAll(ids.flat); }
	resetScaling { scaler = 1; shift = 0; inverter = 1; }
}

AutoLoop : CtLoop {
	var <recTask, <>getFunc, <>recFunc, <dtRec=0.01, <autoIsOn = false;

	init {
		super.init;
		recTask = TaskProxy({ |e|
			var newVal, oldVal;
			loop {
				newVal = getFunc.value;
				oldVal = oldVal ? newVal;
				if (newVal != oldVal) {
					// newVal.postcs;
					this.recordEvent(newVal[0], newVal[0].asSpec.unmap(newVal[1]));
				};
				oldVal = newVal;
				dtRec.wait;
			};
		});
	}
	autoOn { |rec=false|
		autoIsOn = true;
		if (rec) {
			this.startRec
		} {
			if (this.isPlaying.not) { this.play }
		}
	}
	autoOff { autoIsOn = false; this.stop; this.stopRec; }

	startRec {
		super.startRec;
		recTask.play;
	}
	stopRec { |autoplay=false|
		super.stopRec;
		recTask.stop;
		if (autoplay) { super.play };
	}
	play {
		this.stopRec(autoplay: true);
	}

	postRecFix {
		"// AutoLoop.postRecFix - no need to fix first time value I guess.".postln;
	}
		//
	connectToProxyEditor { |editor, editIndex|
		var ed = editor.edits[editIndex];
		var key = ed.labelView.string.asSymbol;
		var sl = ed.sliderView;
		sl.mouseDownAction = { if (this.autoIsOn) { this.startRec; }  };
		sl.mouseUpAction = { if (this.autoIsOn) { this.play; } };

		this.connectToProxy(editor.proxy, key);
	}

	connectToProxy { |proxy, key|
		ctlMap = ctlMap ?? {()};
		ctlMap.put(key, { |val| proxy.set(key, key.asSpec.map(val)) });
		getFunc = { proxy.getKeysValues([key])[0] };
	}

	makeButton { |parent, bounds|
		Button(parent, bounds)
			.states_([[""], ["", Color.white, Color.red]])
			.action_({ |btn| if (btn.value == 1) { this.autoOn } { this.autoOff } });
	}
}
