/*
DMX framework for supercollider
(c) 2007-9 Marije Baalman (nescivi)
GNU/GPL v2.0 or later
*/

// DMX is a masterclass which contains the DMXCues, and the DMXDevice
// for now, it is assumed that there is always 1 dmx device
DMX{
	var <>device;

	var <>channeloffset=0;
	var <>maxchannels=512;
	//	var <>autoSet = false;

	// map is an IdentityDictionary, which allows you to use names for channels
	//	var <map;
	// cues are all the cues that are defined for this thing
	var <cues;
	// the current light cue, containing the settings for this moment
	var <>currentCue;

	var <fadeval;

	*new{
		^super.new.init;
	}

	init{
		//		map = IdentityDictionary.new;
		cues = Array.new;
	}

	/*
	setCurrentMap{ arg name;
		map = DMXMap.at( name );
	}
	*/

	setCue{
		device.sendDMX( currentCue );
	}

	blackout{ |time,curve|
		if ( time.isNil, {
			currentCue = DMXCue.new;
			this.setCue;
		},{
			this.fade( DMXCue.new, time, curve );
		});
	}

	fade{ arg to, time=1.0, curve=\linear, timestep=0.040;
		var spec, startCue, endCue, nsteps, ddmx, curdmx;
		spec = [0,1,curve].asSpec;
		startCue = currentCue;
		if ( to.isKindOf( DMXSubCue ), {
			endCue = currentCue.deepCopy.merge( to );
		}, {
			endCue = to;
		});
		//		endCue.data.postln;
		nsteps = round(time/timestep);
		// can't do more than 256 steps, due to 8bit resolution
		if ( nsteps > 256, { nsteps = 256; timestep = time/nsteps; } );
		ddmx = 1/nsteps;
		//		Tdef( \dmxfade ).envir = ();
		Tdef( \dmxfade, {
			//	envir = ();
			//			envir.put( \timestep, timestep );
			//			envir.put( \nsteps, nsteps );
			Tdef(\dmxfade).set( \speed, 1 );
			nsteps.do{ |i|
				currentCue = DMXCue.new;
				//	spec.map( 1-(ddmx*(i+1)) ).postln;
				currentCue.data = (startCue.data * spec.map( 1-(ddmx*(i+1)) ) ) + (endCue.data * spec.map( ddmx*(i+1) ) );
				fadeval = ddmx*(i+1); // could be displayed
				Tdef(\dmxfade).set( \fadeval, fadeval );
				//	currentCue.data.postln;
				this.setCue;
				( timestep / Tdef(\dmxfade).envir.speed ).wait;
			};
		});
		Tdef( \dmxfade ).play;
	}
}

DMXMap{
	classvar maps;

	*initClass{
		maps = IdentityDictionary.new;
	}

	*addMap{ arg name;
		maps.put( name, IdentityDictionary.new );
	}

	*at{ arg name;
		^maps.at( name );
	}

	*putItem{ arg name, itemName, value;
		maps.at( name ).put( itemName, value );
		^maps.at( name );
	}
}

// DMXCue is a complete set of DMX messages, which make up one scene.
DMXCue{
	var <>name;
	var <>id;

	var <>channeloffset=0;
	var <>maxchannels=512;
    var <>data;

    var <>mode;

	classvar spec;

	*initClass{
		ControlSpec.initClass;
		spec = [0, 255, \linear, 1].asSpec;
	}

	*new{ arg offset, maxch;
		^super.new.init( offset, maxch );
	}

	init{ arg offset, maxch;
        mode = \float;
		channeloffset = offset ? channeloffset;
		maxchannels = maxch ? maxchannels;
		data = Array.fill( this.size, 0 );
	}

	size{
		^(maxchannels-channeloffset);
	}

	// returns the cue as an Int8Array, for sending it to the device
	asInt8{
		^spec.map( data ).asInteger; //.as( Int8Array );
	}

	merge{ |subcue|
		subcue.data.do{ |it,i|
			data.put( i, it );
		}
	}

	put{ |id, val|
		data.put( id, val );
	}

	at{ |id|
		^data.at( id );
	}
}

DMXSubCue{
	var <data;

	*new{
		^super.new.init;
	}

	init{
		data = Order.new;
	}

	put{ |id, val|
		data.put( id, val );
	}

	at{ |id|
		^data.at( id );
	}
}