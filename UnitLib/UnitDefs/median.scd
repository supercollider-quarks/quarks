/*
\median

A median filter, effectively removing spikes from incoming values by calculating the median value of a number of samples (length).

length: length of the median window
amount: mix between original signal and filtered signal (0-1)
residue: (boolean) if true the unit will output the difference between the original input signal and the output signal.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \median, { |length = 3, amount = 1.0, residue = 0|
	var in;
	var median, delaytime, delayed;
	in = UIn.ar(0,1);
	median = Median.ar( length, in );
	delaytime = SampleDur.ir * ((length/2)-1).ceil;
	delayed = DelayN.ar( in, delaytime, delaytime );
	in = (amount * median) + ((1-amount) * delayed);
	in = LinXFade2.ar( in, delayed - in, (residue * 2) - 1 );
	UOut.ar( 0, in );
})
	.category_( \filter )
	.setSpec( \length, [1,31,\lin,1,3].asSpec, \init )
	.setSpec( \residue, BoolSpec(false) );