CmdString {

	var <>cmdSymbol, <>args, <>cmdString;
	var <>comma = ", ";

	*new { arg cmdSymbol ... args;
		^super.newCopyArgs(cmdSymbol, args).init
	}
	
	init {
		var firstWord;
		var numArgs;
		args = args.reject({ arg item, i; item == nil });
		
		// convert from symbol to string
		// openRead and openReadLines require special attention
		if(cmdSymbol==\openRead or: { cmdSymbol==\openReadlines },{
			firstWord = "open" },{
			firstWord = cmdSymbol.asString
		});
		
		// "exceptional" cases: commands that require no (additional) processing
		// except as shown here
		if(cmdSymbol==\reset, { ^"reset()\n" });
		if(cmdSymbol==\push, { ^"push()\n" });
		if(cmdSymbol==\pop, { ^"pop()\n" });

		if(cmdSymbol==\nofill, { ^"nofill()\n" });
		if(cmdSymbol==\nostroke, { ^"nostroke()\n" });

		// build the command string
		cmdString = firstWord ++ "(";
		args.do({ arg item, i;
			cmdString = cmdString ++ item.value ++ comma;
		});
		// remove trailing space and comma
		cmdString.pop; cmdString.pop;

		cmdString = case
		/*
			{ cmdSymbol==\random } { cmdString = cmdString ++ ")"; }
			{ cmdSymbol==\color } { cmdString = cmdString ++ ")"; }    
		*/
			{ cmdSymbol == \openRead } { cmdString ++ ")" ++ ".read()" }
			{ cmdSymbol == \openReadlines } { cmdString ++ ")" ++ ".readlines()" }
			{ true } { cmdString = cmdString ++ ")" };
		^cmdString
		
	}

}