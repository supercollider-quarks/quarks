//agents playing game-of-life. objects are neightbours if they touch
//standard rule modified to also include: if no neighbours then become alive
//with mouse controlling world gravity
//with sound on/off

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 40,
		rule, px= 0, py= 0, synths;
	
	//--synthesis
	SynthDef(\sine, {|out= 0, freq= 400, beat= 1, amp= 1, pan= 0|
		var z;
		z= SinOsc.ar(freq+SinOsc.ar(40, 0, beat), 0, Lag.kr(amp, 0.02));
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("agents test ca2", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	//world= RedWorld(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	world= RedWorld3(RedVector2D[width, height], wgrav, 10, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	{|i|
		var loc= RedVector2D[px+15, py+15];
		var obj= RedAgent(world, loc, RedVector2D[0.5, 0.5].rand2, nil, 1, 10);
		obj.state= (\cell: 2.rand, \neighbours: 0);
		px= px+(obj.size*2-1);
		if(px>(width*0.9), {
			px= 0;
			py= py+(obj.size*2-1);
		});
		obj
	}.dup(n);	//world,loc,vel,acc,mass,size
	
	synths= {|i| Synth(\sine, [\amp, 0, \freq, i/n*100+200])}.dup(n);
	s.sync;
	
	//rule= #[[2, 3], [3]];						//rule "23/3" conways game of life
	//rule= #[[2, 3], [3, 6]];					//rule "23/36" highlife
	rule= #[[2, 3], [0, 3]];					//rule "23/30" modded conways game of life
	
	//--loop
	w.draw{
		world.gravity= w.mouse/RedVector2D[width, height]-0.5;
		world.objects.do{|o, i|
			var neighbours= 0;
			world.objects.do{|oo, j|
				if(i!=j, {
					if(o.loc.distance(oo.loc)<(o.size*2).min(oo.size*2), {
						if(oo.state[\cell]==1, {
							neighbours= neighbours+1;
						}, {
							neighbours= neighbours-1;
						});
					});
				});
			};
			o.state[\neighbours]= neighbours;
		};
		world.objects.do{|o, i|
			var neighbours= o.state[\neighbours];
			if(rule[1].includes(neighbours), {
				o.state[\cell]= 1;					//stay/become alive
			}, {
				if(rule[0].includes(neighbours).not, {
					o.state[\cell]= 0;				//dies
				});
			});
		};
		world.objects.do{|o, i|
			//o.addForceWander2D;
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
			synths[i].set(						//simple a/v mapping
				\amp, o.state[\cell]/n,				//cell on= sound on
				\pan, o.loc[0]/width*2-1,			//pan= x position
				\beat, (1-(o.loc[1]/height)*400)		//freq= y position
			);
			
			//--render
			Pen.fillColor= Color.grey(o.state[\cell]*0.85+0.15, 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
})