//multiple objects are pushed away by a single
//with mouse controlling repulsive object location

(
	var width= 300, height= 300, w, wgrav, world, n= 10,
		rep;
	
	//--world
	w= RedWindow("repulsive test2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.1];
	world= RedWorld(RedVector2D[width, height], wgrav, 5); //dim,grav,maxvel,damp
	
	//--objects
	rep= RedObject(world, RedVector2D[width/2, 100], nil, nil, 50, 5);
	{
		var loc= RedVector2D[width, height].rand;
		RedObject(world, loc, RedVector2D[1.5, 1.0].rand, RedVector2D[0, 0], 1.5, 10)
	}.dup(n);	//world,loc,vel,acc,mass,size
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.green;
		rep.loc= w.mouse;
		Pen.strokeRect(Rect.aboutRedObject2D(rep));
		Pen.strokeColor= Color.white;
		world.objects.do{|o|
			if(o!=rep, {
				o.addForce(o.gravityForce(rep).neg);
				o.addForce(world.gravity);
				o.update;
				world.contain(o);
				
				//--render
				Pen.addRect(Rect.aboutRedObject2D(o));
			});
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)