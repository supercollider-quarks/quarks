
// Basically deprecated. This scheme was too complicated and prone to break.
// Use at your own risk. Good luck!

var saveSubtype = AbstractChuckArray.defaultSubType;

AbstractChuckArray.defaultSubType = \contrapunct;

// converts event stream into event pattern so that other wrappers like Pfindur can be used
{ |holder, driverEvent|
	Prout({ |inEvent|
		var	event;
		loop {
			event = holder.getEvent(inEvent, driverEvent);
			inEvent = event.yield;
		}
	})
} => Func(\defaultStreamWrapper).subType_(\contrapunct);

{ |pattern| pattern } => Func(\defaultPatternWrapper);

{ |pattern| Pfin(1, pattern) } => Func(\oneChildEvent);

// args: child event, bpholder, driver event
{ |event| event } => Func(\defaultEventUpdater);

// a holder for a process that is being driven by another
// most important parms:
// modifyDriver -- a func to parse the driver event before wrapping
// swrapPattern -- wrap the bp's stream to make it a pattern
// pwrapPattern -- wrap the pattern to modify its output (Pfindur is applied automatically)
// updatePattern -- modify each child event before returning
// quant -- when to start child if coordinator is playing and child is not
PR(\abstractProcess).v.clone({
	~prepare = { |bp, parms|
		var	parmsToInsert;
		~bp = bp;
			// scorched-earth handling of parms; eventually I'll add safety checks?
		parmsToInsert = bp.v.coordParms ?? { IdentityDictionary.new };
		parms.respondsTo(\keysValuesDo).if({ parmsToInsert.putAll(parms); });
		currentEnvironment.putAll(parmsToInsert);
		(bp.v.alwaysReset == true).if({ ~doReset = true });
		~makeStreamForKey.(\swrap);
		~makeStreamForKey.(\pwrap);
		~makeStreamForKey.(\update);
		bp.v.isDriven = true;
		~wasPlaying = bp.isPlaying;
		bp.v.isPlaying = false;
		~inited = false;
		bp.addDependant(currentEnvironment);	// get notifications from child process
		bp.changed(\driven);
		currentEnvironment
	};

	~prepForPlay = { 
		~inited.not.if({
			~bp.v.eventStreamPlayer.isNil.if({
				~bp.prepareForPlay;
			}, {
				~bp.populateAdhocVariables;
			});
		});
		~inited = true;
		currentEnvironment
	};

	
	~update = \defaultEventUpdater.asPattern;
	~swrap = \defaultStreamWrapper.asPattern;
	~pwrap = \defaultPatternWrapper.asPattern;
	
		// =>.swrap for func to wrap the child stream in a pattern
	~wrapChildStream = { |driverEvent|
		var	pattern = Func(~swrapStream.next(driverEvent)).doAction(driverEvent);
			// if the next child event was supposed to happen some time after the sync point,
			// add a dummy event to compensate
			// nextSync is unchanged from the last driver event
		(~autoOffset and: { 
				~nextChildEventTime.isNumber and: {
				~nextSync.isNumber and: {
				~nextChildEventTime > ~nextSync } } }).if({
			Pseq([(play: 0, delta: ~nextChildEventTime - ~nextSync), pattern], 1)
		}, {
			pattern
		});
	};
	
		// user hook to provide a Pbindf or other processing
	~wrapPattern = { |pattern, driverEvent|
		pattern
	};
	
		// =>.pwrap for func to wrap the pattern holding the stream
		// modifyDriver should set offset
	~prWrapPattern = { |pattern, driverEvent|
		var	deltaToNextSync = driverEvent.deltaToNextSync;
		pattern = Func(~pwrapStream.next(driverEvent)).doAction(pattern, driverEvent)
			? pattern;
		pattern = ~wrapPattern.(pattern, deltaToNextSync);
//		((~truncatePattern ? true) and: { deltaToNextSync.notNil }).if({
		deltaToNextSync.notNil.if({
			Pfindur(deltaToNextSync, pattern)
		}, {
			pattern
		});
	};
	
	~event = { ~bp.v.event };

	~getEvent = { |inEvent, driverEvent|
		var	event = ~bp.v.eventStream.next(~event.value);
//thisThread.clock.beats.debug("bpholder-getEvent");
//event.debug("bpholder-getEvent");
//event[\note].postcs;
//event[\play].postcs;
		event = event !? { Func(~updateStream.next(driverEvent))
			.doAction(event, /*currentEnvironment,*/ driverEvent)/*.debug("return value from func")*/ ? event };
		~nextChildEventTime = event.notNil.if({ thisThread.clock.beats + event.delta }, { nil });
//event.debug("output event");
		event
	};
	
//	~nextSync = ~nextChildEventTime = 0;
	~autoOffset = true;
	~wasTriggeredExternally = false;
	~offset = 0;
	~lastOffset = 0;	// this is the offset from the previous event

	~doEvent = { |driverEvent, childMetaEvent|
		var	inEvent, /*event, */pattern;

			// processDriverEvent goes false when the child is released by .stop or .play
			// in that case, we should ignore the driver event
		(~processDriverEvent ? true)/*.debug("child event for %".format(~bp.collIndex))*/.if({
			~prepForPlay.value;
				// modifyDriver must set the offset
			~lastDriverEventID = driverEvent[\ID];
			driverEvent = ~modifyDriver.(driverEvent.copy, childMetaEvent) ? driverEvent;
//[thisThread.clock.beats, driverEvent.delta, ~offset, ~lastOffset, driverEvent.delta + ~offset - ~lastOffset].debug("now, delta, offset, lastOffset, deltaToNextSync");
			driverEvent.put(\deltaToNextSync, driverEvent.delta + ~offset - ~lastOffset);
//driverEvent.debug("driverEvent");
			(driverEvent.skipChildren.tryPerform(\includes, ~bp.collIndex) ? false).not.if({
				inEvent = ~bp.v.event.copy.put(\driver, driverEvent);
				~bp.v.driverEvent = driverEvent;
//inEvent.debug("inEvent");

				pattern = ~wrapChildStream.(driverEvent, childMetaEvent);
//pattern.asCompileString.debug("wrapChildStream result");
				pattern = ~prWrapPattern.(pattern, driverEvent, childMetaEvent);
//pattern.asCompileString.debug("prWrapPattern result");

// what is the termination condition? may have to use CleanupStream
// no -- register as dependent of bp
	
					// play the child until the next sync point
				thisThread.clock.sched(0,
					~eventStreamPlayer = PausableEventStreamPlayer(pattern.asStream, inEvent)
						.refresh);
			});
			
			~lastOffset = ~offset;
			~nextSync = thisThread.clock.beats + driverEvent[\deltaToNextSync];

			(process: ~bp.collIndex, delta: driverEvent.deltaToNextSync);
		});
	};
	
	~abortStream = {
		~eventStreamPlayer.stop;
	};

	~update = { |obj, changer|
//this.dumpBackTrace;
//[obj.asCompileString, changer].debug("bpholder-update");
		case { #[\play, \stop].includes(changer) } {
						// set flag to return a nil metaevent next time
					~processDriverEvent = false;
					~wasPlaying = (changer == \play);
					~wasTriggeredExternally = true;
					currentEnvironment.changed(\lostChild);
				}
			{ changer == \driven } {
					~processDriverEvent = true;
				}
			{ changer == \free } {
					~abortStream.value;
					~wasPlaying = false;
					~wasTriggeredExternally = true;
					~processDriverEvent = false;
					currentEnvironment.changed(\lostChild);
				}
	};
	
	// more...
}) => PR(\bpHolder).subType_(\contrapunct);

PR(\abstractProcess).v.clone({
	~childProto = \bpHolder;
	~alwaysReset = true;	// once I stop, my event stream is invalid
	~event = (eventKey: \dummy);
		// using PQ because children may offset their sync points
		// and occur earlier or later than the next driver event
	~prep = { 
		~children = IdentityDictionary.new;
		~queue = PriorityQueue.new;
		~driverEventID = 0;
	};
	
		// what else? prepare for play?
	~driver_ = { |bp|
		var syncTime;
		(~driver !== bp).if({
			~driver.notNil.if({
				~releaseDriver.value;
			});
			~driver = bp;
			~clock.isNil.if({
				~clock = ~driver.v.clock;
			}, {
				(~clock != ~driver.v.clock).if({
					"Clocks do not match. There may be scheduling problems.".warn;
				});
			});
			bp.v.isDriven = true;
			~driverWasPlaying = bp.isPlaying;
			bp.v.eventStreamPlayer.isNil.if({
				bp.prepareForPlay;
			}, {
				bp.populateAdhocVariables;
			});
				// automatically play coordinator if the driver was playing and I am not
				// so that the driver proceeds seamlessly
			((syncTime = bp.v[\eventStreamPlayer].tryPerform(\nextBeat)).notNil 
					and: { ~isPlaying.not }).if({
				BP(~collIndex).play(argQuant: AbsoluteTimeSpec(syncTime));
				bp.v.eventStreamPlayer.stop;
	//			bp.stopNow(notify: false, doCleanup: false);
			});
			bp.changed(\driven);
			bp.addDependant(currentEnvironment);
		});
		currentEnvironment
	};
	
		// release driver from control -- should not be called directly by user
	~releaseDriver = { |doReplay|
		var	syncTime, nextEvent, oldStream;
		~driver.notNil.if({
			~driver.removeDependant(currentEnvironment);
			~driver.v.isDriven = false;
			~driver.v.eventStream = Pevent(Pseq([~nextEvent, ~driver.v.eventStream], 1),
				~driver.v.event).asStream;
			~driver.v.eventStreamPlayer = PausableEventStreamPlayer(~driver.v.eventStream,
				~driver.v.event).refresh;
			((doReplay ? true) and: { ~driver.v.eventStream.notNil
				and: { (syncTime = ~nextSyncTime.(~saveCurrentEvent)).notNil
				and: { syncTime >= ~driver.v.clock.beats } } }).if({
//~nextEvent[\note].asString.debug("nextevent's note");
//"making new event stream".debug;
				~driver.v.clock.schedAbs(syncTime, ~driver.v.eventStreamPlayer);
				~driver.v.isPlaying = true;
			}, {
				~driver.v.isPlaying = false;
			});
			~driver.changed(#[stop, play][~driver.isPlaying.binaryValue]);
			~driver = nil;
		});
		currentEnvironment
	};
	
	~add = { |bp, parms|
		var	nextSync, childObj, proto = parms.atBackup(\childProto, currentEnvironment);
		(bp.exists and: { bp !== ~driver }).if({
			~children.put(bp.collIndex, childObj = PR(proto).v.copy.prepare(bp, parms));
			childObj.addDependant(currentEnvironment);
			(~clock != bp.v.clock).if({
				"Clocks do not match. There may be scheduling problems.".warn;
			});
				// if I am playing and I know when I am going to fire next
			(~isPlaying and: { (nextSync = ~nextSyncTime.value).notNil }).if({
					// is the bp playing? if so, help it make the transition
				childObj.wasPlaying.if({
					~clock.schedAbs(nextSync - 0.05, {
						childObj.nextSync = nextSync;
						childObj.nextChildEventTime = bp.v.eventStreamPlayer
							.tryPerform(\nextBeat);
						nil
					});
//nextSync.debug("nextSync");
//thisThread.clock.beats.debug("current LT");
					~addChildToQueue.(childObj, nextSync);
				}, {		// else start the child on its normal quant
						// run normally until the following sync point
//bp.eventSchedTime(childObj.quant).debug("starting % play at".format(bp.collIndex));
					bp.play(childObj.quant, ~clock, childObj.doReset, notify: false);
					~clock.schedAbs(bp.eventSchedTime(childObj.quant) - 0.05, e {
//						currentEnvironment.use({
							(nextSync = ~nextSyncTime.value).notNil.if({
//thisThread.clock.beats.debug("now");
//nextSync.debug("adding child to queue for sync time");
								~addChildToQueue.(childObj, nextSync);
							}, {
								bp.stopNow;
							});
//						});
					});
				});
			});
		}, {
			"% is already the driver, can't add as child.".format(bp.asCompileString).warn;
		});
	};
	
		// this is sufficient because routine will not refresh the child in the queue
		// if it can't find it in the ~children dictionary
	~removeChild = { |key, freeing|
		var bpwrap = ~children.removeAt(key), syncTime, esp;
		bpwrap.notNil.if({
			bpwrap.bp.removeDependant(bpwrap);
			bpwrap.removeDependant(currentEnvironment);
			bpwrap.bp.v.isDriven = false;
				// to restart the child's native stream,
				// - I must be playing (or been freed at this logical time)
				// - the child must have been playing before becoming my slave
				//   or it must have been .played directly
			((~isPlaying ? false) or: { ~wasPlayingWhenFreed ? false }).if({
				(bpwrap.wasPlaying/*.debug("removechild-wasplaying")*/).if({
						// wasTriggeredExternally == true on .play or .stop on the child BP
					bpwrap.wasTriggeredExternally/*.debug("wasTriggeredExternally")*/.if({
							// there's some time left before the .play kicks in
						((syncTime = ~nextSyncTime.value/*.debug("nextSync")*/) < bpwrap.bp.eventSchedTime/*.debug("eventSchedTime")*/)
						/*.debug("there is time remaining")*/.if({
								// run the stream in the interim
								// by copying the eventstreamplayer, I ensure that the
								// "real" eventstreamplayer can be restarted in BP-play
// some of this should be delegated to bpholder
							~clock.schedAbs(syncTime - 0.03, {
//bpwrap.nextChildEventTime.debug("running filler stream");
								bpwrap.bp.v.clock.schedAbs(bpwrap.nextChildEventTime,
									esp = bpwrap.bp.v.eventStreamPlayer.refresh.copy);
//(bpwrap.bp.eventSchedTime - 0.03).debug("will stop filler stream");
								bpwrap.bp.v.clock.schedAbs(bpwrap.bp.eventSchedTime - 0.03,
									{ esp.stop; nil });
								nil
							});
						}, {
//(bpwrap.bp.eventSchedTime - 0.03).debug("scheduling abortStream for");
							bpwrap.bp.v.clock.schedAbs(bpwrap.bp.eventSchedTime - 0.03,
								{ bpwrap.abortStream; nil });
						});
					}, {		// removeChild was called directly
						bpwrap.abortStream;
						bpwrap.bp.v.clock.schedAbs(bpwrap.nextChildEventTime,
							bpwrap.bp.v.eventStreamPlayer.refresh);
						bpwrap.bp.v.isPlaying = true;
							// this is ok b/c child dependencies are already gone
						bpwrap.bp.changed(\play);
					});
				});
			});
				
			bpwrap.bp.changed(#[stop, play][bpwrap.bp.isPlaying.binaryValue]);
		});
		currentEnvironment
	};
	
	~bindBP = { |bp, adverb, parms|
		(adverb == \driver).if({
			~driver_.(bp);
		}, {
			~add.(bp, parms);
		});
		currentEnvironment
	};
	
	~nextSyncTime = { |event|
		event = event ? ~currentEvent;
		event !? {
			event.nextSyncTime
				?? { ~eventStreamPlayer.tryPerform(\nextBeat) }
//				?? { event.delta + ~eventStreamPlayer.clock.beats }
		};
	};
	
	~addChildToQueue = { |child, time|
		var	nextChildEventTime;
			// take the bp out of the clock's queue; don't notify dependents
			// but only do it if the child is still accepting events
		time/*.debug("calling addChildToQueue for time")*/.notNil.if({
			(child.processDriverEvent/*.debug("child.processDriverEvent")*/ != false).if({
				~clock.schedAbs(time - 0.03, {
//thisThread.clock.beats.debug("updating nextSync at time");
					(nextChildEventTime = child.bp.v.eventStreamPlayer
							.tryPerform(\nextBeat))/*.debug("nextChildEventTime")*/.notNil.if({
						child.nextChildEventTime = nextChildEventTime;
						child.nextSync = time;
					});
					(child.wasPlaying ? false).if({
//thisThread.clock.beats.debug("stopping child ESP at");
						child.bp.stopNow(notify: false, doCleanup: false);
						nil
					});
					(child.doReset ? false).if({ child.bp.reset });
				});
			});
		}, {
			(child.wasPlaying ? false).if({
//thisThread.clock.beats.debug("stopping child ESP at");
				child.bp.stopNow(notify: false, doCleanup: false);
			});
		});
//(time ? thisThread.clock.beats).debug("child is added for sync time");
		child.bp.v.eventSchedTime = time ? thisThread.clock.beats;
		~queue.put(child.bp.v.eventSchedTime, (process: child.bp.collIndex));
	};

		// should be called only in the context of my event stream
	~prepareQueue = { |time|
		~children.keysValuesDo({ |key, bp|
			~addChildToQueue.(bp, time ? ~nextSyncTime.value ? thisThread.clock.beats);
		});
		~stream = (~driver.v.eventStream ?? { ~driver.asStream });
//(time ? thisThread.clock.beats).debug("adding driver to queue");
		~queue.put(time ? thisThread.clock.beats, (process:\driver));
		(play:0, delta:0)
	};
	
	~popEvents = { 
		var	out = List.new, time;
		~queue.notEmpty/*.debug("queue has items")*/.if({
				// if there are stray events in the queue, drop them
			{ thisThread.clock.beats > ~queue.topPriority }.while({
				~queue.pop/*.debug("dropped event")*/;
			});
			time = ~queue.topPriority/*.debug("top priority")*/;
			{ time == ~queue.topPriority }.while({
				out.add(~queue.pop);
			});
//"\n".post;
//out.do(_.postln);
				// driver must execute first
			out.sort({ |a, b|
				(a.process == \driver).binaryValue > (b.process == \driver).binaryValue
			});
		});	// if queue is empty, return nil
	};
	
	~nextDriverID = { ~driverEventID = ~driverEventID + 1 };
	
	~doDriverEvent = { 
		var	event;
		(~driver.notNil and: { (event = ~stream.next(
				~driver.v.event.copy.put(\children, ~children.copy))).notNil }).if({
			event.put(\ID, ~nextDriverID.value);
			~nextEvent.isNil.if({
				~currentEvent = event;
				~nextEvent = ~stream.next(~driver.v.event.copy.put(\children, ~children.copy))
					.put(\ID, ~nextDriverID.value);
			}, {
				~currentEvent = ~nextEvent;
				~nextEvent = event;
			});
		});
//~currentEvent[\note].asString.debug("currentevent note");
//~nextEvent[\note].asString.debug("nextevent note");
		~currentEvent.notNil.if({
			~currentEvent.play;
			~currentEvent.put(\process, \driver)
				.put(\now, thisThread.clock.beats)
				.put(\nextSyncTime, thisThread.clock.beats + ~currentEvent.delta);
//~currentEvent.debug("\ndriver event");
//thisThread.clock.beats.debug("now");
//~currentEvent.nextSyncTime.debug("nextSyncTime");
			~currentEvent
		}, {
			nil.yield;	// stop immediately when driver returns nil
		});
	};
	
	~doChildEvent = { |evt|
		var	child = ~children[evt.process];
		child.notNil.if({
				// child's job to return the metaevent for my PQ, including offset
			child.tryPerform(\doEvent,
				(child.lastDriverEventID == ~currentEvent[\ID]).if(~nextEvent, ~currentEvent),
				evt);
		});
	};

	~asPattern = { 
		// how to initialize the child streams just before play?
		~eventSchedTime/*.debug("bpdriver-aspattern-eventschedtime")*/.notNil.if({
			~clock.schedAbs(~eventSchedTime - 0.05, e {
//"calling prepareQueue".debug;
				~prepareQueue.(~eventSchedTime/*.debug("eventSchedTime")*/);
			});
		});
		Prout({
			var	queueList, event;
//			~driverWasPlaying = true;
			loop {
				((queueList = ~popEvents.value).size > 0).if({
//queueList.postcs;
					queueList.do({ |evt|
						(evt.process == \driver).if({
							event = ~doDriverEvent.value;
						}, {
							event = ~doChildEvent.(evt);
							event.isNil.if({
"removing child % because event was nil".debug(evt.process.tryPerform(\at, \bp).value.tryPerform(\at, \collIndex));
								~removeChild.(evt.process);
							});
						});
						
							// not stopping on nil event here because
							// the end of a child stream shouldn't affect other children
						event.notNil.if({
								// put continuation in the queue
							~queue.put(thisThread.clock.beats + event.delta, event);
						});
					});
				}, {
					~queue.isEmpty.if({
						nil.yield		// terminate if nothing is in the queue
					});
				});
				
				(~queue.topPriority > thisThread.clock.beats).if({
					(play:0, delta: ~queue.topPriority - thisThread.clock.beats).yield;
				}, {		// something is wrong with the queue, so abort
					"Queue top priority is %, before current time %. Aborting."
						.format(~queue.topPriority, thisThread.clock.beats).warn;
					nil.yield;
				});
			}
		});
	};
	
	~stopCleanup = { |auto|
//"bpdriver-stopCleanup".debug;
//		auto.if({
//			~resumeChildren.value;
//		});
		~children.do(_.abortStream);
		~queue.clear;  // events in the queue are now invalid
		~stopAction.value;	// user-definable
		~saveCurrentEvent = ~currentEvent;	// for restarting driver & children upon free
		~currentEvent = nil;
		currentEnvironment
	};
	
	~freeCleanup = { 
"bpdriver-freeCleanup".debug;
		~children.keys.do({ |key|
//key.debug("setting wasPlaying, removing child");
			~children[key].wasPlaying = ~wasPlayingWhenFreed;
			~removeChild.(key, true);	// true == removing child on free
		});
//"releasing driver".debug;
		~releaseDriver.value;
		currentEnvironment
	};
	
	~update = { |changer, what|
		switch(what)
			{ \lostChild } { 
"removing child because of lostChild".debug(changer.bp.collIndex);
			~removeChild.(changer.bp.collIndex) }
			{ \reset } {   // resync current/next events
				(changer === ~driver).if({	// but only if the driver was reset
					~nextEvent = nil
				})
			}
			{ \free } {
				(changer === ~driver).if({
					~releaseDriver.value;
				});
			}
	};
}) => PR(\bpDriver).subType_(\contrapunct);


PR(\bpHolder).v.clone({
		// child processes should not have .prepareForPlay called on them
	~prepForPlay = {
		(~inited ? false).not.if({
			~bp.v.event = ~bp.prepareEvent;
			~bp.populateAdhocVariables(thisThread.clock);
		});
		~inited = true;
	};
	
	~doEvent = { |driverEvent, childMetaEvent|
		var	inEvent, pattern;

			// processDriverEvent goes false when the child is released by .stop or .play
			// in that case, we should ignore the driver event
		(~processDriverEvent ? true).if({
			~prepForPlay.value;
				// modifyDriver must set the offset
			~lastDriverEventID = driverEvent[\ID];
			driverEvent = ~modifyDriver.(driverEvent.copy, childMetaEvent) ? driverEvent;
			driverEvent.put(\deltaToNextSync, driverEvent.delta + ~offset - ~lastOffset);
			(driverEvent.skipChildren.tryPerform(\includes, ~bp.collIndex) ? false).not.if({
				inEvent = ~bp.v.event.copy.put(\driver, driverEvent);
				~bp.v.driverEvent = driverEvent;
				
				~bp.v.preparePlay;
				~bp.v.eventStream = ~bp.v.asPattern(driverEvent).asStream;

				pattern = ~wrapChildStream.(driverEvent, childMetaEvent);
				pattern = ~prWrapPattern.(pattern, driverEvent, childMetaEvent);

					// play the child until the next sync point
				thisThread.clock.sched(0,
					~eventStreamPlayer = PausableEventStreamPlayer(pattern.asStream, inEvent)
						.refresh);
			});
			
			~lastOffset = ~offset;
			~nextSync = thisThread.clock.beats + driverEvent[\deltaToNextSync];

			(process: ~bp.collIndex, delta: driverEvent.deltaToNextSync);
		});
	};
}) => PR(\bpArpegHolder).subType_(\contrapunct);


AbstractChuckArray.defaultSubType = saveSubtype;
