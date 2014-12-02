
XiiBufferPlayer {
	classvar classIDNum;
	var <>xiigui, <>win, params;
	var <>viewerList, <startButtList, <sfdropDownList;
	
	var selbPool, poolName, settingsPool, ldSndsGBufferList;
	var synthList, loopRangeList;
	
	*new {arg server, ch, setting = nil;
		^super.new.initXiiBufferPlayer(server, ch, setting);
	}

	initXiiBufferPlayer {arg server, chnls, setting;

var responder, bufsec, session, quitButt;
var tracks, sndfiles;
var trigID;
var sliderList;
var stMonoList, globalList;
var rowspace = 110, lowRow = 0, virIndex = 0;
var windowSize;
var soundDir;
var glStartButt, glStopButt, volSlList, panSlList, pitchSlList;
var gBufferPoolNum;
var globalVolSlider, tracksButt;
var s, p, point;
var idNum, drawRadioButt, createResponder;
var outbusArray, volArray, panArray, pitchArray, bufferIndexArray; // for storing settings
var rangeArray;

if(classIDNum.isNil, {classIDNum = 50}); // sendtrig id starts with 50
idNum = classIDNum;

tracks = XQ.pref.bufferPlayerTracks; //tracks;
s = server;
sliderList = List.new;
synthList = Array.fill(tracks, nil);
stMonoList = List.new;
globalList = Array.fill(tracks, 0);
volSlList = List.new;
panSlList = List.new;
pitchSlList = List.new;
startButtList = List.new;
sfdropDownList = List.new;
viewerList = Array.fill(tracks, {nil});
rangeArray = Array.fill(tracks, {[0, 1]});
loopRangeList = List.new; // the list of views

bufferIndexArray = Array.fill(tracks, {arg i; i});
outbusArray = Array.fill(tracks, {0});
volArray = Array.fill(tracks, {0});
panArray = Array.fill(tracks, {0});
pitchArray = Array.fill(tracks, {1});
poolName = nil;

bufsec = 1;
gBufferPoolNum = 0;

p = [
Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), Point(15,1), 
Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), Point(66,37), Point(59,43),
Point(53,43), Point(53,12), Point(44,22), Point(53,33), Point(53,43), Point(42,43), Point(34,32),
Point(24,43), Point(7,43), Point(1,36), Point(1,8)
];

xiigui = nil;
point = if(setting.isNil, {Point(10, 300)}, {setting[1]});
params = if(setting.isNil, {[bufferIndexArray, outbusArray, volArray, panArray, pitchArray, poolName, globalList, rangeArray]}, {setting[2]});

poolName = params[5];
settingsPool = poolName;

windowSize = Rect(point.x, point.y, 1015, (tracks/8).round(1)*270);

win = GUI.window.new("- bufferplayer -", windowSize, resizable:false);
win.drawHook = {
	GUI.pen.color = Color.new255(255, 100, 0);
	GUI.pen.width = 3;
	GUI.pen.translate(48,48);
	GUI.pen.scale(0.4,0.4);
	GUI.pen.moveTo(1@7);
	p.do({arg point;
		GUI.pen.lineTo(point+0.5);
	});
	GUI.pen.stroke
};

selbPool = GUI.popUpMenu.new(win, Rect(15, 5, 90, 16))
	.font_(GUI.font.new("Helvetica", 9))
	.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray.sort}))
	//.value_(selbPool.items.indexOfEqual(poolName))
	.value_(0)
	.background_(Color.white)
	.action_({ arg item; var outbus;
		poolName = selbPool.items[item.value];
		params[5] = selbPool.items[item.value]; // store the poolName in the parameter list
		ldSndsGBufferList.value(poolName); 
	});


glStartButt = GUI.button.new(win,Rect(15, 110, 41, 18));
glStartButt.states = [["start",Color.black, Color.clear]];
glStartButt.canFocus_(false);
glStartButt.font_(GUI.font.new("Helvetica", 9));
glStartButt.action = { arg butt;
	startButtList.size.do({arg i; 
		if(params[6][i] == 1, {
			startButtList[i].valueAction_(1);
		});
	});
};

glStopButt = GUI.button.new(win,Rect(61, 110, 41, 18));
glStopButt.states = [["stop",Color.black, Color.clear]];
glStopButt.canFocus_(false);
glStopButt.font_(GUI.font.new("Helvetica", 9));
glStopButt.action = { arg butt;
	startButtList.size.do({arg i; 
		if(params[6][i] == 1, {
			startButtList[i].valueAction_(0);
		});
	});	
};

globalVolSlider = OSCIISlider.new(win, 
		Rect(15, 160, 80, 10), "- vol", 0, 1.0, 0, 0.0001, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 	
					volSlList.size.do({arg i; 
						if(params[6][i] == 1, {
							volSlList[i].valueAction_( sl.value);
						});
					});	
				});
	
OSCIISlider.new(win, 
		Rect(15, 190, 80, 10), "- pan", -1.0, 1.0, 0.0, 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
					panSlList.size.do({arg i; 
						if(params[6][i] == 1, {
							panSlList[i].valueAction_(sl.value);
						});
					});	
				});
OSCIISlider.new(win, 
		Rect(15, 220, 80, 10), "- pitch", 0, 2.0, 1.0, 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 	
					pitchSlList.size.do({arg i; 
						if(params[6][i] == 1, {
							pitchSlList[i].valueAction_(sl.value);
						});
					});	
				});
				
GUI.staticText.new(win, Rect(5, 100, 105, 160))
	.font_(GUI.font.new("Helvetica", 16))
	.string_("")
	.background_(Color.new255(255, 100, 0, 20));

poolName = XQ.poolNames[0];
sndfiles = XQ.bufferNames(poolName);

tracks.do({ arg i; 
	var trigID, ch, sf, glButt, startPos, endPos, selectionfr;

	trigID = idNum + (i * 2);

	if((i>0)and:{(i%8)==0}, {lowRow=lowRow+270; virIndex= virIndex-8});

	sliderList.add( // the left volume signal
		GUI.rangeSlider.new(win, Rect(120+(virIndex+i*rowspace), 5+lowRow, 20, 100))
			.background_(Color.new255(155, 205, 155))
			.knobColor_(
				if(GUI.id == \cocoa, {
					HiliteGradient(XiiColors.darkgreen, Color.white, \h);
				}, {
					XiiColors.darkgreen;
				});
			)
			.lo_(0.0).hi_(0.01)
			.canFocus_(false);
	);
	sliderList.add( // the right volume signal
		GUI.rangeSlider.new(win, Rect(145+(virIndex+i*rowspace), 5+lowRow, 20, 100))
			.background_(Color.new255(155, 205, 155))
			.knobColor_(
				if(GUI.id == \cocoa, {
					HiliteGradient(XiiColors.darkgreen, Color.white, \h);
				}, {
					XiiColors.darkgreen;
				});
			)
			.lo_(0.0).hi_(0.01)
			.canFocus_(false);
	);
	
	stMonoList.add(
		GUI.staticText.new(win, Rect(172+(virIndex+i*rowspace), 50+lowRow, 60, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("oo");
	);

	GUI.staticText.new(win, Rect(172+(virIndex+i*rowspace), 67+lowRow, 60, 16))
		.font_(GUI.font.new("Helvetica", 9))
		.string_("global:");
	glButt = GUI.button.new(win,Rect(206+(virIndex+i*rowspace), 68+lowRow, 12, 12));
	glButt.states = [	["",Color.black, Color.clear],
					["",Color.black, Color.new255(155, 205, 155)]];
	glButt.value = params[6][i];
	glButt.canFocus_(false);
	glButt.action = { arg butt;
		globalList[i] = butt.value;
		params[6] = globalList;
	};

	GUI.button.new(win, Rect(171+(virIndex+i*rowspace), 87+lowRow, 48, 16))
		.font_(GUI.font.new("Helvetica", 9))
		.canFocus_(false)
		.states_([["view", Color.black, Color.clear]])
		.action_({
			if(viewerList[i] == nil, {
				// call this through the bufferpool
				XQ.globalWidgetList.do({ |widget|
					if(widget.isKindOf(XiiBufferPool), {
						if(widget.name == poolName.asString, {
							viewerList[i] =
								widget.viewBuffer(
									XQ.buffers(poolName)[sfdropDownList[i].value], // buffer
									sfdropDownList[i].value, // index
									poolName, // poolname
									[0,0],
									//XQ.selections(poolName)[sfdropDownList[i].value], 
									this, i);
						});	
					});
				});
			},{
				viewerList[i].win.front;
			});
		});

	// outbus
	ch = GUI.popUpMenu.new(win,Rect(120+(virIndex+i*rowspace), 111+lowRow , 46, 16))			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(params[1][i]/2)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.action_({ arg ch; var outbus;
				outbus = ch.value * 2;
				outbusArray[i] = outbus;
				if(synthList[i] !=nil, { synthList[i].set(\out, outbus) });
				outbusArray[i] = outbus;
				params[1] = outbusArray;
			});
	
startButtList.add(GUI.button.new(win,Rect(171+(virIndex+i*rowspace), 110+lowRow, 48, 18))
					.states_([	["play",Color.black, Color.clear],
								["stop",Color.black, Color.new255(155, 205, 155)]])
					.font_(GUI.font.new("Helvetica", 9))
					.action_({ arg butt; var startPos, endPos, buffer;
					
					if(butt.value == 1, {
		
		buffer = XQ.buffers(poolName)[sfdropDownList[i].value];
		trigID = idNum + (i * 2);

		startPos = XQ.selections(poolName)[sfdropDownList[i].value][0] + (params[7][i][0] * XQ.selections(poolName)[sfdropDownList[i].value][1]);
			
		endPos = XQ.selections(poolName)[sfdropDownList[i].value][0] + (XQ.selections(poolName)[sfdropDownList[i].value][1] * params[7][i][1]);

		synthList[i] = 
			if(buffer.numChannels == 2, {
				Synth.new(\xiiBufPlayerSTEREO, 
					[ \bufnum, buffer.bufnum, 
					  \trigID, trigID, // the bus for the gui update
					  \out, params[1][i],
					  \vol, params[2][i], 
					  \pan, params[3][i],
					  \pitch, params[4][i],
					  \startPos, startPos,
					  \endPos, endPos //  XXXXX
					  ], // the default out bus
					  s, \addToHead);
			}, {

				Synth.new(\xiiBufPlayerMONO, 
					[ \bufnum, buffer.bufnum, 
					  \trigID, trigID, // the bus for the gui update
					  \out, params[1][i],
					  \vol, params[2][i],
					  \pan, params[3][i],
					  \pitch, params[4][i],
					  \startPos, startPos,
					  \endPos, endPos
					  ], // the default out bus
					  s, \addToHead);
			});
		},{
			synthList[i].free;
			synthList[i] = nil;			
		});
		});
	);

// soundfiles
sfdropDownList.add(GUI.popUpMenu.new(win,Rect(120+(virIndex+i*rowspace), 135+lowRow , 100, 18))
	.items_([]) // was sndfiles, but this is loaded later
	.value_(params[0][i])
	.font_(GUI.font.new("Helvetica", 9))
	.background_(Color.white)
	.action_({ arg sf; var startPos, endPos; 
		stMonoList.at(i).string_(
			if(XQ.buffers(poolName).at(sf.value).numChannels == 2, 
				{"stereo"}, {"mono"})
		);		
		
		startPos = XQ.selections(poolName)[sf.value][0] + (params[7][i][0] * XQ.selections(poolName)[sf.value][1]);
			
		endPos = XQ.selections(poolName)[sf.value][0] + (XQ.selections(poolName)[sf.value][1] * params[7][i][1]);
		
		synthList.at(i).set(\bufnum, XQ.buffers(poolName)[sf.value].bufnum);
		synthList.at(i).set(\startPos, startPos);
		synthList.at(i).set(\endPos, endPos);
		bufferIndexArray[i] = sf.value;
		params[0] = bufferIndexArray;
	});
);

volSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 160+lowRow, 100, 10), "- vol", 0, 1.0, params[2][i], 0.01, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; var globalActiveCounter = 0, volAll = 0;
				if(synthList[i] !=nil, { synthList[i].set(\vol, sl.value) });
				tracks.do({arg i;
					if(globalList[i] == 1, {
						globalActiveCounter = globalActiveCounter + 1;
						volAll = volAll + sl.value;
						globalVolSlider.value_(volAll/globalActiveCounter);
					})
				});
				volArray[i] = sl.value;
				params[2] = volArray;
			})
		);
panSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 190+lowRow, 100, 10), "- pan", -1.0, 1.0, params[3][i], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 	
				if(synthList[i] !=nil, { synthList[i].set(\pan, sl.value) });
				panArray[i] = sl.value;
				params[3] = panArray;
			})
		);
pitchSlList.add(OSCIISlider.new(win, 
		Rect(120+(virIndex+i*rowspace), 220+lowRow, 100, 10), "- pitch", 0, 2.0, params[4][i], 0.01)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 	
				if(synthList[i] !=nil, { synthList[i].set(\pitch, sl.value) });
				pitchArray[i] = sl.value;
				params[4] = pitchArray;
			})
		);
	
loopRangeList.add(RangeSlider.new(win, Rect(120+(virIndex+i*rowspace), 250+lowRow, 100, 10))
		//.canFocus_(false)
		.focusColor_(Color.white.alpha_(0.1))
		.lo_(params[7][i][0])
		.hi_(params[7][i][1])
		.knobColor_(
			if(GUI.id == \cocoa, {
				HiliteGradient(Color.new(0,0, 0.2), Color.white);
			}, {
				Color.new(0,0, 0.2);
			});
		)
		.action_({arg sl; var startPos, endPos, numSelectedFrames;
			
			startPos = XQ.selections(poolName)[sfdropDownList[i].value][0] 
			+ (sl.lo * XQ.selections(poolName)[sfdropDownList[i].value][1]);
			
			endPos = XQ.selections(poolName)[sfdropDownList[i].value][0] 
				 + (XQ.selections(poolName)[sfdropDownList[i].value][1] * sl.hi);
			
			numSelectedFrames = ( XQ.selections(poolName)[sfdropDownList[i].value][1] * (sl.hi-sl.lo));
			params[7][i][0] = sl.lo;
			params[7][i][1] = sl.hi;

			if(synthList[i] !=nil, { 
				synthList[i].set(\startPos, startPos);
				synthList[i].set(\endPos, endPos);
			});
			
			if(viewerList[i].isNil.not, {
				viewerList[i].setBufferPlayerSelections(startPos, numSelectedFrames);
			});
			
		});
	);

	if(try {XQ.buffers(poolName)} != nil, {
		{stMonoList.wrapAt(i).string_(
		if(XQ.buffers(poolName).wrapAt(i).numChannels == 2, {"stereo"}, {"mono"}))
		}.defer;
	});
}); // end of channel loop

		ldSndsGBufferList = {arg arggBufferPoolName;
			poolName = arggBufferPoolName.asSymbol;
			sndfiles = XQ.bufferNames(poolName);
			
			if(try {XQ.globalBufferDict.at(poolName)[0]} != nil, {
				tracks.do({arg i; var startPos, endPos;
					sfdropDownList[i].items = sndfiles;
					sfdropDownList[i].value_(params[0][i]);
					{stMonoList.at(i).string_(
						if(XQ.buffers(poolName).wrapAt(i).numChannels == 2, 
							{"stereo"}, {"mono"})
						)
					}.defer;
					if(synthList[i] !=nil, { 
						synthList[i].set(\bufnum, 
							XQ.buffers(poolName).wrapAt(i).bufnum);
					});
				});
			});
			
		};
		
		ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0]);

		drawRadioButt = OSCIIRadioButton(win, Rect(15,138,14,14), "draw")
					.font_(GUI.font.new("Helvetica", 9))
					.value_(1)
						.action_({arg val; if(val==1, {
								createResponder.value;
							}, {
								responder.remove;
							})
					});
					
		createResponder = {
			responder = OSCresponderNode(s.addr, '/tr', { arg time, responder, msg;
				{ 
				win.isClosed.not.if({ // if window is not closed, update GUI...
					if((msg[2]-idNum >= 0) && (msg[2] <= (idNum+(tracks*2))), {
						sliderList.at(msg[2]-idNum).hi_(1-(msg[3].ampdb.abs * 0.01)) 
					});
				});
				}.defer;
			}).add;
		};
		createResponder.value;

		win.front;
		win.onClose_({ 
			var t;
			responder.remove;
			synthList.size.do({arg i; synthList[i].free;});
			viewerList.do({|viewer| try{viewer.win.close} });
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, {t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
		
		});
		classIDNum = classIDNum + (tracks*2) + 2; // increase the classvar in case the user opens another bp
	} // end of initXiiBufferPlayer

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

/*
	updatePoolMenu {
		var poolname, poolindex;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		[\poolindex, poolindex].postln;
		
		if(poolindex != nil, { // not first time pool is loaded
			if(poolname == poolName, {
				//selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
				//ldSndsGBufferList.value(poolname);
			},{
				selbPool.valueAction_(poolindex); // nothing changed, but new poolarray or sound 
				ldSndsGBufferList.value(poolname);
			})
		}, {
			if(poolname == poolName, {
				//selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
				//ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // first pool
			},{
				selbPool.valueAction_(0); // loading a pool for the first time (index nil) 
				ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0], true); // first pool
			
			})
		});
	}
*/
	
	// called from XiiSoundFileView when selected loop is moved
	setLoopRange {arg chnl, startPos, numSelectedFrames;
		var lo, hi, selfile;
		selfile = sfdropDownList[chnl].value;
		if(synthList[chnl] !=nil, { 
			synthList[chnl].set(\startPos, startPos);
			synthList[chnl].set(\endPos, startPos+numSelectedFrames);
		});
		
		lo = startPos/XQ.selections(poolName)[params[0][selfile]][1];
		loopRangeList[chnl]
			.lo_(lo)
			.hi_(hi);
	}
	
	updatePoolMenu {
		var poolname, poolindex, sndfiles;
		poolname = selbPool.items.at(selbPool.value); // get the pool name (string)
		selbPool.items_(XQ.globalBufferDict.keys.asArray.sort); // put new list of pools
		poolindex = selbPool.items.indexOf(poolname); // find the index of old pool in new array
		if(poolindex != nil, { // not first time pool is loaded
			if(settingsPool.isNil, {
				selbPool.action.value(poolindex);
			}, {
				ldSndsGBufferList.value(settingsPool);
				poolindex = selbPool.items.indexOfEqual(settingsPool); 
				selbPool.action.value(poolindex);
			}) 
			// ldSndsGBufferList.value(poolname);
		}, {
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0]); // load first pool
			//selbPool.value_(0); // loading a pool for the first time (index nil) 
			selbPool.action.value(0);
		});
	}

	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}
	
}
