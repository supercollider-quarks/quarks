//Batuhan Bozkurt 2009
StageLimiter
{
	classvar lmSynth, lmFunc, activeSynth;
	
	*activate
	{ |numChannels = 2|
		fork
		{
			lmFunc = 
			{ 
				{ 
					activeSynth = 
						Synth(\stageLimiter,
							target: RootNode(Server.default), 
							addAction: \addToTail
						);
				}.defer(0.01) 
			};
			lmSynth = SynthDef(\stageLimiter, 
			{
				var input = In.ar(0, numChannels);
				input = Select.ar(CheckBadValues.ar(input, 0, 0), [input, DC.ar(0), DC.ar(0), input]);
				ReplaceOut.ar(0, Limiter.ar(input)) ;
			}).add;
			Server.default.sync;
			lmFunc.value;
			CmdPeriod.add(lmFunc);
			"StageLimiter active".postln;
		}
	}
	
	*deactivate
	{
		activeSynth.free;
		CmdPeriod.remove(lmFunc);
		"StageLimiter inactive...".postln;
	}
}