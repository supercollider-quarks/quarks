//listener position test
//with mouse controlling listening position/mix
//with sound: freq mapped to y, pan mapped to x, mass mapped to beating, amp and lowpass

(
s.waitForBoot{
	var width= 640, height= 480, w, world, n= 10, synths;
	
	//--world
	w= RedWindow("listening pos test", Rect(128, 64, width, height)).front;
	world= RedWorld3(RedVector2D[width, height], damping:0.5);		 //dim,grav,maxvel,damp
	
	//--synthesis
	SynthDef(\beating, {|out= 0, freq= 400, beat= 1, lp= 800, amp= 0, pan= 0|
		var z;
		z= RLPF.ar(Mix(LFSaw.ar(freq+[0, beat], 0, amp)), lp);
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	s.sync;
	
	//--objects
	{
		var loc= RedVector2D[width, height].rand;		//initial xy location
		var mass= 1.rrand(10);
		RedAgent(world, loc, RedVector2D[0.001, 0.001].rand2, nil, mass, mass) //world,loc,vel,acc,mass,size
			.maxForce_(0.001)
			.wdistance_(10)
			.wchange_(0.9)
			.state_((\synth: Synth(\beating)))
	}.dup(n);
	s.sync;
	
	//--loop
	w.draw{
		Pen.width= 1;
		4.do{|i|
			Pen.fillColor= Color.grey(0.4, i+1/(4));
			Pen.fillOval(Rect.aboutRedVector2D(w.mouse, (4-i)/3*50));
		};
		world.objects.do{|o, i|						//iterate all objects in this world
			o.addForceWander2D;
			o.update;								//object update
			world.contain(o);						//keep within world borders
			
			o.state[\synth].set(
				\freq, (height-o.loc[1]/height).linexp(0, 1, 200, 2000),
				\beat, (10-o.mass).linlin(1, 10, 0.5, 5),
				\lp, (10-o.mass).linexp(1, 10, 200, 2000),
				\pan, o.loc[0]/width*2-1,
				\amp, (width-o.loc.distance(w.mouse)/width).pow(8)
					*(o.mass).linlin(1, 10, 0, 2/n)
			);
			
			//--render
			Pen.strokeColor= Color.grey(0.7, 0.5);
			Pen.strokeOval(Rect.aboutRedObject2D(o));
		}
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {world.objects.do{|o| o.state.at(\synth).free}};
})
