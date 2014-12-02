ModernLife
{
	var win, view, isPlaying, playRoutine, cellSideLen, currentGrid, nextGrid,
	<numDivs, sideLen, cellSideLen, <>fps, <historySize, history, counter, averages,
	<>fadeAmount, firstRule, secondRule, <>lowThresh, <>hiThresh, <>userFunc, <currentFps,
	oldElapsedTime, historySums;
	
	*new
	{|argNumDivs = 50, argWinSize = 500, argHistorySize = 1|
	
		^super.new.init(argNumDivs, argWinSize, argHistorySize);
	}
	
	init
	{|argNumDivs, argWinSize, argHistorySize|
	
		numDivs = argNumDivs;
		sideLen = argWinSize;
		cellSideLen = sideLen / numDivs;
		isPlaying = false;
		fps = 10;
		historySize = argHistorySize;
		counter = 0;
		fadeAmount = 0.7;
		
		currentFps = 0;
		
		lowThresh = 0.001;
		hiThresh = 0.999;
		
		firstRule = [[2, 3], [3]];
		secondRule = [[2, 3], [3]];
		
		userFunc = {};
		
		currentGrid = Array.fill(numDivs, { Int8Array.fill(numDivs, { 2.rand; }); });
		nextGrid = currentGrid.deepCopy;
		history = Array.fill(numDivs, { Array.fill(numDivs, { Array.fill(historySize, { 0.5; }) }); });
		historySums = Array.fill(numDivs, { Array.fill(numDivs, { 0.5 * historySize; }); });
		averages = Array.fill(numDivs, { Array.fill(numDivs, { 0 }); });
		
		
		oldElapsedTime = Main.elapsedTime;
		
		win = Window("CA", Rect(5, 5, sideLen, sideLen), resizable: false).onClose_({ playRoutine.stop; });
		win.view.keyDownAction_
			({|...args| 
				
				//args.postln;
				args[3].switch
				(
					32, //space pressed
					{
						if(isPlaying,
						{
							currentFps = 0;
							playRoutine.stop;
							isPlaying = false;
						},
						{
							playRoutine.reset;
							playRoutine.play(SystemClock);
							isPlaying = true;
							oldElapsedTime = Main.elapsedTime;
						}); 
					},
					99, //c pressed
					{
						currentGrid = Array.fill(numDivs, { Int8Array.fill(numDivs, { 0 }); });
						history = Array.fill(numDivs, { Array.fill(numDivs, { Array.fill(historySize, { 0.5; }) }); });
						averages = Array.fill(numDivs, { Array.fill(numDivs, { 0 }); });
						historySums = Array.fill(numDivs, { Array.fill(numDivs, { 0.5 * historySize; }); });
						nextGrid = currentGrid.deepCopy;
						view.refresh;
					},
					114, //r pressed
					{
						currentGrid = Array.fill(numDivs, { Int8Array.fill(numDivs, { 2.rand }); });
						nextGrid = currentGrid.deepCopy;
						view.refresh;
					}
				)
			})
		.onClose_({ playRoutine.stop; });
			
		view = UserView(win, win.view.bounds)
			.clearOnRefresh_(false)
			.background_(Color.black)
			.drawFunc_
			({
				if(isPlaying,
				{
					Pen.color = Color.gray(0, alpha: fadeAmount);
				},
				{
					Pen.color = Color.black;
				});
				Pen.fillRect(view.bounds);
				
				currentGrid.do
				({|item, xPos|
					
					item.do
					({|state, yPos|
					
						if(state == 1,
						{
							Pen.color = Color.new255(averages[xPos][yPos] * 200, 50, (1 - averages[xPos][yPos]) * 200);
							Pen.fillRect(Rect(xPos * cellSideLen, yPos * cellSideLen, cellSideLen, cellSideLen));
						});
					});
				});
				
				currentFps = ((Main.elapsedTime - oldElapsedTime).reciprocal + currentFps) * 0.5;
				oldElapsedTime = Main.elapsedTime;
			})
			.mouseDownAction_
			({|...args|
				
				var xPos = (args[1] / cellSideLen).floor;
				var yPos = (args[2] / cellSideLen).floor;
				currentGrid[xPos][yPos] = 1 - currentGrid[xPos][yPos];
				view.refresh;
			});
		
		playRoutine = 
			Routine
			({		
				inf.do
				({
					this.gridIterate;
					{ view.refresh; }.defer;
					fps.reciprocal.wait;
				});		
			});
			
		win.front;

	}
	
	getNumNeighbors
	{|gridX, gridY|
	
		var sum;
		gridX = gridX + numDivs;
		gridY = gridY + numDivs;
		
		sum = 
			currentGrid[(gridX) % numDivs][((gridY - 1) % numDivs)] +
			currentGrid[((gridX + 1) % numDivs)][((gridY - 1) % numDivs)] +
			currentGrid[((gridX + 1) % numDivs)][((gridY) % numDivs)] +
			currentGrid[((gridX + 1) % numDivs)][((gridY + 1) % numDivs)] +
			currentGrid[((gridX) % numDivs)][((gridY + 1) % numDivs)] +
			currentGrid[((gridX - 1) % numDivs)][((gridY + 1) % numDivs)] +
			currentGrid[((gridX - 1) % numDivs)][((gridY) % numDivs)] +
			currentGrid[((gridX - 1) % numDivs)][((gridY - 1) % numDivs)];
		^sum;
	}
	
	gridIterate
	{
		var curSum;
		var avgHolder;
		var population = 0;
		
		currentGrid.do
		({|item, xPos|
		
			item.do
			({|state, yPos|
			
				curSum = this.getNumNeighbors(xPos, yPos);
				historySums[xPos][yPos] = historySums[xPos][yPos] - history[xPos][yPos][counter];
				history[xPos][yPos][counter] = state; //inject old state
				historySums[xPos][yPos] = historySums[xPos][yPos] + state;
				
				avgHolder = historySums[xPos][yPos] / historySize;
				averages[xPos][yPos] = avgHolder.copy;
				
				if((avgHolder >= lowThresh) and: { avgHolder <= hiThresh},
				{
					if(state == 1,
					{
						if(firstRule[0].includes(curSum),
						{
							nextGrid[xPos][yPos] = 1;
							population = population + 1;
						},
						{
							nextGrid[xPos][yPos] = 0;
						});
					},
					{
						if(firstRule[1].includes(curSum),
						{
							nextGrid[xPos][yPos] = 1;
							population = population + 1;
						});
					});
				},
				{
					if(state == 1,
					{
						if(secondRule[0].includes(curSum),
						{
							nextGrid[xPos][yPos] = 1;
							population = population + 1;
						},
						{
							nextGrid[xPos][yPos] = 0;
						});
					},
					{
						if(secondRule[1].includes(curSum),
						{
							nextGrid[xPos][yPos] = 1;
							population = population + 1;
						});
					});
				});
				
				
			});
			
		});
		
		userFunc.value(nextGrid, population, history, averages);
		
		currentGrid = nextGrid.deepCopy;
		counter = (counter + 1) % historySize;
	}
	
	setFirstRule
	{|argRuleString|
	
		firstRule = argRuleString.split($/).collect({|item| item.as(Array).collect({|num| num.asString.asInteger; }) });
	}
	
	setSecondRule
	{|argRuleString|
	
		secondRule = argRuleString.split($/).collect({|item| item.as(Array).collect({|num| num.asString.asInteger; }) });
	}
	
	setRule
	{|argRuleString|
	
		firstRule = argRuleString.split($/).collect({|item| item.as(Array).collect({|num| num.asString.asInteger; }) });
		secondRule = argRuleString.split($/).collect({|item| item.as(Array).collect({|num| num.asString.asInteger; }) });
	}
	
	
	//save to/load from file methods
	saveScene
	{|argFile|
	
		var outFile;
		
		outFile = File(argFile, "w");
		outFile.write("?ModernLifeScene 1\n");
		outFile.write(numDivs.asString++"\n");
		outFile.write(currentGrid.asCompileString++"\n");
		outFile.write("?EndSave");
		
		outFile.close;
		"Scene saved!".postln;
	}
	
	loadScene
	{|argFile|
	
		var inFile;
		var savedParams;
		
		if(File.exists(argFile),
		{
			inFile = File(argFile, "r");
			savedParams = inFile.readAllString().split($\n);
			if(savedParams[0] != "?ModernLifeScene 1",
			{
				"The saved file was not created for this version of ModernLife.".error;
				^this.halt;
			});
			
			if(savedParams.last != "?EndSave",
			{
				"The save file is truncated.".error;
				^this.halt;
			});
			
			if(numDivs != savedParams[1].interpret,
			{
				("numDivs in saved file does not match with this instance. It should be:"+savedParams[1]).error;
				^this.halt;
			});
			
			history = Array.fill(numDivs, { Array.fill(numDivs, { Array.fill(historySize, { 0.5; }) }); });
			averages = Array.fill(numDivs, { Array.fill(numDivs, { 0 }); });
			currentGrid = savedParams[2].interpret;
			nextGrid = currentGrid.deepCopy;
			view.refresh;			
		},
		{
			("File" + argFile + "not found.").postln;
			^this.halt;
		});

		
	}
	
	saveState
	{|argFile|
	
		var outFile;
		
		outFile = File(argFile, "w");
		outFile.write("?ModernLife 1\n");
		outFile.write(numDivs.asString++"\n");
		outFile.write(win.bounds.height.asString++"\n");
		outFile.write(history[0][0].size.asString++"\n");
		outFile.write(firstRule.asCompileString++"\n");
		outFile.write(secondRule.asCompileString++"\n");
		outFile.write(counter.asString++"\n");
		outFile.write(fps.asString++"\n");
		outFile.write(lowThresh.asString++"\n");
		outFile.write(hiThresh.asString++"\n");
		outFile.write(fadeAmount.asString++"\n");
		outFile.write(currentGrid.asCompileString++"\n");
		outFile.write(nextGrid.asCompileString++"\n");
		outFile.write(history.asCompileString++"\n");
		outFile.write(averages.asCompileString++"\n");
		outFile.write(userFunc.asCompileString.replace("\n", 30.asAscii)++"\n");
		outFile.write("?EndSave");
		
		outFile.close;
		"State saved!".postln;
	}
	
	injectSaveVals //private method
	{|argValArray|
	
		var va = argValArray;
		firstRule = va[4].interpret;
		secondRule = va[5].interpret;
		counter = va[6].interpret;
		fps = va[7].interpret;
		lowThresh = va[8].interpret;
		hiThresh = va[9].interpret;
		fadeAmount = va[10].interpret;
		currentGrid = va[11].interpret;
		nextGrid = va[12].interpret;
		history = va[13].interpret;
		averages = va[14].interpret;
		userFunc = va[15].replace(30.asAscii.asString, "\n").interpret;
		view.refresh;
		
	}
	
	*loadState
	{|argFile|
	
		var inFile;
		var savedParams;
		var retObject;
		
		if(File.exists(argFile),
		{
			inFile = File(argFile, "r");
			savedParams = inFile.readAllString().split($\n);
			if(savedParams[0] != "?ModernLife 1",
			{
				"The saved file was not created for this version of ModernLife.".error;
				^this.halt;
			});
			
			if(savedParams.last != "?EndSave",
			{
				"The save file is truncated.".error;
				^this.halt;
			});
			
			retObject = ModernLife.new(savedParams[1].interpret, savedParams[2].interpret, savedParams[3].interpret);
			retObject.injectSaveVals(savedParams);
			^retObject;
			
		},
		{
			("File" + argFile + "not found.").postln;
			^this.halt;
		});
	}
	
	hideGui
	{
		win.visible_(false);
	}
	
	showGui
	{
		win.visible_(true);
	}
	
	play
	{
		if(isPlaying.not,
		{
			playRoutine.reset;
			playRoutine.play(SystemClock);
			isPlaying = true;
			oldElapsedTime = Main.elapsedTime;
		}); 
	}
	
	stop
	{
		if(isPlaying,
		{
			currentFps = 0;
			playRoutine.stop;
			isPlaying = false;
		});
	}
}