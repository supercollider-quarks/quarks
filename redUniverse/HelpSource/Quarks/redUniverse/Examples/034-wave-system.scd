//example using the RedWaveSystem class. angluar velocity changes at random

(
	var width= 300, height= 300, w, wgrav, world, n= 50,
		sys, spook= 0;
	
	//--world
	w= RedWindow("wavesystem test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.2); //dim,grav,maxvel,damp
	
	//--system
	sys= RedWaveSystem(n, {5.0.rand2}, {2pi.rand}, {0.5.rand}, 0.25);
	{|i|
		var loc= RedVector2D[width/n*i, height/2];
		sys.add(RedObject(world, loc, nil, 0, 1.5, 10)); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		//if(0.005.coin, {world.gravity= RedVector2D[0, -0.7.rrand(0.7)]; world.gravity.postln});
		if(0.005.coin, {sys.angularVel= 0.5.rand2});
		sys.do{|o, i|
			o.loc= o.loc+RedVector2D[0, sys.wave.wrapAt(i)];
			//o.vel= o.vel+RedVector2D[0, sys.wave.wrapAt(i)];
			//o.addForce(RedVector2D[0, sys.wave.wrapAt(i)]);
		};
		//sys.addForce(world.gravity);
		sys.update;
		
		//--render
		Pen.fillColor= Color.grey(1, 0.5);
		//Pen.strokeColor= Color.grey(1, 0.5);
		//Pen.moveTo(sys.objects[0].loc.asPoint);
		sys.do{|o, i|
			Pen.fillOval(Rect.aboutRedObject2D(o));
			//Pen.lineTo(o.loc.asPoint);
		};
		//Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
