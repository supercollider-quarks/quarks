// SCFreqScope by Lance Putnam
// adapted by ixi for specific purposes

XiiSCFreqScope {

	classvar <server;
	var <scopebuf, <fftbuf;
	var <active, <node, <inBus, <dbRange, dbFactor, rate, <freqMode;
	var <>scope;
	
	*viewClass { ^GUI.scopeView }
	
	*initClass { server = Server.default }
	
	*new { arg parent, bounds;
		bounds.width = 511;
		^super.new.initSCFreqScope(parent, bounds)
	}
	
	initSCFreqScope {arg parent, bounds;
		scope = GUI.scopeView.new(parent, bounds);
		active=false;
		inBus=0;
		dbRange = 96;
		dbFactor = 2/dbRange;
		rate = 4;
		freqMode = 0;
		server = Server.default;
		
		node = server.nextNodeID;
	}
	
	sendSynthDefs {
		// dbFactor -> 2/dbRange
		
		// linear
		SynthDef(\XiiFreqScope0, { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, numSamples, mul, add;
			mul = 0.00285;
			numSamples = (BufSamples.kr(fftbufnum) - 2) * 0.5; // 1023 (bufsize=2048)
			signal = In.ar(in, 2);
			signal = Mix.ar(signal);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			// -1023 to 1023, 0 to 2046, 2 to 2048
			phasor = LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, numSamples, numSamples + 2);
			phasor = phasor.round(2); // the evens are magnitude
			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(server);
		
		// logarithmic
		SynthDef(\XiiFreqScope1, { arg in=0, fftbufnum=0, scopebufnum=1, rate=4, phase=1, dbFactor = 0.02;
			var signal, chain, result, phasor, halfSamples, mul, add;
			mul = 0.00285;
			halfSamples = BufSamples.kr(fftbufnum) * 0.5;
			signal = In.ar(in, 2);
			signal = Mix.ar(signal);
			chain = FFT(fftbufnum, signal);
			chain = PV_MagSmear(chain, 1);
			phasor = halfSamples.pow(LFSaw.ar(rate/BufDur.kr(fftbufnum), phase, 0.5, 0.5)) * 2; // 2 to bufsize
			phasor = phasor.round(2); // the evens are magnitude
			ScopeOut.ar( ((BufRd.ar(1, fftbufnum, phasor, 1) * mul).ampdb * dbFactor) + 1, scopebufnum);
		}).send(server);
		
		"XiiSCFreqScope: SynthDefs sent".postln;
	}
	
	allocBuffers {
		
		scopebuf = Buffer.alloc(server, 2048, 1, 
			{ arg sbuf;
				scope.bufnum = sbuf.bufnum;
				fftbuf = Buffer.alloc(server, 2048, 1,
				{ arg fbuf;
					("XiiSCFreqScope: Buffers allocated (" 
						++ sbuf.bufnum.asString ++ ", "
						++ fbuf.bufnum.asString ++ ")").postln;
				});
			});
	}
	
	freeBuffers {
		if( scopebuf.notNil && fftbuf.notNil, {
			("XiiSCFreqScope: Buffers freed (" 
				++ scopebuf.bufnum.asString ++ ", "
				++ fftbuf.bufnum.asString ++ ")").postln;
			scopebuf.free; scopebuf = nil;
			fftbuf.free; fftbuf = nil;
		});
	}
	
	start {

		// sending bundle messes up phase of LFSaw in SynthDef (????)
//		server.sendBundle(server.latency, 
//			["/s_new", "freqScope", node, 1, 0, 
//				\in, inBus, \mode, mode, 
//				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum]);
		node = server.nextNodeID; // get new node just to be safe
		server.sendMsg("/s_new", "XiiFreqScope" ++ freqMode.asString, node, 1, 0, 
				\in, inBus, \dbFactor, dbFactor,
				\fftbufnum, fftbuf.bufnum, \scopebufnum, scopebuf.bufnum);
	}
	
	kill {
		this.eventSeq(0.5, {this.active_(false)}, {this.freeBuffers});
	}
	
	// used for sending in order commands to server
	eventSeq { arg delta ... funcs;
		Routine.run({
			(funcs.size-1).do({ arg i;
				funcs[i].value;
				delta.wait;
			});
			funcs.last.value;
			
		}, 64, AppClock);
	}
	
	active_ { arg bool;
		server.serverRunning.postln;
		if(server.serverRunning, { // don't do anything unless server is running
		
		if(bool, {
			if(active.not, {
				CmdPeriod.add(this);
				if((scopebuf.isNil) || (fftbuf.isNil), { // first activation
					this.eventSeq(0.5, {this.sendSynthDefs}, {this.allocBuffers}, {this.start; "starting".postln;});
				}, {
					this.start; "Starting freqscope".postln;
				});
			});
		}, {
			if(active, {
				server.sendBundle(server.latency, ["/n_free", node]);
				CmdPeriod.remove(this);
			});
		});
		active=bool;
		
		});
		^this
	}
	
	inBus_ { arg num;
		inBus = num;
		if(active, {
			server.sendBundle(server.latency, ["/n_set", node, \in, inBus]);
		});
		^this
	}
	
	dbRange_ { arg db;
		dbRange = db;
		dbFactor = 2/db;
		if(active, {
			server.sendBundle(server.latency, ["/n_set", node, \dbFactor, dbFactor]);
		});		
	}
	
	freqMode_ { arg mode;
		freqMode = mode.asInteger.clip(0,1);
		if(active, {
			server.sendMsg("/n_free", node);
			node = server.nextNodeID;
			this.start;
		});		
	}
	
	cmdPeriod {
		scope.changed(\cmdPeriod); // XXX was this
		if(active == true, {
			CmdPeriod.remove(this);
			active = false;
			node = server.nextNodeID;
			// needs to be deferred to build up synth again properly
			{ this.active_(true) }.defer( 0.5 );
		});
	}	
}