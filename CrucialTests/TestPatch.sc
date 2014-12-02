
TestPatch : TestAbstractPlayer {

	var p,i;

	makePlayer {
		// patch
		i = Instr("help-Patch",{ arg freq=100,amp=1.0;
				SinOsc.ar([freq,freq + 30],0,amp)
			});
		p = Patch(i,[ 500,	0.3 ]);
		^p
	}
	makeBus {
		^Bus.audio(s,2)
	}
	
	//	AbstractPlayer.bundleClass = MixedBundleTester;
	//	MixedBundleTester.reset;
	//	InstrSynthDef.clearCache(Server.default);
	//}

	test_play {
		p.play;
		this.wait( {p.isPlaying},"wait for patch to play");
		0.3.wait;
		p.stop;
		this.wait( {p.isPlaying.not},"waiting for patch to stop playing");
		
		// no longer true
		//this.assert( p.readyForPlay,"patch should still be ready for play");
		
		p.free;
		this.wait( {p.isPrepared.not},"after free, patch should not be ready for play");

		//0.2.wait;// grr, have to wait for the bundles to be really sent, and they still aren't ?
		//this.assertEquals(MixedBundleTester.bundlesSent.size,2,"should be only two bundles sent: prepare/play and stop");
		//MixedBundleTester.bundlesSent.insp;
	}
	
	test_prepare {
		p.prepareForPlay;
		
		this.wait( {p.isPrepared},"wait for patch to be ready");
		
		p.play;
		this.wait( {p.isPlaying},"wait for patch to play");

		p.free;
		this.wait({ p.isPrepared.not},"wait for patch to be un-ready after free");

		//p.stop;
		//this.wait( {p.isPlaying.not},"waiting for patch to stop playing");
		
		// no longer true
		//this.assert( p.readyForPlay,"patch should still be ready for play");
		
		//p.free;
		//this.wait( {p.readyForPlay.not},"after free, patch should not be ready for play");
	}
	test_gui {
		var s;
		Instr.clearAll;
		Instr("sin",{SinOsc.ar});
		{
			s = Sheet({ arg f;
				Patch("sin").gui(f);
				Patch("sin").gui(f);
			});
			s.close;
		}.defer;
		this.wait( { s.isClosed },"waiting for window to close");
		this.assertEquals( Instr.leaves.size,1,"should only be one instr in the lib");
	}
	test_argsSetter {
		var p,k,l;
		p = Patch({ arg freq; SinOsc.ar(freq) });
		k = KrNumberEditor(440.0,\freq);
		p.args = [k];
		this.assert( p.args[0] === k, "arg should be set with KrNumberEditor");
		this.assert( p.argsForSynth[0] === k, "arg should be set with KrNumberEditor");
	}
	test_krNumberEditor {
		var k,spo;
		p = Patch(i,[ 
				k = KrNumberEditor(440,\freq)
			,	0.3 ]);
		p.play;
		this.wait({p.isPlaying},"wait patch playing");
		0.5.wait;
		spo = p.patchIns.first.connectedTo;
		this.assert(spo.notNil,"connected to UpdatingScalarPatchOut");
		this.assert(spo.source === k,"connected to KrNumberEditor");
	}
	test_irNumberEditor {
		var ine,spo;
		p = Patch(i,[

			ine = IrNumberEditor(440,\freq),
			1.0

		]);
		p.play;
		this.wait({p.isPlaying},"wait patch playing");
		spo = p.patchIns.first;
		this.assert(spo.notNil,"control patchIn for IrNumberEditor");
		this.assertEquals( p.synthDef.controls.size,2,"synth def controls");
	}
	test_startStopStart {
		this.startStopStart;
	}
	test_subpatch {
		var p;
		p = Patch({ arg input;
				RLPF.ar(input,400)
			},[
				Patch({
					Saw.ar.dup
				})
			]);
		
		player = p;
		
		this.startStopStart;
		
		
	}
	test_playerPool {
		var players,pp;
		players = Array.fill(3,{ Patch({ SinOsc.ar(rrand(100,200).dup) }) });
		pp = PlayerPool.new(players,rate:\audio,numChannels:2);
		
		player = Patch({ arg audio;
					RLPF.ar(audio,400)
				},[
					pp
				]);
		
		this.startStopStart;
	}
		
	/*
	test_patchInPatch {
		
		Patch({
			Patch({ |audio|
				RLPF.ar(audio,400,0.2)
			},[
				Patch({ Saw.ar })
			]).ar;
	
		}).play	

		Patch({

			Patch({ Saw.ar }).ar
	
		}).play	

	}
	*/
}

