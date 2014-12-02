
MixerChannel {
	// interface class for basic level and panning faders
	// each instance stores numchannels, level and panning info,
	// and pointers to the Synth object on the server

	// this class uses my class BusDict to store names of active buses!
	// H. James Harkins - jamshark70@dewdrop-world.net

	classvar	<servers;	// collection of active servers. see *new for why
					// dictionary of servers -> dictionary of bus numbers & mixerchannels
					// needed for autorepatching -- order of execution issues

	var	<name,			// mnemonic string, provided at creation
		<def,			// MixerChannelDef
		<server,			// the server
		<inbus, <outbus,	// buses: multiple MixerChannels can go to the same
						// outbus; these are not optional

// DO NOT CHANGE THE ORDER OF THE ABOVE INSTANCE VARIABLES! Below, changes are fine.

		<synth,				// synth object on server
		<fadergroup,			// group for faders/sends; contains synthgroup at head
		<effectgroup,
		<synthgroup,			// group for things played on this channel

		<controls,			// GenericGlobalControls for each parm defined in the MCDef
//		<autoSynths,			// for synth-based automation
		<lineRoutines,		// for lineTo methods

		<>preSends, <>postSends,	// keep track of sends for gui

		<mcgui,					// set when MixerChannelGUI is created
		<>guiUpdateTime = 0.25,		// during LineTo, update every x sec's

		patchesPlayed,		// so that free can tell crucial players to .stop
		<recProxy,		// for recording
		<>scopeSupport,	// MixerScope object
		<>bundled = 0,	// a state var needed for MixerChannelReconstructor
						// 0 == server objects not bundled yet
						// 1 == server objects msgs bundled but not sent
						// 2 == server objects msgs sent, ready to go
						// -1 == freed
		<>midiControl,
		<numMixersInChain = 1,	// used for order-of-execution calculation
							// sum of numMixersInChain of all outputs + 1
		<muted = false,
		<>muteControlName = \level,

			// mixers that have a direct link to this one
			// e.g., if A --> B, B is a descendent of A and A is an antecedent of B
			// A --> B --> C, A is antecedent of B but NOT of C
		<antecedents, <descendents;

///////////////////// CREATION AND INITIALIZATION /////////////////////

	*new { arg 	name = nil, server, inChannels = 1, outChannels = 2,
				level = 0.75, pan = 0, /*postSendReady = false,*/ inbus, outbus,
				completionFunc;

		^this.newFromDef(name, "mix%x%".format(inChannels, outChannels).asSymbol,
			server, (level:level, pan:pan), /*postSendReady,*/ inbus, outbus, completionFunc);
	}

	*newFromDef {	|name, defname, server, initValues, /*postSendReady = false,*/
			inbus, outbus, completionFunc|
		var new;
		server = server ? Server.default;

			// if this is the first mixerchannel on this server,
			// load all mixer and automation synthdefs
		servers.keys.includes(server).not.if({
			servers.put(server, IdentityDictionary.new);
			server.addDependant(MixerChannel);
		});

		new = super.newCopyArgs(name, MixerChannelDef.at(defname), server, inbus, nil)
			.init(outbus, initValues, completionFunc);
		this.changed(\newMixer, new);
		^new
	}

	*freeAllOnServer { |server|
		servers[server].copy.do(_.free)
	}

	*freeAll { |server|
		server.notNil.if({
			this.freeAllOnServer(server)
		}, {
			servers.keysDo({ |key| this.freeAllOnServer(key) });
		});
	}

	init { |bus, initValues, completionFunc|
		server.serverRunning.not.if({
			Error("Server must be booted before creating MixerChannels.").throw;
		}, {
			this.fixParms(bus, initValues, completionFunc)
		});
	}

	fixParms { |bus, initValues, completionFunc|	// make all parameters consistent; allows for
				// missing arguments
				// also adds mixer synth object to server in a new group
				// default outbus is standard output

		var	argOutbus, mctemp;

		antecedents = IdentitySet.new;
		descendents = IdentitySet.new;

		argOutbus = outbus;	// save to determine if order-of-execution will have to be
							// adjusted

		preSends = Array.new;	// initialize these arrays
		postSends = Array.new;
		patchesPlayed = Array.new;

		inbus.isNil.if({	// if in/out buses are nil, create them
			inbus = BusDict.audio(server,
				 max(def.outChannels, def.inChannels), name ++ " in");
		});
		bus.isNil.if({
			bus = Bus.new(\audio, 0, def.outChannels, server);
		});

			// allow bus number as argument
			// note, if you supply a number for an
			// unallocated bus, you might not get an
			// error but your mixerchannel won't work
		inbus = inbus.isKindOf(Bus).if(
			{ inbus },	// if it's already a bus, leave it alone
			{ Bus.new(\audio, inbus,
				max(def.outChannels, def.inChannels), server) }
		);

		inbus = SharedBus.newFrom(inbus, this);	// so Patch.free won't take away my bus

		bus.isMixerChannel.if({
			bus = bus.inbus;		// route output of this to input of dest mc
		});

		bus.isNumber.if(
			{ bus = Bus.new(\audio, bus, def.outChannels, server);
			  argOutbus = bus }		// integer will not be ok for order-of-exec test below
		);

		(mctemp = bus.asMixer).notNil.if({
			numMixersInChain = numMixersInChain + mctemp.numMixersInChain;
		});

		name = (name.isNil).if({  // if name not given, lookup in BusDict
			name = BusDict.audioNames.at(server).at(inbus.index);
			(name == "" || name.isNil).if({	// still none,
				name = "Mixer " ++ inbus.index;	// name after bus number
			})
		}, { name });

		lineRoutines = IdentityDictionary.new;
//		autoSynths = IdentityDictionary.new;

		controls = def.makeControlArray(this);
		this.setControls(initValues);

		MixerChannelReconstructor.add(this);
		servers.at(server).put(inbus.index, this);	// save for repatching purposes
		this.makeServerObjects(bus, completionFunc);

		MixingBoard.refresh;  // list of available in/out buses has changed

		^this
	}

		// this technique is used throughout the MixerChannel hierarchy
		// on cmd-., it's necessary to collect all the messages to recreate the server objects
		// into a big-ass bundle so that all messages execute in sequence
		// therefore, the bundle-creation is split out from the sending-to-server
	makeServerObjects { |bus, completionFunc|
		var bundle;
		bundle = List.new;
		this.makeServerObjectsToBundle(bundle, bus);
		MixerChannelReconstructor.queueBundle(server, bundle,
			(chan: this, func: completionFunc, env: currentEnvironment));
	}

	makeServerObjectsToBundle { arg bundle, bus;
		var	id;
			// place the creation messages into a bundle
			// make groups and drop synth onto server
			// since fadergroup contains synthgroup, order of execution of mixerchannels
			// can be changed by moving fadergroup
		(bundled == 0).if({	// only do this if it hasn't been done before
				// if these groups already exist, they might be targets
				// for other objects; therefore they must keep the same nodeID
			// either the groups already exist, in which case they might be targets
			// so don't create new objects
			// BUT... this is probably b/c of cmd-.
			// in which case the server's node allocator has been killed
			// so we need to reserve node IDs for them
			// does that seem roundabout? perhaps so, but "in object style,
			// the objects abstract away the IDs so they can change at any time"
			if(fadergroup.isNil) {
				fadergroup = Group.basicNew(server,
					server.nodeAllocator.allocPerm).isRunning_(true);
			} {
				fadergroup.nodeID = server.nodeAllocator.allocPerm;
			};
			if(synthgroup.isNil) {
				synthgroup = Group.basicNew(server,
					server.nodeAllocator.allocPerm).isRunning_(true);
			} {
				synthgroup.nodeID = server.nodeAllocator.allocPerm;
			};
			if(effectgroup.isNil) {
				effectgroup = Group.basicNew(server,
					server.nodeAllocator.allocPerm).isRunning_(true);
			} {
				effectgroup.nodeID = server.nodeAllocator.allocPerm;
			};
			bundle.add(fadergroup.addToTailMsg(server.asTarget));
			bundle.add(synthgroup.addToTailMsg(fadergroup));
			bundle.add(effectgroup.addToTailMsg(fadergroup));
				// fader synth goes after effectgroup (i.e. at tail)
			synth = Synth.basicNew(def.defName/*(postSendReady)*/, server,
				server.nodeAllocator.allocPerm);
			bundle.add(synth.addToTailMsg(fadergroup,
				[\busin, inbus.index, \busout, (outbus ? bus).index] ++ this.mapArgList));

				// this is here in case we're reconstructing after a cmd-.
			preSends.do({ |pre| pre.makeServerObjectsToBundle(nil, bundle); });
			postSends.do({ |post| post.makeServerObjectsToBundle(nil, bundle); });
			bus.notNil.if({ this.outbusSetToBundle(bus, false, bundle:bundle); });
			bundled = 1;
		});
	}

	free {
		arg updateGUI = true;	// by default, fix gui
							// but MixingBoard-free calls
							// this and shouldn't update
		inbus.notNil.if({
			MixerChannelReconstructor.remove(this);
			this.stopAuto;

				// tributaries need to know I'm gone -- negate the numMixersInChain
			this.updateMixersInChain(numMixersInChain.neg);

			this.prReleaseAntecedents.prReleaseDescendents;

			(updateGUI && mcgui.notNil).if({
				mcgui.freeOnMixerFree.if({
						// if it's in a gui board, pull from gui
						// false = removeAt shouldn't free mixer
						// since it's being freed here
					mcgui.board.remove(this, false)
				}, {
					mcgui.mixer = nil;
				});
			});
			recProxy.notNil.if({ this.stopRecord });
			scopeSupport.notNil.if({ scopeSupport.free });
				// remove from mc dictionary
			inbus.notNil.if({ servers.at(server).removeAt(inbus.index) });
			this.freePatches; 		// stop all processes -- Patches first
			preSends.do({ arg p; p.free(false) });
			postSends.do({ arg p; p.free(false) });
			fadergroup.free;
			[fadergroup, synthgroup, effectgroup, synth].do({ |node|
				server.nodeAllocator.freePerm(node.nodeID);
			});

				// this is a kludge b/c SharedBus frees itself, but I need to keep it
				// to remove it from BusDict
			inbus.released = true;
			BusDict.free(inbus);
				// and this is needed because a Patch might try to free the bus a second time
			inbus.released = false;

				// if outbus belongs to a mixer, don't free it
			outbus.asMixer.isNil.if({ BusDict.free(outbus); });

			controls.do(_.free);

			// should notify before killing the variables that identify me
			MixerChannel.changed(\mixerFreed, this);

			inbus = outbus = synth = fadergroup = effectgroup = synthgroup =
				controls = /*autoSynths =*/ lineRoutines = def =
				postSends = preSends = server = mcgui = nil;

			bundled = -1;		// to make sure .ready returns false

					// if you're using my MIDI hierarchy, check for midi controls
					// that are connected to this mixer
			'MIDIPort'.asClass.update;
			MixingBoard.refresh;  // update list of in/out buses
		});
	}

	*update { arg obj, changer, subject;
			// MC class is stored as dependant of server
			// to free MC's (most important, remove them from reconstructor)
		switch(changer)
			{ \serverRunning } {
				obj.serverRunning.if({
						// no need for waitForBoot b/c the serverRunning message occurs
						// only when the server has sent an alive message
					MixerChannelDef.sendSynthDefs(obj);
				}, {
					AppClock.sched(5, {
						obj.serverRunning.not.if({
								// free all channels associated with this server
							this.servers[obj].tryPerform(\values).do({ |mc| mc.free });
						});
					});
				});
			}
			{ \serverAdded } {
				subject.addDependant(this);
			};
	}

	freePatches {
		patchesPlayed.do({ arg p; p.stop; });		// stop all processes
		patchesPlayed = Array.new;
	}

	asBus {	// tell the outside world where I live
		^inbus
	}

	asTarget {	// allow mixerchannel to be used as target in Synth.new etc.
		^synthgroup
	}

	outbus_ { arg bus, updateGUI = true, target, moveAction;
		var bundle;
		bundle = List.new;
		this.outbusSetToBundle(bus, updateGUI, target, moveAction, bundle);
		MixerChannelReconstructor.queueBundle(server, bundle);
	}

	outbusSetToBundle { arg bus, updateGUI = true, target, moveAction, bundle;
			// repatch this channel to a new destination
			// moveAction should be \moveBefore, \moveAfter, \moveToHead, \moveToTail

		var mc, oldmc, incr = 0;

		bus.isNumber.if({	// allow integer in
			bus = Bus.new(\audio, bus, def.outChannels, server);
		});

		bus.isMixerChannel.if({	// allow mixerchannel
			bus = bus.inbus;	// now isolate the bus for the following
		});

		((mc = bus.asMixer).notNil).if({
			mc.descendentsInclude(this).if({
				MethodError("% -> % will produce an infinite loop."
					.format(this, mc), this).throw;
			}, {
					// if the dest mixer is routed to another mixer,
					// place the dest mixer before its destination
				incr = incr + mc.numMixersInChain;
				mc.prAddAntecedent(this);
				this.prAddDescendent(mc);
			});
		});

			// bookkeeping for old outbus if it's a mixer
		(oldmc = outbus.asMixer).notNil.if({
			incr = incr + oldmc.numMixersInChain.neg;
			oldmc.prRemoveAntecedent(this);
			this.prRemoveDescendent(oldmc);
		});

			// adjust myself and dependents if needed
		(incr != 0).if({
			this.updateMixersInChain(incr);
				// can't think of a better way than brute force
				// to get all the channels in the right order
				// it would be manageable if I could guarantee that the client always knows the
				// server's node order, but the order can be changed in so many places
				// thus, I just force it.
			MixerChannel.fixNodeOrderToBundle(server, bundle);
		});

		outbus = bus;		// save
		bundle.add(["/n_set", synth.nodeID, \busout, outbus.index]);
		(updateGUI and: { mcgui.notNil }).if({ mcgui.refresh });
	}

	updateMixersInChain { |incr = 0|
		numMixersInChain = numMixersInChain + incr;
		antecedents.do({ |ante|
			(ante !== this).if({ ante.tryPerform(\updateMixersInChain, incr) });
		});
	}

	*fixNodeOrderToBundle { |serv, bundle|
		var	mx;
		((mx = servers[serv]).size > 0).if({
			mx = mx.values.asArray.sort({ |a, b| a.numMixersInChain > b.numMixersInChain });
			bundle.add(mx[0].fadergroup.moveToHeadMsg(serv.asTarget));
			mx.doAdjacentPairs({ |before, after|
				bundle.add(after.fadergroup.moveAfterMsg(before.fadergroup));
			});
		});
	}

		// you may need to specify a dependency other than a send or outbus
	sendsSignalTo { |mc|
		mc.tryPerform(\addDependantMC, this);
	}

	stopsSendingTo { |mc|
		mc.tryPerform(\removeDependantMC, this);
	}

	receivesSignalFrom { |mc|
		this.addDependantMC(mc);
	}

	stopsReceivingFrom { |mc|
		this.removeDependantMC(mc);
	}

		// method name predates inclusion of antecedents and descendents collections
		// not changing for backward compatibility
		// adds mc as an antecedent (source) for this mixer
	addDependantMC { |mc|
		var bundle;
		this.prAddAntecedent(mc);
		mc.prAddDescendent(this);
		mc.updateMixersInChain(numMixersInChain);
		bundle = List.new;
		this.class.fixNodeOrderToBundle(server, bundle);
		(bundle.size > 0).if({
			MixerChannelReconstructor.queueBundle(server, bundle);
		});
	}

	removeDependantMC { |mc|
		var bundle;
		this.prRemoveAntecedent(mc);
		mc.prRemoveDescendent(this);
		mc.updateMixersInChain(numMixersInChain.neg);
		bundle = List.new;
		this.class.fixNodeOrderToBundle(server, bundle);
		(bundle.size > 0).if({
			bundle = [#[error, -1]] ++ bundle ++ [#[error, -2]];
			MixerChannelReconstructor.queueBundle(server, bundle);
		});
	}

	addAsDependantOf { |mc|
		mc.tryPerform(\addDependantMC, (this));
	}

	removeAsDependantOf { |mc|
		mc.tryPerform(\removeDependantMC, (this));
	}

//// PRIVATE METHODS FOR ANTECEDENTS/DEPENDENTS -- DO NOT CALL THESE YOURSELF ////

	descendentsInclude { |mc|
		(mc === this).if({ ^true });
		descendents.includes(mc).if({ ^true }, {
			descendents.do({ |desc|
				desc.descendentsInclude(mc).if({ ^true });
			});
		});
		^false
	}

	prAddAntecedent { |mc|
		antecedents.add(mc);
	}

	prRemoveAntecedent { |mc|
		antecedents.remove(mc);
	}

	prAddDescendent { |mc|
		descendents.add(mc);
	}

	prRemoveDescendent { |mc|
		descendents.remove(mc);
	}

	prReleaseAntecedents {
		antecedents.do({ |ante|
			ante.prRemoveDescendent(this);
		});
	}

	prReleaseDescendents {
		descendents.do({ |desc|
			desc.prRemoveAntecedent(this);
		});
	}

//// END PRIVATE METHODS ////

	active { ^inbus.notNil }
	isMixerChannel { ^true }
	groupBusInfo { |group = \synth|
		^switch(group)
			{ \fader } { [fadergroup, inbus] }
			{ \synth } { [synthgroup, inbus] }
			{ \effect } { [effectgroup, inbus] }
			/* default */ { [synthgroup, inbus] }
	}

	asString {
		^this.active.if({ "MixerChannel(" ++ name ++ ", " ++ server.name ++ ", "
				++ def.inChannels ++ ", " ++ def.outChannels ++ ")" },
			{ ^"MixerChannel(**DEAD**)" });
	}

	asMixerChannelGUI { |board|
		^mcgui ?? { MixerChannelGUI(this, board) }
	}

	postSettings {
		var destMixer;
		"\n".postln;
		this.postln;	// show this.asString
		" Synthgroup: ".post; synthgroup.postln;
		"Effectgroup: ".post; effectgroup.postln;
		"Fader synth: ".post; synth.postln;

		"Destination: ".post;
		destMixer = outbus.asMixer;		// am I routing to a mixer?
		destMixer.notNil.if({
			destMixer.postln;			// show as mixer
		}, {
			(BusDict.audioNames.at(server).at(outbus.index) ++ " -> "
				++ outbus.asString).postln;			// show as bus
		});
		controls.asSortedArray.do({ |pair|
			"%%: %".postf(reptChar(15-(pair[0].asString.size), $ ), pair[0],
				pair[1].value.round(0.001));
			(pair[0] == \level).if({
				" (% dB)\n".postf(pair[1].value.ampdb.round(0.001));
			}, {
				Post.nl;
			});
		});
		(preSends.size > 0).if({
			"Pre-fader sends:".postln;
			preSends.do({ arg send; send.postSettings(1); });  // 1 is for indent
		});
		(postSends.size > 0).if({
			"Post-fader sends:".postln;
			postSends.do({ arg send; send.postSettings(1); });
		});
	}

	asMixer {}

	ready { ^(bundled == 2) }

	copy { ^this }		// bad idea to copy mixerchannels (bundled status may get out of sync)
	deepCopy { ^this }		// Proto copies things, so I prevent copying here
	shallowCopy { ^this }

		// in: (level: GenericGlobalControl, pan: GenericGlobalControl)
		// out: [\level, \c0, \pan, \c1]
	mapArgList {
		^controls.asKeyValuePairs.collect({ |item|
			item.isSymbol.if({ item }, { item.asMapArg });
		})
	}

	inChannels { ^def.inChannels }
	outChannels { ^def.outChannels }

	mcgui_ { |mcg|
		mcgui = mcg;
		controls.do(_.mixerGui_(mcg));
	}

	resyncMIDI {
		midiControl.notNil.if({ midiControl.resync },
			{ mcgui !? { mcgui.resyncMIDI } });
	}

///////////////////// INSTANT SETTING OF BASIC PARMS /////////////////////

	getControl { |name|
		^controls[name].value
	}

	setControl { |name, value, updateGUI = true, stopAutomation = true, resync = true|
			// flags are because a standard level setting by
			// the user should update gui and stop automation
			// but, other processes use this method that
			// should skip one or both of these
		var gc = controls[name];

		gc.notNil.if({
				// deactivate automation if running
			stopAutomation.if({ this.stopAuto(name) });

				// note, this is valid even when muted because
				// mute works by breaking the bus mapping - the gc bus can change
				// but the fader synth doesn't hear it until un-muted
			gc.set(value, updateGUI, resync: resync);
			resync.if({ this.resyncMIDI });

			(updateGUI && mcgui.notNil).if({	// update GUI if it exists
				mcgui.updateView(name, value);
			});
				// notification protocol
			this.changed((what: \control, name: name, value: value));
		});
	}

	setControls { |dict|
		dict !? {
			dict.keysValuesDo({ |k, v|
				this.setControl(k, v);
			});
		}
	}

	mapControl { |name|
		name.notNil.if({
			synth.set(name, controls[name].asMapArg);
		}, {
			controls.keysDo(this.mapControl(_));
		});
	}

	level { ^this.getControl(\level) }
	pan { ^this.getControl(\pan) }

	level_ { arg lev, updateGUI = true, stopAutomation = true;
		this.setControl(\level, lev, updateGUI, stopAutomation)
	}

	pan_ { arg p, updateGUI = true, stopAutomation = true;
		this.setControl(\pan, p, updateGUI, stopAutomation)
	}

	name_ { arg n;	// give mixerchannel a new name and update gui
		name = n;

		mcgui.notNil.if({		// if it's in a board
			mcgui.updateView(\name, n);
		});
		^this
	}

	mute { arg muteMe, updateGUI = true;
		muted = muteMe ?? { muted.not };		// if you supply no argument, I toggle
		muted.if({
			synth.set(muteControlName, 0);	// breaks bus mapping
		}, {
			this.mapControl(muteControlName);	// restore bus mapping
		});
		updateGUI.if({ mcgui.tryPerform(\updateView, \mute, muted.binaryValue) });
	}

	run { arg bool = true;
		fadergroup.run(bool).isRunning_(bool);
	}

	isRunning { ^(fadergroup.isRunning ? true) }

///////////////////// BASIC AUTOMATION /////////////////////

	automate { |name, synthdef, args|
		this.stopAuto(name);

		^controls[name].automate(synthdef, args, fadergroup);
	}

		// workaround for backward compatibility
		// MC used to keep a dictionary of automation synths
		// now the MixerControls keep track of them
		// in case you ask for autoSynths, I need to return that dictionary
		// nils are automatically dropped per Dictionary rule
	autoSynths {
		^controls.collect(_.autoSynth)
	}

		// nil means wipe them all out--dangerous?
	stopAuto { |name|
		name.notNil.if({
			lineRoutines[name].stop;
			lineRoutines[name] = nil;
			controls[name].stopAuto;
		}, {
			controls.keysDo({ |key| this.stopAuto(key) })
		});
	}

	watch { |name|
		var	ctl;
		name.notNil.if({
			(ctl = controls[name]).notNil.if({
				ctl.watch(name, mcgui);
			});
		}, {
			controls.keysDo({ |key| this.watch(key) })
		});
	}

		// no need to put a call to this into this.free because the control
		// will stop watching when it is freed
	stopWatching { |name, count = 0|
		var	ctl;
		name.notNil.if({
			(ctl = controls[name]).notNil.if({
				ctl.stopWatching(count);
			});
		}, {
			controls.keysDo({ |key| this.stopWatching(key, count) })
		});
	}



	levelAuto {
			// place a .kr synthdef onto level control bus
			// now allows .play syntax: m.levelAuto({ LFNoise1.kr(0.1, 0.5, 0.5) });
		arg synthdef, args;
		^this.automate(\level, synthdef, args)
	}

	panAuto {
			// .kr synthdef -> pan control bus
		arg synthdef, args;
		^this.automate(\pan, synthdef, args)
	}

		// line automation -- ...LineTo uses current value as start
		// any warp that can be used in a ControlSpec can be used here
	controlLine {
		arg name, start, end, dur = 1, warp;

		var startTime, timeSpec;

		name.notNil.if({
			this.stopAuto(name);	// begin by clearing automation

				// schedule periodic updates for level and gui
				// spec to convert time into level
			warp ?? { warp = controls[name].spec.warp.class }; // by default use warp from control
			start ?? { start = controls[name].spec.minval };
			end ?? { end = controls[name].spec.maxval };
			timeSpec = ControlSpec.new(start, end, warp);
			lineRoutines[name] = Routine({
				startTime = thisThread.clock.beats;
				{ (thisThread.clock.beats - startTime) <= dur }.while({
					this.setControl(name, timeSpec.map(
						((thisThread.clock.beats - startTime) / dur)
					), true, false);  // update gui but don't stop automation
					guiUpdateTime.yield	// wait before going again
				});
				this.setControl(name, end, true, false);
				nil.yield		// stop
			});
				// previously used routine.play(SystemClock)
				// but this was bad when controlLine was called from another clock
				// this is the proper way: schedule for 'now' on sysclock
			SystemClock.sched(0, lineRoutines[name]);
		}, {
			MethodError("Must specify a control name.", this).throw;
		});
	}

	controlLineTo {
		arg name, end, dur = 1, warp;
		this.controlLine(name, controls[name].value, end, dur, warp);
	}

	levelLine {
		arg start, end, dur = 1, warp;
		this.controlLine(\level, start, end, dur, warp);
	}

	levelTo {		// start from current level
		arg end, dur, warp;
		this.controlLineTo(\level, end, dur, warp);
	}

	panLine {
		arg start, end, dur = 1, warp;
		this.controlLine(\pan, start, end, dur, warp);
	}

	panTo {		// start from current level
		arg end, dur, warp;
		this.controlLineTo(\pan, end, dur, warp);
	}

///////////////////// BASIC PLAYING /////////////////////

	play { arg thing, args;
		^thing.playInMixerGroup(this, synthgroup, thing.tryPerform(\patchClass) ?? { Patch }, args);
	}

	playfx { arg thing, args;
		^thing.playInMixerGroup(this, effectgroup, FxPatch, args);
	}

		// when a patch is played, I have to keep track so I can free server resources
		// on mixer free
	addPatch { |patch|
		patchesPlayed = patchesPlayed.add(patch);
	}

	queueBundle { |bundle|
		bundle.isFunction.if({
			bundle = server.makeBundle(false, bundle);
		});
			// send immediately if channel is ready; otherwise queue it
		this.ready.if({
			server.listSendBundle(nil, bundle)
		}, {
			MixerChannelReconstructor.queueBundle(server, bundle);
		});
	}

		// do this when mixerchannel is ready
	doWhenReady { |func|
		(bundled >= 0).if({	// bundled == -1 if the MC has been freed
			MixerChannelReconstructor.queueBundle(server, nil,
				(func: func, env: currentEnvironment, chan: this))
		});
	}

/////////////////////// SCOPE ///////////////////////

	scope { arg layout, bounds;
		scopeSupport = MixerScope.new(this, layout, bounds);
	}

///////////////////// RECORDING /////////////////////

	prepareRecord { arg path, headerformat = "aiff", sampleformat = "float";
		recProxy.isNil.if({
			recProxy = MixerRecorder.new(this, path, headerformat, sampleformat);
		});
	}

	startRecord { arg path, headerformat = "aiff", sampleformat = "float";
		recProxy.isNil.if({ this.prepareRecord(path, headerformat, sampleformat) });
		recProxy.record(false);
	}

	stopRecord {
		recProxy.close;
		recProxy = nil;
	}

	pauseRecord {
		recProxy.tryPerform(\pause);
	}

	unpauseRecord {
		recProxy.isNil.if({
			this.startRecord;
		}, {
			recProxy.unpause;
		});
	}

	isRecording {
		^(recProxy.notNil and: { recProxy.running })
	}

///////////////////// SENDS /////////////////////

	newPreSend { arg dest, level;		// bus number or mixerchannel
		var new;
		new = MixerPreSend.new(this, dest, level);	// the send will store itself in my array
		mcgui.notNil.if({ mcgui.refresh });
		^new
	}

	newPostSend { arg dest, level;
		var new;
		new = MixerPostSend.new(this, dest, level);
		mcgui.notNil.if({ mcgui.refresh });
		^new
	}

///////////////////// INITIALIZATION /////////////////////

	*initClass {
		servers = Dictionary.new;
		Class.initClassTree(Server);
		Server.set.do({ |srv| srv.addDependant(MixerChannel); });
	}
}

MixerScope {
		// handles preparing the mc for scoping
		// gui is done by MixerScopeGui
	var	<channel, <buffer, <bus, <synthID;

	*new { arg mc, layout;
		^super.new.init(mc);
	}

	init { arg mc, layout;
		if(GUI.current.name == \CocoaGUI and: { mc.server !== Server.internal }) {
			"Scope can only be used with the internal server when using the Cocoa GUI kit.".die;
		} {
			channel = mc;
			bus = mc.inbus;
			if(GUI.current.name == \QtGUI and: { channel.server.hasShmInterface }) {
				buffer = ScopeBuffer.alloc(channel.server, mc.def.outChannels);
			} {
				buffer = Buffer.alloc(channel.server, 4096, mc.def.outChannels);
			};
			this.makeServerObjects;
			this.gui(layout);
		};
	}

	makeServerObjects {
		var bundle;
		bundle = List.new;
		this.makeServerObjectsToBundle(bundle);
		MixerChannelReconstructor.queueBundle(channel.server, bundle);
	}

	makeServerObjectsToBundle { arg bundle;
		var def;
		def = SynthDef.new("mxscope" ++ GUI.current.name ++ buffer.numChannels, { |bus, bufnum|
			(switch(GUI.current.name)
				{ \CocoaGUI } { ScopeOut }
				{ \SwingGUI } { JScopeOut }
				{ \QtGUI } {
					if(channel.server.hasShmInterface) { ScopeOut2 } { ScopeOut }
				}
			).ar(In.ar(bus, buffer.numChannels), bufnum)
		});
		bundle.add(["/d_recv", def.asBytes,
			[\s_new, def.name, synthID = channel.server.nodeAllocator.allocPerm, 1,
				channel.fadergroup.nodeID, bus: bus.index, bufnum: buffer.bufnum]]
		);
	}

	free {
		this.dependants.do({ |d| d.free; d.remove(nil, false); });
			// because of confusion when closing the window, this method may actually
			// be called more than once, in which case channel will be nil
		channel.notNil.if({
			channel.server.sendMsg(\n_free, synthID);
			channel.server.nodeAllocator.freePerm(synthID);
			buffer.free;
			channel.scopeSupport = nil;
			channel = buffer = bus = synthID = nil;
		});
	}

	asString { ^"Scope : " ++ channel.asString }

	guiClass { ^MixerScopeGui }
}

MixerChannelReconstructor {
	classvar	<mixers;	// this has to be different from the Server.servers data structure!
					// this one must be ordered, that one must be keyed by inbus number

	classvar	<bundleQueue,	// for sending big bundles in small batches
			queueRoutine,
			<>queueDelay = 0.05;

	*initClass {
		ServerTree.add(this);
		mixers = IdentityDictionary.new;	// server -> List of mixerchannels
		bundleQueue = List.new;
	}

	*add { arg chan;
		mixers[chan.server].isNil.if({
			mixers.put(chan.server,
				SortedList(function: { |a, b| a.numMixersInChain > b.numMixersInChain }));
			chan.server.addDependant(this);
		});
		mixers[chan.server].add(chan);
	}

	*remove { arg chan;
		mixers[chan.server].remove(chan);
	}

	*queueBundle { |server, bundle, args|
		args.isNil.if({ args = () });
		args.put(\bundle, bundle);
		args.put(\server, server);
		bundleQueue.add(args);
		this.doQueue;
	}

	*doQueue {
			// it's insane that I have to put this function in a var, but it's the only
			// way to get people to stop panicking when they see the "variable declarations"
			// warning.
		var	bundleAction;

		bundleAction = {
			var	thisBundle;
			try {
				thisBundle = bundleQueue.removeAt(0);
				(thisBundle.bundle ?? { [] }).clump(5).do({ |clump|
					thisBundle.server.listSendBundle(thisBundle.server.latency, clump);
					queueDelay.wait;
				});
				thisBundle[\chan] !? { thisBundle[\chan].bundled = 2 };  // "ready"
				thisBundle[\func].notNil.if({
					SystemClock.sched((thisBundle.server.latency ? 0.2) + queueDelay, Routine({
						thisBundle[\env].notNil.if({
							thisBundle[\env].use({
								thisBundle[\func].value(thisBundle[\chan]);
							});
						}, {
							thisBundle[\func].value(thisBundle[\chan]);
						});
						nil.yield
					}));
					queueDelay.wait;
				});
			} { |error|
				error.reportError;
				"Error occured while processing MixerChannel queue. Continuing.".warn;
			}
		};
			// if the routine alredy exists, eventually I'll get around to the last thing
			// in the queue, so don't recreate
		queueRoutine.isNil.if({
			queueRoutine = CleanupStream(
				Routine({
					{ bundleQueue.size > 0 }.while(bundleAction);
					nil.yield;
				}),
			{ queueRoutine = nil; });	// when routine stops, clear so I can restart next time
			SystemClock.sched(0, queueRoutine);
		});
	}

	*doOnServerTree {
		var bundle;
		mixers.keysValuesDo({ |s, channels|
			bundle = List.new;
				// during reconstruction, mixer sends need to know if their target channel
				// exists on the server or not
			channels.do({ |ch| ch.bundled = 0; });
			channels.do({ |ch|
				ch.makeServerObjectsToBundle(bundle);
			});
			MixerChannel.fixNodeOrderToBundle(s, bundle);
			this.queueBundle(s, bundle);
			channels.do(_.bundled = 2);
		});
	}
}
