//everybody chase first boid
//mouse controls the first boid

(
	var width= 300, height= 300, w, wgrav, world, n= 50;
	
	//--world
	w= RedWindow("boids test2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 15, 0.05); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector2D[0.5, 0.5].rand2;
		var mass= 0.5.rrand(4);
		RedBoid(world, loc, vel, RedVector2D[0, 0], mass, mass*5) //world,loc,vel,acc,mass,size
			.maxForce_(2);
	}.dup(n);
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			if(i!=0, {
				o.addForceSeek(world.objects[0].loc);
				//o.addForceArrive(world.objects[0].loc, 100);//everybody chase first boid
			}, {
				world.objects[0].loc= w.mouse;
			});
			//o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			Pen.fillColor= Color.grey(1-(i/n), 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)