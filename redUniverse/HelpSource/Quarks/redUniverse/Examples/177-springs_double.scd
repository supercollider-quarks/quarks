//springs double
//with mouse controlling mid point
//illustrates RedSpring's update2 convenience method for interconnecting two objects with one spring

(
	var width= 300, height= 300, w, world, wgrav, obj, obj2, trg, mp, spr1, spr2;
	
	//--world
	w= RedWindow("spring interconnected3", Rect(128, 64, width, height)).front;
	mp= w.mouse;
	wgrav= RedVector2D[0, 0];
	world= RedWorld1(RedVector2D[width, height], wgrav, 30, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	obj= RedObject(world, RedVector2D[0, 0], RedVector2D[0, 0], RedVector2D[0, 0], 1.5, 10);
	obj2= RedObject(world, RedVector2D[width.rand, height.rand], RedVector2D[0, 0], RedVector2D[0, 0], 2, 10);
	trg= RedObject(world, RedVector2D[width*0.5, height*0.5], RedVector2D[0, 0], RedVector2D[0, 0], 1.5, 10);
	
	spr1 = RedSpring(obj, trg, 0.2, 0.97, 120);
	spr2 = RedSpring(obj2, trg, 0.7, 0.92, 100);
	
	//--loop
	w.draw{
		if(mp!=w.mouse, {						//just to keep target in middle if no mouse input
			trg.loc= w.mouse;
		});
		
		spr1.update2;	//here interconnect first white dot with red dot
		spr2.update2;	//here interconnect second white dot with red dot
		obj.update;
		obj2.update;
		trg.update;
		world.contain(obj);
		world.contain(obj2);
		world.contain(trg);
		
		//--render
		Pen.strokeColor= Color.grey;
		Pen.line(obj.loc.asPoint, trg.loc.asPoint);
		Pen.line(obj2.loc.asPoint, trg.loc.asPoint);
		Pen.stroke;
		Pen.fillColor= Color.red;
		Pen.fillOval(Rect.aboutRedObject2D(trg));
		Pen.fillColor= Color.white;
		Pen.fillOval(Rect.aboutRedObject2D(obj));
		Pen.fillColor= Color.white;
		Pen.fillOval(Rect.aboutRedObject2D(obj2));
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
)
