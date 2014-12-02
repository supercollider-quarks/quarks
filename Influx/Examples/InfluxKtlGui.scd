ToDo:
- unibi, biuni - add proper help file
- Influx - named versions?
Idef, IMdef

Idef(\x)

// InfluxKtlGui sketch tests

a = Influx([\x, \y, \z], [\a, \b, \c, \d]);
g = InfluxKtlGui();
g.object_(a);
g.xyMapDict.put(\x, \z);

// set a button by hand:
g.setButton(2, \otto, { |bt, modif| if (bt.value > 0) { "otto".speak } { "off".speak } });


Ndef(\four, { |a, b, c, d| SinOsc.ar( 440 * (2 ** [a, b, c, d])).sum * 0.1 });
Ndef(\four).play;


// test attachDirect
g.object.attachDirect(Ndef(\four), \four);

// test attachMapped
Ndef(\four).addSpec(\a, [0, 2]);
Ndef(\four).addSpec(\b, [0, 2]);
Ndef(\four).addSpec(\c, [0, 2]);
Ndef(\four).addSpec(\d, [0, 2]);

// same name overwrites previous func
g.object.attachMapped(Ndef(\four), \four);

// setButton by hand for proxy attaching
g.setButton(0, \four, { |bt, modif|
	if (bt.value > 0) {
		g.object.attachMapped(Ndef(\four));
		Ndef(\four).play;
	} {
		if (modif.isAlt) { Ndef(\four).stop; };
		g.object.detach(\four);
	};
});

g.attachToButton(0, Ndef(\four));

Ndef(\four).gui;
