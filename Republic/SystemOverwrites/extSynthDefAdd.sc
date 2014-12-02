
+ SynthDef {

	share { | republic |
		republic = republic ? Republic.default;
		if(republic.isNil) {
				////////////////////////////////////////////////// v
			if(Main.versionAtMost(3, 3)) { this.memStore } { this.prAdd }
		} {
			republic.addSynthDef(this)
		}
	}
	
	*unshare { |name|
		^this.notYetImplemented;
	}
	
	/*	
	*unshare { |name|
		var republic = republic ? Republic.default;
		if(republic.notNil) { 
			republic.removeSynthDef(this)
		}
		
	}*/
	
	add { this.share; }
	
		// temp hack, use try to keep backwards compatibility.
	prAdd { arg libname, completionMsg, keepDef = true;
		try {
			var	servers;
			this.asSynthDesc(libname ? \global, keepDef);
			if(libname.isNil) { 
				servers = Server.allRunningServers
			} {
				servers = SynthDescLib.getLib(libname).servers
			};
			this.sendOrLoad(servers, completionMsg);
		} {
			var	lib, desc = this.asSynthDesc(libname, keepDef);
			libname ?? { libname = \global };
			lib = SynthDescLib.getLib(libname);
			lib.servers.do { |each|
				each.value.sendMsg("/d_recv", this.asBytes, completionMsg.value(each))
			};
		}
	}

	store { this.prStore.share; }
	
		// temp hack
	prStore { arg libname=\global, dir(synthDefDir), completionMsg, mdPlugin;
		var lib = SynthDescLib.getLib(libname);
		var file, path = dir ++ name ++ ".scsyndef";
		if(metadata.falseAt(\shouldNotSend)) {
			protect {
				var bytes, desc;
				file = File(path, "w");
				bytes = this.asBytes;
				file.putAll(bytes);
				file.close;
				lib.read(path);
				lib.servers.do { arg server;
					server.value.sendMsg("/d_recv", bytes, completionMsg)
				};
				desc = lib[this.name.asSymbol];
				desc.metadata = metadata;
				SynthDesc.populateMetadataFunc.value(desc);
				desc.writeMetadata(path);
			} {
				file.close
			}
		} {
			lib.read(path);
			lib.servers.do { arg server;
				this.loadReconstructed(server, completionMsg);
			};
		};
	}
}

