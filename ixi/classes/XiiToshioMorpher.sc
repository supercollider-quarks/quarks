
XiiToshioMorpher {

	var <>xiigui, <>win, params;
	var stateDict, stateNum;
	var ldSndsGBufferList, selbPool;

	*new { arg server, channels, setting = nil; 
		^super.new.initXiiToshioMorpher( server, channels, setting);
	}
	
	initXiiToshioMorpher { arg server, channels, setting; 

		var point, userview;
		var toshioArray;
		var selT; // selected Toshio
		var rotationDirection = 1;
		var agentArray;
		var generative, temposl;
		var bufferPop, poolName, sndNameList, bufferList, gBufferPoolNum;
		var toshioNodeSampleIndexArray;
		var directionsArray;
		var outbus, globalvol, startbutt;
		var statesArray, statespop;
		var silentBuf;
		var tempo, volume, threshold;
		var loopmode, timermode, playmode; 
		var threshradio, timermoderadio, playmodepopup, loopmoderadio, volslider, thresholdslider, outbuspop, threshbool;
		
		selT = [2, 2];
		XiiToshioNode.selectednode = selT;
		toshioNodeSampleIndexArray = {{0} ! 10} ! 10; // storing indexes to buffers
		directionsArray = {{8.rand} ! 10} ! 10; // the directions of the toshios
		
		stateDict = ();		
		stateNum = 0;
		outbus = 0;
		globalvol = 1;
		// clock = TempoClock.new(1, 0, Main.elapsedTime.ceil);
		tempo = 60; // slider bpm value - not clock value (60/60)
		timermode = 0; 	// default: \timer
		playmode = 0;		// default: \morph
		loopmode = 1; 	// default: true
		volume = 1;
		threshold = 5;
		
		if(setting.isNil, {
			point = Point(400, 100);
			params = [toshioNodeSampleIndexArray, directionsArray, 60, timermode, playmode, loopmode, volume, threshold, outbus, 0];
		}, { // coming from a preset setting
			point = setting[1];
			stateDict = setting[2];
			stateNum = stateDict.size;
			// ok - set state 1 as default state, and load vars - GUI views take care of themselves
			params = setting[2].at("state 1".asSymbol);
			toshioNodeSampleIndexArray = params[0].copy;
			directionsArray = params[1].copy;
			tempo = params[2].copy;
			timermode = params[3].copy; 
			playmode = params[4].copy; 
			loopmode = params[5].copy; 
			volume = params[6].copy;
			threshold = params[7].copy;
			outbus = params[8].copy;
			threshbool = params[9].copy;			
		});
		
		silentBuf = Buffer.alloc(server, 44100, 1); // one sec buf
		
		toshioArray = Array.fill(10, {arg xx; 
						Array.fill(10, {arg yy; 
							XiiToshioNode.new(Point(30+(xx*50), 30+(yy*50)), xx, yy)
								.setArrow_(true)
								.setGenerative_(false);
						})
					});
		toshioArray.do({arg row; row.do({arg toshio; toshio.supplyToshioArray(toshioArray)}) });
							
		agentArray = [];
		statesArray = [];
			
		win = Window.new("- toshiomorpher -", Rect(point.x, point.y, 530, 620), resizable:false).front;
	
		userview = UserView.new(win, Rect(10, 10, 510, 510))
				.canFocus_(true)
				.focusColor_(Color.black.alpha_(0))
				//.relativeOrigin_(false)
				.drawFunc_({
					Pen.color = Color.white;
					Pen.fillRect(Rect(0, 0, 510, 510));
					toshioArray.do({arg row; row.do({arg toshio; toshio.draw }); });
				})
				.mouseDownAction_({arg me, x, y, modifiers, buttonNumber, clickCount;
					if(clickCount == 2, {
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.arrowHandler(x,y)}); 
						});
					},{
						toshioArray.do({arg row; 
							row.do({arg toshio; 
								if(toshio.rotateToshio( x, y, rotationDirection ), {
									bufferPop.value_(
										toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]]
									);
								})
							}); 
						});
					});
					me.refresh;		
				})
				.keyDownAction_({arg me, char, modifiers, unicode, keycode;
					var oldsel;
					oldsel = selT;
					// next move
				//	[me, char, modifiers, unicode, keycode].postln;
					
					if(char==$n, {
						agentArray.do({arg agent; agent.start });
						{agentArray.do({arg agent; agent.stop })}.defer(0.2);
					});
					
					if(char==$a, {
						toshioArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]].automate(me);
					});
					
					if(char==$q, { // stop all automation
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.stopAutomation })
						});
					});
					
					
					// reorganise
					if(char==$r, {
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.reOrganise}); 
						});					
						me.refresh;
					});
					
					/*
					// set resetAllFlag to true
					if(char==$t, {
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.setResetAllFlag_(true)}); 
						});					
						me.refresh;
					});
					
					// set resetAllFlag to false
					if(char==$f, {
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.setResetAllFlag_(false)}); 
						});					
						me.refresh;
					});
					*/
					
					/*
					// set energy thresholds
					if(char.asString.interpret.isInteger, {
						var thresh;
						thresh = char.asString.interpret;
						if(thresh == 0, {thresh = inf});
						toshioArray.do({arg row; 
							row.do({arg toshio; toshio.setEnergyThreshold_(thresh)}); 
						});					
						me.refresh;
					});
					*/
					// rotationdirection
					if(char==$z, {
						rotationDirection = -1;
					});
					
					// arrow buttons to move selection box around
					if (unicode == 16rF700, { 
						XiiToshioNode.selectednode = XiiToshioNode.selectednode - [0, 1];
						if(XiiToshioNode.selectednode[1] < 0, {XiiToshioNode.selectednode[1] = 9 });
						toshioArray.do({arg row; row.do({arg toshio; toshio.updateStatus }) });
						bufferPop.value_(
							toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]]
						);
					});
					if (unicode == 16rF703, { 
						XiiToshioNode.selectednode = XiiToshioNode.selectednode + [1, 0];
						if(XiiToshioNode.selectednode[0] > 9, {XiiToshioNode.selectednode[0] = 0 });
						toshioArray.do({arg row; row.do({arg toshio; toshio.updateStatus }) });
						bufferPop.value_(
							toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]]
						);
					});
					if (unicode == 16rF701, { 
						XiiToshioNode.selectednode = XiiToshioNode.selectednode + [0, 1];
						if(XiiToshioNode.selectednode[1] > 9, {XiiToshioNode.selectednode[1] = 0 });
						toshioArray.do({arg row; row.do({arg toshio; toshio.updateStatus }) });
						bufferPop.value_(
							toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]]
						);
					});
					if (unicode == 16rF702, { 
						XiiToshioNode.selectednode = XiiToshioNode.selectednode - [1, 0];
						if(XiiToshioNode.selectednode[0] < 0, {XiiToshioNode.selectednode[0] = 9 });
						toshioArray.do({arg row; row.do({arg toshio; toshio.updateStatus }) });
						bufferPop.value_(
							toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]]
						);
					});
					if (unicode == 13, {
						toshioArray[	XiiToshioNode.selectednode[0]]
									[XiiToshioNode.selectednode[1]].rotate(rotationDirection);
					});
					me.refresh;
				})
				.keyUpAction_({arg me, char, modifiers, unicode, keycode;
					if(char==$z, {
						rotationDirection = 1;
					});
				});
		
		selbPool = PopUpMenu.new(win, Rect(10, 530, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray}))
			.value_(0)
			.canFocus_(false)
			.background_(Color.white)
			.action_({ arg item;
				gBufferPoolNum = item.value;
				ldSndsGBufferList.value(selbPool.items[item.value]);
				agentArray.do({arg agent; agent.supplyBufferPoolName_(poolName) });
			});
	
		bufferPop = PopUpMenu.new(win, Rect(10, 550, 100, 16)) // 550
			.font_(GUI.font.new("Helvetica", 9))
			.items_(["none"])
			.background_(Color.new255(255, 255, 255))
			.canFocus_(false)	
			.action_({ arg popup;
				toshioNodeSampleIndexArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]] = popup.value;
				params[0] = toshioNodeSampleIndexArray;
				if(popup.value == 0, {
					toshioArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]].setSound_(false);
				}, {
					toshioArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]].setSound_(true);
				});
				win.refresh;			
			})
			.addAction({ bufferPop.action.value( bufferPop.value )}, \mouseDownAction);
	
		playmodepopup = PopUpMenu.new(win, Rect(10, 570, 100, 16)) // 550
			.font_(GUI.font.new("Helvetica", 9))
			.items_([\morph, \playback, \softwipe, \copyphase, \rectcomb, \randwipe])
			.value_(params[4]) // indexOf does not work with strings, but symbols
			.background_(Color.new255(255, 255, 255))
			.canFocus_(false)	
			.action_({ arg popup;
				var tempplaymode; // 
				switch(popup.value)
				{0}{
					tempplaymode = \morph;
					playmode = 0;
				}
				{1}{
					tempplaymode = \playback;
					playmode = 1;
				}
				{2}{
					tempplaymode = \softwipe;
					playmode = 2;
				}
				{3}{
					tempplaymode = \copyphase;
					playmode = 3;
				}
				{4}{
					tempplaymode = \rectcomb;
					playmode = 4;
				}
				{5}{
					tempplaymode = \randwipe;
					playmode = 5;
				};

				/*
				if(popup.value == 0, { 
					tempplaymode = \morph;
					playmode = 0;
				}, { 
					tempplaymode = \playback;
					playmode = 1;
				});
				*/
				
				agentArray.do({arg agent; agent.setPlayMode_(tempplaymode) });
				params[4] = popup.value;
			})
			.addAction({ bufferPop.action.value( bufferPop.value )}, \mouseDownAction);
	
		Button.new(win, Rect(10, 590, 100, 16))
			.states_([["random buffers", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioNodeSampleIndexArray = {{((bufferList.size-1).rand)} ! 10} ! 10;
				params[0] = toshioNodeSampleIndexArray;
				toshioArray.do({arg row, x; 
					row.do({arg toshio, y; 
						toshio.setSound_( toshioNodeSampleIndexArray[x][y]!=0 );
					}); 
				});
				agentArray.do({arg agent;
					agent.supplyBufferIndexArray_(toshioNodeSampleIndexArray) 
				});
				userview.refresh;
			 });
	
		Button.new(win, Rect(120, 530, 25, 16))
			.states_([["<", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioArray[	XiiToshioNode.selectednode[0]]
									[XiiToshioNode.selectednode[1]].rotate(-1);
				userview.refresh;
			 });
	
		Button.new(win, Rect(150, 530, 25, 16))
			.states_([[">", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioArray[	XiiToshioNode.selectednode[0]]
									[XiiToshioNode.selectednode[1]].rotate(1);
				userview.refresh;
			 }); 
	
		Button.new(win, Rect(120, 550, 55, 16))
			.states_([["automate", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]].automate(userview);
			 });
	
		Button.new(win, Rect(120, 570, 55, 16))
			.states_([["scramble", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioArray.do({arg row; 
					row.do({arg toshio; toshio.reOrganise}); 
				});					
				userview.refresh;
			 });
	
		Button.new(win, Rect(120, 590, 55, 16))
			.states_([["free nodes", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				toshioNodeSampleIndexArray = {{ 0 } ! 10} ! 10;
				toshioArray.do({arg row, x; 
					row.do({arg toshio, y; 
						toshio.setSound_( false );
					}); 
				});
				userview.refresh;
			 });
	
		statespop = PopUpMenu.new(win, Rect(185, 530, 50, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(stateDict.size>0, {stateDict.keys.asArray.sort}, {["states"]}))
			.value_(0)
			.canFocus_(false)
			.background_(Color.white)
			.action_({ arg popup;
				if(statespop.items.size > 0, { // if there are any states
					params = stateDict.at(popup.items[popup.value].asSymbol).deepCopy;
					
					toshioNodeSampleIndexArray = params[0].deepCopy;
					directionsArray = params[1].deepCopy;
					tempo = params[2].copy;
					timermode = params[3].copy; 
					playmode = params[4].copy; 
					loopmode = params[5].copy; 
					volume = params[6].copy;
					threshold = params[7].copy;
					outbus = params[8].copy;
					threshbool = params[9].copy;
										
					toshioArray.do({arg row, x; 
						row.do({arg toshio, y; toshio.setDirection_(directionsArray[x][y])}); 
					});			
					temposl.valueAction_(tempo);
					timermoderadio.valueAction_(timermode);
					playmodepopup.valueAction_(playmodepopup.items[playmode]);
					loopmoderadio.valueAction_(loopmode.booleanValue);
					volslider.valueAction_(volume);
					thresholdslider.valueAction_(threshold);
					outbuspop.valueAction_(outbus);
					threshradio.valueAction_(threshbool);
					
					// XXX and refresh the userview
					userview.refresh;
				});
				
			});
	
		Button.new(win, Rect(185, 550, 50, 16))
			.states_([["store", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({
				var temparray;
				temparray = {{0}!10}!10;
				toshioArray.do({arg row, x; 
					row.do({arg toshio, y; temparray[x][y] = toshio.getDirection }); 
				});			
				
				stateNum = stateNum + 1;
				if(statespop.items[0] == "states", {
					statespop.items_(["state 1"]);
				}, {
					statespop.items_(statespop.items.add("state"+stateNum));
					statespop.value_(stateNum-1);
				});
				
				directionsArray = temparray;
				
				params = [
					toshioNodeSampleIndexArray.deepCopy,
					directionsArray.deepCopy,
					tempo.copy,
					timermode.copy, 
					playmode.copy, 
					loopmode.copy,
					volume.copy,
					threshold.copy,
					outbus.copy,
					threshradio.value
				];
				stateDict.add(("state "++stateNum.asString).asSymbol -> params.deepCopy);
			});
	
		Button.new(win, Rect(185, 570, 50, 16))
			.states_([["clear", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({
				statesArray = [];
				statespop.items_(["states"]);
				statespop.value_(0);
			});
	
		Button.new(win, Rect(245, 530, 65, 16))
			.states_([["new agent", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({
				agentArray = agentArray.add( 
								XiiToshioAgent.new(XiiToshioNode.selectednode, toshioArray, userview, silentBuf)
									.supplyBufferIndexArray_(toshioNodeSampleIndexArray)
									.supplyBufferPoolName_(poolName)
									.setPlayMode_(playmodepopup.items[playmode])
									.setLoopMode_(loopmode.booleanValue)
									.setTimerMode_(timermode)
									.setOutbus_(outbus)
									.setVolume_(volume)
									.setTempo_(tempo/60);
							);										agentArray.do({arg agent; agent.supplyAgentArray_(agentArray) });
							
				toshioArray[XiiToshioNode.selectednode[0]][XiiToshioNode.selectednode[1]].setWithAgent_(true);
				userview.refresh;
			});
			
		Button.new(win, Rect(245, 550, 65, 16))
			.states_([["kill agent", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ 
				if(agentArray.size > 0, {
					agentArray.last.kill;
					agentArray.removeAt(agentArray.size-1);
					if(agentArray.size == 0, { startbutt.value_(0); });
					userview.refresh;
				}, {
					startbutt.value_(0);
				});
			 });
			 
	
		startbutt = Button.new(win, Rect(245, 570, 65, 16))
			.states_([["start", Color.black, Color.clear], ["stop", Color.black, XiiColors.lightgreen]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)		
			.action_({ arg butt;
				if(butt.value == 1, {
					if(agentArray.size == 0, { // if there is no agent running we just add one
						agentArray = agentArray.add( 
								XiiToshioAgent.new(XiiToshioNode.selectednode, toshioArray, userview, silentBuf)
									.supplyBufferIndexArray_(toshioNodeSampleIndexArray)
									.supplyBufferPoolName_(poolName)
									.setPlayMode_(playmodepopup.items[playmode])
									.setTimerMode_(timermode)
									.setLoopMode_(loopmode.booleanValue)
									.setOutbus_(outbus)
									.setVolume_(volume)
									.setTempo_(tempo/60)
									.start;
								);
						agentArray.do({arg agent; agent.supplyAgentArray_(agentArray) });
					}, {
						agentArray.do({arg agent; agent.start });
					});
				}, {
					agentArray.do({arg agent; agent.stop });
				});
			}); 
		
		temposl = OSCIISlider.new(win, Rect(245, 590, 65, 10), "- bpm", 20, 240, params[2], 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl;
				params[2] = sl.value;
				tempo = sl.value;
				if( timermode == 0, { // timer
					agentArray.do({arg agent; agent.setTempo_(tempo/60) });
				});
			});
	
	
		thresholdslider = OSCIISlider.new(win, Rect(322, 530, 55, 10), "thresh", 1, 10, params[7], 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl;
				threshold = sl.value;
				toshioArray.do({arg row; 
					row.do({arg toshio; 
						toshio.setEnergyThreshold_(threshold);
					}); 
				});
				params[7] = threshold;
			});
	
		threshradio = OSCIIRadioButton(win, Rect(322, 560, 12, 12), "threshold")
			.value_(params[9])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg but; 
				threshbool = but.value;
				toshioArray.do({arg row; 
					row.do({arg toshio; 
						toshio.setGenerative_(but.value);
						if(but.value == 0, {toshio.resetEnergy; });
					}); 
				});
				params[9] = but.value;
			});
		
		timermoderadio = OSCIIRadioButton(win, Rect(322, 576, 12, 12), "timer/snd")
			.value_( params[3])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg but;
				if(but.value==0, { // timermode
					timermode = 0; // timer
					agentArray.do({arg agent; agent.setTempo_(tempo/60).setTimerMode_(\timer) });
				},{  // time-of-sound mode
					timermode = 1; // sound
					agentArray.do({arg agent; agent.setTempo_(1).setTimerMode_(\sound) });
				});
				params[3] = but.value;
			});
			
		loopmoderadio = OSCIIRadioButton(win, Rect(322, 592, 12, 12), "loop")
			.value_(params[5])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg but; 
				if(but.value==0, { // loopmode
					loopmode = 0;
					agentArray.do({arg agent; agent.setLoopMode_(loopmode.booleanValue) });
				}, {
					loopmode = 1;
					agentArray.do({arg agent; agent.setLoopMode_(loopmode.booleanValue) });
				});
				params[5] = but.value;
			});
	
		StaticText.new(win, Rect(390, 530, 80, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("outbus :");
			
		outbuspop = PopUpMenu.new(win, Rect(430, 530, 50, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.background_(Color.new255(255, 255, 255))
			.value_(params[8])
			.action_({ arg popup;
				outbus = popup.value * 2;
				agentArray.do({arg agent; agent.setOutbus_(outbus) });
				params[8] = outbus;
			});
	
		volslider = OSCIISlider.new(win, Rect(390, 555, 90, 10), "- vol", 0, 1, params[6], 0.01, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl;
				volume = sl.value;
				agentArray.do({arg agent; agent.setVolume_(volume) });
				params[6] = volume;
			});
	
		ldSndsGBufferList = {arg argPoolName;
				poolName = argPoolName.asSymbol;
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
						sndNameList = ["none"];
						bufferList = List.new;
						XQ.globalBufferDict.at(poolName)[0].do({arg buffer;
							sndNameList = sndNameList.add(buffer.path.basename);
							bufferList.add(buffer.bufnum);
						});
						 bufferPop.items_(sndNameList);
						 bufferPop.action.value(0); // put the first file into the view and load buffer                       
				}, {
					sndNameList = [];
				});
			};
			
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		toshioArray.do({arg row, x; 
			row.do({arg toshio, y; 
				toshio.setSound_( toshioNodeSampleIndexArray[x][y]!=0 );
			}); 
		});
		
		toshioArray[XiiToshioNode.selectednode[0]] [XiiToshioNode.selectednode[1]].updateStatus; // draw selected
		userview.update; // and update 
		userview.focus(true);

		win.onClose_({
			var t;
			agentArray.do({arg agent; agent.stop }); // kill all agents
			toshioArray.do({arg row; row.do({arg toshio; toshio.stopAutomation }) });
			silentBuf.free;
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, { t = i })});
			try{ XQ.globalWidgetList.removeAt(t) };
		});
	}

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
		if(stateDict.size == 0, {
			stateDict.add("state 1".asSymbol -> params.copy); // we create a state
		});
		^[2, point, stateDict ];
	}
}

