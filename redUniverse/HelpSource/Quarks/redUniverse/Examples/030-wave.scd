//wave system with in a world with gravity changing over time

(
	var width= 300, height= 300, w, wgrav, world, n= 80,
		sys;
	
	//--world
	w= RedWindow("wave test1", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.4];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.01); //dim,grav,maxvel,damp
	
	//--objects
	//
	sys= RedWaveSystem(n, {pi.rand}, {|i| i/n*2pi}, {0.75.rand}, 0.1);
	{|i|
		var loc= RedVector2D[width/n*i, height/2];
		sys.add(RedObject(world, loc, nil, 0, 1.5, 10)); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		if(0.005.coin, {world.gravity= RedVector2D[0, -0.7.rrand(0.7)]; world.gravity.postln});
		sys.do{|o, i|
			o.loc= o.loc+RedVector2D[0, sys.wave.wrapAt(i)];
			o.addForce(world.gravity);
		};
		sys.update;
		
		//--render
		Pen.fillColor= Color.grey(1, 0.4);
		sys.do{|o, i|
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
