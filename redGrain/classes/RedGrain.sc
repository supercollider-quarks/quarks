//redFrik 050331-050607, 121126

RedGrain {
	var <>delta= 0.005, <>buf, <>rate= 1, <>pos= 0, <>dur= 0.2, <>pan= 0, <>amp= 1,
		<>mute= false, <>latency= 0.05, <server, task;
	*new {|server|
		^super.new.initRedGrain(server);
	}
	initRedGrain {|argServer|
		server= argServer ?? Server.default;
	}
	*initClass {
		ServerBoot.addToAll({			//build synthdef at server boot
			SynthDef(\redGrain, {
				|out= 0, bufnum= 0, rate= 1, pos= 0, dur= 1, pan= 0, amp= 1|
				var e, z;
				e= EnvGen.ar(Env.sine(dur), 1, amp*0.1, doneAction: 2);
				z= PlayBuf.ar(
					1,
					bufnum,
					rate*BufRateScale.ir(bufnum),
					1,
					pos*BufSamples.ir(bufnum),
					1
				);
				OffsetOut.ar(out, Pan2.ar(z*e, pan));
			}).add;
		});
	}
	start {|out= 0|
		var synthName= this.prSynthName;
		if(buf.isNil, {"RedGrain: set a buffer first".warn; this.halt});
		mute= false;
		task= Task({
			inf.do{|i|
				if(mute.not, {
					server.sendBundle(latency, [\s_new, synthName, -1, 0, 0,
					//server.sendBundle(latency, [\s_new, synthName, -1, 1, 0,
						\out, out,
						\bufnum, buf.value(i).bufnum,
						\rate, rate.value(i),
						\pos, pos.value(i),
						\dur, dur.value(i),
						\pan, pan.value(i),
						\amp, amp.value(i)
					]);
				});
				delta.value(i).wait;
			};
		}).play;
	}
	stop {task.stop}
	pause {task.pause}
	resume {task.resume}
	prSynthName {^"redGrain"}
}

//this version does not use OffsetOut.  less artifacts when timestretching - just lazy?
RedGrain2 : RedGrain {
	*initClass {
		ServerBoot.addToAll({		//build synthdef at server boot
			SynthDef(\redGrain2, {
				|out= 0, bufnum= 0, rate= 1, pos= 0, dur= 1, pan= 0, amp= 1|
				var e, z;
				e= EnvGen.ar(Env.sine(dur), 1, amp*0.1, doneAction: 2);
				z= PlayBuf.ar(
					1,
					bufnum,
					rate*BufRateScale.ir(bufnum),
					1,
					pos*BufSamples.ir(bufnum),
					1
				);
				Out.ar(out, Pan2.ar(z*e, pan));
			}).add;
		});
	}
	prSynthName {^"redGrain2"}
}