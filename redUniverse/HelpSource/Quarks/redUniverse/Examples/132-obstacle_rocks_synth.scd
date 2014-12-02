//test of many obstacles.
//with sound as they hit rocks

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 6, nRocks= 15, rocks, objs;
	
	//--synthesis
	SynthDef(\pmdrim, {|out= 0, freq= 400, freq2= 200, dur= 0.1, amp= 1, pan= 0|
		var z, e;
		e= EnvGen.ar(Env.perc(0.01, dur), doneAction:2);
		z= SinOsc.ar(freq, SinOsc.ar(XLine.ar(freq2, 1, dur), 0, pi), amp)+ClipNoise.ar(e*0.001);
		Out.ar(out, Pan2.ar(z*e, pan));
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("rocks pinball test", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];					//world xy gravity
	world= RedWorld3(RedVector2D[width, height], wgrav, 15, 0.5); //dim,grav,maxvel,damp
	
	//--objects
	objs= {
		var loc= RedVector2D[250, 10].rand;			//initial xy location
		var vel= RedVector2D[3.5, 0].rand;			//initial velocity
		var acc= RedVector2D[0, 0];					//initial acceleration
		RedObject(world, loc, vel, acc, 1.5, 12); //world,loc,vel,acc,mass,size
	}.dup(n);
	rocks= {
		RedRock(world, RedVector2D[width.rand.round(50), (height*0.5).rand.round(50)+(height*0.4)])
	}.dup(nRocks);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.grey(0.7, 0.9);
		objs.do{|o, i|
			o.addForce(o.world.gravity);			//add gravity
			objs.copyToEnd(i+1).do{|oo| o.collide(oo)};
			o.update;								//object update
			world.contain(o);						//keep within world borders
			Pen.addOval(Rect.aboutRedObject2D(o));
		};
		Pen.stroke;
		
		Pen.fillColor= Color.blue(1, 0.5);
		rocks.do{|o, i|
			objs.do{|oo, j|
				if(o.collide(oo, 10), {
					Synth(\pmdrim, [
						\freq, (height-oo.loc[1]/height).linexp(0, 0.5, 200, 400),
						\freq2, (j/n).linexp(200, 300),
						\amp, 0.2,
						\pan, oo.loc[0]/width*2-1
					]);
				});
			};
			Pen.addOval(Rect.aboutRedObject2D(o));
		};
		Pen.fill;
		
		/*
		world.objects.do{|o, i|						//iterate all objects in this world
			o.addForce(o.world.gravity);			//add gravity
			world.objects.copyToEnd(i+1).do{|oo|
				if(o.isKindOf(RedRock).not, {
				if(o.collide(oo, 10), {
//					if(oo.species!=o.species, {
						Synth(\pmdrim, [
							\freq, (height-o.loc[1]/height).linexp(0, 0.5, 200, 400),
							\freq2, (i/n).linexp(200, 300),
							\amp, 0.1,
							\pan, o.loc[0]/width*2-1
						]);
					});
				});
			};
			o.update;								//object update
			world.contain(o);						//keep within world borders
			
			//--render
			if(o.isKindOf(RedRock), {
				Pen.fillColor= Color.blue(1, 0.5);
				Pen.fillOval(Rect.aboutRedObject2D(o));
			}, {
				Pen.strokeColor= Color.grey(0.7, 0.9);
				Pen.strokeOval(Rect.aboutRedObject2D(o));
			});
		}
		*/
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
}
)
