
XiiMorpherFilter {

	var pointlists, colorsarray, <>filtertype, controls;
	var <>selectedmorpher;
	var <>selectedparam;
	var selcolorsarray;
	var nyquist;
	var paramsHiLo, envelopepoints;
	
	*new { arg filtertype; 
		^super.new.initXiiMorpherFilter( filtertype );
	}
	
	initXiiMorpherFilter { arg filtertype; 
		selectedmorpher = false;
		selectedparam = false;
		controls = [\freq, \rq];
		//selcolorsarray = [Color.red.alpha_(0.3), Color.green.alpha_(0.3)];
		nyquist = Server.default.sampleRate/2;
		paramsHiLo = [[0,1], [0,1], [0,1]]; // range slider scale params
		
		switch(filtertype)
		{\empty}			{ this.createEmpty }
		{\lpf}			{ this.createLPF }
		{\bpf}			{ this.createBPF }
		{\hpf}			{ this.createHPF }
		{\rlpf}			{ this.createRLPF }
		{\rhpf}			{ this.createRHPF }
		{\resonant}		{ this.createResonant }
		{\moogff}			{ this.createMoogFF }
		{\delay}			{ this.createDelay }
		{\freeverb}		{ this.createFreeverb }
		{\distortion}		{ this.createDistortion }
		{\chorus}			{ this.createChorus }
		{\bitcrusher}		{ this.createBitCrusher }
		{\pitchshifter}	{ this.createPitchShifter }
		{\panamp}			{ this.createPanAmp }
		{\cyberpunk}		{ this.createCyberpunk }
		{\magaboveFFT}	{ this.createMagAbove }
		{\brickwallFFT}	{ this.createBrickwall }
		{\rectcombFFT}	{ this.createRectComb }
		{\magsmearFFT}	{ this.createMagSmear }
		;
		
		//this.scalePointLists;
	}

	setPointlist {arg apointlist, index;
		pointlists[index] = apointlist;
	}
	
	getPointlist {arg index;
		^pointlists[index];
	}
	
	/*
	initSynth {arg inbus, outbus, dur;
		switch(filtertype)
		{\lpf}{ Synth(\xiiMorpherFilter ) }
		{\hpf}{ this.createHPF };
	}
	*/
	
	drawFunc {
		if(filtertype != \empty, {
			pointlists.do({arg pointlist, index;
	
				Pen.moveTo(pointlist[0]+Point(241,0));
	
				if(selectedmorpher, {
					Pen.strokeColor = colorsarray[index+1];
				}, {
					Pen.strokeColor = colorsarray[index+1].copy.alpha_(0.3);
				});
			
				Pen.use{
					if(selectedparam==index && selectedmorpher, {
						Pen.setShadow(1@1, 7, Color.black)
					}); 
					(pointlist.size-2).do({arg i;
						Pen.width = 1;
						Pen.line(pointlist[i]+Point(3,2), pointlist[i+1]+Point(3,2));
						Pen.stroke;
					});
				};
				// box
				if(selectedmorpher, {
					Pen.strokeColor = Color.black;
				}, {
					Pen.strokeColor = Color.black.alpha_(0.3);
				});
				(pointlist.size-2).do({arg i;
					Pen.width = 0.5;
					Pen.strokeRect( Rect(pointlist[i].x+0.5, pointlist[i].y+0.5, 4, 4) );
					Pen.stroke;
				});
			});
		});
	}
		
	getColorsControls {
		^[colorsarray, controls];
	}
	
	setParamsHiLo_{arg array;
		paramsHiLo = array;
		//controls.do({arg array, i; array[1] = array[1]*paramsHiLo[0]; array[2] = array[2]*paramsHiLo[1]; });
	}
	
	getParamsHiLo {
		^paramsHiLo;
	}
	
	scalePointLists { // this is the envelope sent to the Synth
		var spec, yloclists;
		yloclists = pointlists.collect({arg pointlist, i; pointlist.collect({arg point; point.y}) });
	//[\paramshilo, paramsHiLo, \controls, controls].postln;
	//[\yloclists, yloclists].postln;
		envelopepoints = yloclists.collect({arg yloclist, i; 
//			spec = [0.0001+(paramsHiLo[i][0]*controls[i][1]), controls[i][1]+(paramsHiLo[i][1]*controls[i][2]), controls[i][3]].asSpec;
			spec = [(controls[i][1]+(paramsHiLo[i][0]*controls[i][2])).round(0.001), (paramsHiLo[i][1]*controls[i][2]).round(0.001), controls[i][3]].asSpec;
			//[\spec, spec].postln;
			spec.map(yloclist.linlin(0, 478, 1, 0)) 
		});
		//envelopepoints.postcs;
		//^envelopepoints;
	}
	
	getEnvArrays {
		^envelopepoints;	
	}
	
	/*
azure3  [ 193, 205, 205 ]
grey99  [ 252, 252, 252 ]
LightCyan1  [ 224, 255, 255 ]
beige  [ 245, 245, 220 ]
BlanchedAlmond  [ 255, 235, 205 ]
LightCyan2  [ 209, 238, 238 ]
MistyRose2  [ 238, 213, 210 ]
khaki1  [ 255, 246, 143 ]
LightGoldenrod1  [ 255, 236, 139 ]
grey82  [ 209, 209, 209 ]
burlywood1  [ 255, 211, 155 ]
MistyRose3  [ 205, 183, 181 ]
wheat3  [ 205, 186, 150 ]
DarkSeaGreen3  [ 155, 205, 155 ]
SlateGray3  [ 159, 182, 205 ]
LightCyan3  [ 180, 205, 205 ]
LightBlue  [ 173, 216, 230 ]
thistle  [ 216, 191, 216 ]
wheat2  [ 238, 216, 174 ]
PaleTurquoise  [ 175, 238, 238 ]
DarkSeaGreen1  [ 193, 255, 193 ]
LightYellow2  [ 238, 238, 209 ]
honeydew2  [ 224, 238, 224 ]
azure2  [ 224, 238, 238 ]
grey93  [ 237, 237, 237 ]
honeydew1  [ 240, 255, 240 ]
grey67  [ 171, 171, 171 ]	
	*/
	
	createEmpty {
		//"Empty created".postln;
		filtertype = \empty;
		pointlists = Array.fill(3, {Array.fill(72, {arg i; Point((i*10), 241)}) });
		colorsarray = [Color.clear, Color.white, Color.white, Color.white];
		controls = [[\para, 0, nyquist, \exp], [\para, 0, nyquist, \exp], [\para, 0, nyquist, \exp]];
	}

	createLPF {
		//"LPF created".postln;
		filtertype = \lpf;
		pointlists = Array.fill(1, {Array.fill(72, {arg i; Point((i*10), 300)}) });
		colorsarray = [Color.new255(193, 205, 205), Color.red, Color.white, Color.white];
		controls = [[\freq, 100, nyquist, \exp]];
	}

	createBPF {
		//"BPF created".postln;
		filtertype = \bpf;
		pointlists = Array.fill(2, {|x| Array.fill(72, {|i| Point((i*10), [300, 70][x])}) });
		colorsarray = [Color.new255(252, 252, 252), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\freq, 100, nyquist, \exp], [\rq, 0.001, 0.999, \lin]];
	}

	createHPF {
		//"HPF created".postln;	
		filtertype = \hpf;
		pointlists = Array.fill(1, {Array.fill(72, {arg i; Point((i*10), 350)}) });
		colorsarray = [Color.new255(224, 255, 255), Color.red, Color.white, Color.white];
		controls = [[\freq, 100, nyquist, \exp]];
	}

	createRLPF {
		//"RLPF created".postln;
		filtertype = \rlpf;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [300, 60][x])}) });
		colorsarray = [Color.new255(245, 245, 220), Color.red, Color.new255(0, 120, 0), Color.white];
		controls = [[\freq, 100, nyquist, \exp], [\rq, 0.001, 0.999, \lin]];
	}
	
	createRHPF {
		//"RHPF created".postln;
		filtertype = \rhpf;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [300, 60][x])}) });
		colorsarray = [Color.new255(255, 235, 205), Color.red, Color.new255(0, 120, 0), Color.white];
		controls = [[\freq, 100, nyquist, \exp], [\rq, 0.001, 0.999, \lin]];
	}

	createResonant {
		//"RHPF created".postln;
		filtertype = \resonant;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [250, 50][x])}) });
		colorsarray = [Color.new255(209, 238, 238), Color.red, Color.new255(0, 120, 0), Color.white];
		controls = [[\freq, 100, nyquist, \exp], [\rq, 0.001, 0.999, \lin]];
	}

	createMoogFF {
		//"MoogFF created".postln;
		filtertype = \moogFF;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [310, 260][x])}) });
		colorsarray = [Color.new255(238, 213, 210), Color.red, Color.green, Color.white];
		controls = [[\freq, 100, nyquist, \exp], [\gain, 0.001, 3.999, \lin]];
	}

	createDelay {
		//"MoogFF created".postln;
		filtertype = \delay;
		pointlists = Array.fill(2, {Array.fill(72, {arg i; Point((i*10), 241)}) });
		colorsarray = [Color.new255(255, 246, 143), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\delay, 0.02, 2, \lin], [\feedback, 0, 1, \lin]];
	}
	
	createFreeverb {
		//"MoogFF created".postln;
		filtertype = \freeverb;
		pointlists = Array.fill(3, {|x| Array.fill(72, {arg i; Point((i*10), [300, 241, 160][x])}) });
		colorsarray = [Color.new255(255, 236, 139), Color.red, Color.new255(0, 100, 0), Color.yellow];
		controls = [[\mix, 0, 1, \lin], [\room, 0, 1, \lin], [\damp, 0, 1, \lin]];
	}

	createDistortion {
		//"MoogFF created".postln;
		filtertype = \distortion;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [380, 420][x])}) });
		colorsarray = [Color.new255(209, 209, 209), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\pregain, 0.01, 20, \lin], [\postgain, 0.01, 20, \lin]];
	}
	
	createChorus {
		//"chorus created".postln;
		filtertype = \chorus;
		pointlists = Array.fill(2, {Array.fill(72, {arg i; Point((i*10), 241)}) });
		colorsarray = [Color.new255(255, 211, 155), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\speed, 0.001, 0.5, \lin], [\depth, 0.0001, 0.1, \lin]]; // could add predelay and ph_diff
	}

	createBitCrusher {
		//"bitcrusher created".postln;
		filtertype = \bitcrusher;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [360, 400][x])}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\samplerate, 600, Server.default.sampleRate, \lin], [\bitsize, 2, 16, \lin]];
	}


	createPitchShifter {
		//"pitchshifter created".postln;
		filtertype = \pitchshifter;
		pointlists = [Array.fill(72, {arg i; Point((i*10), 354)}) , Array.fill(72, {arg i; Point((i*10), 478) }) ];
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\pitch, 0, 4, \lin], [\dispersion, 0, 1, \lin]];
	}

	createPanAmp {
		//"pitchshifter created".postln;
		filtertype = \panamp;
		pointlists = Array.fill(2, {Array.fill(72, {arg i; Point((i*10), 241)}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\pan, -1, 1, \lin], [\amp, 0, 2, \lin]];
	}

	createCyberpunk {
	//	"cyberpunk created".postln;
		filtertype = \cyberpunk;
		pointlists = Array.fill(2, {|x| Array.fill(72, {arg i; Point((i*10), [210, 370][x])}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\pitch, 1, 10, \lin], [\zpch, 1, 10, \lin]];
	}

	createMagAbove {
		//"magabove created".postln;
		filtertype = \magaboveFFT;
		pointlists = Array.fill(1, {Array.fill(72, {arg i; Point((i*10), 380)}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.white, Color.white];
		controls = [[\ceil, 0, 40, \lin]];
	}

	createBrickwall {
		//"brickwall created".postln;
		filtertype = \brickwallFFT;
		pointlists = Array.fill(1, {Array.fill(72, {arg i; Point((i*10), 241)}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.white];
		controls = [[\wipe, -1, 1, \lin]];
	}


	createRectComb {
		//"rectcomb created".postln;
		filtertype = \rectcombFFT;
		pointlists = Array.fill(3, {|x| Array.fill(72, {arg i; Point((i*10), [220, 260, 300][x])}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.blue];
		controls = [[\teeth, 30, 1, \lin], [\phase, 0, 1, \lin], [\width, 0, 1, \lin]];
	}

	createMagSmear {
		//"magsmear created".postln;
		filtertype = \magsmearFFT;
		pointlists = Array.fill(1, {Array.fill(72, {arg i; Point((i*10), 441)}) });
		colorsarray = [Color.new255(205, 183, 181), Color.red, Color.new255(0, 100, 0), Color.blue];
		controls = [[\bins, 0, 100, \lin]];
	}

}


/*




SynthDef(\xiilpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, LPF.ar(In.ar(inbus, 1), freqenv))
}).load(s);


SynthDef(\xiilpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, LPF.ar(In.ar(inbus, 2), freqenv))
}).load(s);


SynthDef(\xiihpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, HPF.ar(In.ar(inbus, 1), freqenv))
}).load(s);


SynthDef(\xiihpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, freqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, HPF.ar(In.ar(inbus, 2), freqenv))
}).load(s);


SynthDef(\xiibpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, BPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiibpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, BPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);


SynthDef(\xiirlpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RLPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiirlpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RLPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiirhpf_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RHPF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiirhpf_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, RHPF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiiresonant_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, Resonz.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiiresonant_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\rq]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, Resonz.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);


SynthDef(\xiimoogff_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\gain]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, MoogFF.ar(In.ar(inbus, 1), freqenv, rqenv))
}).load(s);


SynthDef(\xiimoogff_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, freqs, rqs, freqenv, rqenv;
	times = {71.reciprocal}!71;
	freqs = Control.names([\freq]).ir({1.0.rand}!72);
	rqs = Control.names([\gain]).ir( {0.5}!72);
	freqenv = EnvGen.ar(Env.new( freqs, times, 'sine' ), timeScale:dur);
	rqenv = EnvGen.ar(Env.new( rqs, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	Out.ar(outbus, MoogFF.ar(In.ar(inbus, 2), freqenv, rqenv))
}).load(s);



SynthDef(\xiidelay_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, delays, feedbacks, delayenv, feedbackenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	delays = Control.names([\delay]).ir({1.0.rand}!72);
	feedbacks = Control.names([\feedback]).ir( {0.5}!72);
	delayenv = EnvGen.ar(Env.new( delays, times, 'sine' ), timeScale:dur);
	feedbackenv = EnvGen.ar(Env.new( feedbacks, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
		Ê Êsig = In.ar(inbus, 1); 
		Ê Êfx = sig + LocalIn.ar(1); 
		Ê Êfx = DelayC.ar(fx, 2, delayenv); 
		Ê ÊLocalOut.ar(fx * feedbackenv); 
		Ê// ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
	Out.ar(outbus, fx!2);
}).load(s);



SynthDef(\xiidelay_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, delays, feedbacks, delayenv, feedbackenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	delays = Control.names([\delay]).ir({1.0.rand}!72);
	feedbacks = Control.names([\feedback]).ir( {0.5}!72);
	delayenv = EnvGen.ar(Env.new( delays, times, 'sine' ), timeScale:dur);
	feedbackenv = EnvGen.ar(Env.new( feedbacks, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
		Ê Êsig = In.ar(inbus, 2); 
		Ê Êfx = sig + LocalIn.ar(1); 
		Ê Êfx = DelayC.ar(fx, 2, delayenv); 
		Ê ÊLocalOut.ar(fx * feedbackenv); 
		Ê// ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiifreeverb_1x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, room, damp, mix, roomenv, dampenv, mixenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	room = Control.names([\room]).ir({1.0.rand}!72);
	damp = Control.names([\damp]).ir( {0.5}!72);
	mix = Control.names([\mix]).ir( {0.5}!72);
	roomenv = EnvGen.ar(Env.new( room, times, 'sine' ), timeScale:dur);
	dampenv = EnvGen.ar(Env.new( damp, times, 'sine' ), timeScale:dur);
	mixenv = EnvGen.ar(Env.new( mix, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	sig = In.ar(inbus, 1); 
	fx = FreeVerb.ar(sig, mixenv, roomenv, dampenv); 
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiifreeverb_2x2, { arg inbus=20, outbus=0, dur=1;
	var env, genv, times, room, damp, mix, roomenv, dampenv, mixenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	room = Control.names([\room]).ir({1.0.rand}!72);
	damp = Control.names([\damp]).ir( {0.5}!72);
	mix = Control.names([\mix]).ir( {0.5}!72);
	roomenv = EnvGen.ar(Env.new( room, times, 'sine' ), timeScale:dur);
	dampenv = EnvGen.ar(Env.new( damp, times, 'sine' ), timeScale:dur);
	mixenv = EnvGen.ar(Env.new( mix, times, 'sine' ), timeScale:dur);
	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	sig = In.ar(inbus, 2); 
	fx = FreeVerb.ar(sig, mixenv, roomenv, dampenv); 
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiidistortion_1x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, pregain, postgain, pregainenv, postgainenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	pregain = Control.names([\pregain]).ir({1.0.rand}!72);
	postgain = Control.names([\postgain]).ir( {0.5}!72);
	pregainenv = EnvGen.ar(Env.new( pregain, times, 'sine' ), timeScale:dur);
	postgainenv = EnvGen.ar(Env.new( postgain, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1); 
	sigtocomp = ((sig * pregainenv).distort * postgainenv).distort;
	fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiidistortion_2x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, pregain, postgain, pregainenv, postgainenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	pregain = Control.names([\pregain]).ir({1.0.rand}!72);
	postgain = Control.names([\postgain]).ir( {0.5}!72);
	pregainenv = EnvGen.ar(Env.new( pregain, times, 'sine' ), timeScale:dur);
	postgainenv = EnvGen.ar(Env.new( postgain, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
	sigtocomp = ((sig * pregainenv).distort * postgainenv).distort;
	fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
	Out.ar(outbus, fx);
}).load(s);



SynthDef(\xiibitcrusher_1x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, samplerate, bitsize, samplerateenv, bitsizeenv;
	var fx, sig, bitRedux, downsamp; 
	times = {71.reciprocal}!71;
	samplerate = Control.names([\samplerate]).ir({1.0.rand}!72);
	bitsize = Control.names([\bitsize]).ir( {0.5}!72);
	samplerateenv = EnvGen.ar(Env.new( samplerate, times, 'sine' ), timeScale:dur);
	bitsizeenv = EnvGen.ar(Env.new( bitsize, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1); 
	downsamp = Latch.ar(sig, Impulse.ar(samplerateenv*0.5));
	bitRedux = downsamp.round(0.5 ** bitsizeenv);
	Out.ar(outbus, bitRedux);
}).load(s);


SynthDef(\xiibitcrusher_2x2, { arg inbus=20, outbus=0, dur=1;
	var sigtocomp, genv, times, samplerate, bitsize, samplerateenv, bitsizeenv;
	var fx, sig, bitRedux, downsamp; 
	times = {71.reciprocal}!71;
	samplerate = Control.names([\samplerate]).ir({1.0.rand}!72);
	bitsize = Control.names([\bitsize]).ir( {0.5}!72);
	samplerateenv = EnvGen.ar(Env.new( samplerate, times, 'sine' ), timeScale:dur);
	bitsizeenv = EnvGen.ar(Env.new( bitsize, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
	downsamp = Latch.ar(sig, Impulse.ar(samplerateenv*0.5));
	bitRedux = downsamp.round(0.5 ** bitsizeenv);
	Out.ar(outbus, bitRedux);
}).load(s);




SynthDef(\xiipitchshifter_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, dispersion, pitchenv, dispersionenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	dispersion = Control.names([\dispersion]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	dispersionenv = EnvGen.ar(Env.new( dispersion, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
    fx = PitchShift.ar(sig, 0.1, pitchenv, dispersionenv, 0.0001);
	Out.ar(outbus, fx);
}).load(s);


SynthDef(\xiipitchshifter_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, dispersion, pitchenv, dispersionenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	dispersion = Control.names([\dispersion]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	dispersionenv = EnvGen.ar(Env.new( dispersion, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2); 
    fx = PitchShift.ar(sig, 0.1, pitchenv, dispersionenv, 0.0001);
	Out.ar(outbus, fx);
}).load(s);



SynthDef(\xiipanamp_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pan, amp, panenv, ampenv;
	var sig; 
	times = {71.reciprocal}!71;
	
	pan = Control.names([\pan]).ir({1.0.rand}!72);
	amp = Control.names([\amp]).ir( {0.5}!72);
	panenv = EnvGen.ar(Env.new( pan, times, 'sine' ), timeScale:dur);
	ampenv = EnvGen.ar(Env.new( amp, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = Pan2.ar(In.ar(inbus, 1) * ampenv, panenv); 
	Out.ar(outbus, sig);
}).load(s);



SynthDef(\xiipanamp_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pan, amp, panenv, ampenv;
	var sig; 
	times = {71.reciprocal}!71;
	
	pan = Control.names([\pan]).ir({1.0.rand}!72);
	amp = Control.names([\amp]).ir( {0.5}!72);
	panenv = EnvGen.ar(Env.new( pan, times, 'sine' ), timeScale:dur);
	ampenv = EnvGen.ar(Env.new( amp, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = Pan2.ar(In.ar(inbus, 2).sum * ampenv, panenv); 
	Out.ar(outbus, sig);
}).load(s);




SynthDef(\xiicyberpunk_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, zpch, pitchenv, zpchenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	zpch = Control.names([\zpch]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	zpchenv = EnvGen.ar(Env.new( zpch, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
    fx = Squiz.ar(sig, pitchenv, zpchenv, 0.1);
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiicyberpunk_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, pitch, zpch, pitchenv, zpchenv;
	var fx, sig; 
	times = {71.reciprocal}!71;
	
	pitch = Control.names([\pitch]).ir({1.0.rand}!72);
	zpch = Control.names([\zpch]).ir( {0.5}!72);
	pitchenv = EnvGen.ar(Env.new( pitch, times, 'sine' ), timeScale:dur);
	zpchenv = EnvGen.ar(Env.new( zpch, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
    fx = Squiz.ar(sig, pitchenv, zpchenv, 0.1);
	Out.ar(outbus, fx);
}).load(s);




SynthDef(\xiimagaboveFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, ceil, ceilenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	ceil = Control.names([\ceil]).ir({1.0.rand}!72);
	ceilenv = EnvGen.ar(Env.new( ceil, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_MagAbove(chain, ceilenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);


SynthDef(\xiimagaboveFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, ceil, ceilenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	ceil = Control.names([\ceil]).ir({1.0.rand}!72);
	ceilenv = EnvGen.ar(Env.new( ceil, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_MagAbove(chain, ceilenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiibrickwallFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, wipe, wipeenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	wipe = Control.names([\wipe]).ir({1.0.rand}!72);
	wipeenv = EnvGen.ar(Env.new( wipe, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_BrickWall(chain, wipeenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);


SynthDef(\xiibrickwallFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, wipe, wipeenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	wipe = Control.names([\wipe]).ir({1.0.rand}!72);
	wipeenv = EnvGen.ar(Env.new( wipe, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_BrickWall(chain, wipeenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);




SynthDef(\xiirectcombFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, teeth, phase, width, teethenv, phaseenv, widthenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	teeth = Control.names([\teeth]).ir({1.0.rand}!72);
	teethenv = EnvGen.ar(Env.new( teeth, times, 'sine' ), timeScale:dur);
	phase = Control.names([\phase]).ir({1.0.rand}!72);
	phaseenv = EnvGen.ar(Env.new( phase, times, 'sine' ), timeScale:dur);
	width = Control.names([\width]).ir({1.0.rand}!72);
	widthenv = EnvGen.ar(Env.new( width, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_RectComb(chain, teethenv, phaseenv, widthenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiirectcombFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, teeth, phase, width, teethenv, phaseenv, widthenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	teeth = Control.names([\teeth]).ir({1.0.rand}!72);
	teethenv = EnvGen.ar(Env.new( teeth, times, 'sine' ), timeScale:dur);
	phase = Control.names([\phase]).ir({1.0.rand}!72);
	phaseenv = EnvGen.ar(Env.new( phase, times, 'sine' ), timeScale:dur);
	width = Control.names([\width]).ir({1.0.rand}!72);
	widthenv = EnvGen.ar(Env.new( width, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_RectComb(chain, teethenv, phaseenv, widthenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);




SynthDef(\xiimagsmearFFT_1x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, bins, binsenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	bins = Control.names([\bins]).ir({1.0.rand}!72);
	binsenv = EnvGen.ar(Env.new( bins, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 1);
	chain = FFT(LocalBuf(2048, 1), sig);
	fx = PV_MagSmear(chain, binsenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);



SynthDef(\xiimagsmearFFT_2x2, { arg inbus=20, outbus=0, dur=1;
	var genv, times, bins, binsenv;
	var chain, fx, sig; 
	times = {71.reciprocal}!71;
	
	bins = Control.names([\bins]).ir({1.0.rand}!72);
	binsenv = EnvGen.ar(Env.new( bins, times, 'sine' ), timeScale:dur);

	genv = EnvGen.ar(Env.linen(0.1, dur-0.2, 0.1, 1), doneAction:2);
	
	sig = In.ar(inbus, 2);
	chain = FFT(LocalBuf(2048, 2), sig);
	fx = PV_MagSmear(chain, binsenv);
	Out.ar(outbus, IFFT(fx));
}).load(s);






*/


