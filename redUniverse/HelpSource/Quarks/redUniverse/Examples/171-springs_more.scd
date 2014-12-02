//more springs.  RedSpring is just a simple helper class
//mouse controls red ball (target)

(
	var width= 300, height= 300, w, world, wgrav, objs, sprs, trg, mp, n= 8;
	
	//--world
	w= RedWindow("more springs", Rect(128, 64, width, height)).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0];						//no gravity
	world= RedWorld1(RedVector2D[width, height], wgrav, 30, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	trg= RedObject(world, RedVector2D[width*0.5, height*0.5]);
	objs= {
		var mass= 2.0.rand+1;
		RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[0, 0], RedVector2D[0, 0], mass, mass*10);
	}.dup(n);
	sprs= objs.collect{|obj|
		RedSpring(obj, trg, 0.3, 0.9, 150.rand);		//object, target, stiffness, damping, length
	};
	
	//--loop
	w.draw{
		if(mp!=w.mouse, {							//keeps target in middle if no mouse input
			trg.loc= w.mouse;
		});
		
		sprs.do{|spr|
			spr.update;
			
			//--render springs
			Pen.strokeColor= Color.grey;
			Pen.line(spr.object.loc.asPoint, spr.target.loc.asPoint);
			Pen.stroke;
		};
		
		objs.do{|obj|
			obj.addForce(world.gravity);
			obj.update;
			world.contain(obj);
			
			//--render objects
			Pen.fillColor= Color.grey(0.5, 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(obj));
		};
		
		//--render mouse
		Pen.fillColor= Color.red;
		Pen.fillOval(Rect.aboutRedObject2D(trg));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
