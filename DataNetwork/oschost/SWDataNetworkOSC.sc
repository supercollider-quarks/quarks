// this is a class to manage outgoing osc streams to other applications.
// other applications can register to the network, request info, and subscribe to slots or nodes.

SWDataNetworkOSC{

	classvar <>httppath;

	var <verbose;

	var <>maxMissedPongs = 10;

	var <hiveDictionary;

	var <clientDictionary;
	//	var <clients;
	var <setters;
	var <network;
	var <watcher;

	var <clientPorts;
	var responders;


	var <>gui;

	var <logfile;
	var <logging = false;

	var myhost;

	var <hiveBlockAllocator;
	var <hiveNotifier;

	*initClass{
		Platform.case(
			\osx, { this.httppath = "/Library/WebServer/Documents/"; },
			\linux, { this.httppath = "/var/www/" },
			\windows, { "Please set SWDataNetworkOSC.httppath to an appropriate path!".warn }
		);
	}

	*new{ |netw|
		^super.new.init( netw );
	}

	init{ |netw|
		verbose = Verbosity.new( 0, \datanetworkOSC );
		network = netw;
		network.osc = this;
		clientDictionary = IdentityDictionary.new;
		hiveDictionary = IdentityDictionary.new;
		hiveNotifier = SWAsyncNotifier.new;
		//	clients = Array.new;
		clientPorts = List.new;
		setters = IdentityDictionary.new;

		hiveBlockAllocator = ContiguousBlockAllocator.new( 256 );

		this.createResponders;

		watcher = SkipJack.new( { 
			this.sendPings;
		}, 1, name: "SWDataNetworkOSC" , autostart: true );

		this.announce;

		ShutDown.add( { this.stop });
	}

	makeGui{
		^SWDataNetworkOSCGui.new( this );
	}

	makeHiveGui{
		^this.hiveDictionary.collect{ |it|
			it.gui;
		}
	}

	stop{
		this.activeClientsDo( 'hostQuit', [ myhost ] );
	//	clientDictionary.do{ |it| it.hostQuit( myhost ); };
		this.logMsg("datanetwork stopped" );
		this.removeResponders;
	}

	sendPings{
		clientDictionary.do{ |it| 
			it.ping;
			if ( it.active ){
				if ( it.missedPongs > maxMissedPongs )
				{ 
					this.removeClient( it.addr, it.key );
				} 
			};
		};
	}

	removeResponders{
		responders.do{ |it| it.remove };
	}

	createResponders{
		responders = [
			/// REGISTRATION
			OSCresponderNode( nil, '/register', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.addClient( addr, msg[2] );
				}{
					verbose.value( 0, "missing port in message" );
				};
			}),
			OSCresponderNode( nil, '/unregister', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.removeClient( addr, msg[2] );
				}{ 
					verbose.value( 0, "missing port in message" );
				};
			}),

			OSCresponderNode( nil, '/pong', { |t,r,msg,addr|
				var client;
				verbose.value( 3, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1];
					client = this.findClient( addr, msg[2] );
					if ( client.notNil, { client.pong } ) ;
				}{ 
					verbose.value( 0, "missing port in message");
				};
			}),

			/// QUERIES

			OSCresponderNode( nil, '/query/all', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.allQuery( addr, msg[2] );
				}{ 
					verbose.value( 0, "missing port in message" ); 
				};
			}),
			OSCresponderNode( nil, '/query/expected', { |t,r,msg,addr|
				verbose.value( 1, { msg.postln; } );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.expectedQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/nodes', { |t,r,msg,addr|
				verbose.value( 1, { msg.postln; } );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.nodeQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/slots', { |t,r,msg,addr|
				verbose.value( 1, { msg.postln; } );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.slotQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/clients', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.clientQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/subscriptions', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.subscriptionQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/setters', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.setterQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),

			/// SUBSCRIPTIONS

			OSCresponderNode( nil, '/subscribe/all', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.allNodeSubscribe( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/unsubscribe/all', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.allNodeUnsubscribe( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/subscribe/node', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.nodeSubscribe( addr, msg[2],  msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/unsubscribe/node', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.nodeUnsubscribe( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/subscribe/slot', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.slotSubscribe( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/unsubscribe/slot', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.slotUnsubscribe( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),

			// SETTING, LABELING

			OSCresponderNode( nil, '/add/expected', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.addExpected( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/set/data', { |t,r,msg,addr|
				verbose.value( 2, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.setData( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/label/slot', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.labelSlot( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/label/node', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.labelNode( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			/// GETTING
			OSCresponderNode( nil, '/get/node', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.getNode( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/get/slot', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.getSlot( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			/// REMOVING

			OSCresponderNode( nil, '/remove/node', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.removeNode( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/remove/all', { |t,r,msg,addr|
				verbose.value( 1, { msg.postln;} );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.removeAll( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			/// MAPPING TO MINIBEE OUTPUT

			OSCresponderNode( nil, '/info/minibee', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.minibeeInfo( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),

			OSCresponderNode( nil, '/status/minibee', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.minibeeStatus( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),

			OSCresponderNode( nil, '/query/minibees', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.minibeeQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message"); };
			}),

			// mapping datanodes to minibees
			OSCresponderNode( nil, '/map/minibee/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHiveOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/map/minibee/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHiveCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmap/minibee/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmapHiveOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmap/minibee/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmapHiveCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			OSCresponderNode( nil, '/mapped/minibee/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mappedHiveOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/mapped/minibee/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mappedHiveCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmapped/minibee/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmappedHiveOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmapped/minibee/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmappedHiveCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			// end mapping datanodes to minibees

			// mapping datanodes to broadcat of bees
			OSCresponderNode( nil, '/map/minihive/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHiveAllOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/map/minihive/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHiveAllCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmap/minihive/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmapHiveAllOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmap/minihive/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmapHiveAllCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),

			OSCresponderNode( nil, '/mapped/minihive/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mappedHiveAllOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/mapped/minihive/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mappedHiveAllCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmapped/minihive/output', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmappedHiveAllOutput( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/unmapped/minihive/custom', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.unmappedHiveAllCustom( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),


			/// obsolete :
			OSCresponderNode( nil, '/map/minibee/pwm', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHivePWM( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			OSCresponderNode( nil, '/map/minibee/digital', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.mapHiveDig( addr, msg[2], msg.copyToEnd( 3 ) );
				}{ verbose.value( 0, "missing port in message" ); };			}),
			/// end - obsolete

			// CLIENT HIVE MANAGEMENT

			OSCresponderNode( nil, '/register/hive', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.addHiveClient( addr, msg[2], msg[3] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/unregister/hive', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.removeHiveClient( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/hives', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.hiveQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/query/configurations', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.configQuery( addr, msg[2] );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/info/configuration', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.configInfo( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/configure/minibee', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.configMiniBee( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/configured/minibee', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.configuredMiniBee( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/create', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.createConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/created', { |t,r,msg,addr|
				// from minihive
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.createdConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/delete', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.deleteConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/deleted', { |t,r,msg,addr|
				// from minihive
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.deletedConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/save', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.saveConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/saved', { |t,r,msg,addr|
				// from minihive
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.savedConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/load', { |t,r,msg,addr|
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.loadConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/configuration/loaded', { |t,r,msg,addr|
				// from minihive
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.loadedConfig( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			}),
			OSCresponderNode( nil, '/minihive/error', { |t,r,msg,addr|
				// from minihive
				verbose.value( 1, msg );
				if ( msg.size > 1 ){
					addr.port = msg[1]; this.errorFromHive( addr, msg[2], msg.copyToEnd(3) );
				}{ verbose.value( 0, "missing port in message" ); };
			})
		];
		responders.do{ |it| it.add };
	}

	activeClientsDo{ |method,args|
		clientDictionary.do{ |it|
			if ( it.active ){
				it.performList( method, args );
			}
		}
	}

	activeClientsDoThis{ |method,args|
		clientDictionary.do{ |it|
			if ( it.active ){
				this.performList( method, [it] ++ args );
			}
		}
	}

	

	// ---------- autoconnection and recovery support -------

	announce{ |ports|
		var b,broadcastip;
		var prefix;
		var cips;
		var file;

		// write the file:
		file = File.open( httppath +/+ "SenseWorldDataNetwork", "w");
		if ( file.isOpen.not ){
			"Could not write file with port information, clients may not be able to find the host. See [SW_Apache_setup] for instructions to make this work properly".warn;
			this.logMsg("WARNING: could not write apache file");
		}{ // file is open:
			file.write( NetAddr.langPort.asString);
			file.close;
		};

		if ( ports.isNil, {
			ports = (6000..6011) ++ (57120..57129) ++ 57000 ++ 57600 ++ 7000;
		});

		ports = ports ++ clientPorts;

		NetAddr.broadcastFlag_( true );

		if ( thisProcess.platform.name == \linux, {
			prefix = "/sbin/";
		},{ prefix = "" });

		broadcastip = NetAddr.broadcastIP( prefix );
		myhost = NetAddr.atMyIP( NetAddr.langPort, prefix );

		ports.do{ |it|
			// LAN:
			NetAddr.new( broadcastip, it ).sendMsg( 
				"/datanetwork/announce", myhost.hostname, myhost.port.asInteger );
		};

		cips = this.restoreClientsIPs;
		if ( cips.notNil ){
			cips.do{ |jt|
				ports.do{ |it|
					NetAddr.new( jt, it ).sendMsg( 
						"/datanetwork/announce", myhost.hostname, myhost.port.asInteger );
				};
			};
		};
		
		// localhost
		ports.do{ |it|
			NetAddr.new( "127.0.0.1", it ).sendMsg( 
				"/datanetwork/announce", "127.0.0.1", myhost.port.asInteger );
			};

		this.logMsg( "network announced" );
	}

	backupClientsIPs{ |name|
		var file;
		if ( name.isNil ){
			name = "SWDataNetworOSC_clientIPs";
		}{
			name = (name++"IPS");
		};
		file = File.open( Platform.userAppSupportDir +/+ name, "w" );
		file.write( clientDictionary.collect{ |it| it.addr.addr.asIPString }.asArray.asCompileString );
		file.close;
	}

	restoreClientsIPs{ |name|
		var file,res;
		name = name ? "SWDataNetworOSC_clientIPs";
		file = File.open( Platform.userAppSupportDir +/+ name, "r" );
		if ( file.isOpen ){
			res = file.readAllString.interpret;
			file.close;
			^res;
		};
		file.close;
		^nil;
		//	file.write( clients.collect{ |it| it.addr.hostname }.asCompileString );
	}

	backupClients{ |name|
		name = name ? "SWDataNetworOSC_clients";
		this.backupClientsIPs( name );
		clientDictionary.collect{ |it| 
			[ it.addr, it.subscriptions.asArray,
				it.setters.collect{ |it| [it.id,it.data.size] }.asArray
			] }.asArray.writeArchive( Platform.userAppSupportDir +/+ name  );
	}

	restoreClients{ |name|
		var tcl,tc;
		name = name ? "SWDataNetworOSC_clients";
		tcl = Object.readArchive(  Platform.userAppSupportDir +/+ name );
		tcl.postcs;
		tcl.do{ |it|
			this.addClient( it[0] );
			tc = this.findClient( it[0] );

			// subscriptions
			it[1].do{ |jt|
				if ( jt.isKindOf( Array ), {
					tc.subscribeSlot( *jt );
				},{
					network.addExpected( jt );
					tc.subscribeNode( jt );
				});
			};

			// setters
			it[2].do{ |jt|
				network.addExpected( jt[0] );
				network.setData( jt[0], Array.fill( jt[1], 0 ) );
				network.nodes.postcs;
				setters.put( jt[0], tc.key );
				tc.addSetter( network.nodes.at( jt[0] ).postcs; );
			};
		};
	}

	// -------- 
	

	//------- Methods called by the network

	newExpected{ |id,label|
		this.activeClientsDo( 'newExpected', [id,label] );
		//		clientDictionary.do{ |it| it.newExpected( id, label );	}
	}

	newNode{ | node |
		this.activeClientsDo( 'newNode', [ node ] );
		//		clientDictionary.do{ |it| it.newNode( node ); }
	}

	newSlot{ | slot |
		this.activeClientsDo( 'newSlot', [ slot ] );
		//	clientDictionary.do{ |it| it.newSlot( slot ); }
	}

	newBee{ |bee|
		this.activeClientsDo( 'newBee', [ bee ] );
		//		clientDictionary.do{ |it| it.newBee( bee );	}
	}

	sendData{ |id,data|
		verbose.value( 2, ["sendData", id, data] );
		this.activeClientsDo( 'sendData', [id, data] );
		//	clientDictionary.do{ |it| it.sendData( id, data ); };
	}

	sendDataNode{ |node|
		verbose.value( 2, ["sendDataNode", node.id, node.data] );
		this.activeClientsDo( 'sendDataNode', [ node ] );
		//		clientDictionary.do{ |it| it.sendDataNode( node );	};
	}

	nodeRemoved{ |id|
		this.activeClientsDo( 'nodeRemoved', [ id ] );
		//		clientDictionary.do{ |it| it.nodeRemoved( id ) };
	}

	///--------- subscriptions and data retrieval

	allNodeSubscribe{ |addr,name|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/subscribe/all", 15, [name] );
		},{
			network.nodes.do{ |it| 
				client.subscribeNode( it.id );
				this.getNode( addr, client.key, [it.id] );
				this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"subscribed to node:"+it.id );
			};
			this.logMsg( "/subscribe/all from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	nodeSubscribe{ |addr,name,msg|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/subscribe/node", 15, [name] );
		},{
			client.subscribeNode( msg[0].asInteger );
			this.getNode( addr, client.key,  msg );
			this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"subscribed to node:"+msg[0] );
		});
	}

	slotSubscribe{ |addr,name,msg|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/subscribe/slot", 15, [name] );
		},{
			client.subscribeSlot( msg[0].asInteger, msg[1].asInteger );
			this.getSlot( addr, client.key, msg );
			this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"subscribed to slot:"+msg.copyRange(0,1) );
		});
	}


	allNodeUnsubscribe{ |addr,name|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unsubscribe/all", 15, [name]);
		},{
			client.unsubscribeAll;
			this.logMsg( "/unsubscribe/all from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	nodeUnsubscribe{ |addr,name,msg|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unsubscribe/node", 15, [name] );
		},{
			client.unsubscribeNode( msg[0].asInteger );
			this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"unsubscribed from node:"+msg[0] );
		});
	}

	slotUnsubscribe{ |addr,name,msg|
		var client;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unsubscribe/slot", 15, name );
		},{
			client.unsubscribeSlot( msg[0].asInteger, msg[1].asInteger );
			this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"unsubscribed from slot:"+msg.copyRange(0,1) );
		});
	}


	getNode{ |addr,name,msg|
		var client,data,node;
		//[addr,msg].postln;
		client = this.findClient( addr, name.asSymbol );
		//client.postln;
		if ( client.isNil, {
			this.errorMsg( addr, "/get/node", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.isNil, {
				this.warnMsg( addr, "/get/node", 5, msg );
			},{
				data = [ '/data/node', msg[0], node.data ].flatten;
				addr.sendMsg( *data );
			});
			this.logMsg( "/get/node:" + msg[0] + "from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	getSlot{ |addr,name,msg|
		var client,node;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/get/slot", 15, [name]);
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.isNil, {
				this.warnMsg( addr, "/get/slot", 5, msg );
			},{
				addr.sendMsg( '/data/slot', msg[0], msg[1], node.slots.at( msg[1].asInteger ).value );
			});
			this.logMsg( "/get/slot:" + msg.copyRange(0,1) + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	// --------- client management -----------

	findClient{ |addr,name|
		if ( name.notNil ){
			^clientDictionary.select( { |it| (it.addr == addr) and: (it.key == name.asSymbol) } ).asArray.first;
		}{ // don't check for name
			^clientDictionary.select( { |it| it.addr == addr } ).asArray.first;
		}
	}

	welcomeClientBack{ |client|
		//	("welcoming client back" + client.addr + client.key ).postln;
		client.welcomeBack;
		client.nodeSubs.do{ |it|
			if ( network.nodes[it].notNil){
				client.newNode( network.nodes[it] );
			}
		};
		client.setters.do{ |it|
			//		("setter"+it.id + client.addr).postln;
			setters.put( it.id, client.key );
		};
	}

	addClient{ |addr,name|
		var there,newclient,there2;
		there = this.findClient( addr );
		if ( there.isNil, { 
			// no client had that IP and port before
			// see if the client exists by name in the library
			there = clientDictionary.at( name.asSymbol );
			if ( there.notNil){
				// address may have changed, so we reset it:
				there.addr = addr;
				this.welcomeClientBack( there );
				if ( gui.notNil ){ 
					gui.addClient( there );
				};
				this.logMsg( "client reregistered:"+(addr.asString.replace( "a NetAddr",""))+name );

			}{
				if ( addr.port > 0){
					//		"new client".postln;
					clientPorts.add( addr.port );
					newclient = SWDataNetworkOSCClient.new( addr, name.asSymbol );
					newclient.sendRegistered;
					//	clients = clients.add( newclient );
					//	watcher.start;
					clientDictionary.put( name.asSymbol, newclient );
					if ( gui.notNil ){ 
						gui.addClient( newclient );
					};
					this.logMsg( "client registered:"+(addr.asString.replace( "a NetAddr",""))+name );
				};
			};
		},{
			//	[ there.key, name.asSymbol ].postln;
			//	"client had same ip and port, welcome back".postln;
			if ( there.key != name.asSymbol ){
				// name may have changed.
				clientDictionary.removeAt( there.key );
				there.key = name.asSymbol;
				clientDictionary.put( name.asSymbol, there );
			};
			//	[ there.key, name.asSymbol ].postln;
			//	there.addr = addr;
			this.welcomeClientBack( there );				
			this.logMsg( "client reregistered:"+(addr.asString.replace( "a NetAddr",""))+name );				
		});
	}

	removeClient{ |addr,name|
		var there;
		there = this.findClient( addr );

		//		[addr,there].postln;
		//		thisProcess.dumpBackTrace;

		if ( there.notNil, { 
			if ( there.key == name.asSymbol ){
				/// removed, as we keep the client in the dictionary
				there.active = false;
				//	clientDictionary.removeAt( there );
				addr.sendMsg( '/unregistered', addr.port.asInteger, there.key );
				if ( gui.notNil ){
					gui.removeClient( there );
				};
				this.logMsg( "client unregistered:"+(addr.asString.replace( "a NetAddr","")) );
			}{
				this.errorMsg( addr, "/unregister", 14, [name] );
				this.logMsg( "client tried to unregister:"+(addr.asString.replace( "a NetAddr",""))+"but was not succesful" );

			};
		},{
			this.errorMsg( addr, "/unregister", 3 );
				this.logMsg( "client tried to unregister:"+(addr.asString.replace( "a NetAddr",""))+"but was not succesful" );
		} );

		//		there2 = setters.findKeyForValue( addr );
		//		if ( there2.notNil, { setters.removeAt( there2 ) } );
	}

	//// ------ HIVE CLIENT -------

	allocHiveNodes{ |size|
		^hiveBlockAllocator.alloc( size );
	}

	freeHiveNodes{ |id|
		hiveBlockAllocator.free( id );
	}

	// client could already be there, but not a hive client -> adapt!
	addHiveClient{ |addr,name,nonodes|
		var there,newclient,hiveBlock;
		there = this.findClient( addr );
		if ( there.isNil, { 
			// no client had that IP and port before
			// see if the client exists by name in the library
			there = hiveDictionary.at( name.asSymbol );
			if ( there.notNil){
				// client exists as hive client
				// address may have changed, so we reset it:
				there.addr = addr;
				// TODO: check if range is the same
				//	hiveBlock = this.allocHiveNodes( nonodes );
				//	there.setRange( hiveBlock, hiveBlock + nonodes );
				this.welcomeClientBack( there );
				if ( gui.notNil ){ 
					gui.addClient( there );
				};
				this.logMsg( "hive client reregistered:"+(addr.asString.replace( "a NetAddr",""))+name );

			}{
				// maybe the client was there, but not as a hive client
				there = clientDictionary.at( name.asSymbol );
				if ( there.notNil){
					// address may have changed, so we reset it:
					there.addr = addr;
					hiveBlock = this.allocHiveNodes( nonodes );
					there = SWDataNetworkOSCHiveClient.newFrom( there, hiveBlock, hiveBlock + nonodes );
					//	clients = clients.add( there );
					hiveDictionary.put( name.asSymbol, there );
					this.welcomeClientBack( there );
					if ( gui.notNil ){ 
						gui.addClient( there );
					};
					this.logMsg( "client became hive client and reregistered:"+(addr.asString.replace( "a NetAddr",""))+name );	
				}{
					// completely new client:
					if ( addr.port > 0){
						//		"new client".postln;
						clientPorts.add( addr.port );
						hiveBlock = this.allocHiveNodes( nonodes );
						newclient = SWDataNetworkOSCHiveClient.new( addr, name.asSymbol, hiveBlock, hiveBlock + nonodes );
						newclient.sendRegistered;
						//	clients = clients.add( newclient );
						//	watcher.start;
						clientDictionary.put( name.asSymbol, newclient );
						hiveDictionary.put( name.asSymbol, newclient );
						if ( gui.notNil ){ 
							gui.addClient( newclient );
						};
						this.logMsg( "hive client registered:"+(addr.asString.replace( "a NetAddr",""))+name );
					};
				};
			};
		},{
			//	"client had same ip and port, welcome back".postln;
			there = hiveDictionary.at( name.asSymbol );
			//	there.postln;
			if ( there.notNil ){
				there.addr = addr;
				// TODO: should actually check if the requested range is the same
				//	hiveBlock = this.allocHiveNodes( nonodes );
				//	there.setRange( hiveBlock, hiveBlock + nonodes );
				this.welcomeClientBack( there );
				if ( gui.notNil ){ 
					gui.addClient( there );
				};				
				this.logMsg( "hiveclient reregistered:"+(addr.asString.replace( "a NetAddr",""))+name );				
			}{
				there = clientDictionary.at( name.asSymbol );
			//	there.postln;
				if ( there.notNil ){
					there.addr = addr;
					hiveBlock = this.allocHiveNodes( nonodes );
					there = SWDataNetworkOSCHiveClient.newFrom( there, hiveBlock, hiveBlock + nonodes );
					this.welcomeClientBack( there );
					if ( gui.notNil ){ 
						gui.addClient( there );
					};	
					this.logMsg( "client reregistered as hive client:"+(addr.asString.replace( "a NetAddr",""))+name );				
				}{
					this.errorMsg( addr, "/register/hive", 2);
				};
			};
		});
	}


	removeHiveClient{ |addr,name|
		var there,there2;
		there = this.findClient( addr );
		//		[addr,there].postln;

		if ( there.notNil, { 
			if ( there.key == name.asSymbol ){
				/// removed, as we keep the client in the dictionary
				//	there.setters.do{ |node| setters.removeAt( node.id ) };
				there.active = false;
				//	clients.remove( there );
				this.freeHiveNodes( there.nodeRange[0] );
				addr.sendMsg( '/unregistered/hive', addr.port.asInteger, there.key );
				if ( gui.notNil ){
					gui.removeClient( there );
				};
				this.logMsg( "hive client unregistered:"+(addr.asString.replace( "a NetAddr","")) );
			}{
				this.errorMsg( addr, "/unregister/hive", 14, [name] );
				this.logMsg( "hive client tried to unregister:"+(addr.asString.replace( "a NetAddr",""))+"but was not succesful" );
			};
		},{
			this.errorMsg( addr, "/unregister/hive", 3 );
				this.logMsg( "hive client tried to unregister:"+(addr.asString.replace( "a NetAddr",""))+"but was not succesful" );
		} );
	}

	/// announcing a minibee

	minibeeInfo{ |addr,name,msg|
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/info/minibee", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				res = client.addBee( msg[0].asInteger, msg[1].asInteger, msg[2].asInteger );
				if ( res ){
					this.sendAllClientsBeeInfo( msg[0], msg[1] );
					this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"added minibee:"+msg[0] );
					}{
						this.errorMsg( addr, "/info/minibee", 17, [name, msg[0]] ++ client.nodeRange );
					};
			}{
				this.errorMsg( addr, "/info/minibee", 16, [name,msg[0]] );
			}
		});
	}

	minibeeStatus{ |addr,name,msg|
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/status/minibee", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				res = client.statusBee( msg[0].asInteger, msg[1] );
				if ( res ){
					this.sendAllClientsBeeStatus( msg[0], msg[1] );
					this.logMsg( "client:"+(client.addr.asString.replace( "a NetAddr",""))+"minibee status:"+msg[0]+msg[1] );
				}{
					this.errorMsg( addr, "/status/minibee", 19, [name, msg[0]] ++ client.nodeRange );
				};				
			}{
				this.errorMsg( addr, "/status/minibee", 16, [name,msg[0]] );
			}
		});
	}

	configInfo{ |addr,name,msg|
		// receiving config info message from hive client, passing it on to clients that requested the information
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/info/configuration", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				client.sendConfigInfo( msg );
			}{
				this.errorMsg( addr, "/info/configuration", 18, [name,msg[0]] );
			}
		});
	}

	configMiniBeeLocal{ | beeid, configid |
		// find hive that has this minibee
		hiveDictionary.do{ |it|
			it.configureBee( beeid, configid );
		};
	}

	configMiniBee{ |addr,name,msg|
		// message from an arbitrary client, should be passed onto the correct minihive client to set the configuration for the minibee.
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/configure/minibee", 15, [name] );
		},{
			this.configMiniBeeLocal( msg[0], msg[1] );
			hiveNotifier.add( '/configure/minibee', msg[0], client, '/configured/minibee' );
		});
	}

	configuredMiniBee{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/configured/minibee", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/configure/minibee', msg[0], msg.copyToEnd( 1 ) );
			}{
				this.errorMsg( addr, "/configured/minibee", 16, [name,msg[0]] );
			}
		});
	}

	mappedHiveOutput{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/mapped/minibee/output", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/map/minibee/output', msg[1], msg[0] );
				network.performHook( \mappedOutput, msg[0] );
			}{
				this.errorMsg( addr, "/mapped/minibee/output", 16, [name,msg[1]] );
			}
		});
	}

	mappedHiveCustom{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/mapped/minibee/custom", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/map/minibee/custom', msg[1], msg[0] );
				network.performHook( \mappedCustom, msg[0] );
			}{
				this.errorMsg( addr, "/mapped/minibee/custom", 16, [name,msg[1]] );
			}
		});
	}

	unmappedHiveOutput{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unmapped/minibee/output", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/unmap/minibee/output', msg[1], msg[0] );
				network.performHook( \unmappedOutput, msg[0] );
			}{
				this.errorMsg( addr, "/unmapped/minibee/output", 16, [name,msg[1]] );
			}
		});
	}

	unmappedHiveCustom{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unmapped/minibee/custom", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/unmap/minibee/custom', msg[1], msg[0] );
				network.performHook( \unmappedCustom, msg[0] );
			}{
				this.errorMsg( addr, "/unmapped/minibee/custom", 16, [name,msg[1]] );
			}
		});
	}

	mappedHiveAllOutput{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/mapped/minihive/output", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/map/minihive/output', msg[0] );
				network.performHook( \mappedOutput, msg[0] );
			}{
				this.errorMsg( addr, "/mapped/minihive/output", 16, [name,msg[0]] );
			}
		});
	}

	unmappedHiveAllOutput{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unmapped/minihive/output", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/unmap/minihive/output', msg[0] );
				network.performHook( \unmappedOutput, msg[0] );
			}{
				this.errorMsg( addr, "/unmapped/minihive/output", 16, [name,msg[0]] );
			}
		});
	}

	mappedHiveAllCustom{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/mapped/minihive/custom", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/map/minihive/custom', msg[0] );
				network.performHook( \mappedCustom, msg[0] );
			}{
				this.errorMsg( addr, "/mapped/minihive/custom", 16, [name,msg[0]] );
			}
		});
	}

	unmappedHiveAllCustom{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/unmapped/minihive/custom", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/unmap/minihive/custom', msg[0] );
				network.performHook( \unmappedOutput, msg[0] );
			}{
				this.errorMsg( addr, "/unmapped/minihive/custom", 16, [name,msg[0]] );
			}
		});
	}


	createConfigLocal{ |cid,config|
		// find hive that has this minibee
		hiveDictionary.do{ |it|
			it.createConfig( cid, config );
		};
	}

	createConfig{ |addr,name,msg|
		// message from an arbitrary client, should be passed onto the correct minihive client to set the configuration for the minibee.
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/create", 15, [name] );
		},{
			this.createConfigLocal( msg[0], msg.copyToEnd( 1 ) );
			hiveNotifier.add( '/minihive/configuration/create', msg[0], client, '/minihive/configuration/created' );
		});
	}

	createdConfig{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/created", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/minihive/configuration/create', msg[0], msg.copyToEnd( 1 ) );
			}{
				this.errorMsg( addr, "/minihive/configuration/created", 16, [name,msg[0]] );
			}
		});
	}

	deleteConfigLocal{ |cid|
		// delete config for all hives
		hiveDictionary.do{ |it|
			it.deleteConfig( cid );
		};
	}

	deleteConfig{ |addr,name,msg|
		// message from an arbitrary client, should be passed onto the correct minihive client to set the configuration for the minibee.
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/delete", 15, [name] );
		},{
			this.deleteConfigLocal( msg[0] );
			hiveNotifier.add( '/minihive/configuration/delete', msg[0], client, '/minihive/configuration/deleted' );
		});
	}

	deletedConfig{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/deleted", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/minihive/configuration/delete', msg[0] );
			}{
				this.errorMsg( addr, "/minihive/configuration/deleted", 16, [name,msg[0]] );
			}
		});
	}

	saveConfigLocal{ |filename|
		hiveDictionary.do{ |it|
			it.saveConfig( filename );
		};
	}

	loadConfigLocal{ |filename|
		hiveDictionary.do{ |it|
			it.loadConfig( filename );
		};
	}

	saveConfig{ |addr,name,msg|
		// message from an arbitrary client, should be passed onto the correct minihive client to set the configuration for the minibee.
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/save", 15, [name] );
		},{
			this.saveConfigLocal( msg[0] );
			hiveNotifier.add( '/minihive/configuration/save', msg[0], client, '/minihive/configuration/saved' );
		});
	}

	savedConfig{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/saved", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/minihive/configuration/save', msg[0] );
			}{
				this.errorMsg( addr, "/minihive/configuration/saved", 16, [name,msg[0]] );
			}
		});
	}

	loadConfig{ |addr,name,msg|
		// message from an arbitrary client, should be passed onto the correct minihive client to set the configuration for the minibee.
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/load", 15, [name] );
		},{
			this.loadConfigLocal( msg[0] );
			hiveNotifier.add( '/minihive/configuration/load', msg[0], client, '/minihive/configuration/loaded' );
		});
	}

	loadedConfig{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/configuration/loaded", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.successResult( '/minihive/configuration/load', msg[0] );
			}{
				this.errorMsg( addr, "/minihive/configuration/loaded", 16, [name,msg[0]] );
			}
		});
	}

	errorFromHive{ |addr,name,msg|
		// from minihive should be sent on to the client that requested it
		var client,res;
		client = this.findClient( addr, name.asSymbol );
		if ( client.isNil, {
			this.errorMsg( addr, "/minihive/error", 15, [name] );
		},{
			if ( client.isKindOf( SWDataNetworkOSCHiveClient ) ){
				hiveNotifier.errorResult( msg[0], msg.copyToEnd( 1 ) ); //TODO: this should be a nice string instead
			}{
				this.errorMsg( addr, "/minihive/error", 16, [name,msg[0]] );
			}
		});		
	}


	//------- Queries -------

	allQuery{ |addr,name|
		this.logMsg( "/query/all from client with IP"+addr.ip+"and port"+addr.port );
		this.expectedQuery( addr, name );
		this.nodeQuery( addr, name );
		//this.slotQuery( addr, name );
		this.clientQuery( addr, name );
		this.subscriptionQuery( addr, name );
		this.setterQuery( addr, name );
		this.minibeeQuery( addr, name );
		this.hiveQuery( addr, name );
		this.configQuery( addr, name );
	}

	expectedQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/expected", 15, [name] );
		},{
			if ( network.expectedNodes.size == 0, {
				this.warnMsg( addr, "/query/expected", 7 );
			});
			network.expectedNodes.do{ |key|
				addr.sendMsg( '/info/expected', key );
			};
		});
		this.logMsg( "/query/expected from client with IP"+addr.ip+"and port"+addr.port );
	}

	nodeQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/nodes", 15, [name] );
		},{
			if ( network.nodes.size == 0, {
				this.warnMsg( addr, "/query/nodes", 8 );
			});
			network.nodes.keysValuesDo{ |key,node|
				addr.sendMsg( '/info/node', key, node.key.asString, node.slots.size, node.type );
			};
		});
		this.logMsg( "/query/nodes from client with IP"+addr.ip+"and port"+addr.port );
	}

	slotQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/slots", 15, [name] );
		},{
			if ( network.nodes.size == 0, {
				this.warnMsg( addr, "/query/slots", 8 );
			});
			network.nodes.keysValuesDo{ |key,node|
				node.slots.do{ |it,i|
					addr.sendMsg( '/info/slot', key, i, it.key.asString, it.type );
				};
			};
		});
		this.logMsg( "/query/slots from client with IP"+addr.ip+"and port"+addr.port );
	}

	clientQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/clients", 15, [name] );
		},{
			if ( clientDictionary.size == 0, {
				this.warnMsg( addr, "/query/clients", 9 );
			});
			this.activeClientsDo( 'sendClientInfo', [addr] );
			//	clientDictionary.do{ |it| it.sendClientInfo( addr );};
		});
		this.logMsg( "/query/clients from client with IP"+addr.ip+"and port"+addr.port );
	}

	subscriptionQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/subscriptions", 1 );
		},{
			if( client.subscriptionQuery ){
				this.logMsg( "/query/subscriptions from client with IP"+addr.ip+"and port"+addr.port );
			}{
				this.warnMsg( addr, "/query/subscriptions", 11 );
			};

		});
		this.logMsg( "/query/subscriptions from client with IP"+addr.ip+"and port"+addr.port );
	}

	setterQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/setters", 1);
		},{
			if( client.setterQuery ){
				this.logMsg( "/query/setters from client with IP"+addr.ip+"and port"+addr.port );
			}{
				this.warnMsg( addr, "/query/setters", 10 );
			};
		});
	}

	// -----------------------

	/// -------- node control by clients --------

	setData{ |addr,name,msg|
		var there, addsetter, node, code;
		addsetter = false;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/set/data", 15, [name] );
		},{
			// node id:
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			//	node.postln;
			if ( node.isNil,
				{ // it's a new node for the network:
					if ( setters.at( msg[0] ).isNil, {
						// and there was no setter yet, so client becomes the setter
						setters.put( msg[0], name.asSymbol );
					});
					// if the client is the setter:
					if ( setters.at( msg[0] ) == name.asSymbol ){
						if ( network.isExpected( msg[0] ).not,
							{
								this.errorMsg( addr, "/set/data", 6, msg );
							}, {
								network.setData( msg[0], msg.copyToEnd( 1 ) );
								// .asFloat ); // no conversion, since it can be a string node now.
								// has to look up the newly created node:
								there.addSetter( network.nodes.at( msg[0] ) );
								// only send data back to sender first time the node is set, for confirmation:
								this.getNode( addr, there.key, msg );
								this.logMsg( "client:"+(there.addr.asString.replace( "a NetAddr",""))+"became setter of node:"+msg[0] );
							});
					}{
						// but someone else claimed it already!
						// client isn't the setter
						this.errorMsg( addr, "/set/data", 4, msg );
					};
				},{
					// it's an already existing node (less checks, more efficient!)
					if ( there.checkForSetter( node ) ){
						// client is the setter:
						code = network.setData( msg[0], msg.copyToEnd( 1 ) );
						//.asFloat ); // can be a string node now.
						if ( code != 0 ){ // error occured:
							if ( code == 1 ){
								this.errorMsg( addr, "/set/data", 6, msg );
							};
							if ( code == 2 ){
								this.errorMsg( addr, "/set/data", 12, msg );
							};
						};
					}{
						// client isn't the setter
						this.errorMsg( addr, "/set/data", 4, msg );
					};
				});
		});
	}

	removeAll{ |addr,name|
		var there;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/remove/all", 15, [name]);
		},{
			there.setters.do{ |nd|
				if ( network.nodes.at( nd.id ).notNil,
					{
						network.removeNode( nd.id );
						addr.sendMsg( '/removed/node', nd.id );
					},{
						this.errorMsg( addr, "/remove/all", 5, [nd.id]);
				});
			};
			this.logMsg( "/remove/all: from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	removeNode{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/remove/node", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						// old version:
						//	if ( setters.at( msg[0] ) == addr, {
						network.removeNode( msg[0] );
						addr.sendMsg( '/removed/node', msg[0] );
					},{
						this.errorMsg( addr, "/remove/node", 4, msg );
					});
				},{
					this.errorMsg( addr, "/remove/node", 5, msg );
				});
			this.logMsg( "/remove/node:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	minibeeQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/minibees", 15, [name] );
		},{
			network.getBeeInfo( addr );
			// look for hive clients with bees
			hiveDictionary.do{ |it|
				it.activeBees.do{ |jt|
					this.sendBeeInfo( it, jt.id, jt.inputs, jt.outputs );
				}
			};
		});
		this.logMsg( "/query/minibees from client with IP"+addr.ip+"and port"+addr.port );
	}

	hiveQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/hives", 15, [name] );
		},{
			this.getHiveInfo( addr );
		});
		this.logMsg( "/query/hives from client with IP"+addr.ip+"and port"+addr.port );
	}

	configQuery{ |addr,name|
		var client;
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/query/configurations", 15, [name] );
		},{
			this.getConfigInfo( addr, name );
		});
		this.logMsg( "/query/configurations from client with IP"+addr.ip+"and port"+addr.port );
	}

	getHiveInfo{ |addr|
		hiveDictionary.do{ |it|
			it.sendHiveInfo( addr );
		}
	}

	getConfigInfo{ |addr,name|
		hiveDictionary.do{ |it|
			it.queryConfigInfo( addr );
			//	it.sendConfigInfo( addr );
		}
	}

	sendBeeNoInfo{ |addr|
		this.warnMsg( addr, "/query/minibees", 8 );
	}

	sendAllClientsBeeInfo{ |id,insize,outsize|
		this.activeClientsDoThis( 'sendBeeInfo', [ id, insize, outsize ] );
		//	clientDictionary.do{ |it| this.sendBeeInfo( it.addr, id, insize,outsize ); }
	}

	sendAllClientsBeeStatus{ |id,status|
		this.activeClientsDoThis( 'sendBeeStatus', [ id, status ] );
		//	clientDictionary.do{ |it| this.sendBeeStatus( it.addr, id, status ); }
	}

	sendBeeInfo{ |client, id,insize,outsize|
		verbose.value( 1, [client.addr, id, insize, outsize ].asCompileString );
		client.addr.sendMsg( '/info/minibee', id, insize, outsize );
	}

	sendBeeStatus{ |client, id, status|
		verbose.value( 1, [client.addr, id, status ].asCompileString );
		client.addr.sendMsg( '/status/minibee', id, status );
	}

	runMiniBee{ |beeid,status|
		hiveDictionary.do{ |it|
			it.runMiniBee( beeid, status );
		};		
	}

	saveIDMiniBee{ |beeid|
		hiveDictionary.do{ |it|
			it.saveIDMiniBee( beeid );
		};		
	}

	announceMiniBee{ |beeid|
		hiveDictionary.do{ |it|
			it.announceMiniBee( beeid );
		};		
	}

	loopMiniBee{ |beeid,status|
		hiveDictionary.do{ |it|
			it.loopMiniBee( beeid, status );
		};
	}

	resetMiniBee{ |beeid|
		hiveDictionary.do{ |it|
			it.resetMiniBee( beeid );
		};		
	}

	mapHiveOutputLocal{ |nodeid, beeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.mapHiveOutput( nodeid, beeid );
		};
	}

	unmapHiveOutputLocal{ |nodeid, beeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.unmapHiveOutput( nodeid, beeid );
		};
	}

	mapHiveCustomLocal{ |nodeid, beeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.mapHiveCustom( nodeid, beeid );
		};
	}

	unmapHiveCustomLocal{ |nodeid, beeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.unmapHiveCustom( nodeid, beeid );
		};
	}

	mapHiveAllOutputLocal{ |nodeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.mapHiveAllOutput( nodeid );
		};
	}

	unmapHiveAllOutputLocal{ |nodeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.unmapHiveAllOutput( nodeid );
		};
	}

	mapHiveAllCustomLocal{ |nodeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.mapHiveAllCustom( nodeid );
		};
	}

	unmapHiveAllCustomLocal{ |nodeid|
		//	network.mapHiveOutput( nodeid, beeid ); // TODO: still needed?
		// look for hive clients with bees
		hiveDictionary.do{ |it|
			it.unmapHiveAllCustom( nodeid );
		};
	}


	mapHiveOutput{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minibee/output", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						this.mapHiveOutputLocal( msg[0], msg[1] );
						hiveNotifier.add( '/map/minibee/output', msg[1], there, '/mapped/minibee/output' );
						//		addr.sendMsg( '/mapped/minibee/output', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minibee/output", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minibee/output", 5, msg );
				});
			this.logMsg( "/map/minibee/output:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	unmapHiveOutput{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/unmap/minibee/output", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//	network.unmapHiveOutput( msg[0], msg[1] );
						// look for hive clients with bees
						//	hiveDictionary.do{ |it|
						//	it.unmapHiveOutput( msg[0], msg[1] );
						//};
						this.unmapHiveOutputLocal( msg[0], msg[1] );
						hiveNotifier.add( '/unmap/minibee/output', msg[1], there, '/unmapped/minibee/output' );
						//	addr.sendMsg( '/unmapped/minibee/output', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/unmap/minibee/output", 4, msg );
					});
				},{
					this.errorMsg( addr, "/unmap/minibee/output", 5, msg );
				});
			this.logMsg( "/unmap/minibee/output:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	mapHiveCustom{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minibee/custom", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//network.mapHiveCustom( msg[0], msg[1] );
						//hiveDictionary.do{ |it|
						//	it.mapHiveCustom( msg[0], msg[1] );
						//};
						this.mapHiveCustomLocal( msg[0], msg[1] );
						hiveNotifier.add( '/map/minibee/custom', msg[1], there, '/mapped/minibee/custom' );
						//		addr.sendMsg( '/mapped/minibee/custom', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minibee/custom", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minibee/custom", 5, msg );
				});
			this.logMsg( "/map/minibee/custom:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	unmapHiveCustom{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/unmap/minibee/custom", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//network.unmapHiveCustom( msg[0], msg[1] );
						//	hiveDictionary.do{ |it|
							//	it.unmapHiveCustom( msg[0], msg[1] );
							//};
						this.unmapHiveCustomLocal( msg[0], msg[1] );
						//	addr.sendMsg( '/unmapped/minibee/custom', msg[0], msg[1] );
						hiveNotifier.add( '/unmap/minibee/custom', msg[1], there, '/unmapped/minibee/custom' );
					},{
						this.errorMsg( addr, "/unmap/minibee/custom", 4, msg );
					});
				},{
					this.errorMsg( addr, "/unmap/minibee/custom", 5, msg );
				});
			this.logMsg( "/unmap/minibee/custom:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}




	mapHiveAllOutput{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minihive/output", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						this.mapHiveAllOutputLocal( msg[0] );
						hiveNotifier.add( '/map/minihive/output', msg[0], there, '/mapped/minibee/output' );
						//		addr.sendMsg( '/mapped/minibee/output', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minihive/output", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minihive/output", 5, msg );
				});
			this.logMsg( "/map/minihive/output:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	unmapHiveAllOutput{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/unmap/minihive/output", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//	network.unmapHiveOutput( msg[0], msg[1] );
						// look for hive clients with bees
						//	hiveDictionary.do{ |it|
						//	it.unmapHiveOutput( msg[0], msg[1] );
						//};
						this.unmapHiveAllOutputLocal( msg[0] );
						hiveNotifier.add( '/unmap/minihive/output', msg[0], there, '/unmapped/minihive/output' );
						//	addr.sendMsg( '/unmapped/minibee/output', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/unmap/minihive/output", 4, msg );
					});
				},{
					this.errorMsg( addr, "/unmap/minihive/output", 5, msg );
				});
			this.logMsg( "/unmap/minihive/output:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	mapHiveAllCustom{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minihive/custom", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//network.mapHiveCustom( msg[0], msg[1] );
						//hiveDictionary.do{ |it|
						//	it.mapHiveCustom( msg[0], msg[1] );
						//};
						this.mapHiveAllCustomLocal( msg[0] );
						hiveNotifier.add( '/map/minihive/custom', msg[0], there, '/mapped/minibee/custom' );
						//		addr.sendMsg( '/mapped/minibee/custom', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minihive/custom", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minihive/custom", 5, msg );
				});
			this.logMsg( "/map/minihive/custom:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	unmapHiveAllCustom{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/unmap/minihive/custom", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						//network.unmapHiveCustom( msg[0], msg[1] );
						//	hiveDictionary.do{ |it|
							//	it.unmapHiveCustom( msg[0], msg[1] );
							//};
						this.unmapHiveAllCustomLocal( msg[0] );
						//	addr.sendMsg( '/unmapped/minibee/custom', msg[0], msg[1] );
						hiveNotifier.add( '/unmap/minihive/custom', msg[0], there, '/unmapped/minibee/custom' );
					},{
						this.errorMsg( addr, "/unmap/minihive/custom", 4, msg );
					});
				},{
					this.errorMsg( addr, "/unmap/minihive/custom", 5, msg );
				});
			this.logMsg( "/unmap/minihive/custom:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}


	// is deprecated:
	mapHivePWM{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minibee/pwm", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						// old version:
						//	if ( setters.at( msg[0] ) == addr, {
						network.mapHivePWM( msg[0], msg[1] );
						addr.sendMsg( '/mapped/minibee', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minibee/pwm", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minibee/pwm", 5, msg );
				});
			this.logMsg( "/map/minibee/pwm:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}

	// is deprecated:
	mapHiveDig{ |addr,name,msg|
		var there,node;
		there = this.findClient( addr, name.asSymbol );
		if ( there.isNil, {
			this.errorMsg( addr, "/map/minibee/digital", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			node = network.nodes.at( msg[0] );
			if ( node.notNil,
				{
					if ( there.checkForSetter(node), {
						// old version:
						//	if ( setters.at( msg[0] ) == addr, {
						network.mapHiveDig( msg[0], msg[1] );
						addr.sendMsg( '/mapped/minibee', msg[0], msg[1] );
					},{
						this.errorMsg( addr, "/map/minibee/digital", 4, msg );
					});
				},{
					this.errorMsg( addr, "/map/minibee/digital", 5, msg );
				});
			this.logMsg( "/map/minibee/digital:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
		});
	}


	/// TODO: why am I using a different tactic here??? Fix check for name!

	labelNode{ |addr,name,msg|
		var client;
		// a client should be registered before being able to add expected nodes
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/label/node", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			if ( network.nodes.at( msg[0] ).isNil and: setters.at( msg[0] ).isNil,
				{
					setters.put( msg[0], name.asSymbol );
				});
			if ( network.isExpected( msg[0] ), {
				this.warnMsg( addr, "/label/node", 6, msg );
				if ( setters.at( msg[0] ) == name.asSymbol, {
					network.add( msg[1], msg[0] );
				},{
					this.warnMsg( addr, "/label/node", 4, msg );
				});
			});
		});
		this.logMsg(  "/label/node:" + msg[0] + " from client with IP"+addr.ip+"and port"+addr.port );
	}

	labelSlot{ |addr,name,msg|
		var client;
		// a client should be registered before being able to add expected nodes
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/label/slot", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			if ( network.nodes.at( msg[0] ).isNil and: setters.at( msg[0] ).isNil,
				{
					setters.put( msg[0], name.asSymbol );
				});
			if ( network.isExpected( msg[0] ).not, {
				this.errorMsg( addr, "/label/slot", 6, msg );
			}, {
				if ( setters.at( msg[0] ) == name.asSymbol, {
					network.add( msg[2], [msg[0], msg[1].asInteger] );
				},{
					this.warnMsg( addr, "/label/slot", 4, msg );
				});
			});
		});
		this.logMsg( "/label/slot:" + msg.copyRange(0,1) + " from client with IP"+addr.ip+"and port"+addr.port );
	}


	addExpected{ |addr,name,msg|
		var client;
		// a client should be registered before being able to add expected nodes
		client = this.findClient( addr, name );
		if ( client.isNil, {
			this.errorMsg( addr, "/add/expected", 15, [name] );
		},{
			msg[0] = msg[0].asInteger;
			if ( msg[1].notNil ){
				msg[1] = msg[1].asInteger;
			};
			if ( network.nodes.at( msg[0] ).isNil and: setters.at( msg[0] ).isNil,
				{
					setters.put( msg[0], name.asSymbol );
				});
			if ( setters.at( msg[0] ) == name.asSymbol, {
				switch( msg.size,
					4, { network.addExpected( msg[0], msg[2], msg[1], msg[3] ); },
					3, { network.addExpected( msg[0], msg[2], msg[1] ); },
					2, { network.addExpected( msg[0], size: msg[1] ); },
					1, { network.addExpected( msg[0] ); }
				);
				if ( msg[1].notNil, {
					if ( network.nodes.at( msg[0] ).notNil){
						client.addSetter( network.nodes.at( msg[0] ) );
					};
				})
			},{
				this.warnMsg( addr, "/add/expected", 4, msg );
			});
		});
		this.logMsg("/add/expected:" + msg + " from client with IP"+addr.ip+"and port"+addr.port );
	}

	// ---------------	

	getErrorString{ |addr,id,msg|
		var string;
		switch( id,
			1, { string = "Client with IP"+addr.ip+"and port"+addr.port+"is not registered. Please register first." },
			2, { string = "Client with IP"+addr.ip+"and port"+addr.port+"is already registered. Please unregister first" },
			3, { string = "Client with IP"+addr.ip+"and port"+addr.port+"was not registered."},
			4, { string = "Client with IP"+addr.ip+"and port"+addr.port+"is not the setter of node with id"+msg[0] },
			5, { string = "Node with id"+msg[0]+"is not part of the network" },
			6, { string = "Node with id"+msg[0]+"is not expected to be part of the network" },
			7, { string = "There are no expected nodes in the network" },
			8, { string = "There are no nodes in the network" },
			9, { string = "There are no clients in the network" },
			10, { string = "Client with IP"+addr.ip+"and port"+addr.port+"has no setters"},
			11, { string = "Client with IP"+addr.ip+"and port"+addr.port+"has no subscriptions" },
			12, { string = "Node with id"+msg[0]+"does not have"+(msg.size-1)+"slots" },
			13, { string = "Node with id"+msg[0]+"has wrong type"+msg[3] },
			14, { string = "Client with IP"+addr.ip+"and port"+addr.port+"was not registered under name" + msg[0] },
			15, { string = "Client with IP"+addr.ip+"and port"+addr.port+"and name" + msg[0] + "is not registered. Please register first" },
			16, { string = "Client with IP"+addr.ip+"and port"+addr.port+"and name" + msg[0] + "tried to add a minibee with id" + msg[1] + ", but is not a hive client"},
			17, { string = "Client with IP"+addr.ip+"and port"+addr.port+"and name" + msg[0] + "tried to add a minibee with id" + msg[1] + ", which is out of range of the hive"},
			18, { string = "Client with IP"+addr.ip+"and port"+addr.port+"and name" + msg[0] + "sent a minibee configuration with id" + msg[1] + ", but is not a hive client"},
			19, { string = "Client with IP"+addr.ip+"and port"+addr.port+"and name" + msg[0] + "sent a minibee status update with id" + msg[1] + ", but the minibee is not part of the hive"}
		);	
		^string;
	}

	errorMsg{ |addr,request,id,msg|
		var string = this.getErrorString( addr, id, msg );
		addr.sendMsg( '/error', request, string, id );
		this.logMsg( "/error" + request + string );
	}

	warnMsg{ |addr,request,id,msg|
		var string = this.getErrorString( addr, id, msg );
		addr.sendMsg( '/warn', request, string, id );
		this.logMsg( "/warn" + request + string );
	}

	logMsg{ |string|
		if ( gui.notNil ){
			gui.addLogMsg( string );
		};
		if ( logging ){
			this.writeLogLine( string );
		};
	}

	// recording
	initLog{ |fn|
		fn = fn ? "SWDataNetworkOSCLog";
		logfile =  File(fn++"_"++Date.localtime.stamp++".txt", "w");
		logging = true;
		ShutDown.add( { this.closeLog; });
	}

	writeLogLine{ |line|
		logfile.write( Date.localtime.asString );
		logfile.write( "\t" );
		logfile.write( line.asString );
		logfile.write( "\n" );
	}

	closeLog{
		logging = false;
		logfile.close;
	}
}

