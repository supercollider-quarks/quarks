OSCBus{
	var <bus;
	var <oscTag;
	var <server;
	var responder;
	var <numChannels;
	var <>scale=1;

	*updateWarning{
		"OSCBus: the order of arguments to create an OSCBus has changed to match the logic of OSCFunc. Please update your code!".warn;
	}

	*new{ |cmdName, nchan, addr, server|
		OSCBus.updateWarning;
		^super.new.init(cmdName, nchan, addr, server);
	}

	init{ |addr, cmdName, nchan, s|
		server = s ? Server.default;
		numChannels = nchan ? 1;
		oscTag = cmdName;
		responder = OSCFunc.new(
			{ |msg|
				bus.setn( msg.copyRange( 1, numChannels ) * scale );
			},
			cmdName, addr );
		this.renew;
	}

	renew{
		if ( bus.notNil, { this.free; });
		bus = Bus.control( server, numChannels );
		responder.add;
	}

	free{
		bus.free;
		responder.remove;
		bus = nil;
	}

	printOn { arg stream;
		stream << this.class.name << "(" <<*
			[oscTag, numChannels, responder.srcID.asCompileString, server.asCompileString]  <<")"
	}

	storeOn { arg stream;
		stream << this.class.name << "(" <<*
			[oscTag, numChannels, responder.srcID.asCompileString, server.asCompileString] <<")"
	}

}

DataBus{
	var <bus;
	var <server;
	var <func;
	var <numChannels;
	var updater;
	var <dT = 0.05;
	var <>scale=1;

	*new{ |function, nchan, server|
		^super.new.init(function, nchan, server);
	}

	init{ |function, nchan, s|
		server = s ? Server.default;
		numChannels = nchan ? 1;
		func = function;
		this.renew;
	}

	renew{
		if ( bus.notNil, { this.free; });
		bus = Bus.control( server, numChannels );
		updater = SkipJack( { bus.setn( func.value * scale ) }, dT, autostart: true );
		//		updater.start;
	}

	dT_{ |dt| dT = dt; updater.dt = dT; }

	free{
		bus.free;
		updater.stop;
		bus = nil;
	}

}