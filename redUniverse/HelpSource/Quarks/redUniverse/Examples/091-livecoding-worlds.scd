//everybody wanders and chases the next boid


(
	//~world										//our handles for livecoding
	//~win
	//~n
	
	var width= 300, height= 300, wgrav;
	~n= 8;
	
	//--world
	~win= RedWindow("boids test3", Rect(128, 164, width, height)).front;
	wgrav= RedVector2D[0, 0];
	~world= RedWorld(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;
		var vel= RedVector2D[0.5, 0.5].rand2;
		var mass= 1;
		RedBoid(~world, loc, vel, 0, mass, mass*5) //world,loc,vel,acc,mass,size
			.maxForce_(0.6.rrand(1.6));
	}.dup(~n);
	
	//--loop
	~win.draw{
		~world.objects.do{|o, i|
			if(i!=0, {
				o.addForceWander2D;
				o.addForceSeek(~world.objects.wrapAt(i-1).loc);
			}, {
				o.addForceWander2D;
			});
			o.addForce(~world.gravity);
			o.update;
			~world.contain(o);
			
			//--render
			Pen.fillColor= Color.grey(1-(i/~n), 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	~win.animate= true;
	CmdPeriod.doOnce({if(~win.isOpen, {~win.close})});
)


//--livecoding swapping worlds under the feet of the boids
(
~world= RedUniverse.migrateAll(~world, RedWorld3(RedVector2D[400, 400], RedVector2D[0, 0.98], 5, 0.99));
~win.resize(~world.dim);
)

//back to a world without gravity
(
~world= RedUniverse.migrateAll(~world, RedWorld(RedVector2D[300, 300], RedVector2D[0, 0], 10, 0.99));
~win.resize(~world.dim);
)


//--changing the whole main loop function.  behaviour, colours and shapes
(
~win.background= Color.red;
~win.draw{
	~world.objects.do{|o, i|
		if(i!=0, {
			//o.addForceWander2D;
			o.addForceSeek(~world.objects.wrapAt(i-1).loc);
		}, {
			o.addForceWander2D;
		});
		o.addForce(~world.gravity);
		o.update;
		~world.contain(o);
		
		//--render
		Pen.fillColor= Color.yellow(1-(i/~n), 0.5);
		Pen.fillRect(Rect.aboutRedVector2D(o.loc, o.size*2.5));
	};
}
)
