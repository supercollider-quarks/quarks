//everybody chase mouse x position in 1 dimension.

(
	var width= 600, height= 300, w, world, n= 15,
		att;
	
	//--world
	w= RedWindow("boids test 1d", Rect(128, 64, width, height)).front;
	world= RedWorld3(RedVector[width], nil, 10, 0); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector[width].rand;
		var vel= RedVector[1];
		var mass= 0.5.rrand(4);
		var obj= RedBoid(world, loc, vel, 0, mass, mass*5)
			.maxForce_(1).wdistance_(mass).wchange_(0.5).wradius_(mass*5);
	}.dup(n);	//world,loc,vel,acc,mass,size
	att= RedHiddenObject(world, RedVector[0, 0]);		//hidden attractor
	
	//--loop
	w.draw{
		att.loc= w.mouse;
		
		Pen.fillColor= Color.grey(1, 0.5);
		Pen.strokeColor= Color.white;
		
		world.objects.do{|o, i|
			var xy;
			var trg= o.addForceWander1D;
			o.addForceArrive(att.loc[0]);
			
			o.update;
			world.contain(o);
			
			//--render
			xy= Point(o.loc[0], i.linlin(0, n-1, world.objects[0].size, height-world.objects.last.size));
			Pen.fillOval(Rect.aboutPoint(xy, o.size, o.size));
			//--plot target for testing
			Pen.moveTo(xy);
			Pen.lineTo(Point(trg[0], xy.y));
			Pen.stroke;
			
		};
		
		Pen.strokeColor= Color.yellow;
		Pen.strokeRect(Rect(att.loc[0]-1, 0, 2, height));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
