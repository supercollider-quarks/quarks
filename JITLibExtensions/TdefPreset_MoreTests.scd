
(
x = Tdef(\x).set(\dt, 0.1, \freq, 440, \bend, 4, \sustain, 0.05);
e = TdefPreset(x);


x.addSpec(\dt, [0.03, 3, \exp]);
x.addSpec(\bend, [-24, 24, \lin]);
e.checkSpecsMissing;
// e.checkSpecsMissing(dialog: true);
x.addSpec(\sustain, [0.003, 3, \exp]);
e.proxy.envir.keys;


// make a ParamGui to read along
d = ParamGui(x.envir).moveTo(10, 10);
// and connect it to the proxy's halo:
d.useHalo(x);

Tdef(\x).controlKeys;

e.addSet(\ada);
e.proxy.envir.keys;
e.settings;
e.randSet;

e.proxy.envir.keys;
// attention, same name overwrites!
e.setRand(0.25); e.addSet(\bobo);
e.proxy.envir.keys;
e.setRand(0.25); e.addSet(\coco);
e.someRand(0.25, 0.5); e.addSet(\dad);
e.proxy.envir.keys.printcsAll;

e.settings.printcsAll;
e.setCurr(e.getSetNames.choose.postln);
e.setTarg(e.getSetNames.choose.postln);
e.proxy.envir;
e.getSetNames;
e.proxy.envir.keys;
)

(
// load a synthdef
s.boot;
SynthDef(\pingbend,
    { arg out=0, freq=440, bend = 4, sustain=0.05;
        var env = EnvGen.ar(Env.perc(0.001, sustain, 0.3), doneAction:2);
		freq = freq * XLine.kr(1, bend.midiratio, sustain);
        Out.ar(out, SinOsc.ar(freq, 0, env))
    }).add;

Tdef(\x, { |env|
    loop({
        (
			instrument: \pingbend,
			freq:  env[\freq],
			bend: env[\bend],
			sustain: env[\sustain],
			pan: 1.0.rand2
		).play;

		env[\dt].wait;
    })
});

Tdef(\x).play;
)

// a gui for the NdefPreset
g = TdefPresetGui(e, 8).moveTo(10, 200);

(    // or put it into a window
w = Window("NdefPreset test", Rect(200, 200, 400, 250)).front;
w.addFlowLayout;
g = TdefPresetGui(e, 4, parent: w);
)


e.morph(1.0.rand, \ada, \coco);

e.xfadeTo(\coco, 3, { "xfade doneFunc done.".postln; });
e.xfadeTo(e.getSetNames.choose.postln, exprand(0.3, 10));


(// do recursive crossfading
f = {
	"next xfade: ".post;
	e.xfadeTo(e.getSetNames.choose.postln, exprand(0.3, 10), f)
};

f.value;
)
f = nil; // stop



// add extra params - wont be used in Preset!
e.namesToStore.add(\bendVar);

Tdef(\x).set(\bendVar, 12);
Tdef(\x).addSpec(\bendVar, [0, 24]);

Tdef(\x).set(\bendVar, 12);
Tdef(\x).addSpec(\bendVar, [0, 24]);
Tdef(\x).set(\dens, 12);
Tdef(\x).addSpec(\dens, [1, 1000, \exp]);

// legalize this eventually
e.useHalo(Tdef(\x));

s.latency = 0.03;

Tdef(\x).set(\bendVar, 12);

Tdef(\x, { |env|
    loop({
        (
			instrument: \pingbend,
			freq:  env[\freq],
			bend: env[\bend] + env[\bendVar].rand2,
			sustain: env[\sustain],
			pan: 1.0.rand2
		).play;

		(1/env[\dens]).wait;
    })
});


ProxyPreset.new;

should know its object (proxy)
ask the proxy for its paramKeys
know which ones of them to actually store,
by asking the user for which keys to store,
--- (and maybe which to ignore),
or getting all keys from the proxy.

[ setting of whether to include new settings? ];

for morphing :
-- ask the proxy for specs for them
-- if missing, ask the user for missing specs

-- know how to handle bad values
    (symbols, arrays of different sizes etc)

-- maybe have special methods for morphing those?
	e.g. morphval = coin prob.
	or round / switch at 0.5
	of have some special smart func
	(e.g. array genetic crossing?)




a = Tdef(\x).play;

Tdef(\x).getSpec.printAll;

(
Tdef(\x).set(\dt, 0.1, \freq, 440, \bend, 4, \sustain, 0.05);
Tdef(\x, { |env|
    loop({
        (
			instrument: \pingbend,
			freq:  env[\freq],
			bend: env[\bend],
			sustain: env[\sustain],
			pan: 1.0.rand2
		).postln.play;

		env[\dt].postln.wait;
    })
})
)


(   // make a preset for Ndef(\a)
z = TdefPreset(Tdef(\x));
    // a gui for the NdefPreset
w = Window("NdefPreset test", Rect(200, 200, 420, 250)).front;
w.addFlowLayout;
g = ProxyPresetGui(z, parent: w);
    // and one for the Ndef
b = TdefGui(Tdef(\x), 8, parent: w, bounds: 380@100);

)

z.getFromProxy;

// add missing specs by dialog:
TdefPreset(\x).specsDialog(Tdef(\x).envir.keys, Tdef(\x).getSpec);

Tdef(\x).addSpec(\dt, [0.003, 3, \exp]);
Tdef(\x).addSpec(\bend, [-24, 24]);
Tdef(\x).addSpec(\bend, [-24, 24]);
Tdef(\x).addSpec(\sustain, [0.01, 10, \exp]);

NdefPreset.all;

(      // add 3 settings to the list
a.set(\freq, 1000, \dt, 0.1, \sustain, 0.15, \bend, 5);
z.addSet(\ada);
a.set(\freq, 1500, \dt, 0.3, \sustain, 0.25, \bend, -8);
z.addSet(\bebe);
a.set(\freq, 2500, \dt, 0.05, \sustain, 0.02, \bend, 13);
z.addSet(\coco);
)
    // these are all the settings now:
z.settings.printcsAll; "";

    // current interpolation value
z.morphVal;

z.getFromProxy.asKeyValuePairs.clump(2);

z.blendSets(0.5, z.settings[\ada], z.settings[\bebe]);
z.blend(0.5, \ada, \bebe);

(freq: 200).collect({ |v, k| k.asSpec.unmap(v) })

        Handling settings:

    //    postSettings as saveable code:
z.postSettings

    // add a setting
z.addSet(\blong, [\freq, 666]);
    // add a list of settings
z.addSettings([\blong -> [\freq, 666], \kling -> [\freq, 999]]);

    // remove  a setting
z.removeSet(\kling);
    // remove settings
z.removeSettings([\kling, \blong]);

    // dialog window for storing a setting by name
z.storeDialog
// dialo window for deleting settings
z.deleteDialog


/// currently active setting. A list of [[name, value], [name, value] ... ].
z.currSet;
z.setCurr(\ada);
z.currSet;

    //    targSet    a second setting used for interpolation.
z.targSet;
z.setTarg(\bebe);
z.targSet;

    // step thru presets, up or down by increment
z.stepCurr(1);
z.stepCurr(-1);
z.stepTarg(1);
z.stepTarg(-1);


    // Finding settings by name or index:
z.getSetNames;
z.getIndex(\coco);
z.getSet(\bebe);
z.currIndex;
z.targIndex;


    // Synching setting from/to the proxy

    // ask proxy for current setting
z.getFromProxy
    // set NdefPreset to current proxy setting
z.currFromProxy
    // set proxy to a named setting
z.setProxy(\coco);


        // Randomizing presets
(
// morphing and randomizing settings require specs for all params!
Spec.add(\dens, [1, 1000, \exp]);
Spec.add(\ring, [0.0001, 100, \exp]);
)

// randSet (rand, startSet, except)
Generate a more or less randomized setting based on a named setting
rand - By how much to randomize Default value is 0.25, a qurter of the spec range.
startSet - which set to vary. by default this is currSet.
except - keys to except from variation.

z.randSet(0.1, \ada, [\ring]);


// setRand (rand, startSet, except)
Randomize a setting by some amount, and make it current.
rand -  how much to vary (0.0 is no change, 1.0 is full range)
startSet - which set to start with
except - which keys not to vary

z.setRand(0.03);

// someRand (rand, ratio)
randomize some of the named values by some amount
rand - how much to vary the values (from 0.0 to 1.0)
ratio - which percentage (from 0.0 to 1.0) of the values to vary.

z.someRand(0.5, 0.5)


////////// Morphing between presets:

// morph (val, name1, name2, mapped)
// morphs between two named setting by a blend value, and sets proxy
blend    - blend value between 0 and 1 - 0.0 is all name1, 1.0 is all name2
name1     - the left setting
name2     - the right setting
mapped     - a flag whether to interpolate by mapping/unmapping thru specs.
        requires specs to be globally or locally present.

// creates that setting, and sets proxy
z.morph(0.0, \ada, \coco);
z.morph(0.1, \ada, \coco);
z.morph(0.3, \ada, \coco);
z.morph(0.5, \ada, \coco);
z.morph(1.0, \ada, \coco);
z.morph(0.5, \ada, \coco);

// xfadeTo (target, dur)
Short prose description of method.
target - Explanation of target. Default value is nil. Other information.
dur - Explanation of dur. Default value is nil. Other information.

// inline example
z.xfadeTo(\ada, 2);
z.xfadeTo(\coco, 10);
z.xfadeTo(\bebe, 2);


// blend (blend, name1, name2, mapped)    create a blend between settings name1 and name2
blend     - blend value between 0 and 1 - 0.0 is all name1, 1.0 is all name2
name1     - the left setting
name2     - the right setting
mapped     - a flag whether to interpolate by mapping/unmapping thru specs.
        requires specs to be globally or locally present.

// just creates that setting, does not set proxy
z.blend(0.5, \ada, \coco);