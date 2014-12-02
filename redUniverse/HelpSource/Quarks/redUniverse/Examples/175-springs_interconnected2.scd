//springs interconnected2.  monster
//with mouse controlling mid point
//with simple sounds mapped to each object.

(
s.waitForBoot{
	
	var width= 640, height= 480, wgrav, w, world,
		n= 18, mp, objs, sprs, syns, cent= RedVector2D[width/2, height/2];
	
	//--synthesis
	SynthDef(\imp2, {|out= 0, freq= 0, phase= 0, amp= 0, pan= 0|
		var z;
		z= SinOsc.ar(freq, phase.lagud(0, 0.075), amp);
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("springs interconnected2", Rect(128, 64, width, height), false).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0];						//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 999, 0); //dim,grav,maxvel,damp
	
	//--objects
	objs= {
		var mass= 2;
		RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[0, 0], RedVector2D[0, 0], mass, mass);
	}.dup(n);
	sprs= objs.collect{|x|
		objs.collect{|y|
			if(x!=y and:{0.25.coin}, {
				RedSpring(x, y, 0.2, 0.5, 200.rrand(300));
			});
		}.select{|x| x.notNil};
	}.flat;
	syns= {Synth(\imp2)}.dup(n);
	
	//--loop
	w.draw{
		
		//--render
		Pen.smoothing= true;
		Pen.strokeColor= Color.white;
		sprs.do{|spr|
			spr.update;
			
			//--render springs
			Pen.moveTo(spr.object.loc.asPoint);
			Pen.lineTo(spr.target.loc.asPoint);
		};
		Pen.stroke;
		
		Pen.fillColor= Color.yellow;
		objs.do{|obj, i|
			obj.loc= obj.loc+(w.mouse-cent*0.5*0.01);
			obj.update;
			world.contain(obj);
			
			syns[i].set(
				\amp, (obj.vel.abs.maxItem*0.01).min(0.1),
				\freq, obj.vel.minItem,
				\phase, obj.loc.distance(cent)*50,
				\pan, obj.loc[0].linlin(0, width, -1, 1)
			);
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
}
)
