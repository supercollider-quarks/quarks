/*
\median

An UMap that applies a median filter, effectively removing spikes from incoming values by calculating the median value of a number of samples (length).

value: the value upon which the median is applied (this can be another UMap as well)
length: length of the median window
amount: mix between original signal and filtered signal (0-1)

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \median, { |value = 0.0, length = 3, amount = 1.0|
	var median, delaytime;
	median = Median.kr( length, value ) * amount;
	delaytime = ControlDur.ir * ((length/2)-1).ceil;
	value = DelayN.kr( value, delaytime, delaytime ) * (1-amount);
	UMapOut.kr( median + value );
})
.setSpec( \length, [1,31,\lin,1,3].asSpec, \init )
.mappedArgs_([ \value ])
.category_( 'filter' )