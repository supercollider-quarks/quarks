HrDIYSynth : HadronPlugin
{
	var synthInstance, sDef, codeView, wrapFunc;
	
	*new
	{|argParentApp, argIdent, argUniqueID, argExtraArgs, argCanvasXY|
		
		var numIns = 2;
		var numOuts = 2;
		var bounds = Rect((Window.screenBounds.width - 450).rand, (Window.screenBounds.height - 400).rand, 450, 400);
		var name = "HrDIYSynth";
		^super.new(argParentApp, name, argIdent, argUniqueID, argExtraArgs, bounds, numIns, numOuts, argCanvasXY).init;
	}
	
	init
	{
		window.background_(Color.gray(0.8));
		helpString = "In1/In2 audio inputs, given as args to function. You must return 2 channels of audio inside function.";
		
		{
			codeView = TextView(window, Rect(10, 10, 430, 350))
			.string_("{ arg input; input; }")
			.usesTabToFocusNextView_(false)
			.enterInterpretsSelection_(false)
			.editable_(true);
			
			if(GUI.id == \swing, { SwingOSC.default.sync; });
			
			this.redefineSynth(codeView.string.interpret);
		}.fork(AppClock);
		
		Button(window, Rect(10, 370, 80, 20)).states_([["Evaluate"]])
		.action_
		({
			this.redefineSynth(codeView.string.interpret);
		});
		
		//this.redefineSynth(codeView.string.interpret);
		
		saveGets =
			[
				{ codeView.string.replace("\n", 30.asAscii); }
			];
			
		saveSets =
			[
				{|argg| codeView.string_(argg.replace(30.asAscii.asString, "\n")); }
			]
	
	}
	
	redefineSynth
	{|argWrapFunc|
		
		
		sDef = 
		SynthDef("hrDIYSynth"++uniqueID,
		{
			arg inBus0, inBus1, outBus0, outBus1;
			var inputs = [InFeedback.ar(inBus0), InFeedback.ar(inBus1)];
			
			var sound = SynthDef.wrap(argWrapFunc, [0], [inputs]);
			
			Out.ar(outBus0, sound[0]);
			Out.ar(outBus1, sound[1]);
		});
		
		fork
		{
					
			sDef.add;
			
			Server.default.sync;
			
			if(synthInstance.notNil, { synthInstance.free; });
			synthInstance = 
			Synth("hrDIYSynth"++uniqueID, 
				[
					\inBus0, inBusses[0], 
					\inBus1, inBusses[1],
					\outBus0, outBusses[0],
					\outBus1, outBusses[1]
				], target: group);
		};
	}
	
	wakeFromLoad
	{
		{
			if(GUI.id == \swing, { SwingOSC.default.sync; });
			this.redefineSynth(codeView.string.interpret);
		}.fork(AppClock);
	}
	
	updateBusConnections
	{
		synthInstance.set(\inBus1, inBusses[0], \inBus2, inBusses[1], \outBus0, outBusses[0], \outBus1, outBusses[1]);
	}
	
	cleanUp
	{
		synthInstance.free;
	}
}