
// need iMadeDef for defPerc -- per def

// percussion process templates
// first, generic synthdefs

var saveSubType = AbstractChuckArray.defaultSubType;



// abstract process based on buffers
// the process generates a new Pbind per cycle
// amp pattern is specified in terms of subdivisions:
// #[0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0] will sound on beats 2 and 4
// if ~division = 0.25

// other arrays are per event, using wrapAt
AbstractChuckArray.defaultSubType = \drumSequencer;

PR(\abstractProcess).v.clone({
		// grain: true means don't generate a new node id each time (use -1)
	~event = (grain: true, eventKey: \singleSynthPlayer);
	~requiredKeys = #[\amps, \bufPaths];
	~inChannels = 1;
	~outChannels = 2;
	
//	~bufPaths = #[path0, path1...];	// files to load
//	~bufCoords = #[[start0, len0], [start1, len1]...];	// which portions

	~bufCoords = #[[0, -1]];	// default load whole soundfile for each file
	
	~bufs = #[0];		// default: use only first buffer
	~rates = #[1];	// default: play at normal speed
	~deltaAdjust = #[0];	// push samples ahead (-) or behind (+) to compensate for transient time
						// this array's elements correspond to buffers
						// adjustments given in seconds
						// adjustment.neg <= thisBP.leadTime - MUST BE TRUE
	~divCycle = 0.25;	// or #[0.3, 0.2] for swing
	~beatsPerBar = { (~clock ?? { BP.defaultClock }).beatsPerBar };
	~def = \bufGrain;
	~argPairs = nil;	// you may specify other arg keys as an array: [key, value, key, value]
					// if the key's value is not a pattern, a pattern will be made for it
					// using makePattern

	~attack = 0.001;
	~decay = 0.02;
	~mono = false;	// true = automatically prevent note overlaps
	~compensateEnv = false;		// true = subtract attack and decay time from time value
							// so that the synth plays for exactly "time" seconds
		// compensate sensitivity: 1.0 = subtract whole attack/decay duration
		// 0.0 = subtract none (just like ~compensateEnv = false)
		// 0.5 = subtract *half* of attack + decay
	~compensateAmount = 1.0;

	~compactPatterns = true;

	~prep = #{ 
			// user may supply a mixerchannel
			// if so, ~iMadeMixer flag means the channel won't be freed
		~chan.isNil.if({
			~iMadeMixer = true;
			~chan = MixerChannel(~collIndex, ~master.tryPerform(\server) ?? { Server.default },
				~inChannels ? 1, ~outChannels ? 2, outbus:~master);
		}, { ~iMadeMixer = false });
			// this func is environment safe
			// loadbufs is here for defPerc, which may need resources from postMCCreation
		~chan.doWhenReady({ |chan|
			~postMCCreation.(chan);
			~loadBufs.value;
		});
		currentEnvironment
	};
	~postMCCreation = nil;	// pre-buffer exit point to put in your own initialization
	~postBufferLoad = nil;	// post-buffer exit point
	~loadBufs = #{
		if(~bufPaths.isNil and: { ~buffers.notNil }) {
			~iLoadedBuffers = false;
			~bufPaths = ~buffers.collect(_.path);
			~bufTimes = Array.newClear(~bufPaths.size);
			~buffers.do { |buf, i| ~fixBufTime.(i, buf) };
			~postBufferLoad.();
		} {
			~iLoadedBuffers = true;
			~buffers.notNil.if({ ~buffers.free });
			~bufTimes = Array.newClear(~bufPaths.size);
			~buffers = ~bufPaths.collect({ |path, i|
				Buffer.readAndQuery(~chan.server, path, 
					~bufCoords.wrapAt(i)[0], ~bufCoords.wrapAt(i)[1],
					completionFunc: e { |buf|
						~fixBufTime.(i, buf);
						if(i == (~bufPaths.size - 1)) { ~postBufferLoad.() };
				});
			});
		};
	};
	
	~listBufs = { 
		~buffers.do({ |buf, i| "%: %\n".postf(i, buf) })
	};
	
	~replaceBuf = { |i, path, startFrame, numFrames|
		(i >= ~buffers.size).if({
			~addBuf.(path, startFrame, numFrames);
		}, {
			~buffers[i].free;
			~buffers[i] = Buffer.readAndQuery(~chan.server, path, startFrame, numFrames, e { |buf|
				~fixBufTime.(i, buf)
			});
			~buffers[i]
		});
	};
	
	~addBuf = {  "Not yet implemented.".warn; };
	
	~fixBufTime = { |i, buf|
			// try b/c a failure here shouldn't kill the buffer queue
		try { ~bufTimes[i] = buf.numFrames / buf.sampleRate }
			{ |error| error.reportError; }  // display error but continue
	};
	
	~ampsSize = {
		var	divCycleArray = ~divCycle.asArray;
		~beatsPerBar.value / divCycleArray.sum * divCycleArray.size
	};
	
	~makeDeltaPattern = #{ 
		var	current = 0, thisDelta, totalDelta, dStream, deltas;
		~compactPatterns.if({
			deltas = Array.new(~amps.size);
			dStream = Pn(~divCycle.asPattern, inf).asStream;
			thisDelta = totalDelta = 0;
			~amps.do({ |step, i|
				(step > 0).if({	// there's a note here
					(thisDelta > 0).if({
						deltas.add(thisDelta);
						totalDelta = totalDelta + thisDelta;
						thisDelta = 0;
						current = current + 1;
					});
				});
				thisDelta = thisDelta + dStream.next;
			});
			(totalDelta < ~beatsPerBar.value).if({
				deltas.add(~beatsPerBar.value - totalDelta);
			});
			Pseq(deltas, 1)
		}, { Pfinval(~amps.size, Pn(~divCycle.asPattern, inf)) });
	};
	~makeAmpPattern = #{ 
		~compactPatterns.if({
			Pseq(((~amps[0] == 0).if({ #[0] })) ++ ~amps.select(_ > 0), 1);
		}, {
			Pseq(~amps, 1)
		});
	};
	~makeDefPattern = #{ |repeats = 1|
		var	outArray = Array.new(~amps.size), array = ~def.asArray;
		~amps.do({ |amp, i|
			(amp > 0).if({
				outArray.add(array.wrapAt(i));
			});
		});
		if(outArray.size == 0) { outArray.add(\rest) };
		Pseq(outArray, repeats)
	};
	~makePatternFromArray = #{ |array, repeats, sourceKey|
		var amps, outArray, i = 0;
		(~compactPatterns or: { sourceKey == \def }).if({
			amps = ~amps;
			outArray = Array.new(amps.size);
				// if first is a rest, need a placeholder
			(sourceKey != \def and: { amps[0] == 0 }).if({ outArray.add(0) });
			amps.do({ |amp|
				(amp > 0).if({
					outArray.add(array.wrapAt(i));
					i = i + 1;
				});
			});
			Pseq(outArray, repeats)
		}, {
			Pseq(array, repeats)
		});
	};
	~makePattern = #{ |sourceKey, repeats|
//[sourceKey, ~collIndex, currentEnvironment[sourceKey].asArray].debug("making pattern for");
		~makePatternFromArray.(currentEnvironment[sourceKey].asArray, repeats ? 1, sourceKey);
	};
	~addArgPairs = { |out|
		var	argPairs = ~argPairs.value(out);
		if(argPairs.size > 0) {
			out = out.grow(argPairs.size);
			argPairs.pairsDo({ |key, value|
				out.add(key);
					// if you say nil, look for the key in the envir
				value.isNil.if({ value = key.envirGet });
					// temporarily removing this
					// it prevents use of asMap
//				value.isSymbol.if({ value = value.envirGet });
					// this is the only way to get values from the environment
					// while the pattern is running
				value.isFunction.if({ value = value.(out) });
				(value.size > 0).if({	// assume it's an array, use makePattern
					value = ~makePatternFromArray.(value, 1)
				});
				out.add(value);
			});
		};
		out
	};
	
	~makePbindArray = { 
//		~def = ~def.asArray;
		[\amp, ~makeAmpPattern.value,
			\bufIndex, ~makePattern.(\bufs, inf),
			\bufnum, Pfunc({ |ev|
				~buffers.wrapAt(ev[\bufIndex]).bufnum
			}),
			\rate, ~makePattern.(\rates, inf),
// if user specifies times, they should be in beats. default (whole-buffer) times are in seconds
			\time, ~times.notNil.if({
				(~makePattern.(\times, inf) / thisThread.clock.tempo)
			}, {
				Pfunc({ |ev|
					~bufTimes.wrapAt(ev[\bufIndex]) / ev[\rate]
				});
			}),
				// rest if 0, else give synthdef name
//			\instrument, Pif(Pkey(\amp) > 0, ~makePattern.(\def, inf), \rest),
			\instrument, Pif(Pkey(\amp) > 0, ~makeDefPattern.(inf), \rest),
			\chan, Pfunc({ ~chan }),
			\latencyAdjust, Pfunc({ |ev| ~deltaAdjust.wrapAt(ev[\bufIndex])
				* thisThread.clock.tempo }),
			\timingOffset, Pfunc({ |ev| /*ev.debug("event processed for timingOffset");*/ (ev[\timingOffset] ? 0) + (ev[\latencyAdjust] ? 0) }),
			\delta, ~makeDeltaPattern.value,
			\attack, ~makePattern.(\attack, inf), // Pfunc({ ~attack }),
			\decay, ~makePattern.(\decay, inf), // Pfunc({ ~decay }),
			\time, Pfunc({ |ev|	// check for overlap if mono flag is true
				var time = ev[\time];
					// negative time value defaults to entire buffer dur
				(time < 0).if({ time = ~bufTimes.wrapAt(ev[\bufIndex]) / ev[\rate] });
					// compensate for env ramp in/out
				~mono.if({
					time = min(time, ev[\delta] / thisThread.clock.tempo)
				});
				~compensateEnv.if({
					(time - ((ev[\attack] + ev[\decay]) * ~compensateAmount)).max(0.001)
				}, { time });
			})
		]
	};
	
	~pbindPreAction = nil;		// custom action to run before building Pbind can be written here
	~pbindPostAction = nil;		// same, after building Pbind

	~doPreAction = { 
		Func.exists(~pbindPreAction).if({
			Func(~pbindPreAction).doAction
		}, {
			~pbindPreAction.value
		});
	};

	~doPostAction = { |out|
		Func.exists(~pbindPostAction).if({
			Func(~pbindPostAction).doAction(out)
		}, {
			~pbindPostAction.(out)
		});
	};
	
	~sendSynthDef = {
		var	synthdesc;
		~def.do({ |def|
			if((synthdesc = SynthDescLib.global[def]).notNil) {
				synthdesc.send(s);
			};
		});
	};

	~asPattern = { 
		var	out;
		~prepareForPlay.value;	// one use is streams that must persist past 1 bar
		~sendSynthDef.value;
		PnNilSafe(Plazy(e {
			~doPreAction.value;
			out = ~makePbindArray.value;
			out = ~addArgPairs.(out);
			out = ~doPostAction.(out) ? out;	// do postprocessing on pbind pairs
			Pbind(*out);
		}), inf);
	};
	
	~freeCleanup = #{ 
		~iMadeMixer.if({ ~chan.free });
			// if ~iLoadedBuffers is nil, something went wrong during init
			// don't take the chance of freeing somebody else's stuff
		if(~iLoadedBuffers ? false) { ~buffers.free };
		~free.value	// did you make any other resources?
	};	
}) => PR(\bufPerc);


// use functions to make a pack of synthdefs
// alternately, supply synthdef names for preexisting defs
// can also supply Patches (compound Patches are not supported yet)
// it's assumed that the def contains a fixed-length envelope that will free the node (doneAction:2)

PR(\bufPerc).v.clone({
	~defs = #[0];
	~requiredKeys = #[\objects, \amps];
		// user extensible: define your own functions to add objects as synthdefs
		// String and Symbol are supported by nil
	~classActions = IdentityDictionary[
		'SynthDef' -> \makeSynthDef,		// as it happens, these can be the same b/c of asSynthDef
		'Function' -> \makeFnDef,
		'Patch' -> \makePatchDef,
		'WrapPatch' -> \makePatchDef,
		'FxPatch' -> \makePatchDef
	];
	~patchesToFree = List.new;
		// now, instead of loading buffers, we load synthdefs
		// using names defined in bufPerc, though
	~loadBufs = #{ 
		~buffers.notNil.if({ ~buffers.free });
			// .value allows function to create objects array dynamically
		~buffers = ~objects.value.collect({ |obj| 
			~makeDefForObject.(obj).asSymbol
		});
	};
	~makeDefForObject = #{ |obj|
		var return;
		(return = ~classActions[obj.class.name]).notNil.if({
			return = return.envirGet.(obj)
		});
		return ?? { obj }	// if nothing, return the object itself
	};
	~makeFnDef = #{ |fn|
		var def;
		def = fn.asSynthDef(outClass: 
			(~isFx ? false).if({ \ReplaceOut }, { \Out }))
			.perform(Library.at(\ddwGlobal, \storeMethod));
		def.name
	};
	~makePatchDef = #{ |patch|
		var def;
		def = patch.asSynthDef.perform(Library.at(\ddwGlobal, \storeMethod));
		~patchesToFree.add(patch);
		def.name
	};
	~makeSynthDef = #{ |def| def.perform(Library.at(\ddwGlobal, \storeMethod)).name };
	~replaceDef = { |i, obj|
		(i >= ~buffers.size).if({
			~addBuf.(obj);
		}, {
			~chan.server.sendMsg(\d_free, ~buffers[i]);
			~buffers[i] = ~makeDefForObject.(obj).asSymbol;
			~buffers[i]
		});
	};
	
	~addBuf = {  "Not yet implemented.".warn; };

	~makePbindArray = { 
		[\amp, ~makeAmpPattern.value,
				// rest if 0, else give synthdef name
			\defIndex, ~makePattern.(\defs),
			\instrument, Pfunc({ |ev|
				(ev[\amp] > 0).if({
					~buffers.wrapAt(ev[\defIndex])
				}, { \rest });
			}),
			\chan, Pfunc({ ~chan }),
			\delta, ~makeDeltaPattern.value
		]
	};

	~superfree = ~freeCleanup;
	~freeCleanup = { 
		~patchesToFree.do(_.free);
		~superfree.value;
	};

}) => PR(\defPerc);

ProtoEvent.composite(#[singleSynthTrigger, singleSynthPlayNotify]) => ProtoEvent(\defTrigEvent);

PR(\defPerc).v.clone({
	~event = (eventKey: \defTrigEvent);
	~alwaysReset = true;
	~isFx = false;
	~superprep = ~prep;
	~prep = {
		~superprep.();
			// synthdefs are inited after MixerChannel is ready, so...
		~chan.doWhenReady({
			~eventInitArgs = { () } ! (~buffers.size);
		});
	};
	~makePbindArray = { 
		[\trig, ~makeAmpPattern.value,
				// rest if 0, else give synthdef name
			\defIndex, ~makePattern.(\defs),
			\instrument, Pfunc({ |ev| ~buffers[ev[\defIndex]] }),
			\node, Pfunc({ |ev|
				(ev[\trig] > 0).if({
					~nodes.wrapAt(ev[\defIndex]) ?? { \dummy }
				}, { \rest });
			}),
			\chan, Pfunc({ ~chan }),
			\delta, ~makeDeltaPattern.value
		]
	};
		// SynthDesc msgFunc expects args to exist in the event
		// for simplicity they'll go in the event's proto
		// but you might have different inits for different synths
		// so the protos are kept in an array -- see the .collect
		// in asPattern for their use in playing events
	~setInitArgsInEvent = { |node, i|
		var	proto = ~eventInitArgs[i] ?? { () },
			return;
		(return = ~initArgs.value(node, i)).pairsDo({ |key, val|
			proto.put(key, val.value);
		});
		proto.put(\outbus, ~chan.inbus.index)
			.put(\out, ~chan.inbus.index)
			.put(\i_out, ~chan.inbus.index);
		~eventInitArgs[i] = proto;
		return
	};

	~recvEventNotify = { |node, event|
		~nodes[event[\defIndex]] = node;
		~nodeEventKeys[event[\defIndex]] = \singleSynthTrigger;
	};

	~asPattern = {
		Pseq([
			Pfuncn({	// first, initialize node arrays
				var latency;
				(~nodes.size == 0).if({
						// first event for each index must be to play the node,
						// then notify me
					~nodeEventKeys = \singleSynthPlayNotify ! ~buffers.size;
					~nodes = Array.newClear(~buffers.size);
				});
				(play: 0, delta: 0)
			}, 1),
			PnNilSafe(Plazy(e {
				var	out;
				~doPreAction.value;
				out = ~makePbindArray.value;
				out = ~addArgPairs.(out);
				out = ~doPostAction.(out) ? out;	// do postprocessing on pbind pairs
				Pbind(*out);
			}), inf).collect({ |ev|
				ev.proto = ~eventInitArgs[ev[\defIndex]];
					// which one of the composite events to use?
				ev.protoEvent = ~nodeEventKeys[ev[\defIndex]];
				ev
			});
		]);
	};
	
	~stopCleanup = {
		var	latency, hasGate;
		(~nodes.size > 0).if({
			latency = (~leadTime !? { ~leadTime / ~clock.tempo })
				+ ~chan.server.latency;
			~nodes.do({ |node, i|
				if(node.notNil) {
					hasGate = SynthDescLib.global[~buffers[i]].tryPerform(\hasGate);
					~chan.server.sendBundle(latency,
						(~hasGate ? false).if({ node.setMsg(\gate, 0) }, { node.freeMsg }))
				};
			});
			~nodes = nil;
		});
	};
}) => PR(\defTrig);

// loads the whole beat into 1 buffer; user specifies start positions for segments and times (beats)
// multiple files are supported -- use bufs to select file
PR(\bufPerc).v.clone({
	~requiredKeys = #[bufPaths, amps, start];
	~segStart = #[[0]];	// default, 1 segment encompassing the whole file
	~start = #[0];
	~mono = true;
	~compensateEnv = true;
	
	~makeStartTimes = { 
		~makePatternFromArray.(~start, 1).collect(e { |st, event|
			~segStart.wrapAt(event[\bufIndex]).wrapAt(st)
		});
	};
		
	~makePbindArray = { 
		[\amp, ~makeAmpPattern.value,
			\bufIndex, ~makePattern.(\bufs),
			\bufnum, Pfunc({ |ev|
				~buffers[ev[\bufIndex]].bufnum
			}),
			\rate, ~makePattern.(\rates),
// if user specifies times, they should be scaled by tempo. default times are in seconds
			\start, ~makeStartTimes.value,
			\time, ~times.notNil.if({
				~makePattern.(\times) / thisThread.clock.tempo
			}, {
				Pfunc({ |ev|
					~bufTimes[ev[\bufIndex]]
				});
			}),
				// rest if 0, else give synthdef name
			\instrument, Pfunc({ |ev|
				(ev[\amp] > 0).if({ ~def }, { \rest });
			}),
			\chan, Pfunc({ ~chan }),
			\latencyAdjust, Pfunc({ |ev| ~deltaAdjust.wrapAt(ev[\bufIndex]) }),
			\timingOffset, Pfunc({ |ev| /*ev.debug("event processed for timingOffset");*/ (ev[\timingOffset]/*.debug("timingOffset in calc")*/ ? 0) + (ev[\latencyAdjust] ? 0) }),
			\delta, ~makeDeltaPattern.value,
			\attack, ~makePattern.(\attack), // Pfunc({ ~attack }),
			\decay, ~makePattern.(\decay), // Pfunc({ ~decay }),
			\time, Pfunc({ |ev|	// check for overlap if mono flag is true
				var time = ev[\time];
					// negative time value defaults to entire buffer dur
				(time < 0).if({ time = ~bufTimes.wrapAt(ev[\bufIndex]) / ev[\rate] });
					// compensate for env ramp in/out
				~mono.if({
					time = min(time, ev[\delta] / thisThread.clock.tempo)
				});
				~compensateEnv.if({
					(time - ((ev[\attack] + ev[\decay]) * ~compensateAmount)).max(0.001)
				}, { time });
			})
		]
	};
}) => PR(\break);

AbstractChuckArray.defaultSubType = saveSubType;

// a simple soundfile-segment player
// uses bufPerc's buffer management methods but constructs events from streams, not arrays

PR(\abstractProcess).v.clone({
	~event = (eventKey: \singleSynthPlayer);
	~requiredKeys = #[bufPaths];
	~def = \bufGrain;
	~bufCoords = #[[0, -1]];
	~attack = 0.005;
	~decay = 0.02;
	~argPairs = [];	// there must be a default or ~argPairsStream fails
	~postMCCreation = nil;		// put in your own user preparation func here
	~timeConversion = { |time, ev| time };
	~argPairs_ = { |args|
		~argPairs = args;
		~argPairsStream = Pbind(*~argPairs).asStream;
	};
		// required: put in a pattern factory here
	~asPattern = {
			// assume that if we're calling asPattern, we want to reset all streams
		~sendSynthDef.value;
		~argPairsStream = Pbind(*~argPairs).asStream;
		Pbind(\instrument, BPStream(\def),
			\bufIndex, BPStream(\bufIndex),
			\bufnum, Pfunc({ |ev| ~buffers[ev[\bufIndex]].bufnum }),
			\time, BPStream(\time).collect({ |time, ev| ~timeConversion.(time, ev) }),
			\rate, BPStream(\rate),
			\start, BPStream(\start),
			\amp, BPStream(\amp),
			\attack, BPStream(\attack),
			\decay, BPStream(\decay),
			\chan, ~chan,
			\delta, BPStream(\delta),
			\time, Pkey(\time) / Pfunc({ thisThread.clock.tempo })
		).collect({ |ev|
			~argPairsStream !? { ev = ~argPairsStream.next(ev) };
			ev
		});
	};
}).import((bufPerc: #[prep, loadBufs, fixBufTime, replaceBuf, addBuf, freeCleanup]))
	=> PR(\basicBufferPlayer).subType_(\bufPlayer);
