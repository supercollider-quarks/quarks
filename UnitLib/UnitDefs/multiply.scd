/*
\multiply

A simple amplitude multiplication Udef.

mul: amplitude multiplication value (can be UMap)

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

Udef( \multiply, { |mul = 1|
	var in;
	in = UIn.ar(0, 1 );
	in = in * mul;
	UMixOut.ar( 0, in );
})
	.category_( \utility )
	.setSpec( \mul, [-1,1,\lin,0,1].asSpec );