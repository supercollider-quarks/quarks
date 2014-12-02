//multiple objects in a 3d world
//with simple sound - individual fm sounds as they bounce

(
s.waitForBoot{
	var width= 300, height= 300, depth= 300, w, wgrav, world, n= 10,
		ff= 1.25, ss= 0.25, synths;
	
	//--synthesis
	SynthDef("fm3Dtest", {|out= 0, t_trig= 0, amp= 0.1, dur= 0.1, freq= 400, pan= 0, freq2= 600|
		Out.ar(out,
			Pan2.ar(
				EnvGen.ar(Env.perc(0.01, dur), t_trig)
				*SinOsc.ar(SinOsc.ar(Lag.kr(freq, 0.2), 0, 40, freq2), 0, amp),
				pan
			)
		);
	}).send(s);
	s.sync;
	
	//--world
	w= RedWindow("3d test", Rect(128, 64, width, height)).front;
	wgrav= RedVector3D[0.1, 0.98, 0.1];
	world= RedWorld3(RedVector3D[width, height, depth], wgrav, 20, 0.1); //dim,grav,maxvel,damp
	//world= RedWorld2(RedVector3D[width, height, depth], wgrav, 20, 0.92); //dim,grav,maxvel,damp
	
	//--objects
	{
		var mass= 5.0.rand;
		RedObject(world, RedVector3D[width, height, depth].rand, RedVector3D[2, 2, 2].rand2, RedVector3D[2, 2, 2].rand2*10, mass, mass*5); //world,loc,vel,acc,mass,size
	}.dup(n);
	synths= {Synth("fm3Dtest", [\amp, 0.5/n])}.dup(n);
	
	//--loop
	w.draw{
		Pen.strokeColor= Color.grey(0.5);
		Pen.strokeRect(Rect.aboutRedVector3D(RedVector[width/2, height/2, depth], width/2, width, height, depth, ff, ss));
		Pen.strokeColor= Color.white;
		Pen.strokeRect(Rect.aboutRedVector3D(RedVector[width/2, height/2, 0], width/2, width, height, depth, ff, ss));
		world.objects.do{|o, i|
			var flag= false, dep;
			o.addForce(world.gravity);
			o.update;
			flag= world.contains(o).not;
			world.contain(o);
			dep= depth-o.loc[2]/(depth/2);
						if(flag, {
				synths[i].set(
					\t_trig, 1,
					\amp, dep*((o.mass/5).max(0.2)/n),
					\rate, dep*2,
					\dur, dep*0.5,
					\freq, o.loc[1].linexp(height, 0, 200, 2000),
					\freq2, o.mass.linlin(0, 5, 1200, 200),
					\pan, o.loc[0].linlin(0, width, dep.neg, dep)
				);
			});
			
			//--render
			Pen.fillColor= Color.grey(dep, 0.5);
			Pen.fillOval(Rect.aboutRedObject3D(o, ff, ss));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {synths.do{|x| x.free}};
})
