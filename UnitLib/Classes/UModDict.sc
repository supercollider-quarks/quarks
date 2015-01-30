UModDict : UEvent {
	
	var <>mods;

	*new { |...mods|
		^super.newCopyArgs().init( mods );
	}
	
	init { |inMods|
		// filter out double key mods
		inMods.do({ |item| this.add( item ); });
	}
	
	at { |key|
		^mods.detect({ |item| item.key === key });
	}
	
	units { ^mods }
	groups { ^nil }
	gain { ^0 }
	gain_ { }
	getGain { ^0 }
	muted {^false}
	fadeIn {^0}
	fadeOut {^0}
	fadeInCurve {^0}
	fadeOutCurve {^0}
	
	gui { |parent, bounds| ^UGUI( parent, bounds, this ) }

	add { |mod|
		// replace any mod with this key, or add new if not available
		var index;
		index = mods !? _.detectIndex({ |item| item.key === mod.key });
		if( index.isNil ) {
			mods = mods.add( mod );
		} {
			mods[ index ].dispose;
			mods[ index ].disconnect;
			mods.put( index, mod );
		};
	}
	
	asUModFor { |unit|
		mods = mods.collect( _.asUModFor(unit) );
		^this;
	}
	
	connect { |unit|
		mods.do(_.connect(unit));
	}
	
	disconnect {
		mods.do(_.disconnect);
	}
	
	prepare { |unit, startPos = 0|
		mods.do(_.prepare(unit, startPos));
	}
	
	start { |unit, startPos = 0, latency = 0.2|
		mods.do(_.start( unit, startPos, latency ));
	}
	
	stop { |unit|
		mods.do(_.stop(unit));
	}
	
	dispose { |unit|
		mods.do(_.dispose(unit));
	}
	
	pause { |unit|
		mods.do(_.pause(unit));
	}
	
	unpause { |unit|
		mods.do(_.unpause(unit));
	}
	
	storeArgs {
		^mods;
	}

}