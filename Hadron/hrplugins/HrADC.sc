HrADC : HadronPlugin
{
	var synthInstance, lButton, rButton, levSlider, lastLevel;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 0;
		var numOuts = 2;
		var bounds = Rect((Window.screenBounds.width - 350).rand, rrand(50, Window.screenBounds.height), 350, 50);
		var name = "HrADC";
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		
		window.background_(Color.gray(0.7));
		lastLevel = 0;
		
		helpString = "Inputs are not used. Outputs 1/2 are hardware inputs of your interface(L/R).";
		StaticText(window, Rect(10, 10, 50, 20)).string_("Inputs:");
		
		lButton = Button(window, Rect(50, 10, 40, 20))
		.focusColor_(Color.gray(alpha: 0))
		.states_
		([
			["L", Color.black, Color.gray],
			["L", Color.black, Color(0.5, 0.7, 0.5)]
		])
		.action_
		({|but|
			but.value.switch
			(
				1,
				{
					if(rButton.value == 0, { synthInstance.set(\hw0, 0, \hw1, 0) });
					if(rButton.value == 1, { synthInstance.set(\hw0, 0, \hw1, 1) });
				},
				0,
				{
					if(rButton.value == 0, { synthInstance.set(\hw0, -1, \hw1, -1) });
					if(rButton.value == 1, { synthInstance.set(\hw0, 1, \hw1, 1) });
				}
			);
		});
		
		rButton = Button(window, Rect(90, 10, 40, 20))
		.focusColor_(Color.gray(alpha: 0))
		.states_
		([
			["R", Color.black, Color.gray],
			["R", Color.black, Color(0.5, 0.7, 0.5)]
		])
		.action_
		({|but|
			but.value.switch
			(
				1,
				{
					if(lButton.value == 0, { synthInstance.set(\hw0, 1, \hw1, 1) });
					if(lButton.value == 1, { synthInstance.set(\hw0, 0, \hw1, 1) });
				},
				0,
				{
					if(lButton.value == 0, { synthInstance.set(\hw0, -1, \hw1, -1) });
					if(lButton.value == 1, { synthInstance.set(\hw0, 0, \hw1, 0) });
				}
			);
		});
		
		levSlider = HrSlider(window, Rect(140, 10, 200, 20))
		.action_({|sl| synthInstance.set(\mul, sl.value); lastLevel = sl.value; });
		
		
		fork
		{
			SynthDef("hrADC"++uniqueID,
			{
				arg outBus0, outBus1, hw0=0, hw1=0, mul=0;
				Out.ar(outBus0, SoundIn.ar(hw0, mul.lag(0.1)));
				Out.ar(outBus1, SoundIn.ar(hw1, mul.lag(0.1)));
				
			}).add;
			
			Server.default.sync;
			synthInstance = Synth("hrADC"++uniqueID, [\outBus0, outBusses[0], \outBus1, outBusses[1]], group);
			{ lButton.valueAction_(1); }.defer;
		};
		
		saveGets = 
		[
			{ lButton.value; },
			{ rButton.value; },
			{ levSlider.value; },
			{ levSlider.autoPlay; },
			{ levSlider.automationPlaySize; },
			{ levSlider.boundMidiArgs; },
			{ levSlider.automationData; }
		];
		
		saveSets =
		[
			{|argg| lButton.valueAction_(argg); },
			{|argg| rButton.valueAction_(argg); },
			{|argg| levSlider.valueAction_(argg) },
			{|argg| levSlider.autoPlay_(argg); },
			{|argg| levSlider.automationPlaySize_(argg); },
			{|argg| levSlider.boundMidiArgs_(argg); },
			{|argg| levSlider.automationData_(argg); }
		];
		
		modGets.put(\level, { lastLevel; });
		modSets.put(\level, {|argg| lastLevel = argg; synthInstance.set(\mul, argg); { levSlider.value_(argg); }.defer; });
		
	}
	
	updateBusConnections
	{
		synthInstance.set(\outBus0, outBusses[0], \outBus1, outBusses[1]);
	}
	
	cleanUp
	{
		synthInstance.free;
	}
}