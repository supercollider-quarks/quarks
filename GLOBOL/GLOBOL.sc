
      /////////////////////////////////////////////////////////
     // GLOBOL-2009 : EVERYTHING, EVERYWHERE, ALL THE TIME ///
    /////////////////////////////////////////////////////////
 
  //       ADC, JRH; Donnerstag, 1. Januar 1970 01:02:42            //
 //  NETWORKING OPENS INTERPRETER - USE 'CONNECT' AT YOUR OWN RISK //



GLOBOL {

	classvar <space, <isRunning = false, <numChannels, <>numSpeakers;
	classvar server, responder, <sender, id;
	classvar <classmethdict, <ambiguous;
	classvar <ugenClasses;
	classvar <>laws;
	classvar prevPreProcessor, prevNetFlag;
	
	*run { | n = 8, numOutputs = 2 |
		if(isRunning) { "GLOBOL IS RUNNING ALREADY!".warn; ^this };
		prevPreProcessor = thisProcess.interpreter.preProcessor;
		thisProcess.interpreter.preProcessor = { |string|
			if(isPermitted(this, string).not) { 
				"GLOBOL DOES NOT PERMIT THIS INPUT".warn;
			} {
				this.distribute(string); // SEND TO EVERYWHERE
				interpret(process(this, string));
			};
			"''"; // BLOCK SC REPL
		};
		numChannels = n;
		numSpeakers = numOutputs;
		Server.local.options.numAudioBusChannels = n * 128;
		this.initRealtimeSystem;
		this.buildDict;
		// this.connect;
		id = inf.asInteger.rand;
	}
	
	*end { | time = 8 |
		thisProcess.interpreter.preProcessor = prevPreProcessor;
		space.clear(time).pop;
		isRunning = false;
		this.disconnect;
	}
	
	*buildDict {
		var methdict = (), classdict = ();
		
		Class.allClasses.do { |class|
				classdict.put(class.name.asString
							.collect(_.toUpper).asSymbol, class.name.asString);
				class.methods.do { |method|
					methdict.put(
						*[method.name.asString
							.collect(_.toUpper).asSymbol, method.name.asString]
					)
				}
		};
		// LAWS
		laws !? { 
			laws.keysDo { |key|
				classdict.put(key.asString
							.collect(_.toUpper).asSymbol, key.asString);
			}
		};
		
		ambiguous = this.getSect(classdict, methdict);
		classmethdict = ().putAll(classdict).putAll(methdict) // METHODS OVERRIDE CLASSES
		
	}
	
	*process { | string |
		var lines = split(string, Char.nl);
		
		string.postln; // GLOBOL REPL
		
		^join(collect(lines, { | line |
			
			var newline = String.new;
			
			line = line.replace(":?",
					".playN(%);".format((0..numSpeakers-1).wrapExtend(numChannels))
			);
			// : AND ::
			line = line.replace("::", "&&&");
			line = line.replace(":", ".");
			line = line.replace("&&&", ":");
			// ¤ : LOOK UP GLOBAL LAWS
			line = line.replace([194, 167].collect(_.asAscii).join, "GLOBOL.laws.");
			
			line.postcs;
			line = " " ++ line ++ "  ";
			
			// FIND GLOBOL VARIABLES
			
			(line.size - 2).do { |index|
				var left, middle, right;
				#left, middle, right = line[index..index+2];
				
				// REPLACE UPPER WITH LOWER 
				
				if(	left.isAlpha.not 
					and: { middle.isAlpha }
					and: { middle.isUpper }
					and: { right.isAlpha.not }) 
					{
						middle = middle.toLower;
						if ( (right == $.) or: { line[index + [2, 3]].join == " =" }) {
							newline = newline ++ "~%.ar(%); ".format(middle, numChannels);
							newline = newline ++ "~" ++ middle;
							
						} {
							newline = newline ++ "~%.ar(%)".format(middle, numChannels);
						}
					} {
						newline = newline.add(middle);
					};
				};
				
				newline = replaceCaps(this, newline);
				if(shouldAddSemiColon(this, newline)) { newline = newline ++ ";" };
				
				newline
				
		}), Char.nl);
	}
	
	
	// REPLACE CAP-STRINGS WITH PROPER CLASS AND METHOD NAMES
	
	*replaceCaps { | string |
		
			var allCaps = List.new;
			var capStart, capLength = 0;
			
				string.do { |char, index|
					
					if(char.isAlphaNum and: { char.isUpper }) { 
						if (capLength == 0) { 
							capStart = index; 
						};
						capLength = capLength + 1;
					} { 
						if (capLength > 1) { 
							allCaps.add([capStart, capLength]);
						};
						capLength = 0;
					};
				}; 
				string = string.copy;
				allCaps.do { |list, i| 
					var start, length, bigName, smallName, preceding; 
					#start, length = list;
					
					bigName = string[start..start + length - 1].asSymbol;
					
					if(ambiguous[bigName].notNil and: { string[start + length] == $. }) {
						// WHEN IN DOUBT, ASSUME THAT IT IS A CLASS NAME
						// ONLY WHEN A DOT (COLON IN GLOBOL) COMES AFTER IT
						smallName = ambiguous[bigName];
					} {
						smallName = classmethdict[bigName];
					};
					preceding = string[start - 1];
					
					smallName !? {
						string.overWrite(smallName, start);
					};
					
					// todo: if not found, try to find it in the UGen list.
					// then find the closest approximations (use string: compare)
					// and build a graph from these, using a weighted mix
					// for the other empty arguments use the defaults from the system.
					// if a number is an input to a filter, use ~input instead.
					
					
				};
		
				^string
	}
	
	*shouldAddSemiColon { | string |
		string.reverseDo { |char|
			if(char.isSpace.not and: { char.isAlphaNum.not }) { ^false };
			if(char.isAlphaNum) { ^true };
		};
		^false

	}
	
	// SYNTHESIS //
	
	*initRealtimeSystem {
		ugenClasses = UGen.allSubclasses.collect(_.name).collect(_.asString);
		space = ProxySpace.push(Server.local.reboot);
		isRunning = true;
		
		// TODO: get all UGens, check their arguments.
		// init args to something useful
		// maybe find soundfiles on the system
		space.put(\freq, { LFNoise1.ar(0.001 ! 8).range(220, 250) });
	
	}
	
	
	
	// NETWORKING //
	
	*isPermitted { | string |
		string = string.collect(_.toUpper);
		// THIS IS NOT SAFE, BUT AVERTS SOME BAD SIMPLE IDEAS. 
		^string.find("UNIXCMD").isNil
			and: { string.find("SYSTEMCMD").isNil } 
			and: { string.find("FILE").isNil } 
			and: { string.find("PIPE").isNil }
	}
	
	*distribute { | string |
		sender !? { sender.do(_.sendMsg("/GLOBOL-2009", string, id)) };
	}
	
	*connect { |broadcastAddr|  // broadcastAddr can be an array of NetAddr
		("* NETWORKING OPENS INTERPRETER - USE 'CONNECT' AT YOUR OWN RISK"
		"\nUSE 'DISCONNECT' TO CLOSE INTERPRETER. *").postln;
		sender = if(broadcastAddr.isNil) {
			this.broadcast 
		} {
		 	broadcastAddr
		 };
		prevNetFlag = NetAddr.broadcastFlag;
		NetAddr.broadcastFlag = true;
		
		responder = OSCresponder(nil, "/GLOBOL-2009", { |r,t,msg|
				var code = msg[1].asString;
				var inID = msg[2];
				// GLOBOL DOES TRY NOT TO PERMIT SYSTEM CALLS
				// AVOID INFINITE NETWORK LOOP
				if(inID != id and: { isPermitted(this, code) }) 
				{
					interpret(process(this, code));
				}
		}).add;
	
	}
	
	*disconnect {
		responder.remove;
		responder = nil;
		"* INTERPRETER CLOSED. *".postln;
		sender !? { 
			sender.do(_.disconnect); 
			NetAddr.broadcastFlag = prevNetFlag;
		};
	}
	
	*broadcastIP {
		var line, pipe;
		pipe = Pipe("ifconfig | grep broadcast | awk '{print $NF}'", "r");
		{ line = pipe.getLine }.protect { pipe.close };
		^line
	}
	
	*broadcast { | port = 57120, prefix = "" |
		var hostname = this.broadcastIP(prefix);
		if(hostname.isNil) { 
			hostname = "127.0.0.1"; 
			"no network with broadcast available."
			" provisionally used loopback instead.".collect(_.toUpper).warn;
		};
		^NetAddr(hostname, port)
	}
	
	*getSect { |thisDict, thatDict|
		var res = ();
		thisDict.pairsDo { |key, val|
			thatDict[key] !? {
				if(thatDict[key] == val) { res.put(key, val) };
			}
		};
		^res

	}
	
	*compareUGenStrings { |str1, str2|
		var res = 0;
		str1.do { |char, i|
			if(str2[i] == char) { res = res + 1 } {
				if(str2.includes(char)) { res = res + 0.5 } { res = res - 0.5 };
			};
		}
		^res / (str1.size + str2.size * 0.5)
	
	}
	
	// we'll have to write UGen groupings by hand, probably.
	
	*findUGensFor { |str, n = 4|
		var similarities = ugenClasses.collect { |name|
			
			[this.compareUGenStrings(str, name), name]
		};
		similarities.sort { |a, b| a[0] > b[0] };
		^similarities.keep(n).collect(_.at(1)).collect(_.asSymbol).collect(_.asClass);
	}
	
	*makeDefaultUGenGraph {|str, args, n = 4|
		var ugens = this.findUGensFor(str, n);
		var argData = ugens.collect(this.getArgsForUGen(_));
		var methodOfFirst = this.findUGenMethod(ugens.first);
		var graph;
		args.do { |val, i|
			var key = methodOfFirst.argNames[i];
			key !? {
				argData.do { |dict| dict.put(key, val) }
			} 
		};
		graph = ugens.collect { |ugen, i|
			ugen.performWithEnvir(this.findUGenMethod(ugen).name, argData[i])
		};
		graph.postcs;
		graph = graph * (n..1).normalizeSum;
		^graph.sum
	}
	
	*getArgsForUGen { |ugen|
			var method = this.findUGenMethod(ugen);
			^method !? { method.makeEnvirFromArgs }
	}
	
	*findUGenMethod { |ugen|
		var class = ugen.class;
		^class.findRespondingMethodFor(\kr) 
				?? { class.findRespondingMethodFor(\ar) } 
				?? { class.findRespondingMethodFor(\new) } // THIS ALWAYS RETURNS SOMETHING
	}

}

