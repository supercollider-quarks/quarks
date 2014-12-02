PrePostView - show a proxys amplitude (pre- and post fader) 
	by colored rangesliders over a volSlider

See mainly ProxyMeter;

z = EZSlider(label: \vol, controlSpec: \amp);
y = PrePostView(z.sliderView.parent, volSlider: z.sliderView)
y.setAmps(exprand(0.1, 1.0), exprand(0.01, 0.4));


Ndef(\x, { SinOsc.ar(0.13).abs * BrownNoise.ar });
x = NdefGui(Ndef(\x));
y = PrePostView.forNdefGui(x);
y.setAmps(exprand(0.1, 1.0), exprand(0.01, 0.4));

ProxyMeter.checkKeep([]);
ProxyMeter.checkAdd(Ndef(\x), { |name, vols| y.setAmps(*vols); })
ProxyMeter.skipjack.stop.start;

Ndef(\x, { SinOsc.ar(0.13).abs * BrownNoise.ar });

m = NdefMixer(s);
Ndef(\a).ar;
PrePostView.forMixer(m);

Ndef('pm_x');

ProxyMeter.updateFuncs['pm_x'].value('pm_x', 0.5, 0.9);

NdefMixer(s);


z = EZSlider(label: \vol, controlSpec: \amp);
PrePostView(z.sliderView.parent, volSlider: z.sliderView)

Ndef(\x, { SinOsc.ar(0.13).abs * BrownNoise.ar });
x = NdefGui(Ndef(\x));
ProxyMeter.init;
ProxyMeter.showOnMonitor(x.monitorGui);
ProxyMeter.updateFuncs;
ProxyMeter.arProxies;
ProxyMeter.ampProxies;

	// hm, switching between them does not work yet.
x.object = nil;
x.object = Ndef(\x);
x.object = Ndef(\xyz);

PrePostView.forNdefGui(x);
m = NdefMixer(s);
Ndef(\a).ar;
PrePostView.forMixer(m);

*/
