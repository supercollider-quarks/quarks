//manual waves - no wave system

(
	var width= 300, height= 300, w, wgrav, world, n= 80,
		angularVel= 0.05, nWaves= 3, amps, dx, theta= 0,
		spook= 0;
	
	amps= {0.1.rrand(5)}.dup(nWaves);	//amplitudes
	dx= {							//delta x:s
		var p= 100.0.rrand(300);		//period - how many pixels before repeat
		2pi/p*(width/n)+spook;
	}.dup(nWaves);
	
	//--world
	w= RedWindow("wave phase test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.4];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	{|i|
		var loc= RedVector2D[width/n*i, height/2];
		RedObject(world, loc, nil, nil, 1.5, 10) //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		theta= theta+angularVel;
		if(0.005.coin, {world.gravity= RedVector2D[0, -0.7.rrand(0.7)]; world.gravity.postln});
		//world.objects.do{|o, i| o.loc= RedVector[width/n*i, height/2]};
		nWaves.do{|i|
			var x= theta;
			world.objects.do{|o, j|
				if(i.even, {
					//o.loc= o.loc+RedVector[0, sin(x)*amps[i]];
					o.addForce(RedVector[0, sin(x)*amps[i]]);
				}, {
					//o.loc= o.loc+RedVector[0, cos(x)*amps[i]];
					o.addForce(RedVector[0, cos(x)*amps[i]]);
				});
				x= x+dx[i];
			};
		};
		world.objects.do{|o, i| o.addForce(world.gravity); o.update; world.contain(o)};
		
		//--render
		Pen.fillColor= Color.green(1, 0.4);
		world.objects.do{|o, i|
			Pen.fillRect(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
