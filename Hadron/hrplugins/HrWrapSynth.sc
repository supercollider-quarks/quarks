HrWrapSynth : HadronPlugin
{
	var <synthInstance, sliders, numBoxes, setFunctions, synthBusArgs, startButton, storeArgs, specs;
	var ctlNameStrings;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
			
		var numIns, numOuts, bounds, name = "HrWrapSynth", numControls;
		
		if(argExtraArgs.size == 0, 
		{ 
			argParentApp.displayStatus("This plugin requires an argument. See HrWrapSynth help.", -1);
			this.halt; 
		});
		
		if(SynthDescLib.global.synthDescs.at(argExtraArgs[0].asSymbol) == nil,
		{
			argParentApp.displayStatus("SynthDef"+argExtraArgs[0]+"not found in global SynthDescLib. See HrWrapSynth help.", -1);
			this.halt; 
		});
		
		if(SynthDescLib.global.synthDescs.at(argExtraArgs[0].asSymbol).metadata == nil,
		{
			argParentApp.displayStatus("You need to supply metadata and specs for your synth. See HrWrapSynth help.", -1);
			this.halt; 
		});
		
		numControls = SynthDescLib.global.synthDescs.at(argExtraArgs[0].asSymbol).metadata.at(\specs).size;
		
		numIns = SynthDescLib.global.synthDescs.at(argExtraArgs[0].asSymbol).controlNames.count({|item| item.asString.find("inBus", true, 0) == 0; });
		numOuts = SynthDescLib.global.synthDescs.at(argExtraArgs[0].asSymbol).controlNames.count({|item| item.asString.find("outBus", true, 0) == 0; }).size;
		
		bounds = Rect(400, 400, 350, 50 + (numControls * 30));
		
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		var sdControls, sName;
		sliders = List.new;
		numBoxes = List.new;
		setFunctions = List.new;
		storeArgs = Dictionary.new;
		
		helpString = "This plugin reads a SynthDef from SynthDescLib.default and integrates it with the Hadron system.";	
				
		synthBusArgs = 
		{
			inBusses.collect({|item, cnt| [("inBus"++cnt).asSymbol, inBusses[cnt]] }).flatten ++
			outBusses.collect({|item, cnt| [("outBus"++cnt).asSymbol, outBusses[cnt]] }).flatten;
		};
		
		sName = extraArgs[0].asSymbol;
		
		window.background_(Color(0.9, 1, 0.9));
		
		specs = SynthDescLib.global.synthDescs.at(sName).metadata.at(\specs);
		
		//keeping relevant args
		sdControls = 
		SynthDescLib.global.synthDescs.at(sName).controlNames.reject
		({|item| 
			
			(item.find("inBus", true, 0) == 0) or: { item.find("outBus", true, 0) == 0 };
		});
		
		//drawing gui
		sdControls.do
		({|item, count|
			
			StaticText(window, Rect(10, 10 + (count * 30), 80, 20)).string_(item);
			
			storeArgs.put(item.asSymbol, specs.at(item.asSymbol).default);
			
			numBoxes.add
			(
				NumberBox(window, Rect(200, 10 + (count * 30), 80, 20))
				.value_(specs.at(item.asSymbol).default)
				.action_({|num| sliders[count].valueAction_(specs.at(item.asSymbol).unmap(num.value)); });
			);
			
			sliders.add
			(
				HrSlider(window, Rect(90, 10 + (count * 30), 100, 20))
				.value_(specs.at(item.asSymbol).unmap(specs.at(item.asSymbol).default))
				.action_
				({|sld| 
					
					setFunctions[count].value(sld.value);
				});
			);
			
			setFunctions.add
			({|val|
				
				var mapped = specs.at(item.asSymbol).map(val.value);
				storeArgs.put(item.asSymbol, val);
				synthInstance.set(item, mapped);
				{ numBoxes[count].value_(mapped); }.defer;
			});
			
			//add the modulatable entry for the control
			modGets.put(item.asSymbol, { storeArgs.at(item.asSymbol); });
			modSets.put(item.asSymbol, {|argg| setFunctions[count].value(argg); { sliders[count].value_(argg); }.defer; });
			
			
		});
		
		startButton = 
		Button(window, Rect(10, 10 + (30 * sliders.size), 80, 20))
		.states_([["Start", Color.black, Color(0.5, 0.7, 0.5)], ["Stop", Color.white, Color(0.7, 0.5, 0.5)]])
		.value_(1)
		.action_
		({|btn|
		
			var tempArgs;
			btn.value.switch
			(
				0, { synthInstance.free; },
				1, 
				{ 
					tempArgs = (synthBusArgs.value ++ storeArgs.keys.collect({|item| [item, storeArgs.at(item)]; }).asArray).flatten(1);
					synthInstance = Synth(sName, tempArgs, target: group);
				}
			)
		});
		
		saveGets = 
			sliders.collect({|item, cnt| [{ sliders[cnt].value; }, { sliders[cnt].boundMidiArgs; }, { sliders[cnt].automationData }]; }).flat ++
			[ { startButton.value; }; ];
		
		saveSets =
			sliders.collect
			({|item, cnt| 
				[
					{|argg| sliders[cnt].valueAction_(argg); }, 
					{|argg| sliders[cnt].boundMidiArgs_(argg); }, 
					{|argg| sliders[cnt].automationData_(argg) }
				]; 
			}).flat ++  [ {|argg| startButton.valueAction_(argg); }; ];
		
		synthInstance = Synth(sName, synthBusArgs.value, target: group);	

	}
	
	updateBusConnections
	{
		synthInstance.set(*synthBusArgs.value);
	}
	
	cleanUp
	{
		
	}
}