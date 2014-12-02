SWDataStringNode : SWDataNode {

	var <type = 1;

	initSlots{
		slots.do{ |it,i| slots.put( i, SWDataStringSlot.new([id,i]) ); };
	}

	data_{ |indata|
		if ( indata.size == slots.size , {
			//		data = indata * scale; // no scaling with strings
			data = indata;
			data.do{ |it,i| slots[i].value = it };
			action.value( data, this );
			this.setLastTime;
			^true;
		});
		^false;
	}

	createBus{ |s|
		^this.shouldNotImplement;
	}

	freeBus{
		^this.shouldNotImplement;
	}

	// JITLib support
	kr{
		^this.shouldNotImplement;
	}

	// ---------- debugging and monitoring -------

	monitor{ |onoff=true|
		^this.shouldNotImplement;
	}

	monitorClose{
		^this.shouldNotImplement;
	}


}

SWDataStringSlot : SWDataSlot{

	var <type = 1;

	value_{ |val|
		//	value = val * scale; // no scaling with strings
		value = val;
		// map to control spec from input range after scaling
		//	if ( map.notNil, { value = map.map( range.unmap( value ) ) } );
		action.value( value );
		debugAction.value( value );
		//	if ( bus.notNil, { bus.set( value ) } );
	}

	logvalue{
		^value.asCompileString;
	}

	map_{ |mp|
		^this.shouldNotImplement;
	}

	range_{ |mp|
		^this.shouldNotImplement;
	}

	// currently only does minimum:
	calibrate{ |steps=100| // about two seconds currently
		^this.shouldNotImplement;
	}

	// --- bus support ----

	createBus{ |s|
		^this.shouldNotImplement;
	}

	freeBus{
		^this.shouldNotImplement;
	}

// JITLib support
	kr{
		^this.shouldNotImplement;
	}

	/// ------- debugging and monitoring ------

	monitor{ |onoff=true|
		^this.shouldNotImplement;
	}

	monitorClose{
		^this.shouldNotImplement;
	}

}