//simple test of an obstacle.

(
	var width= 300, height= 300, w, wgrav, world;
	
	//--world
	w= RedWindow("rock test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0];						//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 3, 0); //dim,grav,maxvel,damp
	
	//--objects
	RedObject(world, RedVector2D[width*0.5, height*0.2], RedVector2D[3.0.rand2, 3.0.rand2], 0, 1, 10); //world,loc,vel,acc,mass,size
	RedObject(world, RedVector2D[width*0.5, height*0.5], RedVector2D[3.0.rand2, 3.0.rand2], 0, 1, 20); //world,loc,vel,acc,mass,size
	RedRock(world, RedVector2D[width*0.5, height*0.8], RedVector2D[0, 0], 0, 0, 60); //world,loc,vel,acc,mass,size
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.white;
		world.objects.do{|o, i|						//iterate all objects in this world
			world.objects.copyToEnd(i+1).do{|oo| o.collide(oo)};
			o.update;								//object update
			world.contain(o);						//keep within world borders
			
			//--render
			Pen.addOval(Rect.aboutRedObject2D(o));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
