//a pendulum in 3d

(
	var width= 300, height= 300, depth= 300, w, wgrav, world, obj,
		ploc, f= 1, s= 0.1;
	
	//--world
	w= RedWindow("pendulum 3d", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0, 0];
	world= RedWorld(RedVector3D[width, height, depth], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	w.background= Color.white;
	
	//--objects
	obj= RedPendulum(world, RedVector3D[width*0.5, height*0.5, depth*0.5], nil, nil, 1, 100) //world,loc,vel,acc,mass,size
		.theta_(0).angularVel_(1);
	
	//--loop
	w.draw{
		Pen.color= Color.black;
		Pen.width= 1;
		
		obj.addForceAngular3D(world.gravity);
		obj.update;
		a= obj;
		
		//--render
		ploc= Rect.aboutRedVector3D(obj.pendulumLoc3D, obj.size, width, height, depth, f, s);
		Pen.fillOval(ploc);
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)

/*
//manual control
a.addForceAngular3D(30)
a.addForceAngular3D(5)
a.addForceAngular3D(-5)
a.pendulumOffset3D
a.pendulumLoc3D
*/