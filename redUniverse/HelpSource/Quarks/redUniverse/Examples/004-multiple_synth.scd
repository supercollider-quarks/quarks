//detect floor bounce test
//with simple sound - freq, dur, amp, pan mapped to object properties

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 20;
	
	//--synthesis
	SynthDef(\lightbulbs, {|out= 0, freq= 400, maxDur= 0.3, amp= 1, pan= 0|
		var e, z, freqs, amps, rings;
		freqs= Array.geom(4, freq, Rand(1.5, 2.5));
		amps= Array.geom(4, 0.5, Rand(0, 1));
		rings= Array.geom(4, maxDur, Rand(0, 1))*maxDur;
		e= EnvGen.ar(Env.perc(0.01, maxDur), 1, amp, doneAction:2);
		z= Klank.ar(`[freqs, amps, rings], Impulse.ar(0));
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("bounce test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.1, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.1); //dim,grav,maxvel,damp
	//world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.9); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, 20].rand;
		var mass= 1.0.rrand(4);
		RedObject(world, loc, 0, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o|
			var osize, freq, amp, pan;
			o.addForce(world.gravity);
			o.update;
			if((o.loc[1]+o.size)>height, {			//just floor
			//world.contains(o).not for all walls
				osize= o.size/20;
				freq= 1-osize*200+400;
				amp= o.vel.mag/20;
				pan= o.loc[0]/width*2-1;
				Synth(\lightbulbs, [\freq, freq, \maxDur, osize, \amp, amp, \pan, pan]);
			});
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(o.mass/4, 0.7);
			Pen.strokeOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
})