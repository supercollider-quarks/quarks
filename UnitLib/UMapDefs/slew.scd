/*
\slew

An UMap that limits the speed of value changes

value: the value upon which the lag is applied (this can be another UMap as well)
timeUp: the time it takes to move upwards across the whole range
timeDown: the time it takes to move downwards across the whole range

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \slew, { |value = 0.0, timeUp = 0.0, timeDown = 0.0|
	UMapOut.kr( Slew.kr( value, 1/timeUp, 1/timeDown ));
})
.setSpec( \timeUp, [0,10,\lin].asSpec )
.setSpec( \timeDown, [0,10,\lin].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )