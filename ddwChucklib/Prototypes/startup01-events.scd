
// check SynthDescLib behavior for polySynthPlayer
// polysynthplayer / trigger support for array args
// voicer note must either have delta, length, gate specified or a single note as input - trying to get parms from array notes breaks
// add repetition-factor metric to adaptSeg
// note for doc: asNotePattern is needed because sometimes I want streams that return SequenceNotes,
// not events

// something useful
{ |source, class, key, adverb, parms|
	if(class.exists(key).not) {
		source.chuck(class.new(key), adverb, parms);
	};
} => Func(\chuckIf);


// a pattern helper: like a combination of Pchain and Psetp
Proto {
	~canEmbed = true;
	~outerOverridesInner = true;
	// how many empty embeds of the inner pattern are permitted?
	// without this, infinite outer pattern + failed inner pattern == infinite loop
	~nilTolerance = 1;
	~prep = { |outerPat, innerPat, envir|
		~outerPat = outerPat;
		~innerPat = innerPat;
		~envir = envir ?? { currentEnvironment };
		~nilTolerance = max(~nilTolerance, 1);
		currentEnvironment
	};
	~embedInStream = { |inval|
		var self = currentEnvironment,
		outerStream = ~envir.use { self[\outerPat].asStream },
		innerStream,
		outerEvent, innerEvent,
		nilTolerance, didYield;

		nilTolerance = ~nilTolerance;
		while { nilTolerance > 0 and: { (outerEvent = outerStream.next(inval)).notNil } } {
			didYield = false;
			innerStream = ~envir.use { self[\innerPat].asStream };
			while { (innerEvent = innerStream.next(inval.copy.putAll(outerEvent))).notNil } {
				if(self[\outerOverridesInner] == true) { innerEvent.putAll(outerEvent) };
				inval = innerEvent.yield;
				didYield = true;
			};
			if(didYield) {
				nilTolerance = ~nilTolerance;
			} {
				nilTolerance = nilTolerance - 1;
			};
		};
		inval
	};
	~asStream = { Routine { |inval| ~embedInStream.(inval) } };
	~asPattern = { Prout(e { |inval| ~embedInStream.(inval) }) };
	~play = { |clock, protoEvent, quant|
		~asPattern.().play(clock, protoEvent, quant)
	};
} => PR(\psetp2);



// process prototypes

// start with the abstract

Proto({
	// environment variables supplied by "subclasses"
	// ~event, ~prep, ~stopCleanup, ~freeCleanup, ~preparePlay

	~event = (eventKey: \default);

	~bindPatDefault = \a;	// aPattern, aStream -- clones should override

	// mainly used for adaptPattern, but other uses are conceivable
	~bindPattern = #{ |pat, adverb|
		adverb = adverb ? ~bindPatDefault;
		currentEnvironment.put(adverb.asSymbol, pat = pat.asPattern);
		currentEnvironment.put((adverb ++ "Stream").asSymbol, pat.asStream);
		currentEnvironment
	};

	~bindSymbol = #{ |sym, adverb|
		var	pat;
		((pat = Pdefn(sym)).pattern != Pdefn.default).if({
			// asPattern gets called in ~bindPattern
			~bindPattern.value(pat, adverb)
			}, {
				~bindPattern.value(sym, adverb)
		});
		currentEnvironment
	};

	// special case for Synth Args
	~bindSA = #{ |sa, adverb|
		~argsStream = sa.asPattern.asStream;
		~argKeys = sa.argKeys;
		currentEnvironment
	};

	// these next two are deprecated -- BPStream(key) is a better solution
	// retained for backward compatibility

	~makeStreamForKey = #{ |key, streamKey|
		// output, and stream gets replaced so that playing stream picks it up:
		(streamKey = streamKey ?? { key ++ "Stream" }).asSymbol.envirPut(key.envirGet.asStream)
	};

	~makeProut = #{ |key, reset|
		var	streamKey;
		// create stream if it doesn't exist
		streamKey = (key ++ "Stream").asSymbol;
		(streamKey.envirGet.isNil or: { reset ? true }).if({
			~makeStreamForKey.value(key, streamKey);
		});

		Prout({ |inEvent|
			{ inEvent = streamKey.envirGet.next(inEvent).yield }.loop
		});
	};

	// temporary override of a pattern used with BPStream
	// ref means, if the pattern returns a symbol, look up the target stream
	// revertAction is run when control returns to the main stream
	~override = { |key, pat, ref(true), revertAction, safe(true)|
		var	streamkey = (key ++ "Stream").asSymbol,
		savekey = ("save" ++ streamkey).asSymbol,
		overkey = ("override" ++ streamkey).asSymbol,
		overstream;

		if(safe.not or: { overkey.envirGet.isNil }) {
			// not safe to assume pattern has been .asStream'ed yet
			streamkey.envirGet ?? { ~makeStreamForKey.(key) };

			savekey.envirGet.isNil.if({
				savekey.envirPut(streamkey.envirGet);
			});

			overkey.envirPut(pat.asStream);

			// need a variant on cleanupstream that will seamlessly hand control back
			// to the original stream; cleanupstream returns nil and stops the parent stream
			overstream = Routine({ |inval|
				var	nextval;
				while { (nextval = overkey.envirGet.next(inval)).notNil }
				{	ref.if({
					(nextval.isSymbol and: { nextval.envirGet.notNil }).if({
						inval = nextval.envirGet.next(inval).yield;
					}, { inval = nextval.yield; });
					}, {
						inval = nextval.yield;
					});
				};
				// end of the line; restore the original stream
				// and return 1 valid value so that the main pattern doesn't die
				// after substitution, this routine will not get called again
				~unOverride.(key, revertAction, streamkey, savekey, overkey);
				streamkey.envirGet.next(inval).yield;
			});
			streamkey.envirPut(overstream);
		} {
			"BP(%): Cannot override a parameter that is already overridden."
			.format(~collIndex.asCompileString)
			.warn;
		};
		currentEnvironment
	};
	~unOverride = { |key, revertAction, streamkey, savekey, overkey|
		if(streamkey.isNil) {
			streamkey = (key ++ "Stream").asSymbol;
			savekey = ("save" ++ streamkey).asSymbol;
			overkey = ("override" ++ streamkey).asSymbol;
		};
		if(savekey.envirGet.notNil) {
			streamkey.envirPut(savekey.envirGet);
			savekey.envirPut(nil);
			overkey.envirPut(nil);
			revertAction.value;
		};
		currentEnvironment
	};

	~midiParse = true;		// flag whether to group notes together or use the midi buf as is
	// parsing is better for adaptive sequencing objects

	~canWrap = false;

	~isPlaying = false;
	~isWaiting = false;
	~isDriven = false;

}) => PR(\abstractProcess);


// one shot PR - sets a base barline in the global library

Proto({
	~barLength = 4;
	~path = #[baseBeat];	// may override
	// run this action when resuming rhythmic patterns
	~doAction = { |path|
		var	beats = ~barLength.nextTimeOnGrid(thisThread.clock);
		Library.global.putAtPath(path ? ~path, beats);
		thisThread.clock.setMeterAtBeat(~barLength, beats);
	};
	~freeCleanup = {
		Library.global.removeAtPath(~path)
	};
}) => PR(\setBase);


// basic events first

(play:0) => ProtoEvent(\dummy);

// the default event handles latency differently from BP
Event.default.copy => ProtoEvent(\default);

// Event uses functions to schedule message sends according to timingOffset
// copy these functions into Func repository for these events

Event.default[\schedBundle] => Func(\basicSchedBundle).subType_(\eventHelper);
Event.default[\schedBundleArray] => Func(\basicSchedBundleArray).subType_(\eventHelper);
Event.default[\schedStrummedNote] => Func(\basicSchedStrummedNote).subType_(\eventHelper);


{ |lag, offset, server ...bundle|
	(~immediateOSC ? false).if({
		server.sendBundle(nil, *bundle)
		}, {
			Func(\basicSchedBundleArray).value(lag, offset, server, bundle)
	});
} => Func(\schedEventBundle).subType_(\eventHelper);

{ |lag, offset, server, bundleArray, suppressError = false|
	if(suppressError) {
		bundleArray = bundleArray.insert(0, #[error, -1])
		.add(#[error, -2]);
	};
	(~immediateOSC ? false).if({
		server.sendBundle(nil, *bundleArray);
		}, {
			Func(\basicSchedBundleArray).value(lag, offset, server, bundleArray)
	});
} => Func(\schedEventBundleArray).subType_(\eventHelper);

Event.default[\schedStrummedNote] => Func(\schedEventStrummedNote).subType_(\eventHelper);


(grain: false,
timingOffset: 0,
addAction: \addToTail,
setTarget: {
	~chan.notNil.if({
		~target = (~isFx ? false).if({ ~chan.effectgroup },
			{ ~chan.synthgroup });
		~server = ~target.server;
		~bus = ~chan.inbus;
		~busindex = ~bus.index;
		}, {
			~target.isNil.if({
				~target = Server.default.asTarget;
			});
			~server = ~target.server;
			~bus.notNil.if({
				~bus = ~bus.asBus;
				~busindex = ~bus.index;
				}, {
					~busindex = 0;
			});
	});
},
setArgs: {
	var	args, soloargs, polyargs, lib, desc;
	((lib = SynthDescLib.all[~lib ? \global]).notNil
		and: { (desc = lib[~instrument.asSymbol]).notNil }).if({
		~hasGate = desc.hasGate;
		~msgFunc = desc.msgFunc;
		}, {
			~msgFunc = {
				args = Array.new(currentEnvironment.size*2);
				currentEnvironment.keysValuesDo({ |key, value|
					value.isValidSynthArg.if({ args.add(key).add(value) });
				});
				args
			};
	});
	// not .flopping because this is SINGLE synth player
	~evArgs = ~msgFunc.valueEnvir;
},
makeNode: {
	var	args, nodeID, server, sustain,
	tempo = ~clock.tryPerform(\tempo) ? 1.0,
	bundle,
	lag = ~lag ? 0,
	offset = ~timingOffset/*.debug("~timingOffset")*/ ? 0,
	releaseGate = ~releaseGate ? 0;
	if((currentEnvironment.tryPerform(\isRest) ? false).not and: { ~instrument != \rest and: { ~freq != \rest and: { ~type != \rest } } }) {
		~setTarget.value;
		~busindex.notNil.if({
			server = ~server;	// for ~trace == true
			~setArgs.value;
			(~debug == true).if({
				//thisThread.clock.debug("my clock");
				thisThread.clock.beats.debug("\nnow"); ~evArgs.debug("singleSynthPlayer"); });
			~grain = ~grain ? false;
			nodeID = ~id ?? { ~grain.if({ -1 }, { ~server.nextNodeID }) };
			~node = Synth.basicNew(~instrument, ~server, nodeID);
			bundle = [~node.newMsg(~target, ~evArgs.asOSCArgArray ++ [\outbus, ~busindex,
				\out, ~busindex, \i_out, ~busindex], ~addAction)];
			Func(\schedEventBundleArray).doAction(lag, offset, server, bundle);
			(~trace == true).if({
				Func(\schedEventBundle).doAction(lag + 0.05, offset, server, [\n_trace, nodeID]);
			});
			(~grain.not and: { ~hasGate ? false
				and: { (sustain = ~sustain.value).notNil and: { sustain != inf } } }).if({
				thisThread.clock.sched(sustain, {
					Func(\schedEventBundleArray).doAction(lag, offset, server,
						[[\n_set, nodeID, \gate, releaseGate]], true);
					nil
				});
			});
		});
	};
},
play: {
	~makeNode.value;
	(~collIndex.notNil and: { ~grain.not }).if({
		~notifyDependents.value;
	});
}) => ProtoEvent(\singleSynthPlayer);

ProtoEvent(\singleSynthPlayer).v.copy.putAll((
	notifyDependents: {
		~node !? {
			BP(~collIndex).v.recvEventNotify(~node, currentEnvironment);
		};
	}
)) => ProtoEvent(\singleSynthPlayNotify);

// backward compatibility
ProtoEvent(\singleSynthPlayNotify).v => ProtoEvent(\bufStatusUpdate);


ProtoEvent(\singleSynthPlayer).v.copy.putAll((play: {
	var args, nodeID, msg, bundle, tempo = ~clock.tryPerform(\tempo) ? 1.0;
	// the full condition is needed for Ppar which uses ~freq == \rest
	if((currentEnvironment.tryPerform(\isRest) ? false).not and: { ~node != \rest and: { ~instrument != \rest and: { ~freq != \rest and: { ~type != \rest } } } }) {
		~lag ?? { ~lag = 0 };
		~node = ~node.value;
		~instrument = ~node.defName;
		~setArgs.value;
		msg = [~node.setMsg(*~evArgs)];
		Func(\schedEventBundleArray).doAction(~lag, ~timingOffset, ~node.server, msg);
		(~debug ? false).if({
			msg.debug("singleSynthTrigger")
		});
	}
})) => ProtoEvent(\singleSynthTrigger);

ProtoEvent.composite(#[singleSynthTrigger, singleSynthPlayNotify, polySynthTrigger, polySynthPlayNotify]) => ProtoEvent(\monoSupport);


ProtoEvent(\singleSynthPlayer).v.copy.putAll((
	// setArgs: {
	// 	var	args, lib, desc;
	// 	((lib = SynthDescLib.all[~lib ? \global]).notNil
	// 			and: { (desc = lib[~instrument.asSymbol]).notNil }).if({
	// 		~evArgs = desc.msgFunc.valueEnvir.clump(2);
	// 	}, {
	// 		args = Array.new(currentEnvironment.size);
	// 			// note that this is a clumped array --
	// 			// that's necessary for flopping (multichannel expanding) the args out in makeNode
	// 		currentEnvironment.keysValuesDo({ |key, value|
	// 			value.isValidVoicerArg.if({ args.add([key, value]) });
	// 		});
	// 		~evArgs = args;
	// 	});
	// },
	play: { var args, nodeID;
		if((currentEnvironment.tryPerform(\isRest) ? false).not and: { ~node != \rest and: { ~instrument != \rest and: { ~freq != \rest and: { ~type != \rest } } } }) {
			if(~lag.isNil) { ~lag = #[0] } { ~lag = ~lag.asArray };
			if(~timingOffset.isNil) { ~timingOffset = #[0] } { ~timingOffset = ~timingOffset.asArray };
			~node = ~node.value;
			// assumed to be the same synthdef for each
			// maybe not a safe assumption but a reasonable limitation for now
			~instrument ?? { ~instrument = ~node.asArray.first.defName };
			~setArgs.value;
			~msgArgs = ~evArgs.clump(2)
			.collect(_.flop)
			.flop
			.collect({ |subarray| subarray.flatten(1).asOSCArgArray });
			~node.do({ |node, i|
				Func(\schedEventBundle).doAction(~lag.wrapAt(i), ~timingOffset.wrapAt(i), node.server,
					node.setMsg(*~msgArgs.wrapAt(i)));
			});
		}
})) => ProtoEvent(\polySynthTrigger);


// Seems to work but might need some more testing
// currently does not support multiple output buses - maybe it does now?
// but there should be just 1 instrument

ProtoEvent(\singleSynthPlayer).v.copy.putAll((
	setTarget: {
		~chan.notNil.if({
			~chan = ~chan.asArray;
			~target = ~chan.collect({ |chan|
				(~isFx ? false).if({ chan.effectgroup },
					{ chan.synthgroup });
			});
			~server = ~target.collect(_.server);
			~bus = ~chan.collect(_.inbus);
			}, {
				~target.isNil.if({
					~target = Server.default.asTarget;
				});
				~target = ~target.asArray;
				~server = ~target.collect(_.server);
				~bus.notNil.if({
					~bus = ~bus.asArray.collect(_.asBus);
					}, {
						~bus = ~server.collect({ |server| Bus(\audio, 0, 2, server) });
				});
		});
	},
	checkbuses: {
		block { |break|
			~bus.do({ |bus|
				bus.tryPerform(\index).isNumber.not.if({ break.(false) });
			});
			true
		}
	},
	// setArgs: {
	// 	var	args, lib, desc;
	// 	((lib = SynthDescLib.all[~lib ? \global]).notNil
	// 			and: { (desc = lib[~instrument.asSymbol]).notNil }).if({
	// 		~evArgs = desc.msgFunc.valueEnvir.clump(2);
	// 		~hasGate = desc.hasGate;
	// 	}, {
	// 		args = Array.new(currentEnvironment.size);
	// 			// note that this is a clumped array --
	// 			// that's necessary for flopping (multichannel expanding) the args out in makeNode
	// 		currentEnvironment.keysValuesDo({ |key, value|
	// 			value.isValidVoicerArg.if({ args.add([key, value]) });
	// 		});
	// 		~evArgs = args;
	// 	});
	// },
	makeOneNode: { |i, strum|
		var	instr, nodeID, latency, sustain, server = ~server.wrapAt(i), offset = ~timingOffset ? 0,
		releaseGate = ~releaseGate ? 0;
		((instr = ~instrument/*.wrapAt(i)*/) != \rest).if({
			nodeID = ~id[i] ?? { ~grain.if({ -1 }, { server.nextNodeID }) };
			~node[i] = Synth.basicNew(instr, server, nodeID);
			latency = (strum*i) + ~lag;
			Func(\schedEventBundle).doAction(latency, offset, server,
				~node[i].newMsg(~target.wrapAt(i),
					~msgArgs.wrapAt(i) ++ [\out, ~bus.wrapAt(i).index,
						\i_out, ~bus.wrapAt(i).index, \outbus, ~bus.wrapAt(i).index],
					~addAction)
			);
			sustain = if(~sustain.size > 0) { ~sustain.value.wrapAt(i) } { ~sustain.value };
			(~grain.not and: { ~hasGate ? false
				and: { sustain.notNil and: { sustain != inf } } }).if({
				thisThread.clock.sched(sustain, {
					Func(\schedEventBundleArray).doAction(latency, offset, server,
						[[\n_set, nodeID, \gate, releaseGate]], true);
					nil
				});
			});
		});
	},
	makeNode: {
		var args, instr, nodeID, strum, latency;
		if((currentEnvironment.tryPerform(\isRest) ? false).not and: { ~instrument != \rest and: { ~freq != \rest and: { ~type != \rest } } }) {
			~lag ?? { ~lag = 0 };
			~setTarget.value;
			~checkbuses.value.if({
				//				~instrument = ~instrument.asArray;
				~setArgs.value;
				~msgArgs = ~evArgs.clump(2)
				.collect(_.flop)
				.flop
				.collect({ |subarray| subarray.flatten(1).asOSCArgArray });
				strum = ~strum ? 0;
				~grain = ~grain ? false;
				~node = Array.newClear([/*~instrument.size,*/ ~msgArgs.size, ~bus.size,
					~target.size].maxItem);
				// ~makeOneNode populates the right slot in ~node array
				~id = ~id.asArray;
				~node.size.do({ |i| ~makeOneNode.(i, strum) });
			});
		};
	}
)) => ProtoEvent(\polySynthPlayer);

ProtoEvent(\polySynthPlayer).v.copy.putAll((
	notifyDependents: {
		~node !? {
			BP(~collIndex).v.recvEventNotify(~node, currentEnvironment);
		};
	}
)) => ProtoEvent(\polySynthPlayNotify);

// voicer events (for ai sequencing):

(	timingOffset: 0,
stretch: 1.0,
// args: [],
// maybe you want to use non-equal-temperament. override this func
// or, if you are using modally-mapped values, include the cpsFunc or EqualTemperament derivative
// in the ModalSpec itself
midiNoteToFreq: #{ |notenum|
	~mode.notNil.if({ ~mode.asMode.cpsFunc.value(notenum) },
		{ notenum.midicps });
},

prepNote: #{
	var i, args, argval, thisEvent = currentEnvironment;
	~newFreq = ~freq ?? { ~note.asFloat };
	~mtranspose.notNil.if({ ~newFreq = ~newFreq + ~mtranspose });
	(~midi ? false).not.if({ ~newFreq = ~newFreq.unmapMode(~mode.asMode) });
	~ctranspose.notNil.if({ ~newFreq = ~newFreq + ~ctranspose });

	~newFreq = ~midiNoteToFreq.value(~newFreq).asArray;
	~dur = ~dur ?? { ~delta ?? { ~note.dur } };
	~length = (~length ?? { ~note.length }).asArray;

	// some patterns (e.g. Pfindur) might shorten the delta
	// in which case length could be too long
	// but this really applies only to MonoPortaVoicers,
	// hence the adjust... test
	if(~adjustLengthToRealDelta.value and: { ~dur != currentEnvironment.delta }) {
		~length = ~length * currentEnvironment.delta / ~dur;
	};

	~args = ~args ?? { [~note.tryPerform(\args)].tryPerform(\flatten, 1) } ?? { [] };
	i = 0;	// args should be key value pairs, but might be an array of velocities
	// drop pairs that are not \symbol, value
	{ i < ~args.size }.while({
		~args[i].isSymbol.not.if({
			try { ~args.removeAt(i); ~args.removeAt(i); };
			}, {
				i = i + 2;	// should increment only if not removing an item
		});
	});
	~gate = (~gate ?? { ~note.gate }).asArray;

	// for args array to be valid (argName, value pairs), must have at least 2 items
	(~args.size < 2).if({ ~args = nil });
	if(~voicer.notNil) {
		if(~nodes.isNil) {
			~nodes = ~voicer.prGetNodes(max(~newFreq.size, max(~length.size, ~gate.size)));
		};
		~voicer.setArgsInEvent(currentEnvironment);
	};
	~bassID.notNil.if({
		~note ?? { ~note = SequenceNote(~freq, ~dur, ~length[0], ~gate[0]) };
		Library.put(~bassID, ~note);
		// allow this thread to finish before alerting dependents
		thisThread.clock.sched(0, { BP.changed(thisEvent[\bassID], thisEvent); });
	});
},

play: #{
	var	lag = ~lag ? 0,  // ~timingOffset !? { ~timingOffset / ~clock.tempo };
	timingOffset = ~timingOffset ? 0,
	clock = ~clock,
	voicer = ~voicer,
	bundle, releaseGate;
	~prepNote.value;
	~finish.value;	// user-definable
	(~debug == true).if({
		"\n".debug;
		["voicerNote event", ~clock.beats, ~clock.tempo].debug;
		currentEnvironment.collect({ |value| value.isFunction.not.if(value, nil) }).postcs;
	});
	releaseGate = (~releaseGate ? 0).asArray;
	~nodes.do({ |node, i|
		var	freq = ~newFreq.wrapAt(i), length = ~length.wrapAt(i);
		Func(\schedEventBundleArray).doAction(lag, ~timingOffset, node.server,
			node.server.makeBundle(false, {
				node.trigger(freq, ~gate.wrapAt(i), ~args.wrapAt(i), node.server.latency);
		}));
		(length.notNil and: { length != inf }).if({
			// can't use schedEventBundle
			// because you must not release the VoicerNode
			// until the proper time comes
			thisThread.clock.sched(length + timingOffset, {
				voicer.releaseNode(node, freq, releaseGate.wrapAt(i),
					lag + (node.server.latency ? 0));
			});
		});
	});
},
// for live midi input -- assumes midi note has been put into ~note
releaseNote: #{
	var lag, timingOffset;
	((~immediateOSC ? false) or: { ~voicer.target.server.latency.isNil }).if({
		~voicer.release(~newFreq);
		}, {
			lag = ~lag ?? { 0 };
			timingOffset = ~timingOffset ?? { 0 };
			~voicer.release(~newFreq,
				((lag + timingOffset) / (~clock ?? { thisThread.clock }).tempo) + ~voicer.target.server.latency);
	});
},

// you could override this
adjustLengthToRealDelta: { ~voicer.isKindOfByName(\MonoPortaVoicer) },

keysToPropagate: #[\voicer, \midi, \mode, \timingOffset, \argKeys, \immediateOSC]
) => ProtoEvent(\voicerNote);

// synthNote
ProtoEvent(\voicerNote).v.copy.putAll((
	addAction: \addToTail,
	play: #{
		var	synthLib;
		// cribbed from Event.default
		if (~msgFunc.isNil) {
			synthLib = ~synthLib ?? { SynthDescLib.global };
			~desc = synthLib.synthDescs[~instrument];
			if (~desc.notNil) {
				~hasGate = ~desc.hasGate;
				~msgFunc = ~desc.msgFunc;
			}{
				~hasGate = ~hasGate ? true;
				~msgFunc = ~defaultMsgFunc;
			};
		}{
			~hasGate = ~hasGate ? true;
		};	// end crib
		~prepNote.value;
		~finish.value;	// user-definable
		~lag ?? { ~lag = 0 };
		(~freq.isSymbol.not and: { ~desc.notNil }).if({
			(~freq.size > 0).if({
				(~gate.size == 0).if({ ~gate = [~gate] });
				(~length.size == 0).if({ ~length = [~length] });
				~freq.do({ |freq, i|
					~playOneNote.value(freq, ~gate.wrapAt(i), ~length.wrapAt(i));
				});
				}, {
					~playOneNote.value(~freq, ~gate, ~length)
			});
		});
	},

	playOneNote: #{ |freq, gate, length|
		var	synth, server, groupbus;
		server = ~server ? ~target.server;
		synth = Synth.basicNew(~instrument, server);
		// if groupbus is not nil, then ~target is a MixerChannel
		(groupbus = ~target.tryPerform(\groupBusInfo)).notNil.if({
			Func(\schedEventBundle).doAction(~lag, ~timingOffset, server, synth.newMsg(groupbus[0],
				~args ++ [\outbus, groupbus[1], \out, groupbus[1],
					\freq, freq, \gate, gate], ~addAction));
			}, {
				Func(\schedEventBundle).doAction(~lag, ~timingOffset, server, synth.newMsg(~target,
					~args ++ [\outbus, ~out, \out, ~out, \freq, freq, \gate, gate], ~addAction));
		});
		~hasGate.if({
			Func(\schedEventBundleArray).doAction(~lag + length, ~timingOffset, synth.server,
				[synth.setMsg(\gate, ~releaseGate ? 0)], true);
		});
	},

	releaseNote: nil,

	keysToPropagate: #[\instrument, \target, \out, \midi, \mode, \timingOffset]
)) => ProtoEvent(\synthNote);


// event for a melody wrapper
(	play: #{
	var shouldPlay;
	(~debug == true).if({
		thisThread.beats.debug("\nchord trigger event"); currentEnvironment.debug;
	});
	~preAction.value;	// maybe some kind of cleanup?
	~getTopNoteNum.value;
	// nil is legit if there is no topnote pattern
	shouldPlay = if(~topNoteNum.size > 0) {
		~topNoteNum.every({ |num| num.isNumber or: { num.isNil } })
	} {
		~topNoteNum.isNumber or: { ~topNoteNum.isNil }
	};
	if(shouldPlay) {
		(~midi ? false).if({
			~length = inf;	// should not terminate except by midi note
			}, {
				// not needed here but included for subclasses
				// if previous child stream needs to be stopped, do it in ~finish
				~finish.value;
				~length = ~length ? ~delta;
		});
		// start it at the current logical time
		~clock.sched(0, ~makeChildStream.value);
		NotificationCenter.notify(~child, \runChild, currentEnvironment);
	};
},
getTopNoteNum: {
	~topNoteNum = ~top.tryPerform(\at, \freq)
	?? { ~top.tryPerform(\at, \note).tryPerform(\freq) };
},
makeChildStream: {
	var childStreamTemp, childEvent, child = ~child;
	// create a new stream and put it in the environment
	// child stream needs to know the current event, so it's passed to asPattern

	// downward propagation - responsibility of wrapper
	~propagateDownward.value;
	(childEvent = ~child[\event])
	.parent_(ProtoEvent(childEvent[\eventKey]).v)
	// must make absolutely sure the child plays on the same clock
	.clock_(~clock);

	~child.preparePlay;
	childStreamTemp = ~wrapPattern.value(~child.asPattern(currentEnvironment));
	// child stream should run within the child environment
	~child.use({ childStreamTemp = childStreamTemp.asStream });
	~child.put(\eventStream, CleanupStream(childStreamTemp,
		// maybe somebody else needs to know that you stopped?
		// this doesn't handle manual stop, but parent should stop too
		{ NotificationCenter.notify(child, \childStop); }
	));

	~child.put(\eventStreamPlayer, childStreamTemp = EventStreamPlayer(
		~child.eventStream,
		childEvent
	).refresh);
	childStreamTemp	// return new ESP
},
finish: #{
	(~stopChild ? true).if({
		~child.eventStreamPlayer.stop;
	});
},
wrapPattern: #{ |pattern|
	pattern	// simply return pattern, unmodified
},
keysToPropagate: #[\midi]
) => ProtoEvent(\melWrap);


// melody wrapper event that pauses the parent
// the parent must register a notification for the childStop event so that it can resume when the child is done
// this supports wrapping a melody whose length is not known at play start time

ProtoEvent(\melWrap).v.copy.make({
	~finishSuper = ~finish;
	~finish = {
		~finishSuper.value;
		// user must supply eventstreamplayer
		// this breaks the contract vis-a-vis circular references
		~myThread.pause;
	};
}) => ProtoEvent(\melWrapEmbed);


// macrorhythm protoevent
// dur and length should be populated
// note may contain topNote, but it's up to the child process to use it
// how to use inheritance on a protoevent:
ProtoEvent(\melWrap).v.copy.make({
	// should only be called inside the event
	~wrapPattern = #{ |pattern|
		(~length != inf).if({
			pattern = Pfindur(~length-0.01, pattern);
			}, {
				pattern
		});
	};

	~getChord = true;	// default; midi input may override -- used??

	~keysToPropagate = #[\mode, \midi];
}) => ProtoEvent(\macroRh);


// a general-purpose singleSynthPlayer process

PR(\abstractProcess).v.clone({
	~event = (eventKey: \singleSynthPlayer);

	~inChannels = 2;
	~outChannels = 2;
	// you may specify a server at chuck time
	// but DO NOT save a server into a prototype
	// because, when you do PR => BP, the server gets copied
	// and that messes up a whole lot of stuff
	// to avoid that trouble, give the server as a Function
	// because a Function's return value isn't copied
	// this is used for initialization only,
	// and not used if you give a MixerChannel as ~master
	//	~server = { Server.default };

	// synthdef preparation might depend on the mixerchannel and user-defined resources
	// note the order of initialization carefully
	~prep = {
		var	server = ~master.tryPerform(\server) ?? {
			~server.value ?? { Server.default } };
		if(SynthDescLib.global.servers.value.asArray.includes(server).not) {
			SynthDescLib.global.addServer(server);
		};
		if(~chan.isNil) {
			~chan = MixerChannel(~collIndex, server, ~inChannels, ~outChannels,
				outbus: ~master, completionFunc: { |chan|
					~userpreps.do(_.value);
					~userprep.value;		// preparation specified in chuck parameters
					~makeSynthDefs.value;
			});
			~event[\chan] = ~chan;
		} {
			~iMadeChan = false;
			~event[\chan] = ~chan;
			~userpreps.do(_.value);
			~userprep.value;		// preparation specified in chuck parameters
			~makeSynthDefs.value;
		};
	};

	~delta = 1.0;
	// by default, delta and sustain will have the same value
	~sustain = Pkey(\delta);
	~synth = Pfunc({ ~objects.keys.choose });

	// when you clone, if you need extra preparation, do ~userpreps = ~userpreps.copy.add({  ... });
	~userpreps = List.new;

	// chuck-time parameter for user preparation
	~userprep = nil;

	// ~objects is a dictionary
	// key --> dict; outer key is the identifier for the synth that will be used in the synth pattern
	// dict = (def: definition, args: [arg1: Pattern1, arg2: pattern2...])
	// definition is a function, Patch or SynthDef
	~objects = (
		default: (def: \default, args: [
			freq: PdegreeToKey(Pwhite(35, 50, inf), #[0, 2, 4, 5, 7, 9, 11], 12).midicps
		])
	);

	// support code follows
	~makeSynthDefs = {
		~objects = ~objects.value;
		~objects.keysValuesDo({ |id, def|
			def[\synthdef].isNil.if({
				~makeOneSynthDef.(id, def);
			});
		});
	};

	~makeOneSynthDef = { |id, def|
		var	sdef, reference;
		// allow references to other items in ~objects
		// that is, you can reuse a patch but supply a different argument array
		if(def[\def].isSymbol
			and: { (reference = ~objects[def[\def]]).notNil
				and: { reference !== def } }) {
			// has the reference already been prepared?
			reference[\synthdef].isNil.if({
				~makeOneSynthDef.(def[\def], reference);
			});
			def[\synthdef] = reference[\synthdef];
			def[\name] = reference[\name];
			// no need to resend synthdef
		} {
			sdef = ~makeDefForObject.(def[\def], id);
			def[\synthdef] = sdef;
			def[\name] = sdef.tryPerform(\name) ? sdef;
			sdef.tryPerform(\send, ~chan.server);
			sdef.tryPerform(Library.at(\ddwGlobal, \storeMethod));
		};
		~prepareArgsForOneDef.(id);
	};

	~makeDefForObject = #{ |obj, id|
		var return, reference;
		(return = ~classActions[obj.class.name]).notNil.if({
			return = return.envirGet.(obj, id)
		});
		return ?? { obj }	// if nothing, return the object itself
	};
	~makeFnDef = #{ |fn|
		fn.asSynthDef(outClass: (~isFx ? false).if({ \ReplaceOut }, { \Out }));
	};
	~makePatchDef = #{ |patch, id|
		var	sdef; // = patch.asSynthDef;
		sdef = patch.asSynthDef;
		// I saw a scenario where InstrSynthDef did not come up with a unique name based on args
		// dictionary id must be unique to this BP
		// might have the same ID with different args in different BPs, so add the BP's ID
		sdef.name = ~collIndex ++ id;
		sdef
	};
	~makeSynthDef = #{ |def| def };
	~classActions = IdentityDictionary[
		'SynthDef' -> \makeSynthDef,
		'Function' -> \makeFnDef,
		'Patch' -> \makePatchDef,
		'WrapPatch' -> \makePatchDef,
		'FxPatch' -> \makePatchDef
	];

	~prepareArgsForOneDef = { |id|
		var	streamarray = Array.new(~objects[id][\args].size), fullname;
		~objects[id][\args].pairsDo({ |name, pattern|
			// assigning a Pattern to a BP environment variable automatically creates the stream
			fullname = (id ++ name).asSymbol;
			fullname.envirPut(pattern.asPattern);
			streamarray.add(name).add(BPStream(fullname).asStream);
		});
		~objects[id][\argStreams] = streamarray;
	};

	~reset = {
		~objects.keysValuesDo { |id, obj|
			obj[\args].pairsDo { |name, pattern|
				// this line is one of the dumbest things I ever wrote
				// (id ++ name).asSymbol.envirPut(pattern.asPattern);
				~makeStreamForKey.((id ++ name).asSymbol);
			}
		}
	};

	~basePattern = {
		(~debug ? false).if(DebugPbind, Pbind)
		.new(
			\synthKey, BPStream(\synth),
			\delta, BPStream(\delta),
			\sustain, BPStream(\sustain),
			\instrument, Pfunc({ |ev|
				(ev[\synthKey] == \rest).if({ \rest },
					{ ~objects[ev[\synthKey]].tryPerform(\at, \name) ? \rest })
			}),
			\isFx, Pfunc { ~isFx ? false } //,
			//			\chan, ~chan
		)
	};

	~asPattern = {
		~basePattern.value.collect({ |ev|
			(ev[\synthKey] != \rest).if({
				~getUserArgs.(ev);
			}, { ev });
		});
	};

	~getUserArgs = { |event|
		var key = event[\synthKey], streamout;
		block { |break|
			~objects[key][\argStreams].pairsDo({ |name, stream|
				(streamout = stream.next(event)).isNil.if({
					if(~debug ? false) {
						"% stream was nil, stopping".format(name.asCompileString).warn;
					};
					break.(nil);	// early exit with nil result to stop main stream
					}, {
						(~debug ? false).if({
							[name, streamout].debug("user arg");
						});
						(name.isSequenceableCollection).if({
							name.do({ |n, i| event[n] = streamout[i] })
							}, {
								event[name] = streamout;
						});
				});
			});
			if(~debug ? false) { event.debug("result event") };
			event
		}
	};

	// when you clone, if you need extra preparation, do ~userfrees = ~userfrees.copy.add({  ... });
	~userfrees = List.new;

	// chuck-time parameter for user preparation
	~userfree = nil;

	~freeCleanup = {
		if(~iMadeChan ? true) { ~chan.free };
		// should free patches
		~objects.do({ |def|
			try {
				def[\def].isKindOf(AbstractPlayer).if({ def[\def].free });
			};
		});
		~userfree.value;
		~userfrees.do(_.value);
	};

}) => PR(\basicSynthChooser);
