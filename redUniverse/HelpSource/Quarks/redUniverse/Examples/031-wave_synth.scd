//wave with objects
//with sound - like a big mixer that mix partials. object's y position is amplitude

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 30,
		sys, synths;
	
	//--synthesis
	SynthDef(\sine, {|out= 0, freq= 400, beat= 1, amp= 1, pan= 0|
		var z;
		z= Mix(SinOsc.ar(freq+[0, beat], 0, amp*0.5));
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	s.sync;
	
	//--world
	RedUniverse.clear;
	w= RedWindow("wave test synths", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.04];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.2); //dim,grav,maxvel,damp
	
	//--objects
	sys= RedWaveSystem(n, {|i| i}, 0, {1.0.rand}, 0.1);
	{|i|
		var loc= RedVector2D[width/n*i, height/2];
		sys.add(RedObject(world, loc, nil, nil, 1.5, 12)); //world,loc,vel,acc,mass,size
	}.dup(n);
	synths= {|i| Synth(\sine, [\beat, 0, \amp, 0])}.dup(n);
	s.sync;
	
	//--loop
	w.draw{
		if(0.005.coin, {world.gravity= RedVector2D[0, -0.7.rrand(0.7)]; world.gravity.postln});
		sys.do{|o, i|
			var freq, amp, pan;
			o.loc= o.loc+RedVector[0, sys.wave.wrapAt(i)];
			o.addForce(o.world.gravity);
			freq= o.loc[0]/width*1000+200;
			amp= 1-(o.loc[1]/height).max(0.001).pow(0.25)*0.1;
			pan= o.loc[0]/width*2-1;
			synths[i].set(\amp, amp, \freq, freq, \pan, pan);
		};
		sys.update;
		
		//--render
		Pen.fillColor= Color.grey(1, 0.4);
		sys.do{|o, i|
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
})


//--livecoding
w= RedUniverse.worlds[0]
w.gravity= RedVector2D[-0.3, 0.04];
w.damping= 0.999
w.gravity= RedVector2D[1, 0];
w.damping= 0
w.gravity= RedVector2D[1, 10];
w.damping= 2
w.damping= 0
w.damping= -1
w.damping= -2
