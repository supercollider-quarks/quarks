// make an Influx with KtlLoop for gesture recording.

(
q = q ? ();

a = Influx([\x, \y], [\a, \b, \c,\d, \e, \f, \g, \h], (x: 0.5, y: 0.1));
q.iogui = InfluxIOWGui(a).moveTo(411, Window.screenBounds.height );

// Make a KtlLoop. Its function is made by hand here for full detail
(
k = KtlLoop(\x, { |ev|
	if (ev.type == \set) {
			// filter just the params we are interested in
		ev = ev.select{ |val, key| k.paramsToRecord.includes(key) };
			// rescale if rescale is on
		ev = k.rescaleEvent(ev);
			// map to the objects out specs
		ev = ev.collect{ |val, key| k.object.getSpec(key).map(val) };
			// debug info?
	//	ev.round(0.001).postln;
			// then set the object to the new values!
		k.object.set(*ev.asKeyValuePairs);
	};
});
// which params to record (might be more coming from the source)
k.paramsToRecord_([\x, \y]);
// which in params go to which out params
k.ktlMap_((x: \x, y: y));
// which object gets set to the out param values
// here, the Influx a
k.object = a;
// add outval specs to Influx, so it knows how to rescale
a.addSpec(\x, \pan);
a.addSpec(\y, \pan);

// make a gui for KtlLoop
KtlLoopGui(k, bounds: 320@180).moveTo(5, 320);
);


// make an xy controller for Influx
// its action records into KtlLoop
// and sets its output object
(
q.win = Window("Influx(\x) control", Rect(5, 540, 320, 210)).front;
q.win.addFlowLayout;

q.comp1 = CompositeView(q.win, q.win.bounds.width_(120));
q.comp1.addFlowLayout;
StaticText(q.comp1, Rect(0, 0, 110, 20))
     .align_(\center).string_("Change  weights:");
[
	"set to diagonal", { a.setwPre(\diagL); },
	"disentangle", { a.disentangle(0.3); },
	"entangle", { a.entangle(0.3); },
	"RANDOM", { a.rand(1.0); }
].pairsDo { |label, action|
	Button(q.comp1, Rect(0, 0, 110, 20)).states_([[label]]).action_(action);
};

// w.view.decorator.nextLine.shift(0, 5);
StaticText(q.comp1, Rect(0, 0, 110, 20))
     .align_(\center).string_("Attach to:");

[\dog, \cat].collect { |name, i|
	Button(q.comp1, Rect(0, 0, 110, 20))
	.states_([[name, Color.black], [name, Color.black, Color.green]])
	.action_({ |btn|
		[ { "remove ".post; a.removeMapped(name.postcs);  },
			{ "attach ".post; a.attachMapped( Ndef(name).postcs)  }
		][btn.value].value;
	});
};


q.comp2 = CompositeView(q.win, q.win.bounds.width_(180));
q.comp2.addFlowLayout;

StaticText(q.comp2, Rect(200, 0, 160, 20))
    .align_(\center).string_("Change inputs:");

q.sl2d1 = Slider2D(q.comp2,  Rect(200, 30, 170, 170)).x_(0.5).y_(0.5)
.background_( Color.new255(200, 100, 0) )
.action_({|sl|
	// recording into KtlLoop here
	k.recordEvent((type: \set, x: sl.x, y: sl.y));
	// and this is the normal set function
	// bipolar mapping here done by hand
	a.set(\x, sl.x * 2 - 1, \y, sl.y * 2 - 1);
});

SkipJack({ q.sl2d1.setXY(*[a.inValDict[\x], a.inValDict[\y]] + 1 / 2)  },
	0.05, { q.win.isClosed }, 'sl2d');
);

// make a so-so sound with too many parameters
(
Ndef(\dog).addSpec(\intv, [-24, 24]);
Ndef(\dog).addSpec(\timescale, [0.01, 10, \exp]);
Ndef(\dog).addSpec(\curve, [-10, 10]);
Ndef(\dog).addSpec(\ffreq, \freq);

Ndef(\dog).addSpec(\loopnode, [0, 12]);
Ndef(\dog).addSpec(\dens, [10, 1000]);
Ndef(\dog).addSpec(\resfreq, \freq);
Ndef(\dog).addSpec(\ringtime, [0.01, 1, \exp]);

Ndef(\dog, { |freq=250, intv=19, timescale=1, curve=0, loopnode=0, dens = 10, resfreq = 500, ringtime = 0.01|
    var n = 15;
    var env = EnvGen.kr(
        Env({ 1.0.rand2 }!(n+1), {1.0.rand}!n, curve, releaseNode: n-1, loopNode: loopnode.min(n-3)),
        timeScale: timescale);
    var pitch = (env * [1, 0.33, -1] * intv).midiratio * freq;
    var form = Formant.ar(pitch, pitch.rotate(1) * 1.6, pitch.rotate(2) * 2.6);

    var dusty = Ringz.ar(Dust.ar(dens!3), resfreq * LFNoise0.kr([3, 5, 7], 0.5, 1), ringtime) * ((dens ** -0.5) * (ringtime ** -0.3)) ;
    Splay.ar(form * dusty);

});
Ndef(\dog).play(vol: 0.5);
z = NdefPreset( Ndef(\dog) ).storeToDisk_(true);
try{z.loadSettings};
NdefPresetGui(NdefPreset(\dog), 9).moveTo(697, 496);

// connect the influx outputs to the Ndefs params
a.attachMapped( Ndef(\dog));
);


/* ***** NOW Try this *******
TdefGui(Tdef(\a), bounds: 260@20)
TdefGui(Tdef(\a));

play on the XY controller, hear how the sound changes;
change the weights, note how the sounds in the corners change

hit record on the KtlLoopGui,
record controller moves
stop record, hit play, hit loop

play with loop params:
  tempo,

change weights while loop plays:
   gesture stays the same, sound variety changes
****** NOW Try this ******
  */

// the DubCat example from our Class:

// controlspecs
Spec.add(\pFreq, [20, 400, \exp]);
Spec.add(\pWidth, [0.01, 0.5, \exp]);
Spec.add(\filt, [1, 32, \exp]);
Spec.add(\q, [1, 1000, \exp]);
Spec.add(\modFreq, [0, 200, 7]);
Spec.add(\moDepth, [0, 12*8]);

// a Synth with 6 control parameters
Ndef(\cat, { |pFreq=100, pWidth=0.5, filt=2, q=1, modFreq=0, moDepth=12|
	var signal;
	pFreq = pFreq * SinOsc.ar(modFreq, 0, moDepth).midiratio;
	signal = RLPF.ar(
		LFPulse.ar(
			pFreq * [1, 1.01],
			0,
			pWidth
		),
		(pFreq * filt).clip(50, 15000),
		q.reciprocal
	)
	* AmpComp.kr(pFreq.lag(1));
	Limiter.ar(signal);
});

Ndef(\cat).play(vol: 0.2);	// play it loud
z = NdefPreset( Ndef(\cat) ).storeToDisk_(true);
try{z.loadSettings};
NdefPresetGui(NdefPreset(\cat), 9).moveTo(697, 220);

// a.attachMapped( Ndef(\cat));
);
/*
a.action
z.writeSettings( thisProcess.nowExecutingPath.dirname ++ "/cat.pxPreset")
z.storeToDisk = true;

*/