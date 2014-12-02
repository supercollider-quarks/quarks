			// use Ndef.defaultServer in new and printOn methods. 
+ Ndef {
	
	*new { | key, object |
		// key may be simply a symbol, or an association of a symbol and a server name
		var res, server, dict;
		if(key.isArray) { 
			^[key, object].flop.collect { |pair| this.new(*pair) }
		};
		if(key.isKindOf(Association)) {
			server = Server.named.at(key.value);
			if(server.isNil) {
				Error("Ndef(%): no server found with this name.".format(key)).throw
			};
			key = key.key;
		} {
			server = defaultServer ? Server.default;
		};

		dict = this.dictFor(server);
		res = dict.envir.at(key);
		if(res.isNil) {
			res = super.new(server).key_(key);
			dict.envir.put(key, res)
		};

		object !? { res.source = object };
		^res;
	}
	
	printOn { | stream |
		var serverString = if (server == (Ndef.defaultServer ? Server.default)) { "" } { 
			" ->" + server.name.asCompileString;
		};
		stream << this.class.name << "(" <<< this.key << serverString << ")"
	}
}