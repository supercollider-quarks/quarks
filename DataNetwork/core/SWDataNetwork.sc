// abstracting classes from the XBeeNetwork to a general DataNetwork
// part of SenseWorld (prefix SW)
// developed for the SenseStage project

SWDataNetwork{
	var <spec;
	var <nodes;
	var <>verbose; // 1 is informative, 2 is warning, 3 is all;

	var <>osc;

	var <>expectedNodes;
	//	var <>expectedSize;
	var <watcher;
	var <worrytime = 60; // time after which to worry whether node is still active
	var <>gui;
	var <>baseGui;

	var <debug = false;

	var <>recTask;
	var <logfile;
	var <reclines = 0;
	var <>recnodes;

	var <recTime = false;
	var <timelogfile;

	var <>hive;

	var <>hooks;
	//	var <>newNodeHooks;

	*new{ 
		^super.new.init;
	}

	init{
		verbose = Verbosity.new( 1, \swdatanetwork );
		expectedNodes = Set.new;
		//		expectedSize = IdentityDictionary.new;
		nodes = IdentityDictionary.new;
		hooks = SWHookSet.new;
		hooks.verbose = verbose;
		spec = SWDataNetworkSpec.new( this );
		watcher = SkipJack.new(
			{
				var now = Process.elapsedTime;
				nodes.do{ |it,i| 
					if ( it.elapsed > worrytime,
						{
							verbose.value( 2, "restarting network" );
							//	if ( verbose > 0, { "restarting network".postln; });
							it.restartAction.value;
						});
				};
			}, worrytime/10, name: "DataNetwork-watcher", autostart: false );
		recTask = Task.new( {} );
		this.watch( false );
	}

	performHook{ |type,id|
		hooks.perform( type, id, [ nodes[id] ] );
	}

	addHook{ |id,action, type=\newnode, permanent=false|
		hooks.add( type, id, action, permanent );
		if ( nodes.at(id).notNil and: type == \newnode ){ // perform the action rightaway if node is already there
			hooks.perform( type, id, [nodes[id]] );
			//		action.value( nodes.at(id) );
		};
	}

	removeHook{ |id,type=\newnode|
		hooks.removeAt( id, type );
	}

	/// --------- NODE control ---------

	checkDataType{ |data|
		var type;
		//		data.postcs;
		if ( data.first.isNil ){
			type = -1;
		};
		if ( data.first.isKindOf( SimpleNumber ) ){
			type = 0;
		};
		if ( data.first.isKindOf( Symbol ) or: data.first.isKindOf( String ) ){
			type = 1;
		};
		if ( type.isNil ){ type = 0; }; // proper error catching later
		^type;
	}

	setData{ |id,data|
		var ret = true;
		var ret2;
		var returnCode; // success;
		var lasttime;
		var node,type;
		verbose.value( 3, [id,data] );
		//if ( verbose > 1, { [id,data].postln; } );
		
		if ( id.isKindOf( Integer ) ){
			node = nodes[id];
			if ( node.isNil, {
				type = this.checkDataType( data );
				ret = this.registerNode( id, data.size, type );
				node = nodes[id];
				verbose.value( 2, ("registering node"+id+ret) );
				//	if ( verbose > 0 ) { ("registering node"+id+ret).postln; };
			});
		}{
			node = this.at( id.asSymbol );
			if ( node.isNil ){
				("unknown node"+id).postln;
				returnCode = 3;
			}
		};
		
		if ( ret ) {
			if ( recTime ){
				lasttime = node.lasttime;
			};
			ret2 = node.data_( data );
			if ( ret2 ){
				returnCode = 0;
				if ( recTime ){
					this.writeTimeUpdate( id, node.lasttime - lasttime, node.slots.collect{ |it| it.logvalue } );
				};
				if ( osc.notNil, {
					//	osc.sendData( id, data );
					osc.sendDataNode( node );
				});
			}{
				returnCode = 2; // wrong number of slots;
				verbose.value( 3, "wrong number of slots" );
				//	if ( verbose > 1 ) { "wrong number of slots".postln; };
			}
		}{
			returnCode = 1; // error registering node;
		};
		^returnCode;
	}

	/* This should not be necessary:
	setDataSlot{ |id,id2,value|
		var ret = true;
		if ( verbose > 1, { [id,id2,value].postln; } );
		if ( nodes[id].isNil, {
			ret = false;
			if ( verbose > 0 ) { ("node"+id+"does not exist").postln; };
		});
		if ( ret ) { 
			nodes[id].dataSlot_( id2, value );
			if ( osc.notNil, {
				osc.sendData( id, nodes[id].data );
			});
		};
	}
	*/

	newBee{ |minibee|
		if ( osc.notNil ){
			osc.newBee( minibee );
		}
	}

	getBeeInfo{ |addr|
		if ( osc.notNil ){
			if ( hive.notNil ){
				if ( hive.swarm.size == 0 ){
					osc.sendBeeNoInfo( addr );
				}{
					hive.swarm.do{ |it|
						osc.sendBeeInfo( addr, it.id, it.noInputs, it.noOutputs );
					}
				};
			}
		}
	}

	configureBee{ |miniBee,cid|
		var mb;
		if ( osc.notNil ){
			if ( miniBee.isKindOf( SWMiniBee ) ){
				mb = miniBee.id;
			}{
				mb = miniBee;
			};
			osc.configMiniBeeLocal( mb, cid );
		};
	}

	createConfig{ |...config|
		if ( osc.notNil ){
			osc.createConfigLocal( config[0], config.copyToEnd( 1 ) );
		}
	}

	deleteConfig{ |cid|
		if ( osc.notNil ){
			osc.deleteConfigLocal( cid );
		}
	}

	saveConfig{ |filename|
		if ( osc.notNil ){
			osc.saveConfigLocal( filename );
		}
	}

	loadConfig{ |filename|
		if ( osc.notNil ){
			osc.loadConfigLocal( filename );
		}
	}

	mapHive{ |node,type=\output|
		var id;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		switch( type,
			'custom', { this.mapHiveAllCustom(id)},
			'output', { this.mapHiveAllOutput(id)}
		);
	}

	unmapHive{ |node,type=\output|
		var id;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		switch( type,
			'custom', { this.unmapHiveAllCustom(id)},
			'output', { this.unmapHiveAllOutput(id)}
		);
	}

	mapHiveAllOutput{ |nodeID|
		/*
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		*/
		if ( osc.notNil ){
			osc.mapHiveAllOutputLocal( nodeID );
		}
	}

	unmapHiveAllOutput{ |nodeID|
		/*
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		*/
		if ( osc.notNil ){
			osc.unmapHiveAllOutputLocal( nodeID );
		}
	}

	mapHiveAllCustom{ |nodeID|
		/*
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		*/
		if ( osc.notNil ){
			osc.mapHiveAllCustomLocal( nodeID );
		}
	}

	unmapHiveAllCustom{ |nodeID|
		/*
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		*/
		if ( osc.notNil ){
			osc.unmapHiveAllCustomLocal( nodeID );
		}
	}

	mapBee{ |node,miniBee,type=\output|
		var id,mb;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		if ( miniBee.isKindOf( SWMiniBee ) ){
			mb = miniBee.id;
		}{
			mb = miniBee;
		};
		switch( type,
			'custom', { this.mapHiveCustom(id,mb)},
			'output', { this.mapHiveOutput(id,mb)}
		)
	}

	unmapBee{ |node,miniBee,type=\output|
		var id,mb;
		if ( node.isKindOf( SWDataNode ) ){
			id = node.id;
		}{
			id = node;
		};
		if ( miniBee.isKindOf( SWMiniBee ) ){
			mb = miniBee.id;
		}{
			mb = miniBee;
		};
		switch( type,
			'custom', { this.unmapHiveCustom(id,mb)},
			'output', { this.unmapHiveOutput(id,mb)}
		)
	}

	mapHiveOutput{ |nodeID, miniBee|
		//		("network: mapping hive output" + nodeID + miniBee ).postln;
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		if ( osc.notNil ){
			osc.mapHiveOutputLocal( nodeID, miniBee );
		}
	}

	unmapHiveOutput{ |nodeID, miniBee|
		//		("network: mapping hive output" + nodeID + miniBee ).postln;
		if ( hive.notNil ){
			hive.unmapBee( miniBee, this.nodes[ nodeID ], \output );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		if ( osc.notNil ){
			osc.unmapHiveOutputLocal( nodeID, miniBee );
		}
	}

	mapHiveCustom{ |nodeID, miniBee|
		if ( hive.notNil ){
			hive.mapBee( miniBee, this.nodes[ nodeID ], \custom );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		if ( osc.notNil ){
			osc.mapHiveCustomLocal( nodeID, miniBee );
		}
	}

	unmapHiveCustom{ |nodeID, miniBee|
		if ( hive.notNil ){
			hive.unmapBee( miniBee, this.nodes[ nodeID ], \custom );
			//	this.nodes[ nodeID ].action = { |data| hive.setOutput( miniBee, data) };
		};
		if ( osc.notNil ){
			osc.unmapHiveCustomLocal( nodeID, miniBee );
		}
	}

	// obsolete
	mapHivePWM{ |nodeID, miniBee|
		if ( hive.notNil ){
			hive.mapBee( this.nodes[ nodeID ], miniBee, \pwm );
			//		this.nodes[ nodeID ].action = { |data| hive.setPWM( miniBee, data) };
		};
	}

	// obsolete
	mapHiveDig{ |nodeID, miniBee|
		if ( hive.notNil ){
			hive.mapBee( this.nodes[ nodeID ], miniBee, \digital );
			//		this.nodes[ nodeID ].action = { |data| hive.setDigital( miniBee, data) };
		};
	}

	registerNode{ |id,sz,type|
		var ret,key,nnode;
		if ( type == -1){
			ret = false;
		}{
			ret = ( (sz > 0) and: ( this.isExpected( id ) ) and: (nodes.at(id).isNil ) );
		};
		if ( ret ) {
			if ( type == 0 ){
				nnode = SWDataNode.new( id,sz );
			};
			if ( type == 1 ){
				nnode = SWDataStringNode.new( id,sz );
			};
			if ( nnode.notNil ){
				nodes.put( id, nnode );
				// perform the action to be performed when the node is created
				hooks.perform( \newnode, id, [nnode] );
			//	nodes.postcs;
			//	nodes[id].postcs;
				if ( osc.notNil, {
					osc.newNode( nnode );
				});
				if ( gui.notNil, {
					gui.addNode( nnode );
				});
				key = spec.findNode( id );
				if ( key.notNil, {this.at( key ).key = key; });
				sz.do{ |it| 
					key = spec.findSlot( id, it );
					if ( key.notNil, {this.at( key ).key = key; });
				};
			}{
				verbose.value( 2, ("node with id"+id+"has unknown type"+type ) );
				//	if ( verbose > 0 , {("node with id"+id+"has unknown type"+type ).postln;});
				ret = 13; // error code for wrong type
			};
		}{
			verbose.value( 1, ("node with id"+id+"and size"+sz+"is not expected to be part of the network" ) );
			//	if ( verbose > 0 , {("node with id"+id+"and size"+sz+"is not expected to be part of the network" ).postln;});
		};
		^ret;
	}

	removeNode{ |id|
		nodes.removeAt( id );
		if ( osc.notNil, {
			osc.nodeRemoved( id );
		});
	}

	/// -------- expected nodes -----------

	addExpected{ |id,label=nil,size=nil,type=0|
		if ( this.isExpected( id ).not, {
			expectedNodes = expectedNodes.add( id );
		});
		if ( label.notNil and: (label.asSymbol != \0 ), {
			this.add( label, id );
		},{
			// maybe the label is already in the spec
			label = spec.findNode( id );
		});
		if ( osc.notNil, {
			osc.newExpected( id, label );
		});
		if ( size.notNil, {
			if ( type == 0 ){
				this.setData( id, Array.fill( size, 0 ) );
			}{
				this.setData( id, Array.fill( size, {'0'} ) );
			};
		});
	}

	isExpected{ |id|
		^expectedNodes.includes( id );
	}

	//---- spec (labeling and so on) ---------

	setSpec{ |name|
		spec.fromFile( name );
	}

	// direct access to spec:

	add{ |key, slot|
		var ns;
		spec.add( key, slot );
		if ( osc.notNil, {
			ns = this.at( key );
			//	ns.postln;
			if ( ns.isKindOf( SWDataNode ),{
				osc.newNode( ns );
			});
			if ( ns.isKindOf( SWDataSlot ),{
				osc.newSlot( ns );
			});
		});
	}

	// ---- named access to nodes, values and actions -------

	at{ |key|
		^spec.at( key );
	}

	value{ |key|
		^spec.value( key );
	}

	/* THIS should not be allowed! use setData instead.
	value_{ |key,value|
		spec.value_( key, value );
	}
	*/

	action_{ |key,action|
		spec.action_( key, action );
	}

	//---- -- node bus control -------------

	bus{ |key|
		^spec.bus( key );
	}

	createBus{ |key,server|
		spec.createBus( key, server );
	}

	freeBus{ |key|
		spec.freeBus( key );
	}

	createAllBuses{ |server|
		spec.createAllBuses( server );
	}

	freeAllBuses{
		spec.freeAllBuses;
	}

	createAllNodeBuses{ |server|
		nodes.do{ |it| it.createBus(server) };
	}

	// --------- data input control -------

	worrytime_{ |wt|
		worrytime = wt;
		watcher.dt = wt/10;
	}

	watch{ |onoff=true|
		if ( onoff, { watcher.start }, { watcher.stop; } );
	}


	// ------- data logging and recording --------

	// recording
	initRecord{ |fn,dt=0.025,stamp=false,break=36000|
		//		var recordnodes;
		fn = fn ? "SWDataNetworkLog";

		fn = fn++"_"++Date.localtime.stamp;

		// save a spec if none is given:
		if ( spec.name.isNil ){
			spec.save( fn );
		}{
			spec.save;
		};

		logfile = MultiFileWriter.new( fn ++ ".txt" );
		// FIXME: zipping and tarring seems to give some problems:
		logfile.zipSingle = false;
		logfile.tarBundle = false;
		logfile.stringMethod = \asCompileString;
		//	logfile =  File(fn++"_"++Date.localtime.stamp++".txt", "w");
				//		recnodes = this.writeHeader;

		// copies the spec to the directory into which we are writing our log
		spec.copyFile( logfile.pathDir );

		// save a tab readable version of the spec:
		spec.saveAsTabs( logfile.pathDir +/+ logfile.fileName ++ "_header.txt");

		recTask = Task.new( {
			loop {
				logfile.open;
				logfile.curFile.timeStamp = stamp;
				this.writeHeader;
				//	recnodes.dump;
				break.do{ |it|
					this.writeLine( dt );
					dt.wait;
				};
				logfile.close;
			}
		} );
	}

	record{ |onoff|
		if ( onoff ) {
			recTask.reset.play;
		}{
			recTask.stop;
			if ( logfile.notNil ){ logfile.close; };
		};
	}

	closeRecord{
		this.record( false );
		//	logfile.close;
		recTask = Task.new( {} );
	}

	writeHeader{
		var recslots;
		// this tells the spec used for the recording:
		logfile.writeLine( [spec.name] );
		//		logfile.write( "\n" );
		//		logfile.write( "time\t" );

		recnodes = nodes.select{ |node| node.record }.collect{ |node|
			//			node.slots.do{ |it| logfile.write( it.id.asCompileString ); logfile.write( "\t" ); };
			node.id;
		}.asArray;

		// this creates a header with the ids of the node slots
		recslots = recnodes.collect{ |nodeid|
			nodes[nodeid].slots.collect{ |it| it.id };
		}.flatten;
		
		//	logfile.write( "\n" );

		logfile.writeLine( [ "time" ] ++ recslots );
		//	^recordnodes;
	}

	writeLine{ |dt|
		var data;
		/*
			logfile.write( dt.asString );
			logfile.write( "\t" );
			recordnodes.do{ |it|
			nodes[it].slots.collect{ |slot| slot.value }.do{ |dat|
			logfile.write( dat.asCompileString );
			logfile.write( "\t" );
			}; 
			};
			logfile.write( "\n" );
		*/
		data = recnodes.collect{ |it|
			nodes[it].slots.collect{ |slot| slot.logvalue }
		}.flatten; 

		logfile.writeLine( [dt] ++ data);
	}


	/// log update times

	// recording
	initTimeRecord{ |fn,stamp=false|
		fn = fn ? "SWDataNetworkUpdateLog";
		//	timelogfile =  TabFileWriter(fn++"_"++Date.localtime.stamp++".txt").timeStamp_(stamp);
		timelogfile = MultiFileWriter.new( fn ++ "_"++Date.localtime.stamp++".txt" );
		timelogfile.open;
		//		timelogfile =  File(fn++"_"++Date.localtime.stamp++".txt", "w");
		recTime = true;
	}

	writeTimeUpdate{ |id,time,data|
		timelogfile.writeLine( [id,time,data]);
		/*
		timelogfile.write( id.asString );
		timelogfile.write( "\t" );
		timelogfile.write( time.asString );
		timelogfile.write( "\n" );
		*/
	}

	closeTimeRecord{
		recTime = false;
		timelogfile.close;
	}


	// ------------ Debugging -------------

	debug_{ |onoff|
		debug = onoff;
		nodes.do{ |sl|
			sl.do{ |slt| slt.debug_( onoff ) } };
	}

	// add interfaces:

	makeGui{
		^SWDataNetworkBaseGui.new( this );
	}

	makeNodeGui{
		^SWDataNetworkGui.new( this );
	}

	makeLogGui{
		^SWDataNetworkLogGui.new( this );
	}

	createHost{
		this.addOSCInterface;
	}

	addOSCInterface{
		^SWDataNetworkOSC.new( this );
	}


	/// backward compatibility
	makeBasicGui{
		"DataNetwork: now use .makeGui for this, and .makeNodeGui for the detailed node GUI.".warn;
		^SWDataNetworkBaseGui.new( this );
	}

}


// a DataNode are a collection of slots which are physically connected to each other, e.g. data gathered from the same device.
SWDataNode{

	var <id;
	var <>key;
	var <type = 0;

	var <slots;
	var <data;
	var <>scale = 1;
	var <lasttime;

	var <>action;
	var <>restartAction;

	var >bus;
	var <>databus;

	var <debug = false;
	var <>record = false;
	//	var <>trigger;

	// monitoring support
	var <datamonitor;
	var <>monitorMode = \plotter; // other option is \gnuplot

	*new{ |id,maxs=4|
		^super.new.init(id,maxs);
	}
	
	init{ |ident,maxs|
		id = ident;
		lasttime = 0;
		slots = Array.fill( maxs, 0 );
		data = Array.fill( maxs, 0.0 );
		// the restart action should contain what should be done if the node does not provide data anymore
		restartAction = {};
		action = {};
		lasttime = Process.elapsedTime;
		//		trigger = {};
		this.initSlots;
	}

	initSlots{
		slots.do{ |it,i| slots.put( i, SWDataSlot.new([id,i]) ); };
	}

	// -------- slots and data -------

	size{
		^slots.size;
	}

	data_{ |indata|
		if ( indata.size == slots.size , {
			data = indata.asFloat * scale;
			data.do{ |it,i| slots[i].value = it };
			action.value( data, this );
			this.setLastTime;
			//	trigger.value;
			^true;
		});
		^false;
		//		indata.copyRange(0,data.size-1).do{ |it,i| data[i].value = it };
	}

	/*
	dataSlot_{ |id,indata|
		if( id < slots.size ){
			data[id] = indata * scale;
			slots[id].value = data[id];
			lasttime = Process.elapsedTime;
			action.value( data );
		};
	}
	*/


	value{
		^data;
	}

	setLastTime{
		lasttime = Process.elapsedTime;
	}

	elapsed{
		^(Process.elapsedTime - lasttime );
	}

	// --- Bus support ---

	createBus{ |s|
		if ( this.bus.isNil, {
			s = s ? Server.default;
			databus = DataBus.new( { slots.collect{ |it| it.value } }, slots.size, s )
		});
	}

	bus{
		if ( bus.notNil, { ^bus } );
		if ( databus.isNil, { ^nil } );
		^databus.bus;
	}

	freeBus{
		if ( bus.notNil, { bus.free; bus = nil; },
			{
			if ( databus.notNil, { databus.free; databus = nil; });
			});
	}

// JITLib support
	kr{
		var b;
		this.createBus;
		b = this.bus;
		^In.kr( b, b.numChannels );
	}

	// ---------- debugging and monitoring -------

	debug_{ |onoff|
		debug = onoff;
		slots.do{ |sl|
			sl.do{ |slt| slt.debug_( onoff ) } };
	}

	monitor{ |onoff=true|
		if ( onoff, {
			if ( datamonitor.isNil, { 
				if ( monitorMode == \plotter ){
					datamonitor = SWPlotterMonitor.new( { this.value }, 200, this.size, 0.05, 20 );
				}{
					if ( bus.isNil, { this.createBus } );
					datamonitor = BusMonitor.new( this.bus );
				};
			});
			datamonitor.start;
		}, { datamonitor.stop; });
	}

	isMonitored{
		if ( datamonitor.notNil ){
			if ( datamonitor.isPlaying ){
				^true
			}
		};
		^false;
	}

	monitorClose{
		datamonitor.cleanUp;
		datamonitor = nil;
	}

	printOn { arg stream;
		stream << this.class.name << "(" << id << "," << key << "," << this.size<< "," << type << ")";
	}


}

SWDataSlot{
	var <>id;
	var <>key;
	var <type = 0;

	var <value;
	var <>action;

	var <bus;
	var debugAction;
	var <debug = false;

	var <>scale=1;
	var <map;
	var <range;

	var <>actionSensitivity = 1e-5;

	// monitoring support
	var <datamonitor;
	var <>monitorMode = \plotter; // other option: gnuplot

	*new{ |id|
		^super.new.init(id);
	}

	init{ |ident|
		id = ident;
		action = {};
		debugAction = {};
		value = 0;
	}

	// ------ data and action -----
	value_{ |val|
		var oldval = value;
		value = val * scale;
		// map to control spec from input range after scaling
		if ( map.notNil, { value = map.map( range.unmap( value ) ) } );
		if ( oldval.equalWithPrecision( value, actionSensitivity ).not ){
			action.value( value );
		};
		if ( debug ){
			debugAction.value( value );
		};
		if ( bus.notNil, { bus.set( value ) } );
	}

	logvalue{
		^value;
	}


	/// -------- scaling, mapping and calibrating --------

	map_{ |mp|
		if( range.isNil){
			// input range after scaling:
			range = [0,1].asSpec;
		};
		map = mp;
	}

	range_{ |mp|
		if( map.isNil){
			// input range after scaling:
			map = [0,1].asSpec;
		};
		range = mp;
	}

	// currently only does minimum:
	calibrate{ |steps=100| // about two seconds currently
		var calib,values;
		values = Array.new( steps );
		range = [0,1].asSpec;
		calib = Routine{ 
			var mean;
			steps.do{ |it,i| values.add( this.value ); it.yield; };
			mean = values.sum / values.size;
			range.minval = mean;
			this.debug_( false );
			"calibration done".postln;
		};
		debugAction = { calib.next };
	}

	// --- bus support ----

	createBus{ |s|
		s = s ? Server.default;
		if ( bus.isNil, {
			bus = Bus.control( s, 1 );
		},{
			if ( bus.index.isNil, {
				bus = Bus.control( s, 1 );
			});
		});
	}

	freeBus{
		bus.free;
		bus = nil;
	}

// JITLib support
	kr{
		this.createBus;
		^In.kr( bus );
	}

	/// ------- debugging and monitoring ------

	debug_{ |onoff|
		debug = onoff;
		if ( onoff, {
			debugAction = { |val| [ id, value, key ].postln; };
		},{
			debugAction = {};
		});
	}


	monitor{ |onoff=true|
		if ( onoff, {
			if ( datamonitor.isNil, {
				if ( monitorMode == \plotter ){
					datamonitor = SWPlotterMonitor.new( { this.value }, 200, 1, 0.05, 20 );
				}{
					this.createBus;
					datamonitor = BusMonitor.new( this.bus );
				};
			});
			datamonitor.start;
		}, { datamonitor.stop; });
	}

	isMonitored{
		if ( datamonitor.notNil ){
			if ( datamonitor.isPlaying ){
				^true
			}
		};
		^false;
	}

	monitorClose{
		datamonitor.cleanUp;
		datamonitor = nil;
	}

	printOn { arg stream;
		stream << this.class.name << "(" << id << "," << key << ")";
	}

}



SWDataNetworkSpec{
	classvar <>all,<folder;

	var <>name;
	var <map, network;

	*initClass { 
		// not yet used
		this.makeSaveFolder;
		this.loadSavedInfo;
		all	= all ? Set.new;
	}

	*loadSavedInfo{
		all = (folder+/+"allspecs.info").load;
	}
	
	*makeSaveFolder { 
		var testfile, testname = "zzz_datanetwork_test_delete_me.txt"; 
		folder = (Platform.userAppSupportDir +/+ "DataNetworkSpecs").standardizePath;
		testfile = File(folder +/+ testname, "w");

		if (testfile.isOpen.not) 
			{ unixCmd("mkdir" + folder.escapeChar($ )) }
			{ testfile.close;  unixCmd("rm" + folder.escapeChar($ ) +/+ testname) }
	}

	*saveAll{
		var file, res = false;
		var filename;
		filename = folder +/+ "allspecs.info";
		file = File(filename, "w"); 
		if (file.isOpen) { 
			res = file.write(all.asCompileString);
			file.close;
		};
		^res;
	}


	*new { |netw|
		^super.new.init(netw);
	}

	init{ |netw|
		network = netw;
		map = IdentityDictionary.new;
	}

	saveAsTabs{ |path|
		var file,mynodes,myslots;
		file = TabFileWriter.new( path, "w" );
		
		mynodes = network.nodes.collect{ |node|
			node.id;
		}.asArray;

		// this creates a header with the ids of the node slots
		myslots = mynodes.collect{ |nodeid|
			network.nodes[nodeid].slots.collect{ |it| [it.id,it.key] };
		}.flatten.flop;
		file.writeLine( myslots[0] );
		file.writeLine( myslots[1] );
		file.close;
	}

	save{ |name|
		var file, res = false;
		var filename;
		all.add( name.asSymbol );
		this.name = name ? this.name;
		filename = folder +/+ name ++ ".spec";
		file = File(filename, "w"); 
		if (file.isOpen) { 
			res = file.write(map.asCompileString);
			file.close;
		};
		this.class.saveAll;
		^res;
	}

	fromFile { |name| 
		var slot;
		this.name = name;
		map = (folder +/+ name++".spec").load;
		map.keysValuesDo{ |key,it|
			slot = this.at( key );
			if ( slot.notNil, { slot.key = key; } );
		}
	}

	copyFile{ |target|
		("cp"+(folder +/+ name++".spec")+target).unixCmd;
	}

	fromFileName { |fn| 
		var slot;
		this.name = name;
		map = (fn++".spec").load;
		map.keysValuesDo{ |key,it|
			slot = this.at( key );
			if ( slot.notNil, { slot.key = key; } );
		}
	} 

	findNode{ |id|
		^map.findKeyForValue( id );
	}

	findSlot{ |id1,id2|
		var keySlot = nil;
		map.keysValuesDo{ |key,val| if ( val == [id1,id2] ) { keySlot = key } };
		^keySlot;
		//		^map.findKeyForValue( [id1,id2] );
	}



	// --- the methods below can all be accessed from the network directly ----

	add{ |key, slot|
		key = key.asSymbol;
		if ( key != 'nil' ){
			map.put( key, slot );
		};
		if ( this.at( key ).notNil, {
			this.at( key ).key = key;
		});
	}

	// ------- named access to nodes, slots

	// returns the slot or node
	at{ |key|
		var id1,id2;
		var item;
		key = key.asSymbol;
		item = map.at( key );
		if ( item.isKindOf( Array ), {
			id1 = map.at(key)[0];
			id2 = map.at(key)[1];
			if ( network.nodes[id1].isNil, { ^nil } );
			^network.nodes[id1].slots[id2];
		},{
			^network.nodes[item]
		});
		// map.at(key)
	}

	value{ |key|
		key = key.asSymbol;
		^this.at(key).value;
	}

	/* // this should not be allowed!
	value_{ |key,value|
		var slot;
		slot = this.at(key);
		slot.value_(value);
		^slot;
	}
	*/

	action_{ |key,action|
		var slot;
		key = key.asSymbol;
		slot = this.at(key);
		slot.action_(action);
		^slot;		
	}

	//-------- node bus control ---------

	bus{ |key|
		key = key.asSymbol;
		^this.at(key).bus;
	}

	createBus{ |key,server|
		key = key.asSymbol;
		this.at( key ).createBus( server );
	}

	freeBus{ |key|
		key = key.asSymbol;
		this.at( key ).freeBus;
	}


	createAllBuses{ |server|
		map.do{ |it|
			if ( it.isKindOf( Array ), {
				network.nodes.at( it[0] ).slots.at( it[1] ).createBus( server );
			},{
				network.nodes.at( it ).createBus( server );
			});
		};
	}

	freeAllBuses{
		map.do{ |it|
			network.nodes.at( it[0] ).slots.at( it[1] ).freeBus;
		};
	}


	/*	setAllActions{ |action|
		map.do{ |it|
			device.slots.at( it[0] ).at( it[1] ).action_( action );
		};
		}*/


}
