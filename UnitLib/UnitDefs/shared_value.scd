/*
\shared_value

** this Udef should be used in conjunction with 'shared_in' UMaps **

The 'shared_value' Udef creates a unit that is able to share a value or UMap with other units used further in the chain. The 'shared_value' unit would always need to be first in the order of the chain; it sends the value to a bus to the other units. To retreive the value in an other unit use the 'shared_in' UMapDef, and make sure the 'id' setting is the same on both. The value can be set in 6 different formats: value (0-1), freq (2-20000), amp (0-1, curved for amplitude use), trigger (a trigger signal), boolean (true/false) and range (0-1). The range type produces two shared values from the low and high value of the range. The 'shared_in' UMapDef will automatically map the value to the range of the parameter it is connected to, which means it is possible to use for example the 'freq' value of this unit to influence the 'amp' value of another unit. The shared_value itself does not output any sound, it only sends the value. All this will only work _within_ a single chain.

value: the value to share (can be UMap, not available for 'range' type)
range: the range of the value to be shared (not available for 'trigger' and 'boolean' types, in case of 'range' type this becomes the actual output values)
id: the id (0-99) by which the value can be retreived by a 'shared_in' UMap
type: the type of parameter (\value, \freq, \amp, \trigger, \boolean, \range)
lo_id / hi_id: id's of the lo and hi value in case of type 'range'

--
this is an Udef definition file
part of the Unit lib default UDefs set
*/

(
var defs, def;
defs = [ 
	\value, [ 0, 1 ], 
	\freq, FreqSpec(2,20000), 
	\amp, \amp,
	\trigger, TriggerSpec(),
	\boolean, BoolSpec(),
	\range, [ 0, 1 ].asSpec.asRangeSpec,
].clump(2).collect({ |args|
	var type, spec;
	#type, spec = args;
	spec = spec.asSpec;
	Udef( type, {
		var sig, range;
		switch( type,
			\trigger,  { sig = \value.tr( spec.default ); },
			\boolean, { sig = \value.kr( 1.0 ); },
			\range, { sig = \range.kr( [0.0, 1.0] ); },
			{ 
				sig = \value.kr( spec.default ); 
				sig = spec.unmap( sig );
				range = spec.unmap( \range.kr([spec.minval, spec.maxval]) );
				sig = sig.linlin(*range ++ [0,1,\none]);
			}
		);
		if( type === \range ) {
			ReplaceOut.kr( \lo_id.kr(0) + 1400, sig[0] );
			ReplaceOut.kr( \hi_id.kr(1) + 1400, sig[1] );
		} {
			ReplaceOut.kr(\id.kr(0) + 1400, sig);
		};
	}, addToAll: false)
		.setSpec( \value, spec )
		.setSpec( \range, spec.asControlSpec.asRangeSpec )
		.setSpec( \id, PositiveIntegerSpec( 0, 0, 99 ) )
		.setSpec( \lo_id, PositiveIntegerSpec( 0, 0, 99 ) )
		.setSpec( \hi_id, PositiveIntegerSpec( 1, 0, 99 ) );
});

def = MultiUdef( \shared_value, defs, \shared_io, \type, false );

def
)