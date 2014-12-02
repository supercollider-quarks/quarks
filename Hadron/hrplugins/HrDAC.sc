HrDAC : HadronPlugin
{
	var synthInstance, levelSlider, limButton, lastLevel;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 2;
		var numOuts = 0;
		var bounds = Rect((Window.screenBounds.width - 250).rand, (Window.screenBounds.width - 90).rand, 250, 90);
		var name = "HrDAC";
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		window.background_(Color.gray(0.9));
		lastLevel = 1;
		helpString = "In1/In2 audio inputs. Outputs not used, outputs to your hardware outs.";
		
		StaticText(window, Rect(10, 10, 100, 20)).string_("Level:");
		levelSlider = HrSlider(window, Rect(10, 30, 200, 20))
		.value_(1)
		.action_({|sld| synthInstance.set(\level, sld.value); lastLevel = sld.value; });
		
		StaticText(window, Rect(10, 60, 100, 20)).string_("Limiter/LeakDC:");
		limButton = Button(window, Rect(110, 60, 100, 20))
			.states_
			([
				["Turn On", Color.black, Color(0.5, 0.7, 0.5)], 
				["Turn Off", Color.white, Color(0.7, 0.5, 0.5)]
			])
			.action_
			({
				arg state;
				state.value.switch
				(
					1, { synthInstance.set(\limiter, 1); },
					0, { synthInstance.set(\limiter, 0); }
				);
			});
		
		fork
		{
			SynthDef("hrDAC"++uniqueID,
			{
				arg inBus1, inBus2, level = 1, limiter = 0;
				var sound = [InFeedback.ar(inBus1), InFeedback.ar(inBus2)];
				sound = Select.ar(limiter, [sound, Limiter.ar(LeakDC.ar(sound))]);
				Out.ar(0, sound * level.lag(0.1));
			}).add;
			
			Server.default.sync;
			synthInstance = Synth("hrDAC"++uniqueID, [\inBus1, inBusses[0], \inBus2, inBusses[1]], group);
		};
		
		saveGets =
		[
			{ lastLevel; },
			{ levelSlider.boundMidiArgs; },
			{ limButton.value; }
		];
		
		saveSets =
		[
			{|argg| lastLevel = argg; synthInstance.set(\mul, argg); { levelSlider.value_(argg); }.defer; },
			{|argg| levelSlider.boundMidiArgs_(argg); },
			{|argg| limButton.valueAction_(argg); }
		];
		
		modGets.put(\level, { levelSlider.value; });
		modSets.put(\level, {|argg| levelSlider.valueAction_(argg) });
	}
	
	updateBusConnections
	{
		synthInstance.set(\inBus1, inBusses[0], \inBus2, inBusses[1]);
	}
	
	cleanUp
	{
		synthInstance.free;
	}
}