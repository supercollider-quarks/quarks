//a single object in a 3d world - for testing

(
	var width= 300, height= 300, depth= 300, w, wgrav, world, n= 1;
	
	//--world
	w= RedWindow("single 3d test", Rect(128, 64, width, height)).front;
	wgrav= RedVector3D[0.1, 0.98, 0];
	world= RedWorld3(RedVector3D[width, height, depth], wgrav, 20, 0.05); //dim,maxvel,grav,damp
	//world= RedWorld(RedVector3D[width, height, depth], wgrav, 20, 0.99); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector[width*0.5, height*0.5, 290];
		var vel= RedVector3D[0, 0, -10];
		var acc= RedVector3D[0, 0, 0];
		RedObject(world, loc, vel, acc, 1.5, 10); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.white;
		world.objects.do{|o|
			o.addForce(o.world.gravity);
			o.update;
			world.contain(o);
			//Pen.addRect(Rect.aboutPoint(o.loc.asPoint, o.size, o.size));
			//Pen.addRect(Rect.aboutPoint(w.mouse.asPoint, o.size, o.size));
			Pen.addRect(Rect.aboutRedVector3D(o.loc, o.size, width, height, depth, 1.25, 0.5));
			Pen.addRect(Rect.aboutRedObject3D(o, 1.25, 0.4));
			Pen.addRect(Rect.aboutRedObject3D(o, 1.15, 0.3));
			Pen.addRect(Rect.aboutRedObject3D(o, 1.05, 0.2));
			Pen.addRect(Rect.aboutRedObject3D(o, 1.00, 0.1));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)