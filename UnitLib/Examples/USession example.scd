(
f = { 12.collect({ |i|
		var evt;
		evt = UChain(i/2,i+1,rrand(3.0,10.0),false,\sine, \output).fadeOut_(1).fadeIn_(1);
		evt.units[0].set(\freq,rrand(200.0,600.0) );
		evt;
	}) };
z = UScore(*(12.collect({ |i|
		var evt;
		evt = BufSndFile("@resources/sounds/a11wlk01-44_1.aiff",
				rate: (i-6).midiratio, loop: [true,false].wrapAt(i) ).makeUChain
				.releaseSelf_(true).startTime_(i/2).track_(i).fadeOut_(1).fadeIn_(1);
		
		if( evt.duration == inf ) {
			evt.duration = 8; // looped events stopped by UScore
		};
		evt;
	})++f.()++[
	UScore(*f.()++[
		UScore(*f.())
	]
)]));
z.cleanOverlaps;
x = USession(z,UChain(\sine,\output),UChain(\sine,\output),UChain(\sine,\output),UChain(\sine,\output)
,UChainGroup(UChain([\sine,[\freq,200]],\output),UChain([\sine,[\freq,400]],\output)));
y = USessionGUI(x);
)