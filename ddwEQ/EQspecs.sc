
MultiEQ {	// container for specifications for an EQ bank
	// creates an individual synthdef for each band and allows placing on server	// H. James Harkins, Jan 2003 - jamshark70@dewdrop-world.net	// based on Joseph Anderson's Regalia-Mitra EQ classes	// ftp://ftp.music.washington.edu/Public/jla/RM_Filters.sit		classvar	numArgs = 4,	// must equal the number of eq parms: type, freq, k, rq			<>sampleRate = 44100;
		var	<spec,	// List of EQBand		<numChannels,
		<target, <bus, <addAction, <mul = 1,
		<isPlaying = false,
		<>editor;
		*new {
			  // takes an argument list; .init calls .parseArgs to parse the list		arg numChan = 1 ... z;				^super.new.init(numChan, z)	}		init { arg numChan ... z;
		numChannels = numChan;		spec = this.parseArgs(z);	}		parseArgs {  // turn argument list into array of parameter arrays		arg ... z;	// argument list; may or may not be wrapped in an array				var oneSpec, allSpecs;		z = z.flat;	// remove all inner array wrapping		(z.size == 0).if({			^List.new		// empty list for no specs		}, {			allSpecs = List.new;		// initialize allSpecs						z.do({ arg item;	// iterate over arguments				item.isSymbol.if({	// a symbol indicates a new spec
						// if oneSpec is nil, this is the first one so don't add
					oneSpec.isNil.if(nil,						{ 		// else add it
							allSpecs.add(
								EQBand.newFromArray(oneSpec, numChannels, sampleRate)
									.parent_(this)
							);
						}					);					oneSpec = List.with(item);	// then initialize new spec				}, {					oneSpec.add(item);		// other types, add to onespec				});			});						^allSpecs.add(
				EQBand.newFromArray(oneSpec, numChannels, sampleRate)
					.parent_(this)
			);  // add the last spec			});	}		add {		// add one or more filters		arg ... z;		// same syntax for arglist as .new		var newbands;
		
		newbands = this.parseArgs(z);
		isPlaying.if({
			newbands.do({ arg b, i;
				b.play(target, bus, addAction, 
					((spec.size == 1) && (i == 0)).if(mul, 1)
				);
			});
		});
		spec = spec ++ newbands;
				^this	}
	
	remove { arg band, updateGUI = true;
		var i;
		i = spec.indexOf(band);
		i.notNil.if({ this.removeAt(i, updateGUI) });
	}		removeAt {		// remove a filter		arg i, updateGUI;	// index to filter
		var band;
		
		band = spec.removeAt(i);
		band.free(updateGUI);

		^this	}		at {		// index a filter; returns one band		arg i;		^spec.at(i)	}		play { arg targ, b, addAct = \addToTail, amp = 1;
		target = targ;
		bus = b;
		addAction = addAct ? addAction ? \addToTail;
		spec.do({ arg sp, i;
			sp.play(target, bus, addAction, (i == 0).if(amp, 1));
		});
//		this.mul_(amp ? 1);
		isPlaying = true;
	}
	
	free {
		editor.notNil.if({ editor.w.close });
		spec.do({ arg sp; sp.free });
		isPlaying = false;
	}
	
	asString { 		var out;		out = "MultiEQ.new(" ++ numChannels ++ ", ";   // initialize string		spec.do( {		// loop through filters			arg s, i;			(i > 0).if({ out = out ++ ", " });  // skip comma for first eq			out = out ++ "\\" ++ s.type				++ ", " ++ s.freq.round(0.001)				++ ", " ++ s.k.ampdb.round(0.001) ++ ".dbamp"				++ ", " ++ s.rq.round(0.001);		});		^(out ++ ")")	}		edit { arg targ, b, addAct = \addToTail, amp = 1;	// just like .play
			// assumes your synth or process is already playing
		isPlaying.not.if({ this.play(targ, b, addAct, amp); });  // should be playing to edit
		MultiEQGUI.new(this, 
			targ.tryPerform(\name) ++ " EQ Editor",
			\edit);	// gui needs to know I called, so it can
					// stop the eq on close (& target?)
			// when window closes, edited specs will be printed
	}

	gui { MultiEQGUI.new(this, target.tryPerform(\name) ++ " EQ Editor") }
}


StaticEQ {
	// make a static synthdef out of given specs
	
	classvar	<eqFuncs,
			<>sampleRate = 44100;
	
	var	<spec,
		<defname,
		<numChannels,
		<target, <bus,
		<mul,		// volume scaling after eq
		<synth,
		<readyForPlay = false,
		<mixer;
	
	*initClass {
			// each func takes "in" and outputs eq'ed signal (1 band)
		eqFuncs = IdentityDictionary[
			\eq -> { arg in, k, freq, rq;
				MidEQ.ar(in, freq, rq, k.ampdb)
			},
			\lopass -> { arg in, k, freq;
				LPF.ar(in, freq)
			},
			\hipass -> { arg in, k, freq;
				HPF.ar(in, freq)
			},
			\loshelf -> { arg in, k, b1;
				var allpass;
				allpass = FOS.ar(in, b1.neg, 1, b1, k.neg.sign);
				0.5 * (in + allpass + (k.abs * (in-allpass)))
			},
			\hishelf -> { arg in, k, b1;
				var allpass;
				allpass = FOS.ar(in, b1.neg, 1, b1, k.sign);
				0.5 * (in + allpass + (k.abs * (in-allpass)))
			}
		];
	}
	
	*new {
		arg numChan = 1 ... z;
		^super.new.init(numChan, z)
	}
	
	init { arg numChan ... z;
		numChannels = numChan ? 1;
		spec = MultiEQ.new(numChan, z).spec;
	}
	
	prepareForPlay { arg targ, b, addAction = \addToTail;
		this.fixTarget(targ, b, addAction);
			// will this choke if completionMsg is nil?
		this.asSynthDef.send(target.server);
		
		readyForPlay = true;
	}
	
	play { arg targ, b, addAction = \addToTail, amp = 1;
			// no provision for changing synth--
			// if you need to change, use DynMultiEQ
		var bundle;
				// if already playing and not moving, do nothing
		(synth.notNil && targ.isNil).if({ ^this });
				// now we know targ is not nil, so we're moving
		this.fixTarget(targ, b, addAction);
		spec.do({ |sp| sp.server = target.server });	// update sample rate
		synth.notNil.if({
			synth.perform(EQBand.translateMethods.at(addAction), target);
			synth.set(\bus, bus.index);
			^this	// go back
		});

			// making a new synth
		bundle = List.new;
		mul = amp ? mul ? 1;

			// if it's ready for play now, send the s_new message
		readyForPlay.if({
			synth = Synth.basicNew(defname, target.server);
			bundle.add(synth.newMsg(target, [\bus, bus.index, \mul, mul], addAction));
			target.server.listSendBundle(nil, bundle);
		}, {		// if it's not ready, make the synthdef, send it, and create the synth when done
			defname = this.makeSynthName;
			synth = Synth.basicNew(defname, target.server);
			bundle.add(synth.newMsg(target, [\bus, bus.index, \mul, mul], addAction));
			this.asSynthDef.send(target.server, bundle.at(0));
		});
		
			// add this to the list of patches to free when mixerchannel frees
		if(mixer.notNil) {
			mixer.addPatch(this);
		}	
	}
	
	mul_ { arg amp;
		mul = amp ? mul ? 1;
		synth.notNil.if({ synth.set(\mul, mul) });
	}
	
	free {
		synth.notNil.if({ synth.free; });
		synth = nil;
			// don't keep a garbage synthdef on the server
		target.server.sendMsg(\d_free, defname);
		readyForPlay = false;
	}
	
	stop { this.free }	// mixerchannel auto-free needs this synonym
	
	move { arg targ, moveAction = \moveAfter;
		synth.notNil.if({
			target = targ.asTarget;
			synth.perform(moveAction, target);
		});
	}

	fixTarget { arg targ, b, addAction = \addToTail;
		var	groupbus;
	
		target = targ ? target;
		bus = b;

		target.isKindOf(Group).not.if({	// target is Group, leave it alone, otherwise...
			target.isKindOf(Synth).if({
				addAction = \addAfter;	// if a synth, place imm. after target
			});
			(groupbus = target.tryPerform(\groupBusInfo, \effect)).notNil.if({
				mixer = target;
				bus = groupbus[1];	// must use mc bus
				target = groupbus[0];	// place where effects should go
			});
			target = target.asTarget;
		});
		
		bus.isNumber.if({	// send an index, make a bus from it
			bus = Bus.new(\audio, bus.asInteger, numChannels, target.server);
		});
		
		bus.isNil.if({	// give nothing for bus, use hardware on target's server
			bus = Bus.new(\audio, 0, numChannels, target.server);
		});
	}	
	
	asSynthDef { arg numChan = 1;
		defname = defname ? this.makeSynthName;
		^SynthDef.new(this.defname, {
			arg bus, mul;
			var sig;
			sig = In.ar(bus, numChannels);
			spec.do({ arg sp;
				sig = eqFuncs.at(sp.type).value(sig, sp.k, sp.a0, sp.a1);
			});
			ReplaceOut.ar(bus, sig * mul);
		});
	}
	
	makeSynthName {
		var str;
		str = this.hash.asString;  // or 1.0.rand.asString?
		str.do({ arg s, i; (s == $.).if({ str.put(i, $-) }); });
		^("EQ" ++ str)
	}
	
}
