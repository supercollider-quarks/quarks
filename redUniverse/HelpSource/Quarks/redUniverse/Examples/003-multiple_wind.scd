//multiple objects with gravity + a 'wind' force blowing here and there

(
	var width= 300, height= 300, w, world, wgrav, n= 50,
		wind, cnt= 0, amp= 0.5, period= 400;
	
	//--world
	w= RedWindow("wind test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 7, 0.01); //dim,grav,maxvel,damp
	//world= RedWorld(RedVector2D[width, height], wgrav, 5); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, 50].rand;
		var vel= RedVector2D[3, 3].rand;
		var mass= 0.5.rrand(4);
		RedObject(world, loc, vel, 0, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		cnt= cnt+1;
		wind= amp*sin(2pi*(cnt/period));				//wind blowing
		world.objects.do{|o|
			o.addForce(world.gravity);
			o.addForce(wind);
			o.update;
			world.contain(o);
			
			//--render
			Pen.strokeColor= Color.grey(o.mass/4);
			Pen.strokeRect(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)