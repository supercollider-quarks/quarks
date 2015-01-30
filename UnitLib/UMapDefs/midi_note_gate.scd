/*
\midi_note_gate

Creates an UMap that listens to MIDI velocity in note messages of a specific note. It sets the value to the velocity of a note on message, and to 0 for note-off messages. Works similar to all 'midi_xxx' UMapDefs.

value: the actual output value (this will change when a midi message is received, but can also be changed manually)
active: if true, the UMap will listen to the specified MIDI message. Hitting cmd-. will make the UMap stop listening, but whenever the Unit is started, it will re-activate. The MIDI client will be inited when the first midi_xxx UMap is made 'active'.
nn: the note number to listen to
channel: the MIDI channel to listen to. 0-15 or nil (any channel).
learn: if learn is true, the 'channel' and 'nn' values will be changed according to any incoming MIDI note on message, as long as 'learn' is true.
fromRange: the used range of the midi message
toRange: the output range of the UMap
invert: if true, the input range is inverted (i.e. low values become high and v.v.)
curve: a curvature value for the mapped range. If curve == 0, the range is linear.
clipMode: can be one of the following modes (Symbol):
	'ignore': ignore values outside fromRange
	'clip' (default): clip values outside fromRange
	'fold': fold values outside fromRange
	'wrap': wrap around values outside fromRange
	'none': no clipping applied (values can go outside toRange)
clipSoftness: softens the edges of 'clip' and 'fold' modes.

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \midi_note_gate, { |unit, midifuncs|
	var func;
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifuncs.do(_.free);
	func = { |value, fromRange|
		var toRange, curve;
		toRange = unit.mapGet( \toRange );
		curve = unit.get( \curve );
		if (abs(curve) < 0.001) {
				value = value.linlin( *fromRange ++ [ -1, 1, \none ] );
		} {
			value = value.lincurve( *fromRange ++ [ -1, 1, unit.get( \curve ), \none ] );
		};
		if( unit.get( \invert ).booleanValue ) {
			value = value.neg;
		};
		switch( unit.get( \clipMode ),
			\clip, { value = value.softclip2(1, unit.get( \clipSoftness ) ) },
			\fold, { value = value.softfold2(1, unit.get( \clipSoftness ) ) },
			\wrap, { value = value.wrap2(1) }
		);
		value = value.linlin(-1,1,*toRange ++ [ \none ]);
		unit.mapSet( \value, value );
	};
	[
	MIDIFunc.noteOn({ |value, nn, chan, src|
		var fromRange;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \nn, nn );
			unit.set( \channel, chan );
		};
		fromRange = unit.get( \fromRange );
		if( unit.get( \channel ) !? (_ == chan) ? true && {
			 unit.get( \nn ) == nn;
		}) {
			if( unit.get( \clipMode ) === \ignore ) {
				if( value.inclusivelyBetween( *fromRange ) ) {
					func.value( value, fromRange );
				};
			} {
				func.value( value, fromRange );
			};
		};
	}),
	MIDIFunc.noteOff({ |velo, nn, chan, src|
		var fromRange, value = 0;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \nn, nn );
			unit.set( \channel, chan );
		};
		fromRange = unit.get( \fromRange );
		if( unit.get( \channel ) !? (_ == chan) ? true && {
			 unit.get( \nn ) == nn;
		}) {
			if( unit.get( \clipMode ) === \ignore ) {
				if( value.inclusivelyBetween( *fromRange ) ) {
					func.value( value, fromRange );
				};
			} {
				func.value( value, fromRange );
			};
		};
	})
	];
}, { |unit, midifuncs|
	midifuncs.do(_.free);
	nil;
}, [ 
	[ \nn, 64, [0,127,\lin,1,0].asSpec ],
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \learn, false, BoolSpec( false ) ],
	[ \fromRange, [0,127], RangeSpec(0,127,step:1) ],
	[ \toRange, [0,1], RangeSpec(0,1) ], 
	[ \invert, false, BoolSpec(false) ],
	[ \curve, 0, [-16,16,\lin,0,0].asSpec ],
	[ \clipMode, \clip, ListSpec( [ \ignore, \clip, \fold, \wrap, \none ] ) ],
	[ \clipSoftness, 0, [0,1].asSpec ],
]  )
	.mappedArgs_([\toRange])
	.category_(\control);