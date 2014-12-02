//an area middle of the screen has viscosity - no gravity in this world

(
	var width= 300, height= 300, w, world, acc, vel, loc, obj, visc, vv;
	
	//--world
	w= RedWindow("viscosity test", Rect(128, 64, width, height)).front;
	world= RedWorld(RedVector2D[width, height], 0, 30); //dim,grav,maxvel,damp
	
	//--objects
	acc= RedVector2D[0, 0];
	vel= RedVector2D[3.5, 30];
	loc= RedVector2D[250, 10];
	obj= RedObject(world, loc, vel, acc, 1.5, 10);
	
	visc= Rect(0, height*0.4, width, 40);		//area with viscosity
	vv= -0.13;							//amount of viscosity
	
	//--loop
	w.draw{
		if(visc.containsPoint(obj.loc.asPoint), {
			obj.addForce(obj.viscosityForce(vv));
		});
		obj.update;
		world.contain(obj);
		
		//--render
		Pen.fillColor= Color.yellow;
		Pen.fillRect(visc);
		Pen.strokeColor= Color.white;
		Pen.strokeRect(Rect.aboutRedObject2D(obj));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
