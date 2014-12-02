SSHServer : Server { 
	
	classvar <>sshLogins;
	
		// hack to enable ssh server control
		// any streaming from disk options don't work..
		
	var <sshLogin = "";
	var <>scDirRemote = "/Applications/SuperCollider/";
	
	isLocal_ { |bool = true| isLocal = bool; }
	
	*new { arg name, addr, options, clientID=0, sshLogin;
		^super.new(name, addr, options, clientID)
			.isLocal_( true ) // act as local server
			.remoteControlled_( true )
			.sshLogin_( sshLogin );
		}
			
	sshFullLogin { ^(sshLogin++"@"++addr.addr.asIPString) }
	
	addSSHLogin {
		var fullLogin;
		fullLogin = this.sshFullLogin.asSymbol;
		if( sshLogins.asCollection.includes( fullLogin.asSymbol ).not )
			{ sshLogins = [ fullLogin ] ++ sshLogins.asCollection };
		}
			
	sshLogin_	 { |newLogin|
		sshLogin = newLogin ? sshLogin;
		this.addSSHLogin;
		}
		
	bootServerApp {
		if (inProcess, { 
			"booting internal".inform;
			this.bootInProcess; 
		},{ unixCmd( "ssh" + this.sshFullLogin.quote +
			"'cd" + scDirRemote.quote + ";" +
			"./scsynth" ++ options.asOptionsString(addr.port) + "'");
			("booting " ++ addr.port.asString).inform;
		});
	}
	
	*quitAll { 
		
		Server.set.select({ |item| item.class == SSHServer }).do({ arg server;
			if ((server.sendQuit === true)
				or: {server.sendQuit.isNil and: {server.isLocal or: {server.inProcess}}}) {
				server.quit
			};
		})
		//		set.do({ arg server; if(server.isLocal or: {server.inProcess} ) {server.quit}; })
	
		}
	
	*killAll { |login|
		(if( login.isNil ) { sshLogins } { [ login ] })
			.do({ |li| "ssh '%' '%'".format( li, "killall -9 scsynth" ); });
		this.quitAll;
		}
	
	}