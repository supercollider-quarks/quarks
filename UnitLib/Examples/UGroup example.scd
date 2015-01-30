(
Udef( \groupOut, { |bus = 0| // use a different bus than the one used inside the chain!!
	ReplaceOut.ar( bus + NumOutputBuses.ir + NumInputBuses.ir, 
		UIn.ar( 0, 1 ) * UEnv.kr( useGlobalGain: false ) );
})
	.category_( \group_io )
	.setSpec( \bus, PositiveIntegerSpec( 0 ) );
	
Udef( \groupIn, { |bus = 0, fadeIn = 0|
	var input;
	input = In.ar( bus + NumOutputBuses.ir + NumInputBuses.ir );
	input = input * Line.ar( 0,1, fadeIn);
	UOut.ar( 0, input );
} ).setSpec( \bus, PositiveIntegerSpec( 0 ) )
	.setSpec( \fadeIn, [0,1,\lin].asSpec )
	.category_( \group_io );
)

(
z = UScore(*[

	UChain(0, 0, 10,
		[ 'bufSoundFile', [ 'soundFile', BufSndFile.newBasic("@resources/sounds/a11wlk01-44_1.aiff",
			107520, 1, 44100, 0, nil, 1, true) ] ],
		[ 'groupOut', [ 'bus', 1 ] ],
		[ 'stereoOutput', [ 'point', Point(-3, 3) ] ]
	)
	.ugroup_('default')
	.addAction_('addToTail'),

	UChain(1.0, 1.0, 10,
		[ 'groupIn', [ 'bus', 1, 'fadeIn', 0.1 ] ],
		[ 'delay', [ 'time', 0.2, 'maxTime', 0.4, 'amp', 1.0 ] ],
		[ 'stereoOutput', [ 'point', Point(2, 4) ] ]
	)
	.ugroup_('default')
	.addAction_('addToTail'),

	UChain(2.0, 2.0, 5.0,
		[ 'groupIn', [ 'bus', 1, 'fadeIn', 0.1 ] ],
		[ 'delay', [ 'time', 0.3, 'maxTime', 0.4, 'amp', 1.0 ] ],
		[ 'stereoOutput', [ 'point', Point(6, 2) ] ]
	)
	.ugroup_('default')
	.addAction_('addToTail'),

	UChain(3.0, 3.0, 8,
		[ 'groupIn', [ 'bus', 1, 'fadeIn', 0.1 ] ],
		[ 'delay', [ 'time', 0.4, 'maxTime', 0.4, 'amp', 1.0 ] ],
		[ 'stereoOutput', [ 'point', Point(-2, -3) ] ]
	)
	.ugroup_('default')
	.addAction_('addToTail')

]
);
z.gui;
)
