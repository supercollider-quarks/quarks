
XiiEQMeter {	

	var <>xiigui;
	var <>win, params;

	var multislider, c, b, eqUpdateTask, name;
	var size, synth, cmdPeriodFunc, inbus, onOffButt, refreshTime;
	var colors;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiEQMeter(server, channels, setting);
		}
		
	initXiiEQMeter {arg argserver, channels, setting;
		var s, point;
		var strokeFlag, strokeRButt, fillRButt, freqText, cutfreqs;
		var speedButt;
		
		size = 32; // one extra band in multislider for aesthetic purposes
		s = argserver ? Server.default;
		c = Bus.control(s, size);
		strokeFlag = false;
		refreshTime = 0.1;

		colors = 	[Color.green, Color.black, Color(0.80392156862745, 0.37647058823529, 0.56470588235294, 1), Color(0.93333333333333, 0.38823529411765, 0.38823529411765, 1), Color(0.93333333333333, 0.86666666666667, 0.50980392156863, 1), Color(0.63529411764706, 0.70980392156863, 0.80392156862745, 1), Color(0.80392156862745, 0.70196078431373, 0.54509803921569, 1), Color(0.4, 0.80392156862745, 0.66666666666667, 1), Color(0.80392156862745, 0.71764705882353, 0.61960784313725, 1), Color(0.93333333333333, 0.50980392156863, 0.3843137254902, 1)];
		
		name = "- eq meter -";

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[4,0,0,1,0,0,0]}, {setting[2]});
		
		inbus = params[0]*2;
		cutfreqs = [20, 25, 32.5, 44, 54, 65, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000, 20000];
		
		win = GUI.window.new(name, Rect(point.x, point.y, 520, 243), resizable:false);
		multislider = GUI.multiSliderView.new(win, Rect(10, 5, 496, 200))
						.value_(0.dup(size))
						//.size_(size)
						.isFilled_(true)
						.indexThumbSize_(462/(size+1))
						.background_(Color.green(0.1))
						.canFocus_(false)
						.fillColor_(colors[params[5]])
						.strokeColor_(colors[params[4]])
						.xOffset_(2)
						.action_({arg xb;
							freqText.string_(cutfreqs[xb.index].asString);
						});
		if (GUI.id == \cocoa, { multislider.size_(size) });
						
		b = Buffer.alloc(s, 2048*2, 1);
		
		SynthDef(\XiiEQMeter, { arg inbus; 
			var in, chain, powers, cutfreqs;
			in = InFeedback.ar(inbus, 2);
			in = Mix.ar(in);
			//in = Normalizer.ar(in, 0.94);
			chain = FFT(b.bufnum, in);
			cutfreqs = [20, 25, 32.5, 44, 54, 65, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000, 20000];
			// original freq list:
			//cutfreqs = [20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000];
			powers = FFTSubbandPower.kr(chain, cutfreqs, 0, scalemode: 2);
			Out.kr(c.index, powers);
		}).load(s);

		// INBUS
		GUI.staticText.new(win, Rect(12, 210, 50, 18))
			.string_("inbus")
			.font_(GUI.font.new("Helvetica", 9));

		GUI.popUpMenu.new(win, Rect(40, 212, 40, 14))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(params[0])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				inbus = ch.value * 2;
				synth.set(\inbus, inbus);
				params[0] = ch.value;
			});

		// DRAW STYLE
		GUI.staticText.new(win, Rect(92, 210, 70, 18))
			.string_("bands")
			.font_(GUI.font.new("Helvetica", 9));

		GUI.popUpMenu.new(win, Rect(123, 212, 65, 14))
			.items_(["filled", "unfilled", "lines"])
			.value_(params[1])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				switch (ch.value)
					{0} { multislider.isFilled_(true);
						multislider.drawLines_(false);
						multislider.drawRects_(true);
						}
					{1} { multislider.isFilled_(false);
						 multislider.drawRects_(true);
						 multislider.drawLines_(false); 
						}
					{2} { multislider.drawLines_(true); 
						 multislider.drawRects_(false);
						 multislider.isFilled_(false)
						 };
				params[1] = ch.value;
			});
				
			// since valueAction above does not work.
			switch (params[1])
					{0} { multislider.isFilled_(true);
						multislider.drawLines_(false);
						multislider.drawRects_(true);
						}
					{1} { multislider.isFilled_(false);
						 multislider.drawRects_(true);
						 multislider.drawLines_(false); 
						}
					{2} { multislider.drawLines_(true); 
						 multislider.drawRects_(false);
						 multislider.isFilled_(false)
						};

		// COLORS
		GUI.staticText.new(win, Rect(200, 210, 50, 18))
			.string_("colors")
			.font_(GUI.font.new("Helvetica", 9));

		strokeRButt = OSCIIRadioButton(win, Rect(230, 209, 12, 12), "stroke")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(params[2])
						.action_({ arg butt;
							fillRButt.switchState;
							strokeFlag = true;
							params[2] = butt.value;
							params[3] = fillRButt.value;
						});

		fillRButt = OSCIIRadioButton(win, Rect(230, 222, 12, 12), "fill")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(params[3])
						.action_({ arg butt;
							strokeRButt.switchState;
							strokeFlag = false;
							params[3] = butt.value;
							params[2] = strokeRButt.value;
						});

		GUI.popUpMenu.new(win, Rect(285, 212, 60, 14))
			.items_(["green", "black", "pink", "indian red", "light golen", 
					"steel blue", "navajo white", "aqua marine", "bisque", "salmon"])
			.value_(params[4])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; var color;
				color = colors[ch.value];
				if(strokeFlag, {
					multislider.strokeColor_(color);
					params[4] = ch.value;
				}, {
					multislider.fillColor_(color);
					params[5] = ch.value;
				});
			});

		GUI.staticText.new(win, Rect(360, 210, 60, 18))
			.string_("band freq :")
			.font_(GUI.font.new("Helvetica", 9));

		freqText = GUI.staticText.new(win, Rect(405, 210, 60, 18))
			.string_("25")
			.font_(GUI.font.new("Helvetica", 9));

		speedButt = GUI.button.new(win, Rect(430, 212, 36, 16))
			.states_([
					["fast",Color.black, Color.clear],
					["slow",Color.black, Color.green(alpha:0.2)]
				])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.value_(params[6])
			.action_({ arg butt;
				if(butt.value == 1, {refreshTime = 0.05}, {refreshTime = 0.1});
				params[6] = butt.value;
			});
			
		onOffButt = GUI.button.new(win, Rect(470, 212, 36, 16))
			.states_([
					["on",Color.black, Color.clear],
					["off",Color.black, Color.green(alpha:0.2)]
				])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt;
				if(butt.value == 1, {this.start}, {this.stop});
			});

		cmdPeriodFunc = { onOffButt.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.front;
		win.onClose_({
			var t;
			this.stop;
			b.free;
			c.free;
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	start {
		synth = Synth(\XiiEQMeter, [\inbus, inbus], addAction: \addToTail);
		eqUpdateTask = Task({
			loop{
				c.getn(size, {|vals|
					{
						win.isClosed.not.if({ 
							multislider.value_((vals.log2 * 0.2).max(0).min(1));
						});
					}.defer;
				});
			refreshTime.wait; // is this the right balance?
			};
		}).start;
	}
	
	stop {
		synth.free;
		eqUpdateTask.stop;
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; //channels, point, params
	}

}
