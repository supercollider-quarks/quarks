ColPen {
	
	// collects Pen commands to a classvar instead of drawing
	
	// can be used to generate GUI code. Also used by SVG and asPostScript
	
	classvar <cmds;
	classvar <>pushlevel;
	
	*clear { cmds = []; }  // use to clear cmds
	
	*use { arg function;
		var res;
		this.push;
		res = function.value;
		this.pop;
		^res
	}
	
	*push {
		cmds = cmds.asCollection.add( [\push, (pushlevel = (pushlevel ? 0) + 1).copy ] );
		}
		
	*pop {
		cmds = cmds.asCollection.add( [\pop, pushlevel.copy ] );
		pushlevel = (pushlevel ? 0) - 1;
		}
		
	*line { arg p1, p2;
		^this.moveTo(p1).lineTo(p2);
		}

	// all other commands 
	
	*doesNotUnderstand { arg selector ... args;
		cmds = cmds.asCollection.add( [ selector ] ++ args );
		}
		
	*respondsTo { arg aSymbol; ^true; } // dangerous? at least dirty.. change later
		// although ColPen does actially respond to anything..
		
	// this is why ColPen is useful:

	*draw { |penClass, clear = true, inCmds|	// draw collected items with a pen class
		penClass = ( penClass ?? { GUI.pen } );
		inCmds = inCmds ?? { this.collectUse };
		inCmds.do({ |cmd|
			if( cmd[0] == \use )
				{ penClass.use({ this.draw( penClass, false, cmd[1..] ) }) }
				{ penClass.perform( *cmd ) }
			});
		if( clear ) { this.clear };
		}
		
	*asCode { |penClass, clear = true, inCmds, indent = 0|  // generate executable Pen code
		var outstring = "";
		penClass = ( penClass ? "Pen" ).asString;
		inCmds = inCmds ?? { this.collectUse };
		inCmds.do({ |cmd|
			if( cmd[0] == \use )
				{ outstring = outstring ++ 
					"".extend( indent, $\t ) ++
					penClass ++ ".use({\n" ++
					this.asCode( penClass, false, cmd[1..], indent + 1 ) ++
					"".extend( indent + 1, $\t ) ++
					"});\n";
				}
				{ outstring = outstring ++ "".extend( indent, $\t ) ++
					penClass ++ "." ++ cmd[0] ++ 
						( if( cmd[1..].size > 0 ) 
							{ "( " ++ cmd[1..].collect(_.asShortCS ).join( ", " ) 
								++ " );\n" }
							{ ";\n" });
					if( [\fill, \stroke, \clip].includes( cmd[0] ) )
						{ outstring = outstring ++ "\n"; };
				};
			});
		if( clear ) { this.clear };
		^outstring
		}
	
	// private
	
	*collectUse { |inCmds|
		// recursive collection of all pop/push events
		var pushIndex, popIndex;
		var uselevel = 0;
		inCmds = inCmds ? cmds;
		pushIndex = inCmds.detectIndex({ |item| item[0] == \push; });
		if( pushIndex.isNil )
			{ ^inCmds }
			{ 
			popIndex = (
				inCmds[(pushIndex+1)..].detectIndex({ |item| 
				(item[0] == \pop) && { item[1] == inCmds[ pushIndex ][1] }; 
				}) ?? { inCmds.size-1 }) + pushIndex + 1;
			^(if( pushIndex > 0 ) { cmds[..(pushIndex-1)] ? [] } { [] })  ++ 
				[ [ \use ] ++ this.collectUse( inCmds[(pushIndex+1)..(popIndex-1)] ) ] ++
				this.collectUse( inCmds[(popIndex + 1)..] )
			};
		}
	}