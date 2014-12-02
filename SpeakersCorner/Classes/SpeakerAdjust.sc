SpeakerAdjust { 

	classvar <>freqName = \eqFr, <>gainName= \eqGn, <>bwName= \eqBw;
	var <server, <specs, <vol, <synth, <group; 
		
	*addSpecs { |numChans = 8, numBands = 3, dict|
		var 	freqSpec, gainSpec, bwSpec, threeSpecs; 
		
		freqSpec = \freq.asSpec;
		gainSpec = [-24, 24].asSpec; 
		bwSpec = [0.01, 2, \exp].asSpec;
		threeSpecs = [freqSpec, gainSpec, bwSpec];
		
		dict = dict ? Spec.specs;
		dict.put(freqName, freqSpec);
		dict.put(gainName, gainSpec);
		dict.put(bwName, bwSpec);
		
		numChans.do { |i| 
			numBands.do { |j| 
				this.makeNames(i, j).do { |name, i| 
					dict.put(name, threeSpecs[i]);
				}
			}
		};
	}
	
	*makeNames { |i, j| 
		var ext = "_c" ++ (j+1) ++ "_" ++ (i+1);
		^[freqName, gainName, bwName].collect { |nm| (nm ++ ext).asSymbol };
	}
		// more efficient, but fixed values for everything
	*ar { |ins, specs, vol=1|

		vol = vol.lag(0.2); 
		
		if (ins.size != specs.size) { 
			"SpeakerAdjust: number of ins: % and specs: % dont match."
				.format(ins.size, specs.size).warn;
			^nil
		};
		
		^specs.collect { |spec, i|
			var out, amp, dt, eqSpecs; 

			out = ins[i];
			#amp, dt ... eqSpecs = spec; 

			out = if (amp.notNil, { out * amp }, { out });

			if (dt ? 0 > 0, { out = DelayN.ar(out, dt, dt) });
		
			eqSpecs.do { |spec|
				var freq, db, rq; 
				#freq, db, rq = spec;
				out = MidEQ.ar(out, freq, rq, db);
			};
			out * vol;
		};	
	}
		// create controls for everything
	*arDyn { |ins, specs, vol=1|
		
		vol = vol.lag(0.2); 
		
		if (ins.size != specs.size) { 
			"SpeakerAdjust: number of ins: % and specs: % dont match."
				.format(ins.size, specs.size).warn;
			^nil
		};
		
		^specs.collect { |specList, i|
			var out, amp, dt, eqSpecs; 
			var ampCtl, delayCtl;
			
			out = ins[i];
			#amp, dt ... eqSpecs = specList; 

			delayCtl = NamedControl.kr(("eqDt_c" ++ (i+1)).asSymbol, dt ? 0, 0.2);
			out = DelayN.ar(out, 0.1, delayCtl);

			ampCtl = NamedControl.kr(("eqAmp_c" ++ (i+1)).asSymbol, amp ? 1, 0.2);
			out * (ampCtl * vol);
		
			eqSpecs.do { |specBand, j|
				var freq, db, rq; 
				var freqCtl, gainCtl, rqCtl; 
				var ctlNames = this.makeNames(i, j); 
				#freq, db, rq = specBand.postcs;
				
				freqCtl = NamedControl.kr(ctlNames[0], freq, 0.2);
				gainCtl = NamedControl.kr(ctlNames[1], db, 0.2);
				rqCtl   = NamedControl.kr(ctlNames[2], rq, 0.2);
				
				out = MidEQ.ar(out, freqCtl, gainCtl, rqCtl);
			};
			out
		};	
	}
}