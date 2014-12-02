//a few pendulums that attract eachother

(
	var width= 300, height= 300, w, wgrav, world, n= 18,
		ploc;
	
	//--world
	w= RedWindow("pendulum test3", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[30.rrand(width-30), 30.rrand(height-30)];
		var mass= 8.rrand(30);
		RedPendulum(world, loc, nil, nil, mass, mass*3) //world,loc,vel,acc,mass,size
			.theta_(2pi.rand).angularVel_(1.5.rand2);
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.white;
		Pen.width= 1;
		world.objects.do{|o, i|
			world.objects.do{|oo, j|
				if(i!=j, {
					o.addForceAngular2D(o.gravityForce(oo));
				});
			};
			o.addForceAngular2D(world.gravity);
			o.update;
			
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