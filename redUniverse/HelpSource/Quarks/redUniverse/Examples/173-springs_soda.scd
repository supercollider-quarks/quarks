//spring construction inspired by soda constructor
//mouse drags around balls

(
	var width= 300, height= 300, w, world, wgrav, sprs, over, n= 10;
	
	//--world
	w= RedWindow("soda springs", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld3(RedVector2D[width, height], wgrav, 30, 0.8); //dim,grav,maxvel,damp
	
	//--objects
	{
		var mass= 2.rrand(5);
		RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[0, 0], RedVector2D[0, 0], mass, mass*2);
	}.dup(n);
	sprs= world.objects.collect{|obj, i|
		[
			//object, target, stiffness, damping, length
			RedSpring(obj, world.objects.wrapAt(i-1), 0.95, 0.5, 25.rrand(50)),
			RedSpring(obj, world.objects.wrapAt(i+1), 0.95, 0.5, 25.rrand(50))
		]
	}.flat;
	w.userView.mouseDownAction= {|view, x, y|
		w.mouse= RedVector2D[x, y];
		over= world.objects.detect{|o| o.containsLoc(w.mouse)};
	};
	w.userView.mouseUpAction= {|view, x, y|
		over= nil;
	};
	
	//--loop
	w.draw{
		if(over.notNil, {
			over.loc= w.mouse;
		});
		
		sprs.do{|spr|
			spr.update;
			
			//--render springs
			Pen.strokeColor= Color.grey;
			Pen.line(spr.object.loc.asPoint, spr.target.loc.asPoint);
			Pen.stroke;
		};
		
		world.objects.do{|obj, i|
			obj.addForce(world.gravity);
			//world.objects.do{|oo, j| if(i!=j, {obj.collide(oo)})};
			obj.update;
			world.contain(obj);
			
			//--render objects
			if(obj==over, {
				Pen.fillColor= Color.red;
			}, {
				Pen.fillColor= Color.grey(0.8, 0.5);
			});
			Pen.fillOval(Rect.aboutRedObject2D(obj));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
