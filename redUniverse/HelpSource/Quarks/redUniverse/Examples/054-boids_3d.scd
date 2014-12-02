//multiple boids in a 3d world.

(
	var width= 640, height= 480, depth= 320, w, wgrav, world, n= 60,
		f= 1, s= 0.25;
	
	//--world
	w= RedWindow("boids 3d test", Rect(128, 64, width, height)).front;
	wgrav= RedVector3D[0.1, 0.1, 0.1];
	world= RedWorld3(RedVector3D[width, height, depth], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector3D[width, height, depth].rand;
		var vel= RedVector3D[2, 2, 2].rand2;
		var acc= RedVector3D[0, 0, 0];
		var mass= 0.5.rrand(4);
		var obj= RedBoid(world, loc, vel, acc, mass, mass*10)
			.maxForce_(5.rand+1).wdistance_(mass).wchange_(0.3).wradius_(10);
	}.dup(n);	//world,loc,vel,acc,mass,size
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			var trg= o.addForceWander3D;
			o.addForceArrive(world.objects.wrapAt(i-1).loc);
			//o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(depth-o.loc[2]/(depth*0.5));
			Pen.strokeOval(Rect.aboutRedObject3D(o, f, s));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
