//redFrik 050328, 121126

RedAGrain {
	var server, <synth, <trigBus, <bufBus, <rateBus, <posBus, <durBus, <panBus, <ampBus;
	*new {|server|
		^super.new.initRedXGrain(server);
	}
	initRedXGrain {|argServer|
		server= argServer ?? Server.default;
		trigBus= Bus.audio(server);		//create busses for lfos synths
		bufBus= Bus.audio(server);
		rateBus= Bus.audio(server);
		posBus= Bus.audio(server);
		durBus= Bus.audio(server);
		panBus= Bus.audio(server);
		ampBus= Bus.audio(server);
	}
	*initClass {
		ServerBoot.addToAll({			//build synthdef at server boot
			SynthDef(\redAGrain, {
				|out= 0, attackTime= 0.01, gate= 1,
					trigBus, bufBus, rateBus, posBus, durBus, panBus, ampBus|
				var e, z, inTrig, inBus, inRate, inPos, inDur, inPan, inAmp;
				inTrig= In.ar(trigBus);
				inBus= In.ar(bufBus);
				inRate= In.ar(rateBus);
				inPos= In.ar(posBus);
				inDur= In.ar(durBus);
				inPan= In.ar(panBus);
				inAmp= In.ar(ampBus);
				e= EnvGen.ar(Env.asr(attackTime), gate, doneAction: 2);
				z= TGrains.ar(
					2,
					inTrig,
					inBus,
					inRate,
					inPos*BufDur.kr(inBus),
					inDur,
					inPan,
					inAmp,
					2
				);
				Out.ar(out, z*e);
			}).add;
		});
	}
	start {|out= 0, attackTime= 0.01|	//expects lfo synths mapped to controller busses
		synth= Synth.tail(server, this.prSynthName, [
			\out, out,
			\attackTime, attackTime,
			\trigBus, trigBus.index,
			\bufBus, bufBus.index,
			\rateBus, rateBus.index,
			\posBus, posBus.index,
			\durBus, durBus.index,
			\panBus, panBus.index,
			\ampBus, ampBus.index
		]);
	}
	stop {|releaseTime= 0.1|
		synth.release(releaseTime);
	}
	free {
		this.stop(0);
		trigBus.free;
		bufBus.free;
		rateBus.free;
		posBus.free;
		durBus.free;
		panBus.free;
		ampBus.free;
	}
	prSynthName {^"redAGrain"}
}

RedKGrain : RedAGrain {
	initRedXGrain {|argServer|
		server= argServer ?? Server.default;
		trigBus= Bus.control(server);		//create busses for lfos synths
		bufBus= Bus.control(server);
		rateBus= Bus.control(server);
		posBus= Bus.control(server);
		durBus= Bus.control(server);
		panBus= Bus.control(server);
		ampBus= Bus.control(server);
	}
	*initClass {
		ServerBoot.addToAll({				//build synthdef at server boot
			SynthDef(\redKGrain, {
				|out= 0, attackTime= 0.01, gate= 1,
					trigBus, bufBus, rateBus, posBus, durBus, panBus, ampBus|
				var e, z, inTrig, inBus, inRate, inPos, inDur, inPan, inAmp;
				inTrig= In.kr(trigBus);
				inBus= In.kr(bufBus);
				inRate= In.kr(rateBus);
				inPos= In.kr(posBus);
				inDur= In.kr(durBus);
				inPan= In.kr(panBus);
				inAmp= In.kr(ampBus);
				e= EnvGen.kr(Env.asr(attackTime), gate, doneAction: 2);
				z= TGrains.ar(
					2,
					inTrig,
					inBus,
					inRate,
					inPos*BufDur.kr(inBus),
					inDur,
					inPan,
					inAmp,
					2
				);
				Out.ar(out, z*e);
			}).add;
		});
	}
	prSynthName {^"redKGrain"}
}
