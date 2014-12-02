XiiTrigRecorder {	

	var <>xiigui;
	var <>win, params;
	
	var channels;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiTrigRecorder(server, channels, setting);
		}
		
	initXiiTrigRecorder {arg server, ch, setting;
		
		var bang, endFunc, sensi;
		var analyser, recording;
		var endRecTimer, rectime;
		var prerec, postrec;
		var sensibang, recbang, recButt;
		var recsynth, inBusPop, outputFile;
		var recbuttOn;
		var group, filepath, buffer;
		var osc, countDownTask;
		var point, name, inbus, sensitivity, prerectime;
		
		channels = if(setting.isNil, {ch}, {setting[0]});
		name = "- trigrecorder -";

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[8, 0.4, 1, 5]}, {setting[2]});
		
		inbus = if(setting.isNil, { 8 }, {params[0]});
		recording = false;
		endRecTimer = 0 ;
		sensitivity = params[1];
		prerectime = params[2];
		rectime = params[3];

		group = Group.new;
		filepath = "sounds/ixiquarks/TrigRecorder.aif";
		
		buffer = if(channels == 2, { 
					Buffer.alloc(server, 65536, 2);
				},{
					Buffer.alloc(server, 65536, 1);		
				});
		
		win = GUI.window.new("TrigRecorder", Rect(point.x, point.y, 242, 106), resizable:false);

		sensibang = Bang.new(win, Rect(10, 10, 50, 50))
				.setBackground_(Color.white);
		
		recbang = Bang.new(win, Rect(70, 10, 50, 50))
				.setBackground_(Color.white)
				.setFillColor_(Color.red(alpha:0.5));
				
		recButt = GUI.button.new(win, Rect(79, 70, 42, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.states_([["activate",Color.black,Color.clear], 
						["stop",Color.black,Color.green(alpha:0.2)]])
				.action_({arg butt;
					if(butt.value == 1, {
						recsynth = if(channels==2, {
										Synth(\xiiTrigRecorderRec2x2, 
												["bufnum", buffer.bufnum], 
												target:group, 
												addAction:\addToTail);
									}, {
										Synth(\xiiTrigRecorderRec1x1, 
												["bufnum", buffer.bufnum], 
												target:group, 
												addAction:\addToTail);
									});
						recsynth.run(false);
						recbuttOn = true;
						buffer.write(filepath, "aiff", XQ.pref.bitDepth, 0, 0, true);
					}, {
						recsynth.free;
						recbang.setState_(false);
						sensibang.setState_(false);
						recbuttOn = false;
						buffer.close;// close the recording file
					});
				 });
				 
		outputFile = GUI.button.new(win, Rect(49, 70, 26, 16))
			.font_(GUI.font.new("Helvetica", 9))
				.states_([["file",Color.black,Color.clear]])
				.action_({arg butt;
					CocoaDialog.savePanel({ arg path;
						filepath = path;
						if(filepath.contains(".aif").not, {filepath = filepath++".aif"});
					},{
						filepath = "sounds/ixiquarks/TrigRecorder.aif";
					});
				 });
		
		inBusPop = GUI.popUpMenu.new(win, Rect(10, 70, 36, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(
					if(channels==2, { 
						XiiACDropDownChannels.getStereoChnList
					}, {		
						XiiACDropDownChannels.getMonoChnList
					})
				)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; var inbus;
					inbus = if(channels==2, {popup.value*2}, {popup.value});
					analyser.set(\inbus, inbus);
					params[0] = inbus;
				})
				.value_( inbus/channels );
		
		sensi = OSCIISlider.new(win, Rect(130, 10, 100, 10), "- sensitivity", 0.01, 1, params[1], 0.01, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				analyser.set(\sensitivity, sl.value);
				sensitivity = sl.value;
				params[1] = sl.value;
			});
			
		prerec = OSCIISlider.new(win, Rect(130, 40, 100, 10), "- prerec", 0, 2.0, params[2], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				prerectime = sl.value;
				analyser.set(\prerectime, sl.value);
				params[2] = sl.value;
			});
			
		postrec = OSCIISlider.new(win, Rect(130, 70, 100, 10), "- postrec", 1, 10, params[3], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				rectime = sl.value;
				params[3] = rectime;
			});
		
		analyser = if(channels == 2, {
					Synth(\xiiTrigRecAnalyser2x2, [	\inbus, inbus,
												\sensitivity, sensitivity, 
												\prerectime, prerectime], 
												target: group, addAction:\addToHead);
				}, {
					Synth(\xiiTrigRecAnalyser1x1, [  \inbus, inbus,
												\sensitivity, sensitivity, 
												\prerectime, prerectime], 
												target: group, addAction:\addToHead);
				});
		
		// task that counts down and stops synth if no sound anymore
		countDownTask = Task({
			inf.do({
				endRecTimer = endRecTimer - 1;
				if( endRecTimer < 1, { // we end the recording
					{recbang.setState_(false)}.defer;
					recsynth.run(false);
					recording = false;
					countDownTask.stop;
					endRecTimer = 0;
				});
				1.wait;
			});
		});
		
		// responder to trigger synth
		osc = OSCresponderNode(server.addr, '/tr', { arg time, responder, msg;
			if(msg[2]==666, {
				{sensibang.setState_(true)}.defer;
				AppClock.sched(0.25, {
					win.isClosed.not.if({
						sensibang.setState_(false)});
					});
				if(recbuttOn==true, { // if we are recording
					if( endRecTimer == 0, { 
							{recbang.setState_(true)}.defer;
							recsynth.run(true);
							endRecTimer = rectime; // 10 sec max recording (if only an impulse)
							countDownTask.start;
							recording = true;
					}, { 
						{recbang.setState_(true)}.defer; 
						endRecTimer = rectime; // 5 sec max recording (if only an impulse)
					});
				});
			});
		}).add;
		
		endFunc = { 	
					recButt.valueAction_(0);
					recsynth.free; 
					analyser.free; 
					buffer.close; 
					buffer.free; 
					osc.remove; 
					countDownTask.stop
				};
				
		win.onClose_({ 
			var t;
			endFunc.value;
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		 });	 
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[channels, point, params];
	}
}