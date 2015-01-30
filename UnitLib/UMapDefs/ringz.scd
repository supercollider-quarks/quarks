/*
\ringz

Creates an UMap that routes an input value through a ringing filter with attack and decay and adds the result to the original value. This causes oscillations to happen at value changes.

value: the value upon which the lag is applied (this can be another UMap as well)
freq: the frequency of the filter. Rapid changes of this value may cause the filter to be temporarily unstable
attackTime: the attack time of the 
decayTime: the decay time of the ringing filter
amp: amplitude of the ringing filter

The value arg range is mapped to that of the parameter to which the UMap is connected.

--
this is an UMapDef definition file
part of the Unit lib default UMapDefs set
*/

UMapDef( \ringz, { |value = 0.5, freq = 2, attackTime = 0.001, decayTime = 1, amp = 0.1|
	var sig, scaledValue;
	scaledValue = (value - DC.kr( value ));
	sig = Formlet.kr( scaledValue, freq, attackTime, decayTime ) * amp;
	sig = (sig + value).clip(0,1);
	UMapOut.kr( sig );
})
.setSpec( \freq, FreqSpec(0.01,300, default: 2) )
.setSpec( \attackTime, [0.001,10,\exp, 0, 1].asSpec )
.setSpec( \decayTime, [0.001,10,\exp, 0, 1].asSpec )
.mappedArgs_([ \value ])
.category_( 'filter' )