
XiiSoundScratcher {

	var <>xiigui, <>win, params;

	var moveAction;

	var bounds;
	var bgcolor, fillmode, pointcolor, linecolor, strokecolor, pointsize, pointsizelist;
	var state;
	var pointlist, lastpoint;
	var recPathList;
	var tabletView, soundfile, sndfileview, eraze;
	var selbPool, bufferPop, ldSndsGBufferList;
	var sndNameList, gBufferPoolNum, bufferList;
	var drawLineFlag, myBuffer, synth, myTempBuffer;
	var s, wacomRButt, mouseRButt;
	var randomGrainTask, linearGrainTask, wormGrainTask, graindensity, grainDensitySl;
	var grainEnvType, graindur, grainDurSl;
	var grainFlag, playPathFlag, synthType;
	var wanderCircleRadius; // for the wormGrainTask
	var tempPressureList, startPoint, endPoint, grainCircles, tempRect;
	var circleList, grainCircleSynthList, circleGroup;
	var wacomFlag, poolName;
	
	*new { arg server, channels, setting = nil; 
		if ( GUI.id==\cocoa, {
			^super.new.initXiiSoundScratcher(server, channels, setting);
		},{
			^super.new.initXiiSoundScratcherSwing(server, channels, setting);
		});
	}
	
	initXiiSoundScratcher { arg server, channels, setting;

		var foreColor, bgColor, synthesisStylePop, drawFlag;
		var sineEnvRButt, percEnvRButt;
		var scrambleGrainListButt, saveGrainListButt, grainStatesPop, grainList;
		var clearScreenButt, drawRButt, globalvol, globalVolSl, outbus, outBusPop;
		var recordPathButt, playPathTask, recordPathTask, recPathFlag, addSilencePointTask;
		var pointp, gPressure, rateMultiplier;
		var point;
		var verticalFlag, horizontalFlag, mouseLoc;
	
		s = server; //Server.default;
		bounds = Rect(120, 5, 800, 340);

xiigui = nil;
point = if(setting.isNil, {Point(310, 250)}, {setting[1]});
params = if(setting.isNil, {[0, 1, 0.2, 25, 1, 0, 0, 1, 1.0, 0]}, {setting[2]});

		win = GUI.window.new("- soundscratcher -", 
				Rect(point.x, point.y, bounds.width+20, bounds.height+10), resizable:false);
		pointcolor = Color.new255(200,50,40);
		strokecolor = Color.black;
		pointsize = 6;
		linecolor = Color.new255(40,240,40);
		drawLineFlag = true; // false means granular synthesis - true scratching
		recPathFlag = false;
		recPathList = List.new;
		gPressure = 0; // only playing back in PlayBackTask if pressure is > 0;
		pointp = Point(200,200);
		synthType = \warp;
		verticalFlag = false;
		horizontalFlag = false;
		mouseLoc = [100,100];
		
		globalvol = 1;
		outbus = 0;
		
		foreColor = XiiColors.darkgreen;
		bgColor = XiiColors.lightgreen;

		gBufferPoolNum = 0;
		sndNameList = List.new;
		bufferList = List.new; // contains bufnums of buffers (not buffers)
		drawFlag = true;
		graindensity = 0.1;
		grainEnvType = 0; // hanning envelope
		graindur = 0.05;
		grainList = List.new;  
		grainFlag = false;
		
		playPathFlag = false;
		
		pointlist = List.new;
		pointsizelist = List.new;
		grainCircles = false;
		circleList = List.new;
		tempPressureList = List.new;
		tempRect = Rect(0,0,0,0);
		grainCircleSynthList = List.new;
		circleGroup = Group.new;
		wacomFlag = false;
		rateMultiplier = 1; // used in grainCirles when one wants faster grains in individual synths (keys 1-4)
		
		soundfile = SoundFile.new;
		soundfile.openRead("sounds/a11wlk01.wav");

		sndfileview = SoundFileView.new(win, Rect(120, 5, bounds.width-120, bounds.height-10))
			.soundfile_(soundfile)
			.read(0, soundfile.numFrames)
			.elasticMode_(true)
			.timeCursorOn_(false)
			.timeCursorColor_(Color.white)
			.drawsWaveForm_(true)
			.gridOn_(false)
			.waveColors_([ foreColor, foreColor ])
			.background_(bgColor)
			.canFocus_(false)
			.setSelectionColor(0, Color.new255(105, 185, 125));
		
		soundfile.close;

		if ( GUI.id == \cocoa, 
			{ tabletView = TabletView.new(win, Rect(120, 5, bounds.width-120, bounds.height-10))},
			{ tabletView = UserView.new(win, Rect(120, 5, bounds.width-120, bounds.height-10)) }
		);
		//tabletView = GUI.tabletView.new(win, Rect(120, 5, bounds.width-120, bounds.height-10))
		tabletView
			.canFocus_(false)
			.mouseDownAction_({arg view, x, y, pressure=1;
				//gPressure = pressure; // TESTING
				mouseLoc = [x, y];
				gPressure = if(wacomFlag, {pressure}, {0.5}); // fixing pointsize mouse/wacom
				if( playPathFlag == true, {playPathTask.stop; recordPathButt.valueAction_(0)});
				
				startPoint = Point(x,y);
				if(grainCircles, {// GRAIN CIRCLES
					tempPressureList = List.new;
				}, {
					if(drawLineFlag && grainFlag.not && recPathFlag.not, { this.clear }); // view
					pointp = Point((x+120).round, (y+5).round);
					pointlist.add(pointp);
					pointsizelist.add(gPressure); 
					this.refresh;
					if(myTempBuffer.notNil, {
						switch(synthesisStylePop.value,
							0, { // WARP
							//[\graindensity, graindensity].postln;
							synth = Synth(\xiiWarp, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus, \pointer, x/680, 
							\freq, 1.5-(y/320), \trate, graindensity.reciprocal, \dur, graindur, \vol, globalvol]);
							},
							1, { // SCRATCH
							synth = Synth(\xiiScratch1x2, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus,
							\pos, (x/680)*myBuffer.numFrames, \vol, globalvol]);
						});
					})
				});
			})
			.mouseUpAction_({ arg  view,x,y,pressure=1;
				var meanpressure;
				if(grainCircles, {
					endPoint = Point(x,y);
					tempRect = Rect(0,0,0,0);
					meanpressure = tempPressureList.sum/tempPressureList.size;

					if(startPoint == endPoint, { endPoint = startPoint+2; meanpressure = 0.5;});
					circleList.add([Rect(
										tabletView.bounds.left+endPoint.x.min(startPoint.x)+0.5, 
										tabletView.bounds.top+endPoint.y.min(startPoint.y)+0.5, 
										(endPoint.x - startPoint.x).abs,
										(endPoint.y - startPoint.y).abs
										), 
										meanpressure,
										rateMultiplier] // if speed is increased
								);
					if(myTempBuffer.notNil, {
						if(synthesisStylePop.value == 5, { // grainCircles
							grainCircleSynthList.add(
								Synth(\xiiGrains, [
								\bufnum, myTempBuffer.bufnum, 
								\outbus, outbus,
								\dur, graindur,
								\trate, graindensity.reciprocal * rateMultiplier,
								\left, ((endPoint.x.min(startPoint.x)/680)*myTempBuffer.numFrames)/s.sampleRate,
								\right, ((endPoint.x.max(startPoint.x)/680)*myTempBuffer.numFrames)/s.sampleRate,
								\ratelow, ((endPoint.y.max(startPoint.y)/340) - 1).abs+0.5, // 680
								\ratehigh, ((endPoint.y.min(startPoint.y)/340) - 1).abs+0.5,
								\vol, meanpressure,
								\globalvol, globalvol
								], circleGroup,\addToHead);
							);
						}, {  // grainSquares
							grainCircleSynthList.add(
								Synth(\xiiGrainsSQ, [
								\bufnum, myTempBuffer.bufnum, 
								\outbus, outbus,
								\dur, graindur,
								\trate, graindensity.reciprocal * rateMultiplier,
								\left, ((endPoint.x.min(startPoint.x)/680)*myTempBuffer.numFrames)/s.sampleRate,
								\rate, (((endPoint.y.max(startPoint.y)/340) - 1).abs+0.5 +
								 ((endPoint.y.min(startPoint.y)/340) - 1).abs+0.5) / 2,
								\vol, meanpressure,
								\globalvol, globalvol
								], circleGroup,\addToHead);
							);
						});
					});
					this.refresh;
				}, {
					if(synthesisStylePop.value != 4, { 
						gPressure = 0;
						//pointlist.add(pointp);
						//pointsizelist.add(pressure);
						this.refresh;
						if(myTempBuffer.notNil, {
							if(grainFlag == false, { synth.set(\gate,0)});
						});
					});
				});
			});
			
			/*.action_({ arg  view,x,y,pressure=1;
				gPressure = pressure;
				mouseLoc = [x, y];
				if(grainCircles, {  // GRAIN CIRCLES
					tempPressureList.add(gPressure);
					endPoint = Point(x,y);
					tempRect = Rect(
						tabletView.bounds.left+endPoint.x.min(startPoint.x), 
						tabletView.bounds.top+endPoint.y.min(startPoint.y), 
						(endPoint.x - startPoint.x).abs,
						(endPoint.y - startPoint.y).abs
						);
					this.refresh;
				}, {
					pointp = Point((x+120).round, (y+5).round);
					if(bounds.containsPoint(pointp), {
						// keypressed v or h
						if(verticalFlag, {
							pointlist.add(pointp=Point((startPoint.x+120).round, pointp.y));
						}, {
							if(horizontalFlag, {
								pointlist.add(pointp=Point(pointp.x, startPoint.y));
							}, {
								pointlist.add(pointp);
							});
						});
						pointsizelist.add(if(wacomFlag, {pressure}, {0.5}));
						this.refresh;
					});
					if(myTempBuffer.notNil, {
						if(grainFlag == false, {
						synth.set(\vol, [0,1,\amp, 0.00001].asSpec.map(pressure)*globalvol);
						synth.set(\freq, 1.5-(pointp.y/320));
						synth.set(\pointer, ((x-6)/680)*(myTempBuffer.numFrames/s.sampleRate)); // for warp synth (if using TGrains (which is better))
						// synth.set(\pointer, (x-6)/680); // for warp synth (if using Warp1)
						synth.set(\pos, ((x-6)/680) * myTempBuffer.numFrames);// scratch synth
						});
					});
				});
			})*/
			moveAction = { arg  view,x,y,pressure=1;
				gPressure = pressure;
				mouseLoc = [x, y];
				if(grainCircles, {  // GRAIN CIRCLES
					tempPressureList.add(gPressure);
					endPoint = Point(x,y);
					tempRect = Rect(
						tabletView.bounds.left+endPoint.x.min(startPoint.x), 
						tabletView.bounds.top+endPoint.y.min(startPoint.y), 
						(endPoint.x - startPoint.x).abs,
						(endPoint.y - startPoint.y).abs
						);
					this.refresh;
				}, {
					pointp = Point((x+120).round, (y+5).round);
					if(bounds.containsPoint(pointp), {
						// keypressed v or h
						if(verticalFlag, {
							pointlist.add(pointp=Point((startPoint.x+120).round, pointp.y));
						}, {
							if(horizontalFlag, {
								pointlist.add(pointp=Point(pointp.x, startPoint.y));
							}, {
								pointlist.add(pointp);
							});
						});
						pointsizelist.add(if(wacomFlag, {pressure}, {0.5}));
						this.refresh;
					});
					if(myTempBuffer.notNil, {
						if(grainFlag == false, {
						synth.set(\vol, [0,1,\amp, 0.00001].asSpec.map(pressure)*globalvol);
						synth.set(\freq, 1.5-(pointp.y/320));
						synth.set(\pointer, ((x-6)/680)*(myTempBuffer.numFrames/s.sampleRate)); // for warp synth (if using TGrains (which is better))
						// synth.set(\pointer, (x-6)/680); // for warp synth (if using Warp1)
						synth.set(\pos, ((x-6)/680) * myTempBuffer.numFrames);// scratch synth
						});
					});
				});
			};

	if ( GUI.id == \cocoa, 
		{ tabletView.action_(moveAction) },
		{ tabletView.mouseMoveAction_(moveAction) }
	);





						
		win.view.keyDownAction_({arg this, char, modifiers, unicode; 
			var point, zvol, a;
			if(synthesisStylePop.value == 7, { // keyboard grains mode
				if(pointlist == List[], {
					point = Point(122+(680.rand), 320.rand);
					zvol = 1;
				}, {
					point = pointlist[a = pointlist.size.rand];
					zvol = if(wacomFlag, {pointsizelist[a]}, {1}); // full vol on mouse
				});
				if(myTempBuffer.isNil.not, {
				Synth.grain(\xiiGrain, 
					[\bufnum, myTempBuffer.bufnum,
					\outbus, outbus,
					\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
					\dur, graindur,
					\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
					\rate, 1.5-(point.y/320),
					\envType, grainEnvType
				]);
				});
			}, { // all other modes
			if(char.asString == "c", {
				if(synthesisStylePop.value == 4, {this.clear(true)}, {this.clear(false)});
				if(grainCircles, {	
					circleGroup.freeAll; 
					circleList = List.new;
					circleGroup = Group.new;
					this.refresh;
				});
			});
			if((synthesisStylePop.value == 5) || (synthesisStylePop.value == 6), { // grainCirles
				if(char.asString == "1", { rateMultiplier = 1 });
				if(char.asString == "2", { rateMultiplier = 2 });
				if(char.asString == "3", { rateMultiplier = 3 });
				if(char.asString == "4", { rateMultiplier = 4 });
			});

			if(char.asString == "v", { if(verticalFlag.not, { startPoint = Point(mouseLoc[0],mouseLoc[1]) }); verticalFlag = true; });
			if(char.asString == "h", { if(horizontalFlag.not, { startPoint = Point(mouseLoc[0],mouseLoc[1]) }); horizontalFlag = true; });
			if(char.asString == "d", {drawLineFlag = drawLineFlag.not});
			if(char.asString == "r", {pointlist = pointlist.scramble});
			if(char.asString == "z", { // undo creating a grain, cirle or square
				if((synthesisStylePop.value==2) || (synthesisStylePop.value == 3), {
					if(pointlist.size > 0, {
						pointlist.pop;
						pointsizelist.pop;
						this.refresh;
					});
				});
				if((synthesisStylePop.value==5) || (synthesisStylePop.value == 6), { // grainCirles
					if(circleList.size > 0, {
						circleList.pop;
						grainCircleSynthList[grainCircleSynthList.size-1].free;
						grainCircleSynthList.removeAt(grainCircleSynthList.size-1);
						this.refresh;
					});
				});
			});
			if(synthesisStylePop.value == 4, { // worm
				if (char.asString == "s", {  
					if(pointlist.size > 2, {
						pointlist.removeAt(pointlist.size-1);
						pointsizelist.removeAt(pointlist.size-1);
					});
				});
				if (char.asString == "a", {  
					pointlist.add(										Point(
							pointlist[pointlist.size-1].x+2, 
							pointlist[pointlist.size-1].y+2));
					pointsizelist.add(0.5);
				});
				if (char.asString == "q", { 
					if(wanderCircleRadius > 0.5, {
						wanderCircleRadius = wanderCircleRadius - 0.1;
					});
				});
				if (char.asString == "w", { wanderCircleRadius = wanderCircleRadius + 0.1 });
			});
			});
		});

		win.view.keyUpAction_({arg this, char, modifiers, unicode; 
			if(char.asString == "v", {verticalFlag = false});
			if(char.asString == "h", {horizontalFlag = false});
		});
		
		win.drawHook_({
			if ( GUI.id == \cocoa, { pointcolor.set },{ Pen.color_(pointcolor) });
		
			// scratching or warping
			if(drawLineFlag && grainFlag.not, {
				try{GUI.pen.moveTo(pointlist[0])};
				(pointlist.size-1).do({arg i; var width;
					width = (pointsize-1)*pointsizelist[i];
					if ( GUI.id == \cocoa, {Pen.setShadow(1@1, 5, Color.black)}); // XXX

					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.width = width;
					GUI.pen.line(pointlist[i], pointlist[i+1]);
					GUI.pen.stroke;
				});
			});
			// granular synthesis
			if(grainFlag, {
				pointlist.do({arg point, i; 
					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.fillOval(
						Rect(point.x-((pointsize*pointsizelist[i])/2)-0.5, 
							point.y-((pointsize*pointsizelist[i])/2)-0.5, 
							(pointsize*pointsizelist[i])+1, 
							(pointsize*pointsizelist[i])+1));
				});
			});
			
			// grain circles using TGrains
			if(grainCircles, {
				if(synthesisStylePop.value == 5, { // grainCircles
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeOval(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeOval(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillOval(rectNpressure[0]);
					});
				}, {
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeRect(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeRect(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillRect(rectNpressure[0]);
					});
				});
			});
		});
		
		selbPool = GUI.popUpMenu.new(win, Rect(10, 10, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var checkBufLoadTask;
				if(synthesisStylePop.value == 2, {randomGrainTask.stop}); // granular
				if(synthesisStylePop.value == 3, {linearGrainTask.stop}); // granular
				if(synthesisStylePop.value == 4, {wormGrainTask.stop}); // granular
				ldSndsGBufferList.value(selbPool.items[item.value]); // sending name of pool
				bufferPop.valueAction_(0);
				if(grainFlag == true, { 
					checkBufLoadTask = Task({
						inf.do({
							if(myTempBuffer.numChannels != nil, {
								{
									if(synthesisStylePop.value == 2, {randomGrainTask.start});
									if(synthesisStylePop.value == 3, {linearGrainTask.start});
									if(synthesisStylePop.value == 4, {wormGrainTask.start});
								}.defer;
								
								checkBufLoadTask.stop;
							});
							0.1.wait;
						});
					}).start;
				});
			});

		bufferPop = GUI.popUpMenu.new(win, Rect(10, 32, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["no buffer"])
				.background_(Color.white)
				.action_({ arg popup; 
					var filepath, selStart, selNumFrames, checkBufLoadTask, restartPlayPath;
					restartPlayPath = false;
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				
					if(synthesisStylePop.value == 2, {randomGrainTask.stop});
					if(synthesisStylePop.value == 3, {linearGrainTask.stop});
					if(synthesisStylePop.value == 4, {wormGrainTask.stop});
					if(playPathTask.isPlaying, {playPathTask.stop; restartPlayPath = true;});
					myTempBuffer.free;
					
					filepath = XQ.globalBufferDict.at(poolName)[0][popup.value].path;
					selStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
					selNumFrames =  XQ.globalBufferDict.at(poolName)[1][popup.value][1]-1;
					soundfile = SoundFile.new;
					soundfile.openRead(filepath);
					sndfileview.soundfile_(soundfile);
					sndfileview.read(selStart, selNumFrames);
					sndfileview.elasticMode_(true);
					myBuffer = XQ.globalBufferDict.at(poolName)[0][popup.value];
					// create a mono buffer if the sound is stereo
					if(soundfile.numChannels == 2, {
				myTempBuffer = Buffer.readChannel(s, filepath, selStart, selNumFrames, [0]);
					}, {
					// and make a right size buffer if only part of file is selected
				myTempBuffer = Buffer.read(s, filepath, selStart, selNumFrames);
					});
					soundfile.close;
					
					if((grainFlag == true) || (restartPlayPath), { 
						checkBufLoadTask = Task({
							inf.do({
								if(myTempBuffer.numChannels != nil, {
									{
									if(restartPlayPath, {
										playPathTask.start;
									}, {
										if(synthesisStylePop.value == 2, {randomGrainTask.start});
										if(synthesisStylePop.value == 3, {linearGrainTask.start});
										if(synthesisStylePop.value == 4, {wormGrainTask.start});
									});
									}.defer;
									checkBufLoadTask.stop;
								});
								0.1.wait;
							});
						}).start;
					});
				});
 			});
				
		ldSndsGBufferList = {arg argPoolName, firstpool=false;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
				 // put the first file into the view and load buffer (if first time)
				 if(firstpool, {bufferPop.action.value(0)}); 
			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);

		synthesisStylePop = GUI.popUpMenu.new(win, Rect(10, 54, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["warp", "scratch", "random grains", "linear grains", "worm", "grainCircles", "grainSquares", "keyboard grains"])
				.background_(Color.white)
				.action_({ arg popup;
					params[0] = popup.value;
					randomGrainTask.stop;
					linearGrainTask.stop;
					wormGrainTask.stop;
					grainCircles = false;
					grainCircleSynthList.do(_.free);
					circleGroup.freeAll;
					
					switch(popup.value,
						0, { grainFlag = false; synthType = \warp},
						1, { grainFlag = false; synthType = \scratch},
						2, { // random grains
							recPathFlag = false;
							grainFlag = true;
							randomGrainTask.start;
						},
						3, { // linear grains
							recPathFlag = false;
							grainFlag = true;
							linearGrainTask.start;
						},
						4, { // worm
							recPathFlag = false;
							grainFlag = true;
							wormGrainTask.start;
						},
						5, { // grainCircles
							circleGroup = Group.new;
							grainCircleSynthList = List.new;
							recPathFlag = false;
							grainFlag = false;
							grainCircles = true;
							circleList = List.new;
							this.clear;
						},
						6, { // grainSquares
							circleGroup = Group.new;
							grainCircleSynthList = List.new;
							recPathFlag = false;
							grainFlag = false;
							grainCircles = true;
							circleList = List.new;
							this.clear;
						},
						7, { // keyboard grains
							recPathFlag = false;
							grainFlag = true;
						}
					);
					this.refresh;
				});
		
		grainStatesPop = GUI.popUpMenu.new(win, Rect(10, 76, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["grain states"])
				.background_(Color.white)
				.action_({ arg popup;
					if(grainList.size > 1, {
						if(synthesisStylePop.value == 2, {randomGrainTask.stop});
						if(synthesisStylePop.value == 3, {linearGrainTask.stop});
						if(synthesisStylePop.value == 4, {wormGrainTask.stop});
						pointlist = grainList[popup.value][0].copy;
						pointsizelist = grainList[popup.value][1].copy;
						{this.refresh}.defer(0.2);
						if(synthesisStylePop.value == 2, {randomGrainTask.start});
						if(synthesisStylePop.value == 3, {linearGrainTask.start});
						if(synthesisStylePop.value == 4, {wormGrainTask.start});
					});
				});

		scrambleGrainListButt = GUI.button.new(win, Rect(10, 98, 57, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["scramble", Color.black, Color.clear]])
			.action_({arg butt; var randseed;
				randseed = 100000.rand;
				thisThread.randSeed = randseed;
				pointlist = pointlist.scramble;
				thisThread.randSeed = randseed; // in order to keep the amp correct for each grain
				pointsizelist = pointsizelist.scramble;
				if(grainCircles, { 
					circleList.do({arg proparray; 
						proparray[0].left = 120+(500.rand);
						proparray[0].top = 15+(300.rand);
					});
					grainCircleSynthList.do({arg synth, i;
					synth.set(\left, ((circleList[i][0].left/680)*myTempBuffer.numFrames)/s.sampleRate);
					synth.set(\right,(((circleList[i][0].left+circleList[i][0].width)/680)*myTempBuffer.numFrames)/s.sampleRate);
					// if the synth is graincircle
					synth.set(\ratelow, ((circleList[i][0].top/340) - 1).abs+0.5);
					synth.set(\ratehigh, (((circleList[i][0].top+circleList[i][0].height)/340) - 1).abs+0.5);
					// if the synth is grainsquare
					synth.set(\rate, (((circleList[i][0].top+circleList[i][0].height)/340) - 1).abs+0.5);
					});
					this.refresh;
				});
			});

		saveGrainListButt = GUI.button.new(win, Rect(70, 98, 37, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["save", Color.black, Color.clear]])
			.action_({arg butt;
				grainList.add([pointlist.copy, pointsizelist.copy]);
				grainStatesPop.items_(Array.fill(grainList.size, {arg i; "state "+(i+1).asString}));
				grainStatesPop.value_(grainList.size-1);
			});

		drawRButt = OSCIIRadioButton(win, Rect(15, 120, 12, 12), "draw")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							drawLineFlag = drawLineFlag.not;
							params[1] = butt.value;
						});

		clearScreenButt = GUI.button.new(win, Rect(70, 118, 37, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				if(synthesisStylePop.value == 4, {this.clear(true)}, {this.clear(false)});
				if(grainCircles, {	
					grainCircleSynthList.do(_.free); 
					circleGroup.freeAll;
					circleList = List.new;
					grainCircleSynthList = List.new;
					rateMultiplier = 1;
					circleGroup = Group.new;
					this.clear(false);				
				});
			});

		grainDurSl = OSCIISlider.new(win, Rect(10, 150, 100, 8), "- grain duration", 0.02, 0.5, 0.05, 0.001)
						.font_(GUI.font.new("Helvetica", 9))
						.keyDownAction_({arg this, char, modifiers, unicode;
					if (unicode == 16rF700, { grainDurSl.valueAction_((grainDurSl.value+0.001).round(0.001)) });
					if (unicode == 16rF703, { grainDurSl.valueAction_((grainDurSl.value+0.001).round(0.001)) });
					if (unicode == 16rF701, { grainDurSl.valueAction_((grainDurSl.value-0.001).round(0.001)) });
					if (unicode == 16rF702, { grainDurSl.valueAction_((grainDurSl.value-0.001).round(0.001)) });
						}) // I don't want any keydowns on sliders
						.action_({arg sl; 
							graindur = sl.value;
							if(grainCircles, { circleGroup.set(\dur, graindur) });
							params[2] = graindur;
						});

		grainDensitySl = OSCIISlider.new(win, Rect(10, 180, 100, 8), "- grain density", 0.5, 50, 10, 0.01)
						.font_(GUI.font.new("Helvetica", 9))
						.keyDownAction_({arg this, char, modifiers, unicode; 
					if (unicode == 16rF700, { grainDensitySl.valueAction_((grainDensitySl.value+0.1).round(0.1)) });
					if (unicode == 16rF703, { grainDensitySl.valueAction_((grainDensitySl.value+0.1).round(0.1)) });
					if (unicode == 16rF701, { grainDensitySl.valueAction_((grainDensitySl.value-0.1).round(0.1)) });
					if (unicode == 16rF702, { grainDensitySl.valueAction_((grainDensitySl.value-0.1).round(0.1)) });
						}) 
						.action_({arg sl; 
							graindensity = sl.value.reciprocal;
							if(grainCircles, { 
								grainCircleSynthList.do({arg synth, i; 
									synth.set(\trate, sl.value * circleList[i][2])
								});
							});
							params[3] = sl.value;
						});

		GUI.staticText.new(win, Rect(11, 210, 35, 18))
			.string_("env:")
			.font_(GUI.font.new("Helvetica", 9));

		sineEnvRButt = OSCIIRadioButton(win, Rect(35, 211, 12, 12), "sine")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							grainEnvType = 0;
							percEnvRButt.switchState;
							params[4] = butt.value;
						});

		percEnvRButt = OSCIIRadioButton(win, Rect(75, 211, 12, 12), "perc")
						.font_(GUI.font.new("Helvetica", 9))
						.action_({arg butt;
							grainEnvType = 1;
							sineEnvRButt.switchState;
							params[5] = butt.value;
						});

		recordPathButt = GUI.button.new(win, Rect(10, 235, 100, 16))
			.canFocus_(true)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["prepare recording", Color.black, Color.clear],
					["record", Color.black, Color.clear],
					["recording", Color.black, Color.red(alpha:0.3)],
					["playing", Color.black, Color.green(alpha:0.3)]
					])
			.action_({arg butt;
				if(synthesisStylePop.value != 4, {
				if(butt.value==2, {this.clear; recPathFlag = true; recordPathTask.start;});
				if(butt.value==3, {recordPathTask.stop; playPathFlag = true; recPathFlag = false; playPathTask.start});
				if(butt.value==0, {playPathTask.stop; playPathFlag = false; synth.free; this.clear;});
				});
			});

		wacomRButt = OSCIIRadioButton(win, Rect(10, 263, 12, 12), "tablet ")
						.font_(GUI.font.new("Helvetica", 9))
						.action_({ arg butt;
							mouseRButt.switchState;
							wacomFlag = true;
							pointsize = 6;
							params[6] = butt.value;
						});
		mouseRButt = OSCIIRadioButton(win, Rect(66, 263, 12, 12), "mouse ")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							wacomRButt.switchState;
							wacomFlag = false;
							pointsize = 6;
							params[7] = butt.value;
						});

		globalVolSl = OSCIISlider.new(win, Rect(10, 287, 100, 8), "- global vol", 0, 1, 1, 0.01, \amp)
						.font_(GUI.font.new("Helvetica", 9))						.action_({arg sl; 
							globalvol = sl.value;
							circleGroup.set(\globalvol, globalvol);
							params[8] = sl.value;
						});

		GUI.staticText.new(win, Rect(13, 315, 80, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("outbus :");
		
		outBusPop = GUI.popUpMenu.new(win, Rect(60, 318, 50, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					outbus = popup.value * 2;
					circleGroup.set(\outbus, outbus);
					try{ synth.set(\outbus, outbus) };
					params[9] = popup.value;
				});
		
		win.front;
		win.onClose_({ 
			var t;
			myTempBuffer.free;
			randomGrainTask.stop;
			linearGrainTask.stop;
			wormGrainTask.stop;
			recordPathTask.stop;
			playPathTask.stop;
			grainCircleSynthList.do({arg synth; synth.free;});
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		});
		
		randomGrainTask = Task({
			if(myTempBuffer.notNil, {
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						pointlist.size.do({
							var point, pointindex, zvol;
							pointindex = pointlist.size.rand;
							point = pointlist[pointindex];
							zvol = if(wacomFlag, {pointsizelist[pointindex]}, {1}); // full vol on mouse
							if(point != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum,
									\outbus, outbus,
									\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
									\rate, 1.5-(point.y/320),
									\envType, grainEnvType
								]);
							});
							graindensity.wait;
						});
					}, {
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
 				});
			});
		});
		
		linearGrainTask = Task({
			if(myTempBuffer.notNil, {
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						pointlist.size.do({arg p, pointindex;
							var point, zvol;
							point = pointlist[pointindex];
							zvol = if(wacomFlag, {pointsizelist[pointindex]}, {1}); // full vol on mouse
							if(point != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum, 
									\outbus, outbus,
									\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
									\rate, 1.5-(point.y/320),
									\envType, grainEnvType
								]);
							});
							graindensity.wait;
						});
					}, {
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
 				});
			});
		});

		wormGrainTask = Task({
			var point, taillength, stageRect, tailArray, oldpoint;
			var wanderCircleAngle, destpoint;
			var auto, boundaries, xOffset, yOffset;
			
			stageRect = Rect(122, 7, bounds.width-122, bounds.height-12);

			wanderCircleRadius = 4;
			wanderCircleAngle = 4;
			auto = false;
			boundaries = false;

			if(myTempBuffer.notNil, {
				pointlist = List.new;
				pointsizelist = List.new;
				point = Point(420+(50.rand), 50+(60.rand));
				3.do({arg i; 
					pointlist.add( Point(point.x-(i*2), point.y));
					pointsizelist.add(0.5);
				});
				destpoint = Point(point.x+(20.rand2), point.y+(20.rand2));
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						point.x = destpoint.x;
						point.y = destpoint.y;
			
						if(point.x > stageRect.width, {point.x = 0});
						if(point.x < 0, {point.x = stageRect.width});
						if(point.y > stageRect.height, {point.y = 0});
						if(point.y < 0, {point.y = stageRect.height});
						
						xOffset = (wanderCircleRadius * cos(wanderCircleAngle * pi/90) ) * 2;
						yOffset = (wanderCircleRadius * sin(wanderCircleAngle * pi/90) ) * 2;
				
						destpoint.x = point.x + xOffset;
						destpoint.y = point.y + yOffset;
					
						if(0.3.coin, {
							wanderCircleAngle = wanderCircleAngle + (20.rand2);
						});
						pointlist = pointlist.addFirst(Point(121+point.x, 6+point.y));
						pointlist.pop;

						pointlist.size.do({arg p, pointindex;
							var ppoint;
							ppoint = pointlist[p];
							if(ppoint != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum, 
									\outbus, outbus,
									\pos, ((ppoint.x-122)/680)*myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, 1 * globalvol,
									\rate, 1.5-(ppoint.y/320),
									\envType, grainEnvType
								]);
							});
							(graindensity/(pointlist.size/2.5)).wait; // speed it up
						});
						this.refresh;
					}, {
						"waiting for grains".postln;
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
					
					
 				});
			});
		});

		recordPathTask = Task({ 
			inf.do({
				recPathList.add([pointp, gPressure]);
				0.04.wait;
			});
		});

		playPathTask = Task({ //var counter;
				if(myTempBuffer.notNil, {
					switch(synthType,
						\warp, { // WARP
							synth = Synth(\xiiWarp, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus, 
							\pointer, (recPathList[0][0].x-122)/680, 
							\rate, 1.5-(recPathList[0][0].y/320), \vol, globalvol]);
						},
						\scratch, { // SCRATCH
							synth = Synth(\xiiScratch1x2, 
								[\bufnum, myTempBuffer.bufnum, \outbus, outbus,
								\pos, ((recPathList[0][0].x-122)/680)*myBuffer.numFrames, 
								\vol, globalvol]);
						});
				});
				0.1.wait;
				inf.do({
					pointlist = List.new;
					pointsizelist = List.new;
					recPathList.do({arg pointNsize, i;
						pointlist.add(pointNsize[0]);
						//pointsizelist.add(pointNsize[1]);
						pointsizelist.add(if(wacomFlag, {pointNsize[1]}, {0.5})); // high vol - small width
						
						if(grainFlag == false, {
							synth.set(\vol, [0,1,\amp, 0.00001].asSpec.map(pointNsize[1])*globalvol);
							synth.set(\pointer, (pointNsize[0].x-122)/680);  // warp 
							synth.set(\pos, ((pointNsize[0].x-122)/680) * myTempBuffer.numFrames); // scratch
							synth.set(\freq, 1.5-(pointNsize[0].y/320));
						});
						this.refresh; // redraw gui
						0.04.wait;
					});
				});
		});
		
		// loading settins
		synthesisStylePop.valueAction_(params[0]);
		drawRButt.valueAction_(params[1]);
		grainDurSl.valueAction_(params[2]);
		grainDensitySl.valueAction_(params[3]);
		sineEnvRButt.valueAction_(params[4]);
		percEnvRButt.valueAction_(params[5]);
		wacomRButt.valueAction_(params[6]);
		mouseRButt.valueAction_(params[7]);
		globalVolSl.valueAction_(params[8]);
		outBusPop.valueAction_(params[9]);

		ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // load first pool

	}
	
	refresh {
		{win.refresh}.defer;
	}

	clear { arg worm=false;
		if(worm, { // if worm, then don't delete all points
			pointlist = pointlist.copyRange(0, 1);
			pointsizelist = pointsizelist.copyRange(0, 1);
			this.refresh;
		}, {
			pointlist = List.new;
			pointsizelist = List.new;
			recPathList = List.new;
			this.refresh;
		}); // granular
	}
 
	updatePoolMenu {
		var poolname, poolindex;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		"updating pool menu".postln;
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		if(poolindex != nil, { // not first time pool is loaded
			selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
			"selbpool updating - index nil".postln;
			ldSndsGBufferList.value(poolname);
		}, {
			"first pool".postln;
			selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // load first pool
		});
	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}



/////////////////////////// old sound scratcher for SWING /////////////////////////////

initXiiSoundScratcherSwing { arg server, channels, setting;

		var foreColor, bgColor, synthesisStylePop, drawFlag;
		var sineEnvRButt, percEnvRButt;
		var scrambleGrainListButt, saveGrainListButt, grainStatesPop, grainList;
		var clearScreenButt, drawRButt, globalvol, globalVolSl, outbus, outBusPop;
		var recordPathButt, playPathTask, recordPathTask, recPathFlag, addSilencePointTask;
		var pointp, gPressure, rateMultiplier;
		var point;
		
		s = server; //Server.default;
		bounds = Rect(120, 5, 800, 340);

xiigui = nil;
point = if(setting.isNil, {Point(310, 250)}, {setting[1]});
params = if(setting.isNil, {[0, 1, 0.05, 10, 1, 0, 0, 1, 1.0, 0]}, {setting[2]});

		win = GUI.window.new("- soundscratcher -", 
				Rect(point.x, point.y, bounds.width+20, bounds.height+10), resizable:false);
		pointcolor = Color.new255(200,50,40);
		strokecolor = Color.black;
		pointsize = 6;
		linecolor = Color.new255(40,240,40);
		drawLineFlag = true; // false means granular synthesis - true scratching
		recPathFlag = false;
		recPathList = List.new;
		gPressure = 0; // only playing back in PlayBackTask if pressure is > 0;
		pointp = Point(200,200);
		synthType = \warp;
		
		globalvol = 1;
		outbus = 0;
		
		foreColor = XiiColors.darkgreen;
		bgColor = XiiColors.lightgreen;

		gBufferPoolNum = 0;
		sndNameList = List.new;
		bufferList = List.new; // contains bufnums of buffers (not buffers)
		drawFlag = true;
		graindensity = 0.1;
		grainEnvType = 0; // hanning envelope
		graindur = 0.05;
		grainList = List.new;  
		grainFlag = false;
		
		playPathFlag = false;
		
		pointlist = List.new;
		pointsizelist = List.new;
		grainCircles = false;
		circleList = List.new;
		tempPressureList = List.new;
		tempRect = Rect(0,0,0,0);
		grainCircleSynthList = List.new;
		circleGroup = Group.new;
		wacomFlag = false;
		rateMultiplier = 1; // used in grainCirles when one wants faster grains in individual synths (keys 1-4)
		
		soundfile = SoundFile.new;
		soundfile.openRead("sounds/a11wlk01.wav");

		sndfileview = GUI.soundFileView.new(win, Rect(120, 5, bounds.width-120, bounds.height-10))
			.soundfile_(soundfile)
			.read(0, soundfile.numFrames)
			//.elasticMode_(true)
			.timeCursorOn_(false)
			.timeCursorColor_(Color.white)
			.drawsWaveForm_(true)
			.gridOn_(false)
			.waveColors_([ foreColor, foreColor ])
			.background_(bgColor)
			.canFocus_(false)
			.setSelectionColor(0, Color.new255(105, 185, 125));
		
		soundfile.close;

		tabletView = GUI.userView.new(win, Rect(120, 5, bounds.width-120, bounds.height-20))
			.canFocus_(false)
			.mouseDownAction_({arg view,xx,yy;
			var x, y;
				x = xx - 120;
				y = yy-5;
				//gPressure = pressure; // TESTING
				gPressure = 0.5; // fixing pointsize mouse/wacom
				if( playPathFlag == true, {playPathTask.stop; recordPathButt.valueAction_(0)});
				
				if(grainCircles, {// GRAIN CIRCLES
					startPoint = Point(x,y);
					tempPressureList = List.new;
				}, {
					if(drawLineFlag && grainFlag.not && recPathFlag.not, { this.clear }); // view
					pointp = Point((x+120).round, (y+5).round);
					pointlist.add(pointp);
					pointsizelist.add(gPressure); 
					this.refresh;
					if(myTempBuffer.notNil, {
						switch(synthesisStylePop.value,
							0, { // WARP
							synth = Synth(\xiiWarp, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus, \pointer, x/680, 
							\freq, 1.5-(y/320), \vol, globalvol]);
							},
							1, { // SCRATCH
							synth = Synth(\xiiScratch1x2, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus,
							\pos, (x/680)*myBuffer.numFrames, \vol, globalvol]);
						});
					})
				});
			})
			.mouseMoveAction_({ arg  view, xx, yy;
			var x, y;
				x = xx - 120;
				y = yy-5;

				gPressure = 0.5;
				if(grainCircles, {  // GRAIN CIRCLES
					tempPressureList.add(gPressure);
					endPoint = Point(x,y);
					tempRect = Rect(
						tabletView.bounds.left+endPoint.x.min(startPoint.x), 
						tabletView.bounds.top+endPoint.y.min(startPoint.y), 
						(endPoint.x - startPoint.x).abs,
						(endPoint.y - startPoint.y).abs
						);
					this.refresh;
				}, {
					pointp = Point((x+120).round, (y+5).round);
					if(bounds.containsPoint(pointp), {
						pointlist.add(pointp);
						pointsizelist.add(0.5);
						this.refresh;
					});
					if(myTempBuffer.notNil, {
						if(grainFlag == false, {
						synth.set(\vol, [0,1,\amp, 0.00001].asSpec.map(0.5)*globalvol);
						synth.set(\freq, 1.5-(y/320));
						synth.set(\pointer, (x-6)/680); // for warp synth
						synth.set(\pos, ((x-6)/680) * myTempBuffer.numFrames);// scratch synth
						});
					});
				});
			})
			.mouseUpAction_({ arg  view,xx, yy;
				var meanpressure; var x, y;
				x = xx - 120;
				y = yy-5;
				if(grainCircles, {
					endPoint = Point(x,y);
					tempRect = Rect(0,0,0,0);
					meanpressure = 0.5;

					if(startPoint == endPoint, { endPoint = startPoint+2; meanpressure = 0.5;});
					circleList.add([Rect(
										tabletView.bounds.left+endPoint.x.min(startPoint.x)+0.5, 
										tabletView.bounds.top+endPoint.y.min(startPoint.y)+0.5, 
										(endPoint.x - startPoint.x).abs,
										(endPoint.y - startPoint.y).abs
										), 
										meanpressure,
										rateMultiplier] // if speed is increased
								);
					if(myTempBuffer.notNil, {
						if(synthesisStylePop.value == 5, { // grainCircles
							grainCircleSynthList.add(
								Synth(\xiiGrains, [
								\bufnum, myTempBuffer.bufnum, 
								\outbus, outbus,
								\dur, graindur,
								\trate, graindensity.reciprocal * rateMultiplier,
								\left, ((endPoint.x.min(startPoint.x)/680)*myTempBuffer.numFrames)/44100,
								\right, ((endPoint.x.max(startPoint.x)/680)*myTempBuffer.numFrames)/44100,
								\ratelow, ((endPoint.y.max(startPoint.y)/340) - 1).abs+0.5, // 680
								\ratehigh, ((endPoint.y.min(startPoint.y)/340) - 1).abs+0.5,
								\vol, meanpressure,
								\globalvol, globalvol
								], circleGroup,\addToHead);
							);
						}, {  // grainSquares
							grainCircleSynthList.add(
								Synth(\xiiGrainsSQ, [
								\bufnum, myTempBuffer.bufnum, 
								\outbus, outbus,
								\dur, graindur,
								\trate, graindensity.reciprocal * rateMultiplier,
								\left, ((endPoint.x.min(startPoint.x)/680)*myTempBuffer.numFrames)/44100,
								\rate, (((endPoint.y.max(startPoint.y)/340) - 1).abs+0.5 +
								 ((endPoint.y.min(startPoint.y)/340) - 1).abs+0.5) / 2,
								\vol, meanpressure,
								\globalvol, globalvol
								], circleGroup,\addToHead);
							);
						});
					});
					this.refresh;
				}, {
					if(synthesisStylePop.value != 4, { 
						gPressure = 0;
						pointlist.add(pointp);
						pointsizelist.add(0.5);
						this.refresh;
						if(myTempBuffer.notNil, {
							if(grainFlag == false, { synth.set(\gate,0)});
						});
					});
				});
			})
			.drawFunc_({
			//pointcolor.set;
			GUI.pen.color = pointcolor;
			// scratching or warping
			if(drawLineFlag && grainFlag.not, {
				try{GUI.pen.moveTo(pointlist[0])};
				(pointlist.size-1).do({arg i; var width;
					width = (pointsize-1)*pointsizelist[i];
					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.width = width;
					GUI.pen.line(pointlist[i], pointlist[i+1]);
					GUI.pen.stroke;
				});
			});
			// granular synthesis
			if(grainFlag, {
				pointlist.do({arg point, i; 
					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.fillOval(
						Rect(point.x-((pointsize*pointsizelist[i])/2)-0.5, 
							point.y-((pointsize*pointsizelist[i])/2)-0.5, 
							(pointsize*pointsizelist[i])+1, 
							(pointsize*pointsizelist[i])+1));
				});
			});
			
			// grain circles using TGrains
			if(grainCircles, {
				if(synthesisStylePop.value == 5, { // grainCircles
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeOval(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeOval(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillOval(rectNpressure[0]);
					});
				}, {
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeRect(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeRect(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillRect(rectNpressure[0]);
					});
				});
			});
		});
						
		win.view.keyDownAction_({arg this, char, modifiers, unicode; 
			var point, zvol, a;
			if(synthesisStylePop.value == 7, { // keyboard grains mode
				if(pointlist == List[], {
					point = Point(122+(680.rand), 320.rand);
					zvol = 1;
				}, {
					point = pointlist[a = pointlist.size.rand];
					zvol = if(wacomFlag, {pointsizelist[a]}, {1}); // full vol on mouse
				});
				if(myTempBuffer.isNil.not, {
				Synth.grain(\xiiGrain, 
					[\bufnum, myTempBuffer.bufnum,
					\outbus, outbus,
					\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
					\dur, graindur,
					\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
					\rate, 1.5-(point.y/320),
					\envType, grainEnvType
				]);
				});
			}, { // all other modes
			if(char.asString == "c", {
				if(synthesisStylePop.value == 4, {this.clear(true)}, {this.clear(false)});
				if(grainCircles, {	
					circleGroup.freeAll; 
					circleList = List.new;
					circleGroup = Group.new;
					this.refresh;
				});
			});
			if((synthesisStylePop.value == 5) || (synthesisStylePop.value == 6), { // grainCirles
				if(char.asString == "1", { rateMultiplier = 1 });
				if(char.asString == "2", { rateMultiplier = 2 });
				if(char.asString == "3", { rateMultiplier = 3 });
				if(char.asString == "4", { rateMultiplier = 4 });
			});
			
			if(char.asString == "d", {drawLineFlag = drawLineFlag.not});
			if(char.asString == "r", {pointlist = pointlist.scramble});
			if(char.asString == "z", { // undo creating a cirle or square
				if((synthesisStylePop.value==5) || (synthesisStylePop.value == 6), { // grainCirles
					if(circleList.size > 0, {
						circleList.pop;
						grainCircleSynthList[grainCircleSynthList.size-1].free;
						grainCircleSynthList.removeAt(grainCircleSynthList.size-1);
						this.refresh;
					});
				});
			});
			if(synthesisStylePop.value == 4, { // worm
				if (char.asString == "s", {  
					if(pointlist.size > 2, {
						pointlist.removeAt(pointlist.size-1);
						pointsizelist.removeAt(pointlist.size-1);
					});
				});
				if (char.asString == "a", {  
					pointlist.add(										Point(
							pointlist[pointlist.size-1].x+2, 
							pointlist[pointlist.size-1].y+2));
					pointsizelist.add(0.5);
				});
				if (char.asString == "q", { 
					if(wanderCircleRadius > 0.5, {
						wanderCircleRadius = wanderCircleRadius - 0.1;
					});
				});
				if (char.asString == "w", { wanderCircleRadius = wanderCircleRadius + 0.1 });
			});
			});
		});
			
		/*
		win.drawHook_({
			pointcolor.set;
			// scratching or warping
			if(drawLineFlag && grainFlag.not, {
				try{GUI.pen.moveTo(pointlist[0])};
				(pointlist.size-1).do({arg i; var width;
					width = (pointsize-1)*pointsizelist[i];
					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.width = width;
					GUI.pen.line(pointlist[i], pointlist[i+1]);
					GUI.pen.stroke;
				});
			});
			// granular synthesis
			if(grainFlag, {
				pointlist.do({arg point, i; 
					GUI.pen.color = Color.new(pointcolor.red*pointsizelist[i], 0, 0);
					GUI.pen.fillOval(
						Rect(point.x-((pointsize*pointsizelist[i])/2)-0.5, 
							point.y-((pointsize*pointsizelist[i])/2)-0.5, 
							(pointsize*pointsizelist[i])+1, 
							(pointsize*pointsizelist[i])+1));
				});
			});
			
			// grain circles using TGrains
			if(grainCircles, {
				if(synthesisStylePop.value == 5, { // grainCircles
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeOval(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeOval(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillOval(rectNpressure[0]);
					});
				}, {
					GUI.pen.color = Color.red(alpha:0.5);
					GUI.pen.width = 1;
					GUI.pen.strokeRect(tempRect);
					circleList.do({arg rectNpressure;
						GUI.pen.color = Color.red(alpha:(rectNpressure[1]*1.3)*0.7);
						GUI.pen.strokeRect(rectNpressure[0]);
						GUI.pen.color = Color.red(alpha:(rectNpressure[1])*0.7);
						GUI.pen.fillRect(rectNpressure[0]);
					});
				});
			});
		});
		*/
		
		selbPool = GUI.popUpMenu.new(win, Rect(10, 10, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var checkBufLoadTask;
				if(synthesisStylePop.value == 2, {randomGrainTask.stop}); // granular
				if(synthesisStylePop.value == 3, {linearGrainTask.stop}); // granular
				if(synthesisStylePop.value == 4, {wormGrainTask.stop}); // granular
				ldSndsGBufferList.value(selbPool.items[item.value]); // sending name of pool
				bufferPop.valueAction_(0);
				if(grainFlag == true, { 
					checkBufLoadTask = Task({
						inf.do({
							if(myTempBuffer.numChannels != nil, {
								{
									if(synthesisStylePop.value == 2, {randomGrainTask.start});
									if(synthesisStylePop.value == 3, {linearGrainTask.start});
									if(synthesisStylePop.value == 4, {wormGrainTask.start});
								}.defer;
								
								checkBufLoadTask.stop;
							});
							0.1.wait;
						});
					}).start;
				});
			});

		bufferPop = GUI.popUpMenu.new(win, Rect(10, 32, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["no buffer"])
				.background_(Color.white)
				.action_({ arg popup; 
					var filepath, selStart, selNumFrames, checkBufLoadTask, restartPlayPath;
					restartPlayPath = false;
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				
					if(synthesisStylePop.value == 2, {randomGrainTask.stop});
					if(synthesisStylePop.value == 3, {linearGrainTask.stop});
					if(synthesisStylePop.value == 4, {wormGrainTask.stop});
					if(playPathTask.isPlaying, {playPathTask.stop; restartPlayPath = true;});
					myTempBuffer.free;
					
					filepath = XQ.globalBufferDict.at(poolName)[0][popup.value].path;
					selStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
					selNumFrames =  XQ.globalBufferDict.at(poolName)[1][popup.value][1]-1;
					soundfile = SoundFile.new;
					soundfile.openRead(filepath);
					sndfileview.soundfile_(soundfile);
					sndfileview.read(selStart, selNumFrames);
					sndfileview.elasticMode_(true);
					myBuffer = XQ.globalBufferDict.at(poolName)[0][popup.value];
					// create a mono buffer if the sound is stereo
					if(soundfile.numChannels == 2, {
				myTempBuffer = Buffer.readChannel(s, filepath, selStart, selNumFrames, [0]);
					}, {
					// and make a right size buffer if only part of file is selected
				myTempBuffer = Buffer.read(s, filepath, selStart, selNumFrames);
					});
					soundfile.close;
					
					if((grainFlag == true) || (restartPlayPath), { 
						checkBufLoadTask = Task({
							inf.do({
								if(myTempBuffer.numChannels != nil, {
									{
									if(restartPlayPath, {
										playPathTask.start;
									}, {
										if(synthesisStylePop.value == 2, {randomGrainTask.start});
										if(synthesisStylePop.value == 3, {linearGrainTask.start});
										if(synthesisStylePop.value == 4, {wormGrainTask.start});
									});
									}.defer;
									checkBufLoadTask.stop;
								});
								0.1.wait;
							});
						}).start;
					});
				});
 			});
				
		ldSndsGBufferList = {arg argPoolName, firstpool=false;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
				 // put the first file into the view and load buffer (if first time)
				 if(firstpool, {bufferPop.action.value(0); bufferPop.valueAction_(0)}); 
			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol, true);

		synthesisStylePop = GUI.popUpMenu.new(win, Rect(10, 54, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["warp", "scratch", "random grains", "linear grains", "worm", "grainCircles", "grainSquares", "keyboard grains"])
				.background_(Color.white)
				.action_({ arg popup;
					params[0] = popup.value;
					randomGrainTask.stop;
					linearGrainTask.stop;
					wormGrainTask.stop;
					grainCircles = false;
					grainCircleSynthList.do(_.free);
					circleGroup.freeAll;
					
					switch(popup.value,
						0, { grainFlag = false; synthType = \warp},
						1, { grainFlag = false; synthType = \scratch},
						2, { // random grains
							recPathFlag = false;
							grainFlag = true;
							randomGrainTask.start;
						},
						3, { // linear grains
							recPathFlag = false;
							grainFlag = true;
							linearGrainTask.start;
						},
						4, { // worm
							recPathFlag = false;
							grainFlag = true;
							wormGrainTask.start;
						},
						5, { // grainCircles
							circleGroup = Group.new;
							grainCircleSynthList = List.new;
							recPathFlag = false;
							grainFlag = false;
							grainCircles = true;
							circleList = List.new;
							this.clear;
						},
						6, { // grainSquares
							circleGroup = Group.new;
							grainCircleSynthList = List.new;
							recPathFlag = false;
							grainFlag = false;
							grainCircles = true;
							circleList = List.new;
							this.clear;
						},
						7, { // keyboard grains
							recPathFlag = false;
							grainFlag = true;
						}
					);
					this.refresh;
				});
		
		grainStatesPop = GUI.popUpMenu.new(win, Rect(10, 76, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["grain states"])
				.background_(Color.white)
				.action_({ arg popup;
					if(grainList.size > 1, {
						if(synthesisStylePop.value == 2, {randomGrainTask.stop});
						if(synthesisStylePop.value == 3, {linearGrainTask.stop});
						if(synthesisStylePop.value == 4, {wormGrainTask.stop});
						pointlist = grainList[popup.value][0].copy;
						pointsizelist = grainList[popup.value][1].copy;
						{this.refresh}.defer(0.2);
						if(synthesisStylePop.value == 2, {randomGrainTask.start});
						if(synthesisStylePop.value == 3, {linearGrainTask.start});
						if(synthesisStylePop.value == 4, {wormGrainTask.start});
					});
				});

		scrambleGrainListButt = GUI.button.new(win, Rect(10, 98, 57, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["scramble", Color.black, Color.clear]])
			.action_({arg butt; var randseed;
				randseed = 100000.rand;
				thisThread.randSeed = randseed;
				pointlist = pointlist.scramble;
				thisThread.randSeed = randseed; // in order to keep the amp correct for each grain
				pointsizelist = pointsizelist.scramble;
				if(grainCircles, { 
					circleList.do({arg proparray; 
						proparray[0].left = 120+(500.rand);
						proparray[0].top = 15+(300.rand);
					});
					grainCircleSynthList.do({arg synth, i;
					synth.set(\left, ((circleList[i][0].left/680)*myTempBuffer.numFrames)/44100);
					synth.set(\right,(((circleList[i][0].left+circleList[i][0].width)/680)*myTempBuffer.numFrames)/44100);
					// if the synth is graincircle
					synth.set(\ratelow, ((circleList[i][0].top/340) - 1).abs+0.5);
					synth.set(\ratehigh, (((circleList[i][0].top+circleList[i][0].height)/340) - 1).abs+0.5);
					// if the synth is grainsquare
					synth.set(\rate, (((circleList[i][0].top+circleList[i][0].height)/340) - 1).abs+0.5);
					});
					this.refresh;
				});
			});

		saveGrainListButt = GUI.button.new(win, Rect(70, 98, 37, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["save", Color.black, Color.clear]])
			.action_({arg butt;
				grainList.add([pointlist.copy, pointsizelist.copy]);
				grainStatesPop.items_(Array.fill(grainList.size, {arg i; "state "+(i+1).asString}));
				grainStatesPop.value_(grainList.size-1);
			});

		drawRButt = OSCIIRadioButton(win, Rect(15, 120, 12, 12), "draw")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							drawLineFlag = drawLineFlag.not;
							params[1] = butt.value;
						});

		clearScreenButt = GUI.button.new(win, Rect(70, 118, 37, 16))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				if(synthesisStylePop.value == 4, {this.clear(true)}, {this.clear(false)});
				if(grainCircles, {	
					grainCircleSynthList.do(_.free); 
					circleGroup.freeAll;
					circleList = List.new;
					grainCircleSynthList = List.new;
					rateMultiplier = 1;
					circleGroup = Group.new;
					this.clear(false);				
				});
			});

		grainDurSl = OSCIISlider.new(win, Rect(10, 150, 100, 8), "- grain duration", 0.02, 0.5, 0.05, 0.001)
						.font_(GUI.font.new("Helvetica", 9))
						.keyDownAction_({arg this, char, modifiers, unicode;
					if (unicode == 16rF700, { grainDurSl.valueAction_((grainDurSl.value+0.001).round(0.001)) });
					if (unicode == 16rF703, { grainDurSl.valueAction_((grainDurSl.value+0.001).round(0.001)) });
					if (unicode == 16rF701, { grainDurSl.valueAction_((grainDurSl.value-0.001).round(0.001)) });
					if (unicode == 16rF702, { grainDurSl.valueAction_((grainDurSl.value-0.001).round(0.001)) });
						}) // I don't want any keydowns on sliders
						.action_({arg sl; 
							graindur = sl.value;
							if(grainCircles, { circleGroup.set(\dur, graindur) });
							params[2] = graindur;
						});

		grainDensitySl = OSCIISlider.new(win, Rect(10, 180, 100, 8), "- grain density", 0.5, 50, 10, 0.01)
						.font_(GUI.font.new("Helvetica", 9))
						.keyDownAction_({arg this, char, modifiers, unicode; 
					if (unicode == 16rF700, { grainDensitySl.valueAction_((grainDensitySl.value+0.1).round(0.1)) });
					if (unicode == 16rF703, { grainDensitySl.valueAction_((grainDensitySl.value+0.1).round(0.1)) });
					if (unicode == 16rF701, { grainDensitySl.valueAction_((grainDensitySl.value-0.1).round(0.1)) });
					if (unicode == 16rF702, { grainDensitySl.valueAction_((grainDensitySl.value-0.1).round(0.1)) });
						}) 
						.action_({arg sl; 
							graindensity = sl.value.reciprocal;
							if(grainCircles, { 
								grainCircleSynthList.do({arg synth, i; 
									synth.set(\trate, sl.value * circleList[i][2])
								});
							});
							params[3] = sl.value;
						});

		GUI.staticText.new(win, Rect(11, 210, 35, 18))
			.string_("env:")
			.font_(GUI.font.new("Helvetica", 9));

		sineEnvRButt = OSCIIRadioButton(win, Rect(35, 211, 12, 12), "sine")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							grainEnvType = 0;
							percEnvRButt.switchState;
							params[4] = butt.value;
						});

		percEnvRButt = OSCIIRadioButton(win, Rect(75, 211, 12, 12), "perc")
						.font_(GUI.font.new("Helvetica", 9))
						.action_({arg butt;
							grainEnvType = 1;
							sineEnvRButt.switchState;
							params[5] = butt.value;
						});

		recordPathButt = GUI.button.new(win, Rect(10, 235, 100, 16))
			.canFocus_(true)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["prepare recording", Color.black, Color.clear],
					["record", Color.black, Color.clear],
					["recording", Color.black, Color.red(alpha:0.3)],
					["playing", Color.black, Color.green(alpha:0.3)]
					])
			.action_({arg butt;
				if(synthesisStylePop.value != 4, {
				if(butt.value==2, {this.clear; recPathFlag = true; recordPathTask.start;});
				if(butt.value==3, {recordPathTask.stop; playPathFlag = true; recPathFlag = false; playPathTask.start});
				if(butt.value==0, {playPathTask.stop; playPathFlag = false; synth.free; this.clear;});
				});
			});

		wacomRButt = OSCIIRadioButton(win, Rect(10, 263, 12, 12), "wacom ")
						.font_(GUI.font.new("Helvetica", 9))
						.action_({ arg butt;
							mouseRButt.switchState;
							wacomFlag = true;
							pointsize = 6;
							params[6] = butt.value;
						});
		mouseRButt = OSCIIRadioButton(win, Rect(66, 263, 12, 12), "mouse ")
						.font_(GUI.font.new("Helvetica", 9))
						.value_(1)
						.action_({arg butt;
							wacomRButt.switchState;
							wacomFlag = false;
							pointsize = 6;
							params[7] = butt.value;
						});

		globalVolSl = OSCIISlider.new(win, Rect(10, 287, 100, 8), "- global vol", 0, 1, 1, 0.01, \amp)
						.font_(GUI.font.new("Helvetica", 9))						.action_({arg sl; 
							globalvol = sl.value;
							circleGroup.set(\globalvol, globalvol);
							params[8] = sl.value;
						});

		GUI.staticText.new(win, Rect(13, 315, 80, 20))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("outbus :");
		
		outBusPop = GUI.popUpMenu.new(win, Rect(60, 318, 50, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.action_({ arg popup;
					outbus = popup.value * 2;
					circleGroup.set(\outbus, outbus);
					try{ synth.set(\outbus, outbus) };
					params[9] = popup.value;
				});
		
		win.front;
		win.onClose_({ 
			var t;
			myTempBuffer.free;
			randomGrainTask.stop;
			linearGrainTask.stop;
			wormGrainTask.stop;
			recordPathTask.stop;
			playPathTask.stop;
			grainCircleSynthList.do({arg synth; synth.free;});
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		});
		
		randomGrainTask = Task({
			if(myTempBuffer.notNil, {
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						pointlist.size.do({
							var point, pointindex, zvol;
							pointindex = pointlist.size.rand;
							point = pointlist[pointindex];
							zvol = if(wacomFlag, {pointsizelist[pointindex]}, {1}); // full vol on mouse
							if(point != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum,
									\outbus, outbus,
									\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
									\rate, 1.5-(point.y/320),
									\envType, grainEnvType
								]);
							});
							graindensity.wait;
						});
					}, {
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
 				});
			});
		});
		
		linearGrainTask = Task({
			if(myTempBuffer.notNil, {
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						pointlist.size.do({arg p, pointindex;
							var point, zvol;
							point = pointlist[pointindex];
							zvol = if(wacomFlag, {pointsizelist[pointindex]}, {1}); // full vol on mouse
							if(point != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum, 
									\outbus, outbus,
									\pos, ((point.x-122)/680) * myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, [0,1,\amp, 0.00001].asSpec.map(zvol) * globalvol,
									\rate, 1.5-(point.y/320),
									\envType, grainEnvType
								]);
							});
							graindensity.wait;
						});
					}, {
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
 				});
			});
		});

		wormGrainTask = Task({
			var point, taillength, stageRect, tailArray, oldpoint;
			var wanderCircleAngle, destpoint;
			var auto, boundaries, xOffset, yOffset;
			
			stageRect = Rect(122, 7, bounds.width-122, bounds.height-12);

			wanderCircleRadius = 4;
			wanderCircleAngle = 4;
			auto = false;
			boundaries = false;

			if(myTempBuffer.notNil, {
				pointlist = List.new;
				pointsizelist = List.new;
				point = Point(420+(50.rand), 50+(60.rand));
				3.do({arg i; 
					pointlist.add( Point(point.x-(i*2), point.y));
					pointsizelist.add(0.5);
				});
				destpoint = Point(point.x+(20.rand2), point.y+(20.rand2));
				inf.do({
					if(pointlist.size > 0, { // if there are any grains
						point.x = destpoint.x;
						point.y = destpoint.y;
			
						if(point.x > stageRect.width, {point.x = 0});
						if(point.x < 0, {point.x = stageRect.width});
						if(point.y > stageRect.height, {point.y = 0});
						if(point.y < 0, {point.y = stageRect.height});
						
						xOffset = (wanderCircleRadius * cos(wanderCircleAngle * pi/90) ) * 2;
						yOffset = (wanderCircleRadius * sin(wanderCircleAngle * pi/90) ) * 2;
				
						destpoint.x = point.x + xOffset;
						destpoint.y = point.y + yOffset;
					
						if(0.3.coin, {
							wanderCircleAngle = wanderCircleAngle + (20.rand2);
						});
						pointlist = pointlist.addFirst(Point(121+point.x, 6+point.y));
						pointlist.pop;

						pointlist.size.do({arg p, pointindex;
							var ppoint;
							ppoint = pointlist[p];
							if(ppoint != nil, { // in case grains are erazed in middle of loop
								Synth.grain(\xiiGrain, 
									[\bufnum, myTempBuffer.bufnum, 
									\outbus, outbus,
									\pos, ((ppoint.x-122)/680)*myTempBuffer.numFrames, 
									\dur, graindur,
									\vol, 1 * globalvol,
									\rate, 1.5-(ppoint.y/320),
									\envType, grainEnvType
								]);
							});
							(graindensity/(pointlist.size/2.5)).wait; // speed it up
						});
						this.refresh;
					}, {
						"waiting for grains".postln;
						0.5.wait; // if there are no grains, wait repeatedly until there are
					});
					
					
 				});
			});
		});

		recordPathTask = Task({ 
			inf.do({
				recPathList.add([pointp, gPressure]);
				0.04.wait;
			});
		});

		playPathTask = Task({ //var counter;
				if(myTempBuffer.notNil, {
					switch(synthType,
						\warp, { // WARP
							synth = Synth(\xiiWarp, 
							[\bufnum, myTempBuffer.bufnum, \outbus, outbus, 
							\pointer, (recPathList[0][0].x-122)/680, 
							\rate, 1.5-(recPathList[0][0].y/320), \vol, globalvol]);
						},
						\scratch, { // SCRATCH
							synth = Synth(\xiiScratch1x2, 
								[\bufnum, myTempBuffer.bufnum, \outbus, outbus,
								\pos, ((recPathList[0][0].x-122)/680)*myBuffer.numFrames, 
								\vol, globalvol]);
						});
				});
				0.1.wait;
				inf.do({
					pointlist = List.new;
					pointsizelist = List.new;
					recPathList.do({arg pointNsize, i;
						pointlist.add(pointNsize[0]);
						//pointsizelist.add(pointNsize[1]);
						pointsizelist.add(if(wacomFlag, {pointNsize[1]}, {0.5})); // high vol - small width
						
						if(grainFlag == false, {
							synth.set(\vol, [0,1,\amp, 0.00001].asSpec.map(pointNsize[1])*globalvol);
							synth.set(\pointer, (pointNsize[0].x-122)/680);  // warp 
							synth.set(\pos, ((pointNsize[0].x-122)/680) * myTempBuffer.numFrames); // scratch
							synth.set(\freq, 1.5-(pointNsize[0].y/320));
						});
						this.refresh; // redraw gui
						0.04.wait;
					});
				});
		});
		
		// loading settins
		synthesisStylePop.valueAction_(params[0]);
		drawRButt.valueAction_(params[1]);
		grainDurSl.valueAction_(params[2]);
		grainDensitySl.valueAction_(params[3]);
		sineEnvRButt.valueAction_(params[4]);
		percEnvRButt.valueAction_(params[5]);
		wacomRButt.valueAction_(params[6]);
		mouseRButt.valueAction_(params[7]);
		globalVolSl.valueAction_(params[8]);
		outBusPop.valueAction_(params[9]);
	}
	

}
