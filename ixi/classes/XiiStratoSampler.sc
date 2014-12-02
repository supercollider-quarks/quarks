XiiStratoSampler {
	
	classvar livebufInstances;
	var bufInBufferDictFlag;
	var <>xiigui, <>win, params;
	
	*new { arg server, channels, setting = nil;
		if(server.serverRunning, {
			^super.new.initXiiStratoSampler(server, channels, setting);
		}, {
			XiiAlert("Please boot the server!");
		});
	}
	
	findMySlot {
		var instIndex;
		livebufInstances.do({arg inst, i; if(inst===this, {instIndex = i;}); });
		^instIndex;
	}
	
	initXiiStratoSampler {arg server, channels, setting;
	
///////////////

		var s, time, point;
		var inBusPop, outBusPop, outbus, inbus;
		var update, bufSec, prelevel, preLevelSl, reclevel, recLevelSl;
		var startIndexDrawClock, indexDrawClock, refreshClock, drawIndexButt, drawIndexFlag;
		var recSynth, playSynth, startPlayFunc;
		var volSl, amp, startButt;
		var bufLengthBox, newClearBufButt;
		var bufNameView, writeBufferButt;
		var bufNuminDict, buffer, bufPlot, writtenBufferNames;
		var startingPlayFlag, comingFromFieldFlag, writeBuffer;
		var runningFlag, bufferSecs, toggleButt;

		if(livebufInstances.isNil, {
			livebufInstances = [this];
		}, {
			livebufInstances = livebufInstances.add(this);
		});


s = Server.default;
bufInBufferDictFlag = false;

xiigui = nil;
point = if(setting.isNil, {Point(100, 500)}, {setting[1]});
params = if(setting.isNil, {[4, 0.75, 0.5, 2, 1, 0, 1]}, {setting[2]});

inbus = params[0]*2;
prelevel = params[1];
reclevel = params[2];
bufferSecs = params[3];
amp = params[4];
outbus = params[5]*2;
drawIndexFlag = if(params[6] == 1, {true}, {false});

bufNuminDict = 0;
startingPlayFlag = false;
comingFromFieldFlag = false;
writtenBufferNames = List.new;
runningFlag = false;

buffer = Buffer.alloc(s, s.sampleRate * bufferSecs, 1); 

win = GUI.window.new("- stratosampler -", Rect(point.x, point.y, 820, 240));

bufPlot = XiiBufferPlot.new(buffer, win, Rect( 120, 5, 680, 220));

GUI.staticText.new(win, Rect(13, 5, 80, 20))
	.font_(GUI.font.new("Helvetica", 9))
	.string_("inbus :");

inBusPop = GUI.popUpMenu.new(win, Rect(60, 8, 50, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.background_(Color.new255(255, 255, 255))
			.value_(4)
			.action_({ arg popup;
				inbus = popup.value * 2;
				params[0] = popup.value;
				try{
				recSynth.moveToTail;
				recSynth.set(\inbus, inbus);
				}
			});

preLevelSl = OSCIISlider.new(win, Rect(10, 32, 100, 8), "- preLevel", 0.0, 1.0, params[1], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				prelevel = sl.value;
				recSynth.set(\prelevel, prelevel) ;
				params[1] = sl.value;
			});

recLevelSl = OSCIISlider.new(win, Rect(10, 62, 100, 8), "- recLevel", 0.0, 1.0, params[2], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				reclevel = sl.value;
				recSynth.set(\reclevel, reclevel) ;
				params[2] = sl.value;
			});
				
bufLengthBox = NumberBox.new(win, Rect(10, 90, 40, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.step_(0.1)
			.align_(\center)
			.clipLo_(0.1)
			.clipHi_(120.0)
			.value_(params[3])
			.action_({ arg sbs;
				var val;
				val = sbs.value.clip(0.1, 120);
			});

				
newClearBufButt = GUI.button.new(win, Rect(60, 90, 50, 18))
		.states_([["new/clear", Color.black, Color.clear]])
		.font_(GUI.font.new("Helvetica", 9))			
		.action_({arg butt; var displayBufPlot, mySlot;
			if(bufSec == bufLengthBox.value, {
				fork{
					buffer.fill(0, buffer.numFrames, 0);
					0.5.wait;
					bufPlot.redraw;
				};
			}, {
				//startButt.valueAction_(0);
				bufPlot.remove;
				bufPlot = nil;
				params[3] = bufLengthBox.value;
				s.bind({
					buffer.free;
					s.sync;
					buffer = Buffer.alloc(s, s.sampleRate * bufLengthBox.value, 1);
					s.sync;
					bufPlot = XiiBufferPlot.new(buffer, win, Rect( 120, 5, 680, 220 ));
					
					s.sync;
					//0.5.wait; // should not be necessary
					{
						bufPlot.setIndex_(0);
						bufPlot.redraw;
					}.defer(0.5); // either defer or put the thing into a Routine
				});
			});
		});

bufNameView = GUI.textView.new(win, Rect(10, 122, 63, 14))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("strato"++(this.findMySlot+1)++"_0")
			.keyDownAction_({arg view, key, mod, unicode; 
				if(unicode ==13, {
					comingFromFieldFlag = true;
					writeBufferButt.focus(true);
				});
			});

				
writeBufferButt = GUI.button.new(win, Rect(80, 120, 30, 18))
		.states_([["write", Color.black, Color.clear]])
		.font_(GUI.font.new("Helvetica", 9))			
		.action_({arg butt; 
			writeBuffer.value;
		})
		.keyDownAction_({arg view, key, mod, unicode; // if RETURN on bufNameView
			if(unicode == 13, {
				if(comingFromFieldFlag, {
					"not writing file".postln;
					comingFromFieldFlag = false;
				},{
					writeBuffer.value;
				})
			});
		});

writeBuffer = { 
	var mySlot, writtenPath, copiedBuffer, soundName;
	soundName = bufNameView.string;
	if(writtenBufferNames.size > 0, {
		block{arg break;	
			writtenBufferNames.do({arg name, i;
				if(name == soundName, {
					bufInBufferDictFlag = true;
					mySlot = i;
					break.value;
				}, {
					bufInBufferDictFlag = false;
				});
			});
		};
	}, { // if there is no saved buffer, then following is false:
		bufInBufferDictFlag = false;
	});
	
	writtenPath = "sounds/ixiquarks/"++soundName++".aif";
	bufNameView.string_(PathName(soundName).nextName);
	
	s.bind({	// instead of using Condition
		buffer.path = writtenPath; // for other instr
		buffer.write(writtenPath, "aiff", XQ.pref.bitDepth);
		s.sync;
		copiedBuffer = Buffer.read(s, writtenPath);
		s.sync;
		{
		if(XQ.globalBufferDict.includesKey('stratosamples'), {
			if(bufInBufferDictFlag == true, {
				XQ.globalBufferDict.at('stratosamples')[0].removeAt(mySlot);
				XQ.globalBufferDict.at('stratosamples')[0].insert(mySlot, copiedBuffer);
			},{
				writtenBufferNames.add(soundName);
				XQ.globalBufferDict.at('stratosamples')[0].add(copiedBuffer);
				XQ.globalBufferDict.at('stratosamples')[1].add([0, buffer.numFrames-1]);
			});
		}, {
			XQ.globalBufferDict.add('stratosamples' -> 
					[List[copiedBuffer], List[[0, buffer.numFrames-1]]]); // buf not loaded
			writtenBufferNames.add(soundName);
			// If we're creating this dict in the global buffer pool notify other instr
			XQ.globalWidgetList.do({arg widget;
				{ // the various widgets that receive and use bufferpools
				if(widget.isKindOf(XiiBufferPlayer), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiGrainBox), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiPredators), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiPolyMachine), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiGridder), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiSoundScratcher), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiMushrooms), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiSounddrops), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiToshioMorpher), {widget.updatePoolMenu;});
				if(widget.isKindOf(XiiSoundSculptor), {widget.updatePoolMenu;});
				}.defer;
			});
		});
		}.defer;
	});
};

volSl = OSCIISlider.new(win, Rect(10, 150, 100, 8), "- volume", 0, 1, params[4], 0.01, \amp)
		.font_(GUI.font.new("Helvetica", 9))		
		.action_({arg sl; 
			amp = sl.value;
			playSynth.set(\amp, amp);
			params[4] = sl.value;
		});

GUI.staticText.new(win, Rect(13, 178, 80, 20))
		.font_(GUI.font.new("Helvetica", 9))
		.string_("outbus :");

outBusPop = GUI.popUpMenu.new(win, Rect(60, 180, 50, 16))
		.font_(GUI.font.new("Helvetica", 9))
		.items_(XiiACDropDownChannels.getStereoChnList)
		.value_(params[5])
		.background_(Color.new255(255, 255, 255))
		.action_({ arg popup;
			outbus = popup.value * 2;
			playSynth.set(\outbus, outbus);	
			params[5] = popup.value;	
		});

drawIndexButt = OSCIIRadioButton(win, Rect(10, 208, 12, 12), "draw")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(params[6])
				.action_({arg butt;
					if(butt.value == 1, {
						startIndexDrawClock.value;
						drawIndexFlag = true;
					}, {
						indexDrawClock.stop;
						drawIndexFlag = false;
						bufPlot.setIndex_(0);
						bufPlot.redraw;
					});
					params[6] = butt.value; // settings
				});

toggleButt = GUI.button.new(win, Rect(60, 205, 12, 18))
	.states_([["o", Color.black, Color.green(alpha:0.2)], ["x", Color.black, Color.clear]])
	.font_(GUI.font.new("Helvetica", 9))			
	.action_({arg butt;
		if(butt.value == 1, {
			recSynth.set(\prelevel, 1) ;
			recSynth.set(\reclevel, 0) ;
			preLevelSl.value_(1); // not using valueAction as I want to keep params values
			recLevelSl.value_(0);			
		}, {
			recSynth.set(\prelevel, params[1]) ;
			recSynth.set(\reclevel, params[2]) ;
			preLevelSl.value_(params[1]);
			recLevelSl.value_(params[2]);			
		})
	});

startButt = GUI.button.new(win, Rect(77, 205, 33, 18))
	.states_([["start", Color.black, Color.clear],
			["stop", Color.black, Color.green(alpha:0.2)]])
	.font_(GUI.font.new("Helvetica", 9))			
	.action_({arg butt;
		if(butt.value == 1, {
			if(startingPlayFlag == false, {
				startPlayFunc.value;
			});
		},{
			startingPlayFlag = false; // 
			recSynth.free;
			if(runningFlag, { playSynth.free });
			refreshClock.stop;
			indexDrawClock.stop;
			bufPlot.setIndex_(0);
			runningFlag = false;
			bufPlot.redraw;
		});
	});
	
startPlayFunc = {
	startingPlayFlag = true;
	 bufSec = ((buffer.numFrames/buffer.numChannels)/s.sampleRate)+0.02; // the lookahead time in Limiter
	{bufPlot.redraw}.defer;
	 fork{
		 time = Main.elapsedTime;
		recSynth = Synth(\xiiStratoSamplerRec, 
					[\inbus, inbus, \bufnum, buffer.bufnum, 
					\prelevel, prelevel, \reclevel, reclevel], 
					addAction:\addToTail);
		if(drawIndexFlag == true, { startIndexDrawClock.value; });
		bufSec.wait;
		if(startingPlayFlag, { // still starting? i.e. user hasn't changed his mind and stopped
			runningFlag = true;
			playSynth = Synth(\xiiStratoSamplerPlay, 
						[\bufnum, buffer.bufnum , \endloop, buffer.numFrames, 
						\amp, amp, \outbus, outbus], addAction:\addToTail);
			refreshClock = Task({ 
						inf.do({ 
							{
								win.isClosed.not.if({ 
									bufPlot.redraw;
								});
							}.defer; 
							bufSec.wait;
						}) 
					}).play;
		});
		startingPlayFlag = false; // we have started
	};
};

startIndexDrawClock = {
	indexDrawClock = Task({		 
		inf.do({
			var ind;
			ind = ((Main.elapsedTime-time)*s.sampleRate)%(buffer.numFrames);
			{
				win.isClosed.not.if({				
					bufPlot.setIndex_(ind/(64.852*bufSec)) 
				});	
			}.defer;
			0.1.wait;
		});
	}).play;
};

win.front;
win.onClose_({
	var t;
	refreshClock.stop;
	indexDrawClock.stop;
	buffer.free;
	// this.removeMyInstance; // off, because I want livebufferNumbers to increase
	try{
		recSynth.free;
		playSynth.free;
	};
	
	XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
	try{XQ.globalWidgetList.removeAt(t)};
});

// setting
//inBusPop.valueAction_(params[0]);
//preLevelSl.valueAction_(params[1]);
//recLevelSl.valueAction_(params[2]);
//newClearBufButt.valueAction_(params[3]);
//volSl.valueAction_(params[4]);
//outBusPop.valueAction_(params[5]);


//////////////		

	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}
}