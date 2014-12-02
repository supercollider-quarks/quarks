//wave with objects
//with sound - different objects sound different. object's y position is amplitude

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 60,
		sys, synths;
	
	//--synthesis
	SynthDef(\sine, {|out= 0, freq= 400, beat= 1, amp= 1, pan= 0|
		var z;
		z= Mix(SinOsc.ar(freq+[0, beat], Rand(0, 2pi), amp*0.5));
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	SynthDef(\saw, {|out= 0, freq= 400, beat= 1, amp= 1, pan= 0, lp= 1000|
		var z;
		z= Mix(LPF.ar(LFSaw.ar(freq+[0, beat], Rand(0, 2), amp*0.5), lp));
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("wave test2 synths", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.2];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.5); //dim,grav,maxvel,damp
	//world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.5); //dim,grav,maxvel,damp
	
	//--objects
	sys= RedWaveSystem(n, {1.0.rand}, {pi.rand}, {1.0.rand}, 0.05);
	{|i|
		var loc= RedVector2D[width/n*i, height/2];
		sys.add(RedObject(world, loc, nil, nil, 2.5.rand, 8)); //world,loc,vel,acc,mass,size
	}.dup(n);
	synths= {|i|
		if(i.odd, {
			Synth(\sine, [\beat, 0, \amp, 0]);
		}, {
			Synth(\saw, [\beat, 0, \amp, 0]);
		});
	}.dup(n);
	s.sync;
	
	//--loop
	w.draw{
		if(0.005.coin, {world.gravity= RedVector2D[0.01, -0.7.rrand(0.7)]; world.gravity.postln});
		sys.do{|o, i|
			var freq, amp, pan;
			o.loc= o.loc+RedVector[0, sys.wave.wrapAt(i)];
			o.addForce(world.gravity);
			freq= o.loc[0]/width*1000+200;
			amp= 1-(o.loc[1]/height).max(0.001).pow(0.25)*(8/n);
			pan= o.loc[0]/width*2-1;
			synths[i].set(\amp, amp, \freq, freq, \pan, pan);
		};
		sys.update;
		
		//--render
		Pen.fillColor= Color.grey(1, 0.4);
		sys.do{|o, i|
			if(i.odd, {
				Pen.fillOval(Rect.aboutRedObject2D(o));
			}, {
				Pen.fillRect(Rect.aboutRedObject2D(o));
			});
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
})
