HadronConManager
{
	var areArgsGood;
	
	*new
	{|argSourcePlug, argTargetPlug|
	
		^super.new.init(argSourcePlug, argTargetPlug);
	}
	
	init
	{|argSourcePlug, argTargetPlug|
	
		var win, field, preBakedStr = "", tempInIndexes = List.new, tempOutIndexes = List.new,
		isConnectedResponse, isSourceMono, isTargetMono, sourceFreeOut, targetFreeIn;
		
		isConnectedResponse = this.isConnectedBetween(argSourcePlug, argTargetPlug);
		isSourceMono = if(argSourcePlug.outConnections.size < 2, { true; }, { false; });
		isTargetMono = if(argTargetPlug.inConnections.size < 2, { true; }, { false; });
		
		sourceFreeOut = this.findStereoFreeSlot(argSourcePlug, \out);
		targetFreeIn = this.findStereoFreeSlot(argTargetPlug, \in);
		
		//connection prediction
		if(argSourcePlug === argTargetPlug, //if there is one source
		{
			if(isConnectedResponse[0] == true, //is the plugs outs are connected to its own inputs, show their disconnection string
			{
				isConnectedResponse[1].do
				({|item|
				
					preBakedStr = preBakedStr ++ (item + 1).asString ++ ",0,";
				});
				preBakedStr = preBakedStr[0..(preBakedStr.size - 2)];
			},
			{ //if connected to stg else, propose disconnectiong all
				argSourcePlug.outConnections.do
				({|item, cnt|
				
					if(item != [nil, nil],
					{
						tempOutIndexes.add(cnt+1);
					});
				});
				
				if(tempOutIndexes.size > 0,
				{
					tempOutIndexes.do
					({|item|
						
						preBakedStr = preBakedStr ++ item.asString ++ ",0,";
					});
				});
				
				preBakedStr = preBakedStr[0..(preBakedStr.size - 2)];
			});
		},
		{ //is source and target is different
		
			if(isConnectedResponse[0] == true, //if there are connections between source and target, propose disconnecting them
			{
			
				isConnectedResponse[1].do
				({|item|
				
					preBakedStr = preBakedStr ++ (item + 1).asString ++ ",0,";
				});
				preBakedStr = preBakedStr[0..(preBakedStr.size - 2)];
			},
			{ 
				//if there are no connections between source and target (then we are in this func), 
				//propose to connect first consecutively free two outlets
				//to first consecutively free two outlets, if there is only
				//one outlet of source, propose to connect it to the first odd
				//inlet of the target (also if the next one is free in the target)
				
				if((sourceFreeOut == [nil, nil]) or: { targetFreeIn == [nil, nil] },
				{
					preBakedStr = "";
				},
				{
					if(sourceFreeOut[1] == nil,
					{
						preBakedStr = (sourceFreeOut[0] + 1).asString ++ "," ++ (targetFreeIn[0] + 1).asString;
					},
					{
						if(targetFreeIn[1] == nil,
						{
							preBakedStr = (sourceFreeOut[0] + 1).asString ++ "," ++ (targetFreeIn[0] + 1).asString;
						},
						{
							preBakedStr = 
							(sourceFreeOut[0] + 1).asString ++ "," ++ (targetFreeIn[0] + 1).asString ++ "," ++
							(sourceFreeOut[1] + 1).asString ++ "," ++ (targetFreeIn[1] + 1).asString;
						});
					});
				});
			});
		});
		
		//create window
	
		win = Window("Comma separated \"in,out\" pairs:", Rect(400, 400, 300, 50));
		field = TextField(win, Rect(10, 20, 280, 20)).string_(preBakedStr)
		.action_
		({|fld|
			
			var pairs; 
			
			if(fld.value.size != 0,
			{			
				pairs = fld.value.replace(" ", "").split($,).collect(_.asInteger).clump(2);
				//check for bounds
				pairs.do
				({|item|
					//no need to subtract values for .size, they are already +1
					if(item[1] > argTargetPlug.inBusses.size or: { item[1] < 0 }, 
					{
						areArgsGood = false;
					});
					
					if(item[0] > argSourcePlug.outBusses.size or: { item[0] < 1 },
					{
						areArgsGood = false;
					});
					
				});
				
				if(areArgsGood,
				{
					pairs.do
					({|item|
					
						if(item[1] == 0,
						{
							argSourcePlug.setOutBus(nil, nil, item[0] - 1); //setOutBus knows how to handle this.
						},
						{
							argSourcePlug.setOutBus(argTargetPlug, item[1] - 1, item[0] - 1);
						});
						
					});
					win.close;
					argSourcePlug.parentApp.alivePlugs.do({|plug| if(plug.conWindow.isClosed.not, { plug.refreshConWindow; }) });
					argSourcePlug.parentApp.displayStatus("Right click on canvas to add plugins. Shift+click on a plugin to make connections", 0);
				},
				{
					argSourcePlug.parentApp.displayStatus("One of the indexes are out of bounds...", -1);
					areArgsGood = true;
				});
			});
			
		})
		.focus(true);
		
		areArgsGood = true; //ahh... good intentions...
		
		argSourcePlug.parentApp.displayStatus("Enter input output pairs, comma separated. Ex: 1,1,2,2", 0);
		win.front;		
	}
	
	isConnectedBetween
	{|argSource, argTarget|
	
		var connected = false;
		var conPoints = List.new;
		
		argSource.outConnections.do
		({|item, cnt|
		
			if(item[0] === argTarget,
			{
				connected = true;
				conPoints.add(cnt);
			});
		});
		
		^[connected, conPoints];
	}
	
	findStereoFreeSlot
	{|argPlug, argDirection| //direction is \in or \out
	
		var isLastFree = false;
	
		
		if(argDirection == \out,
		{
			if(argPlug.outConnections.size == 1,
			{
				if(argPlug.outConnections[0] == [nil, nil], { ^[0, nil]; }, { ^[nil, nil] });
			},
			{
				argPlug.outConnections.do
				({|item, cnt|
					
					if(item == [nil, nil],
					{
						if(isLastFree, { if(cnt.odd, { ^[cnt - 1, cnt] }); });
						isLastFree = true;
					},
					{
						isLastFree = false;
					});
				});
			});
			
		},
		{ //if direction is \in
		
			if(argPlug.inConnections.size == 1,
			{
				if(argPlug.inConnections[0] == [nil, nil], { ^[0, nil]; }, { ^[nil, nil] });
			},
			{
				argPlug.inConnections.do
				({|item, cnt|
					
					if(item == [nil, nil],
					{
						if(isLastFree, { if(cnt.odd, { ^[cnt - 1, cnt] }); });
						isLastFree = true;
					},
					{
						isLastFree = false;
					});
				});
			});
		});
		
		^[nil, nil];
	}
}