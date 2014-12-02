
+ Instr {
	miditest { arg channel = 0, initArgs, target, bus, ctlChannel, makeVoicerFunc;
		var voicer, socket, patch, synthdef, layout, argsSize, parg, i, argNames;
		
		var close = {
			var	gctemp;
			voicer.active.if({
				"\n\nTest ended. Last settings of global controls:".postln;
					// must free midi controls in reverse order
					// globalControls is an IdentityDictionary, so this is the only way
				gctemp = Array.new(voicer.globalControls.size);
	
				voicer.globalControlsByCreation.do({ arg gc;
					gctemp.add(gc);
					Post << $\\ << gc.name << ", " << gc.value << ", \n";
				});
				gctemp.reverseDo({ |gc| gc.midiControl.free });
					// prevent infinite recursion
				layout.onClose = nil;
				layout.close;
				socket.free;	// clear and garbage objects
				voicer.free;
				socket = nil;
				voicer = nil;
			});
		};

		// MIDIPort.autoFreeSockets.not.if({
		// 	MethodError("MIDIPort.autoFreeSockets must be true to use miditest.", this).throw;
		// });

		channel = channel ? 0;
			// can have controllers on a different midi channel (or different device)
		ctlChannel = ctlChannel ? channel;
		
			// make the player
		voicer = makeVoicerFunc.(this, initArgs, target: target, bus: bus)
			?? { Voicer.new(20, this, initArgs, target: target, bus: bus) };
		socket = VoicerMIDISocket.new(channel, voicer);  // plug into MIDI

			// make guis for controls other than freq and gate
			// first get the InstrSynthDef
			// how to exempt args? provide non-SimpleNumber in initArgs
			// (Ref of SimpleNumber works to make fixed arg)
		patch = voicer.nodes.at(0).patch;
		synthdef = patch.asSynthDef;
		argNames = patch.argNames ?? { this.argNames };

			// now make midi controllers, but only for kr inputs
			// we have to go to the synthdef because only it knows which are noncontrols
		synthdef.allControlNames.do({ |cname|
			(cname.rate == \control and:
				{ #[\freq, \gate, \out].includes(cname.name).not }
			).if({
				i = argNames.detectIndex({ |name| name == cname.name });
					// you might have added NamedControls in the Instr func
					// which will not have specs and shouldn't turn into midi controls
				if(i.notNil) {
					parg = patch.args[i];
					socket.addControl(
						nil,		// socket will allocate a controller for me
						argNames[i],
						(name == \pb).if(1, patch.args[i].value),
//							(name == \pb).if(
//								[7.midiratio.reciprocal, 7.midiratio, \exponential, 0, 1],
//								parg.tryPerform(\spec) ?? { patch.argSpecs[i] }),
						parg.tryPerform(\spec) ?? { patch.argSpecs[i] },
						ctlChannel
					);
				};
			});
		});
			// make stop button
		voicer.addProcess([["Stop test", close]], \toggle);  // so it's a button not a menu

		layout = voicer.gui.masterLayout;
		layout.onClose = close;
		
		// now user can play
		"\n\nTry your Instr using your midi keyboard. Arguments have been routed as shown".postln;
		"in the console window.".postln;
		^voicer
	}
	
	miditestMono { arg channel = 0, initArgs, target, bus, ctlChannel;
		^this.miditest(channel, initArgs, target, bus, ctlChannel, { |instr, initArgs, target, bus|
			MonoPortaVoicer(1, instr, initArgs, bus, target)
		});
	}
	
	openFile {
		this.path.openTextFile;
	}
	
}


// for SynthDef
// similar except Instr-miditest gets its specs from the Instr
// here, you have to supply the specs in the miditest message
// specs should be given as in Instr: an array each element of which corresponds to an arg in order
// nil specs go to ControlSpecs.specs to look by name
// to give a default value for a midi control, put it in the spec

+ SynthDef {
	miditest { arg channel = 0, specs, target, bus, ctlChannel, makeVoicerFunc;
		var voicer, socket, patch, spec, name, layout, cnames, md;
		
		var close = {
			var gctemp;
			voicer.active.if({
				"\n\nTest ended. Last settings of global controls:".postln;
				gctemp = Array.new(voicer.globalControls.size);
	
				voicer.globalControlsByCreation.do({ arg gc;
					gctemp.add(gc);
					Post << $\\ << gc.name << ", " << gc.value << ", \n";
				});
				gctemp.reverseDo({ |gc| gc.midiControl.free });

					// prevent infinite recursion
				layout.onClose = nil;
				layout.close;
				socket.free;	// clear and garbage objects
				voicer.free;
				socket = nil;
				voicer = nil;
			});
		};

		// MIDIPort.autoFreeSockets.not.if({
		// 	MethodError("MIDIPort.autoFreeSockets must be true to use miditest.", this).throw;
		// });

		channel = channel ? 0;
		ctlChannel = ctlChannel ? channel;

		this.send(target.asTarget.server);	// tell the server about me
		
			// make the player
		voicer = makeVoicerFunc.(this.name, #[], target: target, bus: bus)
			?? { Voicer.new(20, this.name, target: target, bus: bus) };
		socket = VoicerMIDISocket.new(channel, voicer);  // plug into MIDI
		
			// make guis for controls other than freq and gate
			// first get the InstrSynthDef
			// how to exempt args? provide non-SimpleNumber in initArgs
		
			// now make midi controllers
			// use (midiControls: #[name0, name1...]) to choose which controls get midified
			// otherwise it's all of them
		md = this.metadata ?? { () };
		cnames = md[\midiControls] ?? { this.allControlNames.collect(_.name) };
		if(md[\specs].notNil and: { specs.isNil }) {
			specs = this.allControlNames.collect { |cname|
				var temp = md[\specs][cname.name.asSymbol];
				temp.tryPerform(\asSpec) ?? { temp }
			};
		} {
			specs ?? { specs = [] };
		};
		this.allControlNames.do({ arg cn;
			var i;
				// freq and gate must be omitted
			name = cn.name.asSymbol;
			i = this.allControlNames.detectIndex { |cn| name == cn.name.asSymbol };
			#[\freq, \gate, \outbus].includes(name).not.if({
				specs[i].respondsTo(\asSpec).if({
					spec = specs.at(cn.index).asSpec ?
						ControlSpec.specs.at(name) ?
						ControlSpec.new;	// this is probably not 100% robust
				}, {
					spec = specs[i];
				});
				(spec.rate != \scalar and: { cnames.includes(name) }).if({
					socket.addControl(
						nil,		// let Socket find me a controller
						name,
						(name == \pb).if(1, spec.default),
						(name == \pb).if(
							[7.midiratio.reciprocal, 7.midiratio, \exponential, 0, 1],
							spec),
						ctlChannel
					);
				}, {
					if(spec.notNil) {
						voicer.setArgDefaults([name, spec.tryPerform(\default) ?? { spec }])
					};
				});
			});
		});
		
			// make stop button
		voicer.addProcess([["Stop test", close]], \toggle);  // so it's a button not a menu

		layout = voicer.gui.masterLayout;
		layout.onClose = close;
		
		// now user can play
		"\n\nTry your SynthDef using your midi keyboard. Arguments have been routed as shown".postln;
		"in the console window.".postln;
		^voicer
	}

	miditestMono { arg channel = 0, specs, target, bus, ctlChannel;
		^this.miditest(channel, specs, target, bus, ctlChannel, { |instr, initArgs, target, bus|
			MonoPortaVoicer(1, instr, initArgs, bus, target)
		});
	}
}

