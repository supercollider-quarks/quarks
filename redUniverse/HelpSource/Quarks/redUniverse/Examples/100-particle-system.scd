//simple particlesystem.
//with mouse controlling world gravity and system position

(
	var width= 300, height= 300, w, world,
		sys;
	
	//--world
	w= RedWindow("particlesystem test", Rect(128, 64, width, height)).front;
	world= RedWorld3(RedVector2D[width, height], 0, 20, -0.25); //dim,grav,maxvel,damp
	
	//--system
	sys= RedParticleSystem.new;
	
	//--loop
	w.draw{
		world.gravity= RedVector2D.newFrom(w.mouse/([width, height])-0.5);
		
		//--add new particles and update old
		sys.add(RedParticle(world, w.mouse+5.rand2, 0, RedVector2D[2.rand2, 10.rand], 1.5, 5.rand).age_(40.rrand(50)));
		sys.addForce(world.gravity);
		sys.update;
		
		//--render
		sys.do{|o|								//iterate all particles in the system
			Pen.fillColor= Color.grey((o.age/50), 1);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
