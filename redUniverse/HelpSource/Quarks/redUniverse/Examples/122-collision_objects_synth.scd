//multiple objects within a world with gravity force and also collision detection
//color and size depends on object's mass
//sound - collision velocity mapped to amplitude, mass to dur and freq

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 10,
		synths;
	
	//--synthesis
	SynthDef(\fm, {|out= 0, t_trig= 0, dur= 0.5, freq= 400, freq2= 606, amount= 10, amp= 1, pan= 0|
		var e, z;
		e= EnvGen.kr(Env.perc(dur*0.01, dur), t_trig);
		z= BPF.ar(LFSaw.ar(freq+SinOsc.ar(freq2, 0, amount), 0, amp), freq2*2);
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("collision test2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.1, 0.98];					//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var vel= RedVector2D[2.rand2, -5];			//random initial velocity for each object
		var mass= 0.5.rrand(5).round(0.1);			//random mass for each object
		RedObject(world, RedVector2D[width, height].rand, vel, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	synths= {Synth(\fm, [\amp, 0])}.dup(n);
	s.sync;
	
	//--loop
	w.draw{
		var set;
		Pen.width= 3;
		set= Set.new;
		world.objects.do{|o, i|
			o.addForce(world.gravity);
			world.objects.do{|oo, j|
				if(i!=j, {
					if(o.collide(oo), {
						set.add([i, j].sort)
					})
				})
			};
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(o.mass/5);
			Pen.strokeOval(Rect.aboutRedObject2D(o));
		};
		set.do{|indexes|
			var o= world.objects[indexes[0]];
			var oo= world.objects[indexes[1]];
			var mag= o.vel.mag+oo.vel.mag/(world.maxVel*2);
			synths[indexes[0]].set(
				\t_trig, 1,
				\amp, mag.pow(2)/n,
				\dur, o.mass/5,
				\freq, (5-o.mass).linexp(0, 5, 200, 2000),
				\freq2, (5-o.mass).linexp(0, 5, 200, 2000)
			);
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
}
)
