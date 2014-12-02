MServer { 
	// wslib 2007
	// class for wrapping regular workflow to multiple Servers
	// ---- attempt at getting the most out of dual and quad processors:
	// -- OSX seems to do a pretty lousy job dividing threads among processors.
	// -- If you run only one server app on a multi-processer machine the thread
	// -- jumps from one processor to the other many times per second. This results
	// -- in a loss of overal CPU power, and also makes the Server app never use more
	// -- then one CPU at a time. My tests showed that a dual core machine running
	// -- one server app can handle about 20-30% less (!) then a single core machine with
	// -- the same cpu speed. 
	// -- This can be avoided by running as many Server apps as you have cpu's in your
	// -- machine and give them all just about the same amount of tasks to do. Only
	// -- then OSX stops switching CPU's and lets each Server app have more or less
	// -- its own cpu. Result is that your total cpu power is multiplied by your amount of
	// -- processor cores plus the 20-30% loss is cancelled. This goes for both Intel and
	// -- ppc processors.
	// -- Problem however is the workflow. How to use multiple Server apps in a way that
	// -- doesn't affect regular programming.
	// -- This class tries to solve that problem by behaving like a single server, but
	// -- dividing processes automatically in the background.
	
	classvar <>default, <>local;
	
	// defaults:
	classvar <>numServers = 2; // (change numServers to 4 for quad machines)
	classvar <>startPort = 58000;
	classvar <>useRoundButton = true; 
	
	var <>servers, <window, <>pointer = 0;
	
	*new { |name, addr, options, clientID=0, n|
			// options and clientID are the same for every server
		^super.new.init( 
			name.asString.dup(n ? numServers).collect( _ ++ _ ),
			(addr ? NetAddr( "127.0.0.1", 58000 ) ).dup(n ? numServers)
				.collect({ |item,i| item.port = item.port + i })
			, options, clientID);
		}
		
	*new1 { |names, adresses, options, hostName, synthDefDir, clientID = 0 |
			// options and clientID are the same for every server
		^super.new.init( names ? [], adresses ? [], options, clientID, hostName, synthDefDir );
		}
		
	makeDefault { default = this }
		
	init { |argNames, argAddr, argOptions, argClientID|
		
		servers = argNames.collect({ |name, i|
			Server( name, argAddr.wrapAt(i), argOptions, argClientID);
			});
		
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
		
		window = SCWindow("MServer", Rect(10, 10, 300, 27 + ( servers.size * 29))).front;
		window.view.decorator = FlowLayout(window.view.bounds);
		
		GUI.button.new( window, Rect( 0, 0, 56, 16 ) )
				.states_([["killAll", Color.black, Color.clear]])
				.font_( Font( "Monaco", 9 ) )
				.action_( { Server.killAll; } );
			
		GUI.button.new(window, Rect(0,0, 56, 16))
				.states_([["freeAll", Color.black, Color.clear]])
				.font_( Font( "Monaco", 9 ) )
				.action_( { Server.freeAll; } );
				
		GUI.popUpMenu.new(window, Rect(0,0, 48, 16))
				.items_([ "(rec" /*)*/ ] ++ servers.collect(_.name) )
				.font_( Font( "Monaco", 9 ) )
				.action_( { |pu| 
					ServerRecordWindow( servers[ pu.value - 1 ] );
					pu.value = 0;
					} );
					
		window.view.decorator.nextLine;	
			
		servers.do({ |server| server.makeView( window, useRoundButton ); });
		
		}
		
	add { |server|	
		servers = servers.add( server );
		}
		
	++ { |aMServer|
		case { aMServer.class == Server }
			{ this.add( aMServer )  }
			{ aMServer.class == MServer }
			{ servers = servers ++ aMServer.servers };
		}
		
	at { |index| ^servers.wrapAt( index ); }
		
	boot { 
		if( servers[0].addr.ip.asSymbol == '127.0.0.1' )
			{ servers.do( _.boot ) };
		}
	quit {
		if( servers[0].addr.ip.asSymbol == '127.0.0.1' )
			{ servers.do( _.quit ) };
		}
	
	current { ^servers.wrapAt( pointer ); }
	next { pointer = (pointer + 1) % servers.size; ^this.current }
		
	}