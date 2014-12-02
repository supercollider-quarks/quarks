HrCodeSequencer : HadronPlugin
{

	var window, barView, numBpm, numBpb, rewindBut, stopBut, stopFuncBut, pauseBut, playBut,
	seqView, numBars, rulerView, zoom, zoomSlider, cursorView, shouldReRule, stopCodeDoc,
	<tracksHolder, <tracksView, oldScrollVisibleOrigin, <rowHeight, oldBpbCount, oldBarCount,
	<>stopFunc, numTpb, currentTpbReciprocal, consoleWin, garbageBlobs, <numRows, shouldRedrawLines;
	var seqClock, <>oldBeat, stopSeq, playSeq, isPlaying, blobInBeats, <>idBlobDict, addBlobWin,
	pendingJumpTo, pendingJumpBeat;
	
	var <>menuList;
	var <codeBlobs;
	var <>codeEnvirs, <curEnvirIndex;
	
	*initClass
	{
		this.addHadronPlugin;
	}
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 0;
		var numOuts = 0;
		var bounds = Rect(200, 200, 1024, 350);
		var name = "HrCodeSequencer";
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
	
		codeBlobs = List.new;
		
		oldScrollVisibleOrigin = 0@0;
		oldBeat = 0;
		isPlaying = false;
		zoom = 8;
		shouldReRule = true;
		rowHeight = 20;
		
		oldBpbCount = 4;
		oldBarCount = 60;
		
		currentTpbReciprocal = 16.reciprocal;
		
		pendingJumpTo = false;
		
		numRows = 13;
		shouldRedrawLines = true; //row lines on tracksView;
		
		blobInBeats = Array.fill(240, { List[]; }).asList;
		idBlobDict = Dictionary.new;
		
		codeEnvirs = [Environment.new, nil];
		curEnvirIndex = 0;
		
		garbageBlobs = List.new;
		
		stopFunc = {|hadronParent, seqParent, currentBeat| };
		
		outerWindow.view.keyDownAction_
		({|...args|
		
			//args.postln;
			if((args[2] == 8388864) or: { args[2]==8388608 }, //if fn pressed
			{
				args[3].switch
				(
					32, //space
					{
						if(isPlaying, { pauseBut.valueAction_(1); }, { playBut.valueAction_(1); });
					},
					63273, //fn + leftArrow
					{
						if(isPlaying.not, { oldBeat = oldBeat - 1; cursorView.refresh; });
					},
					63275, //fn + rightArrow
					{
						if(isPlaying.not, { oldBeat = oldBeat + 1; cursorView.refresh; });
					},
					114, //fn + r
					{
						if(isPlaying.not, { rewindBut.valueAction_(1); });
					},
					115, //fn + s
					{
						stopBut.valueAction_(1);
					},
					119, //fn + w
					{
						if(isPlaying.not,
						{//"writing".postln;
							codeEnvirs[1-curEnvirIndex] = codeEnvirs[curEnvirIndex].copy;
							parentApp.displayStatus("HrCodeSequencer: Saved the environment... (yay!)", 1);
						},
						{
							parentApp.displayStatus("HrCodeSequencer: Can't save environment while playing...", -1);
						});
					},
					113, //fn + q
					{
						if(isPlaying.not,
						{//"switching".postln;
							if(codeEnvirs[1-curEnvirIndex] != nil, { curEnvirIndex = 1 - curEnvirIndex; });
							parentApp.displayStatus("HrCodeSequencer: Loaded environment...", 1);
						},
						{
							parentApp.displayStatus("HrCodeSequencer: Can't load environment while playing...", -1);
						});
					},
					100, //fn + d
					{
						"debug info:".postln;
						codeEnvirs[curEnvirIndex].asCompileString.postln;
					
					},
					112, //fn + p
					{
						"Alive plugins and their ID numbers:".postln;
						parentApp.alivePlugs.collect({|item| ["Plug:" + item.name + item.ident, "ID:" + item.uniqueID]}).do(_.postln);
					},
					99, //fn + c
					{
						consoleWin = Window("Console...", Rect(400, 400, 350, 40), resizable: false);
						TextField(consoleWin, Rect(10, 10, 330, 20))
							.focus(true)
							.action_
							({|field|
								(">>" + ("{|asd| asd."++field.string.replace(" ", "")++"};").interpret.value(this)).postln;
							});
						consoleWin.front;
					}
				);
			});
		});
		
		barView = CompositeView(window, Rect(0, 0, window.bounds.width, 35)).background_(Color.gray(0.8));

		StaticText(barView, Rect(10, 10, 30, 20)).string_("Bpm:");
		numBpm = NumberBox(barView, Rect(40, 10, 40, 20)).value_(120)
			.action_
			({|num|
				if(isPlaying, { seqClock.tempo = num.value/60; });
			});
		
		StaticText(barView, Rect(90, 10, 30, 20)).string_("Bpb:");
		numBpb = NumberBox(barView, Rect(120, 10, 40, 20)).value_(4)
			.action_
			({|num|
			
				if(this.refreshBeatCount(num.value * numBars.value),
				{
					shouldReRule = true;
					this.drawRuler;
					oldBpbCount = num.value;
				},
				{
					numBpb.value = oldBpbCount;
					//cannot reduce numBpb...
				});
			});
		
		StaticText(barView, Rect(170, 10, 65, 20)).string_("Num. Bars:");
		numBars = NumberBox(barView, Rect(235, 10, 40, 20)).value_(60)
			.action_
			({|num|
				if(this.refreshBeatCount(num.value * numBpb.value),
				{
					shouldReRule = true;
					this.drawRuler;
					oldBarCount = num.value;
				},
				{
					numBars.value = oldBarCount;
					//cannot reduce numBpb...
				});
			});
			
		StaticText(barView, Rect(285, 10, 85, 20)).string_("Ticks per beat:");
		numTpb = NumberBox(barView, Rect(370, 10, 40, 20)).value_(16)
			.action_
			({|num| 
				
				var val = num.value.reciprocal;
				if(isPlaying,
				{
					seqClock.schedAbs(seqClock.beats.ceil, { currentTpbReciprocal = val; nil; });
				},
				{
					currentTpbReciprocal = val;
				});
			});
		
		rewindBut = HrButton(barView, Rect(450, 10, 20, 20)).states_([["<<"]])
			.action_
			({
				oldBeat = 0;
				cursorView.refresh;
			});
		stopBut = HrButton(barView, Rect(475, 10, 20, 20)).states_([["[]"]])
			.action_
			({
				stopFunc.value(parentApp, this, oldBeat);				playBut.valueAction_(0);			
			});
			
		pauseBut = HrButton(barView, Rect(500, 10, 20, 20)).states_([["II"]])
			.action_({ playBut.valueAction_(0); });
			
		playBut = HrButton(barView, Rect(525, 10, 25, 20))
			.states_
			([
				[">", Color.black, Color.gray],
				[">", Color.black, Color.green],
			])
			.action_
			({|btn|
			
				btn.value.switch
				(
					1, { this.playSeq; },
					0, { this.stopSeq; }
				);
			});
			
		stopFuncBut = Button(barView, Rect(570, 10, 80, 20)).states_([["Stop Func."]])
			.action_
			({
				var funcString;
				if(stopFunc == nil, //if there was an error in last func compilation
				{
					funcString = stopCodeDoc.lastFuncString; //return to last document contents
				},
				{//if all was good last time
					funcString = stopFunc.asCompileString; //stringize the last function.
				});
				stopCodeDoc = HrCSDocument.new(this, "Stop function", funcString, codeEnvirs[curEnvirIndex]);
			});
			
		
		Button(barView, Rect(barView.bounds.width - 260, 10, 80, 20)).states_([["To Cursor"]])
			.action_
			({
				seqView.visibleOrigin = (oldBeat*4*zoom)@seqView.visibleOrigin.y;
			});
		
		StaticText(barView, Rect(barView.bounds.width - 160, 10, 50, 20)).string_("Zoom:");
		zoomSlider = Slider(barView, Rect(barView.bounds.width - 120, 10, 100, 20))
			.value_(0.3)
			.step_(0.1)
			.action_
			({|sld|
				
				zoom = 4 + (sld.value * 15);
				shouldReRule = true;
				codeBlobs.do(_.reposition);
				this.drawRuler;
			});
		
		
		seqView = ScrollView(window, Rect(5, 35, window.bounds.width - 10, window.bounds.height - 40))
			.action_
			({|scrl|
				
				if(oldScrollVisibleOrigin.y != scrl.visibleOrigin.y,
				{
					rulerView.bounds = rulerView.bounds.moveToPoint(0@scrl.visibleOrigin.y);
					cursorView.bounds = cursorView.bounds.moveToPoint(0@(scrl.visibleOrigin.y + 20));
					oldScrollVisibleOrigin = scrl.visibleOrigin;
				});
			});
		
		
		tracksHolder = CompositeView(seqView, Rect(0, 30, numBpb.value * 4 * numBars.value * zoom, seqView.bounds.height-50)).background_(Color.blue);
		
		tracksView = UserView(tracksHolder, Rect(0, 0, numBpb.value * 4 * numBars.value * zoom, seqView.bounds.height-50))
			.resize_(5)
			.clearOnRefresh_(false)
			.background_(Color.gray(0.8))
			.focusColor_(Color.gray(alpha: 0))
			//.mouseOverAction_({}) //see this.braceForResize
			.mouseDownAction_
			({|...args| 
				
				if(args[4] == 1,
				{
					menuList.remove;
					
					if(isPlaying.not,
					{
					
						menuList = 
						ListView(tracksHolder, Rect(args[1]+args[0].bounds.left, args[2]+args[0].bounds.top, 150, 80)).background_(Color.gray(0.9))
						.items_(["Add code blob", "Add Row", "Remove last row", "Cancel"])
						.mouseUpAction_
						({|menu|
					
							menu.value.switch
							(
								0, 
								{
									menuList.visible_(false);
									this.showAddBlob(args[1], args[2]);
								},
								1, 
								{ 
									menuList.visible_(false); 
									shouldRedrawLines = true;
									tracksHolder.bounds = tracksHolder.bounds.resizeBy(0, 20); 
									numRows = numRows + 1;								},
								2, 
								{ 
									menuList.visible_(false); 
									if(numRows > 1,
									{
										if(codeBlobs.detect({|item| item.currentRow == (numRows - 1); }).isNil,
										{
											shouldRedrawLines = true;
											tracksHolder.bounds = tracksHolder.bounds.resizeBy(0, -20); 
											
											if(oldScrollVisibleOrigin.y != seqView.visibleOrigin.y,
											{
												rulerView.bounds = rulerView.bounds.moveToPoint(0@seqView.visibleOrigin.y);
												cursorView.bounds = cursorView.bounds.moveToPoint(0@(seqView.visibleOrigin.y + 20));
												oldScrollVisibleOrigin = seqView.visibleOrigin;
											});
											numRows = numRows - 1;
										});
									},
									{
								
										parentApp.displayStatus("HrCodeSequencer: Can't delete more rows...", -1);
					
									});
								},
								3, { menuList.visible_(false); }
							);
						});
					});
				});
			})
			.drawFunc_
			({|view|
			
				//"refreshing!".postln;
				if(shouldRedrawLines,
				{//"redrawing lines".postln;
					Pen.color = Color.gray(0.7);
					
					((view.bounds.height / rowHeight).asInteger - 1).do
					({|cnt|
						
						cnt = cnt+1;
						Pen.line(0@(cnt*rowHeight), view.bounds.width@(cnt*rowHeight));
					});
					
					Pen.stroke;
					shouldRedrawLines = false;
				});
			});
			
		
		cursorView = UserView(seqView, Rect(0, 20, numBpb.value * 4 * numBars.value * zoom, 10))
			//.clearOnRefresh_(false)
			.background_(Color.black)
			.focusColor_(Color.gray(alpha: 0))
			.drawFunc_
			({
				var cursorX;
				//seqClock.beats.postln; 
				cursorX = oldBeat*4*zoom;
				Pen.color_(Color.white);
				Pen.addRect(Rect(cursorX-1, 0, 3, 10));
				Pen.fill;	
				if(isPlaying,
				{
					if((cursorX - seqView.visibleOrigin.x) > 1014,
					{
						seqView.visibleOrigin = cursorX@seqView.visibleOrigin.y;
					});
				});	
			})
			.mouseMoveAction_
			({|view, x, y, mod|
			
				if(isPlaying.not,
				{
					oldBeat = ((x/zoom)/4).round(1);
					view.refresh;
				});
			})
			.mouseDownAction_
			({|view, x, y, mod|
				if(isPlaying.not,
				{
					oldBeat = ((x/zoom)/4).round(1);
					view.refresh;
				});
			});
		
		menuList = ListView(tracksHolder, Rect(0, 0, 100, 40)).background_(Color.white).visible_(false);
		
		this.drawRuler;
		
		saveGets = 
		[
			{ numBpm.value; },
			{ numBpb.value; },
			{ numBars.value; },
			{ numTpb.value; },
			{ currentTpbReciprocal },
			{ zoomSlider.value; },
			{ rewindBut.boundOnMidiArgs; },
			{ rewindBut.boundOffMidiArgs; },
			{ stopBut.boundOnMidiArgs; },
			{ stopBut.boundOffMidiArgs; },
			{ pauseBut.boundOnMidiArgs; },
			{ pauseBut.boundOffMidiArgs; },
			{ playBut.boundOnMidiArgs; },
			{ playBut.boundOffMidiArgs; },
			{ oldBpbCount; },
			{ oldBarCount; },
			{ stopFunc.asCompileString.replace("\n", 30.asAscii); },
			{ garbageBlobs; },
			{ numRows; },
			{
				codeBlobs.collect
				({|item| 
					
					[
						item.color, 
						item.blobXY, 
						item.codeID, 
						item.name, 
						item.blobStartBeat,
						item.blobEndBeat,
						item.currentRow,
						item.oldMouseXY,
						item.gripBound,
						item.oldBlobStartBeat,
						item.oldBlobEndBeat,
						item.sequenceFunc.asCompileString.replace("\n", 30.asAscii);,
						item.lastMouseButton
					];
				});
			}
		];
		
		saveSets =
		[
			{|argg| numBpm.valueAction_(argg); },
			{|argg| numBpb.valueAction_(argg); },
			{|argg| numBars.valueAction_(argg); },
			{|argg| numTpb.valueAction_(argg); },
			{|argg| currentTpbReciprocal = argg; },
			{|argg| zoomSlider.valueAction_(argg); },
			{|argg| rewindBut.boundOnMidiArgs_(argg); },
			{|argg| rewindBut.boundOffMidiArgs_(argg); },
			{|argg| stopBut.boundOnMidiArgs_(argg); },
			{|argg| stopBut.boundOffMidiArgs_(argg); },
			{|argg| pauseBut.boundOnMidiArgs_(argg); },
			{|argg| pauseBut.boundOffMidiArgs_(argg); },
			{|argg| playBut.boundOnMidiArgs_(argg); },
			{|argg| playBut.boundOffMidiArgs_(argg); },
			{|argg| oldBpbCount = argg; },
			{|argg| oldBarCount = argg; },
			{|argg| stopFunc = argg.replace(30.asAscii.asString, "\n").interpret; },
			{|argg| garbageBlobs = argg; },
			{|argg| numRows = argg; tracksHolder.bounds = tracksHolder.bounds.height_(20 * argg); }, 
			{|argg|
			
				argg.do
				({|item|
					
					codeBlobs.add(HrCodeBlob(this, item[3], item[4], item[6], item[0], item[2], item[5]));
					codeBlobs.last.blobXY = item[1];
					codeBlobs.last.oldMouseXY = item[7];
					codeBlobs.last.gripBound = item[8];
					codeBlobs.last.oldBlobStartBeat = item[9];
					codeBlobs.last.oldBlobEndBeat = item[10];
					codeBlobs.last.sequenceFunc = item[11].replace(30.asAscii.asString, "\n").interpret;
					codeBlobs.last.lastMouseButton = item[12];
				});
			}
		];
		
		
		
	}
	
	drawRuler
	{
		var oldTop;
		
		if(rulerView.notNil, { oldTop = rulerView.bounds.top; rulerView.remove; });
	
		rulerView = UserView(seqView, Rect(0, if(rulerView.isNil, { 0; }, { oldTop; }), numBpb.value * 4 * numBars.value * zoom, 20))
			.clearOnRefresh_(false)
			.background_(Color.gray(0.95))
			.drawFunc_
			({		
				var tWidth, ticksPerBar;
				
				if(shouldReRule, 
				{
					
					tWidth = rulerView.bounds.width;
					ticksPerBar = (4 * numBpb.value);
					
					tWidth.do
					({|cnt|
					
						block
						({|break|
							
							if(cnt % ticksPerBar == 0, 
							{
								Pen.font_(Font("Courier-Bold", 10));
								Pen.color_(Color.red);
								Pen.stringAtPoint((cnt / ticksPerBar).floor.asString, (cnt * zoom)@7);
								Pen.color_(Color.black);
								Pen.font_(Font("Courier", 8));
								break.value;
							});
							
							if(cnt % ticksPerBar % 4 == 0,
							{
								Pen.stringAtPoint((cnt % ticksPerBar / 4).floor.asString, (cnt * zoom)@10);
								break.value;
							});
						});
						
					}); 
					shouldReRule = false; 
				});
				
			});
		
		//refresh the cursorview and tracksview also...
		if(cursorView.notNil,
		{
			cursorView.bounds = Rect(cursorView.bounds.left, cursorView.bounds.top, rulerView.bounds.width, 10);
		});
		
		if(tracksHolder.notNil,
		{//.resize_(5) in the child userview handles its own resize.
			shouldRedrawLines = true;
			tracksHolder.bounds = Rect(tracksHolder.bounds.left, tracksHolder.bounds.top, rulerView.bounds.width, tracksHolder.bounds.height);
		});
	}
	
	playSeq
	{|argIsPlayJump|
		var seqThread =
			{
				var cursorX;
				
				try
				({
					loop
					({
						if(seqClock.beats >= oldBeat,
						{
							cursorX = seqClock.beats*4*zoom;
							
							oldBeat = seqClock.beats;
							{ 
								cursorView.refresh;
							}.defer;
							//seqClock.beats.postln;
							this.iterateSeq;			
						});
						currentTpbReciprocal.wait;	
					});
				}, 
				{|error| 
					
					{ parentApp.displayStatus("HrCodeSequencer: Error caught! Stopping...", -1); }.defer;
					this.stopSeq(true); //error flag is true.
					error.throw; 
				});
			};
		
		{
			if(seqView.visibleOrigin.x > (oldBeat*4*zoom),
			{
				seqView.visibleOrigin = (oldBeat*4*zoom)@seqView.visibleOrigin.y;
			});
		}.defer; //defer for this.jumpTo method
		
		//remove previously deleted blob views (which are invisible) from tracksView
		if((argIsPlayJump == true).not,
		{
			garbageBlobs.do({|item| idBlobDict.at(item).blobView.remove; idBlobDict.removeAt(item); });
			garbageBlobs = List.new;
			tracksView.refresh;
		});
		
		
		seqClock = TempoClock(numBpm.value/60, oldBeat);
		//"starting".postln;
		seqClock.schedAbs(0, 
		{ 
			seqClock.beatsPerBar = numBpb.value;
			seqThread.fork(seqClock); 
			isPlaying = true;
			nil; 
		});
		{ rewindBut.enabled_(false); }.defer; //defer for this.jumpTo method
	}
	
	stopSeq
	{|argIsWithError|
	
		//"stopping".postln;
		isPlaying = false;
		seqClock.stop;
		
		if(pendingJumpTo, { oldBeat = pendingJumpBeat.round(1); }, { oldBeat = oldBeat.round(1); });
		pendingJumpTo = false;
		
		{
			rewindBut.enabled_(true);
			cursorView.refresh;
			if(argIsWithError == true, { playBut.value_(0); });
		}.defer;
	}
	
	jumpTo //for use in blob code windows
	{|argBeat|
		
		if(pendingJumpTo.not,
		{
			pendingJumpTo = true;
			pendingJumpBeat = argBeat;
			seqClock.sched(currentTpbReciprocal, { seqClock.stop; oldBeat = argBeat; this.playSeq(true);  pendingJumpTo = false; });
		},
		{
			"There already is a pending \".jumpTo\" for the next iteration...".error;
		});
	}
	
	changeBpm //for use in blob code windows
	{|argBpm|
	
		var newTempo = argBpm;
		seqClock.tempo = newTempo / 60;
		{ numBpm.value_(newTempo) }.defer;
	}
	
	getBpm //for use in blob code windows
	{
		^seqClock.tempo * 60;
	}
	
	resetEnvir //for use within the application.
	{
		codeEnvirs[curEnvirIndex] = Environment.new;
		codeBlobs.do({|item| codeEnvirs[curEnvirIndex].put(item.name.asSymbol, item); });
	}
	
	givePlug
	{|argID|
	
		^parentApp.idPlugDict.at(argID);
	}
	
	refreshBeatCount
	{|argNumNewBeats|
		
		argNumNewBeats = argNumNewBeats.asInteger;
		
		if(argNumNewBeats < blobInBeats.size,
		{
			if(blobInBeats[argNumNewBeats..].detect({|item| item.size > 0; }).notNil,
			{
				^false;
			},
			{
				blobInBeats = blobInBeats[..argNumNewBeats-1];
				^true;
			});
		});
		
		while({ blobInBeats.size < argNumNewBeats; }, { blobInBeats.add(List[]); });
		^true;
	}	
	
	registerBlobInTime
	{|argBlob|
	
		var start = argBlob.blobStartBeat;
		var end = argBlob.blobEndBeat;
		var oldStart = argBlob.oldBlobStartBeat;
		var oldEnd = argBlob.oldBlobEndBeat;
		var newOrder;
		
		if(oldStart.notNil and: { oldEnd.notNil; },
		{
			(oldStart..(oldEnd-1)).do
			({|oldBIndex|
			
				if(blobInBeats[oldBIndex].remove(argBlob.codeID).isNil, { "HrCodeSequencer: This should nevver happen".error; });
			});
		});
		
		(start..(end-1)).do
		({|bIndex|
			
			blobInBeats[bIndex].add(argBlob.codeID);
			newOrder = blobInBeats[bIndex].collect({|item| idBlobDict.at(item).currentRow; }).order;
			blobInBeats[bIndex] = blobInBeats[bIndex][newOrder].asList; //turns into array here, hence the .asList
			
		});
		
		argBlob.oldBlobStartBeat = argBlob.blobStartBeat;
		argBlob.oldBlobEndBeat = argBlob.blobEndBeat;
	}
	
	showAddBlob
	{|argX, argY|
		var abName, abcR, abcG, abcB, colView, okButton;
		
		if(addBlobWin.notNil, { if(addBlobWin.isClosed == false, { addBlobWin.close; }); });
		
		addBlobWin = Window("Add CodeBlob", Rect(300, 300, 210, 120), resizable: false);
		
		StaticText(addBlobWin, Rect(10, 10, 40, 20)).string_("Name:");
		abName = TextField(addBlobWin, Rect(50, 10, 110, 20))
			.focus(true)
			.action_({ okButton.valueAction_(1); });
		
		StaticText(addBlobWin, Rect(10, 40, 40, 20)).string_("Color:");
		abcR = NumberBox(addBlobWin, Rect(50, 40, 30, 20)).value_(256.rand);
		abcG = NumberBox(addBlobWin, Rect(90, 40, 30, 20)).value_(256.rand);
		abcB = NumberBox(addBlobWin, Rect(130, 40, 30, 20)).value_(256.rand);
		
		colView = UserView(addBlobWin, Rect(170, 40, 30, 20, 20))
			.background_(Color.new255(abcR.value, abcG.value, abcB.value))
			.mouseDownAction_
			({|view|
				abcR.value_(256.rand);
				abcG.value_(256.rand);
				abcB.value_(256.rand);
				view.background_(Color.new255(abcR.value, abcG.value, abcB.value));
			});
		
		okButton = Button(addBlobWin, Rect(20, 80, 80, 20)).states_([["Ok"]])
			.action_
			({
				if(abName.string.find(" ").notNil or: { abName.string.size == 0; } or: { abName.string[0].isAlpha.not; } or: { abName.string[0].toLower != abName.string[0]},
				{
					parentApp.displayStatus("HrCodeSequencer: Name can't contain spaces, first character must be a lowercase alphanumeric character", -1);
				},
				{
					if(codeBlobs.collect({|item| item.name.asSymbol }).detect({|item| item == abName.string.asSymbol; }).notNil,
					{
						parentApp.displayStatus("HrCodeSequencer: An item with the same name already exists...", -1);
					},
					{
					codeBlobs.add(HrCodeBlob(this, abName.string, this.coordToBeat(argX).floor, ((argY/20).floor * 20) / rowHeight, Color.new255(abcR.value, abcG.value, abcB.value)));
					addBlobWin.close;
					//HrCodeBlob adds itself.
					//idBlobDict.put(codeBlobs.last.codeID, codeBlobs.last);
					});
				});
			});
		Button(addBlobWin, Rect(110, 80, 80, 20)).states_([["Cancel"]])
			.action_
			({
				addBlobWin.close;
			});
		
		addBlobWin.front;
	}
	
	iterateSeq
	{
		var activeBlob;
		
		blobInBeats[seqClock.beats.floor.asInteger].do
		({|activeBlobID|
			
			activeBlob = idBlobDict.at(activeBlobID);
			codeEnvirs[curEnvirIndex].use({ activeBlob.sequenceFunc.value(parentApp, this, seqClock.beats, activeBlob.blobStartBeat, activeBlob.blobEndBeat, currentTpbReciprocal); });
		});
	}
	
	giveCodeID
	{
		var tempRand = 65536.rand;
		var tempPool = codeBlobs.collect({|item| item.codeID; });
		
		while({ tempPool.detectIndex({|item| item == tempRand; }).notNil },
		{
			tempRand = 65536.rand;
		});
		
		^tempRand;
	}
	
	beatToCoord
	{|argBeat|
	
		^argBeat*4*zoom;
	}
	
	coordToBeat
	{|argXCoord|
	
		^argXCoord / 4 / zoom
	}
	
	giveNumBpb
	{
		^numBpb.value;
	}
	
	getCodeEnvir
	{
		^codeEnvirs[curEnvirIndex];
	}
	
	braceForResize
	{|argCodeBlob|
	
		tracksView.mouseOverAction_
		({
			//"cleaning".postln;
			argCodeBlob.isOnResize = false;
			argCodeBlob.viewRefresh;
			tracksView.mouseOverAction_({ nil; });
		});		
	}
	
	removeBlob
	{|argBlob|
	
		var start = argBlob.blobStartBeat;
		var end = argBlob.blobEndBeat;
		
		(start..(end-1)).do
		({|bIndex|
		
			//("removing"+bIndex.asString).postln;
			if(blobInBeats[bIndex].remove(argBlob.codeID).isNil, { "HrCodeSequencer: This should nevver happen".error; });
		});
		
		garbageBlobs.add(argBlob.codeID);
		
		codeBlobs.remove(argBlob);
		
		//this is done after items in garbageBlobs is removed at play.
		//idBlobDict.removeAt(argBlob.codeID);
		
	}
	
	updateBusConnections
	{//no audio connections...
	}
	
	cleanUp
	{
		if(consoleWin.notNil, { consoleWin.close; });
		if(addBlobWin.notNil, { addBlobWin.close; });
		if(isPlaying, { seqClock.stop; });
	}
}

HrCodeBlob
{
	var <parentSeq, <>color, <>blobXY, <codeID, <name, <blobView, <blobStartBeat, <blobEndBeat, <currentRow,
	<>oldMouseXY, <>isOnResize, <>gripBound, <>oldBlobStartBeat, <>oldBlobEndBeat, <>sequenceFunc, <blobDoc,
	<>lastMouseButton;

	*new
	{|argParentSeq, argName, argStartBeat, argRow, argColor, argOptionalID, argOptionalEndBeat|
	
		^super.new.init(argParentSeq, argName, argStartBeat, argRow, argColor, argOptionalID, argOptionalEndBeat);
	}
	
	init
	{|argParentSeq, argName, argStartBeat, argRow, argColor, argOptionalID, argOptionalEndBeat|
	
		parentSeq = argParentSeq;
		blobStartBeat = argStartBeat;
		blobEndBeat = argOptionalEndBeat ? (argStartBeat + parentSeq.giveNumBpb);
		color = argColor;	
		currentRow = argRow;
		blobXY = parentSeq.beatToCoord(blobStartBeat)@(currentRow * parentSeq.rowHeight);
		name = argName;
		oldMouseXY = 0@0;
		isOnResize = false;
		
		sequenceFunc = {|hadronParent, seqParent, currentBeat, beatStart, beatEnd, callInterval| };
		
		codeID = if(argOptionalID.isNil, { parentSeq.giveCodeID; }, { argOptionalID; });
		
		parentSeq.idBlobDict.put(codeID, this);
		
		parentSeq.codeEnvirs[parentSeq.curEnvirIndex].put(name.asSymbol, this);
		if(parentSeq.codeEnvirs[1 - parentSeq.curEnvirIndex].notNil,
		{
			parentSeq.codeEnvirs[1 - parentSeq.curEnvirIndex].put(name.asSymbol, this);
		});
		
		parentSeq.registerBlobInTime(this);
		
		blobView = UserView(parentSeq.tracksHolder, Rect.newSides(blobXY.x, blobXY.y, parentSeq.beatToCoord(blobEndBeat), blobXY.y + 20))
			.focusColor_(Color(alpha: 0))
			.background_(color)
			.mouseOverAction_
			({|...args|
				
				gripBound = args[0].bounds;
				gripBound = Rect(gripBound.width - 5, 0, 5, parentSeq.rowHeight);
				if(gripBound.contains(args[1]@args[2]),
				{
					isOnResize = true;
					args[0].refresh;
					parentSeq.braceForResize(this);
				},
				{
					isOnResize = false;
					args[0].refresh;
				});				
			})
			.mouseMoveAction_
			({|...args|
			
				var tempXY, delta, newBounds;
				
				if(isOnResize.not,
				{
					tempXY = args[0].bounds.origin + (args[1]@args[2]);
					delta = tempXY - oldMouseXY;
					oldMouseXY = tempXY;
					newBounds = args[0].bounds.moveBy(delta.x, delta.y);
					if(parentSeq.tracksView.bounds.contains(newBounds.leftTop) and: { parentSeq.tracksView.bounds.contains(newBounds.rightBottom) },
					{
						args[0].bounds = newBounds;
					});
				},
				{
					tempXY = args[0].bounds.origin + (args[1]@args[2]);
					if(tempXY.x > (args[0].bounds.origin.x + parentSeq.beatToCoord(0.5)), //if you don't limit it, sc crashes.
					{
						
						gripBound = args[0].bounds;
						gripBound = Rect(gripBound.width - 5, 0, 5, parentSeq.rowHeight);
						delta = tempXY - oldMouseXY;
						oldMouseXY = tempXY;
						args[0].bounds = args[0].bounds.resizeBy(delta.x, 0);
						
						blobEndBeat = parentSeq.coordToBeat(args[0].bounds.rightBottom.x).round(1);
					});
				});
				
				lastMouseButton = 0;
			})
			.mouseUpAction_
			({|...args|
			
				var curBeatDiff = blobEndBeat - blobStartBeat;
				
				if(lastMouseButton == 0,
				{
					oldBlobStartBeat = blobStartBeat.copy;
					
					
					blobStartBeat = parentSeq.coordToBeat(args[0].bounds.left).round(1);
					blobEndBeat = blobStartBeat + curBeatDiff;
					
					currentRow = (args[0].bounds.top).round(parentSeq.rowHeight) / parentSeq.rowHeight;
					isOnResize = false;
					parentSeq.registerBlobInTime(this);
					this.reposition;
				},
				{
					parentSeq.menuList.remove;
					
					if(parentSeq.isPlaying.not,
					{
					
						parentSeq.menuList = 
						ListView(parentSeq.tracksHolder, Rect(args[1]+args[0].bounds.left, args[2]+args[0].bounds.top, 100, 50)).background_(Color.gray(0.9))
						.items_(["Delete", "Cancel"])
						.mouseUpAction_
						({|menu|
						
							menu.value.switch
							(
								0, 
								{ 
									parentSeq.removeBlob(this);
									
									if(parentSeq.codeEnvirs[parentSeq.curEnvirIndex].removeAt(name.asSymbol).isNil,
									{
										"This should never happen. Instance not saved in codeEnvir.".error;
									});
									
									if(parentSeq.codeEnvirs[1 - parentSeq.curEnvirIndex].notNil,
									{
										if(parentSeq.codeEnvirs[1 - parentSeq.curEnvirIndex].removeAt(name.asSymbol).isNil,
										{
											"This should never happen. Instance not saved in old codeEnvir.".error;
										});
										
									});
									
									//actual removal will take place later on when the sequencer starts playing.
									//because .remove in this method might crash sc as of 3.3.1.
									blobView.visible_(false); 
									parentSeq.menuList.visible_(false);
								},
								1, { parentSeq.menuList.visible_(false); }
							);
						});
					});
				});
				
			})
			.mouseDownAction_
			({|...args|
			
				var funcString;
				oldMouseXY = args[0].bounds.origin + (args[1]@args[2]);
				
				lastMouseButton = args[4]; //left click or right click? will be used in mouseUp action.
				
				if(args[5] == 2, //if double clicked
				{
					if(sequenceFunc == nil, //if there was an error in last func compilation
					{
						funcString = blobDoc.lastFuncString; //return to last document contents
					},
					{//if all was good last time
						funcString = sequenceFunc.asCompileString; //stringize the last function.
					});
					blobDoc = HrCSDocument.new(this, name, funcString, parentSeq.getCodeEnvir);
				});
			})
			.drawFunc_
			({|view|
				
				if(isOnResize,
				{
					Pen.color = Color.black;
					Pen.addRect(gripBound);
					Pen.fill;
				});
				
				Pen.color = Color.black;
				Pen.font = Font("Helvetica", 12);
				Pen.stringAtPoint("~"++name, 3@3);
			});
	}
	
	reposition
	{
		blobXY = parentSeq.beatToCoord(blobStartBeat)@(currentRow * parentSeq.rowHeight);
		blobView.bounds = Rect.newSides(blobXY.x, blobXY.y, parentSeq.beatToCoord(blobEndBeat), blobXY.y + 20);
	}	
	
	viewRefresh
	{
		blobView.refresh;
	}
}

HrCSDocument
{
	//parentBlob might actually be the sequencer here, for using the doc class as a stop func in sequencer. hacky...
	var parentBlob, myDoc, docEnvir, <lastFuncString, isDirty, cleanTitle;

	*new
	{|argParentBlob, argTitle, argString, argEnvir|
	
		^super.new.init(argParentBlob, argTitle, argString, argEnvir);
	}
	
	init
	{|argParentBlob, argTitle, argString, argEnvir|
		
		parentBlob = argParentBlob;
		docEnvir = argEnvir;
		
		isDirty = false;
		cleanTitle = argTitle;
		
		myDoc = Document.new(argTitle, argString, envir: docEnvir)
			.promptToSave_(false)
			.keyDownAction_
			({|view, char, mod, unicode, keycode|
				
				//[view, char, mod, unicode, keycode].postln;
				
				if(isDirty.not, { view.title = view.title ++ "-edited"; isDirty = true; });
				
				if((mod == 8388864).or(mod == 8388608),
				{
					unicode.switch
					(
						99, // fn + c pressed
						{ 
							lastFuncString = myDoc.string;
							this.assignFunc;
							isDirty = false;
							view.title = cleanTitle;
							view.editable_(false); 
						},
						112, //fn + p
						{
							"Alive plugins and their ID numbers:".postln; 
							parentBlob.parentSeq.parentApp.alivePlugs.collect //duh...
							({|item| 
								["Plug:" + item.name + item.ident, "ID:" + item.uniqueID]
							}).do(_.postln);
						}		
					);
				});
			})
			.keyUpAction_
			({|view, char, mod, unicode, keycode|
				
				//[view, char, mod, unicode, keycode].postln;
				if((mod == 8388864).or(mod == 8388608),
				{
					unicode.switch
					(
						99, // fn + c unpressed
						{ view.editable_(true); }			
					);
				});
			})
			.onClose_
			({
				lastFuncString = myDoc.string;
				this.assignFunc;
			})
			.syntaxColorize;
	}
	
	closeDoc
	{
		myDoc.close;
	}
	
	assignFunc
	{//a bit hacky, this was first intended only for code blobs, harmless though.
	
		if(parentBlob.class == HrCodeBlob,
		{
			parentBlob.sequenceFunc = myDoc.string.interpret;
		},
		{//else it is HrCodeSequencer
			parentBlob.stopFunc = myDoc.string.interpret; //parent is not blob here, but the sequencer...
		});
	}
}