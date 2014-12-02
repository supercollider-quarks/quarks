HadronStateLoad
{
	var parentApp, loadStage;
	
	*new
	{|argParentApp|
		
		^super.new.init(argParentApp);
	}
	
	init
	{|argParentApp|
	
		parentApp = argParentApp;
		loadStage = 0;
	}
	
	showLoad
	{
		File.openDialog("", {|aFile| parentApp.alivePlugs.size.do({ parentApp.alivePlugs[0].selfDestruct; }); this.loadState(aFile); });
	}
	
	loadState
	{|argFile|
		
		var contents;
		var tempFile = File(argFile, "r");
		
		contents = tempFile.readAllString().split($\n);
		tempFile.close;
		
		
		
		
		{//begin fork
		contents.do
		({|item|
			
			if(item == "?EndPlugs", { loadStage = 3; Hadron.loadDelay.wait; });
			if(item == "?EndConnections", { loadStage = 5; });
			if(item == "?EndPlugParams", { loadStage = 7; });
			if(item == "?EndSave", { loadStage = 8; });
			
			if(loadStage == 2, 
			{ 
				//item.postln; 
				item = item.split(31.asAscii);
				parentApp.prAddPlugin(item[0].interpret, item[1], item[2].asInteger, item[3].interpret, item[4].interpret);
				
				parentApp.idPlugDict.at(item[2].asInteger)
					.outerWindow.bounds = item[5].interpret;
				
				parentApp.idPlugDict.at(item[2].asInteger)
					.oldWinBounds = item[6].interpret;
				
				parentApp.idPlugDict.at(item[2].asInteger).isHidden = item[7].interpret;
				
			});
			
			if(loadStage == 4,
			{
				item = item.split(31.asAscii);
				parentApp.idPlugDict.at(item[0].interpret).inConnections = 
					item[1].interpret.collect
					({|inItem| 
						if(inItem[0] != nil, 
						{ [parentApp.idPlugDict.at(inItem[0]), inItem[1]]},
						{ inItem; }); 
					});
				
				parentApp.idPlugDict.at(item[0].interpret).outConnections = 
					item[2].interpret.collect
					({|outItem| 
						if(outItem[0] != nil, 
						{ [parentApp.idPlugDict.at(outItem[0]), outItem[1]]},
						{ outItem; }); 
					});
			});
			
			if(loadStage == 5,
			{
				parentApp.alivePlugs.do({|plug| plug.wakeConnections; });
				parentApp.alivePlugs.do({|plug| plug.updateBusConnections; });
			});
			
			if(loadStage == 6,
			{
			
				item = item.split(31.asAscii);
				parentApp.idPlugDict.at(item[0].interpret).injectSaveValues(item[1..]);
			});
			
			if(loadStage == 7,
			{
				parentApp.alivePlugs.do(_.wakeFromLoad);
				parentApp.canvasObj.drawCables;
				parentApp.isDirty = false;
				parentApp.displayStatus("State loaded!", 1);
			});
			
			
			
			if(item == "?Hadron 1", { loadStage = 1; });
			if(item == "?StartPlugs", { loadStage = 2; });
			if(item == "?StartConnections", { loadStage = 4; });
			if(item == "?StartPlugParams", { loadStage = 6; });
			
		});
		
		}.fork(AppClock);
		
		
		
		
	}
	
	
	
}