// create a FreeUdef similar to the \sine Udef
(
d = FreeUdef( \testFree, [ \freq, 440, \amp, 0.1 ] )
	.createSynthFunc_({ |unit, target|
		Synth( "u_testFree", unit.getArgsFor( target ), target, \addToTail );
	})
	.setSynthFunc_({ |unit ...keyValuePairs|
		unit.synths.do{ |s|
			var server = s.server;
			s.set(*keyValuePairs.clump(2).collect{ |arr| 
				[arr[0],arr[1].asControlInputFor(server)] }.flatten)
		};
	})
	.addUIO( UOut, \ar, 0, { Silent.ar } )
	.synthDef_( 
		SynthDef( "u_testFree", { |freq = 440, amp = 0.1| 
			UOut.ar( 0, SinOsc.ar( freq, 0, amp ) );
		})
	)
)

d.args;
d.audioOuts; // show the outputs

x = UChain( \testFree, \output ); // make a chain
x.gui;

(
// no setSynthFunc (fallback to Udef:setSynth)
d = FreeUdef( \testFree, [ \freq, 440, \amp, 0.1 ] )
	.createSynthFunc_({ |unit, target|
		Synth( "u_testFree", unit.getArgsFor( target ), target, \addToTail );
	})
	.addUIO( UOut, \ar, 0, { Silent.ar } )
	.synthDef_( 
		SynthDef( "u_testFree", { |freq = 440, amp = 0.1| 
			UOut.ar( 0, SinOsc.ar( freq, 0, amp ) );
		})
	)
)

(
// no createSynthFunc either (fallback to Udef:createSynth)
// only args, UIO and a synthDef are really needed
d = FreeUdef( \testFree, [ \freq, 440, \amp, 0.1 ] )
	.addUIO( UOut, \ar, 0, { Silent.ar } )
	.synthDef_( 
		SynthDef( "u_testFree", { |freq = 440, amp = 0.1| 
			UOut.ar( 0, SinOsc.ar( freq, 0, amp ) );
		})
	)
)

(
// most basic version:
d = FreeUdef( \testFree )
	.synthDef_( 
		SynthDef( "u_testFree", { |freq = 440, amp = 0.1| 
			UOut.ar( 0, SinOsc.ar( freq, 0, amp ) );
		})
	)
	.addSynthDefControls;

)
