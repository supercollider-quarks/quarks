

BusMeters {

	classvar serverMeterViews, 	updateFreq = 10, dBLow = -86.0;

	var <server,<busses;
	var responder, synth, <meters, <peaks,<peaksG;
	var numRMSSamps, numRMSSampsRecip;

	*new { |server,busses|
		^super.new.init(server,busses)
	}

	init { arg aserver, abusses;
		server = aserver ?? {Server.default};
		busses = abusses ?? {[Bus(\audio,0,2,server)]};
		meters = Array.newClear(busses.size);
		peaks = Array.fill(busses.size,{dBLow});
		peaksG = Array.newClear(busses.size);
		numRMSSamps = (server.sampleRate ? server.options.sampleRate ? 44100.0) / updateFreq;
		numRMSSampsRecip = 1 / numRMSSamps;
	}
	gui { arg parent,bounds;
		var meterWidth,meterHeight;
		if(bounds.isNil,{
			meterWidth = 30;
			meterHeight = 130;
		},{
			meterWidth = bounds.width.asFloat / busses.size;
			meterHeight = bounds.height;
		});		
		parent = parent ?? {FlowView(nil,bounds ?? {Rect(0,0,busses.size * (meterWidth + 4),meterHeight + 20)})};
		busses.do { arg b,i;
			parent.comp({ arg v;
				this.makePeak(i,v,Rect(0,0,meterWidth,GUI.skin.buttonHeight));
				this.makeBusMeter(i,v,Rect(0,GUI.skin.buttonHeight,meterWidth,meterHeight))
			},meterWidth@(meterHeight + GUI.skin.buttonHeight));
		};
		
		parent.onClose = { this.stop };
	}
	makeBusMeter { arg bi,layout,bounds;
		var bv,pk;
		bounds = bounds ?? {Rect(0,0,30,180)};
		bv = LevelIndicator( layout, bounds )
					.warning_(-0.2.dbamp)
					.critical_(-0.01.dbamp)
					.style_(0)
					.drawsPeak_(true)
					.numTicks_(9)
					.numMajorTicks_(4);
		meters.put(bi,bv);
		^bv
	}
	makePeak { arg bi,layout,bounds;
		var pk;
		bounds = bounds ?? {Rect(0,0,30,GUI.skin.buttonHeight)};
		pk = ActionButton(layout,dBLow.round(0.1).asString,{
			peaks.put(bi,dBLow);
			pk.label_(dBLow.round(0.1).asString).refresh;
		},bounds.width,bounds.height);
		peaks.put(bi,dBLow);
		peaksG.put(bi,pk);
		^pk
	}		

	start {
		if(synth.isPlaying.not,{
			server.waitForBoot({this.prStart});
		})
	}
	prStart {
		var name;
		name = "BusMeters".catList(busses.collect({ arg bus; bus.index }));
		synth = SynthDef(name, {
			var ins, imp,reset;
			ins = busses.collect({ arg b; In.ar(b.index,b.numChannels) });
			imp = Impulse.ar(updateFreq);
			reset = Delay1.ar(imp);
			SendReply.ar(imp, "/" ++ name,
				ins.collect({ arg in,i; 
					[
						RunningSum.ar(in.squared, numRMSSamps),
						Peak.ar(in, reset).lag(0, 2)
					]
				}).flat
			);
		}).play(RootNode(server), nil, \addToTail);

		responder = OSCresponderNode(server.addr, "/" ++ name, { |t, r, msg|
			{
				try {
					if(meters[0].isClosed.not,{
						meters.do { arg m,i;
							var val,peak;
							val = msg[ [3,4] + (i*4) ];
							peak = msg[ [5,6] + (i*4) ];
							m.value = (val.maxItem.max(0.0) * numRMSSampsRecip).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
							peak = peak.maxItem.ampdb;
							m.peakLevel = peak.linlin(dBLow, 0, 0, 1);
							peaks.put(i, max(peaks.at(i),peak) );
							if(peaksG.at(i).notNil,{
								peaksG.at(i).label_(peaks.at(i).round(0.1).asString).refresh;
							})
						}
					})
				}
			}.defer;
		}).add;
	}	
		
	stop {
		if(synth.isPlaying,{
			synth.free
		});
		responder.remove;
	}
	remove { this.stop }
}

