//if preferred freq match with the human +-100 then play sound and move towards target
//sound: audio input, matching agent's sounds

(
s.waitForBoot{
	var width= 640, height= 480, w, world, n= 20,
		syn, buf, bus, human;
	
	//--world
	w= RedWindow("track test1", Rect(128, 64, width, height)).front;
	world= RedWorld3(RedVector2D[width, height], damping:0.5); //dim,grav,maxvel,damp
	
	//--synthesis
	SynthDef(\beating, {|out= 0, freq= 400, beat= 1, lp= 800, amp= 0, pan= 0|
		var z;
		z= RLPF.ar(Mix(LFSaw.ar(freq+[0, beat], 0, amp)), lp);
		Out.ar(out, Pan2.ar(z, pan));
	}).send(s);
	buf= Buffer.alloc(s, 2048, 1);
	SynthDef(\human, {|out, bufnum, in= 0|
		var input, chain, event, delta, freq, amp, timbre, centroid, hasFreq;
		input= SoundIn.ar(in);
		chain= FFT(bufnum, input);
		event= A2K.kr(PV_JensenAndersen.ar(chain));
		delta= Timer.kr(event);
		#freq, hasFreq= Pitch.kr(input);
		freq= freq*hasFreq;
		amp= Amplitude.kr(input);
		timbre= SpecFlatness.kr(chain);
		centroid= SpecPcile.kr(chain, 0.5);
		Out.kr(out, [event, delta, freq, amp, timbre, centroid])}).send(s);
	s.sync;
	syn= Synth(\human, [\out, 10, \bufnum, buf.bufnum]);//output 6 ch control data from tracker
	bus= Bus(\control, 10, 6);						//access control data at ch 10-15
	
	//--objects
	{|i|
		var loc= RedVector2D[width, height].rand;		//initial xy location
		var mass= 2.rrand(20);
		var a= RedAgent(world, loc, RedVector2D[0.001, 0.001].rand2, nil, mass, mass); //world,loc,vel,acc,mass,size
		a.maxForce= 1;
		a.wdistance= 2;
		a.wchange= 0.9;
		a.state= (
			\synth: Synth(\beating),
			\prefFreq: mass.linexp(2, 20, 2000, 200),	//a favourite pitch depends on mass
			\target: nil							//location for that pitch if found
		);
		a.sense= {
			var listen= human.state.freq;
			if(listen>0 and:{(a.state.prefFreq-listen).abs<100}, {
				a.state.target= human.loc;
				a.state[\synth].set(
					\freq, a.state.prefFreq,
					\amp, 1/n*(width-a.loc.distance(human.loc)/width).pow(2)
				);
			}, {
				a.state[\synth].set(\amp, 0);
				a.state.target= nil;
				a.addForceWander2D;
			});
		};
		a.act= {
			if(a.state.target.notNil, {
				a.addForceArrive(a.state.target);
			});
		}
		
	}.dup(n);
	human= RedAgent(world, RedVector2D[100, 100]);
	human.state= (
		\event: 0,
		\deltax: 0,
		\freq: 0,
		\amp: 0,
		\timbre: 0,
		\centroid: 0
	);
	human.sense= {								//listen and update state
		bus.getn(6, {|arr|
			human.state.event= arr[0];
			human.state.deltax= arr[1];
			human.state.freq= arr[2];
			human.state.amp= arr[3];
			human.state.timbre= arr[4];
			human.state.centroid= arr[5];
		})
	};
	s.sync;
	
	//--loop
	w.draw{
		world.objects.do{|o, i|						//iterate all objects in this world
			human.loc= w.mouse;
			o.update;								//object update
			world.contain(o);						//keep within world borders
			
			//--render
			if(o.state.event.notNil, {
				Pen.fillColor= Color.blue(0.7, 0.5);
			}, {
				if(o.state.target.notNil, {
					Pen.fillColor= Color.yellow;
				}, {
					Pen.fillColor= Color.grey(0.7, 0.5);
				});
			});
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
		//human.state.freq.postln;
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {
		syn.free;
		bus.free;
		buf.free;
		world.objects.do{|o| o.state.at(\synth).free};
	};
})
