//very simple test.  one single object within a world with gravity force

(
	var width= 300, height= 300, w, wgrav, world, n= 1;
	
	//--world
	w= RedWindow("vector test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.4, 0.98];					//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.25); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[250, 10];				//initial xy location
		var vel= RedVector2D[3.5, 0];				//initial velocity
		var acc= RedVector2D[1, 1].rand2;			//initial acceleration
		RedObject(world, loc, vel, acc, 1, 10); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.white;
		world.objects.do{|o|						//iterate all objects in this world
			o.addForce(o.world.gravity);			//add gravity
			o.update;								//object update
			world.contain(o);						//keep within world borders
			
			//--render
			Pen.strokeRect(Rect.aboutRedObject2D(o));
		}
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
