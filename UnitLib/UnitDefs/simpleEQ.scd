/*
\simpleEQ

This is a filter with a lowshelf filter, 1 parametric filter and a hihgshelf filter.

Clicking the [edit] button in UChainGUI opens the EQ Edit window. You can set the filter by dragging in the window, setting the values in combination with the popup menu or choosing from the presets. 

lowMidHi: an UEQ object, holding all settings for the eq.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

EQdef( \low, BLowShelf, \mid, BPeakEQ, \hi, BHiShelf )
	.defaultSetting_([ 
		[ 250, 1, 0 ],
		[ 1000, 1, 0 ], 
		[ 6000, 1, 0 ]
	])
	.name_( \simpleEQ );

Udef( \simpleEQ, { 
	var in;
	in = UIn.ar( 0, 1 );
	UOut.ar(0, UEQ.ar( in, \lowMidHi, \simpleEQ ) );
})	
	.category_( 'eq' );
	