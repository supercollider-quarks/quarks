//patterns and particles

(
	var width= 640, height= 480, w, wgrav, world;
	
	//--world
	w= RedWindow("patterns and particles", Rect(128, 64, width, height)).front;
	w.userView.clearOnRefresh= true;
	w.background= Color.white;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector[width, height], wgrav, 30, 0.1); //dim,grav,maxvel,damp
	~world= world;
	
	//--loop
	w.draw{
		var dead= List.new;
		
		world.objects.do{|o, i|
			o.addForce(world.gravity);
			o.update;
			if(o.age<=0, {dead.add(o)});
			
			//--render
			Pen.fillColor= Color.red;
			Pen.addOval(Rect.aboutRedVector2D(o.loc, o.age));
			Pen.fill;
		};
		
		dead.do{|o| world.remove(o)};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)

//keep reduniverse window open and run the code below line by line
s.boot

(
~createObj= {|x= 0.5, y= 0.5, vx= 0, vy= 0, size= 10, dur= 40|
	var p;
	p= RedParticle(~world, RedVector[x*~world.dim[0], y*~world.dim[1]], RedVector[vx, vy], 0, size, size);
	p.age= dur;
};
)

Pbind(\dur, 0.5, \degree, Pseq([0, 1, 2, 3, 4]), \finish, Pfunc{|ev| ~createObj.value}).play

Pbind(\dur, 0.25, \degree, Pseq([0, 1, 2, 3, 4, 9, 5, 2, 3, 5, 2, 1, 0, 0, 0]), \finish, Pfunc{|ev| ~createObj.value(x: ev.degree/11)}).play

Pbind(\dur, Pseg(Pseq([0.5, 0.1, 0.08, 0.5]), Pseq([5, 1, 3])), \degree, Pseg(Pseq([0, 15, 12, 0]), Pseq([5, 2, 3])), \finish, Pfunc{|ev| ~createObj.value(y: ev.degree.linlin(0, 15, 0.75, 0.25), vx: ev.dur.linlin(0.08, 0.5, 10, 1).rand2, dur: ev.dur.linlin(0.08, 0.5, 30, 50))}).play

Pbind(\dur, 0.125, \amp, Pn(Pshuf([0.1, 0.1, 0.1, 0.05, 0, 0], 4), 4), \degree, Pn(Pshuf([0, 2, 3, 5, 6, 8], 4), 4), \finish, Pfunc{|ev| ~createObj.value(y: ev.degree.lincurve(0, 8, 0.75, 0.25, 3), vy: 1.0.rand2, vx: (ev.degree%2)-0.5*20, dur: ev.amp*200, size: 200)}).play
