//everybody wanders and chases the next boid
//livecoding world crossfades and sequences

(
	//~world										//our handles  for livecoding
	//~win
	//~n
	
	var width= 300, height= 300, wgrav;
	~n= 8;
	
	//--world
	RedUniverse.clear;
	~win= RedWindow("livecoding worlds test", Rect(128, 164, width, height)).front;
	~world= RedWorld.new; //dim,grav,maxvel,damp
	
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
		if(~world.gravity[0]>1, {
			~win.background= Color.red(1, 0.5);
		}, {
			~win.background= Color.blue(~world.gravity[1], 0.5);
		});
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


//--livecoding interpolates worlds

//--first create 2 different worlds
~black= RedWorld3(RedVector2D[400, 400], RedVector2D[0, 0], 5, 0.01);
~blue= RedWorld3(RedVector2D[300, 300], RedVector2D[0, 0.99], 5, 0.99);

//--interpolate and migrate
(
~mix= RedUniverse.interpolate(~blue, ~black, 0.5);		//half black, half blue
~world= RedUniverse.migrateAll(~world, ~mix);			//move all to new world
~win.resize(~world.dim);							//resize window
)


//--task xfade
(
{
	50.do{|i|
		~mix= RedUniverse.interpolate(~black, ~blue, i/49);
		~world= RedUniverse.migrateAll(~world, ~mix);
		~win.resize(~world.dim);
		0.1.wait;
	}
}.fork(AppClock)
)

//--another world with strong gravity left
~red= RedWorld3(RedVector[500, 350], RedVector2D[2, 0], 10);

//--task sequence worlds
(t= {
	inf.do{|i|
		~mix= [~blue, ~black, ~red][i%5%3];
		~world= RedUniverse.migrateAll(~world, ~mix);
		~win.resize(~world.dim);
		0.5.wait;
	}
}.fork(AppClock)
)

t.stop
RedUniverse.clear
~win.close
