Hadron
{
	classvar <>plugins, <>loadDelay = 2;
	var <win, <alivePlugs, <blackholeBus, <aliveMenu, <idPlugDict, <canvasObj,
	statusView, <statusStString, <>isDirty, <canvasButton, instWin;
	
	
	*initClass
	{
		this.plugins = 
		[
			HrADC,
			HrDAC,
			HrStereoMixer,
			HrStereoSplitter,
			HrSimpleModulator,
			HrDIYSynth,
			HrWrapSynth		
		]; //default plugins
	}
	
	*new
	{
		if(Main.version.asFloat < 3.31, { "Hadron requires SuperCollider version 3.3.1 or higher.".error; this.halt; });
		if((GUI.id == \swing) and: { SwingOSC.version < 0.62 }, 
			{ "Hadron requires SwingOSC version 0.62 or higher.".error; this.halt; });
		^super.new.init;
	}
	
	init
	{
		
		Server.default.waitForBoot
		{
			
			alivePlugs = List.new;
			idPlugDict = Dictionary.new;
			isDirty = false;
			
			canvasObj = HadronCanvas.new(this);
			
			blackholeBus = Bus.audio(Server.default, 1);
			win = Window("Hadron", Rect(300, 40, 675, 70), resizable: false).userCanClose_(false);
			
			Button(win, Rect(10, 15, 85, 20))
			.states_
			([
				["New Inst.", Color.black, Color(0.5, 0.7, 0.5)], 
			])
			.action_
			({
				this.prShowNewInstDialog;
			});
			
			
			aliveMenu = PopUpMenu(win, Rect(100, 15, 200, 20))
			.action_({|menu| alivePlugs[menu.value].showWindow; });
			
			Button(win, Rect(310, 15, 50, 20))
			.states_([["Save"]])
			.action_({ this.prShowSave; });
			
			Button(win, Rect(370, 15, 50, 20))
			.states_([["Load"]])
			.action_({ this.prShowLoad; });
			
			Button(win, Rect(310, 15, 50, 20))
			.states_([["Save"]])
			.action_({ this.prShowSave; });
			
			HrButton(win, Rect(430, 15, 50, 20))
			.states_([["Collide", Color.black, Color(0.7, 0.7, 1)]])
			.action_({ alivePlugs.do(_.collide); });
			
			Button(win, Rect(490, 15, 50, 20))
			.states_([["Exit"]])
			.action_
			({
				var tempWin; //can be modal but meh. does SwingOSC have it?
				tempWin = Window("Are you sure?", Rect(400, 400, 190, 85), resizable: false);
				StaticText(tempWin, Rect(0, 15, 190, 20)).string_("Are you sure?").align_(\center);
				Button(tempWin, Rect(10, 50, 80, 20)).states_([["Ok"]]).action_({ tempWin.close; this.graceExit; });
				Button(tempWin, Rect(100, 50, 80, 20)).states_([["Cancel"]]).action_({ tempWin.close; }).focus(true);
			
				tempWin.front;
			});
			
			canvasButton = Button(win, Rect(560, 15, 85, 20))
			.states_
			([
				["Show Canvas", Color.black, Color(0.5, 0.7, 0.5)], 
				["Hide Canvas", Color.white, Color(0.7, 0.5, 0.5)]
			])
			.action_
			({
				arg state;
				state.value.switch
				(
					1, { canvasObj.showWin; },
					0, { canvasObj.hideWin; }
				);
			});
			
			Button(win, Rect(650, 15, 15, 20)).states_([["?"]]).action_({ Hadron.openHelpFile; });
			
			statusView = CompositeView(win, Rect(0, win.view.bounds.height - 20, win.view.bounds.width, 18)).background_(Color.gray(0.8));
			statusStString = StaticText(statusView, Rect(10, 2, win.view.bounds.width, 15)).string_("READY.");
			
			
			win.front;
		}
	}
	
	prShowSave
	{
		HadronStateSave(this).showSaveDialog;
	}
	
	prShowLoad
	{
	
		var tempWin; //can be modal but meh. does SwingOSC have it?
		if(isDirty,
		{
			tempWin = Window("Are you sure?", Rect(400, 400, 190, 100), resizable: false);
			StaticText(tempWin, Rect(0, 10, 190, 20)).string_("Current project will be closed,").align_(\center);
			StaticText(tempWin, Rect(0, 30, 190, 20)).string_("Are you sure?").align_(\center).focus(true);
			Button(tempWin, Rect(10, 60, 80, 20)).states_([["Ok"]])
			.action_
			({ 
				tempWin.close;
				HadronStateLoad(this).showLoad;
				isDirty = false;
			});
			Button(tempWin, Rect(100, 60, 80, 20)).states_([["Cancel"]]).action_({ tempWin.close; }).focus(true);
	
			tempWin.front;
		},
		{
			HadronStateLoad(this).showLoad;
			//isDirty = false; //handled inside HadronStateLoad
		});
		
	}
	
	prGiveUniqueId
	{
		var tempRand = 65536.rand;
		var tempPool = alivePlugs.collect({|item| item.uniqueID; });
		
		while({ tempPool.detectIndex({|item| item == tempRand; }).notNil },
		{
			tempRand = 65536.rand;
		});
		
		^tempRand;
	}
	
	prAddPlugin
	{|argPlug, argIdent, argUniqueID, extraArgs, argCanvasXY|
	
		var tempHolder = argPlug.new(this, argIdent, argUniqueID, extraArgs, argCanvasXY);
		isDirty = true;
		alivePlugs.add(tempHolder);
		idPlugDict.put(tempHolder.uniqueID, tempHolder);
		this.prActiveMenuUpdate;
		alivePlugs.do(_.notifyPlugAdd(tempHolder));
		
		this.reorderGraph;
		canvasObj.cWin.front;
		^tempHolder;
	}
	
	prShowNewInstDialog
	{|argOptionalBound|
	
		var okButton;
		var tempWin = Window("Select Instrument", argOptionalBound ? Rect(200, 200, 200, 130), resizable: false);
		var tempMenu = PopUpMenu(tempWin, Rect(10, 10, 180, 20))
		.items_(Hadron.plugins.collect({|item| item.asString; }) ++ HadronPlugin.plugins.collect(_.asString));
		
		var tempIdent = TextField(tempWin, Rect(90, 40, 80, 20)).action_({ okButton.valueAction_(1); });
		var tempArgs = TextField(tempWin, Rect(90, 70, 80, 20)).action_({ okButton.valueAction_(1); });
		
		instWin = tempWin;
		
		StaticText(tempWin, Rect(10, 40, 80, 20)).string_("Ident name:");
		StaticText(tempWin, Rect(10, 70, 80, 20)).string_("Extra Args:");
		
		okButton = Button(tempWin, Rect(10, 100, 80, 20))
		.focus(true)
		.states_([["Ok"]])
		.action_
		({
			this.prAddPlugin
			(
				tempMenu.items.at(tempMenu.value).interpret, 
				if(tempIdent.string.size == 0, { "unnamed"; }, { tempIdent.string; }),
				nil,
				if(tempArgs.string.size == 0, { nil; }, { tempArgs.string.split($ ); }),
				100@100
			);
			
			this.prShowNewInstDialog(tempWin.bounds); //reshow until closed.
			tempWin.close;
		});
		
		Button(tempWin, Rect(110, 100, 80, 20))
		.states_([["Close"]])
		.action_
		({
			tempWin.close;
		});
		
		tempWin.front;
	}
	
	prActiveMenuUpdate
	{
		aliveMenu.items = alivePlugs.collect({|item| item.class.asString + item.ident; });
	}
	
	displayStatus
	{|argString, statusMood| //statusMood is -1: error, 0: neutral, 1: success

		statusStString.remove;
		statusStString = StaticText(statusView, Rect(10, 2, win.view.bounds.width, 15)).string_(argString);
		statusMood.switch
		(
			-1,
			{//error text
				
				win.front;				
				{
					statusView.background_(Color(1, 0.2, 0.2));
					4.wait;
					statusView.background_(Color.gray(0.8));
				}.fork(AppClock);
			},
			0,
			{//neutral
				//nothing
			},
			1,
			{//success text
				{
					
					statusView.background_(Color(0.2, 1, 0.2));
					4.wait;
					if(win.isClosed.not,
					{
						statusView.background_(Color.gray(0.8));
					});
				}.fork(AppClock);
			}
		);
		
	}
	
	reorderGraph
	{//lame ordering based on vertical alignment on canvas. effective though...
	
		var yOrder;
		
		if(alivePlugs.size > 0,
		{
			yOrder = alivePlugs.collect({|item| item.boundCanvasItem.objView.bounds.top; }).order.order;
			
			(yOrder.size - 1).do
			({|cnt|
				
				alivePlugs[yOrder.indexOf(cnt+1)].group.moveAfter(alivePlugs[yOrder.indexOf(cnt)].group);
			});
		
		});
		//Server.default.queryAllNodes;
	}
	
	graceExit
	{
		canvasObj.cWin.close;
		if(instWin.notNil, { if(instWin.isClosed == false, { instWin.close; }); });
		alivePlugs.size.do({ alivePlugs[0].selfDestruct; });
		win.close;
	}
}