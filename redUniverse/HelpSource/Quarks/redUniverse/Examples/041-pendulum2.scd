//many pendulums in a world with gravity

(
	var width= 300, height= 300, w, wgrav, world, n= 50,
		ploc;
	
	//--world
	w= RedWindow("pendulum test2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.02); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector[0.5, 0.5].rand2;
		RedPendulum(world, loc, vel, 0, 1.rrand(5), 5.rrand(80)) //world,loc,vel,acc,mass,size
			.theta_(2pi.rand).angularVel_(1);
	}.dup(n);
	a= world.objects;
	
	//--loop
	w.draw{
		Pen.width= 1;
		Pen.strokeColor= Color.white;
		world.objects.do{|o, i|
			o.addForceAngular2D(world.gravity);
			//o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			ploc= o.pendulumLoc2D.asPoint;
			Pen.line(o.loc.asPoint, ploc);
			Pen.addOval(Rect.aboutPoint(ploc, o.size/5, o.size/5));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
/*
//manual control - add force to random object
a.choose.addForceAngular2D(30)
a.choose.addForceAngular2D(5)
a.choose.addForceAngular2D(-5)
*/