//discrete world - 2d ca

(
	var width= 640, height= 480, w, wgrav, world, n= 64,
		wx= width/n, hy= height/n;
	
	//--world
	w= RedWindow("discrete world - 2d ca", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= true;
	w.background= Color.black;
	world= RedWorld3(RedVector[wx, hy]); //dim,grav,maxvel,damp
	
	//--objects
	{|x|
		{|y|
			var loc= RedVector[x, y];
			RedAgent(world, loc).state_(2.rand); //world,loc,vel,acc,mass,size
		}.dup(hy);
	}.dup(wx);
	
	//--loop
	w.draw{
		world.objects.do{|o, i|
			var cnt;
			Pen.strokeColor= Color.grey(o.state);
			Pen.strokeRect(Rect(o.loc[0]*n+(n*0.25), o.loc[1]*n+(n*0.25), n*0.5, n*0.5));
			cnt= world.neighbours(o).sum{|oo| oo.state};
			if(cnt==3, {
				o.state= 1;
			}, {
				if(cnt!=2, {
					o.state= 0;
				});
			});
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
