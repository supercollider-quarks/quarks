
TestPlayerSocket : UnitTest {
	
	var s,b,p,g,n;
	
	setUp {
		s = Server.default;
		this.bootServer;
		InstrSynthDef.clearCache(Server.default);
		MixedBundleTester.reset;
		//AbstractPlayer.bundleClass = MixedBundleTester;

		p = Patch("oscillOrc.saw",[
				440,
				0.1
			]);
	
		g = Group.basicNew(s);
		b = Bus.audio(s,1);
		n = MixedBundleTester.new;
	}
	test_children {
		var ps;
		ps = PlayerSocket.new;
		ps.source = p;
		this.assert( ps.children == [p],"children should just be [the patch]");
	}
	
// loadDefFileToBundle
// makePatchOut
// makeResourcesToBundle
// prepareChildrenToBundle
// loadBuffersToBundle

	test_loadDefFileToBundle {
		var ps;
		ps = PlayerSocket.new;
		ps.source = p;
		
		ps.loadDefFileToBundle(n,s);
		// not any more.  it gets loaded at prepareChildren to bundle
		// should have one bundle, the def file of p
		
		//this.assertEquals( n.preparationMessages.size,2,"should be 2 bundle, the def file + EnvelopedPlayer");
		
		// shouldnt the source be loaded as well if its non-nil ?
		//this.assert( n.defNames.includes( ps.envdSource.defName ),"bundle should have envdSource def in there");
		
		//this.assert( p.synthDef.notNil,"patch should have its synthDef there and built");		
	}
	test_makePatchOut {
		var ps,po;
		ps = PlayerSocket.new;
		ps.source = p;
		
		// makePatchOut { arg agroup,private = false,bus,bundle;
		ps.makePatchOut(g,true,b,n);
		
		po = ps.patchOut;
		// you should now have a 1 channel audio patch out
		this.assert( po.notNil,"should have a patchOut");
		this.assert( po.rate === \audio,"audio rate patchOut");
		this.assert( po.group === g,"group g");
		this.assert( po.bus === b,"bus b");
		
		this.assert( ps.group === g,"PlayerSocket should have group g");
		this.assert( ps.bus === b,"PlayerSocket should have group g");
		
		// the source is not yet assigned, and should not be until we make resources
		//this.assert( p.group === g,"PlayerSocket should have group g");
		//this.assert( p.bus === b,"PlayerSocket should have group g");
		
		// these in makeResources
		// no socketGroup in this class
	}
	test_makeResourcesToBundle {
		var ps,po;
		ps = PlayerSocket.new;
		ps.source = p;
		
		// makePatchOut { arg agroup,private = false,bus,bundle;
		ps.makePatchOut(g,true,b,n);
		
		ps.makeResourcesToBundle(n);
		
		this.assert( ps.bus.index == b.index, "bus should be same as the bus I gave you");
		
		// socketGroup
		this.assert( ps.socketGroup.notNil,"socketGroup should exist");
		this.assertEquals( ps.socketGroup.group,g, "socketGroup should be in group g");
		this.assertEquals( ps.socketGroup.group,ps.group, "socketGroup should be in the group of the player socket");
		

	}
	test_prepareChildrenToBundle {
		var ps,po;
		ps = PlayerSocket.new;
		ps.source = p;
		
		// makePatchOut { arg agroup,private = false,bus,bundle;
		ps.makePatchOut(g,true,b,n);
		ps.makeResourcesToBundle(n);
		
		ps.prepareChildrenToBundle(n);
		
		// patch 
		this.assertEquals( p.group ,ps.socketGroup ,"patch should be set into the socket group");
		this.assertEquals( p.bus,  ps.bus,"patch should have the same bus as the ps");
		//this.assert( p.bus.isKindOf(SharedBus),"patch should have shared bus");
	}
	test_loadBuffersToBundle {
		var ps,po;
		ps = PlayerSocket.new;
		ps.source = p;
		// nothing happens
	}


	
	
	
	test_prepareAndQSpawn {
		var q,r,p,t;
		
		q = Patch({ Saw.ar(40.midicps) * 0.05 });
		r = Patch({ Saw.ar(52.midicps) * 0.05 });
		p = PlayerSocket.new(\audio,1);
		p.play(nil,nil,b);

		this.wait( { p.isPlaying },"waiting for player socket to play");
		
		// then get the socked to alternately spawn q and r
		[q,r,q,r].do({ |e|
			p.prepareAndQSpawn(e,0.1);
			this.wait( { e.isPlaying },"waiting for patch "+e+" to play in socket");
			// is it in the socket group ?
			this.assertEquals( e.group , p.socketGroup,""+e+" should be playing in the player socket's socket group");
			1.0.wait;// catch breath
			// socket's envd player = q's synth
			this.assertEquals( s.numSynths, 2,"with "+e+" playing there should be 2 synths on the server");
			0.1.wait;
		});
		
		//building a new synth while playing
		p.prepareAndSpawn(
			t = Patch({ arg freq=400,gate=1.0;
				Saw.ar(freq) * EnvGen.kr(Env.adsr(0.1,2.0,0.3,2.0),gate) * 0.1
			},[
				rrand(38,70).midicps,
				KrNumberEditor(1.0,\gate)
			]),
			0.5
		);
		0.6.wait; // for envelope to stop
		this.assert( t.isPlaying,"new player t should be playing");

		// release voice
		p.releaseVoice(1.0);
		2.0.wait;
		
		this.assertEquals( s.numSynths,0,"voice released, no synths should be on the server");
		
		/*this.assertEquals( BusPool.busses.size,1,"should be the one bus still allocated",true,{
			BusPool.gui;
		});*/
		//this.assertEquals( BusPool.getAnnotations(b).size, 3,"should be no voice still using the bus, but 3 clients still on it");
		
		p.free;
		1.0.wait;
		
		this.assertEquals( BusPool.busses.size,0,"should be no bus still allocated",true,{
			BusPool.gui;
		});
		this.assertEquals( BusPool.itemCount(b), 0,"should be no clients still on the bus");
		
	}



}