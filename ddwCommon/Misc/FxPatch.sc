
PatchNoDep : Patch {
//	setInput { arg ai, ag;
//		//ISSUE if it wasn't a synth input before it won't become one now
//		// but you can respawn
//		var synthArgi;
//		args.put(ai,ag);
//		synthArgi = synthArgsIndices.at(ai);
//		if(synthArgi.notNil,{
//			argsForSynth.put(synthArgi,ag);
//		});
//	}
//	createArgs { arg argargs;
//		this.prCreateArgs(argargs, false);	// do not create dependants for args
//	}
//	
//	watchScalars {}
}

	// I may revert this to Patch later?
FxPatch : PatchNoDep {

	*initClass {
		StartUp.add({
			Spec.specs.putAll(IdentityDictionary[
				\gain2 -> [0.000001, 10, \exponential, 0, 1].asSpec,
				\slope -> [0.1, 10, \exponential, 0, 1].asSpec,
				\myrq -> [1, 0.05].asSpec,
				\mydetune -> [0.95, 0.95.reciprocal, \exponential, 0, 1].asSpec,
				\mybuf -> [0, 128, \linear, 1, 0].asSpec,
				\freqlag -> [0.00001, 20, \exponential].asSpec
			]);
		});
	}

	asSynthDef {
		// could be cached, must be able to invalidate it
		// if an input changes
		^synthDef ?? {
			synthDef = InstrSynthDef.build(this.instr,this.args,ReplaceOut, this);
			defName = synthDef.name;
			numChannels = synthDef.numChannels;
			rate = synthDef.rate;
			synthDef
		}
	}
	
	playToMixer { arg m, atTime = nil, callback;
		var def, busArgIndex;
			// find bus arg and set to mixer's bus
		def = this.asSynthDef;

		if(synthDef.numChannels > m.inChannels) {
			"Playing a %-channel patch on a %-input mixer. Output may be incorrect."
				.format(synthDef.numChannels, m.inChannels).warn;
		};

			// is it a kr argument?
		busArgIndex = instr.argsAndIndices[\bus];

			// busArgIndex may be nil, in which case the [] will fail; hence try-catch
		{	args[busArgIndex].respondsTo(\value_).if({
				args.at(busArgIndex).value = m.inbus.index;
			}, {
				args.put(busArgIndex, m.inbus.index);
			});
		}.try({
			"Your fx Instr doesn't have a 'bus' argument. Results are unpredictable.".warn;
		});

		synthDef = nil;	// clear synthdef so it's rebuilt with new args
		
		this.play(m.effectgroup, atTime,
			SharedBus(def.rate, m.inbus.index, numChannels, m.server), callback);
	}
}
