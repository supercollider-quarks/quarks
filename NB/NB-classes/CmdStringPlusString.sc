
+ String {
	
	py { arg thisDocument;
		var sel, args, firstArg, cmdString, result, temp = "";
		cmdString = this;
		cmdString = cmdString.split($,);
		sel = cmdString.first;
		firstArg = sel.pop;
		cmdString = cmdString.reverse;
		cmdString.pop;
		args = cmdString.reverse;
		args = firstArg.asString.ccatList(args);
		cmdString = sel ++ args;
		cmdString.do({ arg char, i;
			temp = temp ++ char;
			if(char == $ and: { cmdString.at(i+1)==$ }, {
				temp.pop
			});
		});
		cmdString = temp;		
		^thisDocument.publish(cmdString)
	}
	
	cmd { arg ... args;
		^this.buildCmd(args)
	}
	
	buildCmd { arg args;
		var cmdString;	
		args = args.collect({ arg theArg, i;
			if(theArg.isKindOf(String), { 
				theArg = theArg.asCompileString },{
				theArg
			});
		});
		cmdString = (this ++ "(" ++ args.at(0));
		args = args.reverse;
		args.pop;
		args = args.reverse;
		cmdString = cmdString.ccatList(args) ++ ")";
		^cmdString
	}
}
