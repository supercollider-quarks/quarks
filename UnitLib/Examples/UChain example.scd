(
// create some Udefs

Udef( \mix2, { |balance = 0.5|
	var l1, l2;
	l1 = UIn.ar( 0 ) * (1-balance);
	l2 = UIn.ar( 1 ) * balance;
	UOut.ar( 0, l1 + l2 )
} ).loadSynthDef;

Udef( \vibrato, { |rate = 1, amount = #[0.0,1.0]|
	UOut.ar( 0, SinOsc.kr( rate ).range(*amount) * UIn.ar( 0 ) )
} ).loadSynthDef;

)

// \sine and \output are already in the Udef bank

x = UChain( \sine, [ \sine, [ \freq, 550, \u_o_ar_0_bus, 1 ]], \mix2, \vibrato, \output );

y = UChain( [ \sine, [ \freq, 660 ] ], \output );

s.notify; // make sure the server is notified!!

x.prepareAndStart;
x.release;

x.fadeOut = 0.5;
x.fadeIn = 0.5;

x.setDur( 5 );
x.setDur( inf ); // infinite duration (default)

x[1].setAudioOut( 0, 1 ); // 2nd sine output to bus 1, so it goes to second mix input

x.gui; // gui without styling
y.gui;

( // gui with styling
w = Window( "x", Rect( 300,25,200,300 ) ).front;
RoundView.useWithSkin( ( 
	labelWidth: 40, 
	font: Font( Font.defaultSansFace, 10 ), 
	hiliteColor: Color.gray(0.33)
), { x.gui(w); };
);
)