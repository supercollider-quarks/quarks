MixerSend {
		// container for aux sends for MixerChannels & boards
		// input must be a mixerchannel; output can be an mc, bus or bus#

	var	<inMixer, <outbus, <levelControl, <level,
		<sendSynth, /*<levelSynth,*/ <guiUpdater = nil, targ,
		<>guiIndex;	// change this from outside at your peril!
		
	var <>guiUpdateTime = 0.25;		// see .levelLine / .levelTo
		// creation & init methods in MixerPre/PostSend
		
		// setting level
	level_ { arg lev, updateGUI = true, stopAutomation = true;
		stopAutomation.if({ this.stopLevelAuto; });
		level = lev;
		sendSynth.set(\level, level);
			// gui update
		(updateGUI && inMixer.mcgui.notNil && guiIndex.notNil).if({
			inMixer.mcgui.updateView(this.guiKey, level);
		});
		^this
	}
	
		// automation
	levelAuto {
			// place a .kr synthdef onto level control bus
		arg synthdef, args;
		var	levelSynth;
		
			// if levelSynth is active, kill it
		this.stopLevelAuto;

			// create levelControl if it doesn't exist
		levelControl.isNil.if({
			levelControl = GenericGlobalControl("send level", nil, level, \amp);
		});

		levelSynth = levelControl.play(synthdef, args, inMixer.fadergroup);

		sendSynth.map(\level, levelControl.bus.index);
		^levelSynth
	}

	levelSynth { ^levelControl.tryPerform(\autoSynth) }
	
		// line automation -- ...To uses current value as start
		// any warp that can be used in a ControlSpec can be used here
	levelLine {
		arg start = 0, end = 1, dur = 1, warp = \amp;
		
		var startTime, timeSpec;
		this.stopLevelAuto;	// begin by clearing automation

			// start the automation process on the server
			// this line will be revived when bus.get is implemented
		
			// schedule periodic updates for level and gui
			// spec to convert time into level
		timeSpec = ControlSpec.new(start, end, warp);
		guiUpdater = Routine({
			startTime = thisThread.clock.beats;
			{ (thisThread.clock.beats - startTime) <= dur }.while({
				this.level_(timeSpec.map(
					(thisThread.clock.beats - startTime) / dur
				), true, false);  // update gui but don't stop automation
				guiUpdateTime.yield	// wait before going again
			});
			nil.yield		// stop
		}).play(SystemClock);
	}
	
	levelTo {		// start from current level
		arg end, dur, warp = \amp;
		
		this.levelLine(level, end, dur, warp);
	}
	
	stopLevelAuto {
		this.levelSynth.notNil.if({
			levelControl.stopAuto;
// 			sendSynth.map(\level, -1);	// return to static level
// 			this.levelSynth.free;			// kill kr synth
// 			levelSynth = nil;
		});
		guiUpdater.notNil.if({
			guiUpdater.stop;
			guiUpdater = nil;
		});
		^this
	}

	outbus_ { arg bus, updateGUI = true, target, moveAction;
		var bundle;
		bundle = List.new;
		this.outbusSetToBundle(bus, updateGUI, target, moveAction, bundle);
		MixerChannelReconstructor.queueBundle(inMixer.server, bundle);
	}
	
	outbusSetToBundle { 		// repatch send to another bus
		arg bus, updateGUI = true, target, moveAction, bundle;
		var oldmc, mc, incr = 0;
		
		targ = targ ? target;
		
		bus.isMixerChannel.if({
			bus = bus.inbus;
		});
		bus.isNumber.if({
			bus = Bus.new(\audio, bus, inMixer.inChannels, inMixer.server);
		});
		
		((mc = bus.asMixer).notNil).if({
			mc.descendentsInclude(inMixer).if({
				MethodError("MixerSend % -> % will produce an infinite loop."
					.format(inMixer, mc), this).throw;
			}, {
					// if the dest mixer is routed to another mixer,
					// place the dest mixer before its destination
				incr = incr + mc.numMixersInChain;
				mc.prAddAntecedent(inMixer);
				inMixer.prAddDescendent(mc);
			});
		});

			// bookkeeping for old outbus if it's a mixer
		(oldmc = outbus.asMixer).notNil.if({
			incr = incr + oldmc.numMixersInChain.neg;
			oldmc.prRemoveAntecedent(inMixer);
			inMixer.prRemoveDescendent(oldmc);
		});

			// if target of send is not created in the bundle yet, create it
			// before adjusting order of exec and setting outbus
		(mc.tryPerform(\bundled) == 0).if({
			bundle.add(mc.makeServerObjectsToBundle(bundle));
		});

			// adjust myself and dependents if needed
		(incr != 0).if({
			inMixer.updateMixersInChain(incr); 
				// can't think of a better way than brute force 
				// to get all the channels in the right order
				// it would be manageable if I could guarantee that the client always knows the
				// server's node order, but the order can be changed in so many places
				// thus, I just force it.
			MixerChannel.fixNodeOrderToBundle(inMixer.server, bundle);
		});

		bundle.add(["/n_set", sendSynth.nodeID, \busout, bus.index]);
		outbus = bus;
		updateGUI.if({ inMixer.mcgui.refresh });
	}
	
	makeServerObjects { |bus|	// details are handled by subclass - objectsToBundle
		var bundle;
		bundle = List.new;
		this.makeServerObjectsToBundle(bus, bundle);
		MixerChannelReconstructor.queueBundle(inMixer.server, bundle);
	}
	
	postSettings { arg indent = 0;
		var destMixer;
		indent.reptChar.post; " Send synth: ".post; sendSynth.postln;
		indent.reptChar.post; "Destination: ".post;
		destMixer = outbus.asMixer;		// am I routing to a mixer?
		destMixer.notNil.if({
			destMixer.postln;			// show as mixer
		}, {
			(BusDict.audioNames.at(inMixer.server).at(outbus.index) ++ " -> " ++ outbus).postln;			// show as bus
		});
		indent.reptChar.post;
		("      Level: " ++ level.round(0.001) ++ " ("
			++ level.ampdb.round(0.001) ++ " dB)").postln;
	}
	
}	

MixerPreSend : MixerSend {

	*new { arg in, out, level = 1;
		out.notNil.if({
			^super.new.initPreSend(in, out, level);
		}, {
			format("No destination for presend for %", in).warn;
			^nil
		});
	}

	initPreSend { arg in1, out1, lev = 1, target;
		
		inMixer = in1;	// must be mixerchannel in
		level = lev ? 1;
		targ = target;
		
			// Buses should be left alone, other types are fixed by following rules:
		out1.isKindOf(Bus).if({
			nil
		}, {
				// support for mixerchannels in
			out1.isMixerChannel.if({
				out1 = out1.inbus;
			});
			
				// support for sending bus # for out
			out1.isNumber.if({
				out1 = Bus.new(\audio, out1, inMixer.inChannels, inMixer.server);
			});
			
		});
		
			// check numchannels
		(inMixer.inChannels != out1.numChannels).if({
			("Warning: numChannel mismatch creating send for " ++ inMixer.name
				++ " -- " ++ inMixer.inChannels ++ " --> " ++ outbus.numChannels).postln;
		});
		
		level = lev;
		
		this.makeServerObjects(out1);

		guiIndex = inMixer.preSends.size;
		inMixer.preSends = inMixer.preSends.add(this);
			// fix gui display
		inMixer.mcgui.notNil.if({ inMixer.mcgui.refresh });
		
		^this
	}
	
	makeServerObjectsToBundle { arg bus, bundle;
			// reroute mixerchannel output to transfer bus
			// this is the send itself, not merely a transfer
			// takes from sendbus, sends to the send destination
		sendSynth = Synth.basicNew("mixers/Send" ++ inMixer.inChannels, inMixer.server,
			inMixer.server.nodeAllocator.allocPerm);
		bundle.add(sendSynth.addBeforeMsg(inMixer.synth,
			[\busin, inMixer.inbus.index, \busout, (bus ? outbus).index,
			 \level, level]
		));

		bus.notNil.if({ this.outbusSetToBundle(bus, false, targ, bundle:bundle); });
	
	}

	free {
		arg updateGUI = true;
		var mc, bundle;
		
		this.stopLevelAuto;	// cancel automation

			// update node order variables
		(mc = outbus.asMixer).notNil.if({
			bundle = List.new;
			inMixer.updateMixersInChain(mc.numMixersInChain.neg);
			mc.prRemoveAntecedent(inMixer);	// requiring both method calls is bad form
			inMixer.prRemoveDescendent(mc);
		});

		inMixer.preSends.remove(this);

		sendSynth.free;	// stop sending
		sendSynth.server.nodeAllocator.freePerm(sendSynth.nodeID);
		levelControl.free;
		
			// fix gui display
		(inMixer.mcgui.notNil && updateGUI).if({ inMixer.mcgui.board.refresh });
	}

	guiKey { ^("presend" ++ guiIndex).asSymbol }
	
}

MixerPostSend : MixerSend {
	
	*new { arg in, out, level = 1;
		out.notNil.if({
			^super.new.initPostSend(in, out, level);
		}, {
			format("No destination for postsend for %", in).warn;
			^nil
		});
	}

	initPostSend { arg in1, out1, lev = 1, target;
		
		inMixer = in1;	// must be mixerchannel in
		
		level = lev ? 1;
		
		out1.isKindOf(Bus).if({
			nil
		}, {
				// support for mixerchannels for out
			out1.isMixerChannel.if({
				out1 = out1.inbus;
			});
			
				// support for bus # for out
			out1.isNumber.if({
				out1 = Bus.new(\audio, out1, inMixer.outChannels, inMixer.server);
			});
		});
		
			// check numchannels
			// outChannels here b/c this is postfader, using output channels
		(inMixer.outChannels != out1.numChannels).if({
			("Warning: numChannel mismatch creating send for " ++ inMixer.name
				++ " -- " ++ inMixer.outChannels ++ " --> " ++ outbus.numChannels).postln;
		});
		
		this.makeServerObjects(out1);
		
		guiIndex = inMixer.postSends.size;
		inMixer.postSends = inMixer.postSends.add(this);
			// fix gui display
		inMixer.mcgui.notNil.if({ inMixer.mcgui.refresh });
		
		^this
	}
	
	makeServerObjectsToBundle { arg bus, bundle;
			// reroute mixerchannel output to transfer bus
			// this is the send itself, not merely a transfer
			// takes from sendbus, sends to the send destination
		sendSynth = Synth.basicNew("mixers/Send" ++ inMixer.outChannels, inMixer.server,
			inMixer.server.nodeAllocator.allocPerm);
		bundle.add(sendSynth.addAfterMsg(inMixer.synth,
			[\busin, inMixer.inbus.index,
			 \busout, (bus ? outbus).index,
			 \level, level]
		));

		bus.notNil.if({ this.outbusSetToBundle(bus, false, targ, bundle:bundle); });
	
	}
	
	free {
		arg updateGUI = true;
		var mc, bundle;

		this.stopLevelAuto;	// cancel automation

			// update node order variables
		(mc = outbus.asMixer).notNil.if({
			bundle = List.new;
			inMixer.updateMixersInChain(mc.numMixersInChain.neg);
			mc.prRemoveAntecedent(inMixer);	// requiring both method calls is bad form
			inMixer.prRemoveDescendent(mc);
		});

		inMixer.postSends.remove(this);	// pull from channel

		sendSynth.free;	// stop sending
		sendSynth.server.nodeAllocator.freePerm(sendSynth.nodeID);
		levelControl.free;

			// fix gui display
		(inMixer.mcgui.notNil && updateGUI).if({ inMixer.mcgui.board.refresh });
	}

	guiKey { ^("postsend" ++ guiIndex).asSymbol }
}
