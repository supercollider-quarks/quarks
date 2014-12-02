
XiiChannelSplitter {
	
	var <>xiigui;
	var <>win, params;

	var channels;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiChannelSplitter(server, channels, setting);
		}
		
	initXiiChannelSplitter {arg server, ch, setting;
		var bgColor, foreColor, spec;
		var s, name, point;
		var stereoChList, monoChList;
		var inbus, outbus, synth;
		var tgt, addAct, amp;
		var onOffButt, cmdPeriodFunc;
		
		tgt = 1;
		addAct = \addToTail;
		s = server ? Server.local;

		// mono or stereo?
		channels = if(setting.isNil, {ch}, {setting[0]});

		if(ch==1, {name = "- channelsplitter 1x1 -"}, {name = "- channelsplitter 2x2 -"});


		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList =   XiiACDropDownChannels.getMonoChnList;

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		//inbus = params[0];
		if(channels==1, {inbus = params[0]}, {inbus = params[0] * 2});
		outbus = params[1];
		amp = params[2];
		
		win = GUI.window.new(name, Rect(point.x, point.y, 222, 70), resizable:false);
		
		SynthDef(\xiiChannelSplitter1x1, { arg inbus, outbus, amp=1;
			var in;
			in = InFeedback.ar(inbus, 1);
			Out.ar(outbus, in*amp);
		}).load(s);
		
		SynthDef(\xiiChannelSplitter2x2, { arg inbus, outbus, amp=1;
			var in;
			in = InFeedback.ar(inbus, 2);
			Out.ar(outbus, in*amp);
		}).load(s);
				
		spec = ControlSpec(0, 1.0, \amp); // for amplitude in rec slider

		// channels dropdown - INPUT CHANNEL
		GUI.staticText.new(win, Rect(10, 9, 40, 16)).string_("in");
		GUI.popUpMenu.new(win,Rect(35, 10, 50, 16))
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(inbus)
			.background_(Color.white)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				synth.set(\inbus, inbus );
				params[0] = ch.value;
			});
			
		// channels dropdown - OUTPUT CHANNEL
		GUI.staticText.new(win, Rect(10, 34, 40, 16)).string_("out");
		GUI.popUpMenu.new(win,Rect(35, 35, 50, 16))
			.items_(if(channels==1, {monoChList}, {stereoChList}))
			.value_(outbus)
			.background_(Color.white)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				synth.set(\outbus, outbus );
				params[1] = ch.value;
			});
			
		// panning sliders
		OSCIISlider.new(win, Rect(100, 10, 100, 10), "- amp", 0, 1, params[2], 0.01)
			.action_({arg sl; synth.set(\amp, sl.value)});

		GUI.popUpMenu.new(win, Rect(100, 40, 66, 16)) 
		   .font_(GUI.font.new("Helvetica", 9)) 
		   .items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		   .value_(1) 
		   .action_({|v| 
		      addAct = v.items.at(v.value).asSymbol; 
		   }); 

		onOffButt = GUI.button.new(win,Rect(172, 40, 27, 16))
		   .font_(GUI.font.new("Helvetica", 9)) 
			.states_([
					["On",Color.black, Color.clear],
					["Off",Color.black,bgColor]
				])
			.action_({ arg butt;
				if(butt.value == 1, {
					if(channels == 1, {
		        			synth = Synth.new(\xiiChannelSplitter1x1, 
										[\inbus, inbus, \outbus, outbus, \amp, amp], 
										target: tgt.asTarget,
										addAction: addAct); 
					},{
		        			synth = Synth.new(\xiiChannelSplitter2x2, 
										[\inbus, inbus, \outbus, outbus, \amp, amp], 
										target: tgt.asTarget,
										addAction: addAct); 
					});
				},{
					synth.free;
				});
			});
		
		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);
			
		win.front;
		win.onClose_({
			var t;
			onOffButt.valueAction_(0);
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			synth.free;
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[channels, point, params];
	}

}