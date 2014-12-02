//agents.  swap in new agents now and then
//with mouse controlling world gravity
//with sound where fmamount and rmamount depends on the distance to other agents

(
s.waitForBoot{
	var width= 300, height= 300, w, wgrav, world, n= 4,
		objsFunc, fmbusses, rmbusses, delay, dist, drums;
	
	//--synthesis
	SynthDef(\teknoDrum, {|out= 0, t_trig= 0, fmbusin= 50, fmbusout= 60, rmbusin= 70, rmbusout= 80, fmsend= 0, fmamount= 0, rmsend= 0, rmamount= 0, freq= 60, atk= 0.01, dec= 0.2, pitchatk= 0.01, pitchdec= 0.2, env= 0, pol= 1, amp= 1, pan= 0|
		var dry, wet, src, fmod, rmod, freq2, aenv, penv;
		fmod= InFeedback.ar(fmbusin, 1)*(fmamount*5000);
		rmod= InFeedback.ar(rmbusin, 1);
		penv= EnvGen.kr(Env.perc(pitchatk, pitchdec), t_trig, env*pol);
		freq2= freq+penv;
		aenv= EnvGen.kr(Env.perc(atk, dec), t_trig, t_trig);
		dry= SinOsc.ar(freq2+fmod, 0, aenv);
		wet= dry*rmod;
		src= XFade2.ar(dry, wet, rmamount*2-1);
		Out.ar(fmbusout, src*fmsend);
		Out.ar(rmbusout, src*rmsend);
		Out.ar(out, Pan2.ar(src, pan, amp));
	}).send(s);
	
	SynthDef(\teknoDist, {|in= 0, out= 0, lp= 127, hp= 0, res= 0.3, lfo= 0.1, dp= 0, env= 0.01, spd= 3, dis= 0, mix= 0|
		var dry, wet, lfosc, envfollow;
		dry= Limiter.ar(In.ar(in, 2));
		envfollow= dry.mean;
		envfollow= ((envfollow<0)*(envfollow* -1))+((envfollow>0)*(envfollow));
		envfollow= Clip.ar(LPF.ar(envfollow, spd, 2), 0, 1);
		envfollow= envfollow*env;
		lfosc= SinOsc.ar(lfo, 0, dp)+envfollow;
		wet= RHPF.ar(RLPF.ar(dry, (lp+lfosc).midicps, res), hp.midicps, 2);
		wet= (wet*(dis+1)).softclip;
		Out.ar(out, XFade2.ar(dry, wet, mix*2-1));
	}, #[0, 0, 0.1, 0.1, 0.1, 0, 0, 0, 0, 0, 0]).send(s);
	
	SynthDef(\teknoDelay, {|in= 0, out= 0, lp= 127, hp= 0, lfo= 0.1, dp= 0, bpm= 120, sub= 2, fn= 0, fb= 0.1, mix= 0|
		var dry, wet, lfosc, ms, feedback, del;
		feedback= LocalIn.ar(2);
		ms= bpm.pow(-1)/4*sub*60000;
		lfosc= SinOsc.ar(lfo, 0, dp);
		del= DelayC.ar(feedback, 2, ((lfosc+ms)-(ms/sub*fn)/1000).clip(0, 2), fb);
		dry= Limiter.ar(In.ar(in, 2));
		wet= RHPF.ar(RLPF.ar(dry, lp.midicps, 1), hp.midicps, 1);
		wet= wet+del;
		LocalOut.ar(wet);
		Out.ar(out, XFade2.ar(dry, wet, mix*2-1));
	}, #[0, 0, 0.1, 0.1, 0, 0, 0, 0, 0, 0, 0]).send(s);
	
	SynthDef(\teknoBusses, {|in= 80, out= 70|
		var input, mix0, mix1, mix2, mix3;
		input= In.ar(in, 4);
		mix0= input[1]+input[2]+input[3];
		mix1= input[0]+input[2]+input[3];
		mix2= input[0]+input[1]+input[3];
		mix3= input[0]+input[1]+input[2];
		Out.ar(out, [mix0, mix1, mix2, mix3]);
	}).send(s);
	s.sync;
	
	fmbusses= Synth(\teknoBusses, [\out, 50, \in, 60]);
	rmbusses= Synth(\teknoBusses, [\out, 70, \in, 80]);
	delay= Synth(\teknoDelay, [\out, 0, \in, 12, \mix, 0.3]);
	dist= Synth(\teknoDist, [\out, 12, \in, 10]);
	drums= [
		Synth(\teknoDrum, [\out, 10, \fmbusin, 50, \fmbusout, 60, \rmbusin, 70, \rmbusout, 80]),
		Synth(\teknoDrum, [\out, 10, \fmbusin, 51, \fmbusout, 61, \rmbusin, 71, \rmbusout, 81]),
		Synth(\teknoDrum, [\out, 10, \fmbusin, 52, \fmbusout, 62, \rmbusin, 72, \rmbusout, 82]),
		Synth(\teknoDrum, [\out, 10, \fmbusin, 53, \fmbusout, 63, \rmbusin, 73, \rmbusout, 83]),
	];
	s.sync;
	
	//--world
	w= RedWindow("agents test tekno", Rect(128, 64, width, height)).front;
	wgrav= RedVector2D[0, 0.98];
	world= RedWorld(RedVector2D[width, height], wgrav, 8, 0.1); //dim,grav,maxvel,damp
	
	//--objects
	objsFunc= {									//for creating new agents
		var loc= RedVector2D[width, height].rand;
		var obj= RedAgent(world, loc, RedVector2D[0.5, 0.5].rand2, nil, 1, 10)
			.maxForce_(1.2).wchange_(0.9).wdistance_(5).wradius_(10);
		obj.state= (								//init state with random values
			\fmamount: 0,
			\fmsend: 1,
			\rmamount: 0,
			\rmsend: 1,
			\freq: 128.rand.midicps,
			\atk: 0.1.rand,
			\dec: 0.4.rand,
			\pitchatk: 0.1.rand,
			\pitchdec: 0.3.rand,
			\env: 128.rand.midicps,
			\pol: [-1, 1].choose,
			\amp: Pseq({1.0.rand2.round(0.25).clip(0, 1)}.dup(16), inf).asStream,
			\level: 0
		);
	};
	objsFunc.dup(n);								//create n agents for startup
	
	//--sequence
	Tdef(\seq, {									//play the state of the agents
		loop{
			drums.do{|d, i|						//all 4 instruments
				var amp= world.objects[i].state[\amp].next;
				world.objects[i].state[\level]= amp;
				if(amp>0, {d.set(\t_trig, 1, \amp, amp)});
			};
			(1/(120/60)/4).wait					//tempo
		}
	}).play;
	
	//--loop
	w.draw{
		var replace= world.objects.choose;			//pick an agent at random for possible replace
		if(0.005.coin, {							//to replace or not to replace...
			replace.postln;
			world.remove(replace);
			objsFunc.value;						//die and replace with new one
		});
		world.gravity= w.mouse/RedVector2D[width, height]-0.5;
		world.objects.do{|o, i|
			o.addForceWander2D;
			o.addForceSeek(world.objects.wrapAt(i-1).loc);
			world.objects.do{|oo, j|
				if(i!=j, {
					o.state[\fmamount]= (width-o.loc.distance(oo.loc)/width).clip(0, 1);
					o.state[\rmamount]= (width-o.loc.distance(oo.loc)/width).clip(0, 0.6);
				});
			};
			
			o.addForce(world.gravity);
			o.update;
			world.contain(o);
			drums[i].set(
				\fmamount, o.state[\fmamount],
				\fmsend, o.state[\fmsend],
				\rmamount, o.state[\rmamount],
				\rmsend, o.state[\rmsend],
				\freq, o.state[\freq],
				\atk, o.state[\atk]*(3-o.vel.mag).clip(0, 1),
				\dec, o.state[\dec]*(1000-o.age/1000).clip(0, 1),
				\pitchatk, o.state[\pitchatk]*(o.wtheta/2pi),
				\pitchdec, o.state[\pitchdec],
				\env, height-o.loc[1],	//\env, o.state[\env],
				\pol, o.state[\pol],
				\pan, o.loc[0]/width*2-1
			);
			
			//--render
			Pen.fillColor= Color.yellow(o.state[\level]*0.85+0.15, 0.5);
			Pen.fillOval(Rect.aboutRedObject2D(o));
		};
	};
	w.animate= true;
	CmdPeriod.doOnce({if(w.isOpen, {w.close})});
	w.onClose= {
		Tdef(\seq).stop;
		fmbusses.free;
		rmbusses.free;
		delay.free;
		dist.free;
		drums.do{|x| x.free};
	};
})