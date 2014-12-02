//everybody attracts eachother - not clear each frame
//bigger mass is indicated by more white colour

(
	var width= 640, height= 480, w, wgrav, world, n= 12;
	
	//--world
	w= RedWindow("attractor test4", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= false;
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
			Pen.fillColor= Color.hsv(0.4, 1-(o.mass/12), 1, o.mass/12);
			Pen.fillRect(Rect.aboutPoint(o.loc.asPoint, 0.5, 0.5));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)