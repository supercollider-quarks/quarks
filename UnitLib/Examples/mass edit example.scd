// MassEditU:

// create 10 similar units
y = 10.collect({ U( \sine, [ \freq, 220 rrand: 880, \amp, 0.1 rrand: 0.5 ] ) });

// show them all in a window
w = Window("mass-edit", Rect(571, 101, 264, 381)).front;
w.addFlowLayout;
y.do(_.gui(w));

// create a mass editor
z = MassEditU( y );
z.gui;


// MassEditUChain

// create 10 similar uchains
(
y = 10.collect({ 
	UChain( 
		[ 
			[ \sine, [ \freq, 220 rrand: 880, \amp, 0.1 rrand: 0.5 ] ],
			[ \whitenoise, [ \amp, 0.1 rrand: 0.5 ] ]
		].choose,
		\output
	)
	.dur_( 2 rrand: 10 )
	.setGain( -10 rrand: 10 );
});


)

// show them all in a window
w = Window("mass-edit", Rect(571, 101, 360, 381), scroll: true).front;
w.addFlowLayout;
y.do(_.gui(w));

// create a mass editor
z = MassEditUChain( y );
z.gui;
