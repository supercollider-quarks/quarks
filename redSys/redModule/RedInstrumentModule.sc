//redFrik

//--related:
//RedAbstractSampler
//RedInstrumentModule.subclasses
//RedEffectModule.subclasses

RedInstrumentModule : RedAbstractModule {		//abstract class
	var <voices;
	
	prepareForPlay {|server|
		group= group ?? {server.defaultGroup};
		voices= List.new;
	}
	free {
		RedAbstractModule.all.remove(this);
		this.stopAll;
	}
	gui {|parent, position|
		^RedInstrumentGUI(parent, position);
	}
	
	//--for all instruments
	play {|key, args, addAction|
		if(addAction.isNil, {
			addAction= defaultAddAction;			//is by default \addToHead
		});
		if(args.isNil, {
			args= this.args;
		}, {
			args= this.args.copy++args;				//override cvs by adding args last to array
		});
		voices.add(key -> Synth.controls(this.def.name, args, group, addAction));
	}
	stop {|key|
		var i;
		if(key.isNil and:{voices.size>0}, {
			voices.removeAt(0).value.release;
		}, {
			i= voices.detectIndex{|x| x.key==key};
			if(i.notNil, {
				voices.removeAt(i).value.release;
			}, {
				(this.class.name++": couldn't find key"+key).warn;
			});
		});
	}
	stopAll {
		voices.do{|x| x.value.release}.clear;
	}
	synth {|key|
		^voices.detect{|x| x.key==key}.value;
	}
	
	//--for subclasses
	*def {^this.subclassResponsibility(thisMethod)}
}
