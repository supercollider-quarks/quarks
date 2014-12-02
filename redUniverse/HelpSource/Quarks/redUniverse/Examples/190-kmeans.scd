//k-means clustering

(
	var width= 400, height= 400, w, wgrav, world, n= 100,
		kmeans;
	
	//--world
	w= RedWindow("k-means", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= true;
	w.background= Color.black;
	wgrav= RedVector2D[0, 0];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0); //dim,grav,maxvel,damp
	
	//--interface
	~k= 5;										//number of centroids
	~www= world;
	
	//--objects
	{
		var loc= RedVector2D[width.rand, height.rand];
		var vel= RedVector2D[0.3.rand2, 0.3.rand2].round(0.1);
		RedObject(world, loc, vel, 0, 1, 3); //world,loc,vel,acc,mass,size
	}.dup(n);
	kmeans= RedKMeans(~k, 10);						//cluster analyser
	
	//--loop
	w.draw{
		var colors= {|i| Color.hsv(i/~k, 1, 1)}.dup(~k);
		kmeans.k= ~k;
		kmeans.update(world.objects.collect{|o| o.loc});
		world.objects.do{|o, i|
			var index= kmeans.classifications[i];
			var cent= kmeans.centroids[index];
			Pen.strokeColor= colors[index];
			Pen.line(o.loc.asPoint, cent.asPoint);
			Pen.stroke;
			Pen.strokeColor= Color.white;
			Pen.strokeRect(Rect.aboutRedVector2D(o.loc, o.size));
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
		};
		kmeans.centroids.do{|cent, i|
			Pen.fillColor= colors[i].alpha= 0.5;
			Pen.fillOval(Rect.aboutRedVector2D(cent, 15));
		};
		
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)

//--play with the number of centroids/clusters
~k= 15
~k= 50
~k= 1
~k= 2
~k= 3

//--add some gravity to make things harder
~www.gravity= RedVector[0, 0.01]
