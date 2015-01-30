MIDISpec : Spec {
	
	classvar <>valueSpecs, <>typeSpecs;
	
	var <>types, <>defaultType = \noteOn;
	
	*initClass {
		
		valueSpecs = (
			\chan: MIDINumberSpec(maxval:15),
			\note: MIDINoteSpec(),
			\velo: MIDIValueSpec(default:64),
			\num: MIDINumberSpec(),
			\val: MIDIValueSpec(),
			\bend: MIDIValueSpec(maxval:16383, default:8192)
		);
		
		typeSpecs = (
			\noteOn: [ \chan, \note, \velo ],
			\noteOff: [ \chan, \note, \velo ],
			\polyTouch: [ \chan, \note, \val ],
			\cc: [ \chan, \num, \val ],
			\program: [ \chan, \num ],
			\afterTouch: [ \chan, \val ],
			\pitchBend: [ \chan, \val ]
		);
	}
	
	getNames { |type|
		^typeSpecs[ type ];
	}
	
	getSpecs { |type|
		^this.getNames( type ).collect({ |item|
			valueSpecs[ item ];
		});
	}
	
	constrain { |array| // outputs an array [ type, channel, val1 (, val2) ]
		var type, specs;
		if( array.size == 0 ) {
			if( array.class == Symbol ) {
				type = array;
				array = [ type ];
			} {
				type = defaultType;
				array = [ type, array ];
			};
		} {
			if( array[0].class == Symbol ) {
				type = array[0];
			} {
				type = defaultType;
				array = [ type ] ++ array;
			};
		};
		specs = this.getSpecs( type );
		if( specs.isNil ) { 
			type = defaultType;
			specs = this.getSpecs( type );
		};
		array = array.extend( specs.size + 1, nil );
		^[ array[0] ] ++ array[1..].collect({ |item, i|
			specs[i].constrain( item );
		});
	}
	
	
}

MIDIValueSpec : ControlSpec {
	
	var <>allowNil = false;
	
	*new {  arg minval=0, maxval=127, warp='lin', step=1, default, units;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? 0, units ? ""
			).init
	}
	
	constrain { arg value; // allows nil
		^if( value.notNil ) { value.asFloat.clip(clipLo, clipHi).round(step) } {
			if( allowNil.not ) { default } 
		};
	}
}

MIDINumberSpec : MIDIValueSpec {
	
	*new {  arg minval=0, maxval=127, warp='lin', step=1, default, units;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? 0, units ? ""
			).init
	}
	
}

MIDINoteSpec : MIDIValueSpec {
	
	*new {  arg minval=0, maxval=127, warp='lin', step=1, default, units;
		^super.newCopyArgs(minval, maxval, warp, step,
				default ? 60, units ? ""
			).init
	}
}