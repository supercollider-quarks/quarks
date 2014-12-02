HadronCanvasItem
{
	var <parentCanvas, <parentPlugin, <objView, <inPortBlobs, <outPortBlobs,
	oldMouseXY, conMan, isSelected, isOnMouseMove, justSelected;
	
	*new
	{|argParentCanvas, argParentPlugin, argX, argY|
	
		^super.new.init(argParentCanvas, argParentPlugin, argX, argY);
	}
	
	init
	{|argParentCanvas, argParentPlugin, argX, argY|
	
		var numMaxPorts;
		parentCanvas = argParentCanvas;
		parentPlugin = argParentPlugin;
		inPortBlobs = List.new;
		outPortBlobs = List.new;
		//mouseXY = argX@argY;
		oldMouseXY = argX@argY;
		isSelected = false;
		isOnMouseMove = false;
		justSelected = false;
		
		numMaxPorts = max(argParentPlugin.inBusses.size, argParentPlugin.outBusses.size);
		
		argParentPlugin.inBusses.do
		({|bus, count|
		
			inPortBlobs.add(Rect(5 + (count*10), 0, 5, 3));
		});
		
		argParentPlugin.outBusses.do
		({|bus, count|
		
			outPortBlobs.add(Rect(5 + (count*10), 17, 5, 3));
		});
		
		objView = 
		UserView
		(
			parentCanvas.cWin, 
			Rect
			(
				argX, argY, 
				max
				(
					max(100, (numMaxPorts * 10) + 10),
					((this.class.asString.size + parentPlugin.extraArgs.asString.size) * 6) + 10
				), 
			20)
		)
		.background_(Color.gray)
		.focusColor_(Color(alpha: 0))
		.drawFunc_
		({|view|
		
			var tempString;
			
			Pen.font = Font("Helvetica", 10);
			tempString = parentPlugin.class.asString;
			if(parentPlugin.extraArgs.notNil, { tempString = tempString + parentPlugin.extraArgs.asString; });
			Pen.stringAtPoint(tempString, 5@3);
			inPortBlobs.do
			({|blob|
				
				Pen.color = Color.black;
				Pen.addRect(blob);
				Pen.fill;
			});
			
			outPortBlobs.do
			({|blob|
				
				Pen.color = Color.black;
				Pen.addRect(blob);
				Pen.fill;
			});
		})
		.mouseDownAction_
		({|...args|
		
			parentPlugin.parentApp.isDirty = true;
			//args.postln;
			//swingosc has different mouse button and keymod bindings
			if(GUI.id == \swing, 
			{ 
				args[4].switch( 1, { args[4] = 0; }, 3, { args[4] = 1; }); //button bindings
				args[3].switch( 0, { args[3] = 256; }, 131072, { args[3] = 131330; }, 524288, { args[3] = 524576; }); //keyboard bindings
				
			});
			
			args[5].switch
			(
				oldMouseXY = args[0].bounds.origin + (args[1]@args[2]);
				2, //if double clicked
				{ parentPlugin.showWindow; },
				1, //on single click
				{ 
					args[3].switch
					(
						131330, //if shift pressed
						{
							if(parentCanvas.isSelectingTarget,
							{
								conMan = HadronConManager.new(parentCanvas.currentSource, parentPlugin);
								parentCanvas.isSelectingTarget = false;
								parentCanvas.currentSource.boundCanvasItem.objView.background = Color.gray;
								parentCanvas.currentSource = nil;
							},
							{
								parentCanvas.isSelectingTarget = true;
								parentCanvas.currentSource = parentPlugin;
								objView.background = Color.green;
								parentPlugin.parentApp.displayStatus("Source set, Shift+click on target plugin.", 1);
							});
						},
						256, //no modifier keys
						{
							
							this.amSelected;
							isOnMouseMove = false;
							{
								0.1.wait;
								if(isOnMouseMove.not, //if that is a double click, isOnMouseMove will be true and we won't unselect other stuff.
								{ 
									parentCanvas.selectedItems.remove(this); //get yourself out of the way
									parentCanvas.selectedItems.size.do({ parentCanvas.selectedItems[0].amUnselected; });
									parentCanvas.selectedItems.add(this);
									
									
								});
							}.fork(AppClock);
							parentPlugin.parentApp.displayStatus(parentPlugin.helpString, 0);
						},
						524576, //if alt pressed
						{
							this.amSelected;
						}
					); 
				}
			)
		})
		.mouseMoveAction_
		({|...args|
			
			var tempXY = args[0].bounds.origin + (args[1]@args[2]);
			var delta = tempXY - oldMouseXY;
			parentPlugin.parentApp.isDirty = true;
			isOnMouseMove = true;
			if(justSelected,
			{
				parentCanvas.selectedItems.remove(this); //get yourself out of the way
				parentCanvas.selectedItems.size.do({ parentCanvas.selectedItems[0].amUnselected; });
				parentCanvas.selectedItems.add(this);
				justSelected = false;
			});
			//args.postln;
			oldMouseXY = tempXY;
			parentCanvas.selectedItems.do(_.moveBlob(delta));
			
			argParentCanvas.drawCables;
		})
		.keyDownAction_
		({|view, char, modifiers, unicode, keycode|
			
			parentPlugin.parentApp.isDirty = true;
			parentCanvas.handleKeys(view, char, modifiers, unicode, keycode);
		});
	}
	
	moveBlob
	{|argDeltaXY, argMX, argMY|
	
		objView.bounds = objView.bounds.moveBy(argDeltaXY.x, argDeltaXY.y);
		//oldMouseXY = objView.bounds.left@objView.bounds.top;
	}
	checkInsideRect
	{|argSelRect|
	
		if(argSelRect.containsPoint(objView.bounds.leftTop) or: { argSelRect.containsPoint(objView.bounds.rightBottom) },
		{
			this.amSelected;
		},
		{
			this.amUnselected;
		});
	}
	
	amSelected
	{
		if(isSelected.not,
		{
			isSelected = true;
			justSelected = true;
			{ 0.1.wait; justSelected = false; }.fork(AppClock);
			objView.background = Color.gray(0.3);
			parentCanvas.selectedItems.add(this);
		});
	}
	
	amUnselected
	{
		if(isSelected,
		{
			isSelected = false;
			justSelected = false;
			objView.background = Color.gray;
			parentCanvas.selectedItems.remove(this);
		});
	}
	
	cancelSource
	{
		objView.background = Color.gray;
	}
	
	removeFromCanvas
	{
		parentCanvas.selectedItems.remove(this);
		if(parentCanvas.currentSource == parentPlugin,
		{
			parentCanvas.currentSource = nil;
			parentCanvas.isSelectingTarget = false;
		});
		parentCanvas.drawCables;
		objView.remove;
	}
	
	
	signalKill
	{
		parentPlugin.selfDestruct;
	}
}