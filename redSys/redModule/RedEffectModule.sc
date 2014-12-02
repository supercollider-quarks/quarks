//redFrik

//--related:
//RedEffectsRack RedAbstractMix RedEfxRing
//RedEffectModule.subclasses
//RedInstrumentModule.subclasses

RedEffectModule : RedAbstractModule {					//abstract class
	var <synth, internalGroup;
	
	prepareForPlay {|server|
		if(group.isNil, {
			internalGroup= Group.after(server.defaultGroup);
			CmdPeriod.doOnce({internalGroup.free});
			group= internalGroup;
		});
		synth= Synth.controls(this.def.name, args, group, defaultAddAction);
	}
	free {
		RedAbstractModule.all.remove(this);
		synth.free;
		internalGroup.free;
	}
	gui {|parent, position|
		^RedEffectModuleGUI(this, parent, position);
	}
	
	//--for subclasses
	*def {^this.subclassResponsibility(thisMethod)}
}

/*
//effect module template...

RedEfxTemp : RedEffectModule {
	*def {
		^SynthDef(\redEfxTemp, {|out= 0, mix= -1, amount= 1|
			var dry, wet;
			dry= In.ar(out, 2);
			wet= dry*???;
			ReplaceOut.ar(out, XFade2.ar(dry, wet, mix));
		}, metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\mix: ControlSpec(-1, 1, 'lin', 0, -1),
				\amount: ControlSpec(0, 999, 'lin', 0, 1.5)
			),
			order: [
				\out -> \tempOut,
				\mix -> \tempMix,
				\amount -> \tempAmount
			]
		));
	}
}

*/
