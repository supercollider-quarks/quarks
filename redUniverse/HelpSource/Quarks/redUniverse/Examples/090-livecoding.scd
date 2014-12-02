//live coding objects and worlds

(
	//~world
	//~win
	
	var width= 300, height= 300, wgrav;
	
	//--world
	~win= RedWindow("livecoding test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[-0.4, 0.98];
	~world= RedWorld3(RedVector2D[width, height], wgrav, 20, 0.25); //dim,grav,maxvel,damp
	
	//--loop
	~win.draw{
		Pen.strokeColor= Color.white;
		~world.objects.do{|o|
			o.addForce(o.world.gravity);
			o.update;
			o.world.contain(o);
			
			//--render
			Pen.strokeRect(Rect.aboutRedObject2D(o));
		};
		//Pen.strokeRect(Rect.aboutRedVector2D(~win.mouse, 10));
	};
	~win.animate= true;
	CmdPeriod.doOnce({if(~win.isOpen, {~win.close})});
)

//--swapping in new object
a= RedObject(~world, RedVector2D[100, 100], 0, 0, 1.5, 10)
b= RedObject(~world, RedVector2D[200, 100], RedVector2D[5, -5], 0, 1.5, 20)
c= RedObject(~world, mass:5, size:5)
~world.objects.remove(a)
~world.objects.remove(c)
~world.objects= [a, b, c]

a.size= 50
b.size= 100
b.mass= 0.25
c.loc= RedVector2D[100, 100]

~world.gravity= RedVector2D[0.1, 0.1].rand2
