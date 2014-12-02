//redFrik

//--todo:
//multichannel, generalise - now RedMixerChannel only stereo, remake as mono-quad channels

//--related:
//RedMixerChannelGUI RedMixer RedGUICVMixerChannel RedAbstractMix RedAbstractModule

RedMixerChannel {
	var <group, <cvs, /*<isReady= false,*/
		<inserts, muteVol,
		synth, synthEq,
		osc, oscCVS,
		<args;
	*new {|outputChannels= #[0, 1], group, lag= 0.05|
		^super.new.initRedMixerChannel(outputChannels, lag).init(group);
	}
	initRedMixerChannel {|outputChannels, lag|
		
		//--create cvs and argument array
		cvs= (
			\out: CV.new.spec_(\audiobus.asSpec.copy.default_(outputChannels[0])),
			\lag: CV.new.spec_(ControlSpec(0, 99, \lin, 0, lag)),
			\eqHi: CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0)),
			\eqMi: CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0)),
			\eqLo: CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0)),
			\hiFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 7000)),
			\hiBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
			\hiGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0)),
			\miFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 700)),
			\miBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
			\miGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0)),
			\loFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 70)),
			\loBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
			\loGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0)),
			\bal: CV.new.spec_(\bipolar.asSpec),
			\vol: CV.new.spec_(ControlSpec(-80, 20, \lin, 0, 0)),
			\peak0: CV.new.spec_(ControlSpec(0, 1, \lin, 0, 0)),
			\peak1: CV.new.spec_(ControlSpec(0, 1, \lin, 0, 0)),
			\peaked0: CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0)),
			\peaked1: CV.new.spec_(ControlSpec(0, 1, \lin, 1, 0))
		);
		args= [
			\out, cvs.out,
			\lag, cvs.lag,
			\bal, cvs.bal,
			\amp, [cvs.vol, cvs.vol.dbamp]
		];
	}
	init {|argGroup|
		var server;
		synthEq= nil;
		group= argGroup ?? {Server.default.defaultGroup};
		server= group.server;
		
		forkIfNeeded{
			server.bootSync;
			
			//--send definition
			this.def.add;
			this.defEq.add;
			server.sync;
			
			//--create synth
			synth= Synth.controls(this.def.name, args, group, \addToHead);
			server.sync;
			
			oscCVS= [cvs.peak0, cvs.peak1];
			osc= OSCpathResponder(server.addr, ['peaks', synth.nodeID], {|t, r, m|
				m.copyToEnd(3).do{|x, i| oscCVS[i].value= x};
			}).add;
			CmdPeriod.doOnce({osc.remove});
			cvs.peak0.action= {|cv| if(cv.value>=1, {cvs.peaked0.value= 1})};
			cvs.peak1.action= {|cv| if(cv.value>=1, {cvs.peaked1.value= 1})};
			
			cvs.eqHi.action= {|cv| this.prEqAction};
			cvs.eqMi.action= {|cv| this.prEqAction};
			cvs.eqLo.action= {|cv| this.prEqAction};
			this.prEqAction;
			
			inserts= [];
			//isReady= true;
		};
	}
	mute {|boolean|
		if(boolean, {
			if(cvs.vol.input>0, {
				muteVol= cvs.vol.input;
			});
			cvs.vol.input= 0;
		}, {
			if(muteVol.notNil and:{muteVol>0}, {
				cvs.vol.value= muteVol;
			});
		});
	}
	insertClass {|redEfxClass, addAction= \addToHead|
		forkIfNeeded{
			redEfxClass.asArray.do{|x|
				x= x.new(this.out, group);
				group.server.sync;
				if(inserts.isEmpty, {
					if(synthEq.isNil, {
						x.synth.moveBefore(synth);
					}, {
						x.synth.moveBefore(synthEq);
					});
					inserts= inserts.add(x);
				}, {
					if(addAction==\addToHead, {
						x.synth.moveBefore(inserts[0].synth);
						inserts= inserts.addFirst(x);
					}, {
						x.synth.moveAfter(inserts.last.synth);
						inserts= inserts.add(x);
					});
				});
			};
		};
	}
	insert {|redEfx, addAction= \addToHead|
		forkIfNeeded{
			group.server.sync;
			redEfx.asArray.do{|x|
				if(x.synth.isNil, {
					(this.class.name++": could not insert.  redEfx synth not created yet").warn;
				}, {
					x.out= this.out;
					x.group= group;
					if(inserts.isEmpty, {
						if(synthEq.isNil, {
							x.synth.moveBefore(synth);
						}, {
							x.synth.moveBefore(synthEq);
						});
						inserts= inserts.add(x);
					}, {
						if(addAction==\addToHead, {
							x.synth.moveBefore(inserts[0].synth);
							inserts= inserts.addFirst(x);
						}, {
							x.synth.moveAfter(inserts.last.synth);
							inserts= inserts.add(x);
						});
					});
				});
			};
		};
	}
	removeClass {|redEfxClass|
		redEfxClass.asArray.do{|x|
			inserts= inserts.reject{|y| if(y.class==x, {y.free; true}, {false})};
		};
	}
	remove {|redEfx|
		redEfx.asArray.do{|x|
			inserts= inserts.reject{|y| if(y==x, {y.free; true}, {false})};
		};
	}
	removeAll {
		inserts.do{|x| x.free};
		inserts= [];
	}
	defaults {cvs.do{|cv| cv.value= cv.spec.default}}
	out {^cvs.out.value}
	out_ {|index| cvs.out.value= index}
	gui {|parent, position|
		^RedMixerChannelGUI(this, parent, position);
	}
	free {
		this.removeAll;
		synthEq.free;
		synth.free;
		osc.remove;
	}
	resetPeaked {
		cvs.peaked0.value= 0;
		cvs.peaked1.value= 0;
	}
	def {|channels= 2|
		^switch(channels,
			2, {
				SynthDef(\redMixerChannel, {|out= 0, lag= 0.05,
						peakRate= 15, bal= 0, amp= 1|
					var z= In.ar(out, 2);
					var o= Balance2.ar(z[0], z[1], Ramp.kr(bal, lag), Ramp.kr(amp, lag));
					var p= PeakFollower.kr(o);
					SendReply.kr(Impulse.kr(peakRate), 'peaks', p);
					ReplaceOut.ar(out, o);
				}, metadata: (
					specs: (
						\out: \audiobus.asSpec,
						\lag: ControlSpec(0, 99, 'lin', 0, 0.05),
						\peakRate: ControlSpec(0, 60, \lin, 0, 15),
						\bal: \bipolar.asSpec,
						\amp: ControlSpec(0, 1, \lin, 0, 1)
					)
				));
			},
			{(this.class.name++": different num channels todo"+channels).warn}
		);
	}
	defEq {|channels= 2|
		^SynthDef(\redMixerChannelEq, {|out= 0, lag= 0.05,
				eqHi= 0, eqMi= 0, eqLo= 0,
				hiFreq= 7000, miFreq= 700, loFreq= 70,
				hiBand= 1, miBand= 1, loBand= 1,
				hiGain= 1, miGain= 1, loGain= 1|
			var z= In.ar(out, channels);
			var freqHi= Ramp.kr(hiFreq, lag);
			var freqMi= Ramp.kr(miFreq, lag);
			var freqLo= Ramp.kr(loFreq, lag);
			var bandHi= Ramp.kr(hiBand, lag);
			var bandMi= Ramp.kr(miBand, lag);
			var bandLo= Ramp.kr(loBand, lag);
			var gainHi= Ramp.kr(hiGain, lag);
			var gainMi= Ramp.kr(miGain, lag);
			var gainLo= Ramp.kr(loGain, lag);
			z= Mix([
				(z*(1-eqHi))+(BHiPass4.ar(z, freqHi, bandHi, gainHi)*eqHi),
				(z*(1-eqMi))+(BBandPass.ar(z, freqMi, bandMi, gainMi)*eqMi),
				(z*(1-eqLo))+(BLowPass4.ar(z, freqLo, bandLo, gainLo)*eqLo)
			]);
			ReplaceOut.ar(out, z);
		}, #[0, 0, 0.1, 0.1, 0.1], metadata: (
			specs: (
				\out: \audiobus.asSpec,
				\lag: ControlSpec(0, 99, 'lin', 0, 0.05),
				\eqHi: ControlSpec(0, 1, 'lin', 1, 0),
				\eqMi: ControlSpec(0, 1, 'lin', 1, 0),
				\eqLo: ControlSpec(0, 1, 'lin', 1, 0),
				\hiFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 7000)),
				\miFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 700)),
				\loFreq: CV.new.spec_(ControlSpec(20, 20000, \exp, 0, 70)),
				\hiBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
				\miBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
				\loBand: CV.new.spec_(ControlSpec(0.01, 10, \exp, 0, 1)),
				\hiGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0)),
				\miGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0)),
				\loGain: CV.new.spec_(ControlSpec(-40, 20, \lin, 0, 0))
			)
		));
	}
	
	//--private
	prEqAction {
		if([cvs.eqHi, cvs.eqMi, cvs.eqLo].any{|x| x.value==1}, {
			if(synthEq.isNil, {
				synthEq= Synth.controls(\redMixerChannelEq, [
					\out, cvs.out,
					\lag, cvs.lag,
					\eqHi, cvs.eqHi,
					\eqMi, cvs.eqMi,
					\eqLo, cvs.eqLo,
					\hiFreq, cvs.hiFreq,
					\miFreq, cvs.miFreq,
					\loFreq, cvs.loFreq,
					\hiBand, cvs.hiBand,
					\miBand, cvs.miBand,
					\loBand, cvs.loBand,
					\hiGain, [cvs.hiGain, cvs.hiGain.dbamp],
					\miGain, [cvs.miGain, cvs.miGain.dbamp],
					\loGain, [cvs.loGain, cvs.loGain.dbamp]
				], synth, \addBefore);
			});
		}, {
			if(synthEq.notNil, {
				synthEq.free;
				synthEq= nil;
			});
		});
	}
}
