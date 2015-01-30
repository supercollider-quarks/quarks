
x = UChain( \sine, \output );

x[0].freq = UMap( \envelope, [ \env, Env([0.5,0.75,0.25],[2,3]) ] );

x[0].freq = UMap( \line, [ \range, [440,880], \duration, 4 ] );

x.prepareAndStart;
x.release;
x.gui;

(
UMapDef( \mapkr, { |bus = 0, range = #[0.0,1.0]|
	var sig;
	sig = In.kr(bus + UIn.firstControlBus);
	sig = sig.linlin(0,1,*range);
	UMapOut.kr(sig);
});

Udef( \lfo, { |freq = 2, amp = 1|
	var sig;
	sig = SinOsc.kr(freq, 0, amp).range(0,1);
	UOut.kr(0,sig);
}).setSpec( \freq, FreqSpec(0.01,100) );
)

y = UChain( \lfo, \sine, \output );
y[1].freq = UMap( \mapkr, [ \range, [0.5,0.7]]);

y.prepareAndStart;
y.release;