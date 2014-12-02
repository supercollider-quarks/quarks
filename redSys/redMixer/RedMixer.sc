//redFrik

//--related:
//RedMixerGUI RedMixerChannel RedEffectsRack

//--todo:
//solution to redefx/redmixer order
//mono?, swap RedMixerChannels on the fly possible?, or stereo/mono switch for all channels?, quad?
//multichannel, generalise - now RedMixerChannel only stereo, remake as mono-quad channels

RedMixer {
	var <group, <cvs, <isReady, groupPassedIn,
		<channels, <mixers,
		internalSynths, internalInputChannels, internalOutputChannels,
		synthDef;
	*new {|inputChannels= #[[2, 3], [4, 5], [6, 7], [8, 9]], outputChannels= #[[0, 1]], group, lag= 0.05|
		^super.new.initRedMixer(inputChannels, outputChannels, lag).init(group);
	}
	initRedMixer {|argInputChannels, argOutputChannels, lag|
		
		//--create cvs
		cvs= (
			\lag: CV.new.spec_(ControlSpec(0, 99, 'lin', 0, lag))
		);
		cvs.lag.action= {|v|
			mixers.do{|x| x.cvs.lag.value= v.value};
			channels.do{|x| x.cvs.lag.value= v.value};
		};
		
		//--temp storage to avoid channel arguments for init
		internalInputChannels= argInputChannels;
		internalOutputChannels= argOutputChannels;
	}
	init {|argGroup|
		var server;
		isReady= false;
		if(argGroup.isNil, {
			server= Server.default;
			groupPassedIn= false;
		}, {
			server= argGroup.server;
			groupPassedIn= true;
		});
		
		Routine.run{
			if(groupPassedIn.not, {
				server.bootSync;
				group= Group.after(server.defaultGroup);
				server.sync;
				CmdPeriod.doOnce({group.free});
			}, {
				group= argGroup;
			});
			
			//--create mixers
			if(mixers.isNil, {		//when new
				mixers= internalOutputChannels.collect{|x|
					RedMixerChannel(x, group, cvs.lag.value);
				};
			}, {	//when recreating from written archive
				mixers.do{|x| x.init(group)};
			});
			
			//--internal synth for routing from channels to mixers
			if(synthDef.isNil, {	//when new
				synthDef= this.def(internalInputChannels);
			});
			synthDef.send(server);
			server.sync;
			if(internalSynths.isNil, {	//when new
				internalSynths= internalOutputChannels.collect{|x|
					Synth(\redMixerInternalRouting, [\out, x[0]], group);
				};
			}, {	//when recreating from written archive
				internalSynths= this.outputChannels.collect{|x|
					Synth(\redMixerInternalRouting, [\out, x], group);
				};
			});
			
			//--create channels
			if(channels.isNil, {	//when new
				channels= internalInputChannels.collect{|x|
					RedMixerChannel(x, group, cvs.lag.value);
				};
			}, {	//when recreating from written archive
				channels.do{|ch|
					var inserts= ch.inserts.deepCopy;
					ch.init(group);
					inserts.do{|efx|
						// note: the fxs are added in the same order they were stored,
						// (independently of \addToHead or \addToTail)
						// reading them in the order they appear in redMixer.channel[i]
						// and adding them to the channel with \addToTail !
						ch.insertClass(efx.class, \addToTail);
					};
					ch.inserts.do{|efx, i|
						inserts[i].cvs.keysValuesDo{|k, v|
							efx.cvs[k].value= v.value;
						};
					};
				};
			});
//			while({channels.any{|x| x.isReady.not}}, {0.02.wait});
			isReady= true;
		};
	}
	mute {|channel|
		if(channel.isKindOf(Boolean), {
			channels.do{|x| x.mute(channel)};
		}, {
			channel.asArray.do{|x|
				channels[x].mute(true);
			};
		});
	}
	solo {|channel|	
		if(channel==false, {
			this.mute(false);
		}, {
			channels.do{|x, i|
				x.mute(i!=channel);
			};
		});
	}
	mixer {
		^mixers[0];
	}
	free {
		if(groupPassedIn.not, {group.free});
		mixers.do{|x| x.free};
		channels.do{|x| x.free};
		internalSynths.do{|x| x.free};
	}
	defaults {
		channels.do{|x| x.defaults};
		mixers.do{|x| x.defaults};
		cvs.do{|cv| cv.value= cv.spec.default};
	}
	gui {|position|
		^RedMixerGUI(this, position);
	}
	
	inputChannels {
		^channels.collect{|x| x.cvs.out.value};
	}
	inputChannels_ {|arr|
		if(channels.size!=arr.size, {
			(this.class.name++": array must match number of channels").error;
		}, {
			channels.do{|x, i|
				x.cvs.out.value= arr[i][0];
			};
			internalSynths.do{|x|
				x.set(\inputs, arr.collect{|y| y[0]});
			};
		});
	}
	outputChannels {
		^mixers.collect{|x| x.cvs.out.value};
	}
	outputChannels_ {|arr|
		if(internalSynths.size!=arr.size, {
			(this.class.name++": array must match outputChannels argument").error;
		}, {
			arr.do{|x, i|
				mixers[i].cvs.out.value= x[0];
				internalSynths[i].set(\out, x[0]);
			};
		});
	}
	def {|inputChannels= #[[2, 3], [4, 5], [6, 7], [8, 9]]|
		^SynthDef(\redMixerInternalRouting, {|out= 0|
			var c= Control.names(\inputs).kr(inputChannels.collect{|x| x[0]});
			var z= inputChannels.collect{|x, i| In.ar(c.asArray[i], x.size)};
			Out.ar(out, Mix(z));
		});
	}
	
	// RL 2012
	store {|path|
		this.writeArchive(path);
	}
	// RL 2012
	*restoreFile {|path|	//should call .init afterwards to initialize server side
		^Object.readArchive(path);
	}
}
