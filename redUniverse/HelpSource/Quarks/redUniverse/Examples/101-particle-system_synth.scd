//particlesystem.
//with mouse controlling world gravity and system position
//with simple grain sounds mapped to each particle.

(
s.waitForBoot{

	var width= 300, height= 300, w, world,
		sys, synths;
	
	//--synthesis
	SynthDef(\sine, {|out= 0, freq= 400, fm= 1, beat= 1, amp= 0, pan= 0, gate= 1|
		var z, e;
		e= EnvGen.ar(Env.asr(0.01, 1, 0.02), gate, doneAction:2);
		z= SinOsc.ar(freq+SinOsc.ar(fm, 0, beat), 0, Lag.kr(amp, 0.1));
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	synths= ();									//keep track of synths with this dictionary
	
	//--world
	w= RedWindow("particlesystem test2", Rect(128, 64, width, height)).front;
	world= RedWorld3(RedVector2D[width, height], 0, 20, 1.25); //dim,grav,maxvel,damp
	
	//--system
	sys= RedParticleSystem.new;
	sys.removeAction= {|o| synths.at(o.hash).release};	//free synth when particle dies
	
	//--loop
	w.draw{
		
		//--create a new particle and update the old ones
		var newPart= RedParticle(world, w.mouse+5.rand2, 0, RedVector2D[2.rand2, -10.rand], 1.5, 5.rand).age_(40.rrand(50));
		sys.add(newPart);							//add it to the system
		synths.put(newPart.hash, Synth(\sine));		//create a corresponding synth
		sys.addForce(world.gravity);				//world gravity for all particles
		sys.update;								//update the system and thereby all objects
		
		sys.do{|o|								//iterate all particles
			synths.at(o.hash).set(					//find corresponding synth and set params
				\freq, 200.rrand(o.loc[1]+200),
				\pan, o.loc[0]/width*2-1,
				\beat, o.mass*(o.vel.mag*10),
				\fm, o.mass*1000,
				\amp, (1/40)*(o.age/50)*(o.vel.mag/10)
			)
		};
		
		//--mouse
		world.gravity= RedVector2D.newFrom(w.mouse/([width, height])-0.5);
		
		//--render
		sys.do{|o|								//iterate all particles
			Pen.fillColor= Color.grey(o.age/50, 1);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
}
)
