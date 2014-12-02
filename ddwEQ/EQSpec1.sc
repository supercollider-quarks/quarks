
EQSpec1 {
		// takes type, freq, rq and k and turns them into SOS coefficients
		// form borrowed from Casey Basichis
		// sc form of Regalia-Mitra equations borrowed from Joseph Anderson
		
	var	<type = \eq,	// can be \eq, \hishelf, \loshelf, \hipass, \lopass
		<freq = 440,
		<k = 1.0,		// scaling factor: < 1.0 for cut, > 1.0 for boost
		<rq = 1.0;		// reciprocal of q
	
		// assumed default -- may set the instance var directly or use server_
	var	<>sampleRate = 44100;
	
		// coefficients
	var	<a0, <a1;
	
	*newSpec { arg type = \eq, freq = 440, k = 1.0, rq = 1.0, sr = 44100;
		^super.newCopyArgs(type, freq, k, rq, sr).calc;
	}
	
	calc {
		var wc, y, tanw;
		
		switch(type)
			{ \eq } {
				a0 = freq;	// for MidEQ.ar
				a1 = rq;
			}
			
			{ \hipass } {
				a0 = freq;
			}
			
			{ \lopass } {
				a0 = freq
			}
		
			{ \hishelf } {
				k = k.abs;	// positive values for gain at nyquist
				wc = pi * freq * sampleRate.reciprocal;
				a0 = (1 - wc)/(1 + wc);
			}
		
			{ \loshelf } {
				k = k.abs;	// k values will be converted to negative in synthdef
				wc = pi * freq * sampleRate.reciprocal;
				a0 = (1 - wc)/(1 + wc);
			}
			{ Error("EQ type \\" ++ type ++ " is invalid.").throw; };
	}
	
	synthArgs {
		case { type == \eq } {
				^[\freq, freq, \rq, rq, \k, k]
			}
			
			{ #[\lopass, \hipass].includes(type) } {
				^[\freq, freq]
			}
			
			{
				^[\b1, a0, \k, k]
			}
	}
	
	type_ { arg t;
		type = t;
		this.calc;
	}
	
	freq_ { arg f;
		freq = f;
		this.calc;
	}
	
	k_ { arg newk;
		k = newk;
		this.calc;
	}
	
	rq_ { arg r;
		rq = r;
		this.calc;
	}
	
		// there is no server instancevar -- this just verifies the sample rate and recalcs
		// doing this on an EQBand (below) will not move the synth nodes
	server_ { |server|
		sampleRate = server.tryPerform(\sampleRate) ?? { sampleRate };
		this.calc;
	}
}

EQBand : EQSpec1 {
	// contains specs for 1 EQ filter
	// 1 band of stereo eq takes only 1.1% of cpu on a 700MHz iBook
	// this will go down when I make a ugen plugin for eq
	
	classvar	<translateMethods;	// to change add method to move method
	
	var	mul = 1,
		numChannels = 1,
		target, bus, addAction,
		synth,
		<>parent,
		<>gui;
	
	*initClass {
		var	dir;
		
			// if the band is already playing, this.play(target) moves the synth
			// rather than making a new one. This dictionary converts add messages
			// into move messages.
		translateMethods = IdentityDictionary[
			\addToHead -> \moveToHead,
			\addToTail -> \moveToTail,
			\addAfter -> \moveAfter,
			\addBefore -> \moveBefore,
			\moveToHead -> \moveToHead,	// b/c user might specify a move method
			\moveToTail -> \moveToTail,	// this makes sure we don't choke
			\moveAfter -> \moveAfter,
			\moveBefore -> \moveBefore
		];
		
		
	}
	
	*new { arg type = \eq, freq = 440, k = 1, rq = 1, numChannels = 1, sr = 44100;
		^super.new.initBand(type, freq, k, rq, numChannels, sr);
	}
	
	initBand { arg t, f, gain, recq, numCh, sampRt;
		type = t;
		freq = f;
		k = gain;
		rq = recq;
		numChannels = numCh;
		sampleRate = sampRt;
		this.calc;
		^this
	}
	
		// array comes from EQSpecs parsing algorithm
	*newFromArray { arg a, numChan = 1, sr = 44100;
		^this.new(a.at(0) ? \eq, a.at(1) ? 440, a.at(2) ? 1, a.at(3) ? 1,
			numChan, sr
		)
	}
	
		// place the eq band on the server
	play { arg targ, b, addAct = \addToTail, amp = 1;
		var bundle, groupbus, def;
			// fix target
		target = targ ? target;
		bus = b ? bus;
		addAction = addAct ? addAction ? \addToTail;
		target.isKindOf(Group).not.if({	// target is Group, leave it alone, otherwise...
			target.isKindOf(Synth).if({
				addAction = \addAfter;	// if a synth, place imm. after target
			});
			(groupbus = target.tryPerform(\groupBusInfo, \effect)).notNil.if({
				bus = groupbus[1];	// must use mc bus
				target = groupbus[0];	// place where effects should go
			});
			target = target.asTarget;
		});
		this.server_(target.server);	// update sample rate
		
		bus.isNumber.if({	// send an index, make a bus from it
			bus = Bus.new(\audio, bus, numChannels, target.server);
		});
		
		bus.isNil.if({	// give nothing for bus, use hardware on target's server
			bus = Bus.new(\audio, 0, numChannels, target.server);
		});
		
		mul = amp;

		synth.isNil.if({		// make new synth, else move it using action and target
			def = this.makeSynthDef;

			synth = Synth.basicNew("EQ/"++type.asString++numChannels, target.server);
			
			bundle = [\d_recv, def.asBytes, synth.newMsg(target,
				this.calc.synthArgs ++ [\outbus, bus.index, \mul, mul], addAction)];
			target.server.sendBundle(nil, bundle);
		}, {
			synth.perform(translateMethods.at(addAction), target);
		});
	}
	
	run { |flag = true|
		synth !? { synth.run(flag) }
	}
	
		// stop the eq, free server resources
	free { arg updateGUI = true;
		synth.notNil.if({ synth.free; });
		synth = nil;
		updateGUI.if({ gui.free(false) });
	}
	
	mul_ { arg m;
		mul = m;
		synth.notNil.if({ synth.set([\mul, mul]); });
	}

	type_ { arg t, updateGUI = true;
		var bundle, def;
		type = t;
		
			// if type changes, synth node must be replaced
		bundle = List.new;
		synth.notNil.if({
			def = this.makeSynthDef;
			bundle = [synth.freeMsg];
			synth = Synth.basicNew("EQ/"++type.asString++numChannels, target.server);
			bundle = bundle.add([\d_recv, def.asBytes, synth.newMsg(target,
				this.calc.synthArgs ++ [\outbus, bus.index, \mul, mul], addAction)]);
			target.server.listSendBundle(nil, bundle);
		});
		(gui.notNil && updateGUI).if({
			gui.update;
		});
	}
	
	freq_ { arg f, updateGUI = true;
		freq = f;
		this.calc;
		synth.notNil.if({		// if the synth is on, give it the new values
			synth.server.sendBundle(nil, [15, synth.nodeID]++this.synthArgs);
		});
		(gui.notNil && updateGUI).if({
			gui.update;
		});
	}
	
	k_ { arg newk, updateGUI = true;
		k = newk;
		this.calc;
		synth.notNil.if({
			synth.server.sendBundle(nil, [15, synth.nodeID]++this.synthArgs);
		});
		(gui.notNil && updateGUI).if({
			gui.update;
		});
	}
	
	rq_ { arg r, updateGUI = true;
		rq = r;
		this.calc;
		synth.notNil.if({
			synth.server.sendBundle(nil, [15, synth.nodeID]++this.synthArgs);
		});
		(gui.notNil && updateGUI).if({
			gui.update;
		});
	}
	
	makeSynthDef {
		^SynthDef("EQ/" ++ type ++ numChannels, { |outbus, mul = 1, add = 0|
			var	sig = In.ar(outbus, numChannels);
			ReplaceOut.ar(outbus, SynthDef.wrap(StaticEQ.eqFuncs[type], nil, [sig]));
		})
	}
}
