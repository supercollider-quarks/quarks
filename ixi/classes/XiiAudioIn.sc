// for all instruments:

// <>xiigui, <>win
// setting = nil
// params
// getState method
// try{XQ.globalWidgetList.removeAt(t)};


XiiAudioIn {	

	var <>xiigui;
	var <>win, params;
	
	*new { arg server, channels, setting = nil;
		^super.new.initAudioIn(server, channels, setting);
		}
		
	initAudioIn {arg server, channels, setting;
		var responder, inmeterl, leftVol, inmeterr, rightVol, panLslider, panRslider;
		var bgColor, foreColor, spec, outbus;
		var audioInSynth, s, name, point;
		var onOffButt, cmdPeriodFunc;
		
		name = "- audio in -";
		s = server ? Server.local;
		
		// set up presets (storing window location and parameters)
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,0,0,0]}, {setting[2]});
		
		bgColor = XiiColors.lightgreen;
		foreColor = XiiColors.darkgreen;
		outbus = params[4];
		
		win = GUI.window.new(name, Rect(point.x, point.y, 222, 106), resizable:false);
				
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider
		
		inmeterl = GUI.rangeSlider.new(win, Rect(10, 10, 20, 80));
		inmeterl.background_(bgColor).knobColor_(HiliteGradient(XiiColors.darkgreen, Color.white, \h));
		inmeterl.lo_(0).hi_(0.05);
		inmeterl.canFocus_(false);
		
		leftVol = GUI.slider.new(win, Rect(40, 10, 10, 80))
					.canFocus_(false)
					.background_(bgColor).knobColor_(foreColor)
					.value_(params[0])
					.action_({ arg sl; 
						try{ audioInSynth.set(\volL, spec.map(sl.value)) };
						params[0] = sl.value;
					});
		
		inmeterr = GUI.rangeSlider.new(win, Rect(60, 10, 20, 80));
		inmeterr.background_(bgColor).knobColor_(HiliteGradient(XiiColors.darkgreen, Color.white, \h));
		inmeterr.lo_(0).hi_(0.05);
		inmeterr.canFocus_(false);
		
		rightVol = GUI.slider.new(win, Rect(90, 10, 10, 80))
					.canFocus_(false)
					.background_(bgColor).knobColor_(foreColor)
					.value_(params[1])
					.action_({ arg sl; 
						try{ audioInSynth.set(\volR, spec.map(sl.value))};
						params[1] = sl.value;
					});
		
		responder = OSCresponderNode(s.addr, '/tr', { arg t, r, msg;
			{
			win.isClosed.not.if({ 
				if(msg[2] == 800, { inmeterl.hi_(1-(msg[3].ampdb.abs * 0.01)) });
				if(msg[2] == 801, { inmeterr.hi_(1-(msg[3].ampdb.abs * 0.01)) });
			});
			}.defer;
		}).add;
			
		panLslider = OSCIISlider.new(win, Rect(110, 10, 100, 10), "- L pan", -1, 1, -1, 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(params[2])
			.action_({arg sl; audioInSynth.set(\panL, sl.value); params[2] = sl.value;});
		panRslider = OSCIISlider.new(win, Rect(110, 40, 100, 10), "- R pan", -1, 1, 1, 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.value_(params[3])		
			.action_({arg sl; audioInSynth.set(\panR, sl.value); params[3] = sl.value;});
		
		GUI.popUpMenu.new(win,Rect(110, 75, 50, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(outbus/2) // because of stereo
			.background_(Color.white)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				outbus = ch.value * 2;
				audioInSynth.set(\out, outbus );
				params[4] = outbus;
			});
			
		onOffButt = GUI.button.new(win,Rect(170, 75, 36, 16))
			.states_([
					["on",Color.black, Color.clear],
					["off",Color.black,bgColor]
				])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt;
				if(butt.value == 1, {
					audioInSynth = Synth(\xiiAudioIn, [\out, outbus, 
							\volL, leftVol.value, \volR, rightVol.value, 
							\panL, panLslider.value, \panR, panRslider.value]);
				},{
					audioInSynth.free;
					
					{inmeterl.hi_(0);
					inmeterr.hi_(0);}.defer(0.2);
				});
			});
		
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);
			
		win.front;
		win.onClose_({
			var t;
			responder.remove;
			audioInSynth.free;
			CmdPeriod.remove(cmdPeriodFunc);
			
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
		^[2, point, params]; // channels, point, params
	}
	
}