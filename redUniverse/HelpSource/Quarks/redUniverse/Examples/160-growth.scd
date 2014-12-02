//simulating growth - quite heavy on the cpu if you let it run for a while

(
	var width= 300, height= 300, w, wgrav, world, still= RedVector2D[0, 0],
		seeds= 1,								//number of initial objects
		maxFree= 200,							//max number of free objects at a time
		maxTotal= 600;						//also decides when to stop
	
	//--world
	w= RedWindow("growth", Rect(128, 64, width, height)).front;
	w.background= Color.white;
	wgrav= RedVector2D[0, 0];					//world xy gravity
	world= RedWorld(RedVector2D[width, height], wgrav, 99, 0); //dim,grav,maxvel,damp
	
	//--interface
	~initSize= {1.1.rand};							//object size at creation
	~distance= 4;									//collision min distance
	
	//--objects - the first one always in the middle
	RedObject(world, RedVector2D[width*0.5, height*0.5], still, 0, 1, ~initSize.value);
	{RedObject(world, RedVector2D[width.rand, height.rand], still, 0, 1, ~initSize.value)}.dup(seeds-1);
	
	//--loop
	w.draw{
		var free, frozen;
		//Pen.setSmoothing(false);
		Pen.strokeColor= Color.black;
		
		//--separate free and frozen objects
		free= world.objects.select{|o| o.vel!=still};
		frozen= world.objects.select{|o| o.vel==still};
		
		//-add more objects
		if(free.size<maxFree and:{world.objects.size<maxTotal}, {
			RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[1, 1], 0, 1, ~initSize.value);
		});
		
		//--collision detection and drunk walk of free objects
		free.do{|o|
			if(frozen.any{|oo| o.loc.distance(oo.loc)<~distance}, {
				o.vel= still;
			});
			o.vel= o.vel.asPoint.rotate(0.5.rand2).asRedVector2D;
			o.update;
			world.contain(o);
		};
		
		//--render all objects
		world.objects.do{|o, i|						//iterate all objects in this world
			Pen.addRect(Rect.aboutRedObject2D(o));
		};
		Pen.stroke;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)

~initSize= {5};		//varying object sizes
~distance= 5;			//distance to stick
