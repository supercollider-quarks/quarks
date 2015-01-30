
// Show all types of spec
(
x = Udef(\test,{ |bus = 0, freq = 500,thing2=1, thing3 =1,thing4,thing5,thing6|
	 Out.ar(bus,WhiteNoise.ar*0.001*\point.kr(0,0)) },
	[[\bus,0,IntegerSpec()],
	[\point, Point(1,2),PointSpec(Rect(0,0,20,20))], // is not constraining...
	[\thing2,1,BoolSpec()],
	[\thing3,1,ListSpec([1,2,3,4],2)],
	//[\thing4,1,RangeSpec()] this is broken
]);
y = U(\test);
y.gui;
)

RangeSpec()

//EQ

(
var numChannels = 2;
x = MetaU(\param_beq,[\numChannels,numChannels],[\eq_controls,EQSpec().default]);
y = Udef(\noise,{ UOut.ar(0, WhiteNoise.ar.dup(numChannels) *0.1) });
r = MetaU(\copy,[\numChannels,numChannels]);
z = UChain(\noise,x,r);
z.prepareAndStart(s);
x.unit.gui(nil,Rect(0,0,700,700))
)

x.set(\eq_controls,EQArg([ [ 100, 0, 1 ], [ 250, 0, 1 ], [ 1000, 0, 1 ], [ 3500, 0, 1 ], [ 10000, -24, 1 ] ]))
