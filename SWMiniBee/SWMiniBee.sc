// =====================================================================
// SWMiniHive and SWMiniBee

SWMiniHive {

	var <network;

	//	var <detectedNodes;
	var <xbee;

	var <>swarm;

	var <>redundancy = 5;
	var <>serialDT = 0.020;
	var <outTask;

	var <>verbose = 0;

	//	var <>configs;

	var <hiveConfig;

	var <outMessages;
	var <outMsgID = 0;


	var <>verbose = 0;

	var <>gui;

	var <confMsgID = 0;
	var <idMsgID = 0;

	/*
	*new { | network, portName, baudrate |
		^super.new( portName, baudrate ).myInit( network )
	}
	*/

	*new { | network |
		^super.new.network_( network ).init;
	}

	makeGui{
		^gui = SWMiniHiveGui.new( this );
	}

	xbee_{ |xb,autostart=false|
		xbee = xb;
		outMessages.add( [ 0, [ $A, 0, 0, 0 ] ] );
		if ( autostart ){
			this.start;
			this.startSend;
		}
	}

	stopXBee{
		outMessages.add( [ 0, [ $Q, 0, 0, 0 ] ] );
		fork{
			0.5.wait;
			this.stopSend;
			this.stop;
			xbee.close;
		};
	}

	init{
		hiveConfig = SWMiniHiveConfig.new;
		hiveConfig.hive = this;

		//		detectedNodes = Set.new;
		swarm = IdentityDictionary.new;
		//	configs = IdentityDictionary.new;

		outMessages = List.new;
	}

	network_{ |nw|
		network = nw;
		network.hive = this;		
	}

	/*
		addConfig{ |id, noInputs, smpPmsg = 1, msgInt = 50, scale|
		scale = scale ? (1/255);
		configs.put( id, ( noInputs: noInputs, samplesPerMsg: smpPmsg, msgInterval: msgInt, scale: scale ) );
		}
	*/

	addBee{ |id,bee|
		swarm.put( id, bee );
		bee.network = network;
	}

	removeBee{ |id|
		var bee = swarm.at( id );
		if ( bee.notNil ){
			network.removeNode( bee.dataNodeIn );
			swarm.removeAt( id );
		};
	}

	// called from HiveConfig when configuration for minibee changes
	changeBee{ |id|
		this.removeBee( id );
	}

	/*
	resetDetected{
		detectedNodes = Set.new;		
	}

	addDetected{
		detectedNodes.do{ |it|
			network.addExpected( it, ("minibee"++it).asSymbol );
			if ( swarm.at( it ).isNil, {
				this.createBee( it );
			} );
		};
	}
	*/

	createBee{ |id,cid,customConfig|
		var newbee;
		newbee = SWMiniBee.new( id, ("minibee"++id).asSymbol );
		newbee.setConfig( hiveConfig.getConfig( cid ), customConfig );
		this.addBee( id, newbee );
		network.addExpected( id, newbee.label );
		network.newBee( newbee );
	}

	start{
		xbee.action = { |type,msg|
			if ( verbose > 0 ){
				[ type, msg ].postln;
			};
			//	type.postcs;
			switch( type.asSymbol,
				'd',{
					this.parseData( msg );
				},
				's',{ // N byte serial number
					this.parseSerialNumber( msg );
				},
				'w',{ // minibee is waiting for config
					this.waitConfig( msg );
				},
				'c',{ // minibee has been configured 
					this.confirmConfig( msg );
				},
				'i',{ // information message
					msg.postcs;
					msg.copyToEnd(1).collect{ |it| { it.asAscii }.try({ it }) }.postln;
				}
			);
		}
	}

	parseData{ |msg|
		// maybe too intensive to lookup each time?
		var serial = hiveConfig.getSerial( msg[1] );
		if ( serial.notNil ){
			hiveConfig.setStatus( serial, 3 );
			//		detectedNodes.add( msg[1] );
		};
		try{ 
			swarm.at( msg[1] ).parseData( msg[2], msg ); // .copyToEnd( 3 )
		};
	}

	parseSerialNumber{ |msg|
		var size;
		var serial,beerev,beelibv,beecaps;
		if ( verbose > 0 ){
			msg.postcs;
			msg.copyToEnd(1).collect{ |it| { it.asAscii }.try({ it }) }.postln;
		};
		//	size = msg.size;
		beelibv = msg.wrapAt(-3);
		beerev = msg.wrapAt(-2).asAscii;
		beecaps = msg.wrapAt(-1);
		//	beerev = 'A';
		//	beelibv = 1;
		serial = "".catList( 
			msg.copyRange(1,msg.size-4).collect{ |it| // size-3 for new version
				{ it.asAscii }.try({ it }) 
			} ).asSymbol;
		this.sendID( serial );
		hiveConfig.setStatus( serial, 0 );
		hiveConfig.setVersion( serial, beerev, beelibv, beecaps );

		if ( gui.notNil ){ gui.updateGui; };
	}

	waitConfig{ |msg|
		// node 
		var serial,cid;
		if ( verbose > 0 ){
			msg.postcs;
			msg.copyToEnd(1).collect{ |it| { it.asAscii }.try({ it }) }.postln;
		};
		serial = hiveConfig.getSerial( msg[1] );
		cid = hiveConfig.getConfigIDSerial( serial );
		if ( msg[2] == cid ){
			hiveConfig.setStatus( serial, 1 );
			this.sendConfig( msg[2] );
		}{
			"waiting for wrong config: ".post;
			[ serial, cid, msg[1], msg[2] ].postln;
			hiveConfig.setConfigured( serial, 1 );
			hiveConfig.setStatus( serial, 0 );
			this.sendID( serial );
		};
		if ( gui.notNil ){ gui.updateGui; };
	}

	confirmConfig{ |msg|
		// node 
		var serial,cid;
		if ( verbose > 0 ){
			msg.postcs;
			msg.copyToEnd(1).collect{ |it| { it.asAscii }.try({ it }) }.postln;
		};
		serial = hiveConfig.getSerial( msg[1] );
		cid = hiveConfig.getConfigIDSerial( serial );
		if ( msg[2] == cid ){
			// correct config
			hiveConfig.setStatus( serial, 2 );
			hiveConfig.setConfigured( serial, 0 );
			this.createBee( msg[1], msg[2], msg.copyToEnd(8) );
			// do some more configuration checking based on message info
			// node id, config id, samples per message, sample interval,
			// input size, output size
		}{
			"configured with wrong config: ".post;
			[ serial, cid, msg[1], msg[2] ].postln;
			hiveConfig.setConfigured( serial, 1 );
			hiveConfig.setStatus( serial, 0 );
			this.sendID( serial );
		};
		if ( gui.notNil ){ gui.updateGui; };
	}

	sendID{ |serial|
		var id = hiveConfig.getNodeID( serial );
		//	hiveConfig.isConfigured( serial ).postcs;
		switch( hiveConfig.isConfigured( serial ),
			0, {
				idMsgID = idMsgID + 1;
				idMsgID = idMsgID%256;
				outMessages.add( [0, [ $I, idMsgID ] ++ 
				serial.asString.ascii
				++ id ] ) },
			1, {
				idMsgID = idMsgID + 1;
				idMsgID = idMsgID%256;
				outMessages.add( [0, [ $I, idMsgID ] ++ 
				serial.asString.ascii
				++ id ++ hiveConfig.getConfigIDSerial( serial ) ] ) },
			2, { "Please define configuration".postln; }
			);
	}

	sendConfig{ |cid|
		confMsgID = confMsgID + 1;
		confMsgID = confMsgID%256;
		outMessages.add( [0, [$C, confMsgID] ++ hiveConfig.getConfigMsg( cid ) ] );
	}

	stop{
		xbee.action = {};
	}

	startSend{
		if ( outTask.isNil ){ this.createOutputTask };
		outTask.play;
	}

	stopSend{
		outTask.stop;
	}

	createOutputTask{ 
		outTask = Tdef( \miniHiveOut, {
			var msg;
			loop{
				outMessages.copy.do{ |it,i|
					it.postln;
					xbee.sendMsgNoID( it[1][0], it[1].copyToEnd( 1 ) );
					outMessages.remove( it );
					if ( it[0] < this.redundancy ){
						outMessages.add( [it[0] + 1, it[1]] );
					};
					serialDT.wait;
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsOutput < this.redundancy ){
						msg = it.getOutputMsg;
						xbee.sendMsgNoID( $O, msg  );
						if ( verbose > 0 ){ [ $O, msg ].postln; };
						serialDT.wait;
					};
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsPWM < this.redundancy ){
						msg = it.getPWMMsg;
						xbee.sendMsgNoID( $P, msg  );
						if ( verbose > 0 ){ [ $P, msg ].postln; };
						serialDT.wait;
					};
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsDig < this.redundancy ){
						msg = it.getDigMsg;
						xbee.sendMsgNoID( $D, msg );
						if ( verbose > 0 ){ [ $D, msg ].postln; };
						serialDT.wait;
					};
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsRun < this.redundancy ){
						msg = it.getRunMsg;
						xbee.sendMsgNoID( $R, msg  );
						if ( verbose > 0 ){ [ $R, msg ].postln; };
						serialDT.wait;
					};
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsLoop < this.redundancy ){
						msg = it.getLoopMsg;
						xbee.sendMsgNoID( $L, msg  );
						if ( verbose > 0 ){ [ $L, msg ].postln; };
						serialDT.wait;
					};
				};
				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsCustom < this.redundancy ){
						msg = it.getCustomMsg;
						xbee.sendMsgNoID( $E, msg  );
						if ( verbose > 0 ){ [ $E, msg ].postln; };
						serialDT.wait;
					};
				};
				/*				swarm.do{ |it|
					if ( verbose > 1 ){ it.dump };
					if ( it.repeatsMotor < this.redundancy ){
						msg = it.getMotorMsg;
						xbee.sendMsgNoID( $M, msg  );
						if ( verbose > 0 ){ [ $M, msg ].postln; };
						serialDT.wait;
					};
					};*/
				// wait always between iterations to prevent endless loop
				serialDT.wait;
			}
		});
	}

	setRun{ |id,onoff|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setRun( onoff );
		}
	}

	setLoop{ |id,onoff|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setLoop( onoff );
		}
	}

	setOutput{ |id,data|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setOutput( data );
		}
	}

	setCustom{ |id,data|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setCustom( data );
		}
	}

	setPWM{ |id,data|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setPWM( data );
		}
	}

	setDigital{ |id,data|
		var bee = swarm.at(id);
		if ( bee.notNil ){
			bee.setDigital( data );
		}
	}

	mapBee{ |id,node,type,char|
		var bee = swarm.at(id);
		//	("mapping bee" + id + bee + node + type).postln;
		if ( bee.notNil ){
			bee.setMap( node, type, char );
		}
	}

	unmapBee{ |id,node,type,char|
		var bee = swarm.at(id);
		//	("mapping bee" + id + bee + node + type).postln;
		if ( bee.notNil ){
			bee.removeMap( node, type, char );
		}
	}
}

SWMiniBee{

	var <>id; // node ID of the MiniBee itself
	var <>label;
	var <>dataNodeIn; // data node in network that receives data from this minibee
	//	var <>dataNodeOutPWM; // data node in network from which we are sending data to this minibee, PWM
	//	var <>dataNodeOutDig; // data node in network from which we are sending data to this minibee, digital

	var dataNodeOutput, dataNodePWM, dataNodeDig;
	var dataNodeCustom;

	var <network;
	
	var <config;


	/// ------- input --------
	//	var <>dataIn;

	var <>scale = 1;
	var <>noInputs = 1;
	var <>noOutputs = 0;

	var <samplesPerMsg = 1;
	var <msgInterval = 0.050;
	var <dt; // dt for task. Calculated when either of the above are set
	var <lastTime;

	var <timeOutTask;
	var <>timeOutTime = 0.1;

	var <dataInBuffer;
	var <dataTask;
	//	var <dataStream;
	var <dataFunc;

	var <parseFunc;

	var <msgRecvID = 0;

	/// ------- output --------

	var <msgSendID = 0;

	//	var <msgIDpwm = 0;
	//	var <msgIDdig = 0;
	var <repeatsPWM = 10;
	var <repeatsDig = 10;
	var <pwmData;
	var <digData;
	var <repeatsOutput = 10;
	var <outputData;

	var <repeatsCustom = 10;
	var <customData;

	var <repeatsLoop = 10;
	var <loopback;

	var <repeatsRun = 10;
	var <running;

	var <>verbose = 0;


	*new { | id, label |
		^super.new.id_(id).label_( label ).init;
	}


	network_{ |nw|
		network = nw;
		dataFunc = { |data| 
			network.setData( this.dataNodeIn, data );
			if ( verbose > 1 ){ [id, this.dataNodeIn, data].postln; };
		};
	}

	init{ 
		config = SWMiniBeeConfig.new;
		// dataNodeIn can be configured to something else, but by default it is equal to the MiniBee node ID
		dataNodeIn = id;
		// dummy func until network is set:
		dataFunc = { |data| data.postln; };

		dataInBuffer = RingList.fill( 100, 0 );
		//		dataStream = Pseq( dataInBuffer, inf ).asStream;
		dt = msgInterval / samplesPerMsg;
		dataTask = Tdef( (label++"data").asSymbol, {
			loop{
				dataFunc.value( parseFunc.value( dataInBuffer.read ) );
				this.dt.max(0.002).wait;
			}
		});
		// timeOutTask stops the task when we don't get a new message for longer than the msgInterval.
		timeOutTask = Tdef( (label++"timeOut").asSymbol, {
			loop{
				if ( (Process.elapsedTime - lastTime) > timeOutTime ){
					dataTask.stop;
				};
				msgInterval.wait; // maybe add some extra leeway?
			}
		});
		//		lastTime = Process.elapsedTime;
	}

	setConfig{ |conf,custom|
		config.from( conf );
		config.addCustom( custom );
		this.msgInterval = config.msgInterval;
		this.samplesPerMsg = config.samplesPerMsg;
		
		this.noInputs = config.noInputs;
		
		this.noOutputs = config.noOutputs;
		parseFunc = config.getDataFunc;
	}

	msgInterval_{ |mi|
		msgInterval = mi;
		dt = msgInterval / samplesPerMsg;
	}

	samplesPerMsg_{ |sm|
		samplesPerMsg = sm;
		dt = config.msgInterval / samplesPerMsg;
	}
	
	parseData{ |msgID, data| // now still includes 0,1,2 of msg
		var diffTime;
		if ( msgID != msgRecvID ){ // parse only if we didn't haven't parsed this message yet
			msgRecvID = msgID;
			data = data.at( (3..(data.size-1) ) );
			if ( samplesPerMsg == 1 ){
				// if only one sample per message, directly put it on the network
				dataFunc.value( parseFunc.value( data ) );
			}{// if multiple samples per message, we will play them onto the network accordingly
				// automatically adjust msgInterval according to last measured interval.
				diffTime = lastTime;
				lastTime = Process.elapsedTime;
				if ( diffTime.notNil ){
					diffTime = lastTime - diffTime;
					this.msgInterval = diffTime.max( 0.020 ); // at least 0.1ms
				};
				// clump data according to how many inputs we have, and flop it so that each element is a collection of current data values
				data = data.clump( noInputs );
				/*
					if ( noInputs == 1 ){
					data = data.unbubble;
					};
				*/
				if ( verbose > 0 ){ data.postln; };
				// add it to the ringbuffer:
				dataInBuffer.addData( data );
				// resume datatask if it is not still playing
				if ( dataTask.isPlaying.not ){
					dataTask.resume;
					if ( dataTask.isPlaying.not ){
						dataTask.play;
						//		timeOutTask.reset.play;
					};
				};
				// reset the time out task:
				//	timeOutTask.reset.play;
			}
		}
	}

	setRun{ |data|
		if ( verbose > 0 ){ ("set run"+id+data).postln; };
 		running = data;
		repeatsRun = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getRunMsg{
		repeatsRun = repeatsRun + 1;
		^([ id, msgSendID, running ]);
	}

	setLoop{ |data|
		if ( verbose > 0 ){ ("set loop"+id+data).postln; };
 		loopback = data;
		repeatsLoop = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getLoopMsg{
		repeatsLoop = repeatsLoop + 1;
		^([ id, msgSendID, loopback ]);
	}

	setOutput{ |data|
		if ( verbose > 0 ){ ("set output"+id+data).postln; };
 		outputData = data;
		repeatsOutput = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getOutputMsg{
		repeatsOutput = repeatsOutput + 1;
		^([ id, msgSendID ] ++ outputData);
	}


	setCustom{ |data|
		if ( verbose > 0 ){ ("set custom"+id+data).postln; };
 		customData = data;
		repeatsCustom = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getCustomMsg{
		repeatsCustom = repeatsCustom + 1;
		^([ id, msgSendID ] ++ customData);
	}


	setPWM{ |data|
		if ( verbose > 0 ){ ("set pwm"+id+data).postln; };
 		pwmData = data;
		repeatsPWM = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getPWMMsg{
		repeatsPWM = repeatsPWM + 1;
		^([ id, msgSendID ] ++ pwmData);
	}

	setDigital{ |data|
		if ( verbose > 0 ){ ("set dig"+id+data).postln; };
 		digData = data;
		repeatsDig = 0;
		msgSendID = msgSendID + 1;
		msgSendID = msgSendID.mod( 256 );
	}

	getDigMsg{
		repeatsDig = repeatsDig + 1;
		^([ id, msgSendID ] ++ digData);
	}

	setMap{ |node, type|
		//	("BEE: mapping bee"+id+node+type).postln;
		switch( type,
			\custom, {
				if ( dataNodeCustom.notNil ){
					dataNodeCustom.action = {};
				};
				dataNodeCustom = node;
				dataNodeCustom.action = { |data| this.setCustom( data ) };
			},
			\pwm, {
				if ( dataNodeOutput.notNil ){
					dataNodeOutput.action = {};
					dataNodeOutput = nil;
				};
				if ( dataNodePWM.notNil ){
					dataNodePWM.action = {};
				};
				dataNodePWM = node;
				dataNodePWM.action = { |data| this.setPWM( data ) };
			},
			\digital, {
				if ( dataNodeOutput.notNil ){
					dataNodeOutput.action = {};
					dataNodeOutput = nil;
				};
				if ( dataNodeDig.notNil ){
					dataNodeDig.action = {};
				};
				dataNodeDig = node;
				dataNodeDig.action = { |data| this.setDigital( data ) };

			},
			\output, {
				if ( dataNodePWM.notNil ){
					dataNodePWM.action = {};
					dataNodePWM = nil;
				};
				if ( dataNodeDig.notNil ){
					dataNodeDig.action = {};
					dataNodeDig = nil;
				};
				if ( dataNodeOutput.notNil ){
					dataNodeOutput.action = {};
				};
				dataNodeOutput = node;
				dataNodeOutput.action = { |data| this.setOutput( data ) };

			}
		);
	}

	removeMap{ |node, type|
		("BEE: unmapping bee"+id+node+type).postln;
		switch( type,
			\custom, {
				if ( dataNodeCustom.notNil ){
					dataNodeCustom.action = {};
					dataNodeCustom = nil;
				};
			},
			\output, {
				if ( dataNodeOutput.notNil ){
					dataNodeOutput.action = {};
					dataNodeOutput = nil;
				};
			}
		);
	}


}

// EOF