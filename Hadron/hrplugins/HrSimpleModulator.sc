HrSimpleModulator : HadronPlugin
{
	var synthInstance, prOutBus, postOpText, postOpFunc, pollRoutine,
	modControl, startButton;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 1;
		var numOuts = 0;
		var bounds = Rect((Window.screenBounds.width - 350).rand, (Window.screenBounds.height - 115).rand, 450, 115);
		var name = "HrSimpleModulator";
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		window.background_(Color.gray(0.7));
		prOutBus = Bus.control(Server.default, 1);
		helpString = "Input is modulation source (audio). Applies the operation and modulates the target parameter.";
		StaticText(window, Rect(10, 20, 150, 20)).string_("Operation on signal \"sig\":");
		
		postOpFunc = {|sig| (sig * 0.5) + 0.5; };
		
		postOpText = TextField(window, Rect(160, 20, 280, 20)).string_("(sig * 0.5) + 0.5;")
		.action_({|txt| postOpFunc = ("{|sig|"+ txt.value + "}").interpret; });
		
		modControl = HadronModTargetControl(window, Rect(10, 50, 430, 20), parentApp);
		
		startButton = Button(window, Rect(10, 80, 80, 20)).states_([["Start"],["Stop"]])
		.action_
		({|btn|
		
			if(btn.value == 1, { pollRoutine.play(AppClock); }, { fork{ pollRoutine.stop; 0.1.wait; pollRoutine.reset; } });
		});
		
		
		
		pollRoutine = 
		Routine
		({
			loop
			{
				prOutBus.get({|val| { modControl.modulateWithValue(postOpFunc.value(val)); }.defer; });
				0.04.wait;
			}
		});
		
		fork
		{
			SynthDef("hrSimpleMod"++uniqueID,
			{
				arg inBus0;
				var input = InFeedback.ar(inBus0);
				Out.kr(prOutBus, A2K.kr(input));
				
			}).add;
			
			Server.default.sync;
			
			synthInstance = Synth("hrSimpleMod"++uniqueID, [\inBus0, inBusses[0]], target: group);
		};
		
		saveGets =
			[
				{ postOpText.string; },
				{ modControl.getSaveValues; },
				{ startButton.value; }
			];
		
		saveSets =
			[
				{|argg| postOpText.valueAction_(argg); },
				{|argg| modControl.putSaveValues(argg); },
				{|argg| startButton.valueAction_(argg); }
			];
		
		
	}
	
	
	notifyPlugKill
	{|argPlug|
		
		modControl.plugRemoved(argPlug);
	}
	
	notifyPlugAdd
	{|argPlug|
		modControl.plugAdded;
	}
	
	wakeFromLoad
	{
		modControl.doWakeFromLoad;
	}
	
	
	updateBusConnections
	{
		synthInstance.set(\inBus0, inBusses[0]);
	}
	
	cleanUp
	{
		synthInstance.free;
		pollRoutine.stop;
	}
}