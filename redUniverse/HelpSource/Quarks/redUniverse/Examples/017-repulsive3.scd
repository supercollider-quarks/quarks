//multiple objects are pushed away from a moving circle

(
	var width= 640, height= 480, w, world, acc, vel, loc, rep, n= 200;
	
	//--world
	w= RedWindow("repulsive test3", Rect(128, 64, width, height)).front;
	world= RedWorld(RedVector2D[width, height]); //dim,grav,maxvel,damp
	
	//--objects
	rep= RedObject(world, RedVector2D[width*0.5, height*0.5], nil, nil, 50, height*0.2);//area with repulsion
	{
		acc= RedVector2D[0, 0];
		vel= RedVector2D[0, 0];
		loc= RedVector2D[width.rand, height*0.5];
		RedObject(world, loc, vel, acc, 1, 0.5);
	}.dup(n);
	
	//--loop
	w.draw{
		
		rep.loc= RedVector2D[
			sin(w.frame*0.02+sin(w.frame*0.001))*0.5+0.5*width,
			cos(w.frame*0.027+cos(w.frame*0.0012))*0.5+0.5*height
		];
		Pen.strokeColor= Color.yellow;
		Pen.strokeOval(Rect.aboutRedObject2D(rep));
		
		Pen.strokeColor= Color.white;
		world.objects.do{|o, i|
			if(o!=rep, {
				o.addForce(o.gravityForce(rep).neg);
				o.update;
				world.contain(o);
				
				//--render objects
				Pen.addRect(Rect.aboutRedObject2D(o));
			});
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
