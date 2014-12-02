//attractor test

(
	var width= 300, height= 300, w, wgrav, world, n= 3,
		att;
	
	//--world
	w= RedWindow("attractor test1", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.25); //dim,grav,maxvel,damp
	
	//--objects
	att= RedObject(world, RedVector2D[100, 100], RedVector2D[0, 0], RedVector2D[0, 0], 105, 10);
	{
		var loc= RedVector2D[30, 30].rand;			//start at random position upper left
		RedObject(world, loc, RedVector2D[1.5, 1], RedVector2D[0, 0], 1.5, 10); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.green;
		Pen.strokeOval(Rect.aboutRedObject2D(att));
		Pen.strokeColor= Color.white;
		world.objects.do{|o|
			if(o!=att, {
				o.addForce(o.gravityForce(att));
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
