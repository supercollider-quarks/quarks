+ String {
	appleScript { ^this.asAppleScriptCmd.unixCmd; }
	
	asAppleScriptCmd {
		// converts string to applescript via osescript unix command
		var lines, cmd;
		lines = this.split( $\n );
		
		cmd = "osascript";
		
		lines.do({ |line|
			cmd = cmd + "-e" + line.asCompileString;
			});
		
		^cmd;
		}
		
	keyStroke { |mod = nil, app = "SuperCollider"|
		^this.asKeyStrokeCmd( mod, app ).unixCmd;
		}
	
	asKeyStrokeCmd { |mod = nil, app = "SuperCollider"| // command, option, shift, can be array
		var script;
		// hit a keystroke using applescript
		/* example: 
		"k".keyStroke( "command" ); // recompile
		*/
		script = 
		"tell application " ++ app.quote ++
		"\nactivate\nend tell\n" ++
		"tell application \"System Events\"\n" ++
		"tell process " ++ app.quote ++
		"\nset frontmost to true\nend tell\n";
		
		if ( [ String, Symbol ].includes( mod.class ) )
			{ mod = [ mod ] };
		
		mod.do({ |oneMod|
			script = script ++ "key down" + oneMod.asString ++ "\n";
			});
		
		script = script ++ 
		"keystroke" + this.asString.asCompileString ++ "\n";
		
		mod !? { mod.reverse.do({ |oneMod|
			script = script ++ "key up" + oneMod.asString ++ "\n";
			}); };

		script = script ++ "end tell";
		
		^script.asAppleScriptCmd;
		}

	}