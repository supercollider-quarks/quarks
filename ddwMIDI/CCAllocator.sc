
CCAllocator {
	// allocates available continuous controllers
	// some arg names can be reserved like \pb and \mw
	// uses instance rather than class interface b/c you might
	// want a separate allocator for each MIDI channel, or other uses
	
	classvar	<default;
	
	var	<available, <used,		// arrays of controller numbers
							// in VoicerMIDISocket, \pb is a valid controller num
							// (routes to pitch bend)
		<reserved;
	
	*new { arg controlNums, controlTypes, reservedTypes;
		^super.new.init(controlNums, controlTypes, reservedTypes)
	}
	
	*initClass {
		default = CCAllocator.new;
	}
	
	init { arg controlNums, controlTypes, reservedTypes;
		var cc, i;
			// these default settings are for my ReMOTE 25, factory settings
			// controllers and their buttons
		controlNums = controlNums ? #[
			[5, 2], [80, 3], [82, 4], [83, 6], [85, 8], [104, 9], [106, 11], [105, 12],
			[70, 13], [40, 14], [41, 15], [72, 16], [44, 17], [43, 18], [107, 19], [102, 20],
			[108, 21], [109, 22], [110, 23], [111, 24], [114, 25], [115, 26], [116, 27],
				[117, 28], 1, \wheel, \kbprs, 120, 121, \omni];
		controlTypes = controlTypes ? #[\knob, nil, nil, nil, nil, nil, nil, nil,
				\encoder, nil, nil, nil, nil, nil, nil, nil,
				\slider, nil, nil, nil, nil, nil, nil, nil,
				\mw, \pb, \touch, \xtouch, \ytouch, \omni];
			// reserved types correspond to argument names or control types
			// that will automatically be allocated to this specific control by get()
		reservedTypes = reservedTypes ? #[\mw, \pb, \touch, \touchx, \touchy, \omni];

		available = CControl.newArray(controlNums, controlTypes);
		used = Array.new;
		reserved = IdentityDictionary.new;
		reservedTypes.do({ arg type;
			i = controlTypes.indexOf(type);
			i.notNil.if({
				cc = available.at(i);
				reserved.put(cc.type, cc)
			});
		});
	}
	
	get { arg type, name;	// get a controller, by type or any kind if type is nil
			// if it's a reserved name or type, get a reserved control OR first available
			// if not, it will never return a reserved control
		var ccnum, i;
		reserved.includesKey(name ? type).if({  // name is reserved
			ccnum = reserved.at(name ? type);	// get the controller from there
		});
		(ccnum.isNil && (available.size > 0)).if({	// must have an available control
			i = 0;					// look for one, but prefer non-reserved
			ccnum = available.first;
			{ (i < available.size) && (reserved.values.includes(ccnum)
					|| type.notNil.if({ ccnum.tryPerform(\type) != type }, false))
			}.while({
				i = i+1;
				ccnum = available.at(i)/*.dump*/;
			});
		});
		ccnum.notNil.if({
			available.remove(ccnum);
			used = used.add(ccnum);
		});
		^ccnum
	}
	
	free { arg ccnum;		// release this control to be used again
		var index, cc;
		ccnum = ccnum.value;	// if it's a CControl, it must be converted to the cc number
		used.do({ arg u, i;
				// u.type test needed for \pb
			((u.value == ccnum) || (u.type == ccnum)).if({ index = i });
		});
		index.notNil.if({
			cc = used.removeAt(index);
			available = [cc] ++ available;
		});
	}
	
	freeAll { this.init }
	
}

CControl {
	var	<>ccnum, <>type, <>buttonnum, <>index;	// index is for sequential numbering
											// in the gui
	
	*new { arg ccnum, type, index;
		^super.newCopyArgs(ccnum, type.asSymbol, nil, index).init
	}
	
	init {
		ccnum.isArray.if({
			buttonnum = ccnum.at(1);
			ccnum = ccnum.at(0);
		});
	}
	
	*newArray { arg ccnums, types;	// to build a list of controllers
		var	lasttype, index = -1;
		^Array.fill(ccnums.size, { arg i;
			types[i].notNil.if({ index = -1 });
			index = index + 1;
			CControl(ccnums.at(i), lasttype = (types.at(i) ? lasttype), index);
		});
	}
	
	value { ^ccnum }

	asString { ^[ccnum, type].asString }
	
	shortName { ^type.asString[0] ++ index }

}

CCRespGroup {
	var	<responders,		// so a single midi control can be routed to many places
		<parent;
	
	*new { arg parent ... responders;
		^super.new.init(parent, responders);
	}
	
	init { arg p ... resp;
		parent = p;
		responders = resp.flat;
	}
	
	add { arg ... resp;
		responders = (responders ++ resp).flat;
	}
	
	remove { arg ... resp;
		var removed;
		removed = resp.flat.collect({ arg r;
			responders.remove(r);
		});
		^removed
	}
	
	search { arg dest;
		var resp;
		dest.isNil.if({ ^nil });
		responders.do({ arg r;
			(r == dest).if({ resp = r });
		});
		^resp
	}
	
	update {
		responders.copy.do({ arg r;
			r.notNil.if({
				(r.destination.tryPerform(\active) ? true).not.if({
					r.free;
					parent.removeControl(r);
				});
			});
		});
	}
	
	set { arg value, divisor = 127, num;
		responders.do({ arg r; r.set(value, divisor, num) });
	}
	
	setSync { arg value, divisor = 127, num;
		responders.do({ arg r; r.setSync(value, divisor, num) });
	}
	
	free {
		responders.do({ arg r; r.free });
	}
	
	size { ^responders.size }
}
