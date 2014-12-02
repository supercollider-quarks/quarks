SWDataAction{
	
	classvar <all;

	var <id;
	var <function;
	var <>settings;
	var <network;

	*initClass{
		all = IdentityDictionary.new;
	}

	*new{ |id,network,func|
		^super.new.init( id, network, func );
	}

	init{ |ky,netw,func|

		network = netw;
		id = ky;
		settings = IdentityDictionary.new;

		this.initFunction( func );

		all.put( id, this );

	}

	set{ |key,val|
		settings.put( key, val );
	}

	initFunction{ |func|
		function = func;
	}

	value{ arg args;
		//		args.postln;
		network.setData( id, function.value( args, settings ) );
	}

	copy{ |newid|
		^SWDataAction.new( newid, network, function ).settings_(settings);
	}

}

ByteDecodeAction : SWDataAction{
	
	initFunction{ |func|
		function = { |dat| dat.collect{ |v| 8.collect{ |i| v.bitTest( i ).binaryValue; }; }.flatten };
	}

}

LinLeakAction : SWDataAction{

	initFunction{ |func|
		settings.put( \sum, [0] );
		settings.put( \leakRate, 0.015 );
		settings.put( \pumpRate, 1 );
		settings.put( \oldx, [0] );
		settings.put( \dx, [0] );

		function = { |dat,set| 
			set.put( \dx, (set[\oldx]-dat).abs; );
			set.put( \oldx, dat );
			// pump/step by change in x less leak		
			set.put( \sum, 
			(set[\sum] + 
				(set[\dx] * set[\pumpRate]) - 
				set[\leakRate]).max(0); // inflate
			);
			set[\sum]; // return value
		}
	}

	reset{
		settings.put( \sum, [0] );
	}
}

GeomLeakAction : SWDataAction{

	initFunction{ |func|
		settings.put( \sum, [0] );
		settings.put( \leakRate, 0.015 );
		settings.put( \pumpRate, 1 );
		settings.put( \oldx, [0] );
		settings.put( \dx, [0] );

		function = { |dat,set| 
			set[\dx]= (set[\oldx]-dat).abs; 
			set[\oldx]=dat;
			set[\sum] =
			((1-set[\leakRate])*set[\sum] + (set[\dx] * set[\pumpRate])).max(0);
			set[\sum]; // return value
		}
	}

	reset{
		settings.put( \sum, [0] );
	}
}
