/*
\fullEQ

This is a filter with a lowshelf filter, 3 parametric filters and a hihgshelf filter.
Clicking the [edit] button in UChainGUI opens the EQ Edit window. You can set the filter by dragging in the window, setting the values in combination with the popup menu or choosing from the presets. The eq algorithm (EQdef) is the same as that of the global Unit lib EQ (UGlobalEQ), and it shares presets with it.

eq: an UEQ object, holding all settings for the eq.

--
this is an Udef definition file
part of the Unit lib default Udefs set
*/

MultiChannelUdef( \fullEQ, { 
	var in;
	in = UIn.ar( 0, Udef.numChannels );
	UOut.ar(0, UEQ.ar( in, \eq, \default ) );
})	
	.category_( 'eq' );