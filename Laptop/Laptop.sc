Laptop {
	var <internals;
	var <dn;

	// processes:
	var <processes,<processWatcher;
	var <procStats;

	var <tempWatcher;
	var <camera;

	// internal devices
	var <keyboard,<touchpad;

	var <>keyboardID;
	var <>touchpadID;

	*new{
		^super.new.init.myInit;
	}

	myInit{
		this.initKeyboard;
		this.initTouchpad;
	}

	init{
		GeneralHID.buildDeviceList;
		internals = SWDataNetwork.new;
		dn = internals;
		this.initProcesses;
		this.initTemperature;
		this.initCamera;
	}

	initKeyboard{
		// open the keyboard. The keyboard will provide one node in the datanetwork, which will have one slot which is the current value of the key pressed.
		// furthermore, the device will have a couple of hundred of keys that are pressed and unpressed, available through keys.

		keyboard = GeneralHID.open( GeneralHID.findBy( *keyboardID ) );

		internals.addExpected( 1, \keyboard, 1 );
		internals.add( \keyVal, [1,0] );
		internals.nodes[1].scale = 1/ (keyboard.slots[1].size);
		
		keyboard.slots[1].do{ |it,i| 
			if ( keyboard.slots[1][i].notNil, {
				keyboard.add( i, [1,i] );
				keyboard.slots[1][i].action = { |v|
					internals.setData( 1, [v.code] )
				};
			})
		};
	}

	key{ // shorcut for the keyboard
		^keyboard;
	}

	initTouchpad{
		touchpad = GeneralHID.open( GeneralHID.findBy( *touchpadID ) );
		internals.addExpected( 3, \touchpad );

		touchpad.addToNetwork( internals, 3 );

		// can't be accessed properly???
	}

	initProcesses{
		internals.addExpected( 5, \sclang );
		internals.addExpected( 6, \scsynth );
		internals.addExpected( 7, \swingosc );
		processes = Dictionary.new;
		processes.put( 5, "sclang");
		procStats = Dictionary.new;
		processWatcher = SkipJack.new(
			{
				if ( Server.all.asArray.collect{ |it| it.serverRunning }.detect{ |it| it }.notNil )
				{ processes.put( 6, "scsynth" ); }
				{ processes.removeAt( 6 ) };
				
				if ( SwingOSC.default.serverRunning, {
					processes.put( 7, "java" );
				},{ processes.removeAt( 7 ) });
				processes.keysValuesDo{ |k,it|
					procStats.put( it.asSymbol, it.getStats );
					internals.setData( k, procStats[it.asSymbol].getPairs([\RSS,\VSZ,\CPU,\MEM]).clump(2).collect{ |it| it[1]})
				};
			}, 1.0, name: "ThinkpadProcessWatcher" );

		[\RSS,\VSZ,\CPU,\MEM].do{ |it,i|
			(5..8).do{ |j| 	internals.add((it++j).asSymbol, [j,i]); };
		};

	}

	initTemperature{
		internals.addExpected( 10, \temperature );

		tempWatcher = SkipJack.new({
			var tempC,fan,temps;
			tempC = "sensors thinkpad-isa-0000 2> /dev/null".unixCmdGetStdOut;
		
			tempC = tempC.split( $\n );
			tempC = tempC.collect{ |it| it.replace( "       ","").replace("      ","").split( $: ) };
			tempC.do{ |it,i| if (it.size > 1 ){ it[1] = it[1].split( $( ).keep(1).unbubble.replace("  ",""); } };
		
			fan = tempC[2];
			temps = tempC.copyRange( 3, tempC.size - 3);
		
			fan[1] = fan[1].split( $ );
			fan = [ fan[0], fan[1][0].asFloat ];

			
			temps.do{ |it| var s,e; s = it[1].find("+"); e = it[1].find("C"); it[1] = it[1].copyRange( s + 1, e - 3).asFloat };

			// set the labels:
			internals.add( fan[0].asSymbol, [10,0] );
			temps.flop.at(0).do{ |it,i| internals.add( it.asSymbol, [10,i+1]);};

			internals.setData( 10, [ fan[1] ] ++ temps.flop.at(1)  );
			
		}, 1.0, name: "ThinkpadTemperatureWatcher" );

	}

	initCamera{
		internals.addExpected( 8, \motiontrackosc );
		internals.addExpected( 9, \head );
		camera = MotionTrackOSC.new(0,true);
		camera.closeAction = { processes.removeAt(8) };

		camera.dirAction = { |vals| if ( vals[0] == -1, { internals.setData( 9, vals.copyToEnd(1); ) } ) };
		processes.put( 8, "motiontrackosc");

		Task({
			5.0.wait;
			this.camera.hide(1);
			this.camera.subscribeDir(1);
			1.0.wait;
			internals.add( \headx, [9,0]);
			internals.add( \heady, [9,1]);
			internals.add( \headangle, [9,2]);
		}).play;
		
	}

	at{ |key|
		^internals.at( key )
	}
}