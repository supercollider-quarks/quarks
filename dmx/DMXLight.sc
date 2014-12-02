/*
DMX framework for supercollider
(c) 2007-9 Marije Baalman (nescivi)
GNU/GPL v2.0 or later
*/

// This contains a set of higher level classes
// Classes for a specific light, or a group of lights, or a complete set

DMXSet{
	var <>name;
	var <groups;

	*new{ |name|
		^super.new.init.name_( name );
	}

	init{ 
		groups = ();
	}

	addGroup{ |group|
		groups.put( group.name, group );
	}

	removeGroup{ |group|
		groups.removeAt( group.name );
	}
}

DMXGroup{
	var <>name;
	var <lights;

	*new{ |name|
		^super.new.init.name_( name );
	}

	init{ 
		lights = ();
	}

	addLight{ |light|
		lights.put( light.name, light );
	}

	removeLight{ |light|
		lights.removeAt( light.name );
	}
}

DMXLight{
	var <>name;
	var <properties;
	var <channels;

	*new{ |name|
		^super.new.init.name_( name );
	}

	init{ 
		properties = ();
		channels = ();
	}

	setStrobe{ |chans,vals|
		vals = vals ? [0,0,0];
		properties.put( \duration, vals[0] );
		properties.put( \intensity, vals[1] );
		properties.put( \rate, vals[2] );
		channels.put( \duration, chans[0] );
		channels.put( \intensity, chans[1] );
		channels.put( \rate, chans[2] );
		DMX.map.put( name++\_++\duration, chans[0] );
		DMX.map.put( name++\_++\intensity, chans[1] );
		DMX.map.put( name++\_++\rate, chans[2] );
	}

	setGob{ |chans,vals|
		/* set to useful stuff here
		vals = vals ? [0,0,0];
		properties.put( \duration, vals[0] );
		properties.put( \intensity, vals[1] );
		properties.put( \rate, vals[2] );
		channels.put( \duration, chans[0] );
		channels.put( \intensity, chans[1] );
		channels.put( \rate, chans[2] );
			DMX.map.put( name++\_++\duration, chans[0] );
		DMX.map.put( name++\_++\intensity, chans[1] );
		DMX.map.put( name++\_++\rate, chans[2] );
		*/
	}

	setLight{ |chans,vals|
		vals = vals ? [0,0];
		properties.put( \intensity, vals[0] );
		properties.put( \color, vals[1] );
		channels.put( \intensity, chans[0] );
		channels.put( \color, chans[1] );
		DMX.map.put( name++\_++\intensity, chans[0] );
		DMX.map.put( name++\_++\color, chans[1] );
	}

	set{ |key,val|
		properties.put( key, val );
	}
}

// with DMXColor you can store colormaps corresponding to the needed DMX value to get the specified color
DMXColor{
	classvar <>maps;
	classvar <>currentMap;

	*initClass{
		maps = ();
		currentMap = ();
	}

	*get{ |color|
		^currentMap.at( color );
	}
}