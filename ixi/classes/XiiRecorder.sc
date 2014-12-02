
XiiRecorder {	
	var <>xiigui;
	var <>win, params;
	
	var numChannels;
	var recButton; // allowing for outside start of record (Function:record)
	var inbus, recBussesPop, ampAnalyserSynth;
	var openInEditor; // makes Function:record result in sound opening in an editor
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiRecorder(server, channels, setting);
		}
		
	initXiiRecorder {arg server, channels, setting;
		var bgColor, foreColor, spec, outbus;
		var s, name, point;
		var txtv, r, filename, timeText, secTask;
		var stereoButt, monoButt, cmdPeriodFunc;
		var vuview, ampslider, responder;
		var amp, ampAnalFunc;
		
		numChannels = channels;
		filename = "";
		name = "- recorder -";
		s = server ? Server.default;

		xiigui = nil; // not using window server class here
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		params = if(setting.isNil, {[numChannels-1,(numChannels-2).abs,0,1]}, {setting[2]});
		[\params, params].postln;
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		inbus = params[2];
		outbus = 0;
		amp = 1.0;
		responder = OSCresponderNode(s.addr,'/tr',{ arg time, responder, msg;
			if (msg[1] == ampAnalyserSynth.nodeID, {
				{ 
					win.isClosed.not.if({ 
						vuview.value = \amp.asSpec.unmap(msg[3]);
					});
				}.defer;
			});
		}).add;
		
		ampAnalFunc = { // this is called on CmdPeriod so the vuview will work
			ampAnalyserSynth = Synth(\xiiVuMeter, 	
					[\inbus, inbus, \amp, amp], addAction:\addToTail);
		};
		ampAnalFunc.value;
		
		win = GUI.window.new(name, Rect(point.x, point.y, 222, 100), resizable:false);
					
		stereoButt = OSCIIRadioButton(win, Rect(10,5,14,14), "stereo")
						.value_(params[0])
						.font_(GUI.font.new("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
									numChannels = 2;
									params[1] = 0;
									recBussesPop.items_(XiiACDropDownChannels.getStereoChnList);
									recBussesPop.value_(inbus/2);
								}, {
									params[0] = 1;
									numChannels = 1;
									recBussesPop.items_(XiiACDropDownChannels.getMonoChnList);
									recBussesPop.value_(inbus);
								});
								monoButt.switchState;
								params[0] = butt.value;
						});

		monoButt = OSCIIRadioButton(win, Rect(100,5,14,14), "mono ")
						.value_(params[1])
						.font_(GUI.font.new("Helvetica", 9))
						.action_({ arg butt;
								if(butt.value == 1, {
									numChannels = 1;
									params[0] = 0;
									recBussesPop.items_(XiiACDropDownChannels.getMonoChnList);
									recBussesPop.value_(inbus);
								},{
									params[0] = 1;
									numChannels = 2;
									recBussesPop.items_(XiiACDropDownChannels.getStereoChnList);
									recBussesPop.value_(inbus/2);
								});
								stereoButt.switchState;
								params[1] = butt.value;
						});

		txtv = GUI.textView.new(win, Rect(10, 25, 140, 16))
				.hasVerticalScroller_(false)
				.autohidesScrollers_(true)
				.string_(filename);

		recButton = GUI.button.new(win, Rect(104, 50, 46, 16))
			.states_([["Record",Color.black, Color.clear], 
					["Stop",Color.red,Color.red(alpha:0.2)]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt;
				if(s.serverRunning == true, { // if the server is running
					if(butt.value == 1, {
						filename = txtv.string.asString;
						if(filename == "", {
							if(thisProcess.platform.name==\windows, { // Date not working on wz
								filename = "sound" ++ Main.elapsedTime.round; // temp solution
							}, { 
								filename = "rec_" ++ Date.localtime.stamp;
							});
						});
						txtv.string_(filename);
						if ( GUI.id == \swing, { 
							r = XiiRecord(server, inbus, numChannels, 'wav', XQ.pref.bitDepth);
							r.start("sounds/ixiquarks/"++filename++".wav");
						 }, {
							r = XiiRecord(server, inbus, numChannels, 'aiff', XQ.pref.bitDepth);
							r.start("sounds/ixiquarks/"++filename++".aif");
						 });
						r.setAmp_(amp);
						secTask.start;
					}, {
						r.stop;
						secTask.stop;
						vuview.value = 0;
						if(openInEditor.booleanValue, {
							"----------- opening in editor".postln;
							("open -a 'Audacity.app'" 
								+ "sounds/ixiquarks/"++filename++".aif").unixCmd;
						});
						txtv.string_(filename = PathName(filename).nextName);
					});
				}, {
					XiiAlert("you need to start a server in order to record");
					recButton.value_(0);
				});
			});
		
		timeText = GUI.staticText.new(win, Rect(64, 50, 40, 16))
					.string_("00:00");

		// record busses
		recBussesPop = GUI.popUpMenu.new(win, Rect(10, 50, 44, 16))
			.items_(	if(numChannels == 2, {
						XiiACDropDownChannels.getStereoChnList;
					},{
						XiiACDropDownChannels.getMonoChnList;
					});
			)
			.value_(params[2])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				inbus = if(numChannels == 2, {ch.value * 2}, {ch.value});
				ampAnalyserSynth.set(\inbus, inbus);
				params[2] = ch.value;
			});
			
		// the vuuuuu meter
		vuview = XiiVuView(win, Rect(162, 5, 46, 37))
				.canFocus_(false);
				//.relativeOrigin_(false);

		ampslider = OSCIISlider.new(win, Rect(162, 50, 46, 10), "vol", 0, 1, params[3], 0.001, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl;
				amp = sl.value;
				ampAnalyserSynth.set(\amp, amp);
				if(recButton.value == 1, { r.setAmp_(amp) });
				params[3] = sl.value;
			});
			
		SCStaticText(win, Rect(10, 70, 196, 36))
				.font_(GUI.font.new("Helvetica", 9))
				.string_("NOTE: use 8-9 to record mic input");
			
		// updating the seconds text		
		secTask = Task({var sec, min, secstring, minstring;
			sec = 0;
			min = 0;
			inf.do({arg i; 
				sec = sec + 1;
				if(sec > 59, {min = min+1; sec = 0;});
				if(min < 10, {minstring = "0"++min.asString}, {minstring = min.asString});
				if(sec < 10, {secstring = "0"++sec.asString}, {secstring = sec.asString});
				{timeText.string_(minstring++":"++secstring)}.defer;
				1.wait;
			});
		});
		
		cmdPeriodFunc = { recButton.valueAction_(0); {ampAnalFunc.value}.defer(0.1)};
		CmdPeriod.add(cmdPeriodFunc);

		win.front;
		win.onClose_({
			var t;
			recButton.valueAction_(0); // stop recording
			CmdPeriod.remove(cmdPeriodFunc);
			ampAnalyserSynth.free;
			responder.remove;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[numChannels, point, params];
	}
	
	record {arg time, bus, openInEd;
		openInEditor = openInEd;
		recButton.valueAction_(1);
		if( time.isNil.not, { AppClock.sched(time, { recButton.valueAction_(0); nil}) });
		if( bus.isNil.not, { 
			inbus = bus; 
			recBussesPop.value_(inbus/numChannels);
			ampAnalyserSynth.set(\inbus, inbus);
		 });
	}
}

