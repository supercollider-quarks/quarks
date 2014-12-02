
XiiPolyMachine {	

	var <>xiigui, <>win, params;
	
	var bufferPopUpArray, ldSndsGBufferList, sndfiles, gBufferPoolNum, selbPool, bufNumArray;
	var stateDict, stateNum, poolName;
	var cmdPeriodFunc;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiPolyMachine(server, channels, setting);
		}
		
	initXiiPolyMachine {arg server, channel, setting;

		var stepsArray, tClock, globalvol;
		var indexBoxArray, selectBoxArray, clockArray, volArray;
		var startSequencer, startButt, volumeSlider, tempoSlider, outbusPoP;
		var meter, fixedBoxSize, outbus, tempo, synthGroup, inbusArray;
		var statesPop, storeButt, clearButt, clearSeqButt, loadArchive, saveArchive;
		var fixedRadioButt, drawBoxGrids, trackVolumeSlider;
		var envButtArray, envButtOnOffArray;
		var createCodeWin, codeArray, codeFlagArray, audioStreamFlagArray;
		var synthPrototypeArray;
		var createEnvWin, envArray, envFlagArray, createAudioStreamBusWin;
		var point, relativeTime, relativeTimeRadioButt;
		var tracks, lastStateNum;
		
		tracks = XQ.pref.polyMachineTracks; // 4 is default (XQ.pref is set in the preferences/preferences.ixi file)
		fixedBoxSize = true;
		meter = Array.fill(tracks, {16});
		bufNumArray = Array.fill(tracks, {0});
		volArray = Array.fill(tracks, {1});
		outbus = 0;
		inbusArray = Array.fill(tracks, {20}); // each channel can have dif inbus in audiostream
		tempo = 2;
		globalvol = 1;
		synthGroup = Group.new(server, \addToHead);
		stateDict = (); // dictionary of saved states
		codeArray = Array.fill(tracks, {()}); // a dictionary inside the array
		codeFlagArray = Array.fill(tracks, {true});
		audioStreamFlagArray = Array.fill(tracks, {false});
		envFlagArray = Array.fill(tracks, {true});
		envArray = Array.fill(tracks, {[[ 0.0, 1.0, 0.7, 0.7, 0.0], [0.05, 0.1, 0.5, 0.2], 1]}); // levels, times, duration
		relativeTime = false;
		
		synthPrototypeArray = Array.fill(tracks, "{
	var env, sine;
	env = EnvGen.ar(Env.perc, doneAction:2);
	sine = SinOsc.ar(440, 0, 0.5 * env);
	sine ! 2
}.play");


xiigui = nil;
point = if(setting.isNil, {Point(10, 200)}, {setting[1]});
params = if(setting.isNil, {[1, 0, stateDict, 0]}, {setting[2]});

stateDict = params[2];
stateNum = params[2].size; // number of states in the dictionary
lastStateNum = params[3];

		win = GUI.window.new("- polymachine -", Rect(point.x, point.y, 760, tracks*62), resizable:false);
		
		indexBoxArray = Array.fill(tracks, {arg i;
			BoxGrid.new(win, bounds: Rect(260, 10+(i*60), 16*30, 12), columns: 16, rows: 1)
				.setFillMode_(true)
				.setNodeBorder_(4)
				.setFillColor_(Color.white);
		});
		
		selectBoxArray = Array.fill(tracks, {arg i;
			BoxGrid.new(win, bounds: Rect(260, 30+(i*60), 16*30, 23), columns: 16, rows: 1)
				.setBackgrColor_(XiiColors.lightgreen)
				.setFillMode_(true)
				.setNodeBorder_(5)
				.setFillColor_(XiiColors.darkgreen)
				.nodeUpAction_({arg nodeloc; 
					codeArray[i].put(nodeloc[0].asInteger, synthPrototypeArray[i]);
					//[\codeArrayFIXED, codeArray[i]].postln;
				})
				.rightDownAction_({arg nodeloc; 
					selectBoxArray[i].setState_(nodeloc[0], 0, 1);
					createCodeWin.value(i, nodeloc[0]);
				});
		});
		
		drawBoxGrids = {arg numCol, i, sentFromSlider=false; var incr, tmparr;
			meter[i] = numCol;
			if(sentFromSlider.not, {stepsArray[i].value_(numCol)});// steps slider in right pos
			tmparr = selectBoxArray[i].getNodeStates[0];
			tmparr = tmparr++Array.fill((meter.size), {0});
			if(fixedBoxSize == false, {
				//fixedRadioButt.value_(0);
				indexBoxArray[i].remove; selectBoxArray[i].remove; // remove the views
				win.bounds_(Rect(win.bounds.left, win.bounds.top, 760, tracks*62));
				indexBoxArray[i]=BoxGrid.new(win, Rect(260, 10+(i*60), 16*30, 12), numCol,1)
						.setFillMode_(true)
						.setNodeBorder_(4)
						.setFillColor_(Color.white);
				selectBoxArray[i] = 
				BoxGrid.new(win, Rect(260, 30+(i*60), 16*30, 23), numCol, 1)
						.setBackgrColor_(XiiColors.lightgreen)
						.setFillMode_(true)
						.setNodeBorder_(5)
						.setFillColor_(XiiColors.darkgreen)
						.setNodeStates_([tmparr])
						.nodeUpAction_({arg nodeloc; 
							codeArray[i].put(nodeloc[0].asInteger, synthPrototypeArray[i]);
							//[\codeArrayNOTFIXED, codeArray[i]].postln;
						})
						.rightDownAction_({arg nodeloc; 
							selectBoxArray[i].setState_(nodeloc[0], 0, 1);
							createCodeWin.value(i, nodeloc[0]);
						});
			}, {
				//fixedRadioButt.value_(1);
				indexBoxArray[i].remove; selectBoxArray[i].remove;
				incr = meter.copy.sort.top - 16;
				win.bounds_(Rect(win.bounds.left, win.bounds.top, 760+(incr*30), tracks*62));
				indexBoxArray[i] = 
				BoxGrid.new(win, Rect(260, 10+(i*60), numCol*30, 12), numCol, 1)
						.setFillMode_(true)
						.setNodeBorder_(4)
						.setFillColor_(Color.white);
				selectBoxArray[i] = 
				BoxGrid.new(win, Rect(260, 30+(i*60), numCol*30, 23), numCol, 1)
						.setBackgrColor_(XiiColors.lightgreen)
						.setFillMode_(true)
						.setNodeBorder_(5)
						.setFillColor_(XiiColors.darkgreen)
						.setNodeStates_([tmparr])
						.nodeUpAction_({arg nodeloc; 
							codeArray[i].put(nodeloc[0].asInteger, synthPrototypeArray[i]);
							//[\codeArrayFIXED, codeArray[i]].postln;
						})
						.rightDownAction_({arg nodeloc; 
							selectBoxArray[i].setState_(nodeloc[0], 0, 1);
							createCodeWin.value(i, nodeloc[0]);
						});
			});
			win.refresh;
		};

		stepsArray = Array.fill(tracks, {arg i;
			OSCIISlider(win, Rect(130, 10+(i*60), 60, 8), "- steps", 2, 48, 16, 1)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({ arg sl; var state;
					drawBoxGrids.value(sl.value, i, true);
				});
		});
		
		envButtArray = Array.fill(tracks, {arg i;
			GUI.button.new(win, Rect(196, 10+(i*60) , 15, 16))
				.states_([["e", Color.black, Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.action_({
					createEnvWin.value(i);							});
		});

		envButtOnOffArray = Array.fill(tracks, {arg i;
			GUI.button.new(win, Rect(214, 10+(i*60) , 15, 16))
				.states_([["x", Color.black, Color.clear], ["o", Color.black, XiiColors.darkgreen]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.value_(1)
				.action_({ arg butt;
					if(butt.value == 1, {
						envFlagArray[i] = true;
					}, {
						envFlagArray[i] = false;
					});
				});
		});
		
		selbPool = GUI.popUpMenu.new(win, Rect(10, 10, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item;
				poolName = selbPool.items[item.value];
				ldSndsGBufferList.value(poolName);
			});
			
		ldSndsGBufferList = {arg argPoolName, firstpool=false; var numOfBuffers;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndfiles = Array.fill(XQ.globalBufferDict.at(poolName)[0].size, { arg i;
							XQ.globalBufferDict.at(poolName)[0][i].path.basename});
				sndfiles = sndfiles ++ ["scode", "audiostream"];
				numOfBuffers = XQ.globalBufferDict.at(poolName)[0].size;
				
				bufferPopUpArray.do({arg popup, i; 
					popup.items_(sndfiles); 
					popup.value_(i%sndfiles.size); // if no soundfiles, then code
					bufNumArray[i] = i%sndfiles.size; // if there are fewer than 4 soundfiles
					if(firstpool, {popup.action.value(i%sndfiles.size)});
				});

				if(numOfBuffers < tracks, {
					numOfBuffers.do({|i| bufNumArray[i] = i; codeFlagArray[i] = false;});
				}, {
					codeFlagArray = Array.fill(tracks, {false});
					bufNumArray = Array.fill(tracks, {arg i; i});
				});
			}, {
				sndfiles = ["scode", "audiostream"]; // sc code
			});
		};
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);
		
		bufferPopUpArray = Array.fill(tracks, {arg i;
			GUI.popUpMenu.new(win, Rect(130, 37+(i*60) , 100, 18))
				.items_(sndfiles)
				.value_(if(sndfiles.size==2, {0}, {i%sndfiles.size}))
				.font_(GUI.font.new("Helvetica", 9))
				.background_(Color.white)
				.action_({ arg sf;
					if(sndfiles[sf.value] == "scode", {
						audioStreamFlagArray[i] = false; // 
						codeFlagArray[i] = true; // modulo code checks if it's 1/0
					}, {			
						if(sndfiles[sf.value] == "audiostream", {
							audioStreamFlagArray[i] = true; // 
							codeFlagArray[i] = false;
							createAudioStreamBusWin.value(i);
						},{ // playing soundfiles
							audioStreamFlagArray[i] = false; // 
							codeFlagArray[i] = false;
							bufNumArray[i] = sf.value;
						});
					});
				});
		});
		
		trackVolumeSlider = Array.fill(tracks, {arg i;
			GUI.multiSliderView.new(win, Rect(240, 10+(i*60), 12, 45))
				.value_([0.9])		
				.isFilled_(true)
				.canFocus_(false)
				.indexThumbSize_(9)
				.background_(Color.new255(155, 205, 155))
				.fillColor_(Color.new255(103, 148, 103))
				.strokeColor_(Color.new255(103, 148, 103))
				.valueThumbSize_(1)
				.action_({arg xb; 
					volArray[i] = xb.value[0];
				});		
		});
				
		
		statesPop = GUI.popUpMenu.new(win, Rect(10, 32, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(params[2].keys.asArray.sort)
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var chosenstate;
				if(stateNum > 0, { // if there are any states
				//stateDict.at(("state 1").asSymbol).postln;
				chosenstate = stateDict.at(("state "++(item.value+1).asString).asSymbol);
				//[\chosenstate, chosenstate].postln;
				
				fixedBoxSize = chosenstate[tracks].copy;
				volArray = chosenstate[tracks+1].copy;
				tempo = chosenstate[tracks+2].copy;
				codeArray = chosenstate[tracks+3].copy; 
				envArray = chosenstate[tracks+4].copy;
				envFlagArray = chosenstate[tracks+5].copy;
				poolName = chosenstate[tracks+6].copy;
				bufNumArray = chosenstate[tracks+7].copy;
				//[\bufNumArray, bufNumArray].postln;
				//[\poolName, poolName].postln;
			
				clockArray.do({arg clock; clock.tempo_(tempo)});
				envButtOnOffArray.do({arg butt, i; 
					if(chosenstate[9][i] == true, {butt.value_(1)}, 
					{butt.value_(0)}) });
				tracks.do({arg i; drawBoxGrids.value(chosenstate[i].size, i); });
				selectBoxArray.do({arg boxgrid, i; boxgrid.setNodeStates_([chosenstate[i]])});
				trackVolumeSlider.do({arg sl, i; sl.value_([volArray[i]]); });
				selbPool.value_(selbPool.items.indexOfEqual(poolName));
				//ldSndsGBufferList.value(poolName);
				bufferPopUpArray.do({arg popup, i; popup.value_(bufNumArray[i])});
				tempoSlider.value_(tempo*60);
				lastStateNum = item.value; params[3] = lastStateNum;
				});
			});
			
		clearButt = GUI.button.new(win, Rect(10, 57, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				stateNum = 0;
				stateDict = ();
				statesPop.items_(["states"]);
			});

		storeButt = GUI.button.new(win, Rect(60, 57, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["store", Color.black, Color.clear]])
			.action_({arg butt; var statesarray;
				stateNum = stateNum + 1;
				statesPop.items_(Array.fill(stateNum, {|i| "state "++(i+1).asString}));
				statesPop.value_(stateNum-1);
				statesarray = Array.fill(tracks, {|i| selectBoxArray[i].getNodeStates[0]});
				statesarray = statesarray.add(fixedBoxSize.copy); // fixedBoxSize flag - index slot 4
				statesarray = statesarray.add(volArray.copy);     // volume array      - index slot 5
				statesarray = statesarray.add(tempo.copy);        // tempo             - index slot 6
				statesarray = statesarray.add(codeArray.copy);    // code array        - index slot 7
				statesarray = statesarray.add(envArray.copy);     // env array         - index slot 8
				statesarray = statesarray.add(envFlagArray.copy); // on/off states     - index slot 9
				statesarray = statesarray.add(poolName.copy);     // poolName          - index slot 10
				statesarray = statesarray.add(bufNumArray.copy);  // bufNumArray       - index slot 11
				//statesarray = statesarray.add(lastStateNum.copy); // laststateNum      - index slot 12
				//[\bufnumarray, bufNumArray].postln;
				stateDict.add(("state "++stateNum.asString).asSymbol -> statesarray);
				params[2] = stateDict;
				// get to the right state when loading up a setting (-1 because it's used for a menu)
				lastStateNum = stateNum-1; params[3] = lastStateNum; 				//[\statesarray, statesarray].postln;
			});

		clearSeqButt = GUI.button.new(win, Rect(10, 82, 97, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["clear sequencer", Color.black, Color.clear]])
			.action_({arg butt;
				selectBoxArray.do({arg boxgrid; boxgrid.clearGrid});
			});

		loadArchive = GUI.button.new(win, Rect(10, 107, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["load", Color.black, Color.clear]])
			.action_({arg butt;
				GUI.dialog.getPaths({ arg paths; var chosenstate;
					paths.do({ arg p;
						stateDict = Object.readArchive(p);
					});
					stateNum = stateDict.size;
					if(stateNum > 0, { // if there are any states
					statesPop.items_(Array.fill(stateNum, {|i| "state "++(i+1).asString}));
					chosenstate = stateDict.at("state 1".asSymbol);
					fixedBoxSize = chosenstate[tracks].copy;
					volArray = chosenstate[tracks+1].copy; 
					tempo = chosenstate[tracks+2].copy;
					codeArray = chosenstate[tracks+3].copy;
					envArray = chosenstate[tracks+4].copy;
					envFlagArray = chosenstate[tracks+5].copy;
					clockArray.do({arg clock; clock.tempo_(tempo)});
					tracks.do({arg i; drawBoxGrids.value(chosenstate[i].size, i);});
					selectBoxArray.do({arg boxgrid, i; boxgrid.setNodeStates_([chosenstate[i]])});
					trackVolumeSlider.do({arg sl, i; sl.value_([volArray[i]]); });
					tempoSlider.value_(tempo*60);
					});
				},{
					"cancelled".postln;
				});
			});

		saveArchive = GUI.button.new(win, Rect(60, 107, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["save", Color.black, Color.clear]])
			.action_({arg butt;
				GUI.dialog.savePanel({ arg path;
					stateDict.writeArchive(path++".xpm");
				},{
					"cancelled".postln;
				});
			});
			
		fixedRadioButt = OSCIIRadioButton(win, Rect(10, 135, 12, 12), "fix size")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(1)
				.action_({arg butt; var boxarray;
					boxarray = selectBoxArray.copy;
					if(butt.value == 1, {
						fixedBoxSize = true;
						tracks.do({arg i; drawBoxGrids.value(boxarray[i].getNodeStates[0].size, i)});
					},{
						fixedBoxSize = false;
						tracks.do({arg i; drawBoxGrids.value(boxarray[i].getNodeStates[0].size, i)});
					});
				});
				
		relativeTimeRadioButt = OSCIIRadioButton(win, Rect(64, 135, 12, 12), "rel time")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(0)
				.action_({arg butt; var boxarray;
					if(butt.value == 1, {
						relativeTime = true;
						startButt.valueAction_(0);
					},{
						relativeTime = false;
						startButt.valueAction_(0);
					});
				});

		tempoSlider = OSCIISlider.new(win, Rect(10, 157, 100, 8), "- tempo", 60, 480, 120, 1)
						.font_(GUI.font.new("Helvetica", 9))						.action_({arg sl; 
							tempo = sl.value/60;
							clockArray.do({arg clock, i; var temp;
								try({
									if(relativeTime, {
										temp = (tempo * meter[i])/meter.mean;
										clock.tempo_(temp)
									}, {
										clock.tempo_(tempo)
									});
								})
							});
						});
		
		volumeSlider = OSCIISlider.new(win, Rect(10, 187, 100, 8), "- vol", 0, 1.0, 1, 0.01, \amp)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg sl; 
							globalvol = sl.value;
							params[0] = sl.value;
						});
						
		outbusPoP = GUI.popUpMenu.new(win, Rect(10, 218, 50, 16))			.font_(GUI.font.new("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(0)
			.background_(Color.white)
			.action_({ arg ch;
				outbus = ch.value * 2;
				params[1] = ch.value;
			});

		startButt = GUI.button.new(win, Rect(65, 217, 45, 18))
			.states_([["start", Color.black, Color.clear],
					["stop", Color.black, XiiColors.onbutton]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt;
				if(butt.value == 1, {
					startSequencer.value;
				},{
					clockArray.do(_.stop);
					synthGroup.free;
					synthGroup = Group.new;
				});
			});

		startSequencer = {
			synthGroup = Group.new;
			clockArray = Array.fill(tracks, {arg i; var tClock;
				if(relativeTime, {
					tClock = TempoClock( (tempo * meter[i])/meter.mean ); // as time is relative, I get means for means time
				},{
					tClock = TempoClock( tempo );
				});
				tClock.schedAbs(tClock.beats.ceil, { arg beat, sec; var buffer, start, end, code, times, tempsynthdef;
					if(selectBoxArray[i].getState(beat%meter[i], 0) == 1, {
						if(codeFlagArray[i] == true, { // sample or code?
							if(codeArray[i].at((beat%meter[i]).abs.asInteger) != nil, {
								code = codeArray[i].at((beat%meter[i]).abs.asInteger);
								if(code[code.size-4..code.size] == "play", {
									tempsynthdef = (code++"(outbus:"+outbus+")").interpret;
									{server.sendMsg(\d_free, tempsynthdef.defName);}.defer(0.1); // free temp synthDef
								}, {
									code.interpret.value; // if it's not synth code
								});
							});
						},{ // code
						
							if(audioStreamFlagArray[i] == false, { // if playing buffers
							
								if(try{XQ.globalBufferDict.at(poolName)[0][bufNumArray[i]]} != nil, {
									buffer = XQ.globalBufferDict.at(poolName)[0].wrapAt(bufNumArray[i]);
									start = XQ.globalBufferDict.at(poolName)[1].wrapAt(bufNumArray[i])[0];
									end = start + XQ.globalBufferDict.at(poolName)[1].wrapAt(bufNumArray[i])[1];
									
									if(envFlagArray[i] == true, { // envelope on
										times = envArray[i][1]*envArray[i][2];
										if(buffer.numChannels == 1, {
											Synth(\xiiPolyrhythm1x2Env, 
												[\outbus, outbus,
												\bufnum, buffer.bufnum, 
												\vol, volArray[i] * globalvol,
												\startPos, start,
												\endPos, end
												], synthGroup, \addToHead).setn( 
												\levels, envArray[i][0], \times, times);
										},{
											Synth(\xiiPolyrhythm2x2Env, 
												[\outbus, outbus,
												\bufnum, buffer.bufnum, 
												\vol, volArray[i] * globalvol,
												\startPos, start,
												\endPos, end
												], synthGroup, \addToHead).setn(
												\levels, envArray[i][0], \times, times);
										});
									}, { // envelope off
										if(buffer.numChannels == 1, {
											Synth(\xiiPolyrhythm1x2, 
												[\outbus, outbus,
												\bufnum, buffer.bufnum, 
												\vol, volArray[i] * globalvol,
												\startPos, start,
												\endPos, end
												], synthGroup, \addToHead);
										},{
											Synth(\xiiPolyrhythm2x2, 
												[\outbus, outbus,
												\bufnum, buffer.bufnum, 
												\vol, volArray[i] * globalvol,
												\startPos, start,
												\endPos, end
												], synthGroup, \addToHead);
										});
									});
								});
							},{ // if audiostream
								Synth(\xiiPolyrhythmAudioStream2x2Env, 
									[
									\vol, volArray[i] * globalvol,
									\inbus, inbusArray[i], 
									\outbus, outbus], 
									synthGroup, \addToHead).setn(
									\levels, envArray[i][0], \times, envArray[i][1]*envArray[i][2]);
							});
						});
					});
					{indexBoxArray[i].setState_( beat%meter[i], 0, 1 )}.defer;
					{indexBoxArray[i].setState_( beat%meter[i], 0, 0 )}.defer(tClock.beatDur);
					1;
				});
			});
		};
		
		createCodeWin = {arg dictIndex, slot;
				var funcwin, func, subm, test, view;
				funcwin = GUI.window.new("scode", Rect(600, 400, 400, 200)).front;
				view = funcwin.view;
				func = GUI.textView.new(view, Rect(20, 20, 360, 120))
						.font_(GUI.font.new("Monaco", 9))
						.resize_(5)
						.string_(
							if(codeArray[dictIndex][slot.asInteger]==nil, {
								synthPrototypeArray[dictIndex];
							}, {
								codeArray[dictIndex][slot.asInteger].asString;
							});
						);
				test = GUI.button.new(view, Rect(270,150,50,18))
						.states_([["test",Color.black,Color.clear]])
						.resize_(9)
						.font_(GUI.font.new("Helvetica", 9))
						.action_({
							func.string.interpret.value;
						});
						
				subm = GUI.button.new(view, Rect(330,150,50,18))
						.states_([["submit",Color.black,Color.clear]])
						.resize_(9)
						.focus(true)
						.font_(GUI.font.new("Helvetica", 9))
						.action_({
							codeArray[dictIndex].put(slot.asInteger, func.string);
							synthPrototypeArray[dictIndex] = func.string;
							funcwin.close;
						});
		};

		createEnvWin = {arg index;
			var win, envview, timesl, setButt, timeScale;
			timeScale = 1.0;
			win = GUI.window.new("asdr envelope", Rect(200, 450, 250, 130), resizable:false).front;
			win.alwaysOnTop = true;
			
			envview = GUI.envelopeView.new(win, Rect(10, 5, 230, 80))
				.drawLines_(true)
				.selectionColor_(Color.red)
				.canFocus_(false)
				.drawRects_(true)
				.background_(XiiColors.lightgreen)
				.fillColor_(XiiColors.darkgreen)
				.action_({arg b;})
				.thumbSize_(5)
				.env2viewFormat_(Env.new(envArray[index][0], envArray[index][1]))
				.setEditable(0, false);

			timesl = OSCIISlider.new(win, 
						Rect(10, 100, 130, 8), "- duration", 0.1, 10, envArray[index][2], 0.01)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({arg sl; });
			
			setButt = GUI.button.new(win, Rect(160, 100, 60, 16))
					.states_([["set envelope", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						envArray[index] = envview.view2envFormat ++ timesl.value; // levels, times, duration
						win.close;
					});
		};

		createAudioStreamBusWin = {arg index;
			var win, envview, timesl, setButt;
			win = GUI.window.new("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
			win.alwaysOnTop = true;
			
//			SCStaticText(win, Rect(20, 15, 220, 16))
//				.font_(GUI.font.new("Helvetica", 9))
//				.string_("NOTE: you might have to restart Polymachine"); 
//			SCStaticText(win, Rect(20, 30, 220, 16))
//				.font_(GUI.font.new("Helvetica", 9))
//				.string_("in order to receive stream from a bus"); 
				
			GUI.staticText.new(win, Rect(20, 55, 20, 16))
				.font_(GUI.font.new("Helvetica", 9)).string_("in"); 

			GUI.popUpMenu.new(win, Rect(35, 55, 50, 16))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(10)
				.font_(GUI.font.new("Helvetica", 9))
				.background_(Color.white)
				.canFocus_(false)
				.action_({ arg ch;
					inbusArray[index] = ch.value * 2;
				});

			setButt = GUI.button.new(win, Rect(120, 55, 60, 16))
					.states_([["set inbus", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						win.close;
					});
		};

		cmdPeriodFunc = { startButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);
		
		win.front;
		win.onClose_({
			var t;
			clockArray.do(_.stop);
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		});

		// setting		
		volumeSlider.valueAction_(params[0]);
		outbusPoP.valueAction_(params[1]);
		statesPop.valueAction_(params[3]);
		
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
		if(poolindex != nil, { // not first time pool is loaded
			selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
			ldSndsGBufferList.value(poolname);
		}, {
			selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // load first pool
		});
	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}