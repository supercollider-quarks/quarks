/*
\lag_lpf

An UMap that slows down value changes over time. It is implemented as a two-pole low-pass filter. Please note that changing the 'time' variable during playback may cause the filter to become temporarily unstable.

value: the value upon which the lag is applied (this can be another UMap as well)
time: the time it takes to move to within 0.01% towards a new value

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lag_lpf, { |value = 0.0, time = 0.0|
	UMapOut.kr( LPFLag.kr( value, time).clip(0,1) );
})
.setSpec( \time, [0,10,\lin].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )