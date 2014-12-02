//particlesystem.
//with mouse controlling world gravity (the 'wind')
//with simple grain sounds mapped to each particle.

(
s.waitForBoot{

	var width= 300, height= 300, w, wgrav, world,
		sys, makeParticle, n= 200, mp;
	
	//--synthesis
	SynthDef(\noise, {|out= 0, freq= 4000, amp= 0, pan= 0, rel= 0.02|
		var z, e;
		e= EnvGen.ar(Env.perc(0.01, rel), doneAction:2);
		z= BPF.ar(WhiteNoise.ar, freq, 0.3);
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("particlesystem fountain", Rect(128, 64, width, height)).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0.4];						//world xy gravity
	world= RedWorld1(RedVector2D[width, height], wgrav, 20, 1); //dim,grav,maxvel,damp
	
	//--particle
	makeParticle= {
		var newPart= RedParticle(world, RedVector2D[width*0.5, height*0.9], 0, RedVector2D[0.5.rand2, -5.0.rrand(-10)], 1, 3.0.rand).age_(1000);
		sys.add(newPart);							//add it to the system
		Synth(\noise, [							//create a corresponding synth
			\freq, newPart.size.linexp(0, 3, 10000, 3000),
			\amp, newPart.accel.linlin(-5, -10, 0, 0.2),
			\rel, newPart.size*0.05,
			\pan, world.gravity[0]
		]);
	};
	
	//--system
	sys= RedParticleSystem.new;
	sys.removeAction= makeParticle;
	
	//--loop
	w.draw{
		
		//--create a new particle and update the old ones
		if(n>0 and:{0.051.coin}, {
			makeParticle.value;
			n= n-1;
		});
		sys.addForce(world.gravity);				//world gravity for all particles
		sys.update;								//update the system and thereby all objects
		sys.do{|o|
			o.size= (o.age-1000).abs/(o.mass*10);
			if(world.contains(o).not, {
				o.age= 0;
			});
		};
		//sys.objects.size.postln;
		
		//--mouse
		if(w.mouse!=mp, {
			world.gravity= RedVector2D.newFrom(w.mouse/([width, height])-0.5);
		});
		
		//--render
		Pen.strokeColor= Color.white;
		sys.do{|o|								//iterate all particles
			Pen.addOval(Rect.aboutRedObject2D(o));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
}
)
