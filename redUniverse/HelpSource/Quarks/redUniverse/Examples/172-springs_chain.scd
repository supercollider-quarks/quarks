//chain of springs.
//mouse controls red ball (target)

(
	var width= 300, height= 300, w, world, wgrav, objs, sprs, trg, mp, n= 18;
	
	//--world
	w= RedWindow("chain of springs", Rect(128, 64, width, height)).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld1(RedVector2D[width, height], wgrav, 30, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	trg= RedObject(world, RedVector2D[width*0.5, height*0.3]);
	objs= {|i|
		RedObject(world, RedVector2D[width*0.5+i, height*0.3+i], RedVector2D[0, 0], RedVector2D[0, 0], 1.5, 5);
	}.dup(n);
	
	sprs= objs.collect{|obj, i|
		var t;
		if(i==0, {t= trg}, {t= objs[i-1]});			//connect springs in a chain
		RedSpring(obj, t, 0.4, 0.5, 8);				//object, target, stiffness, damping, length
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
