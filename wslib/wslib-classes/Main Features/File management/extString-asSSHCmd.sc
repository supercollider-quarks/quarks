+ String {

	asSSHCmd { |login, addr|
		
		/* 
		This only works for authenticated hosts. Login can be user:password or just user
		If sshCmd fails (result = 255) you might want to try sshRunInTerminal and type the password 
		when required. sshCmd will only work straight from sc if a ssh-keygen file is created and
		placed in the proper location of the remote machine. 
		look at the ssh manual for details:
		
		"man ssh".runInTerminal;
		
		*/
		
		if ( login.isNil ) { login = "USER".getenv;
		"String:asSSHCmd : no loginname provided; using local username instead\n".warn;  
			};
		
		if ( addr.isNil  ) { addr = NetAddr.localAddr; 
			"String:asSSHCmd : no addr provided; using local addr instead (test mode)\n".warn;  };
			
		^"ssh" + login.asString ++ "@" ++ addr.addr.asIPString + this.asCompileString;

		}
	
	sshCmd { |login, addr, perform = \unixCmd | var cmd; cmd = this.asSSHCmd( login, addr );
			cmd !? { ^cmd.perform( perform ); } }
			
	sshSystemCmd { |login, addr| ^this.sshCmd( login, addr, \systemCmd ); }
	sshRunInTerminal { |login, addr| ^this.sshCmd( login, addr, \runInTerminal ); }
	
	}
	
	