//everybody wanders and chases first boid

(
	var width= 300, height= 300, w, wgrav, world, n= 10;
	
	//--world
	w= RedWindow("boids test3", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	world= RedWorld3(RedVector2D[width, height], 0, 10, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector2D[0.5, 0.5].rand2;
		var mass= 0.5.rrand(4);
		RedBoid(world, loc, vel, 0, mass, mass*5) //world,loc,vel,acc,mass,size
			.maxForce_(1.rrand(6));
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			if(i!=0, {
				o.addForceWander2D;
				o.addForceSeek(world.objects[0].loc);
			}, {
				o.addForceWander2D;
			});
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			Pen.fillColor= Color.grey(1-(i/n), 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)