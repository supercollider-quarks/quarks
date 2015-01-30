/*
\midi_noteOn_freq

Creates an UMap that listens to MIDI note on messages. It implements diatonic scaling meant for use with \freq parameters. Works similar to all 'midi_xxx' UMapDefs.

value: the actual output value (this will change when a midi message is received, but can also be changed manually)
active: if true, the UMap will listen to the specified MIDI message. Hitting cmd-. will make the UMap stop listening, but whenever the Unit is started, it will re-activate. The MIDI client will be inited when the first midi_xxx UMap is made 'active'.
channel: the MIDI channel to listen to. 0-15 or nil (any channel).
learn: if learn is true, the 'channel' value will be changed according to any incoming MIDI note on message, as long as 'learn' is true.
range: the used range of input note numbers. Notes outside this range will be ignored.
a: the frequency of "A3" (note number 69)
transpose: amount of transposition in semitones
scale: scaling of semitones around (transposed) note number 69
invert: if true, the range is inverted around note number 69

The 'value' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

ValueUMapDef( \midi_noteOn_freq, { |unit, midifunc|
	var func;
	if(MIDIClient.initialized.not,{ MIDIIn.connectAll });
	midifunc.remove;
	func = { |value, range|
		var outSpec;
		outSpec = unit.getSpec( \value );
		value = value - 69;
		if( unit.get( \invert ).booleanValue ) {
			value = value.neg;
		};
		value = (value * unit.get( \scale )) + unit.get( \transpose );
		value = (value + 69).midicps * (unit.get(\a)/440);
		if( outSpec.isKindOf( FreqSpec ) ) {
			unit.set( \value, value.clip( outSpec.minval, outSpec.maxval ) );
		} {
			unit.mapSet( \value, \freq.asSpec.unmap(value) );
		};
	};
	MIDIFunc.noteOn({ |velo, value, chan, src|
		var range;
		if( unit.get( \learn ).booleanValue == true ) {
			unit.set( \channel, chan );
		};
		range = unit.get( \range );
		if( unit.get( \channel ) !? (_ == chan) ? true ) {
			if( value.inclusivelyBetween( *range ) ) {
				func.value( value, range );
			};
		};
	});
}, { |unit, midifunc|
	midifunc.free;
	nil;
}, [ 
	[ \channel, nil, ListSpec( [ nil ] ++ (..15), 0, [ "any" ] ++ (..15).collect(_.asString) ) ],
	[ \learn, false, BoolSpec( false ) ],
	[ \range, [0,127], RangeSpec(0,127,step:1) ],
	[ \a, 440, FreqSpec(400,480) ],
	[ \transpose, 0, [-36,36,\lin,0,0].asSpec ],
	[ \scale, 1, [0.25,4,\exp,0,1].asSpec ],
	[ \invert, false, BoolSpec(false) ],
]  )
	.category_(\control);