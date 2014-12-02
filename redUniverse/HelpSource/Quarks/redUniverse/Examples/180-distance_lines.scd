//distance lines

(
	var width= 400, height= 400, w, wgrav, world, n= 50, minDist= 50,
		cnt= 0;
	
	//--world
	w= RedWindow("distance lines", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= false;
	w.background= Color.black;
	wgrav= RedVector2D[0, 0.001];
	world= RedWorld(RedVector2D[width, height], wgrav, 20, 0.5); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width.rand, height.rand].round(50);
		var vel= RedVector2D[1.0.rand2, 1.0.rand2].round(1/3);
		var mass= 1.0.rand;
		RedObject(world, loc, vel, 0, mass, mass*10); //world,loc,vel,acc,mass,size
	}.dup(n);
	
	//--loop
	w.draw{
		Pen.fillColor= Color.grey(0, cnt.fold(0, 500)/10000);
		Pen.fillRect(Rect(0, 0, width, height));
		world.objects.do{|o|
			world.objects.do{|oo|
				var dist= o.loc.distance(oo.loc);
				if(dist<minDist, {
					Pen.strokeColor= Color.grey(1, 1-(dist/minDist));
					Pen.line(o.loc.asPoint, oo.loc.asPoint);
					Pen.stroke;
				});
			};
			//o.addForce(world.gravity);
			o.update;
			world.contain(o);
		};
		minDist= minDist*(sin(cnt/25)*0.02+1);
		cnt= cnt+1;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
