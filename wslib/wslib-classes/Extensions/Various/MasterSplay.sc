MasterSplay {	
	classvar <from = 4, <to = 2, <inOffset = 0, <outOffset = 0, <active = false, <synth;
	
	*initClass { CmdPeriod.add( this ); }
	
	*new { |inFrom = 4, inTo = 2|
		from = inFrom; to = inTo;
		if( active.not ) { this.start; } { (this.class.asString + "already running").postln; };
		}
	
	*synthDef {
		/*
		if( to == 2 )
			{
			^SynthDef("_mastersplay", { |in = 0, out = 0, fade = 0.25, gate = 1|
				XOut.ar( out, 
					EnvGen.kr( Env([ 0, 1, 1, 0 ], [fade, 0, fade], \lin, 1), gate, 
					doneAction: 2 ),
					Splay.ar( In.ar( in, from ) ) ++ (0!(from-to).max(0)) 
					);
				});
			 }
			{ */
			
		^SynthDef("_mastersplay", { |in = 0, out = 0, fade = 0.25, gate = 1|
				var spr;
				if ( to == 2 )
					{ spr = 0.5 }
					{ spr = 1 };
				XOut.ar( out, 
					EnvGen.kr( Env([ 0, 1, 1, 0 ], [fade, 0, fade], \lin, 1), gate, 
						doneAction: 2 ),
					SplayAz.ar( to, In.ar( in, from ), spread: spr, center: spr, 
							orientation: 0 ) ++ 
						(0!(from-to).max(0)) 
					);
				});
				
			//};
		}
		
	*cmdPeriod { if( active ) { { this.start }.defer(0.01) } }
	
	*start { 
		active = true;
		{ this.synthDef.send( Server.default );
		  Server.default.sync;
		  synth = Synth("_mastersplay", [ \in, inOffset, \out, outOffset ], 
		  	Server.default, \addAfter ); 
		}.fork;
		}
	
	*stop { active = false; synth !? { synth.release; synth = nil; }; }
	
	}