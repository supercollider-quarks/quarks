
+ SynthDesc { 
	
	defaultNamesVals { |excludeNames = #[\out, \where]| 
		var namesVals;
		var nextName, nextVal; 
		
		controls.do { |ctl| 
		//	if (excludeNames.includes(ctl.name.asSymbol).not) {
				if (ctl.name != '?') { 
						// add previous name and value
					if (nextName.notNil) { 
						namesVals = namesVals.add(nextName.asSymbol).add(nextVal.unbubble);
						nextName = nextVal = nil; 
						
					};
					nextName = ctl.name; 
				};
				nextVal = nextVal.add(ctl.defaultValue.round(0.0001));
		//	};
		};
		namesVals = namesVals.add(nextName.asSymbol).add(nextVal.unbubble);
		
		^namesVals
	}
	
	// build a postable string example event for a synthdesc 
	exampleEventString { |excludeNames = #[\out], linePerPair = true, numIndents = 0| 
		var nl = if (linePerPair, "\n", "");
		var firstIndent = "\t".dup(numIndents).join;
		var lineIndents = if (linePerPair, { "\t".dup(numIndents + 1) }, " ").join;
		var space = if (linePerPair) { "\t" } { " " };
		var str = "%(%'instrument': %" .format(firstIndent, space, name.asSymbol.asCompileString); /*)*/ // bracket matching
		
		this.defaultNamesVals.pairsDo { |parname, val|
			if (excludeNames.includes(parname).not) { 
				str = str ++ (",%%%: %".format(nl, lineIndents, parname.asCompileString, val.unbubble));
			};
		};
		
		/*(*/ // for bracket matching
		str = str ++ "%%).play;\n".format(nl, firstIndent);
		^str;
	}
	
	examplePdefString { 
		var namesVals = this.defaultNamesVals;
		var str = 
			"(\n"
		++ 	"Pdef(\\" ++ { rrand(97, 122).asAscii }.dup(5).join ++ ",\n"
		++ 	"	Pbind("
		++	"\n		'instrument', '%',\n".format(name);

		namesVals.pairsDo { |parname, val, i|
			var comma = if (i < (namesVals.lastIndex - 1), ",", "");
			str = str ++ ("		%, %%\n".format(parname.asCompileString, val.unbubble, comma));
		};
		str = str 
		++ 	"	)\n).play;\n);\n";
		
		^str	
	}
}

+ Republic { 
	
	postSynthDefs { |newDoc = true| 
		var title = "// REPUBLIC - all shared synthdefs";
		var allStr = title ++ "\n\n";

		var synthdefStrings = this.synthDescs.asArray.sort({ |a, b| a.name < b.name })
			.collect { |desc| ("(\n" ++ desc.metadata.sourceCode ++ ".share;\n);\n") }; 		
		if (newDoc) { Document(title, allStr ++ synthdefStrings.join("\n")) };
		allStr.postln;
	}
	
	postEvents { |newDoc = true, linePerPair = false| 
		var title = "// REPUBLIC - example events for all shared synthdefs";
		var allStr = title ++ "\n\n";

		var eventStrings = this.synthDescs.asArray.sort({ |a, b| a.name < b.name })
			.collect (_.exampleEventString(linePerPair: linePerPair, numIndents: 1));
		if (newDoc) { Document(title, allStr ++ eventStrings.join("\n")) };
		allStr.postln;
	}
	
	postPdefs { |newDoc = true| 
		var title = "// REPUBLIC - example Pdefs for all shared synthdefs";
		var allStr = title ++ "\n\n";

		var eventStrings = this.synthDescs.asArray.sort({ |a, b| a.name < b.name })
			.collect (_.examplePdefString); 		
		if (newDoc) { Document(title, allStr ++ eventStrings.join("\n")) };
		allStr.postln;
	}

	postTdefs { |newDoc = true| 
		var title = "// REPUBLIC - example Tdefs for all shared synthdefs";
		var allStr = title ++ "\n\n";
		
		var eventStrings = this.synthDescs.asArray.sort({ |a, b| a.name < b.name })
			.collect (_.exampleEventString(linePerPair: true, numIndents: 2)); 	
		eventStrings.do { |evStr|
			allStr = allStr 
			++ "(\nTdef(\\" ++ { rrand(97, 122).asAscii }.dup(5).join ++ ",{ \n"
			++ "\trrand(13, 34).do { |i|\n"
			++ evStr
			++ "\n\t\t1.wait;\n"
			++ "\t};\n"
			++ "}).play;\n);\n"
		};
		
		if (newDoc) { Document(title, allStr) };
		// allStr.postln;
	}

}
