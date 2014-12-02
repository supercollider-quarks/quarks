ProxyMeter 	easy metering of audio proxy levels

ProxyMeter measures the peak levels of proxies (e.g. those showing on a proxymixer), 
determines pre- and post-slider volume, and can displays these two levels on a mixer, 
or generally, on a MonitorGui.


		Written by adc, based on suggestions of / discussions with Ricardo Guerreiro. 
		Reuses ideas and code from AudioMeter by A. Bartetzki.


ToDo: 	* test some more!

	// prepare:
(
Server.default = s = Server.local;
s.makeWindow;
s.boot;
q = q ? ();
p = p ?? { ProxySpace.new }; p.push;
m = ProxyMixer(p, 8);
)
	// make some proxies
(
~a = { Ringz.ar(Dust.ar([1, 1]), [600, 800], 0.05) };
~b = { Dust.ar([20, 80, 320]) * 0.5 };
~y = { SinOsc.ar(0.2).abs * PinkNoise.ar };
~x = { SinOsc.ar(0.13).abs * BrownNoise.ar };

~x.play(vol: 0.2);
~y.play(vol: 0.1);
)
	// then meter the mixer
ProxyMeter.addMixer(m);

	// see all the meters
ProxyMeter.all;

	// show the amp metering proxies as krs in their proxyspace
ProxyMeter.showKrs;
	// or not
ProxyMeter.hideKrs;

	// all the view currently in use
ProxyMeter.prePostViews;
	// and all the registered guis
ProxyMeter.proxyGuis;

	// add some more
(
~c = { Ringz.ar(Dust.ar([1, 1]), [600, 800], 0.05) };
~d = { Dust.ar([20, 80, 320]) * 0.5 };
~e = { SinOsc.ar(0.2).abs * PinkNoise.ar };
~f = { SinOsc.ar(0.13).abs * BrownNoise.ar };

~g.play(vol: 0.2);
~h.play(vol: 0.1);
)

	// check that editGui also works
m.switchSize(2);
m.editGui.object_(~a);

~g = { LFDNoise3.ar(3).clip(0, 1) * GrayNoise.ar };

	// remove the mixer individually
ProxyMeter.removeMixer(m);


	// cleanup:
	// add mixer again for the test
ProxyMeter.addMixer(m);		

	// closing the mixer should also remove every unneeded meter
	// posts a few removes too many.
m.close;		
p.clear;		

	// should be empty now
ProxyMeter.all;
Ndef.all;	// and no meter proxies here either.



	// similar test with Ndefs and NdefMixer:
(
Ndef(\a, { Ringz.ar(Dust.ar([20]), 300, 0.04) });
Ndef(\b, { Dust.ar([20, 80, 320]) });
Ndef(\y, { SinOsc.ar(0.2).abs * PinkNoise.ar });
Ndef(\x, { SinOsc.ar(0.13).abs * BrownNoise.ar });
)

Ndef(\a).play(vol: 0.25);
Ndef(\b).play(vol: 0.1);
Ndef(\y).play(vol: 0.5);
Ndef(\x).play(vol: 0.05);


n = NdefMixer(s, 10); ProxyMeter.addMixer(n);

n.close;


	// test mapping to a single NdefGui, 
	// and switching objects for them - seems to work fine.
	
(
x = NdefGui(Ndef(\x));
ProxyMeter.addNdefGui(x);
)

Ndef(\x, { SinOsc.ar(0.13).abs * BrownNoise.ar * 0.1 });
Ndef(\xyz, { SinOsc.ar(1).max(0) * BrownNoise.ar });

x.object = nil;
x.object = (Ndef(\xyz));
x.object = (Ndef(\x));

Ndef(\xyz, { SinOsc.ar(1).max(0) * GrayNoise.ar });

