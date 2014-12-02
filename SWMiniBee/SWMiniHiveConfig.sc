SWMiniHiveConfig{

	classvar <>folder;

	var <>hive;

	var <>hiveMap;
	var <configLib;
	var <configLabels; // to derive configIDs from
	//	var <hiveConfigMap;
	//	var <hiveIdMap;
	//	var <hiveConfed;
	//	var <hiveStatus;

	var cidAllocator;

	var idAllocator;

	var <>gui;

	*new{
		^super.new.init;
	}

	makeGui{
		gui = SWMiniHiveConfigGui.new( this );
		^gui;
	}

	init{
		configLib = IdentityDictionary.new;     // labels -> SWMiniBeeConfigs

		configLabels = IdentityDictionary.new; // empty list with config labels

		//		configLabels = List.new; // empty list with config labels

		hiveMap = IdentityDictionary.new; // serial IDs -> SWMiniBeeID's

		/*
		hiveConfigMap = IdentityDictionary.new; // serial IDs -> config label
		hiveIdMap = IdentityDictionary.new;     // serial IDs -> node IDs
		hiveConfed = IdentityDictionary.new;    // serial IDs -> configured?
		hiveStatus = IdentityDictionary.new; // serial IDs -> status
		*/

		cidAllocator = SWMBNumberAllocator.new(1,255);

		idAllocator = SWMBNumberAllocator.new(1,255);
	}

	// create a new bee
	newBee{ |serial|
		var mb;
		mb = SWMiniBeeID.new( serial );
		hiveMap.put( serial, mb );
		^mb
	}

	getBee{ |serial|
		^hiveMap.at( serial );
	}

	// get the node id for a serial number. Assigns a new number if necessary
	getNodeID{ |serial|
		var mb,id;
		mb = hiveMap.at( serial );
		if ( mb.isNil ){
			id = this.assignNodeID( serial );
		}{
			id = mb.nodeID;
			if ( id.isNil ){
				id = this.assignNodeID( serial, mb );
			};
		};
		^id;
	}

	assignNodeID{ |serial, mb|
		var id = idAllocator.alloc;
		if ( mb.isNil ){
			mb = this.newBee(serial);
		};
		mb.nodeID = id;
		^id;
	}

	// finds a serial number based on a certain property (must be instance method of SWMiniBeeID)
	findSerialForProperty { arg prop,val;
		hiveMap.keysValuesDo {|key, mb|
			if(mb.perform(prop) == val) {^key }
		};
		^nil
	}

	// get the serial number for a node id.
	getSerial{ |id|
		var serial;
		serial = this.findSerialForProperty( \nodeID, id );
		//	serial = hiveIdMap.findKeyForValue(id);
		^serial;
	}

	// assign a certain defined configuration to a serial number
	setConfig{ |configLabel,serial|
		hiveMap.at( serial.asSymbol ).configLabel_( configLabel ).configured_( 1 );
		hive.changeBee( this.getNodeID( serial ) );
		/*
		hiveConfigMap.put( serial.asSymbol, configLabel );
		hiveConfed.put( serial.asSymbol, 1 );
		*/
	}

	// set configuration status to a serial number
	setConfigured{ |serial,value=0|
		hiveMap.at( serial.asSymbol ).configured_( value );
		//	hiveConfed.put( serial.asSymbol, value );
	}

	// set status of a specific bee 
	setVersion{ |serial,rev,libv,caps|
		// TODO: if other revision or libversion than before, config has to be adapted!
		hiveMap.at( serial.asSymbol ).revision_( rev ).libversion_( libv ).libcaps_( caps );
		//		hiveStatus.put( serial.asSymbol, value );
	}

	// set status of a specific bee 
	setStatus{ |serial,value=0|
		hiveMap.at( serial.asSymbol ).status_( value );
		//		hiveStatus.put( serial.asSymbol, value );
	}

	getStatus{ |serial|
		// three states : 
		// - sent serial (0), 
		// - is waiting for config(1),
		// - is configured(2),
		// - is sending data(3),
		// - stopped sending data(4)
		// - inactive (not yet started) (5)
		^hiveMap.at( serial ).status;
	}

	// retrieve the configuration number for this serial number
	getConfigIDSerial{ |serial|
		var config,cid;
		config = hiveMap.at( serial ).configLabel;
		if ( config.isNil ){
			("no config known for this device!" + serial).postln;
		}{
			cid = this.getConfigIDLabel( config );
		}
		^cid;
	}

	/// get the id of a certain configuration from its label
	getConfigIDLabel{ |label|
		^(configLabels.at( label ) );
	}

	// get the config by its label
	getConfigByLabel{ |label|
		var config;
		config = configLib.at( label );
		^config;
	}

	// get the configuration message
	getConfigMsg{ |cid|
		var config;
		config = this.getConfig( cid );
		^([cid] ++ config.getConfigMsg);
	}

	// get the configuration itself
	getConfig{ |cid|
		var label,config;
		label = configLabels.findKeyForValue( cid );
		//		label = configLabels.at( cid - 1 );
		config = configLib.at( label );
		^config;
	}



	isConfigured{ |serial|
		// three states : 
		// - send new config(1), 
		// - do not send config(0),
		// - must define config(2)
		var config = hiveMap.at( serial ).configured;
		if ( config.isNil ){
			^2;
		};
		^config;
	}

	// add a new configuration to the library, or replace an old one
	addConfig{ |config|
		config.parseConfig;
		config.hive = this;
		if ( configLabels.at( config.label ).notNil ){
			configLabels.removeAt( config.label );
		};
		configLabels.put( config.label, cidAllocator.alloc );
		configLib.put( config.label.asSymbol, config );

		// TODO: MVC here (in case of multiple guis)
		if ( gui.notNil ){
			gui.updateMenu;
		}
	}

	/*
	// add a new configuration to the library
	replaceConfigID{ |config|
		config.parseConfig;
		//		configLabels.removeAt( config.label.asSymbol );
		configLabels.put(  config.label.asSymbol, cidAllocator.alloc );
	}
	*/

	save{ |name|
		var file;
		var thisf = folder ? thisProcess.platform.userAppSupportDir;
		name = name ? "SWMiniHiveConfig";
		file = File.open( thisf +/+ name, "w" );
		file.write( hiveMap.asCompileString );
		file.write( "\n");
		//		file.write( hiveConfed.asCompileString );
		//		file.write( "\n");
		//		file.write( hiveIdMap.asCompileString );
		//		file.write( "\n");
		file.write( configLabels.asCompileString );
		file.write( "\n");
		file.write( configLib.asCompileString );
		file.write( "\n");
		//	this.writeArchive( thisf +/+ name );
		file.close;
	}

	load{ |name|
		var file;
		var thisf = folder ? thisProcess.platform.userAppSupportDir;
		name = name ? "SWMiniHiveConfig";
		file = File.open( thisf +/+ name, "r" );
		hiveMap = file.getLine(4196*16).interpret;
		//		hiveConfed = file.getLine(4196).interpret;
		//		hiveIdMap = file.getLine(4196).interpret;
		configLabels = file.getLine(1024).interpret;
		configLib = file.getLine(4196*16).interpret;
		file.close;

		configLib.do{ |it| it.hive = this; it.parseConfig; };
		hiveMap.keysValuesDo{ |key,it|
			this.setStatus( key, 5 );
			idAllocator.allocID( it.nodeID );
		};
		configLabels.keysValuesDo{ |key,it|
			cidAllocator.allocID( key );
		};
		
		//	^Object.readArchive( thisf +/+ name );
	}
}
