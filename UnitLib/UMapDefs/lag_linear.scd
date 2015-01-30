/*
\lag_line

An UMap that slows down value changes over time. It does this by creating a line between the old and new value

value: the value upon which the lag is applied (this can be another UMap as well)
time: the time it takes to move to the new value
curve: a curve value for the line. 0 means linear, a negative curve makes the value move faster in the beginning, a positive curve makes it move faster at the end.

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lag_line, { |value = 0.0, time = 0.0, curve = 0.0|
	UMapOut.kr(value.varlag(time, curve));
})
.setSpec( \time, [0,10,\lin].asSpec )
.setSpec( \curve, [-40,40,\lin].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )