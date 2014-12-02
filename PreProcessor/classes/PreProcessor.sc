PreProcessor {
	
	var <>startDelimiter = "<%";
	var <>endDelimiter = "%>";
	var <>verbose = false;

	*current {
		^thisProcess.interpreter.preProcessor
	}
	
	translate { arg code, event;
		var tag, func, languages;
		languages = event.at(\languages);
		if(languages.isNil) { Error("preprocessor: no languages provided in inevent").throw };
	 	tag = event.at(\lang);
	 	func = languages.at(tag);
	 	if(verbose) { 
	 		if(func.isNil) { 
	 			"preprocessor % not found!\n".postf(tag) 
	 		} { 
	 			"using % preprocessor\n".postf(tag) 
	 		}
	 	};
	 	func.value(code, event);
	}
	
	value { arg string;
		var i0 = string.findAll(startDelimiter);
		var i1 = string.findAll(endDelimiter);
		var strings, i2;
		if(i0.isNil or: { i1.isNil }) { 
			^string 
		} {
			if(i0.size != i1.size 
				or: {
					i0.last > i1.last
				}
				or: {
				i0.first > i1.first	
				}) { Error("syntax error. PreProcessor expects closed contexts.").throw };
			
			strings = [string[..i0[0]-1]];
			i0.size.do { |i|
				var start = i0[i];
				var end = i1[i];
				var nextStart = i0[i+1] ?? { string.size };
				strings = strings.add(string[start + startDelimiter.size .. end - 1]);
				strings = strings.add(string[end + endDelimiter.size .. nextStart - 1]);
			};
			
			strings = strings.collect { |code, i|
				var variable;
				if(i.even) { 
					code
				} {
					"{ |event|
						var preprocessor, code;
						if(event.notNil) {
							preprocessor = PreProcessor.current;
							code = %;
							preprocessor !? { preprocessor.translate(code, event) };
							event;
						};
					}".format(code.quote);
				}
			};
			
			^strings.join(Char.nl);
		}
		}

}