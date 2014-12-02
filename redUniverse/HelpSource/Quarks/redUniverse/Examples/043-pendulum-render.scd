//render pendulums - takes a little while

(
	var width= 640, height= 480, w, wgrav, world, n= 50,		ploc;
	
	//--world
	w= RedWindow("pendulum test3", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		RedPendulum(world, loc, nil, 0, 4.0.rand, 5.rrand(80)) //world,loc,vel,acc,mass,size
			.theta_(2pi.rand).angularVel_(0);
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.width= 1;
		100.do{|i|
			Pen.strokeColor= Color.grey(i/100, 0.5);
			world.objects.do{|o|
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
	};
)