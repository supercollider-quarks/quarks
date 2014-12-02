//everybody attracts eachother

(
	var width= 300, height= 300, w, wgrav, world, n= 10;
	
	//--world
	w= RedWindow("attractor test2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 5, 1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var mass= 0.5.rrand(10);
		RedObject(world, loc, 0, RedVector2D[1, 1], mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			world.objects.do{|oo, j|
				if(i!=j, {
					o.addForce(o.gravityForce(oo));
					if(o.loc.distance(oo.loc)<(o.size*2), {
						o.addForce(o.gravityForce(oo).neg);
					});
				});
			};
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(1-(o.mass/12));
			Pen.strokeRect(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)