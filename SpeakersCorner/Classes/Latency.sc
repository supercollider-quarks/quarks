/* 
	Latency.testAudio(2, 1);
	Latency.stop;
*/ 

Latency { 
	classvar <syn, <resp, <latencies, <serv, lastTime;
	classvar <>verbose = false;
	
	*initClass { 
		Class.initClassTree(OSCresponderNode); 
		
		// register to receive latency values
		resp = OSCresponderNode(nil,'/tr',{ arg time,responder,msg;
			var which = msg[2], exactTime = msg[3], delta; 
			
			if ( verbose ) { msg.postln; };
		//	msg.postcs; 
		//	[exactTime, which].dump;
			if ( syn.notNil ){ 
				// make sure we only listen to the trigger coming from the latency measurement synth
				if ( msg[1] == syn.nodeID ){ 
					[ 	{ 	delta = (exactTime - lastTime[1]); 
						[lastTime[0], delta, delta / serv.sampleRate].postln; 
						latencies[lastTime[0] - 1] = delta;
					},
						{ lastTime = [which, exactTime] }
					].clipAt(which).value;
				};
			};
		});
	}
	
	*testAudio { |numChans=5, maxDT = 0.2, server, inChan=0|
		serv = server ? Server.default;
		latencies = Array.newClear(numChans); 
		resp.remove;
		resp.add;
		syn = { 	var pulses, audIn, phase; 
			var pulseFreq = (maxDT * 2 * numChans).reciprocal;
			pulseFreq;
			
			phase = Phasor.ar(0, 1, 0, 2 ** 30);	// time in samples
			audIn = SoundIn.ar(inChan);				// mike input
			
			pulses =  Decay2.ar(
				Impulse.ar( pulseFreq, 
					0.99 - ((0..numChans - 1) / numChans) // phase
				), 0.0, 0.002
			);
				// send when audioin triggers
			SendTrig.ar( Trig1.ar(audIn > 0.1, 0.05), 0, phase);
				// send when each output plays a trigger
			SendTrig.ar(pulses > 0.1, (1..numChans), phase);
			(pulses ++ [ Silent.ar, audIn ])
		}.play(serv);	
	}
	
	*stop { 
		resp.remove;
		syn.free; 
		this.post;
	}
	
	*post { 
		"// measured latencies:".postln;  
		"in samples: ".post; latencies.postcs;
		"in seconds: ".post; (latencies / serv.sampleRate).postcs;
	}
}