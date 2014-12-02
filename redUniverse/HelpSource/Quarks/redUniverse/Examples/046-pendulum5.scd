//some pendulums drifting and coming to a halt

(
	var width= 640, height= 480, w, wgrav, world,
		ypos;
	
	//--world
	w= RedWindow("pendulum test5", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= false;
	
	wgrav= RedVector2D[0, 0];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.001); //dim,grav,maxvel,damp
	
	//--objects
	ypos= 10;
	while({ypos<(height-10)}, {
		var size= 50.0.rand;		//pendulum size
		var loc= RedVector2D[width.rand, ypos+size];
		var vel= RedVector2D[0.5, 0].rand;
		var obj= RedPendulum(world, loc, vel, 0, size, size) //world,loc,vel,acc,mass,size
			.angularVel_(2.0.linrand);
		ypos= ypos+(size*2.25);
		obj;
	});
	
	//--loop
	w.draw{
		Pen.fillColor= Color.white;
		Pen.width= 1;
		world.objects.do{|o, i|
			o.update;
			world.contain(o);
			
			//--render
			Pen.fillRect(Rect.aboutPoint(o.pendulumLoc2D.asPoint, 0.5, 0.5));
		};
		Pen.fill;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
