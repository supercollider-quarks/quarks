/*
    Unit Library
    The Game Of Life Foundation. http://gameoflife.nl
    Copyright 2006-2011 Miguel Negrao, Wouter Snoei.

    GameOfLife Unit Library: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GameOfLife Unit Library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GameOfLife Unit Library.  If not, see <http://www.gnu.org/licenses/>.
*/

SyncCenter {	
	classvar <>mode, <>verbose = false;
	
	classvar <>serverCounts, <master;
	classvar <>recSynths;
	classvar <>masterCountTime;
	classvar <>responder;
	classvar <>global;
	classvar <>ready;
	classvar <>inBus = 0, <>outBus = 14;
	classvar <>latency = 0.2;
	
	classvar <>current;
	
	classvar <>checkForServerQuit = false;
	
	classvar <round = 128;
	
	var <>localCount, <>localCountTime;
	
	*testSampleSched { // test whether this is a sample-sched enabled version of SC
		^'BlockCount'.asClass.notNil;
	}
	
	*initClass {
		
		if( this.testSampleSched ) {
			mode = 'sample'; 
		} { 
			mode = 'timestamp'; 
		};
		serverCounts = Dictionary.new;
		global = this.new;
		ready = Ref(false);
	}
	
	*round_ { |new = 128| round = new; this.changed( \round, round ) }
	
	*servers{ ^serverCounts.keys.select({ |sv| sv != master }).as(Array) }
	
	*add { |server|
		var index;
		if( server.isKindOf( Server ).not ) { 
				"%.add: added item should be a Server".format( this ).warn; 
		} {
			if( this.servers.includes( server ) ) { 
				"%.add: server '%' was already added".format( this, server ).warn; 
			} { 
				serverCounts.put(server,Ref(-1));
				NotificationCenter.register(server, \didQuit, this, { 
					if( checkForServerQuit ) {
						serverCounts.at(server).value_(-1).changed 
					}
				});
			}
		};
	}
		
	*addAll { |array| array.do( this.add( _ ) ); }
	
	*master_ { |server|
		master = server;
		this.add(server);
	}
	
	*recDef { 
		^SynthDef( "sync_receive", { |id = 100, in = 0 |
			// waits for an impulse
			var trig;
			trig = Trig1.ar( SoundIn.ar( in ) - 0.001 );
			SendTrig.ar( trig, id, BlockOffset.ar( trig ) );
		});
	}
			
	*masterDefs { 
		^[SynthDef( "sync_master", { |out = 0, amp = 0.1, id = 99|
			var trig;
			trig = OneImpulse.ar; // also frees itself
			OffsetOut.ar( out, trig * amp );
			SendTrig.ar( trig, id, SpawnOffset.ir );
		}),
		SynthDef( "sync_local_master", {
			SendTrig.ar( OneImpulse.ar, 98, SpawnOffset.ir );
		})
		]
		
	}
	
	*writeDefs{
		if( mode === 'sample' ) {
			this.recDef.writeDefFile;
			this.masterDefs.do(_.writeDefFile);
		};
	}
	
	*loadMasterDefs{
		if( mode === 'sample' ) {
			this.masterDefs.do( _.load( master ) )
		};
	}
	
	*sendDefs { |write = false|
		var recdev;
		if( mode === 'sample' ) {
			recdev = this.recDef;
				
			if( write ) {
				this.servers.do({ |server| recdev.load( server ) });
				this.masterDefs.do( _.load( master ) );
			} { 
				this.servers.do({ |server| recdev.send( server ) });
				this.masterDefs.do( _.send( master ) )
			};
		}
	}
		
	*playRecDefs { 
		var recdev;
		if( mode === 'sample' ) {
			recdev = this.recDef;
			recSynths = recSynths.addAll( 
				this.servers.collect({ |server, i|  
					if(verbose) { ("playing receve sync synthdef for server %, id %\n").postf(server, 100+i) };
					Synth( "sync_receive", [\id, 100+i,\in,inBus], server ).register
				}) 
			); 
		}
	}
		
	*remoteSync { |waitTime = 0.5|
		var counter=0;
		ready.value_(false).changed;
		if( mode === 'sample' ) { 

			if( responder.notNil ) {
				responder.remove; 
				if( verbose ) { "removing responder".postln }
			};
			
			serverCounts.pairsDo{ |server,count|
				count.value_(-1).changed
			};
				
			responder = OSCresponderNode( nil, '/tr', { |time, resp, msg|
				var server, numOfBlocks, offsetInsideBlock, count;
				
				case{ msg[ 2 ] == 99 }
					{ 
						numOfBlocks = msg[4];
						offsetInsideBlock = msg[3];
						count = (numOfBlocks * master.options.blockSize) + offsetInsideBlock;
						serverCounts.at(master).value_( count ).changed; 
						
						current = this.new.localCount_( count )
							.localCountTime_( masterCountTime ); // also local sync
						this.class.changed( \localSync, current );
						serverCounts.do(_.changed); // update gui for all servers, in case localcount arrives late
						if( verbose ) { "setting master count : %\n".postf( count ) }; 
					}
					{ msg[2].exclusivelyBetween( 99, 100 + this.servers.size ) }
					{ 
						server = this.servers[msg[2] - 100];
						numOfBlocks = msg[4];
						offsetInsideBlock = msg[3];
						count = (numOfBlocks * server.options.blockSize) + offsetInsideBlock;
						
						serverCounts.at(server).value_( count ).changed;
						
						if(verbose) { 
							"Setting remoteCounts for server %, id % : %\n"
								.postf( server, msg[2], count )
						};	
					 } {
						 if( verbose ) { ("Received: "++[time, resp, msg] ).postln };
					 };
						
				if( 
					serverCounts.collect{ |count,server| 
						if( server.serverRunning ) {
							count.value != -1 
						} { 
							true 
						} 
					}.as(Array).reduce('&&')

				) { 
					resp.remove; responder = nil;
					ready.value_(true).changed;
					if(verbose) { "received all counts".postln;  }
				};
			}).add;
			
			this.playRecDefs;
			
			{
				masterCountTime = thisThread.seconds + waitTime;
				Synth.sched( waitTime, "sync_master", [\out, outBus], master, \addToHead );
				if( verbose ) { "playing impulse".postln };
				(waitTime + 0.5).wait; // wait another 0.5s for the messages to come in
				if( ready.value ) {
					this.changed( \synced );
					if( verbose ) { "Sync successful!".postln };
				} {
					this.changed( \notSynced );
					if( verbose ) {  "No Sync".postln };
				};
				responder.remove;
				responder = nil;
				recSynths.do({ |synth| if(synth.isPlaying){synth.free} });
				recSynths = nil;
				
			}.r.play;
		};
	}
		
	*localSync { |action|
		^this.new.localSync( true, action );
	}
	
	localSync { |makeCurrent = true, action|
		var synth, latency = 0.2, time, busy;
		if( mode === 'sample' ) {	
			time = thisThread.seconds + latency;
			busy = true;
			synth = Synth.sched( latency, "sync_local_master", target:master, 
					addAction: \addToHead )
				.onTrig_( { |value, time, responder, msg|
					var numOfBlocks, offsetInsideBlock;
					numOfBlocks = msg[4];
					offsetInsideBlock = msg[3];
					localCount = (numOfBlocks * master.options.blockSize) + offsetInsideBlock;
					localCountTime = time;
					busy = false;
					if( makeCurrent ) { this.class.current = this };
					this.class.changed( \localSync, this );
					if(SyncCenter.verbose) {
						 "Local sync successful!".postln;
					};
					action.value( this );
				}, 98 );
				
			if( SyncCenter.verbose ) {	
				{	0.5.wait;
					if( busy ) {
						 "Local sync failed (timeout)".postln;
					};		
				}.fork;
			};
		};
	}
	
	masterSampleCount { |delta = 0.2|
		// predicted sample count of now + delta
		var now;
		if( localCount.notNil and: localCountTime.notNil ) {
			now = thisThread.seconds;
			^localCount + ((now - localCountTime + delta) * master.sampleRate);
		} {	
			if(verbose) {
				 "No localCount/localCountTime available, falling back to blockCount".postln;
			};
			^(master.options.blockSize*master.blockCount) + 
				(delta*master.sampleRate); // fall back to blockCount
		};
	}
	
	getSampleCountForServer{ |delta = 0.2, server|
		if( server == master ) {
			^this.masterSampleCount( delta ).roundUp( round );
		} {
			^( serverCounts.at(server).value - serverCounts.at(master).value ) 
				+ this.masterSampleCount( delta ).roundUp( round );
		}
	}
	
	sendSyncedBundle { |server, delta = 0.2 ... msgs|
		if( (mode === 'sample')  && { serverCounts.keys.includes( server ) }) {
			server.sendPosBundle( delta!?{this.getSampleCountForServer(delta,server)}, *msgs ) 
		} {
			server.sendBundle( delta, *msgs ) // fall back to regular bundling (no warning)
		};
	}
	
	listSendSyncedBundle{ |server, delta = 0.2, msgs|
		if( (mode === 'sample') && { serverCounts.keys.includes( server ) } ) {
			server.listSendPosBundle( delta!?{this.getSampleCountForServer(delta,server)}, msgs ) 
		} {
			server.listSendBundle( delta, msgs );
		};
	}
	
	*sendSyncedBundle{ |server, delta = 0.2 ... msgs|
		current = current ?? { this.new }; // create empty if not there
		^current.sendSyncedBundle(server, delta, *msgs);
	}
	
	*listSendSyncedBundle{ |server, delta = 0.2, msgs|
		current = current ?? { this.new }; // create empty if not there
		^current.listSendSyncedBundle(server, delta, msgs);
	}
	
	*makeWindow {
		SyncCenterGui.new
	}
	
	*gui { ^SyncCenterGui.new }
	
	sched { |server, delta = 0.2, func|
		var bundle;
		server = server ? Server.default;
		bundle = server.makeBundle( false, func );
		this.listSendSyncedBundle( server, delta, bundle );
	}

	*sched { |server, delta = 0.2, func, sc| // can provide a sc
		sc = sc ?? { current = current ?? { this.new } }; // create empty if not there
		^sc.sched(server, delta, func);
	}


}
	