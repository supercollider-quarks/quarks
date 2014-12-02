//redFrik

//--todo:
//gui class
//make left/right speaking voice.  formant synthesis?


RedTest {
	var buf, syn, <sfGrp;
	
	//--pseudo ugen
	*ar {|amp= 1, pan= 0|
		^Pan2.ar(Mix(SinOsc.ar([400, 404], 0, LFNoise0.kr(5).max(0)*amp*0.5)), pan)
	}
	
	//--soundfile
	*sf {|out= 0, group|
		^super.new.initRedTestSF(out, group, 1);
	}
	*sf2 {|out= 0, group|
		^super.new.initRedTestSF(out, group, 2);
	}
	initRedTestSF {|out, group, channels|
		sfGrp= group ? Server.default.defaultGroup;
		Routine.run{
			var files= ["sounds/a11wlk01.wav", Platform.resourceDir+/+"sounds/a11wlk01.wav"];
			sfGrp.server.bootSync;
			files.do{|x|
				if(buf.isNil and:{File.exists(x)}, {
					buf= Buffer.read(sfGrp.server, x);
				});
			};
			if(buf.isNil, {
				(this.class.name++": file not found").error;
			});
			sfGrp.server.sync;
			if(channels==1, {
				SynthDef(\RedTestSF, {|out, buf|
					Out.ar(out, PlayBuf.ar(1, buf, 1, 1, 0, 1));
				}).send(sfGrp.server);
				sfGrp.server.sync;
				syn= Synth.head(sfGrp, \RedTestSF, [\out, out, \buf, buf]);
			}, {
				SynthDef(\RedTestSF2, {|out, buf|
					Out.ar(out, Pan2.ar(PlayBuf.ar(1, buf, 1, 1, 0, 1), 0));
				}).send(sfGrp.server);
				sfGrp.server.sync;
				syn= Synth.head(sfGrp, \RedTestSF2, [\out, out, \buf, buf]);
			});
		}
	}
	sfBus_ {|out|
		syn.set(\out, out);
	}
	sfFree {
		syn.free;
		buf.free;
	}
	
	//--speaker tests
	*speaker {|channels, amp= 1, dur= 1|
		Routine.run{
			Server.default.bootSync;
			channels= channels ? [0, 1];
			SynthDef(\redTestPink, {|out= 0, gate= 1, amp= 1|
				var e= EnvGen.kr(Env.perc, gate, doneAction:2);
				var z= PinkNoise.ar(e*amp);
				Out.ar(out, z);
			}).add;
			Server.default.sync;
			Pbind(\instrument, \redTestPink, \dur, dur, \out, Pseq(channels, inf), \amp, amp).play;
		};
	}
	*speaker2 {|channels, amp= 1, dur= 1|
		Routine.run{
			Server.default.bootSync;
			channels= channels ? [0, 1];
			SynthDef(\redTestPing, {|out= 0, gate= 1, freq= 400, amp= 1|
				var e= EnvGen.kr(Env.perc, gate, doneAction:2);
				var z= SinOsc.ar(freq, 0, e*amp);
				Out.ar(out, z);
			}).add;
			Server.default.sync;
			Pbind(\instrument, \redTestPing, \dur, dur, \out, Pseq(channels, inf), \degree, Pseq(channels, inf), \amp, amp).play;
		};
	}
}
