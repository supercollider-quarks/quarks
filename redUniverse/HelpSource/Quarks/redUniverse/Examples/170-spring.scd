//simple spring
//mouse controls red ball (target)

(
	var width= 300, height= 300, w, world, wgrav, obj, trg, mp;
	
	//--world
	w= RedWindow("spring test", Rect(128, 64, width, height)).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld1(RedVector2D[width, height], wgrav, 30, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	obj= RedObject(world, RedVector2D[0, 0], RedVector2D[0, 0], RedVector2D[0, 0], 1.5, 10);
	trg= RedObject(world, RedVector2D[width*0.5, height*0.5]);
	
	//--loop
	w.draw{
		if(mp!=w.mouse, {						//just to keep target in middle if no mouse input
			trg.loc= w.mouse;
		});
		
		obj.spring(trg, 0.1, 0.9, 50);			//target, stiffness, damping, length
		
		obj.addForce(world.gravity);
		obj.update;
		world.contain(obj);
		
		//--render
		Pen.strokeColor= Color.grey;
		Pen.line(obj.loc.asPoint, trg.loc.asPoint);
		Pen.stroke;
		Pen.fillColor= Color.red;
		Pen.fillOval(Rect.aboutRedObject2D(trg));
		Pen.fillColor= Color.white;
		Pen.fillOval(Rect.aboutRedObject2D(obj));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
