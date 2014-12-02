//k-means clustering2
//points in space moving around at random - kmeans find central point and draw a blob

(
	var width= 640, height= 480, w, wgrav, world, n= 100,
		kmeans;
	
	//--world
	w= RedWindow("k-means2", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= true;
	w.background= Color.black;
	wgrav= RedVector2D[0, 0];
	world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0); //dim,grav,maxvel,damp
	
	//--interface
	~k= 10;										//number of centroids
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
		var groups= {List.new}.dup(~k);
		var colors= {|i| Color.hsv(i/(~k-1), 0.8, 1)}.dup(~k);
		kmeans.k= ~k;
		kmeans.update(world.objects.collect{|o| o.loc});
		world.objects.do{|o, i|
			var index= kmeans.classifications[i];
			groups[index].add(o.loc.asPoint);
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
		};
		groups.do{|list, i|
			Pen.strokeColor= colors[i];
			list.do{|p, j|
				Pen.addRect(Rect.aboutPoint(p, 0.5, 0.5));
			};
			Pen.stroke;
		};
		Pen.strokeColor= Color.white;
		kmeans.centroids.do{|cent, i|
			var start;
			var c= cent.asPoint;
			Pen.fillColor= colors[i];
			groups[i].sort{|a, b| (c-a).theta>(c-b).theta}.do{|p, j|
				if(j==0, {
					start= p;
					Pen.moveTo(start);
				}, {
					Pen.quadCurveTo(p, c);
					if(j==(groups[i].size-1), {
						Pen.quadCurveTo(start, c);
					});
				});
			};
			Pen.fill;
			//Pen.fillColor= Color.white;
			//Pen.fillOval(Rect.aboutPoint(c, 15, 15));//draw centroid
		};
		
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)

//--play with the number of centroids/clusters
~k= 15
~k= 35
~k= 2
~k= 3
~k= 10

//--add some gravity to make things harder
~www.gravity= RedVector[0.01, 0.01]
~www.gravity= RedVector[0, 0]