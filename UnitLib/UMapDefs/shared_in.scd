/*
\shared_in

** this UMapDef should be used in conjunction with 'shared_out' **

The shared_in UMapDef can receive a value from UMaps used earlier in the chain. This is useful when multiple units or unit parameters need to use the same information, or derrive things from it. The shared_in would always need to come after a 'shared_out' in the order of the chain; it recieves the data from a bus. To send the data use the 'shared_out' UMapDef in an other unit or parameter earlier in the chain, and make sure the 'id' setting is the same on both. There can be multiple 'shared_in' UMaps with the same 'id' in one chain, which would all receive data from the same 'shared_out'. 'shared_in' automatically maps the incoming value to the range of the parameter it is connected to. I.e. if the 'shared_out' containse a frequency value between 2 and 20000, and the 'shared_in' is connected to an amplitude parameter, the value will be scaled from 2-20000 to 0-1, according to the specs. This scaling can be influenced both with the 'range' parameter on 'shared_out' and the 'range', 'curve', 'invert' and 'clipMode' settings on 'shared_in'. All this will only work _within_ a single chain.

id: the id (0-99) by which the value can be retreived from 'shared_out_point'
range: the range to map the received value to
curve: a curve setting into which the value can be wrapped
invert: (boolean) if true the range is inverted; high becomes low and v.v.
clipMode: if the 'range' on the corresponding 'shared_out' UMap is not full, the sent value may go out of bounds. The clipMode on 'shared_in' defines how to deal witch such values:
	0: clip; clip everything between the min and max of the 'range'
	1: fold; fold (mirror) values outside the range so that they fall into it
	2: wrap; wrap around at the borders of the range
	3: none; no clipping; values are allowed to go outside the 'range'
clipSoftness: an amount of softening applied to the edges of the range when in 'clip' or 'fold' mode

The 'range' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \shared_in, { |id = 0, range = #[0.0, 1.0], curve = 0.0, invert = 0, clipMode = 0, clipSoftness = 0|
	var sig;
	sig = (In.kr(id + 1400) * 2) - 1;
	sig = Select.kr( curve.inRange(-0.001,0.001), [
		sig.lincurve(-1,1,-1,1, curve, \none ),
		sig
	]);
	sig = sig * ((invert * -2) + 1);
	sig = Select.kr( clipMode, [
		sig.softclip2(1, clipSoftness),
		sig.softfold2(1, clipSoftness),
		sig.wrap2(1),
		sig
	]);
	sig = sig.linlin(-1,1,*range ++ [ \none ]);
	UMapOut.kr(sig);
})
	.mappedArgs_([ \range ])
	.setSpec( \id, PositiveIntegerSpec( 0, 0, 99 ) )
	.setSpec( \clipMode, ListSpec( (..3), 0, [ \clip, \fold, \wrap, \none ] ) )
	.setSpec( \clipSoftness, [0,1,\lin,0,0].asSpec )
	.setSpec( \curve, [-16,16,\lin,0,0].asSpec )
	.setSpec( \invert, BoolSpec(false) )
	.category_( 'shared_io' );