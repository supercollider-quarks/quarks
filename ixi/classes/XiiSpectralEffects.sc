
// clips bins to a threshold
XiiMagClip {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagClip(server, channels, setting);
		}
		
	initMagClip {arg server, channels, setting;
		var threshSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagClip1x1, { arg inbus=0, outbus=0, thresh = 0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagClip(chain, thresh);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_MagClip2x2, { arg inbus=0, outbus=0, thresh = 0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagClip(chain, thresh); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		threshSpec = ControlSpec.new(0.01, 40, \exp, 0.01, 5); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Thresh", "FxLevel", "DryLevel"], 
		Ê Ê[ \thresh, \vol, \dryvol, \bufnum], 
		Ê Ê[threshSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[5, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- magclip 2x2 -", \xiiSpectral_MagClip2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- magclip 1x1 -", \xiiSpectral_MagClip1x1, params, channels, this, setting);
		})
	}

	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}



// average magnitudes across bins - smears it with its neighbors 
XiiMagSmear {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagSmear(server, channels, setting);
		}
		
	initMagSmear {arg server, channels, setting;
		var binsSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagSmear1x1, { arg inbus=0, outbus=0, bins = 0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagSmear(chain, bins);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_MagSmear2x2, { arg inbus=0, outbus=0, bins = 0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagSmear(chain, bins); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		binsSpec = ControlSpec.new(1, 50, \lin, 1, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Bins", "FxLevel", "DryLevel"], 
		Ê Ê[\bins, \vol, \dryvol, \bufnum], 
		Ê Ê[binsSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- magsmear 2x2 -", \xiiSpectral_MagSmear2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- magsmear 1x1 -", \xiiSpectral_MagSmear1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




// average magnitudes across bins - smears it with its neighbors 
XiiMagShift {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagShift(server, channels, setting);
		}
		
	initMagShift {arg server, channels, setting;
		var stretchSpec, shiftSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagSmear1x1, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagShift(chain, stretch, shift);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_MagSmear2x2, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_MagShift(chain, stretch, shift); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		stretchSpec = ControlSpec.new(0.1, 5, \lin, 0.01, 1); 
		shiftSpec = ControlSpec.new(0.1, 50, \lin, 0.01, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Stretch", "Shift", "FxLevel", "DryLevel"], 
		Ê Ê[\stretch, \shift, \vol, \dryvol, \bufnum], 
		Ê Ê[stretchSpec, shiftSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1.0, 0.1, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- magshift 2x2 -", \xiiSpectral_MagSmear2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- magshift 1x1 -", \xiiSpectral_MagSmear1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }
	
	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }
	
}





// freezes the magnitudes when level > 0.5 
XiiMagFreeze {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initMagFreeze(server, channels, setting);
		}
		
	initMagFreeze {arg server, channels, setting;
		var params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_MagFreeze1x1, { arg inbus=0, outbus=0, freeze=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_MagFreeze(chain, freeze > 0.5 );
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_MagFreeze2x2, { arg inbus=0, outbus=0, freeze=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_MagFreeze(chain, freeze > 0.5); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Freeze",  "FxLevel", "DryLevel"], 
		Ê Ê[\freeze, \vol, \dryvol, \bufnum], 
		Ê Ê[\unipolar, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[0.5, 0.1, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- magfreeze 2x2 -", \xiiSpectral_MagFreeze2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- magfreeze 1x1 -", \xiiSpectral_MagFreeze1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}





// makes series of gaps in spectrum 
XiiRectComb {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initRectComb(server, channels, setting);
		}
		
	initRectComb {arg server, channels, setting;
		var teethSpec, widthSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_RectComb1x1, { arg inbus=0, outbus=0, teeth=0, phase=0, width=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_RectComb(chain, teeth, phase, width);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_RectComb2x2, { arg inbus=0, outbus=0, teeth=0, phase=0, width=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 2);
			chain = FFT(bufnum, in);
			chain = PV_RectComb(chain, teeth, phase, width); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		teethSpec = ControlSpec.new(0.01, 35, \lin, 0.01, 1); 
		//widthSpec = ControlSpec.new(0.1, 50, \lin, 0.01, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Teeth", "Phase", "Width", "FxLevel", "DryLevel"], 
		Ê Ê[\teeth, \phase, \width, \vol, \dryvol, \bufnum], 
		Ê Ê[teethSpec, \bipolar, \amp, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[5.0, 0.5, 0.2, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- rectcomb 2x2 -", \xiiSpectral_RectComb2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- rectcomb 1x1 -", \xiiSpectral_RectComb1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}






// randomises the order of bins 
XiiBinScramble {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initBinScramble(server, channels, setting);
		}
		
	initBinScramble {arg server, channels, setting;
		var trigSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_BinScramble1x1, { arg inbus=0, outbus=0, wipe=0, width=0, trig=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_BinScramble(chain, wipe, width, Impulse.kr(trig));
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_BinScramble2x2, { arg inbus=0, outbus=0, wipe=0, width=0, trig=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_BinScramble(chain, wipe, width, Impulse.kr(trig)); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		trigSpec = ControlSpec.new(0, 5, \lin, 0.01, 0); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Wipe", "Width", "Trig", "FxLevel", "DryLevel"], 
		Ê Ê[\wipe, \width, \trig, \vol, \dryvol, \bufnum], 
		Ê Ê[\unipolar, \unipolar, trigSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[0.3, 1, 0, 1.0, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- binscramble 2x2 -", \xiiSpectral_BinScramble2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- binscramble 1x1 -", \xiiSpectral_BinScramble1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




// shift and scale the position of the bins 
XiiBinShift {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initBinShift(server, channels, setting);
		}
		
	initBinShift {arg server, channels, setting;
		var stretchSpec, shiftSpec, params, s, buffer; 
		s = server ? Server.default;
		
		SynthDef(\xiiSpectral_BinShift1x1, { arg inbus=0, outbus=0, stretch=0, shift=0, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = PV_BinShift(chain, stretch, shift);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_BinShift2x2, { arg inbus=0, outbus=0, stretch=0, shift=0, trig=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = PV_BinShift(chain, stretch, shift); 
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		stretchSpec = ControlSpec.new(0.1, 5, \lin, 0.01, 1); 
		shiftSpec = ControlSpec.new(-128, 228, \lin, 1, 1); 

		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});

		params = [ 
		Ê Ê["Stretch", "Shift", "FxLevel", "DryLevel"], 
		Ê Ê[\stretch, \shift, \vol, \dryvol, \bufnum], 
		Ê Ê[stretchSpec, shiftSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1, 1, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- binshift 2x2 -", \xiiSpectral_BinShift2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- binshift 1x1 -", \xiiSpectral_BinShift1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}





// shift and scale the position of the bins 
XiiSpectralDelay {	
	var <>xiigui;

	*new { arg server, channels, setting = nil;
		^super.new.initSpectralDelay(server, channels, setting);
		}
		
	initSpectralDelay {arg server, channels, setting;
		var delaySpec, params, s, buffer; 
		s = server ? Server.default;
		buffer = Buffer.alloc(s, 1024, 1);

		SynthDef(\xiiSpectral_Delay1x1, { arg inbus=0, outbus=0, stretch=0, delay=0.7, bufnum=0, dryvol = 0, vol=1;
			var in, chain;
			in = InFeedback.ar(inbus, 1);
			chain = FFT(bufnum, in);
			chain = chain.pvcollect(buffer.numFrames, {|mag, phase, index|
				mag + DelayN.kr(mag, 2, delay);
			}, frombin: 0, tobin: 256, zeroothers: 1);
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain) * vol));
		}).load(s);
		
		
		SynthDef(\xiiSpectral_Delay2x2, { arg inbus=0, outbus=0, stretch=0, delay=0, trig=0, bufnum=0, dryvol=0, vol=1;
			var in, chain;
			in = Mix.ar(InFeedback.ar(inbus, 2));
			chain = FFT(bufnum, in);
			chain = chain.pvcollect(buffer.numFrames, {|mag, phase, index|
				mag + DelayN.kr(mag, 2, delay);
			}, frombin: 0, tobin: 256, zeroothers: 1);
			// I need to delay the output of raw signal by the size of FFT buffer
			Out.ar(outbus, (DelayL.ar(in, 0.2, ((2*2048))/SampleRate.ir(0)) * dryvol) + (IFFT(chain).dup * vol));
		}).load(s);
		
		delaySpec = ControlSpec.new(0.1, 2, \lin, 0.01, 1); 
/*
		if(channels == 2, { 	// stereo
			buffer = Buffer.alloc(s, 2048, 2); // a four second 2 channel Buffer
		}, {
			buffer = Buffer.alloc(s, 2048, 1); // a four second 1 channel Buffer
		});
*/
		params = [ 
		Ê Ê["Delay", "FxLevel", "DryLevel"], 
		Ê Ê[\delay, \vol, \dryvol, \bufnum], 
		Ê Ê[delaySpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1, 1, 0, buffer.bufnum]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- spectraldelay 2x2 -", \xiiSpectral_Delay2x2, params, channels, this, setting); 
			}, {				// mono
			XiiEffectGUI.new("- spectraldelay 1x1 -", \xiiSpectral_Delay1x1, params, channels, this, setting);
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}


