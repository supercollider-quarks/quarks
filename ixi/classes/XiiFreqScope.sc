XiiFreqScope {
	
	var <scope, <>win;
	var <>xiigui, params;

	*new { arg server, channels, setting = nil;
		if(Server.default == GUI.stethoscope.defaultServer, {
			^super.new.initFreqScope(setting);
		},{
			if(GUI.id == \cocoa, {
				XiiAlert("The FreqScope works only on the internal server!");
			},{
				XiiAlert("The FreqScope works only on the local server!");
			});
		});
	}
	
	initFreqScope { arg setting;
		var rect, scope, pad, font, freqLabel, freqLabelDist, dbLabel, dbLabelDist;
		var setFreqLabelVals, setDBLabelVals;
		var scopeColor, bgColor, height, busNum;
		var onOffButt, cmdPeriodFunc, name, point;
		
		if(scopeColor.isNil, { scopeColor = Color.green });
		if(bgColor.isNil, { bgColor = Color.green(0.1) });
		
		busNum=0;
		height=300;

		name = "- freqscope -";
		
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,7]}, {setting[2]});

		rect = Rect(point.x, point.y, 511, height);
		pad = [30, 38, 14, 10]; // l,r,t,b
		font = GUI.font.new("Helvetica", 9);
		freqLabel = Array.newClear(12);
		freqLabelDist = rect.width/(freqLabel.size-1);
		dbLabel = Array.newClear(17);
		dbLabelDist = rect.height/(dbLabel.size-1);
		
		setFreqLabelVals = { arg mode, bufsize;
			var kfreq, factor, halfSize;
			
			factor = 1/(freqLabel.size-1);
			halfSize = bufsize * 0.5;
			
			freqLabel.size.do({ arg i;
				if(mode == 1, {
					kfreq = (halfSize.pow(i * factor) - 1)/(halfSize-1) * 22.05;
				},{
					kfreq = i * factor * 22.05;
				});
					
				if(kfreq > 1.0, {
					freqLabel[i].string_( kfreq.asString.keep(4) ++ "k" )
				},{
					freqLabel[i].string_( (kfreq*1000).asInteger.asString)
				});
			});
		};
		
		setDBLabelVals = { arg db;
			dbLabel.size.do({ arg i;
				dbLabel[i].string = (i * db/(dbLabel.size-1)).asInteger.neg.asString;
			});
		};

		win = GUI.window.new(name, rect.resizeBy(pad[0] + pad[1] + 18, pad[2] + pad[3] + 4), false);
		
		freqLabel.size.do({ arg i;
			freqLabel[i] = GUI.staticText.new(win, Rect(pad[0] - (freqLabelDist*0.5) + (i*freqLabelDist), pad[2] - 10, freqLabelDist, 10))
				.font_(font)
				.align_(0)
			;
			GUI.staticText.new(win, Rect(pad[0] + (i*freqLabelDist), pad[2], 1, rect.height))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;
		});
		
		dbLabel.size.do({ arg i;
			dbLabel[i] = GUI.staticText.new(win, Rect(0, pad[2] + (i*dbLabelDist), pad[0], 10))
				.font_(font)
				.align_(1)
			;
			GUI.staticText.new(win, Rect(pad[0], dbLabel[i].bounds.top, rect.width, 1))
				.string_("")
				.background_(scopeColor.alpha_(0.25))
			;		
		});

		scope = XiiSCFreqScope(win, Rect(pad[0], pad[2], rect.width, rect.height));
		
		setFreqLabelVals.value(scope.freqMode, 2048);
		setDBLabelVals.value(scope.dbRange);

		GUI.staticText.new(win, Rect(pad[0] + rect.width + 8, rect.height - 110, pad[1], 10))
			.string_("inbus")
			.font_(font);

		GUI.popUpMenu.new(win, Rect(pad[0] + rect.width + 8, rect.height - 95, pad[1], 14))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(params[0])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; var inbus;
				inbus = ch.value * 2;
				scope.inBus_(inbus);
				params[0] = ch.value;
			});

		GUI.staticText.new(win, Rect(pad[0] + rect.width + 8, rect.height - 75, pad[1], 10))
			.string_("FrqScl")
			.font_(font);
			
		GUI.popUpMenu.new(win, Rect(pad[0] + rect.width + 8, rect.height - 60, pad[1], 16))
			.items_(["lin", "log"])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.value_(params[1])
			.action_({ arg view;
				scope.freqMode_(view.value);
				setFreqLabelVals.value(scope.freqMode, 2048);
				params[1] = view.value;
			})
			.canFocus_(false)
			.font_(font);
		
		GUI.staticText.new(win, Rect(pad[0] + rect.width + 8, rect.height - 40, pad[1], 10))
			.string_("dbCut")
			.font_(font);
			
		GUI.popUpMenu.new(win, Rect(pad[0] + rect.width + 8, rect.height - 25, pad[1], 16))
			.items_(Array.series(12, 12, 12).collect({ arg item; item.asString }))
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.action_({ arg view;
				scope.dbRange_((view.value + 1) * 12);
				setDBLabelVals.value(scope.dbRange);
				params[2] = view.value;
			})
			.canFocus_(false)
			.font_(font)
			.value_(params[2]);
			 
		onOffButt = GUI.button.new(win, Rect(pad[0] + rect.width + 8, rect.height-2, pad[1], 16))
			.states_([["On", Color.black, Color.clear], ["Off", Color.black, Color.green(alpha:0.2)]]) 
			.action_({ arg view;
				if(view.value == 1, {
					scope.active_(true);
				},{
					scope.active_(false);
				});
			})
			.font_(font)
			.canFocus_(false);
		
		// ugly code ahead warning!
		scope.scope
			.background_(bgColor)
			.style_(1)
			.canFocus_(false)
			.waveColors_([scopeColor.alpha_(1)]);
			
		scope
			.inBus_(busNum);


		cmdPeriodFunc = { 
		//onOffButt.valueAction_(0)
		};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({
			var t; 
			scope.kill;
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, { t = i}) });
			try{XQ.globalWidgetList.removeAt(t)};
		 }).front;
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; // channels, point, params
	}

}

