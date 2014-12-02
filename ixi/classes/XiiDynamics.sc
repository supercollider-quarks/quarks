
XiiLimiter {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiLimiter(server, channels, setting);
		}
		
	initXiiLimiter {arg server, channels, setting;
		var durSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiLimiter1x1, {arg inbus=0,
							outbus=0, 
							level=1,
							dur = 0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Limiter.ar(sig, level, dur); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiLimiter2x2, {arg inbus=0,
							outbus=0, 
							level=1,
							dur = 0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = Limiter.ar(sig, level, dur); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		durSpec = ControlSpec.new(0.001, 0.1, \linear, 0.001, 0.01); 
		
		params = [ 
		Ê Ê["Level", "Dur"], 
		Ê Ê[ \level, \dur], 
		Ê Ê[ \amp, durSpec], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 0.01]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- limiter 2x2 -", \xiiLimiter2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- limiter 1x1 -", \xiiLimiter1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}



XiiNormalizer {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiNormalizer(server, channels, setting);
		}
		
	initXiiNormalizer {arg server, channels, setting;
		var durSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiNormalizer1x1, {arg inbus=0,
							outbus=0, 
							level=1,
							dur = 0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Normalizer.ar(sig, level, dur); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiNormalizer2x2, {arg inbus=0,
							outbus=0, 
							level=1,
							dur = 0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = Normalizer.ar(sig, level, dur); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		durSpec = ControlSpec.new(0.001, 0.1, \linear, 0.001, 0.01); 
		
		params = [ 
		Ê Ê["Level", "Dur"], 
		Ê Ê[ \level, \dur], 
		Ê Ê[ \amp, durSpec], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 0.01]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- normalizer 2x2 -", \xiiNormalizer2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- normalizer 1x1 -", \xiiNormalizer1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




XiiGate {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiGate(server, channels, setting);
		}
		
	initXiiGate {arg server, channels, setting;
		var gateSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiGate1x1, {arg inbus=0,
							outbus=0,
							gate=1;
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Gate.ar(sig, gate); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiGate2x2, {arg inbus=0,
							outbus=0,
							gate=1;
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = Gate.ar(sig, gate); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		gateSpec = ControlSpec.new(0, 1.0, \linear, 0.0001, 1); 
		
		params = [ 
		Ê Ê["Gate"], 
		Ê Ê[ \gate], 
		Ê Ê[gateSpec], 
		Ê Êif(setting.notNil, {setting[5]}, {[1]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- gate 2x2 -", \xiiGate2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- gate 1x1 -", \xiiGate1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




XiiCompressor {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiCompressor(server, channels, setting);
		}
		
	initXiiCompressor {arg server, channels, setting;
		var linearSpec, clampSpec, relaxSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiCompressor1x1, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=1,
							slopeAbove=0.5,
							clampTime=0.01,
							relaxTime=0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiCompressor2x2, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=1,
							slopeAbove=0.5,
							clampTime=0.01,
							relaxTime=0.012;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,2); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		linearSpec = ControlSpec.new(0.001, 1, \linear, 0.01, 1); 
		clampSpec = ControlSpec.new(0.002, 0.015, \lin, 0.001, 0.01); 
		relaxSpec = ControlSpec.new(0.004, 0.030, \lin, 0.001, 0.012); 
		
		params = [ 
		Ê Ê["Thresh", "SlopeBelow", "SlopeAbove", "ClampTime", "RelaxTime"], 
		Ê Ê[ \thresh, \slopeBelow, \slopeAbove, \clampTime, \relaxTime], 
		Ê Ê[linearSpec, linearSpec, linearSpec, clampSpec, relaxSpec ], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1, 0.5, 0.01, 0.012]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- compressor 2x2 -", \xiiCompressor2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- compressor 1x1 -", \xiiCompressor1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




XiiSustainer {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiSustainer(server, channels, setting);
		}
		
	initXiiSustainer {arg server, channels, setting;
		var linearSpec, clampSpec, relaxSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiSustainer1x1, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=0.1,
							slopeAbove=1,
							clampTime=0.01,
							relaxTime=0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiSustainer2x2, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=0.1,
							slopeAbove=1,
							clampTime=0.01,
							relaxTime=0.012;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,2); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		linearSpec = ControlSpec.new(0.001, 1, \linear, 0.01, 1); 
		clampSpec = ControlSpec.new(0.002, 0.015, \lin, 0.001, 0.01); 
		relaxSpec = ControlSpec.new(0.004, 0.030, \lin, 0.001, 0.012); 
		
		params = [ 
		Ê Ê["Thresh", "SlopeBelow", "SlopeAbove", "ClampTime", "RelaxTime"], 
		Ê Ê[ \thresh, \slopeBelow, \slopeAbove, \clampTime, \relaxTime], 
		Ê Ê[linearSpec, linearSpec, linearSpec, clampSpec, relaxSpec ], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 0.1, 1, 0.01, 0.012]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- sustainer 2x2 -", \xiiSustainer2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- sustainer 1x1 -", \xiiSustainer1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




// lowest level (thresh) of noisegate has to be more than 0
XiiNoiseGate {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiNoiseGate(server, channels, setting);
		}
		
	initXiiNoiseGate {arg server, channels, setting;
		var linearSpec, slopeAboveSpec, clampSpec, relaxSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiNoiseGate1x1, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=1,
							slopeAbove=10,
							clampTime=0.01,
							relaxTime=0.01;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiNoiseGate2x2, {arg inbus=0,
							outbus=0, 
							thresh=1,
							slopeBelow=1,
							slopeAbove=10,
							clampTime=0.01,
							relaxTime=0.012;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,2); 
		Ê Êfx = Compander.ar(sig, sig, thresh, slopeBelow, slopeAbove, clampTime, relaxTime); 
		Ê ÊOut.ar(outbus, fx) 
		}).load(s); 	

		linearSpec = ControlSpec.new(0.001, 1, \linear, 0.01, 1); 
		slopeAboveSpec = ControlSpec.new(1, 20, \linear, 0.1, 10); 
		clampSpec = ControlSpec.new(0.002, 0.015, \lin, 0.001, 0.01); 
		relaxSpec = ControlSpec.new(0.004, 0.030, \lin, 0.001, 0.012); 
		
		params = [ 
		Ê Ê["Thresh", "SlopeBelow", "SlopeAbove", "ClampTime", "RelaxTime"], 
		Ê Ê[ \thresh, \slopeBelow, \slopeAbove, \clampTime, \relaxTime], 
		Ê Ê[linearSpec, linearSpec, slopeAboveSpec, clampSpec, relaxSpec ], 
		Ê Êif(setting.notNil, {setting[5]}, {[1, 1, 10, 0.01, 0.012]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- noisegate 2x2 -", \xiiNoiseGate2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- noisegate 1x1 -", \xiiNoiseGate1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}




// not ready yet:


XiiExpander {	
	var <>xiigui;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiExpander(server, channels, setting);
		}
		
	initXiiExpander {arg server, channels, setting;
		var freqSpec, params, s; 
		s = server ? Server.local;
		
		// mono
		SynthDef(\xiiExpander1x1, {arg inbus=0,
							outbus=0, 
							freq=200,
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus,1); 
		Ê Êfx = HPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		// stereo
		SynthDef(\xiiExpander2x2, {arg inbus=0,
							outbus=0, 
							freq=200,
							fxlevel = 0.7, 
							level=1.0;
							
		Ê Êvar fx, sig; 
		Ê Êsig = InFeedback.ar(inbus, 2); 
		Ê Êfx = HPF.ar(sig, freq); 
		Ê ÊOut.ar(outbus, (fx * fxlevel) + (sig * level)) 
		}).load(s); 	

		freqSpec = ControlSpec.new(20, 20000, \exponential, 1, 2000); 
		
		params = [ 
		Ê Ê["Freq", "Fx level", "Dry Level"], 
		Ê Ê[ \freq, \fxlevel, \level], 
		Ê Ê[freqSpec, \amp, \amp], 
		Ê Êif(setting.notNil, {setting[5]}, {[2000, 1, 0]})
		]; 
		
		xiigui = if(channels == 2, { 	// stereo
			XiiEffectGUI.new("- expander 2x2 -", \xiiExpander2x2, params, channels, this, setting); /// 
			}, {				// mono
			XiiEffectGUI.new("- expander 1x1 -", \xiiExpander1x1, params, channels, this, setting); /// 
		})
	}
	start { xiigui.start }
	
	stop { xiigui.stop }

	setInBus_ {arg ch; xiigui.setInBus_(ch) }
	
	setOutBus_ {arg ch; xiigui.setOutBus_(ch) }
	
	setLoc_{arg x, y; xiigui.setLoc_(x, y) }

	setSlider_{arg slidernum=0, val=0.5; xiigui.setSlider_(slidernum, val) }

}
