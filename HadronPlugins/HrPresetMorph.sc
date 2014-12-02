HrPresetMorph : HadronPlugin
{
	var <surfaceView, <presetList, curPresets, nPresetText, addButton, <mouseXY, <compositeBack,
	canvasItems, refreshRoutine, isRefreshing, rotateCounter, rotFunc, <>menuList,
	senseDistance, senseCurve;
	
	*initClass
	{
		this.addHadronPlugin;
	}
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 0;
		var numOuts = 0;
		var bounds;
		var name = "HrPresetMorph";
		
		if(argExtraArgs.isNil, 
		{ 
			bounds = Rect(200, 200, 610, 455);
		}, 
		{ 
			if(argExtraArgs.size == 1,
			{
				bounds = Rect(200, 200, 160 + argExtraArgs[0].asInteger, 455);
			},
			{
				bounds = Rect(200, 200, 160 + argExtraArgs[0].asInteger, argExtraArgs[1].asInteger + 5);
			});
		});
		
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
	
		outerWindow.acceptsMouseOver = true;
		curPresets = Dictionary.new;
		canvasItems = List.new;
		isRefreshing = false;
		
		helpString = "Cmd (ctrl) + drag presets to surface. Right click to presets (on list and canvas) for options.";
		
		menuList = ListView(window, Rect(0, 0, 100, 40)).background_(Color.white).visible_(false);
		
		rotateCounter = -pi;
		
		rotFunc =
		{//calculates coords for rotating cursor line
			rotateCounter = (rotateCounter + 0.4).wrap(-pi, pi);
			[([-25, 25] * rotateCounter.sin), ([-25, 25] * (rotateCounter + (pi/2)).sin)];
		};
		
		refreshRoutine = Routine({ loop({ surfaceView.refresh; 0.04.wait; }); });
		
		compositeBack = CompositeView(window, Rect(160, 0, window.bounds.width - 165, window.bounds.height));
		surfaceView = 
		UserView(compositeBack, Rect(0, 0, window.bounds.width - 165, window.bounds.height))
		.focusColor_(Color.gray(alpha: 0))
		.background_(Color.white)
		.mouseOverAction_({|...args| mouseXY = (args[1]@args[2]); })
		.mouseMoveAction_
		({|...args|
			if(isRefreshing == false,
			{
				//"starting".postln;
				refreshRoutine.reset;
				refreshRoutine.play(AppClock);
				isRefreshing = true;
			});
			mouseXY = (args[1]@args[2]);
		})
		.mouseUpAction_
		({
			if(isRefreshing,
			{
				//"stopping".postln;
				refreshRoutine.stop;
				isRefreshing = false;
				rotateCounter = -pi;
				surfaceView.refresh;
			});
		})
		//.mouseDownAction_({|...args|  })
		.canReceiveDragHandler_({ true; })
		.receiveDragHandler_
		({|...args| 
			
			if(GUI.id == \swing, { mouseXY = 10@10; });
			if(canvasItems.detect({|item| item.name == presetList.items[presetList.value] }).notNil,
			{
				parentApp.displayStatus("HrPresetMorph: Preset" + presetList.items[presetList.value].asString + "is already on surface.", -1); 
			},
			{
				canvasItems.add(HrPresetMorphItemView.new(this, mouseXY, presetList.items[presetList.value]));
			});
		})
		.drawFunc_
		({
			var lineXs, lineYs;
			
			if(isRefreshing,
			{
				# lineXs, lineYs = rotFunc.value;
				Pen.color = Color.black;
				Pen.line(lineXs[0]@lineYs[0] + mouseXY, lineXs[1]@lineYs[1] + mouseXY);
				Pen.stroke;
			});
			
			this.calcNewParams(mouseXY);
			
		});
		
		senseDistance = surfaceView.bounds.leftTop.dist(surfaceView.bounds.rightBottom).asInteger;
		senseCurve = Env([0, 0.05, 0.1, 0.9, 1], [3, 0.5, 0.85, 0.15], [0, 0, 0, -4]).asSignal(senseDistance).asArray.reverse;
		
		nPresetText = TextField(window, Rect(10, 10, 100, 20))
		.action_({ addButton.valueAction_(1); });
		
		addButton = Button(window, Rect(115, 10, 40, 20)).states_([["Add"]])
		.action_({ this.addPreset; });
		
		presetList = ListView(window, Rect(10, 40, 145, window.bounds.height - 40))
		//.mouseUpAction_({|view|  })
		.mouseDownAction_
		({|...args| 
		
			//args.postln;
			
			if(GUI.id == \swing, 
			{ 
				args[4].switch( 1, { args[4] = 0; }, 3, { args[4] = 1; }); //button bindings
				args[3].switch( 0, { args[3] = 256; }, 131072, { args[3] = 131330; }, 524288, { args[3] = 524576; }); //keyboard bindings
			});
			
			if((args[5] == 2) and: { presetList.items.size > 0; }, { this.loadPreset(args[0].value); });
			if((args[4] == 1) and: { presetList.items.size > 0; },
			{
				menuList.remove;
				menuList = ListView(window, Rect(args[1]+args[0].bounds.left, args[2]+args[0].bounds.top, 100, 40)).background_(Color.gray(0.85))
				.items_(["Delete", "Cancel"])
				.mouseUpAction_
				({|menu|
				
					if(menu.value == 0, { this.removeSelectedPreset; });
					menuList.visible = false;
				});
			});
		})
		.keyDownAction_
		({|...args|
			
			if((args[3] == 127) and: { args[0].items.size > 0; }, //if del pressed and items not empty
			{
				this.removeSelectedPreset;
			});
		});
		
		saveGets =
		[
			{ presetList.items; },
			{ curPresets; },
			{ canvasItems.collect({|item| [item.name, item.view.bounds, item.color] }); }
		]; 
		
		saveSets =
		[
			{|argg| presetList.items_(argg); },
			{|argg| curPresets = argg; },
			{|argg|
				
				argg.do
				({|item|
				
					canvasItems.add(HrPresetMorphItemView.new(this, item[1].left@item[1].top, item[0]).color_(item[2]));
				})		
			}
		];
		
		//modGets.put(\surfaceX, { lastCalcedXY.x / surfaceView.bounds.width; });
		//modGets.put(\surfaceY, { lastCalcedXY.y / surfaceView.bounds.height; });
		
		//modSets.put(\surfaceX, {|argg| argg = argg * surfaceView.bounds.width; { this.calcNewParams(argg@lastCalcedXY.y, true); }.defer; });
		//modSets.put(\surfaceY, {|argg| argg = argg * surfaceView.bounds.height; { this.calcNewParams(lastCalcedXY.x@argg, true); }.defer; });
		
			
	}
	
	calcNewParams
	{|argXY|
	
		var multiplier = 0, tempSum = 0;
		
		if((canvasItems.size > 0) and: { isRefreshing; },
		{
			canvasItems.do
			({|canItem|
			
				var tempDist;
				tempDist = canItem.view.bounds.origin.dist(argXY);
				tempSum = tempSum + ((1 - (tempDist/senseDistance)) * senseCurve[tempDist]);
			});
			
			multiplier = tempSum.reciprocal;
			
			canvasItems.do
			({|canItem|
			
				curPresets.at(canItem.name).keys.do
				({|plugID|
				
					curPresets.at(canItem.name).at(plugID).keys.do
					({|plugParam|
					
						var nextVal = 0;
						var mySavedVal = curPresets.at(canItem.name).at(plugID).at(plugParam);
		
						canvasItems.do
						({|inCItem|
						
							var farSavedVal = curPresets.at(inCItem.name).at(plugID); //hold the plug first, and see if it exists
							var tempDist = inCItem.view.bounds.origin.dist(argXY);
							
							if(farSavedVal.notNil,
							{
								farSavedVal = farSavedVal.at(plugParam);
							},
							{
								farSavedVal = mySavedVal; //drop to locally saved value
							});
							
							nextVal = nextVal +
								(farSavedVal * ((1 - (tempDist/senseDistance)) * senseCurve[tempDist]) * multiplier)
						});
						
						if(parentApp.idPlugDict.at(plugID).modGets.at(plugParam).value != nextVal,
						{//send only if value changes...
						
							parentApp.idPlugDict.at(plugID).modSets.at(plugParam).value(nextVal);
						});
						
					});
				})
			});
		});
	}
	
	addPreset
	{
		var tempPresetName, nameCounter = 1, tempPreset, tempPresetItem,
		addFunc, isReplacing = false;
		var tempWin;
		
		tempPresetName = nPresetText.string.asSymbol;	
		
		addFunc = 
		{
	
			if(isReplacing == false,
			{
				while({presetList.items.detect({|item| item == tempPresetName; }).notNil} , 
				{
					tempPresetName = (nPresetText.string + "(%)".format(nameCounter.asString)).asSymbol;
					nameCounter = nameCounter + 1;
				});
				
				//add only if not replacing
				presetList.items = presetList.items.add(tempPresetName);
			});
			
			
			presetList.refresh;
			
			tempPreset = Dictionary.new;
			
			parentApp.alivePlugs.do
			({|plugItem|
				
				if(plugItem.modGets.size != 0,
				{
					tempPresetItem = Dictionary.new;
					plugItem.modGets.keys.do
					({|keyItem|
						tempPresetItem.put(keyItem, plugItem.modGets.at(keyItem).value);
					});
					tempPreset.put(plugItem.uniqueID, tempPresetItem);
					
				});
			});
			
			curPresets.put(tempPresetName, tempPreset);
		};
		
		if(presetList.items.detect({|item| item == tempPresetName; }).notNil,
		{
			
			tempWin = Window("Replace preset?", Rect(400, 400, 190, 85), resizable: false);
			StaticText(tempWin, Rect(0, 15, 190, 20)).string_("Are you sure?").align_(\center);
			Button(tempWin, Rect(10, 50, 80, 20)).states_([["Replace"]]).action_({ isReplacing = true; addFunc.value; tempWin.close; });
			Button(tempWin, Rect(100, 50, 80, 20)).states_([["Create new"]]).action_({ isReplacing = false; addFunc.value; tempWin.close; }).focus(true);
		
			tempWin.front;
			
		}, { addFunc.value; });	
		
	}
	
	loadPreset
	{|argIndex|
	
		var tempName = presetList.items[argIndex];
		
		curPresets.at(tempName).keys.do
		({|plugID|
		
			curPresets.at(tempName).at(plugID).keys.do
			({|param|
			
				parentApp.idPlugDict.at(plugID).modSets.at(param).value(curPresets.at(tempName).at(plugID).at(param));
			});
		});
	}
	
	removeSelectedPreset
	{
		var temp;
		curPresets.removeAt(presetList.value);
		temp = canvasItems.detectIndex({|item| item.name == presetList.items[presetList.value] });
		if(temp.notNil,
		{
			canvasItems[temp].removeSelf;
			canvasItems.removeAt(temp);
			surfaceView.refresh;
		});
		
		presetList.items.removeAt(presetList.value);
		presetList.items = presetList.items; //hack? to .refresh does nothing...

	}
	
	removeFromSurface
	{|argSurfaceItem|
	
		canvasItems.remove(argSurfaceItem);
		argSurfaceItem.removeSelf;
		surfaceView.refresh;
	}
	
	notifyPlugKill
	{|argPlugin|
	
		var tempID = argPlugin.uniqueID;
		
		curPresets.do
		({|preset|
			
			preset.removeAt(tempID);
		});
	}
	
	cleanUp
	{
	}
	
	updateBusConnections
	{
	}
}

HrPresetMorphItemView
{

	var caller, <name, mouseXY, <view, <color, <menuList;
	
	*new
	{|argParent, argCoords, argName|
	
		^super.new.init(argParent, argCoords, argName);
	}
	
	init
	{|argParent, argCoords, argName|
	
		name = argName;
		color = Color.rand;
		caller = argParent;
		
		//menuList = ListView(argParent.compositeBack, Rect(0, 0, 100, 40)).background_(Color.gray(0.85)).visible_(false);
		
		view = UserView(argParent.compositeBack, Rect(argCoords.x, argCoords.y, 5 + (name.asString.size*6.5), 20))
		.focusColor_(Color.gray(alpha: 0))
		.background_(color)
		.mouseDownAction_
		({|...args|
		
			if(GUI.id == \swing, 
			{ 
				args[4].switch( 1, { args[4] = 0; }, 3, { args[4] = 1; }); //button bindings
				args[3].switch( 0, { args[3] = 256; }, 131072, { args[3] = 131330; }, 524288, { args[3] = 524576; }); //keyboard bindings
			});
			
			mouseXY = args[0].bounds.origin + (args[1]@args[2]);
			
			args[5].switch
			(
				2, //if double clicked, load the preset
				{
					caller.loadPreset(caller.presetList.items.indexOf(name.asSymbol));
				}
				
			);
			
			args[4].switch
			(
				1, //if right clicked
				{
					argParent.menuList.remove;
					argParent.menuList = ListView(argParent.compositeBack, Rect(mouseXY.x - argParent.compositeBack.bounds.left, mouseXY.y - argParent.compositeBack.bounds.top, 150, 40)).background_(Color.gray(0.85))
					.items_(["Remove from surface", "Cancel"])
					.mouseUpAction_
					({|menu|
					
						if(menu.value == 0, { caller.removeFromSurface(this); });
						argParent.menuList.visible = false;
					});
					
				}
			);			
		})
		.mouseMoveAction_
		({|...args|
		
			var tempXY = args[0].bounds.origin + (args[1]@args[2]);
			var delta = tempXY - mouseXY;
			mouseXY = tempXY;
			//args[0].bounds = args[0].bounds.postln.moveBy(delta.x, delta.y);
			args[0].bounds = 
			Rect
			(
				(args[0].bounds.left + delta.x).clip(0, caller.surfaceView.bounds.width - args[0].bounds.width),
				(args[0].bounds.top + delta.y).clip(0, caller.surfaceView.bounds.height - 20),
				args[0].bounds.width,
				args[0].bounds.height
			);
		
		})
		.drawFunc_
		({|view|
			
			Pen.color = Color.black;
			Pen.font = Font(Font.defaultMonoFace, 10);
			Pen.stringAtPoint(name.asString, 3@3);
		});
	}
	
	color_
	{|argColor|
	
		color = argColor;
		view.background_(color);
	}
	
	removeSelf
	{
		view.remove;
	}
}