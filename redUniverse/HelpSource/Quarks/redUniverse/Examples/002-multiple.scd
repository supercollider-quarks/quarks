//multiple objects within a world with gravity force
//color and size depends on object's mass

(
	var width= 300, height= 300, w, wgrav, world, n= 80;
	
	//--world
	w= RedWindow("vector test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.1, 0.98];					//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.2); //dim,grav,maxvel,damp
	
	//--objects
	{
		var mass= 1.0.rrand(4);						//random mass for each object
		RedObject(world, RedVector2D[width, 20].rand, 0, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o|
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(o.mass/4);
			Pen.strokeRect(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)