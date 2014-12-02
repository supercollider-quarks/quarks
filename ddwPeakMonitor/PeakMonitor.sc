
PeakMonitor {
		// back end for graphic peak follower

	classvar	<all;	// kr bus # -> monitor object

	var	<target,	// may be bus or mixer
		<synth,	// writes peak level(s) onto control bus(es)
		<bus,
		<synthTarget,
		<freq,
		<>peaks,
		resp,	// OSCresponderNode
		<updater,
		<mixer;	// used only when PM is hitting a mixerchannel
		
	*initClass {
		all = IdentityDictionary.new;
		CmdPeriod.add(this);
	}

	*new { arg target = 0, freq = 6, layout;
		^super.new.init(target, freq, layout)
	}
	
	init { arg t, f = 2, layout;
		var	groupbus;		// to determine if the target is a mixerchannel
		freq = f ? 2;
		(groupbus = t.tryPerform(\groupBusInfo, \fader)).notNil.if({
			mixer = t;	// save for free
			target = groupbus[1];	// if it's postsendready, fader's output is on the mc's bus
			synthTarget = groupbus[0];	// place at tail of fadergroup
		}, {
				// convert other types of args
			target = t.asBus(\audio, 2, t.tryPerform(\server) ? Server.default);
				// go to rootnode -- need to consider this an fx synth, so group 0
			synthTarget = Group.basicNew(target.server, 0);
		});
		
		bus = Bus.control(target.server, target.numChannels);
		peaks = Array.fill(target.numChannels, 0);

		this.prInit;
		
		all.put(bus.index, this);	// so OSCresponder can find me
		updater = Routine({
					// first get the results from the last period
					// then reset the Peak ugen immediately after -- bundling ensures timing
			{	freq.reciprocal.wait;
				target.server.sendBundle(nil, [\c_getn, bus.index, bus.numChannels], 
					[\n_set, synth.nodeID, \t_trig, 1]);
			}.loop
		}).play(SystemClock);
		
		this.gui(layout);
	}
	
	freq_ { arg f;
		freq = f;
	}
	
	free {
		updater.stop;
			// if there's a gui, drop it
		this.dependants.do({ arg d; d.remove });
		resp.remove;
		synth !? { synth.free };
		bus !? {
			all.removeAt(bus.index);
			bus.free;
		};
		synth = bus = peaks = target = synthTarget = updater = mixer = nil;
	}
	
	*freeAll {
		all.do({ arg p; p.free });
	}
	
	*cmdPeriod {
		this.freeAll;
	}
	
	numChannels { ^target.numChannels }
	
	guiClass { ^PeakMonitorGui }

	asString { ^"PeakMonitor(" ++ target.asString ++ ", " ++ freq ++ ")" }


/// private -- set synth and osc responder
	prInit {
		var	defname = "ddw_peakmon" ++ bus.numChannels;
		synth = Synth.basicNew(defname, target.server);
		
		SynthDef(defname, {
			arg bus, kbus, t_trig;  // t_trig lets client control timing/reset of Peak
			var sig;
			sig = Peak.ar(In.ar(bus, target.numChannels), T2A.ar(t_trig));
			Out.kr(kbus, sig);
		}).send(target.server, 
			synth.newMsg(synthTarget, [\bus, target.index, \kbus, bus.index, \freq, freq],
				\addToTail));
	
		resp = OSCpathResponder(synthTarget.server.addr, ['/c_setn', bus.index], { arg t, r, m;
			PeakMonitor.all[m[1]].tryPerform(\peaks_, m[3..]).changed;
		}).add;
	}
}
