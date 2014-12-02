
// WARNING: THIS CODE IS MANY YEARS OLD! IT IS A PATCH CONVERTED INTO A CLASS. BAD CODING AHEAD!
// Oh Dios!

XiiGrainBox {
	var <>xiigui, <>win, params;

	var selbPool, ldSndsGBufferList, sndNameList, fileListPopup, gBufferPoolNum;
	var poolName, envViewList;
	
	*new {arg server, channels, setting = nil;
		^super.new.initXiiGrainBox(server, channels, setting);
	}

	initXiiGrainBox {arg server, channels, setting;
	
var w, envView, nPoints, xLoc, yLoc, boxLocList, synth, ly, lx, synthsList, bufferList, 
popup, openFile, sndNameTextField, sndNmeFieldList, sndNList, sampleDurList, cpu, popupOutBus;
var xfield, yfield, infoFunc, infoButt, record, recorder, s, r, b;
var startStopButt, randCntrPos;
var paraDict1, paraDict2, getCenterPos, selbox, p;
var cmdPeriodFunc, outbusarray, channelbuffers, point, glVolSlider;

envViewList = List.new;
gBufferPoolNum = 0;
bufferList = List.new; // contains bufnums of buffers (not buffers)
sndNameList = [];
sampleDurList = List.new;
randCntrPos = [];
channelbuffers = [nil, nil];
paraDict1 = ( 'delayTime': 4.55, 'cntrPosRandWidth': 0.01, 'trigRate': 7.49, 'amp': 0.45, 
  'rateRandFreq': 1.96, 'centerPos': 0.79, 'dur': 0.11, 'cntrPosRandFreq': 1.25, 'revVol': 0.03, 
  'decayTime': 1.51, 'aDelTime': 2.97, 'durRandWidth': 0.02, 'pan': 0.41, 'rateRandWidth': 0.03, 
  'aDecTime': 2.18, 'freq': 0.98, 'durRandFreq': 1.17 ); // a dictionary for updated parameters
paraDict2 = ( 'delayTime': 4.55, 'cntrPosRandWidth': 0.01, 'trigRate': 7.75, 'amp': 0.45, 
  'rateRandFreq': 1.79, 'centerPos': 0.36, 'dur': 0.11, 'cntrPosRandFreq': 1.92, 'revVol': 0.03, 
  'decayTime': 1.34, 'aDelTime': 2.97, 'durRandWidth': 0.02, 'pan': 0.41, 'rateRandWidth': 0.03, 
  'aDecTime': 1.93, 'freq': 1, 'durRandFreq': 1.79 ); // a dictionary for updated parameters

s = server;
channels = 2; // how many granular channels are there?

synthsList = List.new;
fileListPopup = List.new;

p = [
Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
Point(24,43), Point(7,43), Point(1,36), Point(1,8)
];

xiigui = nil;
point = if(setting.isNil, {Point(100, 130)}, {setting[1]});
params = if(setting.isNil, {[1, [0,0]]}, {setting[2]});
outbusarray = params[1];

win = GUI.window.new("- grainbox -", Rect(point.x, point.y, 756, 300), resizable:false);
win.drawHook = {
	GUI.pen.color = Color.new255(255, 100, 0);
	GUI.pen.width = 3;
	GUI.pen.translate(45,210);
	GUI.pen.scale(0.4,0.4);
	GUI.pen.moveTo(1@7);
	p.do({arg point;
		GUI.pen.lineTo(point+0.5);
	});
	GUI.pen.stroke;
};



getCenterPos = { arg envval, i; var spec, myvar, sampledur, file;
	if(sampleDurList.size > 0, { 
	 	file = fileListPopup[i].value;
		 sampledur = sampleDurList[file][1]-sampleDurList[file][0];
		// sampleDurList comes from XQ and is therefore with a start and end value of selection
		spec = [0, sampledur, \linear, 0.01, randCntrPos[i]].asSpec;
		myvar = spec.map(envval);// horizontal
	}, {	myvar = nil;});
	myvar;
};

nPoints = 10; // how many boxes (or nodes in the envelope)
channels.do({arg i; var sampleInSec, r, c; // LOOP

	if(try{sampleDurList.at(i)} != nil, {
		sampleInSec = sampleDurList.at(i)[1] - sampleDurList.at(i)[0];
		randCntrPos = randCntrPos.add(sampleInSec / (4.0.rand));
	}, {
		randCntrPos = randCntrPos.add(1.0.rand)
	});

selbPool = GUI.popUpMenu.new(win, Rect(10, 5, 102, 16))
	.font_(GUI.font.new("Helvetica", 9))
	.items_(if(XQ.poolNames == [], {["no pool", "no pool 2"]}, {XQ.poolNames}))
	.value_(1)
	.background_(Color.white)
	.action_({ arg item; var dictArray;
		var filepath, selStart, selEnd, soundfile, myTempBuffer;
		poolName = selbPool.items[item.value];
		ldSndsGBufferList.value(poolName.asSymbol);
		//fileListPopup[0].items_(sndNameList);
		//fileListPopup[1].items_(sndNameList);
		//fileListPopup[1].value = 1;
	});
	if(sndNameList == [], {
		selbPool.items_(["no bufferPool", "no bufferpool"]);
	});

// ===================== ENVELOPE VIEW CODE ==============================
envViewList.add(
envView = GUI.envelopeView.new(win, Rect(125+(i*316), 5, 300, 250))
			.thumbSize_(8.0)
			.fillColor_(Color.clear)
			.background_(Color.new255(255, 255, 255, 155))
			.drawLines_(false)
			.selectionColor_(Color.white)
			.drawRects_(true)
			.canFocus_(false)
			.action_({arg envView; var box;	
	
	box = envView.index;
	selbox = box; // select the box for key updates
	
	Set[
	{box == 0} -> {var spec, myvar, spec2, myvar2; 
		 			spec = [1, 60, \exponential, 0.01, 10].asSpec;  // freq
					myvar = spec.map(envView.value.at(1).at(box));					spec2 = [0.1, 1.5, \exponential, 0.01, 0.4].asSpec; // snd length or dur
					myvar2 = spec2.map(envView.value.at(0).at(box));
					synthsList.at(i).set(\trigRate, myvar, \dur, myvar2);
					xfield.string_("grainDur");
					yfield.string_("trigRate");
					if(i == 0, {
					paraDict1.add(\trigRate -> myvar);
					paraDict1.add(\dur -> myvar2);
					}, {
					paraDict2.add(\trigRate -> myvar);
					paraDict2.add(\dur -> myvar2);
					});
					},
	{box == 1} -> {var spec, myvar; 
					spec = [-2, 2, \linear, 0.01, 1].asSpec;
					myvar = spec.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\freq, myvar);
					xfield.string_("-");
					yfield.string_("pitch");
					if(i == 0, {
					paraDict1.add(\freq -> myvar)
					},{
					paraDict2.add(\freq -> myvar)
					});
					},
	{box == 2} -> {var spec, myvar, dur;
					myvar = getCenterPos.value(envView.value.at(0).at(box), i);
					if(myvar != nil, {synthsList.at(i).set(\centerPos, myvar)});
					xfield.string_("centrePos");
					yfield.string_("-");
					if(i == 0, {
					paraDict1.add(\centerPos -> myvar)
					},{
					paraDict2.add(\centerPos -> myvar)
					});
					
					},
	{box == 3} -> {var spec, myvar, spec2, myvar2;
					spec = [0.01, 1.0, 0, 0.01, 0.4].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0, 0.9, 0, 0.01, 0.1].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\pan, myvar, \amp, myvar2);
					xfield.string_("grainPan");
					yfield.string_("amplitude");
					if(i == 0, {
					paraDict1.add(\pan -> myvar);
					paraDict1.add(\amp -> myvar2);
					},{
					paraDict2.add(\pan -> myvar);
					paraDict2.add(\amp -> myvar2);
					})
					},
	{box == 4} -> {var spec, myvar, spec2, myvar2;
					spec = [0, 0.4, 0, 0.01, 0].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0, 10, 0, 0.01, 0.1].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\cntrPosRandWidth, myvar, \cntrPosRandFreq, myvar2);
					xfield.string_("rCentrWidth");
					yfield.string_("rCentrFreq");
					if(i == 0, {
					paraDict1.add(\cntrPosRandWidth -> myvar);
					paraDict1.add(\cntrPosRandFreq -> myvar2);
					},{
					paraDict2.add(\cntrPosRandWidth -> myvar);
					paraDict2.add(\cntrPosRandFreq -> myvar2);
					});
					},
	{box == 5} -> {var spec, myvar, spec2, myvar2;
					spec = [0, 0.4, 0, 0.01, 0].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0, 10, 0, 0.01, 0.1].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\durRandWidth, myvar, \durRandFreq, myvar2);
					xfield.string_("rWidthDur");
					yfield.string_("rFreqDur");
					if(i == 0, {
					paraDict1.add(\durRandWidth -> myvar);
					paraDict1.add(\durRandFreq -> myvar2);
					},{
					paraDict2.add(\durRandWidth -> myvar);
					paraDict2.add(\durRandFreq -> myvar2);
					})
					},
	{box == 6} -> {var spec, myvar, spec2, myvar2;
					spec = [0, 0.3, 0, 0.01, 0].asSpec;
					myvar = spec.map(envView.value.at(1).at(box)); // y axis here!
					synthsList.at(i).set(\revVol, myvar);
					xfield.string_("-");
					yfield.string_("reverbVol");
					if(i == 0, {
					paraDict1.add(\revVol -> myvar);
					},{
					paraDict2.add(\revVol -> myvar);
					})
					},
	{box == 7} -> {var spec, myvar, spec2, myvar2;
					spec = [0.01, 6.0, 0, 0.01, 0.1].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0.01, 10, 0, 0.01, 4.0].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\delayTime, myvar, \decayTime, myvar2);
					xfield.string_("delTimeRev");
					yfield.string_("decTimeRev");
					if(i == 0, {
					paraDict1.add(\delayTime -> myvar);
					paraDict1.add(\decayTime -> myvar2);
					},{
					paraDict2.add(\delayTime -> myvar);
					paraDict2.add(\decayTime -> myvar2);
					})
					},
	{box == 8} -> {var spec, myvar, spec2, myvar2;
					spec = [0.01, 10, 0, 0.01, 0.1].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0.01, 20, 0, 0.01, 0.1].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\aDelTime, myvar, \aDecTime, myvar2);
					xfield.string_("allpDelTime");
					yfield.string_("allpDecTime");
					if(i == 0, {
					paraDict1.add(\aDelTime -> myvar);
					paraDict1.add(\aDecTime -> myvar2);
					},{
					paraDict2.add(\aDelTime -> myvar);
					paraDict2.add(\aDecTime -> myvar2);
					})
					},
	{box == 9} -> {var spec, myvar, spec2, myvar2;
					spec = [0, 0.3, 0, 0.01, 0].asSpec;
					myvar = spec.map(envView.value.at(0).at(box));
					spec2 = [0, 10, 0, 0.01, 0.1].asSpec;
					myvar2 = spec2.map(envView.value.at(1).at(box));
					synthsList.at(i).set(\rateRandWidth, myvar, \rateRandFreq, myvar2);
					xfield.string_("randWidthPitch");
					yfield.string_("randFreqPitch");
					if(i == 0, {
					paraDict1.add(\rateRandWidth -> myvar);
					paraDict1.add(\rateRandFreq -> myvar2);
					},{
					paraDict2.add(\rateRandWidth -> myvar);
					paraDict2.add(\rateRandFreq -> myvar2);
					})
					}
	].case;
	});
);

xLoc = Array.newClear(0);
yLoc = Array.newClear(0); 
boxLocList = Array.newClear(0);

// make locations for the boxes
lx = [0.02, 0.25+0.2.rand, 0.1+0.9.rand, 0.4, 0.03, 0.05, 0.5, 0.76, 0.3, 0.1];
ly = [0.50, 0.75, 0.2, 0.4+0.1.rand, 0.1+0.3.rand, 0.1+0.3.rand, 0.1, 0.1+0.1.rand, 0.1, 0.1+0.2.rand]; // the y locations list

nPoints.do({arg i;
	xLoc = xLoc.add(lx.at(i));
	yLoc = yLoc.add(ly.at(i));
	});
boxLocList = Array.with(xLoc,yLoc);
//envView.value_(boxLocList);

if(setting.isNil, {envView.value_(boxLocList);}, {envView.value_(params[2+i]);});

c = [
Color.new255(255, 140, 0), // Orange
Color.new255(225, 52, 54), // Red
Color.new255(70, 100, 155), // Blue
Color.new255(238, 238, 0),  // Yellow
Color.new255(70, 100, 155, 122), // Blue alpha
Color.new255(205, 120, 0, 122), // Orange alpha
Color.new255(70, 155, 100), // SeaGreen
Color.new255(127, 65, 138, 122), // greenish alpha 
Color.new255(46, 139, 127, 122), // greenish alpha
Color.new255(205, 52, 54, 122) // Red alpha
];
nPoints.do({arg i; envView.setFillColor(i,c.at(i)) });

envView.setThumbSize(0, 10);
envView.setThumbSize(1, 10);
envView.setThumbSize(2, 10);
envView.setThumbSize(3, 10);
envView.setThumbSize(6, 10);
envView.connect(2, [4.0]);
envView.connect(0, [5.0]);
envView.connect(6, [7.0, 8.0]);
envView.connect(1, [9.0]);
envView.drawLines_(true);
envView.select(-1); 

// ================================ END OF ENVELOPE VIEW CODE ======================

fileListPopup.add(GUI.popUpMenu.new(win,Rect(190+(i*316), 264, 140, 18))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(sndNameList)
			.background_(Color.new255(255, 255, 255))
			.value_(i)
			.action_({ arg popup; var filepath, soundfile, selStart, selEnd;
				var checkBufLoadTask, myTempBuffer;
				filepath = XQ.globalBufferDict.at(poolName)[0][popup.value].path;
				selStart = XQ.globalBufferDict.at(poolName)[1][popup.value][0];
				selEnd = selStart + XQ.globalBufferDict.at(poolName)[1][popup.value][1]-1;
				soundfile = SoundFile.new;
				soundfile.openRead(filepath);
				//myBuffer = XQ.globalBufferList[gBufferPoolNum][0][popup.value];
				if(soundfile.numChannels == 2, {
				//myTempBuffer = Buffer.readChannel(s, filepath, 0, soundfile.numFrames, [0]);
					myTempBuffer = Buffer.readChannel(s, filepath, selStart, selEnd, [0]);
				}, {
				// and make a right size buffer if only part of file is selected
					myTempBuffer = Buffer.read(s, filepath, selStart, selEnd);
				});
				channelbuffers.at(i, myTempBuffer);
				soundfile.close;
				checkBufLoadTask = Task({
						inf.do({
							if(myTempBuffer.numChannels != nil, {
								synthsList.at(i).set(\buffer, myTempBuffer.bufnum);
								checkBufLoadTask.stop;
							});
							0.1.wait;
						});
					}).start;
				});
		);

popupOutBus = GUI.popUpMenu.new(win,Rect(127+(i*316), 264,50,18))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(XiiACDropDownChannels.getStereoChnList)
				.background_(Color.new255(255, 255, 255))
				.value_(outbusarray[i]/2)  // divide the array by 2 because of stereo busses
				.action_({ arg popup; var outbus;
						outbus = popup.value * 2;
						outbusarray[i] = outbus;
						synthsList.at(i).set(\out, outbus);
						params[1] = outbusarray; // put both in here
				});
	
});
// ============================ END OF CHANNEL FUNC ==================================



// -----------------------------------------------

startStopButt = GUI.button.new(win,Rect(10, 30, 102, 18))
			.states_([["start",Color.black,Color.clear],["stop",Color.black,Color.green(alpha:0.2)]])
			.font_(GUI.font.new("Helvetica", 9))			
			.action_({arg butt; var dictArray;
				dictArray = [paraDict1, paraDict2];
				fileListPopup.do({arg popup; popup.action.value(popup.value)}); // load monobuf
				if(butt.value == 1, {
					channels.do({arg i;
						synthsList.add(Synth.new(\xiiGranny, [
									\out, outbusarray[i],
									\trigRate, dictArray[i].trigRate,
									\freq, dictArray[i].freq, 
									\centerPos, dictArray[i].centerPos, 
									\dur, dictArray[i].dur, 
									\pan, dictArray[i].pan, 
									\amp, dictArray[i].amp, 
									\buffer, channelbuffers.at(i),
									\cntrPosRandWidth, dictArray[i].cntrPosRandWidth,
									\cntrPosRandFreq, dictArray[i].cntrPosRandFreq, 
									\durRandWidth, dictArray[i].durRandWidth,  
									\durRandFreq, dictArray[i].durRandFreq, 
									\revVol, dictArray[i].revVol,  
									\delayTime, dictArray[i].delayTime,
									\decayTime, dictArray[i].decayTime,
									\aDelTime, dictArray[i].aDelTime,
									\aDecTime, dictArray[i].aDecTime,
									\rateRandWidth, dictArray[i].rateRandWidth,
									\rateRandFreq, dictArray[i].rateRandFreq							]));
					});
				},{
					synthsList.do(_.free);
					synthsList = List.new;
				});
			});

		glVolSlider = OSCIISlider.new(win, Rect(10, 60, 100, 8), "- vol", 0, 1, 1, 0.01, \amp)
			.canFocus_(false)
			.action_({arg sl;
				synthsList.do({arg synth; synth.set(\vol, sl.value)});
				params[0] = sl.value;
			});
		
		GUI.staticText.new(win, Rect(10, 104, 230, 15)).string_("x = ");
		GUI.staticText.new(win, Rect(10, 130, 230, 15)).string_("y = ");
		xfield = 	GUI.textField.new(win,Rect(32,104,78,18));
					xfield.font_(GUI.font.new("Monaco", 9));
					xfield.setProperty(\align,\center);
		yfield = 	GUI.textField.new(win,Rect(32,130,78,18));
					yfield.font_(GUI.font.new("Monaco", 9));
					yfield.setProperty(\align,\center);
		
		
		infoButt = GUI.button.new(win,Rect(10, 156, 102, 18))
					.states_([["color info", Color.black, Color.clear]])
					.font_(GUI.font.new("Helvetica", 9))
					//.background_(Color.new255(160, 170, 255, 100))
					.action_({this.infoFunc});
		
		cmdPeriodFunc = { startStopButt.valueAction_(0);};
		CmdPeriod.add(cmdPeriodFunc);
		
		win.front;
		win.onClose_({
			var t;
			CmdPeriod.remove(cmdPeriodFunc);
			synthsList.do(_.free);
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		});

		ldSndsGBufferList = {arg argPoolName;
			poolName = argPoolName.asSymbol;
			if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
				sndNameList = [];
				bufferList = List.new;
				sampleDurList = List.new;
				XQ.globalBufferDict.at(poolName)[0].do({arg buffer, i;
					sndNameList = sndNameList.add(buffer.path.basename);
					bufferList.add(buffer.bufnum);
					sampleDurList.add(XQ.globalBufferDict.at(poolName)[1][i]/s.sampleRate);
					// put the new sounds into popupmenus
					fileListPopup[0].items_(sndNameList);
					fileListPopup[1].items_(sndNameList);
					fileListPopup[1].value = 1;
				 });
			}, {
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(XQ.poolNames[0]); // load the first pool 
		this.updatePoolMenu;
		glVolSlider.valueAction_(params[0]);
		selbPool.valueAction_(0);
	}
	
	updatePoolMenu {
		var pool, poolindex, poolnamesarray;
		poolnamesarray = XQ.globalBufferDict.keys.asArray.sort;
		pool = selbPool.items.at(selbPool.value);  
		selbPool.items_(poolnamesarray);
		poolindex = selbPool.items.indexOf(pool);
		if(poolindex == nil, {
			selbPool.value_(0);
			ldSndsGBufferList.value(poolnamesarray[0]);
		},{
			selbPool.value_(poolindex);
			ldSndsGBufferList.value(pool);
		});
	}
	
	// ================ THE INFO FUNCTION (CALLED FROM BUTTON) ========================	
	infoFunc {	 
		var w, r, colList, stringList, b;
		colList = [
		Color.new255(255, 140, 0), // Orange
		Color.new255(205, 120, 0, 122), // Orange alpha
		Color.new255(225, 52, 54), // Red
		Color.new255(205, 52, 54, 122), // Red alpha
		Color.new255(70, 100, 155), // Blue
		Color.new255(70, 100, 155, 122), // Blue alpha
		Color.new255(238, 238, 0),  // Yellow
		Color.new255(70, 155, 100), // SeaGreen
		Color.new255(127, 65, 138, 122), // greenish alpha 
		Color.new255(46, 139, 127, 122) // greenish alpha
		];
		stringList = [
		["x = grain duration","y = grain trigger rate"],
		["x = rand width of duration","y = rand freq of duration"],
		["x = -","y = pitch"],
		["x = rand width of pitch variation","y = rand freq of pitch change"],
		["x = centre position","y = -"],
		["x = rand width of centre position","y = rand freq of centre position"],
		["x = pan of grains","y = amplitude"],
		["x = -","y = volume of reverb"],
		["x = delay time of reverb","y = decay time of reverb"],
		["x = allpass delay time","y = allpass decay time"],
		];

		w = GUI.window.new("   colour info", Rect(250, 100, 280, 480));
		w.alpha = 0.2;
		w.front;

		r = Routine.new({
		10.do({arg i;	
			w.bounds_(Rect(120-(i*10), 100, 280, 480));
			w.alpha = if(i<4, {0.4}, {i/9});
			});
			r = nil;
		});
		AppClock.play(r);
		
		10.do({arg i;		
			b = 	GUI.textField.new(w,Rect(20,20+(i*40),30,30));
			b.background_( colList.at(i) );
			GUI.staticText.new(w, Rect(60, 18+(i*40), 230, 15)).string_(stringList[i][0]);
			GUI.staticText.new(w, Rect(60, 34+(i*40), 230, 15)).string_(stringList[i][1]);
			});
	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		
		params = [params[0], params[1]]
						.add(envViewList[0].value.round(0.01)) // had to round due to problems
						.add(envViewList[1].value.round(0.01)); // with float res in archive
		^[2, point, params];
	}

}