HadronCanvas
{
	var <cWin, <cView, parentApp, <oldBounds, isHidden, <>isSelectingTarget,
	<>currentSource, cablePointsBucket, anchorMouseXY, draggingMouseXY, isDragSelecting,
	<>selectedItems;
	
	*new
	{|argParentApp|
		^super.new.init(argParentApp);
	}
	
	init
	{|argParentApp|
	
		parentApp = argParentApp;
		isHidden = true;
		isDragSelecting = false;
		
		anchorMouseXY = 0@0;
		
		cablePointsBucket = List.new;
		selectedItems = List.new;
		
		isSelectingTarget = false; //true when an object is selected with shift as source.
		currentSource = nil;
		
		oldBounds = Rect(10, Window.screenBounds.height - 550, 800, 500);
		
		cWin = Window("Canvas", oldBounds)
		.userCanClose_(false)
		.acceptsMouseOver_(true);
		
		cWin.view.keyDownAction_({|...args| this.handleKeys(*args); });
		
		
		
		cView = UserView(cWin, cWin.view.bounds)
		.background_(Color.white)
		.resize_(5)
		.drawFunc_
		({
			var tempRect;
			var tempDist;
			
			Pen.color_(Color.black);
			if(cablePointsBucket.size > 0,
			{
				cablePointsBucket.do
				({|item|
				
					//Pen.line(item[0], item[1]);
					tempDist = (item[0].y - item[1].y).abs.clip(0, 50);
					Pen.moveTo(item[0]);
					Pen.curveTo(item[1], item[0].x@(item[0].y+tempDist), item[1].x@(item[1].y-tempDist));
				});
				
				Pen.stroke;
				//cablePointsBucket = List.new;
			});
			
			if(isDragSelecting,
			{
				tempRect = Rect.fromPoints(anchorMouseXY.x@anchorMouseXY.y, draggingMouseXY.x@draggingMouseXY.y);
				parentApp.alivePlugs.do({|item| item.boundCanvasItem.checkInsideRect(tempRect); });
				Pen.addRect(tempRect);
				Pen.stroke;
			});
			
			
		})
		.mouseDownAction_
		({
			arg view, x, y, mod, button, cCount;
			var tempField;
			var tempPlugStr;
			var tempID = nil;
			
			//swingosc has different mouse button bindings.
			if(GUI.id == \swing, { button.switch( 1, { button = 0; }, 3, { button = 1; }); });
			//[view, x, y, mod, button, cCount].postln;
			button.switch
			(
				1,
				{
					parentApp.displayStatus("Enter plugin name, (optional) followed by ID name in paranthesis.", 0);
					cWin.front;
					tempField = TextField(cWin, Rect(x, y, 200, 20))
					.focus(true)
					.keyDownAction_({ 1; }) //do not bubble up to the parent view.
					.action_
					{|field|
					
						if(field.value == "", { tempField.remove; },
						{
							tempPlugStr = field.value.split($ ).asList;
							block
							{|break|
								tempPlugStr.do
								({|item, cnt|
								
									if(item[0].asSymbol == '(', //)
									{
										tempID = item.replace("(", "").replace(")", "");
										tempPlugStr.removeAt(cnt);
										//break.value;
									});
								});
							};
							
							if((Hadron.plugins ++ HadronPlugin.plugins).includes(tempPlugStr[0].interpret),
							{
								if(tempPlugStr.size == 1,
								{
									parentApp.prAddPlugin(tempPlugStr[0].interpret, tempID ? "unnamed", nil, nil, x@y);
								},
								{
									
									parentApp.prAddPlugin(tempPlugStr[0].interpret, tempID ? "unnamed", nil, tempPlugStr[1..].asArray, x@y);
								
								});
								tempField.remove;
								parentApp.displayStatus("Right click on canvas to add plugins. Shift+click on a plugin to make connections", 0);
							},
							{
								parentApp.displayStatus("Plugin"+tempPlugStr[0]+"mistyped or not installed...", -1);
							});
						});
					};
				},
				0,
				{
					anchorMouseXY = x@y;
					//selectedItems.size.postln;
					selectedItems.size.do({|cnt| selectedItems[0].amUnselected; }); //amUnselected method deletes items
					if(isSelectingTarget, 
					{ 
						isSelectingTarget = false; 
						currentSource.boundCanvasItem.cancelSource; 
						currentSource = nil; 
					});
				}
			);
		})
		.mouseMoveAction_
		({
			arg view, x, y;
			isDragSelecting = true;
			draggingMouseXY = x@y;
			cView.refresh;
		})
		.mouseUpAction_
		({
			isDragSelecting = false;
			cView.refresh;
		})
		.keyDownAction_ //some of are manually bubbled up from child views
		({|view, char, modifiers, unicode, keycode|
		
			//[view, char, modifiers, unicode, keycode].postln;
			this.handleKeys(view, char, modifiers, unicode, keycode);
			
		});
		
		//Hacky hiding after the inner view is created, or inner view bounds get messed up in SwingOSC.
		cWin.bounds_(Rect(0, 0, 0, 0)); //hidden by default.
		//cWin.front;
		if(GUI.id == \swing, { cWin.visible_(false); });		
	}
	
	hideWin
	{
		if(isHidden.not,
		{
			isHidden = true;
			oldBounds = cWin.bounds;
			parentApp.displayStatus("READY.", 0);
			cWin.bounds = Rect(0, 0, 0, 0);
			if(GUI.id == \swing, { cWin.visible_(false); });
			parentApp.canvasButton.value_(0);
			parentApp.win.front;
		});
	}
	
	showWin
	{
		if(isHidden,
		{
			isHidden = false;
			if(GUI.id == \swing, { cWin.visible_(true); });
			cWin.bounds = oldBounds;
			parentApp.canvasButton.value_(1);
			parentApp.displayStatus("Right click on canvas to add plugins. Shift+click on a plugin to make connections", 0);
			cWin.refresh;
			cWin.front;
		});
	}
	
	drawCables
	{
		var sBound, tBound; //sourceview, targetview
		cablePointsBucket = List.new;
		
		parentApp.alivePlugs.do
		({|item|
		
			sBound = item.boundCanvasItem;
			item.outConnections.do
			({|conItem, count|
			
				if(conItem != [nil, nil],
				{
					
					tBound = conItem[0].boundCanvasItem;
					cablePointsBucket.add
					(
						[
							((sBound.objView.bounds.left + sBound.outPortBlobs[count].left+3))@
							(sBound.objView.bounds.top+20),
							((tBound.objView.bounds.left + tBound.inPortBlobs[conItem[1]].left+3))@
							(tBound.objView.bounds.top)
						]
					);							
				});	
			})
		});
		cView.refresh;
	}
	
	handleKeys
	{|view, char, modifiers, unicode, keycode|
	
		var coord1, coord2, topItem;
		var schedOldText;
		var tempNewIdent, cleanIdent, currentIdents, appendIndex, duped; //for duplicate operation
	
		//[view, char, modifiers, unicode, keycode].postln;
		
		//SwingOSC has different keyboard codes
		if(GUI.id == \swing, 
		{
			modifiers.switch( 0, { modifiers = 256; }, 131072, { modifiers = 131330; });
		});
		
		if((char == $D) and: { modifiers == 131330 }, //if shift+d, duplicate
		{
			duped = List.new;
			
			selectedItems.do
			({|item|
				
				appendIndex = 1;
				currentIdents = parentApp.alivePlugs.collect({|aPlug| [aPlug.class, aPlug.ident.asSymbol]; });
				cleanIdent = item.parentPlugin.ident.asString.copy;
				if(cleanIdent.find("_copy").notNil, { cleanIdent = cleanIdent[..(cleanIdent.find("_copy") - 1)]});
				
				tempNewIdent = (cleanIdent ++ "_copy" ++ appendIndex).asSymbol;
				
				while({ currentIdents.detectIndex({|cItem| cItem == [item.parentPlugin.class, tempNewIdent]}).notNil; }, 
				{
					appendIndex = appendIndex + 1;
					tempNewIdent = (cleanIdent ++ "_copy" ++ appendIndex).asSymbol;
				}); 
				duped.add
				(
					parentApp.prAddPlugin
					(
						item.parentPlugin.class, 
						tempNewIdent, 
						nil, 
						item.parentPlugin.extraArgs, 
						(item.objView.bounds.moveBy(30, 30).left@item.objView.bounds.moveBy(30, 30).top)
					)
				);
				
			});
			
			selectedItems.size.do({|cnt| selectedItems[0].amUnselected; }); //amUnselected method deletes items
			duped.do({|item| item.boundCanvasItem.amSelected});
			^this;
		});
		if(unicode == 127 and: { this.selectedItems.size > 0 }, //if delete pressed, and there are selected items...
		{
			this.deleteSelected;
			^this;
		});	
		
		if(char == $c and: { selectedItems.size == 2 } and: { modifiers == 256 },
		{
			coord1 = selectedItems[0].objView.bounds.top;
			coord2 = selectedItems[1].objView.bounds.top;
			topItem = if(coord1 < coord2, { 0; }, { 1; });
			
			HadronConManager(selectedItems[topItem].parentPlugin, selectedItems[1-topItem].parentPlugin);
			^this;
		});
		
		if(char == $c and: { selectedItems.size == 1 } and: { modifiers == 256 },
		{	
			HadronConManager(selectedItems[0].parentPlugin, selectedItems[0].parentPlugin);
			^this;
		});
		
		if(char == $o, 
		{ 
			parentApp.reorderGraph;
			schedOldText = parentApp.statusStString.string.copy;
			parentApp.displayStatus("Ordered nodes based on vertical alignment on canvas...", 1);
			AppClock.sched(4, { parentApp.displayStatus(schedOldText, 0); nil; });
			^this; 
		});
		
		if(char == $q, { Server.default.queryAllNodes; });
		
		if(char == $h, { this.hideWin; });
	}
	
	deleteSelected
	{
		var tempWin; //can be modal but meh. does SwingOSC have it?
		tempWin = Window("Are you Sure?", Rect(400, 400, 190, 100), resizable: false);
		StaticText(tempWin, Rect(0, 10, 190, 20)).string_(selectedItems.size.asString+"instance(s) will be killed!").align_(\center);
		Button(tempWin, Rect(10, 60, 80, 20)).states_([["Ok"]]).action_
		({ 
			selectedItems.size.do
			({
				selectedItems[0].signalKill;
			});
			this.drawCables;
			tempWin.close; 
			
		});
		Button(tempWin, Rect(100, 60, 80, 20)).states_([["Cancel"]]).action_({ tempWin.close; });
		
		tempWin.front;
		
	}
}