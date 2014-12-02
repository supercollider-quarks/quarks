XiiMushrooms {

	var ldSndsGBufferList, selbPool;
	var <>xiigui, <>win, params;

	*new { arg server, channels, setting = nil; 
		if(server.serverRunning, {
			^super.new.initXiiMushrooms( server, channels, setting);
		}, {
			XiiAlert("Please boot the server!");
		});
	}
	
	initXiiMushrooms { arg server, channels, setting; 

////////////////////

var s, time, point;
var outBusPop, outbus, inbus;
var update, bufSec, thresh, threshSl, mingap, mingapSl;
var startIndexDrawClock, indexDrawClock, refreshClock, drawIndexButt, drawIndexFlag;
var synth, startPlayFunc;
var volSl, amp, startButt;
var bufLengthBox, newClearBufButt;
var bufNameView, writeBufferButt;
var bufNuminDict, buffer, bufPlot;
var bufferPop, bufferList, sndNameList, myBuffer, soundfile;
var poolName, myTempBuffer, onsetFramesList, onsetWaits, timeSynthTask, fftbuf, trackbuf;
var timeRadioButt, fftRadioButt, analysisType, oscResponder,  currentTime, fftOnsets;
var rate, rateSl, onsetVolSl, funcamp;
var analyseBufButt, analysisSynth;
var soundFuncPop, playFunc, setPlayFunc;
var freqAnalysisFlag, synthRunningFlag, freq;
var createAudioStreamBusWin, createCodeWin, synthDefPrototype, synthDefInUse, chooseBufferWin;
var myPlayBuf, onsetSelStart, onsetSelEnd;
var audiostreamwin, bufferwin;

s = server; //Server.default;

thresh = 0.25;
mingap = 5;

xiigui = nil;
point = if(setting.isNil, {Point(100, 100)}, {setting[1]});
params = if(setting.isNil, {[1, 0.25, 5, 1, 0, 1, 1, 1, 0, 1]}, {setting[2]});

freq = 440;
funcamp = 1.0;
amp = 1.0;
drawIndexFlag = true;
freqAnalysisFlag = false;
inbus = 20;
outbus = 0;
bufNuminDict = 0;
rate = 1;
analysisType = \time; // time or fft;
synthRunningFlag = false;

fftbuf = Buffer.alloc(s, 512);
trackbuf = Buffer.alloc(s, 512);

win = GUI.window.new("- mushrooms -", Rect(point.x, point.y, 820, 320), resizable: false);

// just display a temp buf in the GUI so it's not empty
{bufPlot = XiiBufferOnsetPlot.new(fftbuf, win, Rect( 120, 5, 680, 300))}.defer(1);

synthDefPrototype = 
{SynthDef(\xiiMushroom, {arg outbus=0, freq=440, pan=0, amp=1;
	var env, sine;
	env = EnvGen.ar(Env.perc, doneAction:2);
	sine = SinOsc.ar(freq, 0, env*amp);
	Out.ar(outbus, Pan2.ar(sine, pan));
}).play(Server.default)}.asCompileString;


oscResponder =  OSCresponderNode(s.addr,'/tr',{ arg thistime, responder, msg;
	if(msg[2] == 840, {
		if(freqAnalysisFlag, {	// analysing sound
			fftOnsets.add( Point(	120+((thistime-time)/((myTempBuffer.numFrames/s.sampleRate)/680)), 
								305-(msg[3]*550)) );  
		}, {	// playing sound
			freq = 200+(msg[3]*1000);
			playFunc.value;
		});
	});
}).add;


		selbPool = GUI.popUpMenu.new(win, Rect(10, 10, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var checkBufLoadTask;
				ldSndsGBufferList.value(selbPool.items[item.value]); // sending name of pool
			});

		bufferPop = GUI.popUpMenu.new(win, Rect(10, 32, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["no buffer"])
				.background_(Color.white)
				.action_({ arg popup; 
					var filepath, selStart, selNumFrames, checkBufLoadTask, restartPlayPath;
					restartPlayPath = false;
					startButt.valueAction_(0); // stop the synth if playing
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
					myTempBuffer.free; // free the old temp buf
					filepath = XQ.globalBufferDict.at(poolName)[0][popup.value].path;
					selStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
					selNumFrames =  XQ.globalBufferDict.at(poolName)[1][popup.value][1]-1;

					soundfile = SoundFile.new;
					soundfile.openRead(filepath);
					myBuffer = XQ.globalBufferDict.at(poolName)[0][popup.value];
					// create a mono buffer if the sound is stereo
					if(soundfile.numChannels == 2, {
				myTempBuffer = Buffer.readChannel(s, filepath, selStart, selNumFrames, [0]);
					}, {
					// and make a right size buffer if only part of a mono file is selected
				myTempBuffer = Buffer.read(s, filepath, selStart, selNumFrames);
					});
					bufPlot.remove;
					soundfile.close;
					{bufPlot = XiiBufferOnsetPlot.new(myTempBuffer, win, Rect( 120, 5, 680, 300))}.defer(1);
				}, {
					XiiAlert("You need to load a buffer for this app to work");
				});
 			});
				
		ldSndsGBufferList = {arg argPoolName, firstpool=false;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
				 });
				 bufferPop.items_(sndNameList);
				 // not sure I want to change file
				 if(firstpool, {bufferPop.action.value(0)}); 
			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);

		soundFuncPop = GUI.popUpMenu.new(win, Rect(10, 54, 100, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["sample", "sine", "bells", "sines", "synth1", "ks_string", 
				"ixi_string", "impulse", "ringz", "klanks", "scode", "audiostream"])
				.background_(Color.new255(255, 255, 255))
				.value_(1)
				.action_({ arg popup;
					if(soundFuncPop.items[popup.value] == "sample", {
						chooseBufferWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "scode", {
						createCodeWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "audiostream", {
						createAudioStreamBusWin.value;
					}); 
					setPlayFunc.value(popup.value);
					params[0] = popup.value;
				});

threshSl = OSCIISlider.new(win, Rect(10, 76, 100, 8), "- thresh", 0.0, 1.0, 0.25, 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				thresh = sl.value;
				if(synthRunningFlag, { synth.set(\thresh, thresh) });
				params[1] = thresh;
			});

mingapSl = OSCIISlider.new(win, Rect(10, 102, 100, 8), "- minGap", 0.0, 10.0, 5.0, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
				mingap = sl.value;
				params[2] = mingap;
			});

timeRadioButt = OSCIIRadioButton(win, Rect(10, 130, 11, 11), "Time")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(1)
				.action_({arg butt;
					if(butt.value == 1, {
						analysisType = \time;
					}, {
						analysisType = \fft;
					});
					fftRadioButt.switchState;
					params[3] = butt.value;
				});

fftRadioButt = OSCIIRadioButton(win, Rect(10, 145, 11, 11), "FFT")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(0)
				.action_({arg butt;
					if(butt.value == 1, {
						analysisType = \fft;
					}, {
						analysisType = \time;
					});
					timeRadioButt.switchState;
					params[4] = butt.value;
				});

analyseBufButt = GUI.button.new(win, Rect(60, 130, 50, 18))
		.states_([["analyse", Color.black, Color.clear]])
		.font_(GUI.font.new("Helvetica", 9))			
		.action_({arg butt; var displayBufPlot;
			startButt.valueAction_(0);
			if(analysisType == \time, { // calculating Onsets from analysing Buffer Array
				bufPlot.redraw(thresh, mingap);
				fork{0.3.wait;
				{bufPlot.drawOnsets}.defer;
				1.wait;
				onsetFramesList = bufPlot.getOnsetTimesList;
				onsetFramesList.add((myTempBuffer.numFrames/s.sampleRate));
				// convert times into waits
				onsetWaits = onsetFramesList.collect({arg item, i; 
								if(i==0, 
									{item}, 
									{item-onsetFramesList[i-1]})
							});
				}
			}, { // Detecting Onsets through playing using FFT
				if(freqAnalysisFlag == false, {
					fftOnsets = List.new;
					freqAnalysisFlag = true;
					time = Main.elapsedTime;
					analysisSynth = Synth(\xiiMushFFT, 
							[\thresh, thresh, \bufnum, myTempBuffer.bufnum, 
							\endloop, myTempBuffer.numFrames,
							\fftbuf, fftbuf.bufnum, \trackbuf, trackbuf.bufnum, \amp, amp]);
					if(drawIndexFlag == true, { startIndexDrawClock.value; });
				SystemClock.sched(myTempBuffer.numFrames/s.sampleRate, {
						analysisSynth.free; 
						freqAnalysisFlag = false;
						indexDrawClock.stop;
						{bufPlot.drawFFTMushrooms(fftOnsets)}.defer;
						nil
					});
				});
			});
		});

rateSl = OSCIISlider.new(win, Rect(10, 166, 100, 8), "- rate", -2, 2, 1, 0.01)
		.font_(GUI.font.new("Helvetica", 9))
		.action_({arg sl; 
			rate = sl.value;
			if(synthRunningFlag, { synth.set(\rate, rate) });
			params[5] = rate;
		});

volSl = OSCIISlider.new(win, Rect(10, 192, 100, 8), "- volume", 0, 1, 1, 0.01, \amp)
		.font_(GUI.font.new("Helvetica", 9))
		.action_({arg sl; 
			amp = sl.value;
			if(freqAnalysisFlag, { analysisSynth.set(\amp, amp) });
			if(synthRunningFlag, { synth.set(\amp, amp) });
			params[6] = amp;
		});

onsetVolSl = OSCIISlider.new(win, Rect(10, 218, 100, 8), "- onsetvol", 0, 1, 1, 0.01, \amp)
		.font_(GUI.font.new("Helvetica", 9))
		.action_({arg sl; 
			funcamp = sl.value;  // the amplitude of the soundFunc synths
			params[7] = funcamp;
		});

GUI.staticText.new(win, Rect(13, 260, 80, 20))
		.font_(GUI.font.new("Helvetica", 9))
		.string_("outbus :");

outBusPop = GUI.popUpMenu.new(win, Rect(60, 260, 50, 16))
		.font_(GUI.font.new("Helvetica", 9))
		.items_(XiiACDropDownChannels.getStereoChnList)
		.background_(Color.new255(255, 255, 255))
		.action_({ arg popup;
			outbus = popup.value * 2;
			if(synthRunningFlag, { synth.set(\outbus, outbus) });
			params[8] = popup.value;
		});

drawIndexButt = OSCIIRadioButton(win, Rect(10, 288, 12, 12), "draw")
				.font_(GUI.font.new("Helvetica", 9))
				.value_(1)
				.action_({arg butt;
					if(butt.value == 1, {
						startIndexDrawClock.value;
						drawIndexFlag = true;
					}, {
						indexDrawClock.stop;
						drawIndexFlag = false;
						try{bufPlot.setIndex_(0)};
					});
					params[9] = butt.value;
				});

startButt = GUI.button.new(win, Rect(60, 284, 50, 18))
	.states_([["play", Color.black, Color.clear],
			["stop", Color.black, Color.green(alpha:0.2)]])
	.font_(GUI.font.new("Helvetica", 9))			
	.action_({arg butt;		
		if(butt.value == 1, {
			startPlayFunc.value;
		},{
			synth.free;
			refreshClock.stop;
			indexDrawClock.stop;
			timeSynthTask.stop;
			synthRunningFlag = false;
		});
	});

startPlayFunc = {
	var onsetsList;
	onsetsList = bufPlot.getOnsetsList;
	 bufSec = (myTempBuffer.numFrames/myTempBuffer.numChannels)/s.sampleRate;
	 time = Main.elapsedTime;
	if(freqAnalysisFlag == false, {
		if(drawIndexFlag == true, { startIndexDrawClock.value; });
		synthRunningFlag = true;
		if(analysisType == \time, {	// TIME DOMAIN ANALYSIS SYNTH
			synth = Synth(\xiiMushTime, 
						[\bufnum, myTempBuffer.bufnum, 
						\endloop, myTempBuffer.numFrames, 
						\amp, amp, \rate, rate, 
						\outbus, outbus], addAction:\addToTail);
		
			timeSynthTask = Task({
				if(onsetWaits.size > 1, {
					inf.do({
						if(rate.isPositive, {
							onsetWaits.do({arg onsetwait, i;
								(rate.reciprocal.abs * onsetwait).wait;
								if(i != (onsetWaits.size-1), {
									freq = (330-onsetsList[i+1].y)+200;
									playFunc.value;
								 });
							})
						}, {
							onsetWaits.reverse.do({arg onsetwait, i;
								(rate.reciprocal.abs * onsetwait).wait;
								if(i != (onsetWaits.size-1), {
									freq = (330-onsetsList[i+1].y)+200;
									playFunc.value;
								 });
							})
						});
					});
				});
			}).start;
		}, {   // FREQUENCY DOMAIN ANALYSIS SYNTH
			synth = Synth(\xiiMushFFT, 
					[\bufnum, myTempBuffer.bufnum, \endloop, myTempBuffer.numFrames, 
					\startloop, 0, \amp, amp, \thresh, thresh, \rate, rate,
					\fftbuf, fftbuf.bufnum, \trackbuf, trackbuf.bufnum,
					\outbus, outbus], addAction:\addToTail);
		
		});
	});
};

startIndexDrawClock = {
	indexDrawClock = Task({
		0.1.wait;
		inf.do({
			var ind;
			ind = (rate * ((Main.elapsedTime-time)*s.sampleRate)%(myTempBuffer.numFrames));
			{try{bufPlot.setIndex_(ind/(64.852*bufSec))}}.defer;
			0.1.wait;
		});
	}).play;
};

	setPlayFunc = {arg funcnr=0;
		playFunc = switch (funcnr,
			0, { {arg rate; 
				if(try{XQ.globalBufferDict.at(poolName)[0][0]} != nil, {
						if(myBuffer.numChannels == 1, {
						Synth(\xiiMush1x2, [	\outbus, outbus,
											\bufnum, myPlayBuf.bufnum, 
											\startPos, onsetSelStart, 
											\endPos, onsetSelEnd,
											\vol, funcamp
						])
					},{
						Synth(\xiiMush2x2, [	\outbus, outbus,
											\bufnum, myPlayBuf.bufnum, 
											\startPos, onsetSelStart, 
											\endPos, onsetSelEnd,
											\vol, funcamp
						])
					});
				});
			} },
			1, { {
					Synth(\xiiSine, [		\outbus, outbus,
										\freq, freq,
										\phase, 1.0.rand,
										\amp, funcamp
					])
			} },
			2, { {
					Synth(\xiiBells, [		\outbus, outbus,
										\freq, freq,
										\amp, funcamp
					])
			} },
			3, { {
					Synth(\xiiSines, [		\outbus, outbus,
										\freq, freq,
										\amp, funcamp
					])
			} },
			4, { {
					Synth(\xiiSynth1, [	\outbus, outbus,
										\freq, freq,
										\amp, funcamp
					])
			} },
			5, { {
					Synth(\xiiKs_string, [	\outbus, outbus,
										\note, freq, 
										\pan, 0.7.rand2, 
										\rand, 0.1+0.1.rand, 
										\delayTime, 2+1.0.rand,
										\amp, funcamp
										]);
			} },
			6, { {
					Synth(\xiiString, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2, 
										\amp, funcamp
										]);
			} },
			7, { {
					Synth(\xiiImpulse, [	\outbus, outbus,
										\pan, 0.7.rand2,
										\amp, funcamp
										]);
			} },
			8, { {
					Synth(\xiiRingz, [		\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, funcamp
										]);
			} },
			9, { {
					Synth(\xiiKlanks, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, funcamp
										]);
			} },
			10, { {
					Synth(\xiiMushroom, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, funcamp
										]);
			} },
			11, { { // the audio stream
					Synth(\xiiAudioStream,[	\outbus, outbus,
										\inbus, inbus,
										\pitchratio, 1, 
										\pan, 0.7.rand2,
										\amp, funcamp
										]).setn(
											\levels, [0, 0.0, 1.0, 0.8, 0.0], 
											\times, [0.0, 0.01, 0.1, 0.162]										);
			} }
			)
	};

setPlayFunc.value(1);

	createCodeWin = {
		var funcwin, func, subm, test, view;
		funcwin = GUI.window.new("scode", Rect(600,300, 440, 200)).front;
		funcwin.alwaysOnTop = true;
		
		view = funcwin.view;
		func = GUI.textView.new(view, Rect(20, 10, 400, 140))
				.font_(GUI.font.new("Monaco", 9))
				.resize_(5)
				.focus(true)
				.string_(
					if(synthDefInUse.isNil, { 
						synthDefPrototype
					},{
						synthDefInUse
					});
				);
		test = GUI.button.new(view, Rect(280,160,50,18))
				.states_([["test",Color.black,Color.clear]])
				.resize_(9)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					func.string.interpret.value;
				});
				
		subm = GUI.button.new(view, Rect(340,160,50,18))
				.states_([["submit",Color.black,Color.clear]])
				.resize_(9)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					func.string.interpret;
					synthDefInUse = func.string;
					funcwin.close;
				});

		};
		
		createAudioStreamBusWin = {
			var envview, timesl, setButt;
			audiostreamwin = GUI.window.new("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
			audiostreamwin.alwaysOnTop = true;
				
			GUI.staticText.new(audiostreamwin, Rect(20, 55, 20, 16))
				.font_(GUI.font.new("Helvetica", 9)).string_("in"); 

			GUI.popUpMenu.new(audiostreamwin, Rect(35, 55, 50, 16))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.value_(10)
				.font_(GUI.font.new("Helvetica", 9))
				.background_(Color.white)
				.canFocus_(false)
				.action_({ arg ch; var inbus;
					inbus = ch.value * 2;
				});

			setButt = GUI.button.new(audiostreamwin, Rect(120, 55, 60, 16))
					.states_([["set inbus", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						audiostreamwin.close;
					});
		};

		chooseBufferWin = {			
			var selbPool, bufferPop, ldSndsGBufferList, setButt, poolName;
			bufferwin = GUI.window.new("Choose Buffer", Rect(200, 450, 200, 66), resizable:false).front;
			bufferwin.alwaysOnTop = true;

		selbPool = GUI.popUpMenu.new(bufferwin, Rect(10, 10, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(XQ.globalBufferDict.keys.asArray == [], 
					{["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
			.value_(0)
			.background_(Color.white)
			.action_({ arg item;
				ldSndsGBufferList.value(selbPool.items[item.value]); // sending name of pool
				bufferPop.valueAction_(0);
			});

		bufferPop = GUI.popUpMenu.new(bufferwin, Rect(10, 32, 100, 16)) // 550
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["no buffer"])
				.background_(Color.white)
				.action_({ arg popup; 						
					if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
						myPlayBuf = XQ.globalBufferDict.at(poolName)[0][popup.value];
						onsetSelStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
						onsetSelEnd = onsetSelStart + XQ.globalBufferDict.at(poolName)[1][popup.value][1]-1;
	
					});	
				});
				
		ldSndsGBufferList = {arg argPoolName; var sndNameList;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
					if(buffer.path.notNil, {
						sndNameList = sndNameList.add(buffer.path.basename);
					},{
						sndNameList = sndNameList.add("liveBuffer "++i.asString);
					});
				 });
				 bufferPop.items_(sndNameList);
				 bufferPop.action.value(0); // put the first file into the view and load buffer
			}, {
				"got no files".postln;
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);

		setButt = GUI.button.new(bufferwin, Rect(120, 32, 60, 16))
					.states_([["set buffer", Color.black, Color.clear]])
					.focus(true)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({
						bufferwin.close;
					});
		};

win.front;
win.onClose_({
	var t;
	refreshClock.stop;
	indexDrawClock.stop;
	myTempBuffer.free; // free the temporary buffer
	synth.free;
	analysisSynth.free;
	timeSynthTask.stop;
	oscResponder.remove;
	XQ.globalWidgetList.do({arg widget, i; if(widget == this, { t = i })});
	try{XQ.globalWidgetList.removeAt(t)};
	try{ audiostreamwin.close };
	try{ bufferwin.close };

});

// setting
soundFuncPop.valueAction_(params[0]);
threshSl.valueAction_(params[1]);
mingapSl.valueAction_(params[2]);
timeRadioButt.valueAction_(params[3]);
fftRadioButt.valueAction_(params[4]);
rateSl.valueAction_(params[5]);
volSl.valueAction_(params[6]);
onsetVolSl.valueAction_(params[7]);
outBusPop.valueAction_(params[8]);
drawIndexButt.valueAction_(params[9]);

///////////////////
	}

/*	
	updatePoolMenu {
		var pool, poolindex;
		pool = selbPool.items.at(selbPool.value);  
		selbPool.items_(XQ.globalBufferDict.keys.asArray); 
		poolindex = selbPool.items.indexOf(pool);
		if(poolindex != nil, {
			selbPool.value_(poolindex);
		});
	}
*/
	
	updatePoolMenu {
		var poolname, poolindex;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		if(poolindex != nil, {
			selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
			ldSndsGBufferList.value(poolname);
		}, {
			selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // load first pool
		});
	}

	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

	
}