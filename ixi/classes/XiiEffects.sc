XiiDelay {	
	var <>xiigui; // to get and set settings
	var <>win; // to get access to the GUI
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiDelay(server, channels, setting);
		}
		
	initXiiDelay {arg server, channels, setting;

		var delayTimeSpec, delayTailSpec, params, s; 
		
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiDelay1x1, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
							
		   var fx, sig; 
		   sig = InFeedback.ar(inbus,1); 
		   fx = sig + LocalIn.ar(1); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 
		
		// stereo
		SynthDef(\xiiDelay2x2, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
							
		   var fx, sig; 
		   sig = InFeedback.ar(inbus,2); 
		   fx = sig + LocalIn.ar(2); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
   		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 

		delayTailSpec = ControlSpec.new(0.01, 2, \exponential, 0, 1.2); 
		
		params = [ 
		   ["Delay", "Feedback", "Fx level", "Dry Level"], 
		   [ \delay, \feedback, \fxlevel, \level], 
		   [delayTailSpec, \amp, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[0.4, 0.4, 0.8, 1]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- delay 2x2 -", \xiiDelay2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("- delay 1x1 -", \xiiDelay1x1, params, channels, this, setting);
		});
		win = xiigui.win;
	}
	
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }
}


XiiFreeverb {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initFreeverb(server, channels, setting);
		}
		
	initFreeverb {arg server, channels, setting;
	
		var mixSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiFreeverb1x1, {| inbus=0, outbus=0, mix=0.25, room=0.15, damp=0.5, fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 1); 
		   fx = FreeVerb.ar(sig, mix, room, damp); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 

		// stereo
		SynthDef(\xiiFreeverb2x2, {| inbus=0, outbus=0, mix=0.25, room=0.15, damp=0.5, fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 2); 
		   fx = FreeVerb.ar(sig, mix, room, damp); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		mixSpec = ControlSpec.new(0, 1, \linear, 0.01, 0.75); 
		
		params = [ 
		   ["Room", "Damp", "Dry/Wet", "Fx Level", "Dry Level"], 
		   [\room, \damp, \mix, \fxlevel, \level], 
		   [\amp, \amp, mixSpec, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[0.8, 0.8, 0.75, 0.8, 0.2 ]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- freeverb 2x2 -", \xiiFreeverb2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- freeverb 1x1 -", \xiiFreeverb1x1, params, channels, this, setting); 
			});
		win = xiigui.win;

	}
	
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}


XiiAdCVerb {	

	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initAdCVerb(server, channels, setting);
		}
		
	initAdCVerb {arg server, channels, setting;
	
		var roomSpec, mixSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiAdcverb1x1, {| inbus=0, outbus=0, revtime=3, hfdamping=0.5, mix=0.1, level=0 | 
		   	var fx, fxIn, sig; 
		   	sig = InFeedback.ar(inbus, 1); 
			fxIn = LeakDC.ar(sig) * mix;
		    	fx = XiiAdCreVerb.ar(fxIn, revtime, hfdamping, nOuts: 1);

			Out.ar(outbus, LeakDC.ar(fx + (sig * level))) // Leak DC to fix Karplus-Strong problem
		},[0.1,0.1,0.1,0.1, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiAdcverb2x2, {| inbus=0, outbus=0, revtime=3, hfdamping=0.5, mix=0.1, level=0 | 
		   	var fx, fxIn, sig; 
		   	sig = InFeedback.ar(inbus, 2); 
			fxIn = LeakDC.ar(sig.sum) * mix; // make a mono in, leakdc it
		    	fx = XiiAdCreVerb.ar(fxIn, revtime, hfdamping, nOuts: 2);
			Out.ar(outbus, LeakDC.ar((sig * level) + fx)) // level
		},[0,0,0,0]).load(s); // having a lag here would result in big blow of noise at start
				
		roomSpec = ControlSpec.new(0.1, 10, \exponential, 0.01, 3); 
		mixSpec = ControlSpec.new(0, 1, \amp, 0.01, 0.1); 
		
		params = [ 
		   ["RevTime", "Damp", "Fx level", "Dry Level"], 
		   [\revtime, \hfdamping, \mix, \level], 
		   [roomSpec, \amp, mixSpec, \amp], 
		   if(setting.notNil, {setting[5]}, {[8, 0.7, 0.1, 1 ]})
		]; 
		
		xiigui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("- adcverb 2x2 -", \xiiAdcverb2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- adcverb 1x1 -", \xiiAdcverb1x1, params, channels, this, setting); 
		});
		win = xiigui.win;
	}
	
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }
	
}

XiiDistortion {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initDistortion(server,channels, setting);
		}
		
	initDistortion {arg server, channels, setting;
		var s, params, preGainSpec, postGainSpec; 
		s = server ? Server.default;

		// mono
		SynthDef(\xiiDistortion1x1, {| inbus=0, outbus=0, pregain=0.048, postgain=15, mix = 0.5, level=0 | 
			var sig, sigtocomp, fx, y, z;
			sig = InFeedback.ar(inbus, 1);
			sigtocomp = ((sig * pregain).distort * postgain).distort;
			fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
			Out.ar(outbus, LeakDC.ar((fx * mix) + (sig *level)) );
		},[0, 0, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiDistortion2x2, {| inbus=0, outbus=0, pregain=0.048, postgain=15, mix = 0.5, level=0 | 
			var sig, sigtocomp, fx, y, z;
			sig = InFeedback.ar(inbus, 2);
			sigtocomp = ((sig * pregain).distort * postgain).distort;
			fx = Compander.ar(sigtocomp, sigtocomp, 1, 0, 1 );
			Out.ar(outbus, LeakDC.ar((fx * mix) + (sig *level)) );
		},[0, 0, 0.1]).load(s); 

		preGainSpec = ControlSpec.new(0.01, 20, \linear, 0, 1); 
		postGainSpec = ControlSpec.new(0.01, 20, \linear, 0, 1); 
		
		params = [ 
		   ["PreGain", "PostGain", "Fx Level", "Dry Level"], 
		   [\pregain, \postgain, \mix, \level], 
		   [preGainSpec, postGainSpec, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[10, 10, 0.5, 0]})
		]; 
		
		xiigui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("- distortion 2x2 -", \xiiDistortion2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- distortion 1x1 -", \xiiDistortion1x1, params, channels, this, setting); 
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}

XiiixiReverb {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initixiReverb(server, channels, setting);
		}
		
	initixiReverb {arg server, channels, setting;
		
		var s, predelSpec, combDecSpec, allpassDecSpec, params; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiReverb1x1, {| inbus=0,outbus=0,predelay=0.048,combdecay=15,allpassdecay=1,fxlevel=0.31,level=0 | 
			var sig, y, z;
			sig = InFeedback.ar(inbus, 1); 
			// predelay
			z = DelayN.ar(sig, 0.1, predelay);
			// 7 length modulated comb delays in parallel :
			y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) })); 
		
			6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
			Out.ar(outbus, (sig * level) + (y * (fxlevel*0.5))); // as fxlevel is 1 then I lower the vol a bit
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiReverb2x2, {| inbus=0,outbus=0,predelay=0.048,combdecay=15,allpassdecay=1, fxlevel=0.31, level=0 | 
			var sig, y, z;
			sig = InFeedback.ar(inbus, 2); 
			// predelay
			z = DelayN.ar(sig, 0.1, predelay);
			// 7 length modulated comb delays in parallel :
			y = Mix.ar(Array.fill(7,{ CombL.ar(z, 0.05, rrand(0.03, 0.05), combdecay) })); 
		
			6.do({ y = AllpassN.ar(y, 0.050, rrand(0.03, 0.05), allpassdecay) });
			Out.ar(outbus, (sig*level) + (y * (fxlevel*0.5))); // as fxlevel is 1 then I lower the vol a bit
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		
		predelSpec = ControlSpec.new(0.01, 0.1, \linear, 0, 0.045); 
		combDecSpec = ControlSpec.new(0.1, 15, \linear, 0, 15); 
		allpassDecSpec = ControlSpec.new(0.01, 5, \linear, 0, 1); 
		
		params = [ 
		   ["Predelay", "Combdecay", "Allpass", "Fx level", "Dry Level"], 
		   [\predelay, \combdecay, \allpassdecay, \fxlevel, \level], 
		   [predelSpec, combDecSpec, allpassDecSpec, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[0.045, 15, 1, 0.31, 0.5]})
		]; 
		
		xiigui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("- ixireverb 2x2 -", \xiiReverb2x2, params, channels, this, setting);
			},{				// mono
			XiiEffectGUI.new("- ixireverb 1x1 -", \xiiReverb1x1, params, channels, this, setting);
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }
	
}

XiiChorus {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initChorus(server, channels, setting);
		}
		
	initChorus {arg server, channels, setting;
	
		var s, params, preDelaySpec, depthSpec, speedSpec; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiChorus1x1, { arg inbus=0, outbus=0, predelay, speed, depth, ph_diff, fxlevel=0.6, level=0;
		   	var in, sig, mods, numDelays = 12;
		   	in = InFeedback.ar(inbus, 1);
		   	mods = { |i|
		      	SinOsc.kr(speed * rrand(0.92, 1.08), ph_diff * i, depth, predelay);
		  	} ! numDelays;
		   	sig = DelayL.ar(in, 0.5, mods);
		   	sig = Mix(sig); 
			Out.ar(outbus, (sig * fxlevel) + (in * level));
		},[0, 0, 0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiChorus2x2, { arg inbus=0, outbus=0, predelay, speed, depth, ph_diff, fxlevel=0.6, level=0;
		   	var in, sig, mods, numOutChan=2, numDelays = 12;
		   	in = InFeedback.ar(inbus, 2);
		   	mods = { |i|
		      	SinOsc.kr(speed * rrand(0.92, 1.08), ph_diff * i, depth, predelay);
		  	} ! (numDelays * numOutChan);
		   	sig = DelayL.ar(in, 0.5, mods);
		   	sig = Mix(sig.clump(numOutChan)); 
			Out.ar(outbus, (sig * fxlevel) + (in * level));
		},[0, 0, 0.1]).load(s); 
		
		preDelaySpec = ControlSpec.new(0.0001, 0.2, \linear, 0, 0.1); 
		depthSpec = ControlSpec.new(0.0001, 0.1, \amp, 0, 0.5);
		speedSpec = ControlSpec.new(0.001, 0.5, \exponential, 0, 0.1); 
		
		params = [ 
		   ["PreDelay", "Depth", "Speed", "Fx Level", "Dry Level"], 
		   [\predelay, \depth, \speed, \fxlevel, \level], 
		   [preDelaySpec, depthSpec, speedSpec, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[0.08, 0.05, 0.1, 0.5, 0]})
		]; 
		
		xiigui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("- chorus 2x2 -", \xiiChorus2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- chorus 1x1 -", \xiiChorus1x1, params, channels, this, setting); 
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}


XiiOctave {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initOctave(server, channels, setting);
		}
		
	initOctave {arg server, channels, setting;
	
		var pitchSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiOctave1x1, {| inbus=0, outbus=0, pitch1=1, pitch2=1, vol1=0.25, vol2=0.25, dispersion=0, fxlevel=0.5, level=0 | 
		   var fx1, fx2, sig; 
		   sig = InFeedback.ar(inbus, 1); 
		   fx1 = PitchShift.ar(sig, 0.2, pitch1, dispersion, 0.0001);
		   fx2 = PitchShift.ar(sig, 0.2, pitch2, dispersion, 0.0001);
		   Out.ar(outbus,  ( ((fx1 * vol1) + (fx2 * vol2)) * fxlevel) + (sig * level) ); 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiOctave2x2, {| inbus=0, outbus=0, pitch1=1, pitch2=1, vol1=0.25, vol2=0.25, dispersion=0, fxlevel=0.5, level=0 | 
		   var fx1, fx2, sig; 
		   sig = InFeedback.ar(inbus, 2); 
		   fx1 = PitchShift.ar(sig, 0.2, pitch1, dispersion, 0.0001);
		   fx2 = PitchShift.ar(sig, 0.2, pitch2, dispersion, 0.0001);
		   Out.ar(outbus,  ( ((fx1 * vol1) + (fx2 * vol2)) * fxlevel) + (sig * level) ); 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		pitchSpec = ControlSpec.new(0, 2, \linear, 0.01, 1); 
		
		params = [ 
		   ["Pitch1", "Vol1", "Pitch2", "Vol2", "Dispersion", "Fx level", "Dry Level"], 
		   [\pitch1, \vol1, \pitch2, \vol2, \dispersion, \fxlevel, \level], 
		   [pitchSpec, \amp, pitchSpec, \amp, \amp, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[1.25, 0.25, 0.5, 1.0, 0, 1.0, 0.2 ]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- octave 2x2 -", \xiiOctave2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- octave 1x1 -", \xiiOctave1x1, params, channels, this, setting); 
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}

XiiTremolo {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initTremolo(server, channels, setting);
		}
		
	initTremolo {arg server, channels, setting;
	
		var freqSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiTremolo1x1, {| inbus=0, outbus=0, freq=1, strength=1, fxlevel=0.5, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 1); 
		   fx = sig * SinOsc.ar(freq, 0, strength, 1); 
		   Out.ar(outbus, (fxlevel * fx) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 
		
		// stereo
		SynthDef(\xiiTremolo2x2, {| inbus=0, outbus=0, freq=1, strength=1, fxlevel=0.5, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 2); 
		   fx = sig * SinOsc.ar(freq, 0, strength, 1); 
		   Out.ar(outbus, (fxlevel * fx) + (sig * level)) // level 
		},[0,0,0.1,0.1,0,0]).load(s); 

		freqSpec = ControlSpec.new(0.1, 12, \linear, 0, 2); 
		
		params = [ 
		   ["Freq", "Strength", "Fx level", "Dry Level"], 
		   [\freq, \strength, \fxlevel, \level], 
		   [freqSpec, \amp, \amp, \amp], 
		   if(setting.notNil, {setting[5]}, {[0.5, 0.4, 0.65, 0 ]})
		]; 
		
		xiigui = if(channels == 2, {	// stereo
			XiiEffectGUI.new("- tremolo 2x2 -", \xiiTremolo2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("- tremolo 1x1 -", \xiiTremolo1x1, params, channels, this, setting);
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}
	
XiiEqualizer {
	var <>xiigui;
	var <>win, params;
	var msl;
	
	*new { arg server, channels, setting= nil;
		^super.new.initEqualizer(server, channels, setting);
		}
		
	initEqualizer {arg server, channels, setting;
	
		var s, size, bandSynthList, freqList;
		var mslwLeft, mslwTop;
		var signalGroup, eqGroup;
		var lay, inbus, outbus, tgt, addAct, fxOn, cFreqWin, theQ; 
		var name = "- equalizer -";
		var point;
		var stereoChList, monoChList;
		var onOffButt, cmdPeriodFunc;
		
		s = server ? Server.default;

		if ( (Archive.global.at(\win_position).isNil), {
			Archive.global.put(\win_position, IdentityDictionary.new);
		});
		// add pair if not there already else fetch the info
		if ( (Archive.global.at(\win_position).at(name.asSymbol).isNil), {
			point = Point(660,240);
			Archive.global.at(\win_position).put(name.asSymbol, point);
		}, {
			point = Archive.global.at(\win_position).at(name.asSymbol);
		});
		
		// mono
		SynthDef(\xiiEqband1x1, { arg inbus=20, outbus=0, freq=333, rq=0.5, amp;
			var signal, in, srq;
			in = InFeedback.ar(inbus, 1);
			srq = rq.sqrt; // thanks BlackRain - Q is compensated
			signal = BPF.ar(BPF.ar(in, freq, srq), freq, srq, amp); // double BPF
			Out.ar(outbus, signal ); // 15 bands dividing 1 = 0.0666
		}).load(s);

		// stereo
		SynthDef(\xiiEqband2x2, { arg inbus=20, outbus=0, freq=333, rq=0.5, amp;
			var signal, in, srq;
			in = InFeedback.ar(inbus, 2);
			srq = rq.sqrt; // thanks BlackRain - Q is compensated
			signal = BPF.ar(BPF.ar(in, freq, srq), freq, srq, amp); // double BPF
			Out.ar(outbus, signal ); // 15 bands dividing 1 = 0.0666
		}).load(s);
				
		tgt = 1; 
		addAct = \addToTail; 
		fxOn = false; 
		signalGroup = Group.new(s, \addToTail);
		size = 31;
		mslwLeft = 10;
		mslwTop = 5;
		bandSynthList = List.new;
		freqList = [20, 25, 31.5, 40, 50, 63, 80, 100, 125, 160, 200, 250, 
		315, 400, 500, 630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 
		5000, 6300, 8000, 10000, 12500, 16000, 20000]; // 1/3 octave bands

		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[Array.fill(size, 0.5), 0, 0, 0.5]}, {setting[2]});

		inbus = params[1]; 
		outbus = params[2];
		theQ = params[3];

		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList = XiiACDropDownChannels.getMonoChnList;

		win = GUI.window.new(name, Rect(point.x, point.y, 520, 243), resizable:false).front;
		
		msl = GUI.multiSliderView.new(win, Rect(mslwLeft, mslwTop, 496, 200))
			.value_(params[0])
			.isFilled_(false)
			.strokeColor_(Color.new255(10, 55, 10))
			.fillColor_(Color.green(alpha: 0.2))
			.valueThumbSize_(4.0)
			.indexThumbSize_(10.0)
			.gap_(6)
			.canFocus_(false)
			.background_(Color.white)
			.action_({arg xb; 
				cFreqWin.string_(freqList.at(xb.index).asString);
				bandSynthList[xb.index].set(\amp, xb.value.at(xb.index));
			});

		GUI.staticText.new(win, Rect(365, 215, 60, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("band freq:"); 
		
		cFreqWin = GUI.staticText.new(win, Rect(410, 215, 60, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("0"); 
		
		OSCIISlider.new(win, Rect(445, 214, 60, 8), "- Q", 0.001, 1, params[3], 0.001, \exp)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(true)
			.action_({arg sl; 
					eqGroup.set(\rq, sl.value); 
					theQ = sl.value;
					params[3] = sl.value;
				});
						
		win.view.decorator = lay = FlowLayout(win.view.bounds, 5@215, 5@215); 
		
		// inBus
		GUI.staticText.new(win, 30 @ 15).font_(GUI.font.new("Helvetica", 9)).string_("inBus").align_(\right); 

		GUI.popUpMenu.new(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(inbus/channels)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {inbus = ch.value}, {inbus = ch.value * 2});
				if (fxOn, { eqGroup.set(\inbus, inbus) });
				params[1] = inbus;
			});

		// outBus
		GUI.staticText.new(win, 30 @ 15).font_(GUI.font.new("Helvetica", 9)).string_("outBus").align_(\right); 
		
		GUI.popUpMenu.new(win, 40 @ 15)
			.items_(if(channels==1, {monoChList},{stereoChList}))
			.value_(outbus/channels)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(channels==1, {outbus = ch.value}, {outbus = ch.value * 2});
				if (fxOn, { eqGroup.set(\outbus, outbus) });
				params[2] = outbus;
			});
					
		// Target
		GUI.staticText.new(win, 15 @ 15).font_(GUI.font.new("Helvetica", 9)).string_("Tgt").align_(\right); 
		GUI.numberBox.new(win, 40 @ 15).font_(GUI.font.new("Helvetica", 9)).value_(tgt).action_({|v| 
		   v.value = 0.max(v.value); 
		   tgt = v.value.asInteger; 
		}); 
		
		// addAction
		GUI.popUpMenu.new(win, 60@15) 
		   .font_(GUI.font.new("Helvetica", 9)) 
		   .items_(["addToHead", "addToTail", "addAfter", "addBefore"]) 
		   .value_(1) 
		   .action_({|v| 
		      addAct = v.items.at(v.value).asSymbol; 
		   }); 
		
		// Print
		GUI.button.new(win,18@15) 
		   .font_(GUI.font.new("Helvetica", 9)) 
		   .states_([["#"]]) ;

		// on off
		onOffButt = GUI.button.new(win, 40@15) 
		   .font_(GUI.font.new("Helvetica", 9)) 
		   .states_([["On", Color.black, Color.clear],
					["Off", Color.black, Color.green(alpha:0.2)]]) 
		   .action_({|v| 
		      if ( v.value == 0, { 
		         eqGroup.free;
				bandSynthList = List.new;
		      },{ 
		         fxOn = true; 	
				eqGroup = Group.new(s, \addToTail); // was addAfter
				if(channels == 2, { 	// stereo
					size.do({arg i;
						bandSynthList.add(Synth(\xiiEqband2x2, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \freq, freqList[i], 
									 \rq, theQ, 
									 \amp, msl.value.at(i)], 
									target: eqGroup)); //addAction: \addToTail
					})
					}, {				// mono
					size.do({arg i;
						bandSynthList.add(Synth(\xiiEqband1x1, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \freq, freqList[i], 
									 \rq, theQ, 
									 \amp, msl.value.at(i)], 
									target: eqGroup)); //addAction: \addToTail
					})					
				}); // end if
		       }) 
		   }); 
		
		// drawing the line
		win.drawHook = {
				GUI.pen.color = Color.new255(0, 100, 0, 100);
				31.do({arg i;
					GUI.pen.moveTo(mslwLeft+ 6+ (i*16) @ (mslwTop));
					GUI.pen.lineTo(mslwLeft+ 6+ (i*16) @ (mslwTop+200));
					GUI.pen.stroke
				});
				GUI.pen.moveTo(mslwLeft @ (mslwTop+100));
				GUI.pen.lineTo(mslwLeft+496 @ (mslwTop+100));
				GUI.pen.stroke;
			};
			
		cmdPeriodFunc = { onOffButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({ 
			var t;
			size.do({arg i; bandSynthList[i].free}); 
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			point = Point(win.bounds.left, win.bounds.top);
			Archive.global.at(\win_position).put(name.asSymbol, point);
			}); 
		win.refresh;
	}
	
	getState { // for save settings
		var point;
		params[0] = msl.value.round(0.01);
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params]; // channels, point, params
	}
}
	
XiiRandomPanner {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initRandomPanner(server, channels, setting);
		}
		
	initRandomPanner {arg server, channels, setting;

		var volSpec, trigfreqSpec, strengthSpec, params, s; 
		
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiRandompanner1x2, { arg inbus=0, outbus=0, trigfreq=10, strenght=0.81;
			var sig, signal, pan, trig;
			trig = 	Dust.kr(trigfreq);
			sig =   	InFeedback.ar(inbus, 1);
			pan= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			signal = 	Pan2.ar(sig, pan);
			Out.ar(outbus, signal);
		}).load(s);
		
		// stereo
		SynthDef(\xiiRandompanner2x2, { arg inbus=0, outbus=0, trigfreq=10, strenght=0.81;
			var sig, left, right, panL, panR, trig;
			trig = 	Dust.kr(trigfreq);
			sig =   	InFeedback.ar(inbus, 2);
			panL= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			panR= 	TBetaRand.kr(-1, 1, strenght, strenght, trig);
			left =  	Pan2.ar(sig[0], panL);
			right = 	Pan2.ar(sig[1], panR);
			Out.ar(outbus, left);
			Out.ar(outbus, right);
		}).load(s);


		trigfreqSpec = ControlSpec.new(0.5, 10, \exponential, 0, 2); 
		strengthSpec = ControlSpec.new(1, 0.01, \linear, 0, 0.5); 

		params = [ 
		   ["Freq", "Strength"], 
		   [ \trigfreq, \strength], 
		   [trigfreqSpec, strengthSpec], 
		   if(setting.isNil.not, {setting[5]}, {[0.2, 0.5]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("RandomPanner 2x2", \xiiRandompanner2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("RandomPanner 1x2", \xiiRandompanner1x2, params, channels, this, setting);
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}

XiiCombVocoder {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initCombVocoder(server, channels, setting);
		}
		
	initCombVocoder {arg server, channels, setting;
		var volSpec, delayTailSpec, params, s; 
		s = server ? Server.default;
		// mono
		SynthDef(\xiiCombvocoder1x1, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
		   var fx, sig; 
		   sig = InFeedback.ar(inbus,1); 
		   fx = sig + LocalIn.ar(1); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		},[nil, nil, 0.2,0.2,0.1,0.1]).load(s); 
		
		// stereo
		SynthDef(\xiiCombvocoder2x2, {arg inbus=0,
							outbus=0, 
							maxDelay=2, // hardcoded here
							delay=0.4, 
							feedback=0.0, 
							fxlevel = 0.7, 
							level=1.0;
		   var fx, sig; 
		   sig = InFeedback.ar(inbus,2); 
		   fx = sig + LocalIn.ar(2); 
		   fx = DelayC.ar(fx, maxDelay, delay); 
		   LocalOut.ar(fx * feedback); 
		   Out.ar(outbus, (fx * fxlevel) + (sig * level)) 
		},[nil, nil, 0.2,0.2,0.1,0.1]).load(s); 

		delayTailSpec = ControlSpec.new(0.3, 0.001, \exponential, 0, 1.2); 
		volSpec = ControlSpec.new(0.3, 0.999, \exponential, 0, 1.2); 

		params = [ 
		   ["Delay", "Feedback", "Fx level", "Dry Level"], 
		   [ \delay, \feedback, \fxlevel, \level], 
		   [delayTailSpec, volSpec, \amp, \amp], 
		   if(setting.isNil.not, {setting[5]}, {[0.025, 0.87, 1.0, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- combvocoder 2x2 -", \xiiCombvocoder2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- combvocoder 1x1 -", \xiiCombvocoder1x1, params, channels, this, setting);
		});
		win = xiigui.win;
	}
}

XiiMRRoque {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initMRRoque(server, channels, setting);
		}
		
	initMRRoque {arg server, channels, setting;

		var rateSpec, timeSpec, params, s; //, buffer; 
		
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiMrRoque1x1, {|inbus=0, outbus=0, mix = 0.25, room = 0.15, damp = 0.5, 
				outmix = 0.25, outroom = 0.15, outdamp = 0.5, rate=1, end = 4, vol=1|
			
			var in, reverb, reverb2, signal, sr, buffer;
			buffer = LocalBuf(s.sampleRate * 4.0, 1);
			sr = SampleRate.ir(0);
			in = InFeedback.ar(inbus, 1);
			reverb = FreeVerb.ar(in, mix, room, damp);
			BufWr.ar(reverb, buffer, Phasor.ar(0, 1, 0, sr*end));
			signal = BufRd.ar(1, buffer, Phasor.ar(0, BufRateScale.kr(0) * rate, 0, sr*end));
			reverb2 = FreeVerb.ar(signal, outmix, outroom, outdamp);	
			Out.ar(outbus, (signal+reverb2) * vol);
		},[0.2,0.2,0.1,0.1]).load(s); 
				
		// stereo
		SynthDef(\xiiMrRoque2x2, {|inbus=0, outbus=0, mix = 0.25, room = 0.15, damp = 0.5, 
				outmix = 0.25, outroom = 0.15, outdamp = 0.5, rate=1, end = 4, vol=1|
			
			var in, reverb, reverb2, signal, sr, buffer;
			buffer = LocalBuf(s.sampleRate * 4.0, 2);
			sr = SampleRate.ir(0);
			in = InFeedback.ar(inbus, 2);
			reverb = FreeVerb.ar(in, mix, room, damp);
			BufWr.ar(reverb, buffer, Phasor.ar(0, 1, 0, sr*end));
			signal = BufRd.ar(2, buffer, Phasor.ar(0, BufRateScale.kr(0) * rate, 0, sr*end));
			reverb2 = FreeVerb.ar(signal, outmix, outroom, outdamp);	
			Out.ar(outbus, (signal+reverb2) * vol);
		},[0.2,0.2,0.1,0.1]).load(s); 

		timeSpec = ControlSpec.new(1.0, 4.0, \lin, 0.1, 4.0); 
		rateSpec = ControlSpec.new(-1.5, 1.5, \lin, 0.1, -1.0); 
		
		/*
		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, s.sampleRate * 4.0, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, s.sampleRate * 4.0, 1); // a four second 1 channel Buffer
		});
		*/
		
		params = [ 
		   ["Time", "Rate", "PreMix", "PreRoom", "PreDamp", "Mix", "Room", "Damp", "Volume"], 
		   [ \end, \rate, \mix, \room, \damp, \outmix, \outroom, \outdamp, \vol ], 
		   [timeSpec, rateSpec, \amp, \amp, \amp, \amp, \amp, \amp, \amp], 
		   if(setting.isNil.not, {setting[5]}, {[4, -1.0, 0.4, 0.4, 0.2, 0.4, 0.4, 0.2, 1.0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- mr roque 2x2 -", \xiiMrRoque2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- mr roque 1x1 -", \xiiMrRoque1x1, params, channels, this, setting);
		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}

XiiMultiDelay {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initXiiMultiDelay(server, channels, setting);
		}
		
	initXiiMultiDelay {arg server, channels, setting;

		var delay1Spec, delay2Spec, delay3Spec, delay4Spec, params, s; 
		//var buffer;
		
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiMultidelay1x1, {arg outbus=0, inbus=0, amp = 1,
				dtime1=1, d1amp = 0.6, 
				dtime2=3, d2amp = 0.6,
				dtime3=4, d3amp = 0.6,
				dtime4=4.5, d4amp = 0.6;
			var in, delays, d1, d2, d3, d4, buffer;
			buffer = LocalBuf(s.sampleRate * 10.0, 1);
			in = InFeedback.ar(inbus, 1); 
			d1 = BufDelayN.ar(buffer, in, dtime1) * d1amp;
			d2 = BufDelayN.ar(buffer, in, dtime2) * d2amp;
			d3 = BufDelayN.ar(buffer, in, dtime3) * d3amp;
			d4 = BufDelayN.ar(buffer, in, dtime4) * d4amp;
			Out.ar(outbus, in + d1 + d2 + d3 + d4);
		}).load(s);

		// stereo
		SynthDef(\xiiMultidelay2x2, {arg outbus=0, inbus=0, amp = 1,
				dtime1=1, d1amp = 0.6, 
				dtime2=3, d2amp = 0.6,
				dtime3=4, d3amp = 0.6,
				dtime4=4.5, d4amp = 0.6;
			var in, delays, d1, d2, d3, d4, buffer;
			in = InFeedback.ar(inbus, 2); 
			buffer = LocalBuf(s.sampleRate * 10.0, 1);
			d1 = BufDelayN.ar(buffer, in, dtime1) * d1amp;
			d2 = BufDelayN.ar(buffer, in, dtime2) * d2amp;
			d3 = BufDelayN.ar(buffer, in, dtime3) * d3amp;
			d4 = BufDelayN.ar(buffer, in, dtime4) * d4amp;
			Out.ar(outbus, in + d1 + d2 + d3 + d4);
		}).load(s);

		/*
		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, s.sampleRate * 10.0, 2); // a 10 second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, s.sampleRate * 10.0, 1); // a 10 second 1 channel Buffer
		});
		*/
		
		delay1Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 1); 
		delay2Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 2); 
		delay3Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 3.5); 
		delay4Spec = ControlSpec.new(0.1, 10, \lin, 0.1, 4); 
		
		params = [ 
		   ["Dry vol", "Delay 1", "vol", "Delay 2", "vol", "Delay 3", "vol", "Delay 4", "vol"], 
		   [ \amp, \dtime1, \d1amp, \dtime2, \d2amp, \dtime3, \d3amp, \dtime4, \d4amp], 
		   [\amp, delay1Spec, \amp, delay2Spec, \amp, delay3Spec, \amp, delay4Spec, \amp], 
		   if(setting.isNil.not, {setting[5]}, {[1, 1, 1, 2, 1, 3.5, 1, 4, 1]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- multidelay 2x2 -", \xiiMultidelay2x2, params, channels, this, setting);
			}, {				// mono
			XiiEffectGUI.new("- multidelay 2x2 -", \xiiMultidelay1x1, params, channels, this, setting);		});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }	
	
	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}



XiiCyberPunk {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initCyberPunk(server, channels, setting);
		}
		
	initCyberPunk {arg server, channels, setting;
	
		var pitchSpec, punkSpec, memlenSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiCyberPunk1x1, {| inbus=0, outbus=0, pitchratio=0, zcperchunk=0, memlen =0, fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 1); 
		   fx = Squiz.ar(sig, pitchratio, zcperchunk, memlen); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		}).load(s); 

		// stereo
		SynthDef(\xiiCyberPunk2x2, {| inbus=0, outbus=0,pitchratio=0, zcperchunk=0, memlen =0,  fxlevel=0.75, level=0 | 
		   var fx, sig; 
		   sig = InFeedback.ar(inbus, 2); 
		   fx = Squiz.ar(sig, pitchratio, zcperchunk, memlen); 
		   Out.ar(outbus, (fx*fxlevel) + (sig * level)) // level 
		}).load(s); 

		pitchSpec = ControlSpec.new(1, 10, \linear, 0.01, 1); 
		punkSpec = ControlSpec.new(1, 10, \linear, 0.01, 1); 
		memlenSpec = ControlSpec.new(0.001, 0.2, \linear, 0.001, 0.1); 
		
		params = [ 
		   ["PitchRatio", "zcChunk", "Memlen", "Fx Level", "Dry Level"], 
		   [\pitchratio, \zcperchunk, \memlen, \fxlevel, \level], 
		   [pitchSpec, punkSpec, memlenSpec, \amp, \amp], 
		   if(setting.isNil.not, {setting[5]}, {[2, 1, 0.1, 1, 0 ]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- cyberpunk 2x2 -", \xiiCyberPunk2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- cyberpunk 1x1 -", \xiiCyberPunk1x1, params, channels, this, setting);
			});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}



XiiBitCrusher {	
	var <>xiigui, <>win;

	*new { arg server, channels, setting = nil;
		^super.new.initCyberPunk(server, channels, setting);
		}
		
	initCyberPunk {arg server, channels, setting;
	
		var sampleSpec, bitSpec, params, s; 
		s = server ? Server.default;
		
		// mono
		SynthDef(\xiiBitCrusher1x1, {| inbus=0, outbus=0, samplerate=22050, bitsize=0, fxlevel=0.75, level=0 | 
		   var fx, sig, downsamp, bitRedux; 
		   sig = InFeedback.ar(inbus, 1);
		   // thanks alberto!
		   downsamp = Latch.ar(sig, Impulse.ar(samplerate*0.5));
		   bitRedux = downsamp.round(0.5 ** bitsize);
		   Out.ar(outbus, (bitRedux*fxlevel) + (sig * level)) // level 
		}).load(s); 

		// stereo
		SynthDef(\xiiBitCrusher2x2, {| inbus=0, outbus=0, samplerate=22050, bitsize=0, fxlevel=0.75, level=0 | 
		   var fx, sig, downsamp, bitRedux; 
		   sig = InFeedback.ar(inbus, 2);
		   // thanks alberto!
		   downsamp = Latch.ar(sig, Impulse.ar(samplerate*0.5));
		   bitRedux = downsamp.round(0.5 ** bitsize);
		   Out.ar(outbus, (bitRedux*fxlevel) + (sig * level)) // level 
		}).load(s); 

		sampleSpec = ControlSpec.new(600, s.sampleRate, \exponential, 1, s.sampleRate); 
		bitSpec = ControlSpec.new(2, 16, \exponential, 1, 16); 
		
		params = [ 
		   ["sampleRate", "bitSize", "Fx Level", "Dry Level"], 
		   [\samplerate, \bitsize, \fxlevel, \level], 
		   [sampleSpec, bitSpec, \amp, \amp], 
		   if(setting.isNil.not, {setting[5]}, {[s.sampleRate, 16, 1, 0 ]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- bitcrusher 2x2 -", \xiiBitCrusher2x2, params, channels, this, setting); 
			},{				// mono
			XiiEffectGUI.new("- bitcrusher 1x1 -", \xiiBitCrusher1x1, params, channels, this, setting);
			});
		win = xiigui.win;
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }
	
	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }
	
}