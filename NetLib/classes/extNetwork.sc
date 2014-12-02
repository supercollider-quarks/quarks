
+NetAddr {

	*ifconfigPath{ |prefix=""|
		if ( prefix == "" ){
			Platform.case(
				\linux, { prefix = "/sbin/"}
			);
		};
		if ( prefix == "" ){
			^"ifconfig";
			}{
			^(prefix+/+"ifconfig");
		};
	}

	// check this again!!
	*myIP { |prefix="",device=""|
		var j, k, res, bc;
		// prefix argument, since on Linux ifconfig does not lie in the user's path, but in /sbin/, so it can be passed as an argument (nescivi, April 2008)
		
		res = Pipe.findValuesForKey( (this.ifconfigPath(prefix)+device), "inet");
		bc = this.broadcastIP( prefix, device );

		if ( res.notNil, {
			// fix for Linux output: (nescivi, April 2008)
			res.do{ |it,i| res[i] = it.replace("addr:",""); };
		});

		if(bc.notNil) {
			// broadcast starting bits of IP are not necessary of length 3 each, so we need to calculate the first two fields
			k = 0;
			2.do{ |i| k = k + bc.find(".", offset: k);	};
			bc = bc.keep(k+1);
			if ( bc != "255.255", { // generic broadcast mask should not prevent us from finding a valid myIP
				res = res.select { |x| x.beginsWith(bc) }; // choose match with broadcast
			} );
		};
		
		if ( res.notNil, {
			// don't want to check for netmask here.
			// need a simpler and better solution later.
			res = res.reject(_ == "127.0.0.1");

		
			if(res.size > 1) { postln("the first of those devices was chosen: " ++ res) };
			res = res.first;
		});

		
		^res ?? { 
			"chosen loopback IP, no other ip available".warn; 
			"127.0.0.1"
		}
	}

	// convenience method (added by nescivi, April 2008)
	*atMyIP { |port=57110,prefix=""|
		var hostname = this.myIP(prefix);
		^NetAddr(hostname, port )
	}
	
	*broadcastIP { arg prefix = "", device = "";
		^Platform.case(
			\linux, {
				var  res,k,delimiter=$ ;
				res = Pipe.findValuesForKey( this.ifconfigPath(prefix)+device, "broadcast");
				res = res ++ Pipe.findValuesForKey( this.ifconfigPath(prefix)+device, "Bcast", $:);
		
				if(res.size > 1) { postln("the first of those devices were chosen: " ++ res) };
				res.do{ |it,i|
					k = it.find(delimiter.asString) ?? { it.size } - 1;
					res[i] = (it[0..k]);
				};
				res.first
			},
			\osx, {
				var line, pipe;
				pipe = Pipe( (this.ifconfigPath(prefix)+"% | grep broadcast | awk '{print $NF}'")
					.format(device), 
					"r"
				);
				{ line = pipe.getLine }.protect { pipe.close };
				line
			}
		)
	}
	
	*broadcast { arg port = 57120, prefix="", device = "";
		var hostname = this.broadcastIP(prefix, device);
		if(hostname.isNil) { 
			hostname = "127.0.0.1"; 
			"no network with broadcast available. provisionally used loopback instead.".warn;
		};
		^NetAddr(hostname, port)
	}
	
	// assume a broadcast (see NamedNetAddr)
	sendNamedMsg { arg ... args;
		this.sendMsg(*args.insert(1, \broadcast))
	}
	listSendNamedMsg { arg args;
		this.sendMsg(*args.insert(1, \broadcast))
	}

}

+Pipe {
	*do { arg commandLine, func; 
		var line, pipe = this.new(commandLine, "r"), i=0;
		{
			line = pipe.getLine;
			while { line.notNil } {
				func.value(line, i);
				i = i + 1;
				line = pipe.getLine;
			}
		}.protect { pipe.close };
		
	}
	*findValuesForKey { arg commandLine, key, delimiter=$ ;
		var j, k, indices, res, keySize;
		key = key ++ delimiter;
		keySize = key.size;
		Pipe.do(commandLine, { |l|
			indices = l.findAll(key);
			indices !? {
				indices.do { |j|
					j = j + keySize;
					while { l[j] == delimiter } { j = j + 1 };
					k = l.find(delimiter.asString, offset:j) ?? { l.size } - 1;
					res = res.add(l[j..k])
				};
			};
		});
		^res
	}
	
}

/*

+String {
	// a markov set would maybe be better
	
	*rand { arg length = 8, nCapitals = 0, pairProbability = 0.2;
		var consonants = "bcdfghjklmnpqrstvwxz";
		var vowels = "aeiouy";
		var cweight = #[ 0.07, 0.03, 0.07, 0.06, 0.07, 0.03, 0.01, 0.07, 0.07, 0.06, 0.07, 0.06, 
								0.01, 0.06, 0.07, 0.07, 0.01, 0.03, 0.01, 0.04 ];
		var vweigth = #[ 0.19, 0.19, 0.19, 0.19, 0.19, 0.07 ];
		var lastWasVowel = false;
		var last, res, ci, breakCluster=false;
		res = this.fill(length, { |i|
						var vowel = if(breakCluster.not and: {pairProbability.coin}) 
									{ÊbreakCluster = true; lastWasVowel.not } 
									{ breakCluster = false; lastWasVowel };
						if(vowel) {
							lastWasVowel = false;
							last = vowels.wchoose(vweigth)
						} { 
							lastWasVowel = true;
							last = if(last == $q) { $u } { 
								consonants.wchoose(cweight)
							};
						};
		});
		if(nCapitals > 0) {
			ci = [0] ++ (2..length-2).scramble.keep(nCapitals - 1);
			if(ci.size < nCapitals) { ci = ci.add(length-1) };
			if(ci.size < nCapitals) { ci = ci.add(1) };
			ci.do {|i|
				res[i] = res[i].toUpper;
			};
		};
		^res
	}
}

+Symbol {
	*rand { arg length=8, nCapitals=0;
		^String.rand(length, nCapitals).asSymbol
	}
}

*/

