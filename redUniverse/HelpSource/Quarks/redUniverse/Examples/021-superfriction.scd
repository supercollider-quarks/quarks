//an area middle of the screen has super friction - no gravity in this world

(
	var width= 300, height= 300, w, world, acc, vel, loc, obj, fric, ff;
	
	//--world
	w= RedWindow("superfriction test", Rect(128, 64, width, height)).front;
	world= RedWorld(RedVector2D[width, height], 0, 30, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	acc= RedVector2D[0, 0];
	vel= RedVector2D[1.5, 1.5];
	loc= RedVector2D[250, 50];
	obj= RedObject(world, loc, vel, acc, 1.5, 10);
	
	fric= Rect(0, height*0.4, width, 40);		//area with super friction
	ff= 0.5;								//amount of friction
	
	//--loop
	w.draw{
		if(fric.containsPoint(obj.loc.asPoint), {
			if(obj.vel.mag>0 and:{obj.vel.mag>obj.frictionForce(ff).mag}, {
				obj.addForce(obj.frictionForce(ff));
			}, {
				obj.vel= obj.vel*0;		//stop object
			});
		});
		obj.update;
		world.contain(obj);
		
		//--render
		Pen.fillColor= Color.green;
		Pen.fillRect(fric);
		Pen.strokeColor= Color.white;
		Pen.strokeRect(Rect.aboutRedObject2D(obj));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
