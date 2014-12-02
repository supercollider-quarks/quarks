
// 	Option 3 - same infrastructure everywhere, 
	change settings individually or globally 
	
	* e.g. have the same Ndef run on all machines, 
	send set commands to it ("bing");


// N-Tandems

	// prepare listening to tandem-related messages:
(
	o = OSCresponder(nil, '/setndef', { |t, resp, msg|
		var ndefname, pairs;
		ndefname = msg[1];
		pairs = msg.drop(2);
		Ndef(ndefname).set(*pairs);
	}).add;
)

// make a tandem-controlled sound (e.g. traffic lights)
(
	Ndef(\sound, { |x = 0.5, y = 0, z = 0|
	SinOsc.ar([240, 439] + (x * 1000) * 2)
	* 
	LFPulse.kr(y.linexp(0, 1, 0.1, 1000), 0, z.range(0.1, 0.9))
	}).play
);

Ndef(\sound).fadeTime = 10; 

q = q ? ();
q.myGroup = r.nameList.scramble.keep(4);

	// send tandem messages to all, some 
r.send(\all, '/setndef', \sound, \x, 0.2.rand + 0.8);

r.send(\otto, '/setndef', \sound, \x, 0.3, \y, 0.4, \z, 0.7);

r.send(\abc, '/setndef', \sound, \x, 1.0.rand, \y, 1.0.rand, \z, 1.0.rand);

r.send(q.myGroup, '/setndef', \sound, \x, 1.0.rand, \y, 1.0.rand, \z, 1.0.rand);


//////////////////////////////////////////////////////////////////////////
//////////////////////////////////////// Bing. 

	// change sound: 
(
Ndef(\sound, { |freq=300, x = 0.5, y = 0, z = 0|
	var trig = T2A.ar((HPZ2.kr([x, y, z]) > 0).sum);
	freq = freq * (1 + [0, x, y, z]);
	Splay.ar(
		Ringz.ar(trig, freq, 1.3)
	);
}).play
);

NdefGui(Ndef(\sound));




//////////////////// Example 2 - Ndefs that play samples ////////////////////////


// (4) sample player

q = q ? ();
// first time: make a buffer.
q.buffer1 = Buffer.readChannel(s, "/Volumes/data/archiv/ton/long_mellowsong.aiff", channels: [0]); // <-- your filepath here

// change sample
q.buffer1.allocReadChannel("/Volumes/data/archiv/ton/more_mellow.aiff", channels: [0]);// <-- filepath path here

(
Ndef(\sample, { |rate = 1, trate = 8, pos = 0.5, orate = 1, freq = 2500, reso = 10|
	var lfo = SinOsc.kr(orate).range(0, 1);
	var son = TGrains.ar(2, 
		Impulse.ar(trate), 
		q.buffer1,
		rate,
		BufDur.kr(q.buffer1) * lfo * pos,
		2 / trate
	) * 80;
	BPF.ar(son * reso.sqrt, freq.lag(0.2), 1/reso) ;
	//son
}).play
)


(
Ndef(\sample, { |rate = 1, trate = 8, pos = 0.5, orate = 1, freq = 500|
		var lfo = LFNoise1.kr(orate).range(0, 1);
		var trig = Impulse.ar(trate * (lfo + 1)) + Dust.ar(trate * 0.2);
		var son = TGrains.ar(2, 
			trig, 
			q.buffer1,
			rate,
			BufDur.kr(q.buffer1) * pos * (lfo + 1 * 0.3),
			0.5 / trate
		);
		Ringz.ar(son * 180, freq.lag(0.2), 0.0005) * LFPulse.kr(orate.lag(1) * 1.4, 0, 0.2);
	}).play
);

Ndef(\sample).fadeTime = 1;

r.send(\all, '/setndef', \sample, \rate, -1.2, \trate, 3, \orate, 1);
r.send(\all, '/setndef', \sample, \freq, 510);

r.send(\all, '/setndef', \sample, \freq, 190);
r.send(\all, '/setndef', \sample, \orate, 0.1);
r.send(\all, '/setndef', \sample, \trate, 8);
r.send(\all, '/setndef', \sample, \pos, 1);
r.send(\all, '/setndef', \sample, \rate, -3);

(
Tdef(\x, {
	5.do {
		r.send(\all, '/setndef', \sample, \rate, 2, \freq, 1170 + 10.0.rand2, \orate, 0.2, \pos, 1);
		0.1.wait;
	}
}).play;
)

