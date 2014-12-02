
XiiSoundSculptor {

	var <>xiigui, <>win, params;
	var ldSndsGBufferList, selbPool;

	*new { arg server, channels, setting = nil; 
		^super.new.initXiiSoundSculptor( server, channels, setting);
	}
	
	initXiiSoundSculptor { arg server, channels, setting; 
		var soundfile, sndfileview, userView;
		var startPoint, endPoint, outbus, outbuspop, globalvol, globalVolSl, pitch, pitchSl, startButt;
		var pointcolor, strokecolor, pointsize, pointlist, linecolor, pointp, mouseLoc;
		var morpherArray, selMorpher, selParameter, playSound, envvalues;
		var bufferPop, synthesisStylePop;
		var sndNameList, bufferList, poolName;
		var morphButtsArray, morphRangesArray, morphsParamsButtArray;
		var lowtext, hightext, actualtext;
		var colors, controls, paramsHiLo;
		var low, high, envlists, synthNamesArray, actualsynthsarray;
		var selBuffer, selStart, selEnd, soundDur;
		var sourceplaying, sourcesynth, bufferpool;
		var timeCursorTask, recorder, point;
		// var recbus;
		
		
		pointp = Point(200,200);
		pointlist = Array.fill(72, {arg i; Point((i*10), 241)});
		
		morpherArray = Array.fill(10, { XiiMorpherFilter.new(\empty) });
		synthNamesArray = Array.fill(10, { \empty });
		
		selMorpher = 0;
		selParameter = 0;
		paramsHiLo = [[0,1], [0,1], [0,1]]; // range slider scale params
		
		outbus = 0;
		pitch = 1;
		globalvol = 1;
		sourceplaying = false;
		
		if(setting.isNil, {
			point = Point(400, 100);
			params = [0];
		}, { // coming from a preset setting
			point = setting[1];
			params = setting[2];
		});

		win = Window.new("- soundsculptor -", Rect(point.x, point.y, 840, 500), resizable:false).front;
		
		soundfile = SoundFile.new;
		soundfile.openRead("sounds/a11wlk01.wav");
		
		sndfileview = SoundFileView.new(win, Rect(120, 5, 700, 482))
			.soundfile_(soundfile)
			.read(0, soundfile.numFrames)
			.elasticMode_(true)
			.timeCursorOn_(true)
			.timeCursorColor_(Color.white)
			.drawsWaveForm_(true)
			.gridOn_(false)
			.waveColors_([ XiiColors.darkgreen, XiiColors.darkgreen ])
			.background_(XiiColors.lightgreen)
			.canFocus_(false)
			.setSelectionColor(0, Color.new255(105, 185, 125));
		
		soundfile.close;
		
		userView = UserView.new(win, Rect(120, 5, 700, 482))
			.canFocus_(false)
			.mouseDownAction_({arg view, x, y, pressure;
				if(selParameter<controls.size, {
				mouseLoc = [x, y];
				startPoint = Point(x,y);
				pointp = Point(x.round(10).round, (y+5).round);
				pointlist = morpherArray[selMorpher].getPointlist(selParameter);
				pointlist[pointp.x/10] = pointp;
				morpherArray[selMorpher].setPointlist(pointlist.copy, selParameter);
				view.refresh;
				});
			})
			.mouseMoveAction_({ arg view, x, y, pressure;
				if(selParameter<controls.size, {
				mouseLoc = [x, y];
				pointp = Point((x-5).round(10).clip(0, 710), (y-5).clip(0, 478));
				pointlist[pointp.x*0.1] = pointp;
				morpherArray[selMorpher].setPointlist(pointlist.copy, selParameter);
				actualtext.string_("actual: " + [high, low, controls[selParameter][3]].asSpec.map(y/478).round(0.001) );
				});
				view.refresh;
			})
			.mouseUpAction_({ arg view, x, y, pressure;
				morpherArray[selMorpher].scalePointLists;
				envlists = morpherArray[selMorpher].getEnvArrays;
			})
			.drawFunc_({
				morpherArray.do({arg morpher; morpher.drawFunc });
			});
			
			win.view.keyDownAction_({arg view, char, modifiers, unicode, keycode;
				var thisbuttstate, newbuttstate;
				switch(keycode)
				{ 126 } {
					if(modifiers.isCmd, { // cmd arrow moves the ordering of filters
						if(selMorpher>0, {
							thisbuttstate = morphButtsArray[selMorpher].states;
							newbuttstate = morphButtsArray[selMorpher-1].states;
							morphButtsArray[selMorpher].states_(newbuttstate);
							morphButtsArray[selMorpher-1].states_(thisbuttstate);
							morpherArray.swap(selMorpher, selMorpher-1);
							morpherArray.do({arg morpher, ii;
								if(selMorpher == ii, { morpher.selectedmorpher = true }, { morpher.selectedmorpher = false });
							});
							synthNamesArray.swap(selMorpher, selMorpher-1);
						});
					}, {
						if(selMorpher==0, { selMorpher = 9 }, { selMorpher = selMorpher-1 });
						morphButtsArray[selMorpher].valueAction_(1);
						morphButtsArray[selMorpher].focus(true);
					});
					win.refresh;
					}
				{ 125 } {
					if(modifiers.isCmd, { // cmd arrow moves the ordering of filters
						if(selMorpher<10, {
							thisbuttstate = morphButtsArray[selMorpher].states;
							newbuttstate = morphButtsArray[selMorpher+1].states;
							morphButtsArray[selMorpher].states_(newbuttstate);
							morphButtsArray[selMorpher+1].states_(thisbuttstate);
							morpherArray.swap(selMorpher, selMorpher+1);
							morpherArray.do({arg morpher, ii;
								if(selMorpher == ii, { morpher.selectedmorpher = true }, { morpher.selectedmorpher = false });
							});
							synthNamesArray.swap(selMorpher, selMorpher+1);
						});
					}, {
						if(selMorpher==9, { selMorpher = 0 }, { selMorpher = selMorpher+1 });
						morphButtsArray[selMorpher].valueAction_(1);
						morphButtsArray[selMorpher].focus(true);
					});
					win.refresh;
				}
				{ 123 } { // left
					morphsParamsButtArray[selParameter].states_(
						[[morphsParamsButtArray[selParameter].states[0][0], Color.black, Color.clear]]);
					if(selParameter==0, {
						selParameter = controls.size-1;
					}, {
						selParameter = selParameter-1;
					});
					morphsParamsButtArray[selParameter].states_(
						[[morphsParamsButtArray[selParameter].states[0][0], Color.black, Color.green(alpha:0.3)]]);
					morphsParamsButtArray[selParameter].valueAction_(1);			win.refresh;
				}
				{ 124 } { // right
					morphsParamsButtArray[selParameter].states_(
						[[morphsParamsButtArray[selParameter].states[0][0], Color.black, Color.clear]]);
					if(selParameter>(controls.size-2), {
						selParameter = 0;
					}, {
						selParameter = selParameter+1;
					});
					morphsParamsButtArray[selParameter].states_(
						[[morphsParamsButtArray[selParameter].states[0][0], Color.black, Color.green(alpha:0.3)]]);
					morphsParamsButtArray[selParameter].valueAction_(1);
					win.refresh;
				}
				{ 49 } {
					playSound.value;
				};
			});
		
				selbPool = GUI.popUpMenu.new(win, Rect(10, 10, 100, 16))
					.font_(GUI.font.new("Helvetica", 9))
					.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
					.value_(0)
					.background_(Color.white)
					.action_({ arg item; var checkBufLoadTask;
						ldSndsGBufferList.value(selbPool.items[item.value]); // sending name of pool
						bufferPop.valueAction_(0);
					});
		
				bufferPop = GUI.popUpMenu.new(win, Rect(10, 32, 100, 16)) // 550
						.font_(GUI.font.new("Helvetica", 9))
						.items_(["no buffer"])
						.background_(Color.white)
						.action_({ arg popup; 
							var filepath, selNumFrames;
							if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
								selBuffer = XQ.globalBufferDict.at(poolName)[0][popup.value];
								if( selBuffer.isKindOf(Buffer), {
									filepath = XQ.globalBufferDict.at(poolName)[0][popup.value].path;
									selStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
									selNumFrames =  XQ.globalBufferDict.at(poolName)[1][popup.value][1];
									selEnd = selStart + selNumFrames;
									soundDur = (selNumFrames-selStart)/Server.default.sampleRate;
									
									soundfile = SoundFile.new;
									soundfile.openRead(filepath);
									sndfileview.soundfile_(soundfile);
									sndfileview.read(selStart, selNumFrames);
									sndfileview.elasticMode_(true);
									soundfile.close;
								});
							});
		 				})
		 				.keyDownAction_({arg view, char, modifiers, unicode, keycode;
		 					if(keycode == 49, {nil});
		 				});
						
				ldSndsGBufferList = {arg argPoolName, firstpool=false;
					poolName = argPoolName.asSymbol;
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
						sndNameList = [];
						XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
							sndNameList = sndNameList.add(buffer.path.basename);
						 });
						 bufferPop.items_(sndNameList);
						 // put the first file into the view and load buffer (if first time)
						 if(firstpool, {bufferPop.action.value(0)}); 
					}, {
						sndNameList = [];
					});
				};
				
				ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		
				synthesisStylePop = PopUpMenu.new(win, Rect(10, 54, 100, 16)) // 550
						.font_(GUI.font.new("Helvetica", 9))
						.canFocus_(false)
						.items_([\empty, \lpf, \bpf, \hpf, \rlpf, \rhpf, \resonant, \moogff, 
								"-effects menuline", \delay, \freeverb, 
								\distortion, \chorus, \bitcrusher, \pitchshifter, \panamp, \cyberpunk, 
								"-fft menuline", \magaboveFFT,
								\brickwallFFT, \rectcombFFT, \magsmearFFT])
						.background_(Color.white)
						.action_({ arg popup;
							var morpher, filtername;
							filtername = popup.items[popup.value];
							morpherArray.put(selMorpher, morpher = XiiMorpherFilter.new(filtername));
							#colors, controls = morpher.getColorsControls;
							morphButtsArray[selMorpher].states_([[filtername, Color.black, colors[0]]]);
							morphButtsArray[selMorpher].focus(true);
							
							morpherArray.do({arg morpher; morpher.selectedmorpher = false });
							morpherArray[selMorpher].selectedmorpher = true;
							selParameter = 0; // set params to the first param
							morpherArray[selMorpher].scalePointLists;
							envvalues = morpherArray[selMorpher].getEnvArrays;
		
							3.do({arg i; morphsParamsButtArray[i].states_([["none"]]).focus(false)}); // clear params
							controls.do({arg ctrl, i; morphsParamsButtArray[i].states_([[ctrl[0]]]) });
							morphsParamsButtArray[0].valueAction_(1).focus(true);
							
							// update the colors of the rangesliders
							morphRangesArray.do({arg rangesl, ix;
								rangesl.knobColor_(
									if(GUI.id == \cocoa, {
										HiliteGradient(Color.black, colors[ix+1], \h);
									}, {
										colors[ix+1];
									});
								);
							});
							morpherArray[selMorpher].getParamsHiLo.do({arg array, i;
								morphRangesArray[i].lo_(array[0]).hi_(array[1]);
							});
							synthNamesArray[selMorpher] = filtername;
							win.refresh;
						});
		
				morphButtsArray = Array.fill(10, {arg i;
					Button.new(win, Rect(10, 76+(i*15), 100, 14))
						.states_([["empty"]])
						.font_(Font.new("Helvetica", 9))
						.action_({
							selMorpher = i;
							#colors, controls = morpherArray[selMorpher].getColorsControls.copy;
							low = controls[0][1]; 
							high = controls[0][2];
							morpherArray.do({arg morpher, ii;
								if(selMorpher == ii, { morpher.selectedmorpher = true }, { morpher.selectedmorpher = false });
							});
							// update the params buttons
							controls.do({arg ctrl, iii; morphsParamsButtArray[iii].states_([[ctrl[0]]]) });
							morphsParamsButtArray[0].valueAction_(1).focus(true);
							// update the colors of the rangesliders
							morphRangesArray.do({arg rangesl, ix;
								rangesl.knobColor_(
									if(GUI.id == \cocoa, {
										HiliteGradient(Color.black, colors[ix+1], \h);
									}, {
										colors[ix+1];
									});
								)											});
							morpherArray[selMorpher].getParamsHiLo.do({arg array, i;
								morphRangesArray[i].lo_(array[0]).hi_(array[1]);
							});
							win.refresh;
						})
						.keyDownAction_({nil});
				});
				
				morphsParamsButtArray = Array.fill(3, {arg i;
					Button.new(win, Rect(10+(i*33), 230, 33, 16))
						.font_(Font.new("Helvetica", 9))
						.canFocus_(false)
						.states_([["params"]])
						.action_({arg view;
							morphsParamsButtArray.do({arg butt;
								butt.states_([[butt.states[0][0], Color.black, Color.clear]]);				
							});		
							morphButtsArray[selMorpher].focus(true);
							if(i<controls.size, {
								selParameter = i;
								morpherArray[selMorpher].selectedparam = i;
								// update strings
								if(controls.isNil.not, {
									paramsHiLo = morpherArray[selMorpher].getParamsHiLo;
									low = (controls[i][1]+(paramsHiLo[i][0]*controls[i][2])).round(0.001);
									high = (paramsHiLo[i][1]*controls[i][2]).round(0.001);
									lowtext.string_("low: " + low);
									hightext.string_("high: " + high);
									view.states_([[view.states[0][0], Color.black, Color.green(alpha:0.3)]]);
								});
							});
							win.refresh;
						}); 
				});
				
				morphRangesArray = Array.fill(3, {arg i;
					RangeSlider(win, Rect(14+(i*34), 250, 20, 80))
						.lo_(0)
						.hi_(1)
						.canFocus_(false)
						.knobColor_(
							if(GUI.id == \cocoa, {
								HiliteGradient(Color.black, Color.white, \h);
							}, {
								Color.white;
							});
						)
						.mouseDownAction_({
							selParameter = i;
							morphsParamsButtArray[selParameter].valueAction_(1);
						})	
					.action_({ arg slider;
						morphButtsArray[selMorpher].focus(true);
						selParameter = i;
						if(controls.isNil.not, {
							if(i<controls.size, {
								low = (controls[selParameter][1]+(slider.lo*controls[selParameter][2])).round(0.001);
								high = (slider.hi*controls[selParameter][2]).round(0.001);
								paramsHiLo[i] = [slider.lo, slider.hi];
								lowtext.string_("low: " + low);
								hightext.string_("high: " + high);
							});
						});
						win.refresh;
					})
					.mouseUpAction_({
						morpherArray[selMorpher].setParamsHiLo_(paramsHiLo.copy);
						morpherArray[selMorpher].scalePointLists;
					});
				});
		
				hightext = StaticText.new(win, Rect(10, 335, 80, 16))
							.font_(Font.new("Helvetica", 9))
							.string_("high:"); 
		
				lowtext = StaticText.new(win, Rect(10, 351, 80, 16))
							.font_(Font.new("Helvetica", 9))
							.string_("low:"); 
		
				actualtext = StaticText.new(win, Rect(10, 367, 80, 16))
							.font_(Font.new("Helvetica", 9))
							.string_("actual:"); 
		
				globalVolSl = OSCIISlider.new(win, Rect(10, 388, 100, 8), "- global vol", 0, 1, 1, 0.01, \amp)
								.font_(GUI.font.new("Helvetica", 9))						.action_({arg sl; 
									globalvol = sl.value;
									if(sourceplaying, {sourcesynth.set(\vol, globalvol) });
								});
		
				pitchSl = OSCIISlider.new(win, Rect(10, 416, 100, 8), "- pitch", 0.1, 2, 1, 0.01, \lin)
								.font_(GUI.font.new("Helvetica", 9))						.action_({arg sl; 
									pitch = sl.value;
									if(sourceplaying, {sourcesynth.set(\rate, pitch) });
								});
		
				StaticText.new(win, Rect(14, 446, 60, 20))
					.font_(GUI.font.new("Helvetica", 9))
					.string_("outbus :");
					
				outbuspop = PopUpMenu.new(win, Rect(60, 448, 50, 16))
					.font_(GUI.font.new("Helvetica", 9))
					.items_(XiiACDropDownChannels.getStereoChnList)
					.background_(Color.new255(255, 255, 255))
					.value_(params[0])
					.canFocus_(false)
					.action_({ arg popup;
						outbus = popup.value * 2;
						if(sourceplaying, {sourcesynth.set(\outbus, outbus) });
						params[0] = outbus;
					});
		
				Button.new(win, Rect(10, 472, 48, 16))
					.font_(Font.new("Helvetica", 9))
					.canFocus_(false)
					.states_([["bounce"]])
					.action_({arg view;
						var filepath, recbuffer;
						// check if the bounce folder exists
						if(File.exists("sounds/ixiquarks/SoundSculptor").not, {
							"mkdir sounds/ixiquarks/SoundSculptor".unixCmd;
						});
						filepath = "sounds/ixiquarks/SoundSculptor/SndSclpt_"++Date.localtime.stamp++".aif";
						recorder = XiiRecord(server, 0, selBuffer.numChannels, sampleFormat: XQ.pref.bitDepth);
						recorder.start( filepath );
						playSound.value;
						
						Routine({
							(selBuffer.duration+0.1).wait;
							recorder.stop;
							0.1.wait;
							XQ.globalWidgetList.do({arg widget; // search and see if a SoundSculptor Bufferpool exists
								if(widget.isKindOf(XiiBufferPool), {
									if(widget.name == "SoundSculptor", {bufferpool = widget})
								});  
							});
							if(bufferpool.isNil, {
								{bufferpool = XiiBufferPool.new(server, poolname:"SoundSculptor")}.defer; // change into server
								XQ.globalWidgetList.add(bufferpool); // add the pool to the registry 
							});
							bufferpool.loadBuffers([filepath]); // only one sound added, stick it in an array anyway
						}).play(AppClock);
					});
					
				startButt = Button.new(win, Rect(62, 472, 48, 16))
					.font_(Font.new("Helvetica", 9))
					.canFocus_(true)
					.states_([["start", Color.black, Color.clear], ["stop", Color.black, Color.green(alpha:0.3)]])
					.action_({arg view;
						if(view.value == 1, {
							playSound.value;
						}, {
							sourcesynth.free;
							actualsynthsarray.do({arg synth; synth.free });
							timeCursorTask.stop;
							timeCursorTask.reset;
						});
					});
		
				ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // load first pool
				
				playSound = {
					var morpherexistsFlag, sourceoutbus, actualmorpherarray;
					if(selBuffer.isNil.not, {
					// --- check if there is an effect, if not, routing out on 0
					morpherexistsFlag = false;
					actualsynthsarray = synthNamesArray.copy.removeEvery([\empty]);
					if(actualsynthsarray.size>0, { morpherexistsFlag = true });
					if(morpherexistsFlag, { sourceoutbus = 110}, { sourceoutbus = 0});
					
					actualmorpherarray = morpherArray.reject({arg morpher; morpher.filtertype==\empty});
					timeCursorTask.play;
					startButt.value_(1);
					sourceplaying = true;
					// update GUI after the playing of the sound
					AppClock.sched(soundDur*pitch.reciprocal, { 
						startButt.value_(0); 
						sourceplaying = false; 
					});
					// source sound
					if(selBuffer.numChannels == 1, {		
						sourcesynth = Synth(\xiiSamplePlayer1x2, 
												[\outbus, sourceoutbus,
												\bufnum, selBuffer.bufnum, 
												\rate, pitch, 
												\vol, globalvol,
												\startPos, selStart,
												\endPos, selEnd,
												\dur, soundDur*pitch.reciprocal], addAction:\addToHead);
					}, {
						sourcesynth = Synth(\xiiSamplePlayer2x2, 
												[\outbus, sourceoutbus, 
												\bufnum, selBuffer.bufnum, 
												\rate, pitch, 
												\vol, globalvol,
												\startPos, selStart,
												\endPos, selEnd,
												\dur, soundDur*pitch.reciprocal], addAction:\addToHead);
					});
					// effects synthdefs
					actualsynthsarray.collect({arg synthname, i;
						#colors, controls = actualmorpherarray[i].getColorsControls;
						envvalues = actualmorpherarray[i].getEnvArrays;
						if(envvalues.isNil.not, {
							switch(controls.size) 
							{ 1 } {
								Synth(("xii"++synthname++if(selBuffer.numChannels==1, {"_1x2"}, {"_2x2"})).asString, 
									[\inbus, 110+(i*selBuffer.numChannels),
									\outbus, if(i==(actualsynthsarray.size-1), { outbus }, {110+((i+1)*selBuffer.numChannels)}),
									\dur, soundDur*pitch.reciprocal, 
									controls[0][0], envvalues[0]
									], addAction:\addToTail);
								
							}
							{ 2 } {
								Synth(("xii"++synthname++if(selBuffer.numChannels==1, {"_1x2"}, {"_2x2"})).asString, 
									[\inbus, 110+(i*selBuffer.numChannels),
									\outbus, if(i==(actualsynthsarray.size-1), { outbus }, {110+((i+1)*selBuffer.numChannels)}),
									\dur, soundDur*pitch.reciprocal, 
									controls[0][0], envvalues[0], // freqs
									controls[1][0], envvalues[1]  // rqs
									], addAction:\addToTail);
								
							}
							{ 3 } {
								Synth(("xii"++synthname++if(selBuffer.numChannels==1, {"_1x2"}, {"_2x2"})).asString, 
									[\inbus, 110+(i*selBuffer.numChannels),
									\outbus, if(i==(actualsynthsarray.size-1), { outbus }, {110+((i+1)*selBuffer.numChannels)}),
									\dur, soundDur*pitch.reciprocal, 
									controls[0][0], envvalues[0],
									controls[1][0], envvalues[1],
									controls[2][0], envvalues[2]
									], addAction:\addToTail);
								
							}
						});
					});
					}, {
						startButt.value_(0);
						XiiAlertSheet(win, Rect(20, 20, 280, 120), "No buffer selected!"); 
					});
				};
				
				timeCursorTask = Task({ var pos, resetclock; // resetclock should be defined above and killed on win.close
					pos = 0;
					resetclock = SystemClock.sched(soundDur*pitch.reciprocal, { 
								pos = 0;
								{
								sndfileview.timeCursorPosition_(0);
								}.defer;
								timeCursorTask.reset;
								timeCursorTask.stop;
							nil});
					inf.do({
						{
						win.isClosed.not.if({ // if window is not closed, update...
							sndfileview.timeCursorPosition_(pos)
						})
						}.defer;
						pos = pos + ((Server.default.sampleRate/20)*pitch);
						0.05.wait;
					});
				});
				
		win.onClose_({
			var t;
			try{ actualsynthsarray.do({arg synth; synth.free})};
			try{ sourcesynth.free };
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
		^[2, point, params ];
	}	
	
}