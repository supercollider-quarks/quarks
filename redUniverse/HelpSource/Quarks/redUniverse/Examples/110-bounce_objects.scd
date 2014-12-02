//objects shoot out off each other.  with friction and world damping to compensate.

(
	var width= 300, height= 300, w, wgrav, world, n= 18;
	
	//--world
	w= RedWindow("bounce test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.1, 0.38];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.92); //dim,grav,maxvel,damp
	
	//--objects
	{|i|
		var mass= 1;
		RedObject(world, RedVector2D[i*20+10, 20], RedVector2D[0.001, 0.001], nil, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.grey(0.7, 0.5);
		Pen.width= 2;
		world.objects.do{|o, i|
			o.addForce(world.gravity);
			o.addForce(o.frictionForce(-0.3));
			o.update;
			world.objects.do{|oo, j|
				if(i!=j, {
					if(o.contains(oo), {
						o.addForce(o.gravityForce(oo).neg*100);
					});
				});
			};
			world.contain(o);
			
			//--render
			Pen.addOval(Rect.aboutRedObject2D(o));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
