


InstrSpawner : Patch {

	classvar <>latency=0.07;

	var <>deltaPattern,<deltaStream,<delta;
	var streams,sendArray,firstSecretDefArgIndex,spawnTask,<clock,<spawnGroup;

	*new { arg func,args,deltaPattern = 1.0;
		^super.new(func,args).deltaPattern_(deltaPattern)
	}
	storeParamsOn { arg stream;
		var last;
		last = args.size - 1;
		if(this.instr.path.notNil,{
			stream << "(" <<< this.instr.dotNotation << ",[";
		},{
			stream << "(" << this.instr.func.def.sourceCode << ",[";
		});
		args.do({ arg ag,i;
			stream <<< enpath(ag);
			if(i != last,{ stream << "," });
		});
		stream << "]," <<< deltaPattern << ")";
	}

	asSynthDef {
		var initArgs;
		^synthDef ?? {
			initArgs = args.collect({ arg a,i;
						var spec;
						if(a.rate == \stream,{
							// use an Ir to reserve an input into the synth.
							// a Pattern does not know the spec/default of its output
							// but we do.
							spec = this.instr.specs.at(i);
							if(spec.isKindOf(StreamSpec),{
								spec = spec.itemSpec;
							});
							IrNumberEditor(spec.default,spec);
						},{
							a
						})
					});
			synthDef = InstrSynthDef.build(this.instr,initArgs,Out);
			defName = synthDef.name;
			numChannels = synthDef.numChannels;
			rate = synthDef.rate;
			this.watchNoncontrols;
			stepChildren = synthDef.secretObjects;
			synthDef
		}
	}
	makeTask {
		deltaStream.reset; delta = 0.0;
		streams.do({ arg stream; stream.reset });
		spawnTask = Routine({
			var server;
			server = this.server;
			while({
				this.spawnNext;
			},{
				server.sendBundle(latency,sendArray);
				delta.wait // we are running slightly ahead of arrival time
			});
		});
	}
	spawnNext {
		if((delta = this.deltaStream.next(this)).isNil,{ ^false });
		//if(delta < 0.001,{
		//	Error("Delta too fast, stopping." + delta).throw;
		//});
		// only sending secret def args the first time !
		// if sample changes, need to put into sendArray
		streams.do({ arg s,i;
			var val;
			if((val = s.next(this)).isNil ,{
				 ^false
			});
			sendArray.put(i * 2 + 6,val);
		});
		^true
	}
	update { arg changed,changer;
		// one of my noncontrol inputs changed
		if(this.isPlaying,{
			//"one of my inputs changed, need to send sample change etc.".warn;

			synthDef.secretDefArgs.do({ arg newarg,i;
				sendArray.put(i + firstSecretDefArgIndex, newarg);
			})
		},{
			this.invalidateSynthDef;
		})
	}

	makeResourcesToBundle { arg bundle;
		// called by AbstractPlayer prepareToBundle
		spawnGroup = Group.basicNew;
		NodeWatcher.register(spawnGroup);
		this.annotate(spawnGroup,"spawnGroup");
		bundle.add( spawnGroup.addToTailMsg(group) );
	}
	prepareToBundle { arg agroup,bundle,private = false, bus,defWasLoaded = false;
		super.prepareToBundle(agroup,bundle,private,bus,defWasLoaded);

		streams = Array(argsForSynth.size);
		sendArray = Array(argsForSynth.size * 2);
		synthArgsIndices.do({ arg ind,i;
			var input;
			if(ind.notNil,{
				// a pattern or player
				// eg. a ModalFreq would use asStream rather than its synth version
				input = args.at(i);
				if(input.class === Patch and: {input.spec.rate === 'stream'},{
					// still needs to be built
					streams.add(input.value.asStream);
				},{
					streams.add(input.asStream);
				});
				sendArray.add(this.instr.argNameAt(i));
				sendArray.add(nil); // where the value will go
			})
		});
		deltaStream = deltaPattern.asStream;

		sendArray = ["/s_new",defName,-1,1,nil] ++ sendArray;
		firstSecretDefArgIndex = sendArray.size;
		sendArray = sendArray	++ synthDef.secretDefArgs(args)
					++ [\out, this.patchOut.synthArg];

		streams.do({ arg s,i;
			// replace players with their outputs
			if(s.isKindOf(AbstractPlayer),{
				streams.put(i,s.synthArg);
			})
		});
		sendArray.put(4,spawnGroup.nodeID);
		sendArray.put(sendArray.size - 1, this.patchOut.synthArg);

		CmdPeriod.add(this);
	}
	freeResourcesToBundle { arg bundle;
		spawnGroup.freeToBundle(bundle);
		bundle.addFunction({ spawnGroup  = nil; });
		// this happens in .stop too
		bundle.addFunction({ CmdPeriod.remove(this) });
	}
	children { ^args ++ [deltaPattern] }
	prepareChildrenToBundle { arg bundle;
		this.children.do({ arg child;
			child.prepareToBundle(spawnGroup,bundle,true,nil,true)
		});
		stepChildren.do({ arg child;
			child.prepareToBundle(spawnGroup,bundle,true,nil,false);
		});
	}

	spawnToBundle { arg bundle;
		if(patchOut.isNil,{
			Error(thisMethod.asString
				+ "patchOut is nil. This has not been prepared for play." + this).throw;
		});
		this.makeTask;

		if((delta = deltaStream.next).notNil and: {this.spawnNext},{
			this.asSynthDef;// make sure it exists

			this.children.do({ arg child;
				child.spawnToBundle(bundle);
			});
			this.stepChildren.do({ arg child;
				child.spawnToBundle(bundle);
			});
			bundle.add(sendArray);
			bundle.addMessage(this,\didSpawn);
		});
	}

	didSpawn {
		super.didSpawn;
		this.startTask;
	}
	startTask {
		clock = SystemClock;
		clock.sched((delta - latency).max(0.0),spawnTask);
	}

	isPlaying {
		^spawnTask.isPlaying
	}
	didStop {
		super.didStop;
		spawnTask.stop;
		spawnTask = nil;
	}
	didFree {
		CmdPeriod.remove(this);
	}
	cmdPeriod {
		this.didStop;
	}

	guiClass { ^InstrSpawnerGui }
}


InstrGateSpawner : InstrSpawner {

	var <beat=0;

	makeTask {
		deltaStream.reset; delta = 0.0; beat = 0.0;
		streams.do({ arg stream; stream.reset });
		spawnTask = Routine({
			var server;
			server = this.server;
			while({
				beat = beat + delta;
				this.spawnNext;
			},{
				server.sendBundle(latency,sendArray);
				delta.wait // we are running slightly ahead of arrival time
			});
		});
	}

	startTask {
		clock = TempoClock.default;
		// like clock.play without the quantize
		//first event has been sent, sched to the second
		clock.schedAbs(clock.elapsedBeats + (delta - Tempo.secs2beats(latency)), spawnTask);
	}
}


ScurryableInstrGateSpawner : InstrGateSpawner {

	var scurried = 0,stepsToDo = 0;

	scurry { arg by=10;
		if(stepsToDo == 0,{ stepsToDo = by; },{
			"already scurrying".inform;
		});
	}
	makeTask {
		deltaStream.reset; delta = 0.0; beat = 0.0;
		streams.do({ arg stream; stream.reset });
		spawnTask = Routine({
			var server,accumDelta = 0.0;
			server = this.server;
			while({
				beat = beat + delta;
				this.spawnNext;
			},{
				if(scurried < stepsToDo,{
					server.sendBundle( latency + (Tempo.beats2secs( accumDelta ) ), sendArray );
					accumDelta = accumDelta + delta;
					scurried = scurried + 1;
					// last time...
					if(scurried == stepsToDo,{
						stepsToDo = 0;
						scurried = 0;
						accumDelta.wait;
						accumDelta = 0.0;
					});
					// go round again
				},{
					// play as normal
					server.sendBundle(latency, sendArray);
					delta.wait // we are running slightly ahead of arrival time
				});
			});
		});
	}
}

