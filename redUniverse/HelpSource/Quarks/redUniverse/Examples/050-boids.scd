//everybody chase first boid. interesting exploring behaviour. slow down as approaching
//mouse controls the first boid

(
	var width= 300, height= 300, w, wgrav, world, n= 5;
	
	//--world
	w= RedWindow("boids test1", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 10, 0.05); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector2D[0.5, 0.5].rand2;
		var mass= 0.5.rrand(4);
		var obj= RedBoid(world, loc, vel, RedVector2D[0, 0], mass, mass*5)
			.maxForce_(1).wdistance_(3.rrand(13)).wchange_(0.75).wradius_(10.rrand(50));
	}.dup(n);	//world,loc,vel,acc,mass,size
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			var trg;
			if(i!=0, {
				trg= o.addForceWander2D;
				o.addForceArrive(world.objects[0].loc);
				//--plot target for testing
				//Pen.strokeColor= Color.red;
				//Pen.strokeOval(Rect.aboutRedVector2D(trg, o.wradius));
				
			}, {
				world.objects[0].loc= w.mouse;
			});
			//o.addForce(world.gravity);			//no gravity involved
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