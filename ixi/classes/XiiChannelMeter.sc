XiiChannelMeter {

	*new { arg server, channels, setting=nil;
		^super.new.initMeters(server, channels, setting);
	}
	
	// thanks Scotty!
	initMeters {arg server, channels, setting; 
	
		var win, inmeters, outmeters, inresp, outresps, insynth, outsynths, func;
		var numIns, numOuts;
		var view, viewWidth, meterWidth = 15, gapWidth = 4;
		var updateFreq = 10, dBLow = -80;
		var numRMSSamps, numRMSSampsRecip;
		var name, point;
		
		name = "- level meter -";
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});

		numIns = 2;
		numOuts = XQ.pref.numberOfChannels;
		viewWidth = (numIns + numOuts + 2) * (meterWidth + gapWidth);
		win = Window.new(" - level meter - ", Rect(point.x, point.y, viewWidth + 20, 230));
		//window.view.background = Color.grey(0.4);
		
		view = CompositeView(win, Rect(10,25, viewWidth, 180) );
		view.addFlowLayout(0@0, gapWidth@gapWidth);
		
		// dB scale
		UserView(view, Rect(0,0,meterWidth,195)).drawFunc_({
			"0".drawCenteredIn(Rect(0, 0, meterWidth, 12), Font("Helvetica-Bold", 10), Color.white);
			"-80".drawCenteredIn(Rect(0, 170, meterWidth, 12), Font("Helvetica-Bold", 10), Color.white);
		});
		
		// ins
		StaticText(win, Rect(10, 5, 100, 15))
			.font_(Font("Helvetica-Bold", 10))
			.stringColor_(Color.white)
			.string_("Inputs");
		inmeters = Array.fill( numIns, { arg i;
			var comp, level;
			comp = CompositeView(view, Rect(0,0,meterWidth,195)).resize_(5);
			StaticText(comp, Rect(0, 180, meterWidth, 15))
				.font_(Font("Helvetica-Bold", 9))
				.stringColor_(Color.white)
				.string_(i.asString);
			if (GUI.id == \cocoa, 
				{ level = SCLevelIndicator( comp, Rect(0,0,meterWidth,180) ).warning_(0.9).critical_(1.0) },
				{ level = LevelIndicator( win, Rect(0,0,meterWidth,180) ).warning_(0.9).critical_(1.0) }
			);
				level.drawsPeak_(true)
				.numTicks_(9)
				.style_(2)
				.numSteps_(20)
				.numMajorTicks_(3);
		});
		
		// divider
		UserView(view, Rect(0,0,meterWidth,180)).drawFunc_({
			Pen.color = Color.white;
			Pen.line(((meterWidth + gapWidth) * 0.5)@0, ((meterWidth + gapWidth) * 0.5)@180);	
			Pen.stroke;
		});
		
		// outs
		StaticText(win, Rect(10 + ((numIns + 2) * (meterWidth + gapWidth)), 5, 100, 15))
			.font_(Font("Helvetica-Bold", 10))
			.stringColor_(Color.white)
			.string_("Outputs");
		outmeters = Array.fill( numOuts, { arg i;
			var comp;
			comp = CompositeView(view, Rect(0,0,meterWidth,195));
			StaticText(comp, Rect(0, 180, meterWidth, 15))
				.font_(Font("Helvetica-Bold", 9))
				.stringColor_(Color.white)
				.string_(i.asString);
			SCLevelIndicator( comp, Rect(0,0,meterWidth,180) ).warning_(0.9).critical_(1.0)
				.drawsPeak_(true)
				.numTicks_(9)
				.numMajorTicks_(3);
		});

		win.front;
		
		func = {
			numRMSSamps = Server.default.sampleRate / updateFreq;
			numRMSSampsRecip = 1 / numRMSSamps;
			inresp = OSCresponder(nil, "/ixiInLevels", { |t, r, msg| 			{try {
				msg.copyToEnd(3).pairsDo({|val, peak, i| 
					var meter;
					i = i * 0.5;
					meter = inmeters[i];
					meter.value = (val * numRMSSampsRecip).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
					meter.peakLevel = peak.ampdb.linlin(dBLow, 0, 0, 1);
				}) }}.defer; 
			}).add;
			outresps = Array.fill(2, {arg i; OSCresponder(nil, "/ixiOutLevels"++i.asString, { |t, r, msg| 			{try {
				msg.copyToEnd(3).pairsDo({|val, peak, it | 
					var meter;
					it = it * 0.5;
					meter = outmeters[it+(i*26)];
					meter.value = (val * numRMSSampsRecip).sqrt.ampdb.linlin(dBLow, 0, 0, 1);
					meter.peakLevel = peak.ampdb.linlin(dBLow, 0, 0, 1);
				}) }}.defer; 
			}).add;
			});
			
			Server.default.bind({
			insynth = SynthDef("ixiInputLevels", {
				var in, imp;
				in = InFeedback.ar(NumOutputBuses.ir, numIns);
				imp = Impulse.ar(updateFreq);
				SendReply.ar(imp, "/ixiInLevels", 
					[RunningSum.ar(in.squared, numRMSSamps), Peak.ar(in, Delay1.ar(imp)).lag(0, 3)].flop.flat
				);
			}).play(RootNode(Server.default), nil, \addToHead);
			
			outsynths = Array.fill(2, {arg i; SynthDef("ixiOutputLevels"++i.asString, {
				var in, imp;
				in = InFeedback.ar(i*26, 26);
				imp = Impulse.ar(updateFreq);
				SendReply.ar(imp, "/ixiOutLevels"++i.asString, 
					[RunningSum.ar(in.squared, numRMSSamps), Peak.ar(in, Delay1.ar(imp)).lag(0, 3)].flop.flat
				);
			}).play(RootNode(Server.default), nil, \addToTail);
			});
			
			});
		};
		
		win.onClose_({
			var t;
			inresp.remove;
			outresps.do({arg resp; resp.remove;});
			insynth.free;
			outsynths.do({arg synth; synth.free}); 
			ServerTree.remove(func);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};

			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
		
		ServerTree.add(func);
		if(Server.default.inProcess, func); // otherwise starts when booted
	}

}
