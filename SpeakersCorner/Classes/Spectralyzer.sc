Spectralyzer { 
	var <nBands, <minFreq, <maxFreq, <server, <rq, <freqs; 
	var <inPx, <ampsPx, task, lastNDbs, <avgN = 5; 
	
	var <view, <ampsV, <dbVals, <midSl, <rangeSl, <avgSl, <dtSl;
	var <midDb = -30, <rangeDb = 15, <dt=0.1;
	
	*new { |nBands=61, minFreq=20, maxFreq=20000, server| 
		server = server ? Server.default;
		^super.newCopyArgs(nBands, minFreq, maxFreq, server).init;
	}
	
	init { 
		freqs = (maxFreq / minFreq) ** ((0..nBands - 1) / (nBands - 1)) * minFreq;
		rq = freqs[1] / freqs[0] - 1; 
		inPx = NodeProxy(server, \audio, 1);
		ampsPx = NodeProxy(server, \control, nBands + 1);
		ampsPx.source = this.ampsFunc; 
		task = Task { 
			loop { max(0.1, dt).wait; 
				this.poll({ |mAmps| { this.setAmps(mAmps) }.defer; });
			};
		};
	}
	
	midDb_ { |val| midDb = val.clip(-100, 0); midSl.value_(midDb); }
	rangeDb_ { |val| rangeDb = val.clip(0, 30); rangeSl.value_(rangeDb); }
	avgN_ { |val| avgN = val.clip(1, 50); avgSl.value_(avgN); }
	dt_ { |val| dt = val.clip(0.01, 1); dtSl.value_(dt); }
	
	ampsFunc { 
		^{ Amplitude.kr([inPx.ar(1)] ++ BPF.ar(inPx.ar(1), freqs, rq), 0.0, 1) }
	}
	listenTo { |src| inPx.source = src; }
	
	poll { |func| ^ampsPx.bus.getn(nBands, func) }

	start { 
		inPx.wakeUp; 
		ampsPx.wakeUp; 
		task.stop.play; 
	}
	stop { 
		inPx.end; 
		ampsPx.end; 
		task.stop; 
	}
	setAmps { |amps|
		var viewVals; 
		dbVals = amps.drop(1).ampdb; 
		lastNDbs = lastNDbs.add(dbVals).keep(avgN.neg.asInteger); 
		
		viewVals = (lastNDbs.mean - midDb) / (rangeDb * 2) + 0.5; 
		this.setViewVals(viewVals);
	}

	setViewVals { |vals| if (view.isClosed.not) { ampsV.value_(vals) } }

	makeWindow { arg parent, labelsPerLine = 31; 
		
		var win, flo, width = 1024, ezWidth;
		var labelWid = 32, maxNumLabels = 31, numLabels, numLines; 
		var comp, font = Font("Monaco", 9); 
		var multiHeight = 200;
		
		view = parent ?? { 
			win = Window(this.class.name, Rect(0,0, width + 10, multiHeight + 60))
				.front;
			view = win.view; 
		};
		
		flo = view.addFlowLayout(1@1, 2@2);
		
		Button(view, Rect(0,0, 60,20)).states_([["start"]])
			.action_({ this.start });
			
		Button(view, Rect(0,0, 60,20)).states_([["stop"]])
			.action_({ this.stop });
		
		ezWidth = width - (60 + 2 * 2) / 4 - 2; 
		
		#midSl, rangeSl, avgSl, dtSl = [	
			[\midDb, [-80,0,\lin,1,-30].asSpec],
			[\rangeDb, [2,40,\lin,1,15].asSpec],
			[\avgN, [1, 50, \lin,1, 5].asSpec],
			[\dt, [0.01, 1, \exp, 0.01, 0.1].asSpec]
		].collect { |list| var name, spec; 
			#name, spec = list;
			EZSlider(view, ezWidth@20, name, spec, 
				{|sl| this.perform((name ++ "_").asSymbol, sl.value); }, 
				labelWidth: 50, numberWidth: 25
			);
		};
		
		flo.nextLine;
		
			// make labels: 
		
		comp = CompositeView(view, Rect(0,0, width, multiHeight + 50)); 
		
		numLines = (freqs.size / maxNumLabels).ceil.asInteger;
		numLabels = (freqs.size / numLines).ceil.asInteger; 
		labelWid = (width / numLabels).trunc;
		
		numLabels.do { |i| 
			var left = i * labelWid; 
			var top = 0; // stacked ... i % numLines * 20;
			
			var freq = freqs[i * numLines].round(0.1);
			if (freq >= 100, { freq = freq.round(1) }); 
			
			StaticText(comp, Rect(left, top, labelWid - 1, 16))
				.background_(Color(0.9,0.9,0.9))
				.string_(freq).align_(0)
				.font_(font);
			StaticText(comp, Rect(left + (labelWid / (numLines + 1)), top + 16, 1, 4))
				.background_(Color.black)
		};
		
		flo.nextLine;
		flo.dump; 
		
		ampsV = MultiSliderView(comp, Rect(0, 20, width + 2, multiHeight))
			.valueThumbSize_(2.0).gap_(2.0)
			.indexThumbSize_(width / freqs.size - 2.0)
			.value_(0.5 ! freqs.size);
		
		3.do { |i|
			StaticText(comp, Rect(0, i + 1 * (multiHeight / 4) + 20, width, 1))
				.background_(Color.white.alpha_([0.3, 0.6, 0.3][i]));
		};
	}
}