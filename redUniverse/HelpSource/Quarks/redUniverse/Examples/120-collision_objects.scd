//multiple objects within a world with gravity force and also collision detection
//color and size depends on object's mass

(
	var width= 300, height= 300, w, wgrav, world, n= 20;
	
	//--world
	w= RedWindow("collision test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.1, 0.98];					//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 10, 0.6); //dim,grav,maxvel,damp
	
	//--objects
	{
		var vel= RedVector2D[2.rand2, -5];			//random initial velocity for each object
		var mass= 0.5.rrand(4);						//random mass for each object
		RedObject(world, RedVector2D[width, height].rand, vel, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.width= 1;
		world.objects.do{|o, i|
			o.addForce(world.gravity);
			world.objects.do{|oo, j| if(i!=j, {o.collide(oo)})};
			//world.objects.copyToEnd(i+1).do{|oo| o.collide(oo)};//faster but less accurate
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(o.mass/4);
			Pen.strokeOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)