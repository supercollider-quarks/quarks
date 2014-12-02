/*	Sandrode - simulates Touch Sandrodes as used in CircuitBending
	requires the MultiTouchPad quark by Batuhan Bozkurt.
	Inspired by Peter Blasser's magical Fyrall, Fourses, et al.

Quarks.checkoutAll;

/// wait wait wait ...

Quarks.install("MultiTouchPad");

// download tongsengmod from here: 
https://github.com/downloads/batuhan/tongsengmod/tongsengmod.zip

put code/addToExtensions folder in:
~Library/Application Support/SuperCollider/Extensions/

// and put it in usr/bin: 
unixCmd("open /usr/bin");

	// then recompile SC3:
thisProcess.recompile;


	// OK, ready!



// tested with external Magic Trackpad - only sees the internal one, hm.

*/

(

Server.default = s = Server.internal;
s.boot;

GUI.cocoa;
q = q ? ();
q.sandNames = [\a, \b, \c, \d, \e];

	// make 5 sandrodes
Sandrode(\a, 0.25@0.50, 0.15);
Sandrode(\b, 0.40@0.25, 0.15);
Sandrode(\c, 0.60@0.25, 0.15);
Sandrode(\d, 0.75@0.50, 0.15);
Sandrode(\e, 0.50@0.75, 0.15);

	// make a MultiTouchPad - uses MultiTouchPad quark.
MultiTouchPad.gui;
w = MultiTouchPad.guiWin; 
u = w.view.children.first; 

StaticText(w, Rect( 0, 0, 400, 100))
	.stringColor_(Color.grey(0, 0.5)) 
	.string_(
"// shortcut keys for MultiTouchPad.guiWin:
	// m to maximise window
	// x to set window t0 normal size
	// space to start sandrodes, 
	// . to stop, 
	// d to redraw".postln);
	
w.view.keyDownAction = Sandrode.keydownFunc;


	// adapt drawing function of MultiTouchPad
u.drawFunc = Sandrode.mtpDrawFunc;

	// draw the sandrode areas
Sandrode.drawAll(w.view);

Sandrode.verbose = true;
Sandrode.verbose = false;


MultiTouchPad.start;

	// set the three MultiTouchPad actions:
MultiTouchPad.touchAction = {|curID, xys| 
	Sandrode.addMtpTouch(curID, xys);
//	[\touch, curID].postln;
};

MultiTouchPad.untouchAction = {|curID| 
	Sandrode.removeTouch(curID);
//	[\untouch, curID].postln;
};
	// this one does the most:
	// ccalculate how strong the auras influence each other;
	// based on that, set the modulation weights in the Ndef below:
	
MultiTouchPad.setAction = {|curID, xys| 
	Sandrode.addMtpTouch(curID, xys);
	Sandrode.auras.keysValuesDo { |key, aura|
		var vals = q.sandNames.collect { |k2| (aura[k2] ? 0); };
		Ndef(\nodes).setn(key, vals.round(0.0001));	
	};
};
)
	// and the sound: 
	// a network of modulating VarSaws.
	// controlled by aura weights ...
(
Spec.add(\octaves, [0, 9]);
Spec.add(\angle, [0, 1]);
Spec.add(\delaytime, [0, 0.1, \amp]);
Spec.add(\spiky, [0.1, 10, \exp]);
Spec.add(\hairy, [0.0, 2, \amp]);
Spec.add(\xtalk, [0.0, 1, \amp]);

Ndef(\nodes, { |widefreq=10, hairy = 0.618, angle = 0.4, octaves = 4, delaytime = 0.01, spiky = 1, xtalk = 0.03|
	var n = Sandrode.all.size; 

	var modIn = LocalIn.ar(n);

	var influxes = [
		\a.kr(0!n),
		\b.kr(0!n),
		\c.kr(0!n),
		\d.kr(0!n),
		\e.kr(0!n)
	];
	
	var activity = (influxes.sum.sum * 0.3).clip(0, 1);
	
	var influxes2 = influxes + xtalk.lag(0.5);
	var freqs = ([ 	
			(modIn * influxes2[0]).sum, 
			(modIn * influxes2[1]).sum, 
			(modIn * influxes2[2]).sum, 
			(modIn * influxes2[3]).sum, 
			(modIn * influxes2[4]).sum
		] * (octaves.lag(0.5) * 12 * hairy.lag(0.5))).midiratio * widefreq.lag(0.5)
		* (2 ** [-2, -1, 0, 1, 2].scramble);
	
	var freqs2 = DelayN.ar(freqs, 0.1, delaytime);
		// roll off angles and spikyness for high freqs:
	var angles = (angle * 2 - 1) ** 0.1 + 1 * 0.5;
		// not working: scale angles to smaller range when freq is high;
	 // freqs2.collect { |freq| freq.explin(100, 5000, 1, 0) * ((angle * 2 - 1) ** 0.33 * 0.5) + 0.5 };
	var spikyness = spiky ** freqs.explin(100, 5000, 1, 0.25);
		
		// the oscillators
	var oscs = VarSaw.ar(freqs2.clip(0, 20000), 0, angles) ** spikyness;
	
	// influxes.first.poll;
		// out to feedback
	LocalOut.ar(oscs);
		// mix to two chans
	Splay.ar(oscs) * activity.linexp(0, 1, 0.1, 1);
}).play;
NdefGui(Ndef(\nodes), 12);
)

	// test setting node weights
Ndef(\nodes).set([\a, \b, \c, \d, \e].choose, ({1.0.rand.squared}!5).round(0.001));


(
Ndef('nodes').set('octaves', 4.7227722772277, 'widefreq', 4.2369058214701, 'hairy', 2.0, 'spiky', 0.27267389657355, 'delaytime', 0.031849818645231, 'xtalk', 0.0, 'angle', 0.53960396039604);
)


	// general Sandrode tests

c = Sandrode(\a, 0.5 @ 0.5, 0.2);
c.dump;

w = Window.new.front;
c.drawOn(w.view);
c.sectArea(Rect(0.5, 0.5, 0.1, 0.1)); // 0.25
