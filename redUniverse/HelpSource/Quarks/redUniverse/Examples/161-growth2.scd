//growth2 - rendering

(
	var width= 640, height= 480, w, wgrav, world, still= RedVector2D[0, 0],
		maxFree= 800,							//max number of free objects at a time
		maxTotal= 1000;						//also decides when to stop
	
	//--world
	w= RedWindow("growth2", Rect(128, 64, width, height)).front;
	w.background= Color.white;
	wgrav= RedVector2D[0.1, 0];					//world xy gravity
	world= RedWorld(RedVector2D[width, height], wgrav, 99, 0); //dim,grav,maxvel,damp
	
	//--interface
	~initSize= {2.0.rand};							//object size at creation
	~distance= 1;									//collision min distance
	
	//--objects - the first one always in the middle
	RedObject(world, RedVector2D[width*0.5, height*0.5], still, 0, 1, ~initSize.value);//seed
	RedObject(world, RedVector2D[width.rand, height.rand], still, 0, 1, ~initSize.value);
	
	//--loop
	w.draw{
		var free, frozen, rrr;
		
		world.gravity= RedVector2D[0.1.rand2, 0.1.rand2];
		~distance= 10.rand+1;
		rrr= 0.6.rand2;
		
		Pen.strokeColor= Color.black;
		
		(100+2).rand.do{
			
			//--separate free and frozen objects
			free= world.objects.select{|o| o.vel!=still};
			frozen= world.objects.select{|o| o.vel==still};
			
			//-add more objects
			if(free.size<maxFree and:{world.objects.size<maxTotal}, {
				RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[0.5.rand2, 0.5.rand2], 0, 1, ~initSize.value);
			});
			
			//--collision detection and drunk walk of free objects
			free.do{|o|
				if(frozen.any{|oo| o.loc.distance(oo.loc)<~distance}, {
					o.vel= still;
				});
				o.vel= o.vel.asPoint.rotate(rrr.rand2).asRedVector2D;
				o.addForce(o.world.gravity);
				o.update;
				world.contain(o);
			};
			
			//--render all objects
			world.objects.do{|o, i|					//iterate all objects in this world
				Pen.addRect(Rect.aboutRedObject2D(o));
			};
		
		
		};
		Pen.stroke;
	};
	w.play(0.3);		//slow updaterate
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
