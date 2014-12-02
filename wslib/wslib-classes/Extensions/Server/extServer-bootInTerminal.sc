+ Server {
	
	bootInTerminal {
		if (inProcess, { // not in terminal
			"booting internal : not in Terminal".inform;
			this.bootInProcess; 
			pid = thisProcess.pid;
		},{
			(
			"echo ' '; echo booting " ++ addr.port.asString ++ "; echo ' ' ;" ++
			program ++ options.asOptionsString(addr.port)).runInTerminal;
			("booting " ++ addr.port.asString ++ " in Terminal").inform;
		});
	}

	}
	
+ SSHServer {

	bootInTerminal {
		if (inProcess, { 
			"booting internal".inform;
			this.bootInProcess; 
		},{ runInTerminal( "ssh" + this.sshFullLogin.quote +
			"'cd" + scDirRemote.quote + ";" +
			"./scsynth" ++ options.asOptionsString(addr.port) + "'");
			("booting " ++ addr.port.asString ++ "in Terminal").inform;
		});
	}
	
	}