	// setup: named groups of names
	// can also have overlaps
(
q = q ? ();
q.groups = (
	A: [\adc, \ce, \jt], 
	B: [\do, \aw, \nm],
	C: [\pet, \adc, \jt]
);
q.myGroup = q.groups[\A];
)

(
Tdef(\xxy, { 
	
	var servers = r.servers.atAll(q.groups[\C]);
	var dt;
	100.do { |j| 
		dt = exprand(0.05, 0.2) ;
	servers.do { |x, i|
		(
			instrument: \xxy, 
			server: x.postln, 
			freq: 500 + (100 * (i + j % 7) ), 
			sustain: 0.01, 
			noise: 0.5,
			amp: 1
		).play;
		
		dt.wait;
	};
	};
}).play;
);