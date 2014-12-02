//detect border bounce test
//with simple sound - freq, dur, amp, pan mapped to object properties
//and some different graphics

(
s.waitForBoot{
	var width= 400, height= 400, w, wgrav, world, n= 25;
	
	//--synthesis
	SynthDef(\lightbulbs, {|out= 0, freq= 400, maxDur= 0.3, amp= 1, pan= 0|
		var e, z, freqs, amps, rings;
		freqs= Array.geom(4, freq, Rand(1.5, 2.5));
		amps= Array.geom(4, 0.5, Rand(0, 1));
		rings= Array.geom(4, maxDur, Rand(0, 1))*maxDur;
		e= EnvGen.kr(Env.perc(0.01, maxDur), 1, amp, doneAction:2);
		z= Klank.ar(`[freqs, amps, rings], Impulse.ar(0));
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("bounce test", Rect(128, 64, width, height)).front;
	w.background= Color.white;
	wgrav= RedVector2D[0, 0];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;		//random position
		var vel= RedVector2D[4, 4].rand2.round(2);	//quantise velocities
		var mass= 2.rrand(14);
		RedObject(world, loc, vel, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.black;
		Pen.fillColor= Color.red(1, 0.7);
		world.objects.do{|o|
			var osize, freq, amp, pan;
			o.addForce(world.gravity);
			o.update;
			if(world.contains(o).not, {				//detect border
				osize= o.size/10;
				freq= 1-osize*200+400;
				amp= o.vel.mag/15;
				pan= o.loc[0]/width*2-1;
				Synth(\lightbulbs, [\freq, freq, \maxDur, osize, \amp, amp, \pan, pan]);
			});
			world.contain(o);
			
			//--render
			Pen.strokeRect(Rect.aboutRedObject2D(o));
			Pen.fillRect(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
})