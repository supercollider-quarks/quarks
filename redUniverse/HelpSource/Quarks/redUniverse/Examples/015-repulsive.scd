//one object pushes away the other in a world without bounds
//with mouse controlling repulsive object location

(
	var width= 300, height= 300, w, wgrav, world, rep, obj;
	
	//--world
	w= RedWindow("repulsive test1", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 10); //dim,grav,maxvel,damp
	
	//--objects
	rep= RedObject(world, RedVector2D[100, 100], RedVector2D[0, 0], RedVector2D[0, 0], 105, 5);
	obj= RedObject(world, RedVector2D[130, 30], RedVector2D[1.5, 1], RedVector2D[0, 0], 1.5, 10);
	
	//--loop
	w.draw{
		rep.loc= w.mouse;
		obj.addForce(obj.gravityForce(rep).neg);
		obj.update;
		world.contain(obj);
		
		//--render
		Pen.strokeColor= Color.green;
		Pen.strokeRect(Rect.aboutRedObject2D(rep));
		Pen.strokeColor= Color.white;
		Pen.strokeRect(Rect.aboutRedObject2D(obj));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)