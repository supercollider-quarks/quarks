/*

a = XiiSpectralEQ.new(s, 2)

*/

XiiSpectralEQ {
	var <>xiigui;
	var <>win, params;
	var msl;
	var synth;
	
	*new { arg server, channels=2, setting= nil;
		^super.new.initSpectralEQ(server, channels, setting);
		}
		
	initSpectralEQ {arg server, channels, setting;
	
		var s, size, bandSynthList, freqList;
		var mslwLeft, mslwTop;
		var lay, inbus, outbus, tgt, addAct, fxOn, cFreqWin, theQ; 
		var name = "Spectral Equalizer";
		var point;
		var stereoChList, monoChList;
		var onOffButt, cmdPeriodFunc;
		var bufA, bufB;
		var spectralsl;
		
		
		size = 1024;
		spectralsl = Array.fill(size, 0.0);

		s = server ? Server.local;

		bufA = Buffer.alloc(s, size, 1);
		bufB = Buffer.alloc(s, size, 1);
		bufB.loadCollection(spectralsl);
	   
		if ( (Archive.global.at(\win_position).isNil), {
			Archive.global.put(\win_position, IdentityDictionary.new);
		});
		// add pair if not there already else fetch the info
		if ( (Archive.global.at(\win_position).at(name.asSymbol).isNil), {
			point = Point(160,240);
			Archive.global.at(\win_position).put(name.asSymbol, point);
		}, {
			point = Archive.global.at(\win_position).at(name.asSymbol);
		});
		// END OF ARCHIVE CODE... Thanks blackrain again.

		SynthDef(\xiiSpectralEQ2x2, { arg inbus=0, outbus=0, bufnumA=0, bufnumB=1;
			var inA, chainA, inB, chainB, chain;
			inA = Mix.ar(InFeedback.ar(inbus, 2));
			chainA = FFT(bufnumA, inA);
			chain = PV_MagMul(chainA, bufnumB); 
			Out.ar(outbus, IFFT(chain).dup);
		}).load(s); 

		SynthDef(\xiiSpectralEQ1x1, { arg inbus=0, outbus=0, bufnumA=0, bufnumB=1;
			var inA, chainA, inB, chainB, chain;
			inA = InFeedback.ar(inbus, 1);
			chainA = FFT(bufnumA, inA);
			chain = PV_MagMul(chainA, bufnumB); 
			Out.ar(outbus, IFFT(chain));
		}).load(s); 
				
		tgt = 1; 
		addAct = \addToTail; 
		fxOn = false; 

		point = if(setting.isNil, {Point(100,100)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[Array.fill(size, 0.5), 0, 0, 0.5]}, {setting[2]});

		inbus = params[1]; 
		outbus = params[2];

		stereoChList = XiiACDropDownChannels.getStereoChnList;
		monoChList = XiiACDropDownChannels.getMonoChnList;

		win = GUI.window.new(name, Rect(point.x, point.y, 540, 243), resizable:false).front;
		
		msl = GUI.multiSliderView.new(win, Rect(10, 5, size/2 , 200))
			.value_(Array.fill(512, 0)) //.value_(spectralsl)
			.isFilled_(true)
			.canFocus_(false)
			.strokeColor_(Color.new255(10, 55, 10))
			.fillColor_(XiiColors.darkgreen)
			.background_(XiiColors.lightgreen)
			.valueThumbSize_(0.0)
			.indexThumbSize_(1.0)
			.gap_(0)
			.action_({ arg mltsl; 
				bufB.setn(msl.index*2, [msl.value[msl.index], msl.value[msl.index]]);
			});
		
		GUI.staticText.new(win, Rect(365, 215, 60, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("band freq:"); 
		
		cFreqWin = GUI.staticText.new(win, Rect(410, 215, 60, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("0"); 
		
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
				if (fxOn, { synth.set(\inbus, inbus) });
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
				if (fxOn, { synth.set(\outbus, outbus) });
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
		        	synth.free;
				// eqGroup.free;
				// bandSynthList = List.new;
		      },{ 
		         fxOn = true; 	
		
				if(channels == 2, { 	// stereo
					"synth created".postln;
					synth = Synth(\xiiSpectralEQ2x2, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \bufnumA, bufA,
									 \bufnumB, bufB,
									 ], 
									addAction: \addToTail); //addAction: \addToTail
					}, {				// mono
					synth = Synth(\xiiSpectralEQ1x1, 
									[\inbus, inbus, 
									 \outbus, outbus,
									 \bufnumA, bufA,
									 \bufnumB, bufB,
									 ], 
									addAction: \addToTail); //addAction: \addToTail
				}); // end if
		       }) 
		   }); 
			
		cmdPeriodFunc = { onOffButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);

		win.onClose_({ 
			var t;
			synth.free;
			//size.do({arg i; bandSynthList[i].free}); 
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
