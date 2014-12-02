
XiiLoadXiiLangInstr {
	
	*new {
		^super.new.initXiiLoadXiiLangInstr;
		}
		
	initXiiLoadXiiLangInstr {
		
		SynthDef(\xlang_shake, {arg out=0, amp=1;
				var buffer, player, env, signal;
				buffer = Buffer.read(Server.default, "/Users/thor/Library/Application Support/SuperCollider/sounds/insand/short/insects/insec2");
				player= if(buffer.numChannels==1, { 
					PlayBuf.ar(1, buffer, 1)!2 }, {
					PlayBuf.ar(2, buffer, 1)});
				env = EnvGen.ar(Env.perc(0.01, 0.4), doneAction:2);
				signal = player * env * amp;
				Out.ar(out, signal);
		}).memStore;
		
		/*
		 Synth(\xlang_shake)
		*/
		
		
		SynthDef(\xlang_insec, {arg out=0, amp=1;
				var buffer, player, env, signal;
				buffer = Buffer.read(Server.default, "/Users/thor/Library/Application Support/SuperCollider/sounds/insand/short/insects/insec");
				player= if(buffer.numChannels==1, { 
					PlayBuf.ar(1, buffer, 1)!2 }, {
					PlayBuf.ar(2, buffer, 1)});
				env = EnvGen.ar(Env.perc(0.01, 0.4), doneAction:2);
				signal = player * env * amp;
				Out.ar(out, signal);
		}).memStore;
		
		/*
		 Synth(\xlang_insec)
		*/
		
		SynthDef(\bass, {arg out, freq=220, amp=0.4;
			var env, signal;
			env = EnvGen.ar(Env.perc(0.01, 0.4), doneAction:2);
			signal = SinOsc.ar([freq/2, (freq/2)+2], 0, amp) * env;
			Out.ar(out, signal*env);
		}).memStore;

		/*
		 Synth(\xlang_bass, [\freq, 344])
		*/

		
		SynthDef(\moog, {arg out=0, freq=220, amp=0.4;
			var env, signal;
			env = EnvGen.ar(Env.perc(0.01, 0.4), doneAction:2);
			signal = MoogFF.ar(Saw.ar([freq, freq+2], amp), 4*freq, 3) * env;
			Out.ar(out, signal*env);
		}).memStore;

		/*
		 Synth(\xlang_moog, [\freq, 344])
		*/
		
		
		
	}
}