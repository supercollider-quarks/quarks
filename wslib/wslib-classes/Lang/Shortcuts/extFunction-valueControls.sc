// wslib 2006

// automatically add controls for all arg names (use inside a SynthDef function)
// maybe already there? couldn't find it though..

+ Function {
	
	valueControls { |... prependArgs|  // for prependArgs no controls are created
		^UGen.buildSynthDef
			.buildUgenGraph( this, prependArgs: prependArgs ); 
		}
	
	}
	