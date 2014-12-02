//everybody attracts eachother within a world with gravity

(
	var width= 300, height= 300, w, wgrav, world, n= 10;
	
	//--world
	w= RedWindow("attractor test3", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 6, 0.02); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var mass= 0.5.rrand(10);
		RedObject(world, loc, RedVector2D[0, 0], RedVector2D[0.5, 0.5], mass, mass*5);
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			world.objects.do{|oo, j|
				if(i!=j, {						//do not attract yourself
					o.addForce(o.gravityForce(oo));	//add gravity from all other masses
				});
			};
			o.addForce(world.gravity);				//add gravity from the world
			o.update;								//calculate accel, vel and loc
			world.contain(o);						//keep object within the world
			
			//--render
			Pen.fillColor= Color.grey(1-(o.mass/12), 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)