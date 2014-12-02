//multiple objects in a 3d world.

(
	var width= 300, height= 300, depth= 300, w, wgrav, world, n= 60,
		f= 1.25, s= 0.25;
	
	//--world
	w= RedWindow("multi 3d test", Rect(128, 64, width, height)).front;
	wgrav= RedVector3D[0.1, 0.1, 0.1];
	world= RedWorld3(RedVector3D[width, height, depth], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	//world= RedWorld2(RedVector3D[width, height, depth], wgrav, 20, 0.92); //dim,grav,maxvel,damp
	
	//--objects
	{
		var mass= 1;
		RedObject(world, RedVector3D[width, height, depth].rand, RedVector3D[2, 2, 2].rand2, RedVector3D[2, 2, 2].rand2, mass, mass*10); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.grey(0.5);
		Pen.strokeRect(Rect.aboutRedVector3D(RedVector[width*0.5, height*0.5, depth], width*0.5, width, height, depth, f, s));
		Pen.strokeColor= Color.white;
		Pen.strokeRect(Rect.aboutRedVector3D(RedVector[width*0.5, height*0.5, 0], width*0.5, width, height, depth, f, s));
		world.objects.do{|o|
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(depth-o.loc[2]/(depth*0.5));
			Pen.strokeRect(Rect.aboutRedObject3D(o, f, s));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
