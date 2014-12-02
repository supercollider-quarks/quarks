HrStereoSplitter : HadronPlugin
{
	var synthInstances, summerSynth, sourceSlider, parNumIns, volSliders, volNums,
	transitBus, mixerGroup, currentSlValues;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 2;
		var numOuts = 2;
		var bounds;
		var name = "HrStereoSplitter";
		if(argExtraArgs.isNil, { numOuts = 4; }, { numOuts = 2 * argExtraArgs[0].asInteger; });
		bounds = Rect(200, 200, max(250, 50 + (20 * numOuts)), 150);
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		
		window.background_(Color.gray(0.9));
		helpString = "Number of stereo outputs are set by an argument. Input is distributed to outputs in LRLR alignment.";
		synthInstances = List.new;
		volSliders = List.new;
		volNums = List.new;
		currentSlValues = List.new;
		
		(outBusses.size/2).do
		({|cnt|
		
			
			volSliders.add(HrSlider(window, Rect(25+(40*cnt), 20, 40, 100)).value_(1)
				.action_
				({|sld| 
					
					currentSlValues[cnt] = sld.value;
					volNums[cnt].valueAction_(sld.value); 
				}) 
			);
			
			volNums.add(NumberBox(window, Rect(25+(40*cnt), 120, 40, 20)).value_(1)
				.action_
				({|nmb| 
					synthInstances[cnt].set(\mul, nmb.value); 
					volSliders[cnt].value_(nmb.value);
				}) 
			);
		});
		
		fork
		{
			SynthDef("hrSplitOut"++uniqueID, 
			{
				arg inBus0, inBus1, outBusL, outBusR, mul=1;
				var inL = InFeedback.ar(inBus0);
				var inR = InFeedback.ar(inBus1);
				
				var smoothed = mul.lag(0.1);
				
				Out.ar(outBusL, inL * smoothed);
				Out.ar(outBusR, inR * smoothed);
				
			}).add;
			
			Server.default.sync;
			
			(outBusses.size/2).do
			({|cnt|
			
				synthInstances.add
				(
					Synth("hrSplitOut"++uniqueID, 
						[
							\inBus0, inBusses[0], 
							\inBus1, inBusses[1],
							\outBusL, outBusses[0 + (cnt*2)], 
							\outBusR, outBusses[1 + (cnt*2)], 
							\mul, 1
						], target: group)
				);			
					
			});
			
		};
		
		saveGets = 
			volSliders.collect({|item| { item.value; }; }) ++
			volSliders.collect({|item| { item.boundMidiArgs; }; }) ++
			volSliders.collect({|item| { item.automationData; }; });
			
		saveSets = 
			volSliders.collect({|item| {|argg| item.valueAction_(argg); }; }) ++
			volSliders.collect({|item| {|argg| item.boundMidiArgs_(argg); }; }) ++
			volSliders.collect({|item| {|argg| item.automationData_(argg); }; }); 
		
		volSliders.size.do
		({|cnt|
		
			modGets.put(("level"++cnt).asSymbol, { currentSlValues[cnt]; });
			modSets.put(("level"++cnt).asSymbol,
			{|argg| 
				
				synthInstances[cnt].set(\mul, argg); 
				currentSlValues[cnt] = argg;
				
				{ volSliders[cnt].value_(argg) }.defer;
			});
		});
	}
	
	updateBusConnections
	{
		(outBusses.size/2).do
		({|cnt|
		
			synthInstances[cnt].set
			(
				\outBusL, outBusses[0 + (cnt*2)], 
				\outBusR, outBusses[1 + (cnt*2)]
			);			
				
		});
		
	}
	
	cleanUp
	{
		//group will be freed for you
	}
}