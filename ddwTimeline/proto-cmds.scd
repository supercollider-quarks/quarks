// setcmd doesn't handle synthdesc right

// h. james harkins - jamshark70@dewdrop-world.net
// not ready for prime time!

// important safety tip: setDoneSignal should not populate the doneSignal variable in a deferred function

var	saveSubType = AbstractChuckArray.defaultSubType;

AbstractChuckArray.defaultSubType = \tlCmd;

protect {

Proto({
	// subclasses should have methods
	// ~prep
	// ~fire
	// ~done
	// ~setDoneSignal (optional)
	// ~clearDoneSignal (optional)
	// ~free (optional)
	
	~isRunning = false;
	
		// parameters that one might want to override must be included in parentKeys below
	~isTLCommand = true;
		// if true, this command will be included in \sync or \cmdSync instructions
	~shouldSync = true;
	
	~play = { |parms|
//thisThread.clock.beats.debug(">> %: play".format(~collIndex));
		if(~isRunning.not) {
			~setParms.(parms);
			~isRunning = true;
//~setDoneSignal.postcs;
//"calling setDoneSignal".debug;
			~setDoneSignal.(parms);
//"calling fire".debug;
			~fire.(parms);
//			currentEnvironment.changed(\play, parms);
			NotificationCenter.notify(currentEnvironment, \play, [parms]);
			~schedDone.(parms);
		};
//thisThread.clock.beats.debug("<< abstractcmd: play");
		currentEnvironment
	};
	~schedDone = { |parms|
		if(~doneSignal.isNil) {
			thisThread.clock.sched((~dur.value(parms) ? 0), e { ~stop.value });
		};
	};
	~stop = { |parms|
		var	notifyTime;

// currentEnvironment.env.debug("abstractcmd: stop");

		if(~isRunning) {
			~done.(parms);

			~clearDoneSignal.(parms);

// "setting isRunning false".debug;
			~isRunning = false;
// "\nsending done notification".debug;
// currentEnvironment.listVars;

			if((notifyTime = parms.tryPerform(\at, \notifyTime)).notNil) {
				thisThread.clock.schedAbs(notifyTime, e {
					NotificationCenter.notify(currentEnvironment, \done, [parms]);
				});
			} {
				NotificationCenter.notify(currentEnvironment, \done, [parms]);
			};
//			currentEnvironment.changed(\done, parms);
		};
		currentEnvironment
	};
	~setParms = { |parms|
		var	e = currentEnvironment.env;
//		if(e.proto.isNil) {
//			e.proto = ();
//		};
		parms.respondsTo(\keysValuesDo).if({
			e/*.proto*/.putAll(parms);
		});
	};
}, parentKeys: #[isTLCommand, shouldSync]) => PR(\abstractTLCommand);

PR(\abstractTLCommand).clone({
	~id = 0;
	~fire = { |parms|
		thisThread.clock.beats.debug("firing %".format(~id));
		currentEnvironment
	};
	~done = { |parms|
		thisThread.clock.beats.debug("done %".format(~id));
		currentEnvironment
	};
}, parentKeys: #[id]) => PR(\testTLCmd);

// run an arbitrary function
PR(\abstractTLCommand).clone({
	~fire = { |parms|
//		~setParms.(parms);
		~func.value;
	};
}) => PR(\funcCmd);

PR(\abstractTLCommand).clone({
	~iMadeSynthDef = false;
	~fire = { |parms|
//		~setParms.(parms);
		var	msg, templib;
		// you might provide a synthdef or func - needs to be sent
		if(~newDef = [Symbol, String].includes(~name.class).not) {
			~synthdef = ~name.asSynthDef;
			~iMadeSynthDef = (~synthdef !== ~name);
			// we need a temp SynthDesc, not to add to the main lib
			// but SynthDesc.readFile must have a dictionary to add to
			// so we give it a throwaway
			// actually this is necessary because the synthdef file format
			// can contain multiple defs, and readFile has to handle that
			templib = SynthDesc.readFile(CollStream(~synthdef.asBytes),
				dict: IdentityDictionary.new);
			~synthDesc = templib[~synthdef.name.asSymbol];
		};
		~server ?? { ~server = Server.default };
		~nodeID ?? { ~nodeID = ~server.nextNodeID };
		~node = Synth.basicNew(~name, ~server, ~nodeID);
		~latency ?? { ~latency = ~server.latency };
		~args = ~getArgs.value(parms);
		~quant ?? { ~quant = 0 };
		~schedTime = ~quant.nextTimeOnGrid(thisThread.clock);
		msg = ~node.newMsg(~target, ~args, ~addAction ? \addToHead);
		if(~newDef) {
			msg = [\d_recv, ~synthdef.asBytes, msg];
		};
		thisThread.clock.schedAbs(~schedTime, e {
			if(~latency == 0) {
				~server.sendBundle(nil, msg)
			} {
				~server.sendBundle(~latency, msg)
			};
		});
		NodeWatcher.register(~node, true);  // true == assumePlaying
		~node.addDependant(currentEnvironment);
	};
	~getArgs = { |parms|
		var	result;
		~synthDesc ?? {
			~synthDesc = (~descLib ?? SynthDescLib.global).at(~name.asSymbol);
		};
		~synthDesc.notNil.if({
			currentEnvironment.env.use({ ~synthDesc.msgFunc.valueEnvir });
		}, {
			// this is pretty much disastrous but we need *some* fallback
			[currentEnvironment.env, parms].do { |dict|
				dict.tryPerform(\keysValuesDo, { |key, value|
					if(value.isValidSynthArg) {
						result = result.add(key).add(value);
					};
				});
			};
			result
		});
	};
	~schedDone = { |parms|
		if(~dur.notNil) {
//"synthCmd scheduling release".debug;
			thisThread.clock.schedAbs(~schedTime + ~dur, e { ~stop.(parms) });
		};	// else -- node had better end on its own and trigger ~update
	};
	~done = { |parms|
		var	msg;
//">> synthCmd:done".debug;
		if(~node.isPlaying) {
//"sending release message".debug;
			if(~synthDesc.notNil and: { ~synthDesc.hasGate }) {
				msg = ~node.setMsg(\gate, 0);
			} {
				msg = ~node.freeMsg;
			};
			if(~latency != 0) {
				~server.sendBundle(~latency, msg);
			} {
				~server.sendBundle(nil, msg);
			};
		};
		if(~iMadeSynthDef) { ~server.sendMsg(\d_free, ~synthdef.name) };
		~stopAction.value(parms);
//"<< synthCmd:done".debug;
	};
	~update = { |obj, msg|
		if(obj == ~node and: { msg == \n_end }) {
			~node.removeDependant(currentEnvironment);
			~privateNodeEndAction.value;
			~stop.value;
		};
//// debug
//		if(obj == ~node and: { msg == \n_go }) {
//			~node.trace;
//		};
	};
	~isSynth = true;
}) => PR(\synthCmd);

PR(\synthCmd).clone {
	~superFire = ~fire;
	~superDone = ~done;
	~controlName = \level;
	~outName = \out;
	~fire = { |parms|
		~mixer = ~mixer.value;
		if(~mixer.notNil) {
			~server = ~mixer.server;
			~target = ~mixer.fadergroup;
				// this is really fucking stupid
				// it's supposed to put all the parms into the environment (~setParms)
				// but it's missing this one ???
			~outName ?? { ~outName = parms.tryPerform(\at, \outName) ? \out };
			~outName.envirPut(~mixer.controls[~controlName].index);
	//~mixer.debug("mixer to automate");
	//~mixer.controls[~controlName].index.debug(~outName);
	//~controlName.debug("watching");
	//currentEnvironment.listVars;
			~mixer.stopAuto(~controlName).watch(~controlName);
			~superFire.(parms);
			~mixer.autoSynths.put(~controlName, ~node);
		};
	};
	~done = { |parms|
//~controlName.debug("stopped watching");
		if(~node === ~mixer.autoSynths[~controlName]) {
			~mixer.stopWatching(~controlName);
			~mixer.autoSynths.put(~controlName, nil);
		};
		~superDone.(parms);
	};
	~privateNodeEndAction = {
		// make sure client knows the real final value from the synth
		var	mctl;
		try {
			mctl = ~mixer.controls[~controlName];
			mctl.bus.get({ |value| try { mctl.value = value } });
		};
	};
} => PR(\mixerAutoSynthCmd);

PR(\mixerAutoSynthCmd).clone {
	~fire = { |parms|
		if(~gc.isFunction) { ~gc = ~gc.value };
		if(~gc.notNil) {
			~server = ~gc.server;
			~target = ~target ?? { ~server.asTarget };
			~outName ?? { ~outName = parms.tryPerform(\at, \outName) ? \out };
			~outName.envirPut(~gc.index);
			~gc.stopAuto.watch;	// should replace existing automation
			~superFire.(parms);
			~gc.autoSynth = ~node;
		};
	};
	~done = { |parms|
//~controlName.debug("stopped watching");
		if(~node === ~gc.autoSynth) {
			~gc.stopWatching;
			~gc.autoSynth = nil;
		};
		~superDone.(parms);
	};
	~privateNodeEndAction = {
		// make sure client knows the real final value from the synth
		var	gc = ~gc;
		try { gc.bus.get({ |value| try { gc.value = value } }) };
	};
} => PR(\gcAutoSynthCmd);

PR(\abstractTLCommand).clone({
	~fire = { |parms|
		var	synthDesc;
//		~setParms.(parms);
		~nodes = ~iterator.activeCmds.select({ |cmd|
			cmd.tryPerform(\isSynth) ? false
		});
		if(~nodes.size > 0) {
			~nodes.do({ |node|
				synthDesc = node.synthDesc;
				~args = ~getArgs.(parms);
				if(node.latency == 0) {
					node.node.set(*~args);
				} {
					node.server.sendBundle(node.latency, node.node.setMsg(*~args))
				};
			});
		};
		~dur = 0;
	};
}).import((synthCmd: #[getArgs, setParms])) => PR(\setCmd);


// Pdef player

PR(\abstractTLCommand).clone({
	~quant = Quant(0);
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~pdefs = Array(~name.size);
		~players = Array(~name.size);
		~name.do({ |name|
			if(Pdef(name).isActive) {
				if(parms.tryPerform(\at, \playWarning) != false) {
					"Pdef(%) is already playing; ignored".format(name.asCompileString).warn;
				};
			} {
				~pdefs.add(Pdef(name));
				if(Pdef(name).clock !== thisThread.clock) {
					Pdef(name).clock = thisThread.clock;
				};
					// user hook to manipulate Pdef before play
				~preparePdef.(Pdef(name));
//name.debug("Pdefcmd - playing");
//[thisThread.clock.beats, Pdef(name).eventSchedTime(~quant)].debug("now, go time");
//~quant.dump;
				Pdef(name).play(thisThread.clock, ~protoEvent, ~quant, ~doReset ? false);
				~players.add(Pdef(name).player);
			};
		});
		if(~waitForStop == nil) {
			~waitForStop = ~pdefs.collect(_.key);
		} {
				// could be immutable as input
			~waitForStop = ~waitForStop.copy;
		};
	};
	~schedDone = { |parms|
		var	stopTime;
			// nothing played
		if(~pdefs.size == 0) {
			~stop.value;
		} {
			~pdefs.do({ |pdef| pdef.player.addDependant(currentEnvironment) });
			if(~dur.isNumber and: { ~dur.isStrictlyPositive }) {
				stopTime = thisThread.clock.beats + ~dur;
				thisThread.clock.sched(~dur - 0.001, e {
					~stop.value((notifyTime: stopTime));
				});
			};
		};
	};
	~done = { |parms|
			// if iterator stops automatically and this command should NOT sync,
			// then we don't want the pdefs to stop
//[~name, parms].debug("Pdefcmd:done");
		if(~shouldSync or: { parms.tryPerform(\at, \manualStop) == true }) {
//"stopping pdefs".debug;
				// pdefs will be removed from this array on each stopNow
				// so copying the array is necessary
				// also at end, this process might be responsible for stopping everything
			if(~stopAll == true, { Pdef.all }, { ~pdefs.copy })
			.do({ |pdef| pdef.stop });
		}
	};
	~update = { |obj, what, how|
		var	index;
//[obj, what, how].debug("Pdefcmd got update");
		if(what == \stopped and: { (index = ~players.indexOf(obj)).notNil }) {
				// user hook to reset Pdef after completion
			~pdefs[index].stop;
			~resetPdef.(obj);
				// the Pdef has already forgotten about its player
				// but I haven't
			~players[index].removeDependant(currentEnvironment);
			~waitForStop.remove(~pdefs[index].key);
			~pdefs.removeAt(index);
			~players.removeAt(index);
			if(~waitForStop.size == 0) {
				~stop.value;
			};
		};
	};	
}) => PR(\pdefCmd);


// bp command

PR(\abstractTLCommand).clone({
	~quant = NilTimeSpec.new;
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~bps = Array(~name.size);
		~name.do({ |name|
			if(BP.exists(name).not or: { BP(name).isPlaying }) {
				if(parms.tryPerform(\at, \playWarning) != false) {
					"BP(%) is already playing, or doesn't exist; ignored"
						.format(name.asCompileString).warn;
				};
			} {
				~bps.add(BP(name));
//				if(BP(name).clock !== thisThread.clock) {
//					thisThread.clock => BP(name);
//				};
					// user hook to manipulate BP before play
				~prepareBP.(BP(name));
// name.debug("bpcmd - playing");
// [thisThread.clock.beats, thisThread.clock.baseBarBeat, BP(name).eventSchedTime(~quant)].debug("now, basebar, go time");
// ~quant.dump;
				/*thisThread*/ BP(name).clock.schedAbs(BP(name).eventSchedTime(~quant), e {
						// if not running, cmd was stopped before BP fired
						// so skip the .play
					if(~isRunning) {
//[~name, thisThread.clock.beats].debug("playing bps now");
						BP(name).play(NilTimeSpec.new, nil, doReset: ~doReset ? false);
					} {
						BP(name).removeDependant(currentEnvironment);
					};
				});
			};
		});
		if(~waitForStop == nil) {
			~waitForStop = ~bps.collect(_.collIndex);
		} {
				// could be immutable as input
			~waitForStop = ~waitForStop.copy;
		};
	};
	~schedDone = { |parms|
		var	stopTime;
			// nothing played
		if(~bps.size == 0) {
			~stop.value;
		} {
			~bps.do({ |bp| bp.addDependant(currentEnvironment) });
			if(~dur.isNumber and: { ~dur.isStrictlyPositive }) {
				stopTime = thisThread.clock.beats + ~dur;
				thisThread.clock.sched(~dur - 0.001, e {
					~bps.copy.do({ |bp| bp.stopNow(notifyTime: stopTime); });
				});
			};
		};
	};
	~done = { |parms|
			// if iterator stops automatically and this command should NOT sync,
			// then we don't want the bps to stop
		var	timeSpecForStop = if(~stopAll != true) { NilTimeSpec.new };
//[~name, parms].debug("\n\nbpcmd:done");
//thisThread.dumpBackTrace;
		if(~shouldSync or: { parms.tryPerform(\at, \manualStop) == true }) {
//"stopping bps".debug;
				// bps will be removed from this array on each stopNow
				// so copying the array is necessary
				// also at end, this process might be responsible for stopping everything
			if(~stopAll == true, { BP.all }, { ~bps.copy })
			.do({ |bp| bp.stopNow(quant: timeSpecForStop) });
		}
	};
	~update = { |obj, what, how|
//[obj.tryPerform(\collIndex), what, how].debug("bpcmd got update");
		if(what == \stop and: { how == \stopped and: { ~bps.includes(obj) } }) {
				// user hook to reset BP after completion
			~resetBP.(obj);
			~bps.remove(obj);
			~waitForStop.remove(obj.collIndex);
			obj.removeDependant(currentEnvironment);
			if(~waitForStop.size == 0) {
				~stop.value;
			};
		};
	};	
}, parentKeys: #[quant]) => PR(\bpCmd);

// this command triggers one event in the registered bp's
// it must be non-syncing and have a cleanup function saved in ~cleanup
PR(\abstractTLCommand).clone({
	~shouldSync = false;
	~fire = { |parms|
//		~setParms.(parms);
		~name = ~name.asArray;
		~bps = Array(~name.size);
		~name.do({ |name|
			if(BP(name).isPlaying) {
				"BP(%) is playing; can't trigger one event".format(name.asCompileString).warn;
			} {
				~bps.add(BP(name));
				BP(name).addDependant(currentEnvironment);
				if(BP(name).clock !== thisThread.clock) {
					thisThread.clock => BP(name);
				};
					// user hook to manipulate BP before play
				~prepareBP.(BP(name));
				thisThread.clock.schedAbs(BP(name).eventSchedTime(~quant), {
					BP(name).triggerOneEvent(NilTimeSpec.new, nil, doReset: ~doReset ? false);
				});
			};
		});
	};
	~done = { |parms|
		~cleanup.(parms);
		if(parms.tryPerform(\at, \manualStop) ? false) {
			~bps.do(_.stopNow);
		};
		~bps.do({ |bp| bp.removeDependant(currentEnvironment) });
	};
	~update = { |obj, what, how|
//[obj.tryPerform(\collIndex), what, how].debug("bp1eventCmd got update");
		obj.removeDependant(currentEnvironment);
		switch(what)
				// triggerOneEvent failed
			{ \oneEventEmpty } { ~stop.value }
				// somebody else started playing the process - this guy is dead now
			{ \play } { ~stop.value };
	};	
}, #[shouldSync]) => PR(\bp1eventCmd);

PR(\abstractTLCommand).clone({
	~beatsPerBar = 4;
	~fire = { |parms|
//		~setParms.(parms);
			// No implementations of 'setMeterNextBar'?
//		thisThread.clock.setMeterNextBar(~beatsPerBar);
		thisThread.clock.setMeterAtBeat(~beatsPerBar, thisThread.clock.nextBar);
		~stop.();
	};
}) => PR(\meterCmd);

// REALLY experimental: flow-of-control commands

// looping over a whole array
// how to stop: same as others, set a user signal or specify a duration

PR(\abstractTLCommand).clone({
	~autoSync = true;
	~repeats = inf;
	// atypical for cmds, but this means the cmd determines when it stops,
	// not a preset duration (which is 0 by default - very bad here!)
	~doneSignal = true;
	~fire = { |parms|
//		~setParms.(parms);
		// ~repeats==inf and no cmds ==> infinite loop
		if(~cmds.size == 0) { ~stop.() };
		~repeats = ~repeats.value;
		~myIterator = ~makeIterator.();
		~myIterator.play(argClock: thisThread.clock);
	};
	~done = { |parms|
		~myIterator.removeDependant(currentEnvironment);
		if(~myIterator.isRunning) { ~myIterator.stop };
	};
	~update = { |obj, what, activeCmds, endStatus|
		if((~isRunning ? false) and: { what == \done }) {
			// activeCmds.debug("got done notification; looping back");
			~myIterator.removeDependant(currentEnvironment);
			~repeats = ~repeats - 1;
			if(endStatus == \normal and: { ~repeats > 0 }) {
				~myIterator = ~makeIterator.();
				~myIterator.play(argClock: thisThread.clock, runningCmds: activeCmds);
			} {
				// iterator can stop by a nil item
				// but hardstop only if a stop is not scheduled by other means
				if(~repeats <= 0 or: { ~dur.isNil and: { ~doneSignal.isNil } }) {
					~stop.((activeCmds: activeCmds))
				};
			};
		};
	};
	~makeIterator = {
		var iterator = TLSequenceIterator(~cmds, ~sequencer, autoSync: ~autoSync);
			// looping works by receiving "stop" signal, then restarting upon that signal
		iterator.addDependant(currentEnvironment);	// returns iterator
	};
}) => PR(\loopCmd);

// send user-defined notification from the iterator
PR(\abstractTLCommand).clone({
	~fire = { |parms|
//[~iterator, parms.atBackup(\notification, currentEnvironment), parms].debug("notifyCmd");
		parms[\iterator].changed(parms.atBackup(\notification, currentEnvironment),
			parms ?? ~parms);
	};
}) => PR(\notifyCmd);


// SECTION object
// responsible for saving bp states and selecting sequences (if needed) by user input

Proto({
	~name = "";		// to display in GUI
	~autoRestoreStates = true;
	~bps = nil;	// fill in with an array of BP names (symbols)
	~status = \idle;
	~autoSync = true;	// passed into TLSequenceIterator
	~clock = TempoClock.default;
	// used to delay stopping the last iterator
	// if the section item's 'dur' is a number especially
	~leadTime = 0;

	~isLoaded = false;

		// parent will call this one
	~prInitProc = {
		if(~isLoaded.not) {
			~initProcesses.();
			~isLoaded = true;
		};
	};
	
	~prUnload = {
		if(~isLoaded) {
			~unloadProcesses.();
			~isLoaded = false;
		};
	};

	~initProcesses = nil;		// user-supplied function to load resources
	~unloadProcesses = nil;

	~play = { |savedCmds|
		if(~isRunning.().not) {
			~savedCmds = savedCmds;
//savedCmds.debug("section:play - savedcmds");
			~clock ?? { ~clock = TempoClock.default };
			~initStates.();
			~preparePlay.();
			~seqPlayer = ~makeSeqPlayer.();
//~clock.tempo.debug("section %'s tempo".format(~name));
			~clock.sched(0, ~seqPlayer);
			~status = \running;
			currentEnvironment.changed(\play);
			~schedDone.();
		};
	};
	~schedDone = {
		var	now = thisThread.clock.beats;
		if(~dur.notNil) {
			~clock.sched(~dur - 0.001, e { ~stop.(now + ~dur) });
		}	// else, there should be a user input trigger to move to the next section
	};
	~stop = { |notifyTime, manualStop = true|
		var	stopParms;
		if(~isRunning.()) {
			~status = \idle;
			stopParms = (manualStop: manualStop ? true);
			~currentIterator.stop(stopParms);
			// that was a nasty bug: it's possible to be between iterators --
			// this one's iterator ended and it's waiting for a trigger to go on.
			// So there are commands in ~savedCmds not owned by any iterator,
			// and it isn't sufficient to stop the iterator alone.
			if(manualStop) { ~savedCmds.do(_.stop(stopParms)) };
			~done.(manualStop);
			~clearDoneSignal.();
			~clock.schedAbs((notifyTime ?? { ~clock.beats }), e {
//~clock.beats.debug("sending done notification at");
//~savedCmds.debug("with these saved commands");
				currentEnvironment.changed(\done, ~savedCmds);
			});
			if(thisThread === ~seqPlayer) {
				nil.yield
			} {
				~seqPlayer.stop;
			};
		};
		currentEnvironment
	};
	~done = {
	};
	
	~isRunning = { ~status != \idle };
	
//	~saveStates = {
//		var	bps;
//		bps = ~bps ?? { BP.keys };
//		~states ?? { ~states = IdentityDictionary.new };
//		bps.do({ |bp|
//			~states.put(bp, BP(bp).archiveState);
//		});
//	};
//	~restoreStates = {
//		if(~states.notNil) {
//			~states.keysValuesDo({ |bpname, archive|
//				BP(bpname).restoreArchive(archive);
//			})
//		};
//	};
//	
//	~initStates = {
//		if(~states.isNil) {
//			~saveStates.()
//		} {
//			if(~autoRestoreStates) {
//				~restoreStates.();
//			};
//		};
//	};

	~makeSeqPlayer = {
//		~seqStream ?? { ~seqStream = ~seqPattern.().asStream };
			// should always reset: start from beginning of section every time you play
		~seqStream = ~seqPattern.().asStream;
			// ~condition is used elsewhere, not here, but needs to exist
		~condition ?? { ~condition = Condition.new };
		Routine({ |inval|
			var	item, delta;
			loop {
				item = ~seqStream.next(~seqEventProto);
				if(item.notNil) {
					delta = ~playItem.(item);
					delta.yield;	// playItem returns time to wait
				} {
					// always stop last section as if manually stopped
					// (forcing all children to stop)
					~stop.(nil, ~isLastSection ? false)
				};
			}
		})
	};
	
	~update = { |obj, what, args|
// [obj, what, args].debug("section % received update".format(~name));
		if(obj === ~currentIterator) {
			switch(what)
				{ \done } {
					obj.removeDependant(currentEnvironment);
						// iterator might remove commands after this happens
					~savedCmds = args.copy;
					if(~status == \sync) { ~condition.unhang };
				}
		};
			// a command might send a notification that's really meant for the section sequencer
			// it can set the passthru flag
			// try returns nil if there's an error
		if(try { args[\passthru] } == true) {
			currentEnvironment.changed(what, args)
		};
	};
	
	~playItem = { |item|
		var	seq = item[\sequence], dur = item[\dur],
			iterator, /*updater, */quant;
		if(seq.size > 0) {
			~currentIterator = iterator = TLSequenceIterator(seq, currentEnvironment,
					autoSync: ~autoSync)
				.onStop_(item[\onStop])
				.play(0, ~clock, ~savedCmds/*.debug("adding saved cmds")*/);
			~savedCmds = nil;
			iterator.addDependant(currentEnvironment);
		};
		case
			{ dur.isNumber and: { dur >= 0.001 } } {
				~clock.sched(dur - 0.001, {
					iterator.removeDependant(currentEnvironment);
					thisThread.clock.sched(~leadTime, { iterator.stop });
				});
				dur	// return value
			}
				// cannot sync if there's nothing to play
			{ dur == \sync and: { iterator.notNil } } {
				~status = \sync;
				~condition.hang;
				~status = \running;
				0	// after waking up, go immediately
			}
			{ dur == \trigger } {
				~setTrigger.(item);
				~status = \waitForTrigger;
				~condition.hang;
				~status = \running;
				item[\quant].asQuant.nextTimeOnGrid(~clock) - ~clock.beats
//				0	// after waking up, go immediately
			}
			{ (quant = dur.tryPerform(\asQuant)).notNil } {
				quant = quant.nextTimeOnGrid(~clock) - ~clock.beats;
				~clock.sched(quant - 0.001, {
					iterator.removeDependant(currentEnvironment);
					thisThread.clock.sched(~leadTime, { iterator.stop });
				});
				quant
			}
			{ seq == \initNext } {
				currentEnvironment.changed(\initNext);
				dur
			}
				// default case: invalid object - stop rather than loop infinitely
			{
				MethodError("Invalid item in section", item).throw
			}
	};

	~setTrigger = { |item|
		if(~currentTriggerItem.isNil) {
			~currentTriggerItem = item;
			item[\init].(item);
		} {
			"Another trigger is already active. Ignored.".warn;
		};
	};
	~cleanupTrigger = { |triggerID, parms|
		if(~currentTriggerItem.notNil) {
			~currentIterator.stop;
			~currentTriggerItem[\clear].(~currentTriggerItem, triggerID, parms);
			~currentTriggerItem = nil;
		};
	};
	~doTrigger = { |triggerID, parms|
		if(~status == \waitForTrigger) {
			if(~receivedTrigger.(triggerID, parms)) {
				~status = \running;
				~cleanupTrigger.(triggerID, parms);
				~condition.unhang;
			} {
				"Received inactive trigger %; ignored.".format(triggerID).warn;
			};
		} {
			"Section is not waiting for user trigger. Cannot ~doTrigger now.".warn;
		};
	};
	~receivedTrigger = { |triggerID, parms|
		~currentTriggerItem[\id].matchItem(triggerID)
			and: { ~currentTriggerItem[\verify].(~currentTriggerItem, parms) ? true }
	};
		// SEQUENCE SELECTION
		// up to you to decide how to store sequences
		// pattern must return events (i.e., you can use Pbind) with
		// (sequence: array for TLSeqIt, dur: timing info)
		// sequence = nil means pause based on timing
		// dur = number, run sequence for exactly dur beats (Quant OK too)
		// dur = \sync, resume section when sequence stops
		// dur = \trigger (or nil), wait for some user signal - user's responsibility to maintain
	~seqPattern = {};	// you supply!
	~seqEventProto = ();
}) => PR(\tlSection);

Proto({
		// should send .changed(\section, sectionIndex) when moving to a new section

	~sections = [];	// supply array of sections
	~status = \idle;
	~recordingOn = false;
	~recordingOn_ = { |bool(false)|
		~recordingOn = bool;
		currentEnvironment.changed(\recording, bool);
	};
	
	~currentSection = 0;
	
	~countOffBeats = 4;
//	~clock = TempoClock.default;
		// sections could run on different clocks
		// that could come back to haunt me...
	~clock = { ~sections[~currentSection].tryPerform(\clock) ?? { TempoClock.default } };
	~quant = Quant(1);	// start (to count off) on next whole bar
	~sectionLeadTime = 0.9;	// how many beats before barline to kick off the section
	
	~autoReset = true;	// automatically reset all BPs?
	~doNotReset = nil;	// collection of bp names NOT to reset
	
//	~condition = Condition.new;
	
	~isRunning = { ~status != \idle };
	
	~recvPlayButton = { |section|
		if(~isRunning.()) { ~stop.() }
			{ ~currentSection = section ?? { ~currentSection }; ~play.() };
	};
	
	~play = {
//">> sectionSequencer:play".debug;
		if(~autoReset) {
			~doReset.();
		};
		~startingSection = ~currentSection;
		~loadResources.(~currentSection);
		~countOffAndGo.();
//		~status = \playing;
		currentEnvironment.changed(\play);
//"<< sectionSequencer:play".debug;
	};
	
	~stop = { |lastSection = false|
//lastSection.debug(">> sectionSequencer:stop");
		if(~status == \countingOff) {
			~status = \idle;
			~countOffRoutine.stop;
			currentEnvironment.changed(\stop);
			currentEnvironment.changed(\countoff, nil);
		} {
			~sections[~currentSection].stop/*(nil, manualStop)*/;
			~sections[~currentSection].removeDependant(currentEnvironment);
				// must force manual stop for last section at end of piece
				// but this didn't work
			if(lastSection ? false) { ~sections.last.stop(nil, true) };
//			if(thisThread === ~sectionRoutine) { nil.yield }
//				{ ~sectionRoutine.stop };
			~status = \idle;
			~currentSection = ~startingSection;
			currentEnvironment.changed(\stop);
			currentEnvironment.changed(\section, ~currentSection);
			~savedCmds = nil;
			~nextSectionToPlay = nil;
			// (~currentSection+1).debug("stop is calling loadResources for");
			~loadResources.(~currentSection+1);
			~sectionRoutine.stop;
			~sectionRoutine = nil;
			~clock.value.sched(5.0, e {
				// if I'm playing, then the recorder doesn't belong to me anymore
				if(~isRunning.().not) {
					try { topEnvironment[\master].stopRecord };
				};
				nil
			});
		};
//"<< sectionSequencer:stop".debug;
	};
	
	~startLoadingAt = 0;
	~gotoSection = { |section|
		var	toStop = ~sections[~currentSection];
// [~currentSection, section].debug(">> gotoSection (current, next)");
		if(~isRunning.()) {
			// "loading resources (playing)".debug;
			~loadResources.(section, ~currentSection);
			~nextSectionToPlay = section;
			toStop.stop(nil, false);  // will advance by ~update
		} {
			// "loading resources (not playing)".debug;
			~currentSection = section;
			~loadResources.(section/*, ~currentSection*/);
		};
		currentEnvironment.changed(\section, section);
// [~currentSection, section].debug("<< gotoSection (current, next)");
	};

	~loadResources = { |section|
//section.debug(">> loadResources");
			// must add 1 because do runs from 0 to n-1
		(section+1).do({ |sect|
			if((~sections[sect].tryPerform(\isLoaded) ? true).not) {
				~sections[sect].prInitProc;
			};
		});
//debug("<< loadResources");
	};
	
	~countOffAndGo = {
		var	clock = ~clock.value, master = topEnvironment[\master],
			goTime = ~quant.nextTimeOnGrid(clock) + ~countOffBeats - ~sectionLeadTime,
			started = false;

		~sections.last.isLastSection = true;

		~status = \countingOff;
		clock.schedAbs(~quant.nextTimeOnGrid(clock), ~countOffRoutine = Routine({
			currentEnvironment.changed(\countoff, \init);
			// count off first
			~countOffBeats.do({ |count|
				currentEnvironment.changed(\countoff, count + 1);
				if(started.not and: { goTime - thisThread.clock.beats < ~sectionLeadTime }) {
					started = true;

					//[clock.beats, goTime].debug("starting section player - now, go time");

					thisThread.clock.sched(0, ~sectionRoutine = Routine({
						if(~recordingOn) {
							if(master.recProxy.notNil) {
								master.stopRecord;
								0.2.wait;
							};
							master.startRecord;
							0.2.wait;
						};
						~status = \playing;
						while { ~isRunning.() and: { ~currentSection < ~sections.size } } {
							/*~savedCmds.*/debug("playing section %".format(~sections[~currentSection].name));
							Library.at(\remoteAddr).tryPerform(\sendMsg, \nowPlaying,
								~sections[~currentSection].segID);
							~sections[~currentSection].play(~savedCmds);
							//"section sequencer: set savedCmds to nil".debug;
							~savedCmds = nil;
							// ~update method below will catch notifications
							~sections[~currentSection].addDependant(currentEnvironment);
							~loadResources.(~currentSection + 1);
							// note, this doesn't force-stop the routine
							// just pauses it
							nil.yield;
						};
						~stop.(true);	// stop last section
					}));
				};
				1.0.wait;
			});
			~countOffRoutine = nil;
			currentEnvironment.changed(\countoff, nil);
		}));
	};
	
	~doReset = {
		(BP.keys - ~doNotReset.as(IdentitySet)).do({ |name|
			BP(name).reset;
		});
	};
	
	~gotoSection = { |section|
		if(~isRunning.()) {
			MethodError("Cannot change section while playing", currentEnvironment).throw;
		} {
			~currentSection = section;
			currentEnvironment.changed(\section, section);
		};
	};
	~currentSection_ = { |section| ~gotoSection.(section) };
	
	~countOffBeats_ = { |beats|
		~countOffBeats = beats;
		currentEnvironment.changed(\countOffBeats, beats);
	};
	
	~autoReset_ = { |autoReset|
		~autoReset = autoReset;
		currentEnvironment.changed(\autoReset, autoReset);
	};
	
	~update = { |obj, what, args|
//[obj, what, args].debug("section sequencer got update");
		var	section;
		if(obj === ~sections[~currentSection]) {
			switch(what)
				{ \done } {
//args.debug("section sequencer update - savedCmds");
					~savedCmds = args;
					obj.removeDependant(currentEnvironment);
					if(~isRunning.()) {
						~currentSection = ~currentSection + 1;
//~currentSection.debug("section done, moving to");
						~nextSectionToPlay = nil;
						currentEnvironment.changed(\section, ~currentSection);
						~clock.value.sched(0, ~sectionRoutine);
//						~condition.unhang;
					};
				}
				{ \initNext } {
					if(~sections[~currentSection + 1].notNil) {
						~sections[~currentSection + 1].prInitProc;
					}
				}
				{ \unload } {
					args.tryPerform(\at, \name).asArray.do { |sectName|
						if((section = ~sections.detect { |sct| sct.name == sectName }).notNil) {
							section.prUnload;
						}
					};
				}
		};
	};
	~sectionNames = { ~sections.collect({ |sect, i| "%: %".format(i+1, sect.name) }) };
}) => PR(\sectionSequencer);

PR.allOfType(\tlCmd).do({ |pr|
	pr.value.put(\collIndex, pr.collIndex)
});

} { AbstractChuckArray.defaultSubType = saveSubType; };
