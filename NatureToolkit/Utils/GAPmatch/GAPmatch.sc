//Batuhan Bozkurt 2009

GAPmatch
{
	var <gaInstance, <>synthDef, <poolSize, <>sampleLoc,
	<>synthParams, <>paramSpace, <numThreads, <>tLatency, <servers,
	<defaultBuffer, <curGeneration;
	
	var gui;
	
	var <soundBufs, playerBusses, <sOutBusses, responder, sGroups;
	
	var thrCondition, jobClumps, fitnessClumps, curRoundIndex, proceedToNextGen, <>lastGen,
	<shouldContinue;
	
	*new
	{	
		arg argPoolSize, argSynthDef, argSampleLoc, argSynthParams, argParamSpace, 
			argNumThreads, argServers;
		
		^super.new.init
			(
				argPoolSize, 
				argSynthDef, 
				argSampleLoc, 
				argSynthParams, 
				argParamSpace, 
				argNumThreads,
				argServers
			);
	}
	
	init
	{
		arg argPoolSize, argSynthDef, argSampleLoc, argSynthParams, argParamSpace, 
			argNumThreads, argServers;
			
		var tempSynthDef;
	
		poolSize = argPoolSize ?? { this.reportError("argPoolSize"); };
		synthDef = argSynthDef ?? { this.reportError("argSynthDef"); };
		sampleLoc = argSampleLoc ?? { this.reportError("argSampleLoc"); };
		synthParams = argSynthParams ?? { this.reportError("argSynthParams"); };
		paramSpace = argParamSpace ?? { this.reportError("argParamSpace"); };
		numThreads = argNumThreads ?? { this.reportError("argNumThreads"); };
		servers = argServers ?? 
			{ "Server(s) not provided, will use default".postln; [Server.default]; };
			
		//sanity checks
		if(servers.every({|item| item.isKindOf(Server); }).not,
			{
				"The last argument must be an array of servers.".error;
				^this.halt;
			});
			
		if(Server.default.serverRunning.not,
			{
				"Default server must be booted before creating an instance.".error;
				^this.halt;
			});
		
		if(servers.every({|srv| srv.serverRunning == true; }).not,
			{
				"Supplied servers should be booted before creating an instance.".error;
				^this.halt;
			});
			
		if((numThreads % servers.size) != 0,
			{
				"argNumThreads must be evenly divisible by number of supplied servers".error;
				^this.halt;
			});
		
		if(poolSize < (numThreads * servers.size),
			{
				"argPoolSize cannot be smaller than (argNumThreads * argServers.size).".error;
				^this.halt;
			});
			
		if((poolSize % (numThreads * servers.size)) != 0,
			{
				"argPoolSize must be divisible by (argNumThreads * argServers.size).".error;
				^this.halt;
			});
		
			
		thrCondition = Condition.new(false);
		shouldContinue = Condition.new(true);
		tLatency = 0.1;
		proceedToNextGen = true;
		curGeneration = 0;
		
		gaInstance = GAWorkbench
			(
				poolSize, 
				paramSpace, 
				{ 1; },
				{|chrom|
					
					var tempChrom = paramSpace.value;
					var randIndex = (tempChrom.size - 1).rand;
					chrom[randIndex] = tempChrom[randIndex];
					chrom;
				}
			); 
		
		gaInstance.mutationProb = 0.1;
		
		sGroups = servers.collect({|srv| Group.new(srv) });
		defaultBuffer = Buffer.readChannel(Server.default, sampleLoc, channels: [0]).normalize;
		soundBufs = servers.collect
			({|srv| 
				
				Buffer.readChannel(srv, sampleLoc, channels: [0]).normalize;
			});
		
		playerBusses = servers.collect({|srv| Bus.audio(srv, 1); });
		sOutBusses = servers.collect({|srv| { Bus.audio(srv, 1); } ! numThreads; });
		
		servers.do({|srv| synthDef.send(srv); });
		synthDef.send(Server.default);
		
		tempSynthDef = SynthDef(\gapmatch_player,
		{
			arg bus, buffer;
			OffsetOut.ar(bus, PlayBuf.ar(1, buffer, doneAction: 2));
		});
		
		servers.do({|srv| tempSynthDef.send(srv); });
		
		tempSynthDef = SynthDef(\gapmatch_comparator,
		{
			arg totalTime, synthBus, realBus, cIndex1, cIndex2;
			
			var inSynth = In.ar(synthBus, 1);
			var inReal = In.ar(realBus, 1);
			var endTrig = Line.kr(0, 1, totalTime).floor;
			var chainA, chainB, chain;
			chainA = FFT(LocalBuf(2048), inSynth, wintype: 1);
			chainB = FFT(LocalBuf(2048), inReal, wintype: 1);
			chain = FrameCompare.kr(chainA, chainB, 0.5);
			SendReply.kr(endTrig, 'gapmatch_anafinito', [chain, cIndex1, cIndex2]);
			FreeSelf.kr(endTrig);
			//Out.ar(0, [inSynth, inReal]);
			
		});
		
		servers.do({|srv| tempSynthDef.send(srv); });
		
		responder = OSCresponderNode(nil, 'gapmatch_anafinito',
			{|t, r, msg, other|
				
				var fitness = msg[3];
				var index1 = msg[4];
				var index2 = msg[5];
				//[t, r, msg, other].postln;
				//DPR.dpr("curRound", curRoundIndex);
				curRoundIndex = curRoundIndex - 1;
				fitnessClumps[index1][index2] = fitness.reciprocal;
				//fitnessClumps.flat.count({|item| item == nil; }).postln;
				//fitnessClumps.flat.includes(nil).postln;
				//DPR.dpr("curRoundIndex", curRoundIndex);
				//fitnessClumps.asCompileString.postln;
				if(curRoundIndex == 0,
				{
					//"curRound finished".postln;
					thrCondition.test = true;
					thrCondition.signal;
					if(fitnessClumps.flat.includes(nil).not,
					{
						//"generation finished".postln;
						curGeneration = curGeneration + 1;
						gaInstance.injectFitness(fitnessClumps.flat);
						fitnessClumps.flat.asCompileString;
						lastGen = [gaInstance.genePool, gaInstance.fitnessScores];
						gui.updateTexts([curGeneration, lastGen[1][0]]);
						gaInstance.crossover;
						jobClumps = gaInstance.genePool.clump(numThreads);
						fitnessClumps = Array.fill(gaInstance.genePool.size).clump(numThreads);
						thrCondition.test = false;
					});
				});
				
				
			});
			
		gui = GAPmatchGUI.new(this);
		gui.showWin;
	}
	
	startJob
	{
		var numServers = servers.size;
		var currentServer = 0;
		proceedToNextGen = true;
		shouldContinue.test = true;
		curRoundIndex = 0;
		gui.sButton.value = 1;
		
		
		fork
		({
			
			while({ proceedToNextGen; },
			{
				jobClumps = gaInstance.genePool.clump(numThreads);
				fitnessClumps = Array.fill(gaInstance.genePool.size).clump(numThreads);
				thrCondition.test = true;
				if(responder.notNil, { responder.remove; });
				responder.add;
				currentServer = 0;
				
				jobClumps.do
				({|jItem, jCount|
				
					//curRoundIndex = jItem.size * 2;
					
					if(currentServer == (numServers - 1), { thrCondition.test = false; });
					//DPR.dpr("current server", currentServer);
					//DPR.dpr("jCount", jCount);
					//DPR.dpr("jItem.size", jItem.size);
					servers[currentServer].makeBundle(tLatency,
						{
							Synth(\gapmatch_player, 
								[
									\bus,
									playerBusses[currentServer],
									\buffer,
									soundBufs[currentServer]
								],
								target: sGroups[currentServer]
							);
							jItem.do
							({|gParam, gCount|
								
								curRoundIndex = curRoundIndex + 1;
								Synth
								(
									synthDef.name, 
									([synthParams, gParam].flop.flat ++ 
										[\outBus, sOutBusses[currentServer][gCount]]), 
									target: sGroups[currentServer]
								);
								Synth(\gapmatch_comparator,
									[
										\totalTime,
										soundBufs[0].duration,
										\synthBus,
										sOutBusses[currentServer][gCount],
										\realBus,
										playerBusses[currentServer],
										\cIndex1,
										jCount,
										\cIndex2,
										gCount
									], target: sGroups[currentServer], addAction: \addToTail);
							});
						});
						
					currentServer = (currentServer + 1) % numServers;
					if(thrCondition.test == false,
					{
						thrCondition.wait;
						sGroups.do(_.freeAll);
						servers.do(_.sync);
						
					});
					
					shouldContinue.wait;
				});
			});
			
		});
	}
	
	stopJob
	{
		proceedToNextGen = false;
	}
	
	pauseJob
	{
		shouldContinue.test = false;
		gui.sButton.value = 0;
	}
	
	resumeJob
	{
		shouldContinue.test = true;
		shouldContinue.signal;
		gui.sButton.value = 1;
	}
	
	playBest
	{
		var synth;
		fork
		{
			synth = Synth(synthDef.name, [synthParams, lastGen[0][0]].flop.flat ++ [\outBus, 0]);
			[synthParams, lastGen[0][0]].flop.flat.asCompileString.postln;
			NodeWatcher.register(synth);
			(soundBufs[0].duration * 2).wait;
			if(synth.isRunning, { synth.free; });
		};
	}
	
	cleanUp
	{
		this.hideGui;
		sGroups.do(_.free);
		sOutBusses.do(_.do(_.free));
		playerBusses.do(_.free);
		soundBufs.do(_.free);
		responder.remove;
	}
	
	reportError
	{|argErr|
	
		(argErr + "was not provided. see GAPmatch help for argument details.").postln;
		^this.halt;
	}
	
	showGui
	{
		gui.showWin;
	}
	
	hideGui
	{
		gui.hideWin;
	}
}

GAPmatchGUI
{
	var <win, parent, genText, fitText, isStarted, <sButton, <pButton;
	
	*new
	{|argParent|
	
		^super.new.init(argParent);
	}
	
	init
	{|argParent|
	
		parent = argParent;
		isStarted = false;
		win = Window("GAPmatch", Rect(300, 300, 280, 100), false).userCanClose_(false);
		genText = StaticText(win, Rect(10, 10, 250, 20)).string_("Current generation: 0");
		fitText = StaticText(win, Rect(10, 30, 250, 20)).string_("Current fitness:         N/A");
		sButton = Button(win, Rect(10, 60, 80, 20)).states_([["Start"], ["Pause"]])
			.action_
			({|btn|
			
				btn.value.switch
				(
					1,
					{
						if(isStarted == false,
							{
								parent.startJob;
							},
							{
								parent.resumeJob;
							});
					},
					0,
					{
						parent.pauseJob;
					}
				);
			});
			
		pButton = Button(win, Rect(100, 60, 80, 20)).states_([["Play best"]])
			.action_
			({
				parent.playBest;
			});
			
		Button(win, Rect(190, 60, 80, 20)).states_([["Hide"]]).action_({ win.visible = false; });
		this.singSang(win, Rect(220, 15, 50, 30), Color.gray(alpha: 0), Color(0,0,0,rrand(0.3,0.9)));
	}
	
	updateTexts
	{|argVals|
	
		{
			genText.string = "Current generation: %".format(argVals[0]);
			fitText.string = "Current fitness:         %".format(argVals[1].round(0.0001));
		}.defer;
	}
	
	showWin
	{
		{
			win.visible = true;
			win.front;
		}.defer;
	}
	
	hideWin
	{
		{ win.visible = false; }.defer;
	}
	
	singSang
	{|argParent, argBounds, argBColor, argFColor, maxHarm|
	
		var lView, drawOne, didDraw = false;
		
		drawOne =
			{|numCycle|
				
				var angs = [[-pi, pi], [pi, -pi]].scramble;
				var curAng = 0;
				var xApart = argBounds.width / (numCycle * 2);
				var xCoords = (numCycle * 2).collect({|cnt| cnt * xApart; }) + (xApart / 2);
				
				Pen.color = argFColor.value;
				Pen.width = rrand(0.4, 0.6);
				
				xCoords.do
				({|coord|
				
					Pen.addArc(coord@(argBounds.height / 2), xApart / 2, *angs[curAng]);
					curAng = 1 - curAng;
				});
				
				Pen.stroke;
				
			};
		lView = UserView(argParent, argBounds).background_(argBColor.value)
		.clearOnRefresh_(false)
		.focusColor_(Color.gray(alpha: 0))
		.mouseDownAction_
			({
				lView.clearDrawing;
				didDraw = false;
				lView.refresh;
			})
		.drawFunc_
			({
				if(didDraw == false,
				{
					6.do({|cnt| drawOne.value(cnt + 1); });
					didDraw = true;
				});
			});
		
		^lView;
	}
}