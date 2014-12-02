
XiiSoundDrops {

	var ldSndsGBufferList, selbPool;
	var stateDict, stateNum;
	var <>xiigui, <>win, params;

	*new { arg server, channels, setting = nil; 
		^super.new.initXiiSoundDrops( server, channels, setting);
	}
	
	initXiiSoundDrops { arg server, channels, setting; 

var s, msl, volMsl, stepsSl, dropsSl, speedSl, randButt, globalVolSl;
var timeTask, dropcount, drawer;
var bufferPop, soundFuncPop, resetButt, startButt;
var sndNameList, bufferList, gBufferPoolNum, poolName;
var slValues, trackVolumes, stepsValues;
var globalRButt, singularRButt, local, setToTopButt, slIndex;
var numberBoxArray, generateNumBoxArray;
var soundFuncArray, playFuncsArray;
var outbus, inbus, globalvol, playFunc, createScalesWin;
var createCodeWin, createAudioStreamBusWin, keyboard, fundamental, scaleButt, freqText, freqTextView;
var generateNoteViews, resolution, tonalityButt, resSlider, outbusPop;
var loadArchive, saveArchive, speed;
var tonal, point;
var notes, note; // scale chosen
var statesPop, clearButt, storeButt, draw, drawButt, backgrounddraw;
var scale, selectall;
var change, backGRView;
var freq, amp, pitchratio, buffer;
var scalewin, aswin;

s = server;
xiigui = nil;

stateDict = ();
stateNum = 0;

dropcount = 16;
globalvol = 0.7;
fundamental = 36;
resolution = 24;
speed = 0.4;
outbus = 0;
tonal = false;
draw = false;
backgrounddraw = true;
change = true;
scale = false;
selectall = false;

server = Server.default;
stepsValues = Array.fill(dropcount, 8);
trackVolumes = Array.fill(dropcount, 0.3);
slValues = Array.fill(dropcount, {|i| 
				1.0.rand.round(stepsValues[i].reciprocal) // so it lands in place
			});

soundFuncArray = Array.fill(48, {
				().add(\playFunc -> 1)
				.add(\freq -> rrand(110, 1760))
				.add(\pitchratio -> 1)
				.add(\amp -> 1)	
				.add(\buffer -> 0)	
				.add(\inbus -> 20)
				.add(\codeFlag -> false)	
				.add(\code -> "{
	var env, sine;
	env = EnvGen.ar(Env.perc, doneAction:2);
	sine = SinOsc.ar(440, 0, env);
	sine ! 2
}.play")
});

if(setting.isNil, {
	point = Point(100, 100);
	params = [soundFuncArray, slValues, stepsValues, trackVolumes, resolution, globalvol, dropcount, speed, outbus, draw];
}, { // coming from a preset setting
	point = setting[1];
	stateDict = setting[2];
	stateNum = stateDict.size;

	// ok - set state 1 as default state, and load vars - GUI views take care of themselves
	params = setting[2].at("state 1".asSymbol);
	soundFuncArray = params[0].copy;
	slValues = params[1].copy;
	stepsValues = params[2].copy;
	trackVolumes = params[3].copy; 
	resolution = params[4].copy; 
	globalvol = params[5].copy; 
	dropcount = params[6].copy;
	speed = params[7].copy;
	outbus = params[8].copy;
	draw = params[9].copy;
	
});

local = false;
slIndex = 0;

win = GUI.window.new("- sounddrops -", Rect(point.x, point.y, 820, 343), resizable:false).front;


backGRView = GUI.userView.new(win, Rect(120, 5, 680, 200))
		.canFocus_(false)
		.drawFunc_({ |view|
			if(backgrounddraw == true, {
				if(selectall == true, {
					GUI.pen.fillColor = XiiColors.lightgreen;
				},{ 
					GUI.pen.fillColor = Color.white;
				});
				GUI.pen.fillRect(Rect(0, 0, 680, 200));
				backgrounddraw = false; // never draw this view again
			})
		})
		//.relativeOrigin_(true) // use this for the refresh                 XXX
		.clearOnRefresh_(false);


	drawer = GUI.userView.new(win, Rect(120, 5, 680, 200))
			.canFocus_(false)
			.drawFunc_({ |view|
				
				if(change == true, {
					if(draw == true, {
						GUI.pen.color = Color(0.54509803921569, 0.0, 0.0, 0.9);
						dropcount.do({ |i|
							stepsValues.do({ |steps, ix|
								steps.do({ |iy|
									GUI.pen.line(
									Point(	(1+(ix*(680/dropcount))),
											3.5+(((iy+1)*(192/steps))).round(1)),
									Point(	(1+(ix*(680/dropcount))+(680/dropcount)),
											3.5+(((iy+1)*(192/steps))).round(1))
									);
								});
							});
						});
						GUI.pen.stroke;
						//draw = false; // no need for this because of change = false
					});
					change = false;
				});
			})
			//.relativeOrigin_(true) // use this for the refresh
			.clearOnRefresh_(false); // no refresh when window is refreshed


msl = GUI.multiSliderView.new(win, Rect(120, 5, 680, 200))
	.value_(params[1])
	.isFilled_(false)
	.strokeColor_(Color.new255(10, 55, 10))
	.fillColor_(XiiColors.lightgreen)
	.valueThumbSize_(7)
	.indexThumbSize_(680/dropcount)
	.gap_(0)
	.showIndex_(true)
	.indexIsHorizontal_(true) // another view option - go other direction
	.canFocus_(false)
	.background_(Color.clear)
	.action_({ |xb|
		slValues = xb.value;
		params[1] = slValues;
		slIndex = xb.index;
		volMsl.index = xb.index;
		freqTextView.string_(soundFuncArray[xb.index].freq.round(0.01).asString);
		soundFuncPop.value_( soundFuncArray[slIndex].playFunc );
		bufferPop.value_( soundFuncArray[slIndex].buffer );
		if(selectall == true, { 
			selectall=false;
			backgrounddraw = true; 
			backGRView.clearDrawing; 
		});
	});
	
	
volMsl = GUI.multiSliderView.new(win, Rect(120, 210, 680, 22))
	.value_(params[3])
	.isFilled_(true)
	.strokeColor_(Color.new255(10, 55, 10))
	.fillColor_(XiiColors.lightgreen)
	.valueThumbSize_(0)
	.indexThumbSize_(680/dropcount)
	.gap_(0)
	.showIndex_(true)
	.focusColor_(Color.black.alpha_(0.0))
	.indexIsHorizontal_(true) // another view option - go other direction
	.canFocus_(true)
	.background_(Color.white)
	.action_({arg xb;
		msl.index_(xb.index);
		slIndex = xb.index;
		trackVolumes = xb.value;
		params[3] = trackVolumes;
		soundFuncArray[xb.index].amp = xb.value[xb.index];
		freqTextView.string_(soundFuncArray[xb.index].freq.round(0.01).asString);
		soundFuncPop.value_( soundFuncArray[slIndex].playFunc );
		bufferPop.value_( soundFuncArray[slIndex].buffer );
		if(selectall == true, { 
			selectall = false; 
			backgrounddraw = true; 
			backGRView.clearDrawing; 
		});
	})
	.keyDownAction_({ arg view, key, modifiers, unicode; var note;
		//if(unicode == 1, { // select all (ctrl + a)
		if(modifiers.isCtrl, {
			selectall = true;
			msl.index_(-1);
			backgrounddraw = true; 
			backGRView.clearDrawing;
			backGRView.refresh;
		});
		if(unicode == 16rF703, { 
			volMsl.index = volMsl.index + 1; 
			msl.index = volMsl.index;
			slIndex = msl.index;	
			note = soundFuncArray[slIndex].freq; // new note
			freqTextView.string_(note.round(0.01).asString);
			soundFuncPop.value_( soundFuncArray[slIndex].playFunc );
			bufferPop.value_( soundFuncArray[slIndex].buffer );
			if(tonal, {
				keyboard.clear;
				if(scale, {keyboard.showScale(notes, fundamental, Color.new255(103, 148, 103))});
				if(note.freqIsMicroTone, {
					keyboard.setColor(note.cpsmidi, Color.red);
				}, {
					keyboard.setColor(note.cpsmidi, 	Color.grey);
				});
			});
			if(selectall == true, { // deselect all if moved to a new index
				selectall = false;			
				backgrounddraw = true; 
				backGRView.clearDrawing; 
			});
 		});
		if (unicode == 16rF702, { 
			volMsl.index = volMsl.index - 1; 
			msl.index = volMsl.index;
			slIndex = msl.index;	
			note = soundFuncArray[slIndex].freq; // new note
			freqTextView.string_(note.round(0.01).asString);
			soundFuncPop.value_( soundFuncArray[slIndex].playFunc );
			bufferPop.value_( soundFuncArray[slIndex].buffer );
			if(tonal, {
				keyboard.clear;
				if(scale, {keyboard.showScale(notes, fundamental, Color.new255(103, 148, 103))});
				if(note.freqIsMicroTone, {
					keyboard.setColor(note.cpsmidi, Color.red);
				}, {
					keyboard.setColor(note.cpsmidi, Color.grey);
				});
			});
			if(selectall == true, { // deselect all if moved to a new index
				selectall = false;			
				backgrounddraw = true; 
				backGRView.clearDrawing; 
			});
		});
		if (unicode == 16rF700, { 
			if((modifiers & 262144) != 0, { // press ctrl to control upper multislider
				if(msl.value[volMsl.index] < 1, {
					msl.value = (msl.value[volMsl.index] = msl.value[volMsl.index] + stepsValues[volMsl.index].reciprocal);
					slValues = msl.value;
				});
			}, {
				if(volMsl.value[volMsl.index] < 1, {
					volMsl.value = volMsl.value[volMsl.index] = volMsl.value[volMsl.index] + 0.05
			});
			if(selectall == true, { // deselect all if moved to a new index
				selectall = false;			
				backgrounddraw = true; 
				backGRView.clearDrawing; 
			});
			});
		});
		if (unicode == 16rF701, { 
			if((modifiers & 262144) != 0, { // press ctrl to control upper multislider
				if(msl.value[volMsl.index] > 0, {
					msl.value = (msl.value[volMsl.index] = msl.value[volMsl.index] - stepsValues[volMsl.index].reciprocal);
					slValues = msl.value;
				});
			}, {
				if(volMsl.value[volMsl.index] > 0, {
					volMsl.value = volMsl.value[volMsl.index] = volMsl.value[volMsl.index] - 0.05
				});
			});
			if(selectall == true, { // deselect all if moved to a new index
				selectall = false;			
				backgrounddraw = true; 
				backGRView.clearDrawing; 
			});
		});
	});


	generateNumBoxArray = {|dropcount|
		try{numberBoxArray.do({|box| box.remove})}; // if loading a setting remove old boxes
		numberBoxArray = Array.fill(dropcount, {|i| 
			NumberBox(win, Rect(120+(i*(680/dropcount)), 240, (680/(dropcount)-1), 12))
				.font_(GUI.font.new("Helvetica", if(dropcount>42, {8}, {9})))
				.value_( stepsValues[i] )
				.focusColor_( XiiColors.darkgreen )
				.align_(\center)
				.background_(Color.white)
				.action_({ arg sbs;
					var val;
					val = sbs.value.clip(2, 24);
					sbs.value=val;
					stepsValues[i] = val;
					params[2] = stepsValues.copy;
					if(drawButt.value == 1, { // if drawing, then draw
						drawer.clearDrawing;
						draw = true;
						change = true;
						drawer.refresh;
					});
				})
				.keyDownAction_({ arg view, key, modifiers, unicode; 
					if(unicode == 16rF703, { 
						volMsl.focus(true);
						slIndex = msl.index+1;	
						msl.index = slIndex;
						volMsl.index = slIndex;
					});
					if(unicode == 16rF702, { 
						volMsl.focus(true);
						slIndex = msl.index-1;	
						msl.index = slIndex;
						volMsl.index = slIndex;
					});
					if (unicode == 16rF700, { 
						view.valueAction_(view.value+1);
					});
					if (unicode == 16rF701, { 
						view.valueAction_(view.value-1);
					});
				});
		});
	};
	generateNumBoxArray.(dropcount);
	
	// tonal or microtonal views?
	generateNoteViews = { arg res = 24, generatedfromslider = false; 
		var microtone2Darray;
		keyboard.remove;
		if(tonal == true, { // tonal view
			resSlider.remove;
			keyboard = MIDIKeyboard.new(win, Rect(200, 266, 599, 67), 7, 36)
						.keyDownAction_({arg nte; 
							note = nte;
							soundFuncArray[slIndex].freq = note.midicps;
							freqTextView.string_(note.midicps.round(0.01).asString);
						})
						.keyTrackAction_({arg nte; 
							note = nte;
							soundFuncArray[slIndex].freq = note.midicps;
							freqTextView.string_(note.midicps.round(0.01).asString);
						 });
		
			scaleButt = GUI.button.new(win, Rect(120, 266, 74, 16))
							.font_(GUI.font.new("Helvetica", 9))
							.canFocus_(false)
							.states_([["scales", Color.black, Color.clear]])
							.action_({
								createScalesWin.value;
							});
		},{
			if(generatedfromslider.not, { scaleButt.remove });
			microtone2Darray = Array.fill(7, {|i|
						Array.fill(resolution, {|j| ((fundamental+(i*12)).midicps)*2.pow(j/res) });
					}).reverse;
			keyboard = Grid.new(win, Rect(200, 266, 599, 66), columns: res, rows: 7, border:true)
						.setBackgrColor_(Color.white)
						.nodeDownAction_({arg nl; // nodeloc 
							var freq;
							freq = microtone2Darray[nl[1]] [nl[0]];
							soundFuncArray[slIndex].freq = freq;
							// highest pitch is 8133.64
							soundFuncArray[slIndex].pitchratio = [0.2, 4].asSpec.map(freq/8133.64);
							freqTextView.string_((microtone2Darray[nl[1]][nl[0]].round(0.01)).asString);
						})
						.nodeTrackAction_({arg nl; 
							var freq;
							freq = microtone2Darray[nl[1]] [nl[0]];
							soundFuncArray[slIndex].freq = freq;
							// highest pitch is 8133.64 and pitchratio is thus from 0.2 to 4
							soundFuncArray[slIndex].pitchratio = [0.2, 4].asSpec.map(freq/8133.64);
							freqTextView.string_((microtone2Darray[nl[1]][nl[0]].round(0.01)).asString);
						})
						.nodeUpAction_({arg nodeloc; 
							keyboard.setState_(nodeloc[0], nodeloc[1], false) 
						})
						.setBackgrDrawFunc_({
							7.do({ |i|
								GUI.pen.color = XiiColors.lightgreen.alpha_(0.3+(i/10));
								// Pen.fillRect(Rect(55+(i*(700/12)), 50, 600/12, 240));
								GUI.pen.fillRect(Rect(200, 272+(i*8.3), 600, 11));
							});
						});
				if(generatedfromslider.not, { // I don't want this view to create itself repeatedly
					resSlider = OSCIISlider(win, Rect(120, 266, 74, 8), "- res", 5, 48, res, 1)
							.font_(GUI.font.new("Helvetica", 9))
							.canFocus_(false)
							.action_({ |sl| 
								resolution = sl.value;
								generateNoteViews.( resolution, true);
								params[4] = resolution;
							});
				});

		});
	};
	generateNoteViews.( resolution, false ); // tonal, resolution, generated from slider
	
	freqText = GUI.staticText.new(win, Rect(120, 293, 30, 16))
					.font_(GUI.font.new("Helvetica", 9))
					.string_("freq:");

	freqTextView = GUI.textView.new(win, Rect(141, 295, 51, 12))
					.font_(GUI.font.new("Helvetica", 9))
					.string_(" 440")
					.keyDownAction_({arg view, key, mod, unicode; 
						if(unicode ==13, {
							soundFuncArray[slIndex].freq = view.string.asFloat;
							volMsl.focus(true);
						});
					});

	tonalityButt = GUI.button.new(win, Rect(120, 316, 74, 16))
					.font_(GUI.font.new("Helvetica", 9))
					.canFocus_(false)
					.states_([	["tonal", Color.black, Color.clear], 
								["microtonal", Color.black, Color.clear]])
					.action_({ |bt|
						tonal = tonal.not;
						generateNoteViews.(resolution);
					});

	selbPool = GUI.popUpMenu.new(win, Rect(10, 5, 100, 16))
		.font_(GUI.font.new("Helvetica", 9))
		.items_(if(XQ.globalBufferDict.keys.asArray == [], {["no pool"]}, {XQ.globalBufferDict.keys.asArray}))
		.value_(0)
		.canFocus_(false)
		.background_(Color.white)
		.action_({ arg item;
			gBufferPoolNum = item.value;
			ldSndsGBufferList.value(selbPool.items[item.value]);
			//soundFuncArray.do({|dict| dict.buffer = sndNameList.size.rand }); // assign random buf
		});

	bufferPop = GUI.popUpMenu.new(win, Rect(10, 27, 100, 16)) // 550
			.font_(GUI.font.new("Helvetica", 9))
			.items_(["no buffer 1", "no buffer 2"])
			.background_(Color.new255(255, 255, 255))
			.canFocus_(false)	
			.action_({ arg popup;
				if(selectall == true, { // all drops selected
					soundFuncArray.do({|dict| dict.buffer = popup.value.copy });
				}, {
					soundFuncArray[slIndex].buffer = popup.value.copy;
				});
			})
			.addAction({ bufferPop.action.value( bufferPop.value )}, \mouseDownAction);

	ldSndsGBufferList = {arg argPoolName;
			poolName = argPoolName.asSymbol;
				if(try {XQ.globalBufferDict.at(poolName)[0] } != nil, {
					sndNameList = [];
					bufferList = List.new;
					XQ.globalBufferDict.at(poolName)[0].do({arg buffer;
						sndNameList = sndNameList.add(buffer.path.basename);
						bufferList.add(buffer.bufnum);
					// assign random buffer to each drop
						soundFuncArray.do({|dict| dict.buffer = sndNameList.size.rand });
					});
					 bufferPop.items_(sndNameList);
					 bufferPop.action.value(0); // put the first file into the view and load buffer
			}, {
				sndNameList = [];
			});
		};
		
		ldSndsGBufferList.value(selbPool.items[0].asSymbol);

		soundFuncPop = GUI.popUpMenu.new(win, Rect(10, 49, 100, 16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(["sample", "sine", "bells", "sines", "synth1", "ks_string", 
				"ixi_string", "impulse", "ringz", "klanks", "scode", "audiostream"])				.background_(Color.new255(255, 255, 255))
				.value_(1)
				.canFocus_(false)
				.action_({ arg popup;
					soundFuncArray[slIndex].codeFlag = false; // set to false by default
					if(soundFuncPop.items[popup.value] == "scode", {
						createCodeWin.value;
					}); 
					if(soundFuncPop.items[popup.value] == "audiostream", {
						createAudioStreamBusWin.value;
					}); 
					if(selectall == true, { // all drops selected
						// MIGHT HAVE TO USE DEEPCOPY HERE
						soundFuncArray.do({ |dict| dict.playFunc=popup.value});
					}, {
						soundFuncArray[slIndex].playFunc = popup.value;
					});
				})
				.addAction({ soundFuncPop.action.value( soundFuncPop.value )}, \mouseDownAction);


		loadArchive = GUI.button.new(win, Rect(10, 70, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["load", Color.black, Color.clear]])
			.action_({arg butt;
				GUI.dialog.getPaths({ arg paths; var chosenstate;
					paths.do({ arg p;
						stateDict = Object.readArchive(p);
					});
					stateNum = stateDict.size;

					params = stateDict.at("state 1".asSymbol); // get the 1st state
					soundFuncArray = params[0].copy;
					slValues = params[1].copy;
					stepsValues = params[2].copy;
					trackVolumes = params[3].copy; 
					resolution = params[4].copy; 
					globalvol = params[5].copy; 
					dropcount = params[6].copy;
					speed = params[7].copy;
					outbus = params[8].copy;
					draw = params[9].copy;
					
					statesPop.items_(stateDict.keys.asArray.sort);
					msl.indexThumbSize_(680/dropcount);
					volMsl.indexThumbSize_(680/dropcount);
					msl.value_(slValues);
					volMsl.value_(trackVolumes);
					if(tonal.not, {resSlider.valueAction_(resolution)});
					globalVolSl.value_(globalvol);
					dropsSl.value_(dropcount);
					speedSl.value_(speed);
					outbusPop.value_(outbus/2);
					drawButt.value_(draw.binaryValue);
					generateNumBoxArray.(dropcount);
					soundFuncPop.value_();
				},{
					"cancelled".postln;
				});
			});

		saveArchive = GUI.button.new(win, Rect(60, 70, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["save", Color.black, Color.clear]])
			.action_({arg butt;
				GUI.dialog.savePanel({ arg path;
					stateDict.writeArchive(path++".rdr");
				},{
					"cancelled".postln;
				});
			});
		
		statesPop = GUI.popUpMenu.new(win, Rect(10, 93, 100, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(if(stateDict.size>0, {stateDict.keys.asArray.sort}, {["states"]}) )
			.value_(0)
			.background_(Color.white)
			.action_({ arg item; var chosenstate;
				if(stateNum > 0, { // if there are any states
					params = stateDict.at(item.items[item.value].asSymbol).deepCopy;
					soundFuncArray = params[0].copy;
					slValues = params[1].copy;
					stepsValues = params[2].copy;
					trackVolumes = params[3].copy; 
					resolution = params[4].copy; 
					globalvol = params[5].copy; 
					dropcount = params[6].copy;
					speed = params[7].copy;
					outbus = params[8].copy;
					draw = params[9].copy;
					// XXX
					msl.indexThumbSize_(680/dropcount);
					volMsl.indexThumbSize_(680/dropcount);
					msl.value_(slValues);
					volMsl.value_(trackVolumes);
					if(tonal.not, {resSlider.valueAction_(resolution)});
					globalVolSl.value_(globalvol);
					dropsSl.value_(dropcount);
					speedSl.value_(speed.reciprocal);
					outbusPop.value_(outbus/2);
					//[\draw, draw].postln;
					//[\drawbinary, draw.binaryValue].postln;
					drawButt.value_(draw.binaryValue);
					generateNumBoxArray.(dropcount);
					bufferPop.value_(soundFuncArray[msl.index].buffer); // set the sound and buffer to the selected drop
					soundFuncPop.value_(soundFuncArray[msl.index].playFunc);
					
					if(drawButt.value == 1, {
						drawer.clearDrawing;
						draw = true;
						change = true;
						drawer.refresh;
					});

				});
				volMsl.focus(true);
			});
			
		clearButt = GUI.button.new(win, Rect(10, 113, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["clear", Color.black, Color.clear]])
			.action_({arg butt;
				stateNum = 0;
				stateDict = ();
				statesPop.items_(["states"]);
			});

		storeButt = GUI.button.new(win, Rect(60, 113, 47, 18))
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["store", Color.black, Color.clear]])
			.action_({arg butt; var statesarray;
				
				stateNum = stateNum + 1;
				statesPop.items_(Array.fill(stateNum, {|i| "state "++(i+1).asString}));
				statesPop.value_(stateNum-1);
				
				params = [
					soundFuncArray.deepCopy,
					slValues.copy,
					stepsValues.copy,
					trackVolumes.copy, 
					resolution.copy, 
					globalvol.copy,
					dropcount.copy,
					speed.copy,
					outbus.copy,
					draw.copy
				];
				//[\paramsdraw, params[9]].postln;
				stateDict.add(("state "++stateNum.asString).asSymbol -> params.copy);
				soundFuncArray = soundFuncArray.deepCopy; // make a new stamp of sndfarray
			});

stepsSl = OSCIISlider(win, Rect(10, 136, 100, 8), "- steps", 2, 32, 8, 1)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ |sl| 
				stepsValues = Array.fill(dropcount, { sl.value });
				msl.step_( sl.value.reciprocal );
				numberBoxArray.do({|box, i| box.value_( sl.value ) });
				if(drawButt.value == 1, {
					drawer.clearDrawing;
					draw = true;
					change = true;
					drawer.refresh;
				});
			});

dropsSl = OSCIISlider(win, Rect(10, 163, 100, 8), "- drops", 2, 48, params[6], 1)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ |sl| 
				dropcount = sl.value; 
				// upper multislider
				slValues = slValues.extend(dropcount.asInteger, sl.value.reciprocal); // XXX
				stepsValues = stepsValues.extend(dropcount.asInteger, stepsSl.value);
				msl.indexThumbSize_(680/dropcount);
				msl.value_(slValues);
				params[1] = slValues;
				params[2] = stepsValues; 
				
				// lower multislider
				trackVolumes = trackVolumes.extend(dropcount.asInteger, 0.0); // silent on grow
				volMsl.indexThumbSize_(680/dropcount);
				volMsl.value_(trackVolumes);
				win.refresh;
				generateNumBoxArray.(dropcount);
				params[3] = trackVolumes;
				params[6] = dropcount;
				
				if(drawButt.value == 1, {
					drawer.clearDrawing;
					draw = true;
					change = true;
					drawer.refresh;
				});
			});

speedSl = OSCIISlider(win, Rect(10, 190, 100, 8), "- speed", 2, 16, params[7], 0.1)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ |sl| 
				speed = sl.value.reciprocal;
				params[7] = speed;
			});

		setToTopButt = GUI.button.new(win, Rect(10, 218, 47, 18))
			.states_([["init", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt;
				slValues = Array.fill(dropcount, {1.0});
				{ msl.value_(slValues) }.defer;
			});

		randButt = GUI.button.new(win, Rect(60, 218, 47, 18))
			.states_([["scramble", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt;
				slValues = Array.fill(dropcount, {|i| 
							1.0.rand.round(stepsValues[i].reciprocal) // so it lands in place
						});
				{ msl.value_(slValues) }.defer;
			});

		GUI.button.new(win, Rect(10, 238, 47, 18))
			.states_([["reset", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt;
				slValues = Array.fill(dropcount, {1.0});
				stepsValues = Array.fill(dropcount, {stepsSl.value});
				generateNumBoxArray.(dropcount);
				if(drawButt.value == 1, { // if drawing, then draw
					drawer.clearDrawing;
					draw = true;
					change = true;
					drawer.refresh;
				});
				{ msl.value_(slValues) }.defer;
			});

		GUI.button.new(win, Rect(60, 238, 47, 18))
			.states_([["rand", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt;
				slValues = Array.fill(dropcount, {1.0.rand});
				stepsValues = Array.fill(dropcount, {exprand(2,32).round(1)});
				numberBoxArray.do({|box, i| box.value_(stepsValues[i] ) });
				if(drawButt.value == 1, {
					drawer.clearDrawing;
					draw = true;
					change = true;
					drawer.refresh;
				});
				{ msl.value_(slValues) }.defer;
			});

	drawButt = OSCIIRadioButton(win, Rect(10, 266, 14, 14), "draw")
		.font_(GUI.font.new("Helvetica", 9))
		.value_( if(draw, {1}, {0}) )
		.action_({arg bt; 
			if( bt.value == 1, { 
				draw = true;
				change = true;
				drawer.refresh;
			}, { 
				drawer.clearDrawing;
				draw = false;
				change = true;
				drawer.refresh;
			});
			params[9] = draw; 
		});

	globalVolSl = OSCIISlider(win, Rect(10, 290, 100, 8), "- vol", 0, 1, params[5], 0.01, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ |sl| 
				globalvol = sl.value;	
				params[5] = globalvol;			
			});

	outbusPop = GUI.popUpMenu.new(win, Rect(10, 316, 50, 16))			.font_(GUI.font.new("Helvetica", 9))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(params[8]/2)
			.canFocus_(false)
			.background_(Color.white)
			.action_({ arg ch;
				outbus = ch.value * 2;
				params[8] = outbus;
			});

	startButt = GUI.button.new(win, Rect(65, 316, 45, 16))
			.states_([["start", Color.black, Color.clear],
					["stop", Color.black, XiiColors.onbutton]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt;
				if(butt.value == 1, { timeTask.start }, { timeTask.stop });
				volMsl.focus(true);
			});

	timeTask = Task({
		inf.do({
			var tempsynthdef, code;
			slValues = slValues.collect({|val, i| 
				val = val - stepsValues[i].reciprocal; 
				if(val< -0.05, { // BANG! - making noise
					val = 1.0 - stepsValues[i].reciprocal;
					if(soundFuncArray[i].codeFlag == false, { // evaluating synths
						freq = soundFuncArray[i].freq;
						amp = soundFuncArray[i].amp; 
						pitchratio = soundFuncArray[i].pitchratio;
						buffer = soundFuncArray[i].buffer;
						inbus = soundFuncArray[i].inbus;
						playFuncsArray[soundFuncArray[i].playFunc].value;
					}, { // evaluating sc-code
						code = soundFuncArray[i].code;
						if(code[code.size-4..code.size] == "play", { // is it a synthdef??
							tempsynthdef = (soundFuncArray[i].code++"(outbus:"+outbus+")").interpret;
							{server.sendMsg(\d_free, tempsynthdef.defName);}.defer(0.1); // free temp synthDef
						}, { // or just any old sc-code??
							soundFuncArray[i].code.interpret.value; // if it's not synth code
						});
					});
				});
				val
			});
			{ msl.value_(slValues) }.defer;
			speed.wait;
		});
	});

	createCodeWin = {arg dictIndex, slot;
		var funcwin, func, subm, test, view;
		funcwin = GUI.window.new("scode", Rect(600,400, 400, 200)).front;
		view = funcwin.view;
		func = GUI.textView.new(view, Rect(20, 20, 360, 120))
				.font_(GUI.font.new("Monaco", 9))
				.resize_(5)
				.focus(true)
				.string_(
					soundFuncArray[slIndex].code.asString;
				);
		test = GUI.button.new(view, Rect(270,150,50,18))
				.states_([["test",Color.black,Color.clear]])
				.resize_(9)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					func.string.interpret.value;
				});
				
		subm = GUI.button.new(view, Rect(330,150,50,18))
				.states_([["submit",Color.black,Color.clear]])
				.resize_(9)
				.focus(true)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					soundFuncArray[slIndex].codeFlag = true;
					soundFuncArray[slIndex].code = func.string;
					funcwin.close;
				});
	};

	createAudioStreamBusWin = {arg index;
		var envview, timesl, setButt;
		aswin = GUI.window.new("audiostream inbus", Rect(200, 450, 250, 100), resizable:false).front;
		aswin.alwaysOnTop = true;
		
		GUI.staticText.new(aswin, Rect(20, 55, 20, 16))
			.font_(GUI.font.new("Helvetica", 9)).string_("in"); 

		GUI.popUpMenu.new(aswin, Rect(35, 55, 50, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(10)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch;
				if(selectall == true, { // all drops selected
					soundFuncArray.do({ |dict| dict.put(\inbus, ch.value * 2) });
				}, {
					soundFuncArray[slIndex].put(\inbus, ch.value * 2);
				});
			});

		setButt = GUI.button.new(aswin, Rect(120, 55, 60, 16))
			.states_([["set inbus", Color.black, Color.clear]])
			.focus(true)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({
				aswin.close;
			});
	};
	
	createScalesWin = {
		var envview, timesl, setButt, setRandButt, clearButt;
		scalewin = GUI.window.new("scales and chords", 
						Rect(200, 450, 250, 100), resizable:false).front;
		scalewin.alwaysOnTop = true;
		notes = (0..11);
		
		GUI.staticText.new(scalewin, Rect(10, 15, 40, 16))
			.font_(GUI.font.new("Helvetica", 9)).string_("scales :"); 

		GUI.popUpMenu.new(scalewin, Rect(50, 15, 90, 16))
			.items_(Array.fill(XiiTheory.scales.size, {arg i; XiiTheory.scales[i][0]}))
			.value_(0)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.action_({ arg ch;
				scale = true;
				notes = XiiTheory.scales[ch.value][1];
				keyboard.showScale(notes, fundamental, Color.new255(103, 148, 103));
			});

		GUI.staticText.new(scalewin, Rect(10, 35, 40, 16))
			.font_(GUI.font.new("Helvetica", 9)).string_("chords :"); 

		GUI.popUpMenu.new(scalewin, Rect(50, 35, 90, 16))
			.items_(Array.fill(XiiTheory.chords.size, {arg i; XiiTheory.chords[i][0]}))
			.value_(0)
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.action_({ arg ch;
				scale = true;
				notes = XiiTheory.chords[ch.value][1];
				keyboard.showScale(notes, fundamental, Color.new255(103, 148, 103));
			});

		setRandButt = GUI.button.new(scalewin, Rect(150, 15, 60, 16))
				.states_([["set random", Color.black, Color.clear]])
				.canFocus_(false)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					var scale;
					scale = notes + 36; // one octave
					6.do({|i| scale = scale++(notes+36+((i+1)*12)) }); // 6 other octaves
					soundFuncArray.do({ |dict| dict.freq = scale.choose.midicps; });
				});
		
		clearButt = GUI.button.new(scalewin, Rect(150, 35, 60, 16))
				.states_([["clear keyb", Color.black, Color.clear]])
				.canFocus_(false)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					scale = false;
					keyboard.clear;
				});
				
		GUI.button.new(scalewin, Rect(50, 65, 160, 16))
				.states_([["set last note as fundamental", Color.black, Color.clear]])
				.canFocus_(false)
				.font_(GUI.font.new("Helvetica", 9))
				.action_({
					keyboard.clear;
					fundamental = note;
					keyboard.showScale(notes, fundamental, Color.new255(103, 148, 103));
				});
	};

	playFuncsArray = [
			 { // |freq=440, amp=1, pitchratio=1, buffer=0| 
				var myBuffer, selStart, selEnd; // the sample player
				if(try{XQ.globalBufferDict.at(poolName)[0][buffer]} != nil, {
					myBuffer = XQ.globalBufferDict.at(poolName)[0][buffer];
					selStart = XQ.globalBufferDict.at(poolName)[1][buffer][0];
					selEnd = selStart + XQ.globalBufferDict.at(poolName)[1][buffer][1];

					if(myBuffer.numChannels == 1, {
						Synth(\xiiSounddrops1x2, [	\outbus, outbus,
											\bufnum, myBuffer.bufnum, 
											\startPos, selStart, 
											\endPos, selEnd,
											\amp, amp,
											\vol, globalvol
						])
					},{
						Synth(\xiiSounddrops2x2, [	\outbus, outbus,
											\bufnum, myBuffer.bufnum, 
											\startPos, selStart, 
											\endPos, selEnd,
											\amp, amp,
											\vol, globalvol
						])
					});
				});
			},
			{
					Synth(\xiiSine, [		\outbus, outbus,
										\freq, freq,
										\phase, 1.0.rand,
										\amp, amp * globalvol
					])
			},
			{
					Synth(\xiiBells, [		\outbus, outbus,
										\freq, freq,
										\amp, amp * globalvol
					])
			},
			{
					Synth(\xiiSines, [		\outbus, outbus,
										\freq, freq,
										\amp, amp * globalvol
					])
			},
			{
					Synth(\xiiSynth1, [	\outbus, outbus,
										\freq, freq,
										\amp, amp * globalvol
					])
			},
			{
					Synth(\xiiKs_string, [	\outbus, outbus,
										\note, freq, 
										\pan, 0.7.rand2, 
										\rand, 0.1+0.1.rand, 
										\delayTime, 2+1.0.rand,
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiString, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2, 
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiImpulse, [	\outbus, outbus,
										\pan, 0.7.rand2,
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiRingz, [		\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiKlanks, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiGridder, [	\outbus, outbus,
										\freq, freq, 
										\pan, 0.7.rand2,
										\amp, amp * globalvol
										]);
			},
			{
					Synth(\xiiAudioStream,[	\outbus, outbus,
										\inbus, inbus,
										\pitchratio, pitchratio, 
										//\pan, 0.7.rand2,
										\amp, amp * globalvol
										]);
			}
		];
		
		volMsl.focus(true);
		win.front;
		win.onClose_({
			var t;
			timeTask.stop;
			XQ.globalWidgetList.do({arg widget, i; if(widget == this, { t = i })});
			try{ XQ.globalWidgetList.removeAt(t) };
			try{ scalewin.close };
			try{ aswin.close };
		});
	}

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
			ldSndsGBufferList.value(XQ.globalBufferDict.keys.asArray[0]); // load first pool
		});
	}
	
	getState { // for save settings
		var point;
		point = Point(win.bounds.left, win.bounds.top);
		if(stateDict.size == 0, {
			stateDict.add("state 1".asSymbol -> params.copy); // we create a state
		});
		^[2, point, stateDict ];
	}
}


