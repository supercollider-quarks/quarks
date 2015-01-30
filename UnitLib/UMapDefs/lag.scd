/*
\lag

An UMap that slows down value changes over time. It is implemented as a one-pole low-pass filter, with different settings for up- and downward movement.

value: the value upon which the lag is applied (this can be another UMap as well)
timeUp: the time it takes to move upwards to within 0.01% towards a new value
timeDown: the time it takes to move downwards to within 0.01% towards a new value
order: (1-4) order of the lag, a higher order produces a smoother lag curve. The UMap cascades four lag filters, and the order parameter sets how many of them are actually used.

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \lag, { |value = 0.0, timeUp = 0.0, timeDown = 0.0, order = 1|
	4.do({ |i|
		var amt;
		amt = (order - i).clip(0,1) * (order ** -0.5 );
		value = value.lag( timeUp * amt, timeDown * amt );
	});
	UMapOut.kr(value);
})
.setSpec( \timeUp, [0,10,\lin].asSpec )
.setSpec( \timeDown, [0,10,\lin].asSpec )
.setSpec( \order, [1,4,\lin,0,1].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )