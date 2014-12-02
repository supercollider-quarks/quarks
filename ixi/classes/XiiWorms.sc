XiiWorms {

	var <>xiigui, <>win, params;

	var selbPool, ldSndsGBufferList, sndNameList, bufferPop, gBufferPoolNum;
	var preyArray, a, poolname;
	
	*new {arg server, channels, setting = nil;
		^super.new.initXiiWorms(server, channels, setting);
	}

	initXiiWorms {arg server, channels, setting;
	
		var ww, wview, outBus, soundFuncPop, bufferList;
		var predatorArray;
		var sampleNameField, pitchSampleField, keybButt;
		var playButt, cmdPeriodFunc;
		var createCodeWin, createAudioStreamBusWin, createEnvWin, synthDefPrototype, synthDefInUse;
		var inbus, createEnvButt, envButt;
		var curveSl, lengthSl, speedSl, volSl, point;
		var funcwin, envwin, aswin, midikwin; // in order to close all windows on main win closing
 		var sizeSl, rangeSl, circleVolSl;

		gBufferPoolNum = 0;
//		preyArray = [];
		predatorArray = [];
		sndNameList = List.new;
		bufferList = List.new; // contains bufnums of buffers (not buffers)
		
		synthDefInUse = nil;
		synthDefPrototype = 
		{SynthDef(\xiiCode, {arg outbus=0, freq=440, pan=0, amp=1;
			var env, sine;
			env = EnvGen.ar(Env.perc, doneAction:2);
			sine = SinOsc.ar(freq, 0, env*amp);
			Out.ar(outbus, Pan2.ar(sine, pan));
		}).play(Server.default)}.asCompileString;

xiigui = nil;
point = if(setting.isNil, {Point(208, 164)}, {setting[1]});
params = if(setting.isNil, {[4, 18, 20, 1, 0, 0.4]}, {setting[2]});

		win = GUI.window.new("- worms -", Rect(point.x, point.y, 960, 720), resizable:false);
		wview = win.view;
		
		a = XixiPainter.new(win, Rect(10, 5, 940, win.bounds.height-110)); // 640 * 480 resolution
		
		preyArray = Array.fill(3, { XixiSampleCircle.new(Point.new(100+(400.rand), 100+(200.rand)), Rect(10, 5, 940, win.bounds.height-110), a) });

		//preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
		preyArray.do({|prey| prey.supplyPreyArray(preyArray)});
		preyArray.choose.selected = true;
		
		a.addToDrawList(preyArray);
		
//		predatorArray.do({|predator| predator.supplyOtherWorms(predatorArray)});
//		a.addToDrawList(predatorArray);
		a.frameRate = 0.05;

		// -- the buffers
		ldSndsGBufferList = {arg argPoolName;
			poolname = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolname)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				XQ.globalBufferDict.at(poolname)[0].do({arg buffer;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
			}, {
				sndNameList = [];
			});
		};

		// -- the GUI
		// add predator
		Button.new(win, Rect(10, win.bounds.height-95, 65, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["add worm",Color.black,Color.clear]])
			.action_({ var p;
				p = 	XixiWorm.new(Point.new(600.rand, 500.rand), 6, lengthSl.value, Rect(10, 5, 940, win.bounds.height-110));
				predatorArray = predatorArray.add(p);
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				predatorArray.do({|predator| predator.supplyPredatorArray(predatorArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
				win.refresh;
			});
			
		// delete predator
		Button.new(win, Rect(10, win.bounds.height-74, 65, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["del predator",Color.black,Color.clear]])
			.action_({
				if(predatorArray.size > 1, {predatorArray.removeAt(0)});
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				predatorArray.do({|predator| predator.supplyPredatorArray(predatorArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
				win.refresh;
			});
		
		// add circle
		Button.new(win, Rect(78, win.bounds.height-95, 50, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["add circle",Color.black,Color.clear]])
			.action_({ var p;
				p = XixiSampleCircle.new(Point.new(100+(400.rand), 100+(200.rand)), Rect(10, 5, 620, 470), a);
				p.supplyTextFields([sampleNameField, pitchSampleField]);
				p.setRandomBuffer(gBufferPoolNum); // new prey gets a random buffer
				preyArray = preyArray.add(p);
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				preyArray.do({|prey| 
					prey.supplyPredatorArray(predatorArray);
					prey.supplyPreyArray(preyArray);
				});
				predatorArray.do({|predator| predator.supplyPreyArray(preyArray)});
				win.refresh;
			});
		
		// delete prey
		Button.new(win, Rect(78, win.bounds.height-74, 50, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["del prey",Color.black,Color.clear]])
			.action_({
				if(preyArray.size > 1, {preyArray.removeAt(0)});
				a.replaceDrawList(preyArray);
				a.addToDrawList(predatorArray);
				preyArray.do({|prey| prey.supplyPreyArray(preyArray)});
				preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
				predatorArray.do({|predator| predator.supplyPreyArray(preyArray)});
				win.refresh;
			});
		
		/*
		pitchSampleField = StaticText.new(win, Rect(275, 535, 60, 20))
				.font_(GUI.font.new("Helvetica", 9))
				.string_("prey sample :");
				
		sampleNameField =	GUI.staticText.new(win, Rect(340, 535, 100, 20))
				.font_(GUI.font.new("Helvetica", 9))
				.string_("none");
		
		
		StaticText.new(win, Rect(265, 530, 205, 30))
				.background_(Color.new255(255, 100, 0, 30))
				.string_("");
		
		preyArray.do({|prey| prey.supplyTextFields([sampleNameField, pitchSampleField])});
		
		*/
		
		selbPool = PopUpMenu.new(win, Rect(400, win.bounds.height-95, 102, 16)) // 530
				.font_(GUI.font.new("Helvetica", 9))
				.items_( if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}) )
				.value_(0)
				.background_(Color.white)
				.action_({ arg item;
					try{
						gBufferPoolNum = item.value;
						ldSndsGBufferList.value(selbPool.items[item.value]);
						bufferPop.items_(sndNameList);
						preyArray.do({|prey, i| prey.setRandomBuffer(selbPool.items[item.value])});
					}
				});		
			
		bufferPop = PopUpMenu.new(win,Rect(400, win.bounds.height-74, 102, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["no buffer 1", "no buffer 2"])
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					preyArray.do({|prey| prey.setMyBuffer(selbPool.items[gBufferPoolNum], popup.value)});
				})
				.addAction({bufferPop.action.value( bufferPop.value )}, \mouseDownAction);
		
		/*
		StaticText.new(win, Rect(410, win.bounds.height-60, 80, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("sound :");
		*/
		
		soundFuncPop = PopUpMenu.new(win, Rect(510, win.bounds.height-95, 60, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["sample", "sine", "bells", "sines", "synth1", "ks_string", 
				"ixi_string", "impulse", "ringz", "klanks", "scode", "audiostream"])
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					createEnvButt.value(false);
					if(soundFuncPop.items[popup.value] == "scode", {
						createCodeWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "audiostream", {
						createAudioStreamBusWin.value;
						createEnvButt.value(true);
					}); 
					if(soundFuncPop.items[popup.value] == "sample", {
						createEnvButt.value(true);
						preyArray.do({|prey, i| prey.setRandomBuffer(selbPool.items[selbPool.value])});
					}); 
					preyArray.do({|prey| prey.setAteFunc_(popup.value)});
					params[3] = popup.value;
				})
				.value_(1);
		
		/*
		StaticText.new(win, Rect(375, win.bounds.height-74, 80, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("outbus :");
		*/
		
		outBus = PopUpMenu.new(win, Rect(510, win.bounds.height-74, 60,16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup; var outbus;
					preyArray.do({|prey| prey.setOutBus_(popup.value * 2)});
					params[4] = popup.value;
				});
		
		GUI.button.new(win, Rect(610, win.bounds.height-74, 70, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["fixed pitch",Color.black,Color.clear], ["locative pitch",Color.black,Color.clear]])
			.action_({arg butt; 
				preyArray.do({|prey| prey.setPitchMode_(butt.value)});
				if(butt.value == 1, {
					keybButt = GUI.button.new(win, Rect(477, 485, 26, 18))
						.font_(GUI.font.new("Helvetica", 9))
						.canFocus_(false)
						.states_([["key",Color.black,Color.clear]])
						.action_({var func, k;
							func = {arg note; 	
								preyArray.do({|prey| prey.setPitch_(note)})
							};
							midikwin = GUI.window.new("set pitch", 
							Rect(win.bounds.left+400, win.bounds.top+230, 400, 80), resizable:false).front;
							midikwin.alwaysOnTop = true;
							k = MIDIKeyboard.new(midikwin, Rect(10, 5, 374, 60), 5, 24);
							k.keyDownAction_({arg note; func.value(note)});
							k.keyTrackAction_({arg note; func.value(note)});
						});
					win.refresh;
				}, {
					if(ww.isKindOf(GUI.window.new), {ww.close;});
					keybButt.remove;
					win.refresh;
				});
			});
			
		// start stop
		playButt = Button.new(win, Rect(585, win.bounds.height-95, 45, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["start",Color.black,Color.clear], ["stop",Color.black, Color.green(alpha:0.2)]])
			.action_({arg butt;
				if(butt.value == 1, {
					predatorArray.postln;
					if(predatorArray == [], {
						predatorArray = predatorArray.add(XixiWorm.new(Point.new(300.rand, 300.rand), 6, 10, Rect(12, 5, win.bounds.width-20, win.bounds.height-110)) );
						predatorArray.do({|predator| predator.supplyOtherWorms(predatorArray)});
						preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
						a.addToDrawList(predatorArray);
//					}, {
//						predatorArray = predatorArray.add(XixiWorm.new(Point.new(300.rand, 300.rand), 6, 10, Rect(12, 5, 630, 470)) );
//						predatorArray.do({|predator| predator.supplyOtherWorms(predatorArray)});
//						preyArray.do({|prey| prey.supplyPredatorArray(predatorArray)});
//						a.addToDrawList(predatorArray);
					});
					a.start;
				}, {
					a.stop;
				});
			});
		
		
		volSl = OSCIISlider.new(win, Rect(510, win.bounds.height-50, 117, 10), "- vol", 0, 1, 0.4, 0.0001, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				preyArray.do({|prey, i| prey.setVolume_(sl.value)});
				params[5] = sl.value;
			});
		
		curveSl = OSCIISlider.new(win, Rect(137, win.bounds.height-95, 117, 10), "- curverange", 1, 50, 4, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.setCurves_(sl.value)});
				params[0] = sl.value;
			});
		
		// tailslider
		lengthSl = OSCIISlider.new(win, Rect(137, win.bounds.height-65, 117, 10), "- length", 1, 25, 10, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.setLength_(sl.value)});
				params[1] = sl.value;
			});
		
		speedSl = OSCIISlider.new(win, Rect(137, win.bounds.height-35, 117, 10), "- speed", 5, 60, 20, 0.1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				predatorArray.do({|predator, i| predator.setSpeed = sl.value/10});
				params[3] = sl.value;
			});

		sizeSl = OSCIISlider.new(win, Rect(265, win.bounds.height-95, 117, 10), "- size", 15, 100, 20, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				preyArray.do({|prey, i| prey.setSize_(sl.value)});
				win.refresh;
				//params[0] = sl.value;
			});
		
		// rangeslider
		rangeSl = OSCIISlider.new(win, Rect(265, win.bounds.height-65, 117, 10), "- range", 20, 200, 10, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				preyArray.do({|prey, i| prey.setRange_(sl.value)});
				win.refresh;
				//params[1] = sl.value;
			});
		
		circleVolSl = OSCIISlider.new(win, Rect(265, win.bounds.height-35, 117, 10), "- volume", 0, 10, 5, 0.1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				preyArray.do({|prey, i| prey.setAmp = sl.value/10});
				//params[3] = sl.value;
			});
		
		// -- stuff to do when GUIs are created
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		try{preyArray.do({|prey, i| prey.setMyBuffer(gBufferPoolNum, i, true)})}; // loading = true
		
		createEnvButt = {arg state;
			if(state == true, {
				envButt = GUI.button.new(win, Rect(477, 506, 26, 18))
					.font_(GUI.font.new("Helvetica", 9))
					.canFocus_(false)
					.states_([["env",Color.black,Color.clear]])
					.action_({var func, k;
						createEnvWin.value;
					});
			}, {
				envButt.remove;
				win.refresh;
			})
		};
		
		/*
		createCodeWin = {
				var func, subm, test, view;
				funcwin = Window.new("scode", Rect(600, 400, 440, 200)).front;
				funcwin.alwaysOnTop = true;
				
				view = funcwin.view;
				func = GUI.textView.new(view, Rect(20, 10, 400, 140))
						.font_(GUI.font.new("Monaco", 9))
						.resize_(5)
						.focus(true)
						.string_(
							if(synthDefInUse.isNil, { 
								synthDefPrototype
							},{
								synthDefInUse
							});
						);
				test = Button.new(view, Rect(280,160,50,18))
						.states_([["test",Color.black,Color.clear]])
						.resize_(9)
						.font_(GUI.font.new("Helvetica", 9))
						.action_({
							func.string.interpret.value;
						});
						
				subm = Button.new(view, Rect(340,160,50,18))
						.states_([["submit",Color.black,Color.clear]])
						.resize_(9)
						.font_(GUI.font.new("Helvetica", 9))
						.action_({
							func.string.interpret;
							synthDefInUse = func.string;
							funcwin.close;
						});

		};

		*/
		
		createEnvWin = {arg index;
			var envview, timesl, setButt, timeScale;
			var selectedprey;
			
			preyArray.do({|prey, i| if(prey.selected == true, {selectedprey = i})}); // find the selected prey

			timeScale = 1.0;
			
			envwin = GUI.window.new("adsr envelope", Rect(200, 450, 250, 130), resizable:false).front;
			envwin.alwaysOnTop = true;
			
			envview = GUI.envelopeView.new(envwin, Rect(10, 5, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.canFocus_(false)
				.drawRects_(true)
				.background_(XiiColors.lightgreen)
				.fillColor_(XiiColors.darkgreen)
				.action_({arg b; })
				.thumbSize_(5)
				.env2viewFormat_(Env.new(preyArray[selectedprey].getEnv[0], preyArray[selectedprey].getEnv[1]))
				.setEditable(0, false);


			timesl = OSCIISlider.new(envwin, 
						Rect(10, 100, 130, 8), "- duration", 0.1, 10, preyArray[selectedprey].getEnv[2], 0.01)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg sl; });
			
			setButt = GUI.button.new(envwin, Rect(160, 100, 60, 16))
					.states_([["set envelope", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						var env;
						env = envview.view2envFormat ++ timesl.value; // levels, times, duration
						preyArray[selectedprey].setEnvelope_(env);
						envwin.close;
					});
		};

		createAudioStreamBusWin = {arg index;
			var envview, timesl, setButt;
			aswin = GUI.window.new("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
			aswin.alwaysOnTop = true;
				
			GUI.staticText.new(aswin, Rect(20, 55, 20, 16))
				.font_(GUI.font.new("Helvetica", 9)).string_("in"); 

			GUI.popUpMenu.new(aswin, Rect(35, 55, 50, 16))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(10)
				.font_(GUI.font.new("Helvetica", 9))
				.background_(Color.white)
				.canFocus_(false)
				.action_({ arg ch; var inbus;
					inbus = ch.value * 2;
					preyArray.do({|prey| prey.setInBus_(inbus)});
				});

			setButt = GUI.button.new(aswin, Rect(120, 55, 60, 16))
					.states_([["set inbus", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						aswin.close;
					});
		};
			
		createEnvButt.value(false);  // only on sample: default is sine, so no env button

		cmdPeriodFunc = { playButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({ 
			var t;
			a.stop;
			a.remove;
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({ arg widget, i; if(widget == this, {t = i}) });
			try{ XQ.globalWidgetList.removeAt(t) };
			try{ funcwin.close };
			try{ envwin.close };
			try{ aswin.close };
			try{ midikwin.close };
		});
		
		// setting
		curveSl.valueAction_(params[0]);
		//lengthSl.valueAction_(params[1]);
		//speedSl.valueAction_(params[2]);
		soundFuncPop.valueAction_(params[3]);
		outBus.valueAction_(params[4]);
		volSl.valueAction_(params[5]);
 	}
	
	/*
	updatePoolMenu {
		var pool, poolindex;
		pool = selbPool.items.at(selbPool.value);        // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray); // put new list of pools
		poolindex = selbPool.items.indexOf(pool);        // find the index of old pool in new array
		if(poolindex != nil, {
			selbPool.value_(poolindex); // so nothing changed, but new poolarray
		});	
	}
	*/

	updatePoolMenu {
		var poolname, poolindex;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		if(poolindex != nil, {
			selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
			ldSndsGBufferList.value(poolname);
		}, {
			selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0]); // load first pool
		});
	}

	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}