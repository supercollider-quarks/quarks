MultiServer {
	classvar <>default;
	
	var <>servers, <window, <>pointer = 0, <>hostName, synthDefDir;
	
	*new { |names, adresses, options, hostName, synthDefDir, clientID = 0 |
			// options and clientID are the same for every server
		^super.new.init( names ? [], adresses ? [], options, clientID, hostName, synthDefDir );
		}
		
	*row { |n = 1, startName = "server", startAddr, options, hostName, synthDefDir, clientID = 0|
		var names, adresses;
		startAddr = startAddr ? NetAddr( "127.0.0.1", 58000 );
		
		options = options ? Server.default.options;
		names = { |i| startName ++ (i+1); } ! n;
		adresses = { |i| NetAddr( startAddr.hostname, startAddr.port + i ); } ! n;
		
		^this.new( names, adresses, options, hostName, synthDefDir, clientID );
		
		}
		
	*rowFindHost { |hostName, n = 1, startName = "server", startPort = 58000, options, 
			synthDefDir, clientID = 0|
		var addr;
		options = options ? Server.default.options;
		if( hostName.isNil )
			{ addr = NetAddr( "127.0.0.1", startPort ); }
			{ addr = NetAddr( hostName.findReplaceAll( " ", "-" )
			.replaceExtension( "local" ), startPort );  };
		^this.row( n, startName, addr, options, hostName, synthDefDir, clientID );
		}
		
	makeDefault { default = this }
		
	init { |argNames, argAddr, argOptions, argClientID, argHostName, argSynthDefDir|
		
		servers = argNames.collect({ |name, i|
			Server( name, argAddr.wrapAt(i), argOptions, argClientID);
			});
		
		hostName = argHostName ? "";
		synthDefDir = argSynthDefDir ?? { SynthDef.synthDefDir; };
		
		}
	
	synthDefDir { ^"/Volumes/" ++ hostName ++ "/" ++ synthDefDir; }
	

	openHost { |login, password, serverName|
		if( ( "/Volumes/" ++ (serverName ? hostName) ).pathMatch.size == 0 )
			{ (serverName ? hostName).openServer( login, password, hostName ); }
			{ ("/Volumes/" ++ (serverName ? hostName) ++ "/").openInFinder; } 
		}
		
	sendMsg { arg ... msg;
		servers.do( _.sendMsg(*msg) );
	}
	
	sendBundle { arg time ... msgs;
		servers.do( _.sendBundle(time, *msgs) );
	}
	
	listSendMsg { arg msg;
		servers.do( _.sendMsg(*msg) );
	}
	
 	listSendBundle { arg time, msgs;
		servers.do( _.sendBundle(time, *msgs) );
	}
	
	makeWindow {
	
		//var window;
		if( window.notNil && { window.dataptr.notNil }) { window.front; ^this };
		
		window = SCWindow("MultiServer", Rect(10, 10, 400, 3 + ( servers.size * 29))).front;
		window.view.decorator = FlowLayout(window.view.bounds);
		servers.do({ |server| server.makeView( window ); });
		
		}
		
	add { |server|	
		servers = servers.add( server );
		}
		
	++ { |aMultiServer|
		case { aMultiServer.class == Server }
			{ this.add( aMultiServer )  }
			{ aMultiServer.class == MultiServer }
			{ servers = servers ++ aMultiServer.servers };
		}
		
	at { |index| ^servers.wrapAt( index ); }
		
	boot { |delay = 0|
		if( servers[0].addr.ip.asSymbol == '127.0.0.1' ) {
			if(delay>0){ 
				Routine({
					servers.do{ |server| 
						server.boot;
						delay.wait;
					}
				}).play
			}{
				servers.do(_.boot)
			}
 		}
	}
	
	quit {
		if( servers[0].addr.ip.asSymbol == '127.0.0.1' )
			{ servers.do( _.quit ) };
		}
	
	current { ^servers.wrapAt( pointer ); }
	next { pointer = (pointer + 1) % servers.size; ^this.current }
		
	}
		
	
			
		
		