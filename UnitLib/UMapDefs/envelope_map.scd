/*
\envelope_map

Creates an UMap for indexing an envelope.

index: the index (0-1, modulatable)
env: an Env or EnvM object, containing time and level values. 


The 'env' arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \envelope_map, { |index = 0.0|
	UMapOut.kr( UIEnvGen.kr( \env, nil, index ) );
})
	.category_( 'utility' )
	.mappedArgs_( [ \env ] );