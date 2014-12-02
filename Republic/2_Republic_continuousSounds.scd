// continuous sounds are not fully tested yet - please try them with many participants,
// and report results to jrh and adc. 

// Node allocation should be fine, as each allocator uses IDs for its range only.
// Bus allocation also looks ok - test with people playing Ndefs on each others machines.


// for testing on a single machine, make a second server:
(
if (r.nameList.includes(\otto).not) { 
	y = SharedServer(\otto, NetAddr("127.0.0.1", 57108), r.defaultServerOptions, r.nextFreeID);
	y.boot; y.makeWindow;
	r.servers.put(\otto, y);
};
)

// continuous sound on my own machine: 
(
x = { |freq = 400, mod = 12, amp = 0.1| 
	SinOsc.ar(
		freq * LFNoise0.kr([8, 12], mod).midiratio
	) * amp.lag(0.5)
}.play;
)
x.set(\freq, 600, \mod, 3);
x.free;

(	// play it on a different machine: 
x = { |freq = 400, mod = 12, amp = 0.1| 
	SinOsc.ar(
		freq * LFNoise0.kr([8, 12], mod).midiratio
	) * amp.lag(0.5)
}.play(r.servers[\otto]);
)
x.set(\freq, 600, \mod, 24);
x.free;



	// to play Ndefs on the server you like, 
	// set Ndef.defaultServer properly. 
Ndef.defaultServer = r.myServer;

	// get the proxyspace, and set it to p: 
	// now Ndefs and ~proxies in proxyspace p are the same. 
p = Ndef.dictFor(r.myServer); 
p.push;

Ndef(\a, { |freq = 400, mod = 12, amp = 0.1| 
	SinOsc.ar(
		freq * LFNoise0.kr([8, 12], mod).midiratio
	) * amp.lag(0.5)
});

Ndef(\a).play

Ndef(\a) === ~a;	// true!
~a.end(4);

~a.play;
~a.set(\freq, 300, \mod, 24);

	// play on the Ndef.defaultServer - usually my shared server
(
Ndef(\pulsy, { |freq1 = 5, freq2 = 50, freq3 = 0.5, wid = 0.2, ffreq = 2000, reso = 5|
	var pulses = LFPulse.ar([freq1, freq2, freq3], 0, wid);
	pulses = Splay.ar([ pulses[0] * pulses[1], pulses[1] * pulses[2], pulses[2] * pulses[0]]);
	RLPF.ar(pulses, ffreq, 1/reso) * 0.2
}).play;
)

Ndef(\pulsy).end;

	// play a different Ndef on server \otto:  
(
Ndef(\dusty -> \otto, { |dens = 3, freq = 400, ringtime = 0.3, amp = 0.1| 
	Ringz.ar( 
		Dust.ar(dens.dup(3)).lag(0.001), 
		freq * [ 0.618, 1, 1.618], 
		ringtime /  [ 0.618, 1, 1.618]
	).sum * amp;
}).play
)

NdefGui(Ndef(\dusty -> \otto)); 

Ndef(\dusty -> \otto).end(2);

