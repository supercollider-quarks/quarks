
Voicer {		// collect and manage voicer nodes
			// H. James Harkins -- jamshark70@dewdrop-world.net

// TEMPORARY, experimental
	classvar <>parallelSynthGroup = false;

	var	<nodes,
		<voices,		// maximum number of voices
		<target, <>addAction,
		<bus,
		<>stealer = \preferEarly,	// a symbol, method name for node locator
			// must be one of: cycle, strictCycle, random, preferEarly, preferLate
		cycleRout, strictCycRout,	// see cycle and strictCycle

		<globalControls,		// so all nodes can share a bus for a given control
							// an IDict of VoicerGlobalControls
		<proxy,				// my proxy for voicer processes

		<>latency = nil,	// node latency; can be overridden at trigger time

		<>clock;

	var	<susPedalNodes, <susPedal = false;

// TEMPORARY, experimental
	var iMadeTarget = false;

	*new { arg voices = 1, things, args, bus, target, addAction = \addToTail;
			// things can be a single thing or an Array of things
			// args can be an array of pairs [name, value, name, value...] or array of such arrays
		^super.new.init(voices, things, args, bus, target, addAction);
	}

	init { arg v, th, ar, b, targ, addAct;
		var args, lcm;		// for initializing nodes

		globalControls = IdentityDictionary.new;

		target = targ.asTarget;
		if(parallelSynthGroup) {
			if(targ.isKindOfByName('MixerChannel')) {
				// I need the object now
				target = ParGroup.basicNew(targ.server, targ.server.nodeAllocator.allocPerm);
				targ.doWhenReady {
					// but can't add immediately
					targ.server.sendMsg(*(target.newMsg(targ.synthgroup)));
					iMadeTarget = true;
				}
			} {
				target = ParGroup(target);
				iMadeTarget = true;
			};
		};

		NodeWatcher.newFrom(target.server);	// voicernodes need to watch synths on server

			// using groupBusInfo might seem like a long way around,
			// but it also checks that the target is a Mixer (without isKindOf)
			// thus, the bus argument is overridden ONLY for mixers
		(args = targ.tryPerform(\groupBusInfo)).notNil.if({
			bus = args[1];
		}, {
			bus = b ? Bus.new(\audio, 0, 1, target.server);
		});

		addAction = addAct;

		voices = (v ? 1).max(1);		// must have at least one node
			// convert initial args to array of arrays
		ar.isNil.if({ ar = [] });
			// if first element is array, you have [[],[]...]
		(ar.at(0).size > 0).if({ args = ar }, { args = [ar] });
			// same thing for patches
		(th.size == 0 or: { th.isString }).if({ th = [th] });

			// create nodes: loop thru things
		nodes = Array.new(voices);	// must add the nodes incrementally
		lcm = th.size.lcm(args.size);
		voices.do({ |i|
			nodes = nodes.add(this.makeNode(th.wrapAt(i), args.wrapAt(i),
					// i < th.size.lcm(args.size) : patches will become superfluous
					// after least common multiple of # of instrs and # of arg sets
				(i < lcm).if({ nil }, // nil=not superfluous, make patch
						// else, wrap around and get defname to reuse
					{ nodes[i % lcm].defname })
			));
		});

		susPedalNodes = IdentitySet.new;
	}

	makeNode { arg thing, args, defname;
			// strings/symbols: treat as defname
		case
			{ thing.isString or: { thing.isSymbol } } {
				^SynthVoicerNode.new(thing, args, bus, target, addAction, this, defname);
			}

			{ thing.isKindOf(Instr) or: { thing.class.name == 'WrapInstr' } } {
				^InstrVoicerNode.new(thing, args, bus, target, addAction, this, defname);
			}

				// default branch, error
			{ Error("%: Invalid object to use as instrument. Can't build voicer.".format(thing)).throw }
	}

// SUPPORT METHODS FOR NODE LOCATORS:
	nonplaying {	// returns all nonplaying nodes, (or if none, an array containing earliest node)
		var n;
		n = nodes.select({ arg n; n.reserved.not });
		(n.size > 0).if({ ^n }, { ^[ this.earliest ] });
	}

	playingNodes {
		^nodes.select(_.isPlaying)
	}

	earliest {	// earliest triggered node
		^nodes.copy.sort({ arg a, b; a.lastTrigger < b.lastTrigger }).at(0)
	}

	latest {
		^nodes.copy.sort({ arg a, b; a.lastTrigger > b.lastTrigger }).at(0)
	}

		// find earliest active node with this frequency
	firstNodeFreq { arg freq;
		var	nodesTemp;
		nodesTemp = nodes.select({ |n|
			(n.frequency == freq) and: { n.isPlaying and: { n.isReleasing.not } }
		});
			// must not consider pedal-sustaining nodes
		(susPedalNodes.size > 0).if({
			nodesTemp = (IdentitySet(nodesTemp.size).addAll(nodesTemp).removeAll(susPedalNodes))
				.asArray;
		});
		^nodesTemp.sort({ |a, b| a.lastTrigger < b.lastTrigger }).at(0)
	}

		// this method is reserved for Event usage
		// you should not use it yourself - instead use trigger, release and gate methods
	prGetNodes { |numNodes|
		var	node;
		^Array.fill(numNodes, {
				// must set reserved = true
				// so that the next node request doesn't return the same
			node = this.perform(stealer).reserved_(true);
		});
	}


// NODE LOCATORS:
// to choose one, do yourVoicer.stealer_( a symbol == the method name )
	strictCycle {
			// always returns next item in nodes, whether playing or not
		strictCycRout.isNil.if({
			strictCycRout = Routine.new({
				nodes.do({ arg n; n.yield });
			});
		});
		^strictCycRout.next
	}

	cycle {
			// returns next non-playing item in nodes
			// if all nodes playing, returns earliest triggered
		cycleRout.isNil.if({
			cycleRout = Routine.new({
				loop {
					nodes.do({ arg n;
							// if nonplaying returns a playing node, then all nodes are playing
						(this.nonplaying.at(0).reserved).if({
							this.earliest.yield
						}, {
							n.reserved.not.if({ n.yield })
						});
					});
				}
			});
		});
		^cycleRout.next
	}

	random {
			// returns a random non-playing node
		var n;
		n = this.nonplaying;
			// if 1 or more nodes are not playing, return one of them
		(n.at(0).reserved.not).if({ ^n.choose },
			{ ^this.earliest }		// otherwise, give earliest triggered node
		);
	}

	preferEarly {
			// find first non-playing node -- THE DEFAULT METHOD
		^this.nonplaying.sort({ arg a, b; a.lastTrigger < b.lastTrigger }).at(0)
	}

	preferLate {
			// find last non-playing node
		^this.nonplaying.sort({ arg a, b; a.lastTrigger > b.lastTrigger }).at(0)
	}

// PLAYING/RELEASING METHODS:
// trigger plays, release kills a node by frequency, gate starts and schedules the release
		// lat -1 means use value defined in the voicer
	trigger1 { arg freq, gate = 1, args, lat = -1;
		var node;
			// freq may be a symbol to produce a rest
		freq.isValidVoicerArg.if({
			node = this.perform(stealer);
				// args may be [\key, value] or [[\key, value], [\key, value]]
				// in the latter case, trigger1 should take only the first subarray
			node.trigger(freq, gate,
				(args.size > 0 and: { args[0].respondsTo(\wrapAt) }).if(
					{ args[0] }, { args }), (lat ? 0).isNegative.if({ latency }, { lat }));
			^node	// give node back to user
		}, {
			^nil
		});
	}

		// trigger one or many
	trigger { arg freq, gate = 1, args, lat = -1;
		var bundle, node, nodecoll;
		(lat ? 0).isNegative.if({ lat = latency });
		(freq.size > 0).if({
				// if many freqs, convert args to array of arrays if it's not already
			args.isNil.if({ args = [] });
			args.at(0).respondsTo(\wrapAt).not.if({ args = [args] });
				// same for gates - otherwise, an array for gates will cause stuck nodes
			gate.isNil.if({ gate = [1] });
			gate.respondsTo(\wrapAt).not.if({ gate = [gate] });
				// for each freq, get node and play it
			^freq.collect({ arg f, i;
				f.isValidVoicerArg.if({
					this.perform(stealer).trigger(f, gate.wrapAt(i), args.wrapAt(i), lat);
				});
			});
		}, {
			^this.trigger1(freq, gate, args, lat);
		});
	}

	gate1 { arg freq, dur, gate = 1, args, lat = -1;
			// play & schedule release for 1 note
		var node, synth;
		(lat ? 0).isNegative.if({ lat = latency });
		node = this.trigger1(freq, gate, args, lat);
		synth = node.synth;
		(clock ? thisThread.clock).sched(dur, {
			node.release(0, lat, freq)
		});
		^node
	}

		// gate one or many
	gate { arg freq, dur, gate = 1, args, lat = -1;
		var nodecoll;
		(lat ? 0).isNegative.if({ lat = latency });
		(freq.size > 0).if({
			nodecoll = this.trigger(freq, gate ? 1, args, lat);  // play them
				// if single dur, convert to array
			(dur.size > 0).not.if({ dur = [dur] });
			nodecoll.do({		// schedule releases
				arg node, i;
				(clock ? thisThread.clock).sched(dur.wrapAt(i), {
					node.release(0, lat, freq.wrapAt(i))
				});
			});
			^nodecoll
		}, {
			^this.gate1(freq, dur, gate, args, lat)
		});
	}

		// release a specific VoicerNode object
		// especially useful in Events
	releaseNode { |node, freq, releaseGate = 0, lat = -1|
		susPedal.if({
			susPedalNodes.add(node);
		}, {
			node.release(releaseGate, (lat ? 0).isNegative.if({ latency }, { lat }), freq);
		});
	}

	release1 { arg freq, lat = -1;
		var node;
		(node = this.firstNodeFreq(freq)).notNil.if({
			this.releaseNode(node, freq, 0, lat);
		});
	}

	release { arg freq, lat = -1;
		var node, nodecoll;
		(freq.size > 0).not.if({
			this.release1(freq, lat)
		}, {
			freq.do({ arg f; this.release1(f, lat) });
		});
	}

	releaseAll { |lat|
		nodes.do({ arg n; n.release(latency: lat) });
		susPedalNodes = IdentitySet.new;
	}

// suspednodes?
	releaseNow1 { arg freq, sec;
		^this.firstNodeFreq(freq).releaseNow(sec);
	}

	releaseNow { arg freq, sec;
		var node;
		(freq.size > 0).not.if({
			^this.releaseNow1(freq, sec)
		}, {
			^freq.collect({ arg f; this.release1(f, sec) });
		});
	}

// CONVENIENCE: Apply methods to many nodes
	set { arg args, lat;
		var bus, ar;	// bus holder used in loops, argument sub-collection

		(lat ? 0).isNegative.if({ lat = latency });

		args = args.clump(2);	// group in pairs

			// do global-mapped controls
			// if globalControls dict returns non-nil for this name, then it's mapped
		ar = args.select({ arg a; globalControls.at(a.at(0).asSymbol).notNil });

			// set the buses to the associated values
		ar.do({ arg a; globalControls.at(a.at(0).asSymbol).set(a.at(1), true, lat); });

			// now do non-global controls
		ar = args.reject({ arg a; globalControls.at(a.at(0).asSymbol).notNil }).flatten(1);

			// must do this way b/c nodes may be of different types
			// node's responsibility to check if it's loaded
		nodes.do({ arg n; n.set(ar, lat); });
	}

		// apply to initArgs within nodes
		// does not affect currently playing nodes, only new ones
	setArgDefaults { arg args;
		nodes.do({ |n| n.setArgDefaults(args); });
	}

	setArgsInEvent { |event|
		var	build = {
			var synthDesc, argList, controls, cname, value,
					// why? I want VoicerNode's initargs to override parent event defaults
					// but keys set in the local event should take precedence
				eventWithoutParent = currentEnvironment.copy.parent_(nil),
				argsDict = IdentityDictionary.new;
			~args = ~args.asArray;
			~args.tryPerform(\pairsDo) { |key, value|
				argsDict.put(key, value);
			};
			~args = ~nodes.collect({ |node, i|
					// if synthdesc is available
					// we can't assume the same synthdesc for every node
				(synthDesc = node.getSynthDesc(~synthLib)).notNil.if({
					controls = synthDesc.controls.select({ |c|
						c.rate != \noncontrol and:
							{ #[freq, gate, out, i_out, outbus].includes(c.name.asSymbol).not }
					});
					argList = Array(controls.size * 2);
					controls.do({ |c|
						cname = c.name.asSymbol;
						if(cname != '?') {
							(eventWithoutParent[cname].size == 0).if({
								eventWithoutParent[cname] = eventWithoutParent[cname].asArray;
							});
							value = eventWithoutParent[cname].wrapAt(i)
								?? { argsDict[cname] }
								?? { node.initArgAt(cname) };
								// add value: environment overrides node's initarg,
								// which overrides the SynthDef's default
								// used to add synthdef default explicitly (c.defaultValue)
								// but that breaks t_gate, so now adding only values
								// that exist in the event or have Voicer-specific defaults
							if(value.notNil) {
								argList.add(cname).add(value)
							};
						};
					});
					argList
				}, {
					argList = Array(~argKeys.size * 2);
					~argKeys.do({ |c|
						c = c.asSymbol;
						if(c.envirGet.isNil) { c.envirPut(argsDict[c]) };
						if(c.envirGet.size == 0) { c.envirPut(c.envirGet.asArray) };
						argList.add(c).add(c.envirGet.wrapAt(i));
					});
					argList
				});
			});
		};
		(event !== currentEnvironment).if({
			event.use(build)
		}, {
			build.value
		});
		^event
	}

	target_ { |targ|
		var	groupbus;
			// check for mixerchannel
		(groupbus = targ.tryPerform(\groupBusInfo)).notNil.if({
			#target, bus = groupbus;
		}, {
			target = targ.asTarget;	// if not MC, then use current bus
		});
			// must propagate to all the nodes
		nodes.do({ |n|
			n.target = target;
			n.bus = bus;
		})
	}

	sustainPedal { |sustain|
		susPedal = sustain ?? { susPedal.not };
			// do I need to fix the array here?
		susPedal.not.if({
			susPedalNodes.do({ |n| n.release(0) });  // is that right?
			susPedalNodes = IdentitySet.new;
		});
	}

	mapGlobal { arg name, bus, value, spec, allowGUI = true; // maps name to a kr bus in every node
		var	gc;
		if(globalControls[name].isNil) {
			globalControls.put(name, gc = VoicerGlobalControl.new(name, bus, this,
				value ? 0, spec, allowGUI));
			^gc		// so user can reference this gc directly
		} {
			^globalControls[name]
		}
	}

	unmapGlobal { arg name;
		var gc;
		name = name.asSymbol;
		gc = globalControls.at(name);
		gc.notNil.if({		// make sure there's something to remove
			globalControls.removeAt(name);
			gc.free;
		});
	}

	maxControlNum {	// for indexing in VoicerGlobalControl
		^globalControls.collect({ |gc, key| gc.voicerIndex }).maxItem ? 0
	}

	globalControlsByCreation {
		^globalControls.values.select({ |gc| gc.allowGUI })
			.asArray.sort({ |a,b| a.voicerIndex < b.voicerIndex })
	}

	proxify {
		proxy.isNil.if({
			proxy = VoicerProxy.new(this);
		});
		^proxy
	}

	addProcess { arg states, type;
		this.proxify;
		^proxy.addProcess(states, type);
	}

	removeProcess { arg p;
		^proxy.tryPerform(\removeProcess, p)
	}

	removeProcessAt { arg i;
		^proxy.tryPerform(\removeProcessAt, i)
	}

	processes { ^proxy.tryPerform(\processes) }

		// if the voicer's target is a MC, assign it to the gui
	draggedIntoMixerGUI { |gui|
		var	mc;
		(mc = bus.tryPerform(\asMixer)).notNil.if({
			gui.mixer_(mc);
			gui.refresh;
		});
	}

// BOOKKEEPING:
	free {
			// activates onClose which frees the gui
		nodes.do(_.dtor);	// clean up stuff
		proxy.notNil.if({ proxy.modelWasFreed; });	// deactivate proxy
		globalControls.do({ arg gc; gc.free });
		globalControls = IdentityDictionary.new;
		voices = nil;
		if(iMadeTarget) {
			target.server.nodeAllocator.freePerm(target.nodeID);
			target.server.sendBundle(nil,
				#[error, -1],
				target.freeMsg,
				#[error, -2]
			);
		};
			// asClass b/c you might not have MIDI Suite installed
			// nil.update is OK (no-op)
		'MIDIPort'.asClass.update;	// clears VoicerMIDISocket associated with me
						// if socket is pointing to proxy, the socket will stay put
	}

	active { ^voices.notNil }

	run { arg bool = true;
		var mixer;
		(mixer = this.asMixer).notNil.if({
			mixer.run(bool);
		}, {
			target.run(bool).isRunning_(bool);
		});
	}

	isRunning {
		var mixer;
		^(mixer = this.asMixer).notNil.if({
			mixer.isRunning;
		}, {
			target.tryPerform(\isRunning) ? true;
		});
	}

	asMixer { ^bus.asMixer }

	panic {		// free all nodes
		nodes.do({ arg n; n.releaseNow });
	}

	cleanup {		// free non-playing nodes; kind of superfluous now
		this.nonplaying.do({ arg n; n.free });
	}

	steal_ { |bool = true|
		nodes.do(_.steal = bool);
	}

		// trace all playing nodes
		// no need to check here b/c VoicerNode tests isPlaying before issuing n_trace
	trace {
		nodes.do({ |node| node.trace });
	}

// GUI support
	guiClass { ^VoicerGUI }

	asString {
		if(nodes.size == 0) {
			^"Voicer : failed to init"
		} {
			^("Voicer : " ++ nodes.at(0).displayName)
		}
	}

	editor { proxy.isNil.if({ ^nil }, { ^proxy.editor }) }

	draggedIntoVoicerGUI { arg dest;		// drag a voicer into a gui switches the gui to this vcr
		var oldProxy;
		oldProxy = proxy;		// must clear from old gui if there was one
		dest.model.voicer_(this);	// set new gui's proxy to this voicer
		oldProxy.notNil.if({ oldProxy.voicer_(nil) });	// clear old proxy
	}

	proxy_ { arg pr;	// set my proxy and fix my gc's proxies
		pr.isNil.if({ proxy.clearControlProxies });
		proxy = pr;
		proxy.notNil.if({ proxy.switchControlProxies });
	}

// chucklib support
	bindClassName { ^Voicer }

// drop in some events to use with voicers
	*initClass {
		StartUp.add {
			Event.parentEvents.put(\voicerMIDI, (args: [],

					// maybe you want to use non-equal-temperament. write it here
				midiNoteToFreq: #{ |notenum|
					notenum.midicps
				},

				prepNote: #{
					var i;
					~freq = ~freq ?? { ~note.freq };
					(~midi ? true).if({ ~freq = ~midiNoteToFreq.value(~freq).asArray },
						{ ~freq = ~freq.asArray });
					~dur = ~dur ?? { ~delta ?? { ~note.dur } };
					~length = (~length ? ~note.length).asArray;
						// some patterns (e.g. Pfindur) might shorten the delta
						// in which case length could be too long
						// but this really applies only to MonoPortaVoicers,
						// hence the adjust... test
					if(~adjustLengthToRealDelta.value and: { ~dur != currentEnvironment.delta }) {
						~length = ~length * currentEnvironment.delta / ~dur;
					};
					~args = ~args ? ~note.args;
					~gate = (~gate ?? {
							// identify the \gate, xxx pair in the args array
							// 2nd removeAt should return the value *wink*
						(i = ~args.detectIndex({ |item| item == \gate })).notNil
							.if({ ~args.removeAt(i); ~args.removeAt(i); }, { 0.5 });
					}).asArray;

					~nodes = ~voicer.prGetNodes(max(~freq.size, max(~length.size, ~gate.size)));
					~voicer.setArgsInEvent(currentEnvironment);
				},

				play: #{
					var	lag, timingOffset = ~timingOffset ? 0, releaseGate,
						voicer = ~voicer;
					voicer.notNil.if({
						lag = ~latency;
						~prepNote.value;
						~finish.value;	// user-definable
						releaseGate = (~releaseGate ? 0).asArray;

						~nodes.do({ |node, i|
							var	freq = ~freq.wrapAt(i), length = ~length.wrapAt(i);
							~schedBundleArray.(~lag ? 0, timingOffset,
								node.server,
								node.server.makeBundle(false, {
									node.trigger(freq, ~gate.wrapAt(i), ~args.wrapAt(i));
								})
							);
							(length.notNil and: { length != inf }).if({
								thisThread.clock.sched(length + timingOffset, {
									voicer.releaseNode(node, freq, releaseGate.wrapAt(i),
										node.server.latency.notNil.if({ lag + node.server.latency }));
								});
							});
						});
					});
					~delta ?? { ~delta = ~dur };
				},
					// you could override this
				adjustLengthToRealDelta: { ~voicer.isKindOfByName(\MonoPortaVoicer) }
			));

			Event.addEventType(\voicerNote, #{|server|
				var	lag, strum, sustain, i, timingOffset = ~timingOffset ? 0, releaseGate,
					voicer = ~voicer;

				~freq = (~freq.value + ~detune).asArray;

				if (~freq.isSymbol.not) {
					~amp = ~amp.value.asArray;
					lag = ~lag;
					strum = ~strum;
					sustain = ~sustain = ~sustain.value.asArray;
						// for mono voicers, adjust sustain if note's delta is altered
					if(voicer.isKindOfByName(\MonoPortaVoicer)
					and: { ~dur != currentEnvironment.delta }) {
						~sustain = max(0.01, ~sustain * currentEnvironment.delta / ~dur);
					};

					~gate = (~gate ?? {
							// identify the \gate, xxx pair in the args array
							// 2nd removeAt should return the value *wink*
						(i = ~args.detectIndex({ |item| item == \gate })).notNil
							.if({ ~args.removeAt(i); ~args.removeAt(i); }, { 0.5 });
					}).asArray;
					releaseGate = (~releaseGate ? 0).asArray;

					~nodes = voicer.prGetNodes(max(~freq.size, max(~sustain.size, ~gate.size)));
					voicer.setArgsInEvent(currentEnvironment);

					~nodes.do({ |node, i|
						var latency, freq, length;

						latency = i * strum + lag;
							// backward compatibility: I should NOT add server latency
							// for newer versions with Julian's schedbundle method
						if(~addServerLatencyToLag ? false) {
							latency = latency + (node.server.latency ? 0)
						};
						freq = ~freq.wrapAt(i);
						length = ~sustain.wrapAt(i);

						~schedBundleArray.(latency, ~timingOffset,
							node.server,
							node.server.makeBundle(false, {
								node.trigger(freq, ~gate.wrapAt(i), ~args.wrapAt(i));
							})
						);
						(length.notNil and: { length != inf }).if({
							thisThread.clock.sched(length + timingOffset, {
								voicer.releaseNode(node, freq, releaseGate.wrapAt(i),
									node.server.latency.notNil.if({ lag + node.server.latency }));
							});
						});
					});
				};
			});
		}
	}
}

MonoPortaVoicer : Voicer {

	var	<>portaTime = 0,	// portamento time
		<lastFreqs;		// last triggered frequencies (for portamento)

	init { arg v, th, ar, b, targ, addAct, preAlloc;
		var args;		// for initializing nodes

		globalControls = IdentityDictionary.new;
		lastFreqs = List.new;

		target = targ.asTarget;
		NodeWatcher.newFrom(target.server);	// voicernodes need to watch synths on server

		(args = targ.tryPerform(\groupBusInfo)).notNil.if({
			bus = args[1];
		}, {
			bus = b ? Bus.new(\audio, 0, 1, target.server);
		});

		addAction = addAct;

		voices = 1;		// may have only one node for a mono voicer
			// convert initial args to array of arrays
		ar.isNil.if({ ar = [] });
			// if first element is array, you have [[],[]...]
		(ar.at(0).size > 0).if({ args = ar }, { args = [ar] });
			// create nodes: loop thru things
		(th.size > 0 and: { th.isString.not }).if({
			nodes = Array.fill(voices, { arg i;
				this.makeNode(th.wrapAt(i), args.wrapAt(i), preAlloc, i);
			})
		}, {
			nodes = Array.fill(voices, {arg i;
				this.makeNode(th, args.wrapAt(i), preAlloc);
			})
		});
	}

	makeNode { arg thing, args, preAlloc;
			// strings/symbols: treat as defname
		case
			{ thing.isString or: { thing.isSymbol } } {
				^MonoPortaSynthVoicerNode.new(thing, args, bus, target, addAction, this);
			}

			{ thing.isKindOf(Instr) or: { thing.class.name == 'WrapInstr' } } {
				^MonoPortaInstrVoicerNode.new(thing, args, bus, target, addAction, this);
			}

				// default branch, error
			{ Error("%: Invalid object to use as instrument. Can't build voicer.".format(thing)).throw }
	}

	releaseNode { |node, freq, releaseGate = 0, lat = -1|
		(lat ? 0).isNegative.if({ lat = latency });
		lastFreqs.remove(freq);
		(lastFreqs.size > 0
		and: { thisThread.seconds > node.lastTrigger }).if({
			if(node.frequency != lastFreqs.last) {
				nodes.at(0).set([\freq, lastFreqs.last], lat);
				nodes.at(0).frequency = lastFreqs.last;
			};
			^nodes.at(0)
		}, {
			^this.firstNodeFreq(freq).release(releaseGate, lat)
		});
	}

	release1 { arg freq, lat = -1;
			// because this is MonoPortaVoicer, with only one node,
			// dispatch to releaseNode directly
		this.releaseNode(nodes.first, freq, 0, lat);
	}

	gate1 { arg freq, dur, gate = 1, args, lat = -1;
			// play & schedule release for 1 note
		var node;
		(lat ? 0).isNegative.if({ lat = latency });
		node = this.trigger1(freq, gate, args, lat);
		(clock ? thisThread.clock).sched(dur, { this.release(freq, lat) });
		^node
	}

		// gate one or many
	gate { arg freq, dur, gate = 1, args, lat = -1;
		var node, nodecoll;
		(lat ? 0).isNegative.if({ lat = latency });
		(freq.size > 0).if({
			nodecoll = this.trigger(freq, gate ? 1, args, lat);  // play them
				// if single dur, convert to array
			(clock ? thisThread.clock).sched(dur, { this.release(freq, lat) });
			^nodecoll
		}, {
			^this.gate1(freq, dur, gate, args, lat)
		});
	}

	panic {	// panic button needs to clear lastFreqs list in additiion to other activities
		super.panic;
		lastFreqs = List.new;
	}

}


// this may not be required since node stealing has been fixed

VoicerNoGate : Voicer {	// just like Voicer, except synthdefs should use fixed-length envelopes

		// since env is fixed-length, no need to schedule releases
		// but, we do need to set the release flag in the node so
		// the node can be reused without stealing
	gate1 { arg freq, dur, gate = 1, args, lat = -1;
			// play & schedule release for 1 note
		var node;
		node = this.trigger1(freq, gate, args, (lat ? 0).isNegative.if({ latency }, { lat }));
		node.isPlaying = false;
		^node
	}

		// gate one or many
	gate { arg freq, dur, gate = 1, args, lat = -1;
		var node, nodecoll;
		(lat ? 0).isNegative.if({ lat = latency });
		(freq.size > 0).if({
			nodecoll = this.trigger(freq, gate ? 1, args, lat);  // play them
			^nodecoll.do({ |node| node.isPlaying = false });
		}, {
			^this.gate1(freq, dur, gate, args, lat)
		});
	}

}
