


// NOTE on Trigger IDs: 
// BufferPlayer uses TriggerIDs from 50 to (number of instances * 50)
// AudioIn uses TriggerID number 800
// Recorder uses TriggerID nr 820
// Mushrooms uses TriggerID nr 840

// NEW IN VERSION 2:
// Amp slider in Recorder and in BufferPool
// Open sounds folder in Player
// two new instruments: LiveBuffers and Mushrooms
// Fixing loading of bufferpools (instruments would automatically load new sound)

// NEW IN VERSION 3:
// store settings
// bug fixes

// NEW IN VERSION 4:
// Relative tempi in PolyMachine and an increase up to 48 steps per track
// User definable number of tracks in PolyMachine
// User definable number of tracks in BufferPlayer
// PolyMachine: Fixing sc-code such that one does not have to submit code for each box
// BufferPool soundfile view now displays selections in the soundfile
// Fixing Gridder (the params argument so the transpose is set to 1 again)
// Fixing loadup of synthdefs in PolyMachine (removing from server)
// Optimising the distribution code
// Record fixed
// Fixing the route ordering of channels - now no need to restart effects
// Fixing amplifier
// Settings store bufferpools and their contents
// Effects remember their on/off state
// Refining small functions in SoundScratcher
// Fixing settings in the Quarks interface
// BufferPool and Recorder now get a new logical filename in text field when recording stops
// some new spectral effects
// new time domain effect called cyberpunk (thanks dan stowell for ugen)
// Added views that display frames, selection start and selection end in BufferPool SndFileView
// optimization of code
// got rid of all environmental variables and store envir vars in the XQ class
// soundfilefolder created on default if it doesn't exist
// new filter: Moog VCF

// NEW IN VERSION 5:
// new instrument: Sounddrops
// new tool: Theory (scales and chords)
// new instrument : Quanoon
// styles options in WaveScope
// keyboard grains mode in SoundScratcher instrument (both with drawn grains and without)
// Added a Function:record method. Now you can do {SinOsc.ar(222)}.record(3) // 3 sec file
// Added outbus in the SoundFilePlayer widget of BufferPool.
// Ported to SwingOSC
// new spectral effect: Speactral Delay
// Added better accessibility to bufferPools through the XQ class (good for live coding)
// fixing noise bug in synthdefs, (inserted a LPF instead of the RLPF)

// NEW IN VERSION 6:
// Adding a rec/play toggle button in the StratoSampler
// change in colours in Quanoon and adding keyboard for fundamental key
// fixing updates from bufferpools in instruments
// adding Z (undo) for grains in SoundScratcher
// adding pitchratio in Sounddrops (using the microtonal keyboard)
// Automation (path recording) of sliders. (Press A for automation, C for clearing)
// PolyMachine remebers states when stored in Settings
// "free" button in BufferPools GUI frees only the selected buffer not all buffers
// Adding Limiter to Recorder and BufferPool recording (thus no distortion possible)
// Preventing accidental stop (apple+dot) with a Warning window [Removed]
// Adding range slider in bufferplayer and view of soundfile
// Adding cropping to SoundfileView (in bufferpool and bufferplayer) - this makes editing better
// Adding support for different samplerate and bitdepth. (samplerate is detected from hardware but bitdepth set in the preferences file - internal bitdepth in sc is 32 but for written files other values can be set)
// Adding BitCrusher effect

// NEW IN VERSION 7:
// Double click to open instruments in ixi quarks view and sound pool
// BUGfix: Erros when playing quanoon and the cursor goes below the strings 
// Players remembers their sounds when settings are stored
// Stratosampler has 120 seconds buffer (not limited to 10 secs)
// Dragging soundfiles to Soundpools (former bufferPools) now possible
// Dragging soundfiles to Player
// Spectrogram view added to utilities
// ModalWindows Implemented instead of XiiAlert
// Added a ChannelMeter utility
// Changed cropping of soundfiles, such that the crop does not overwrite the original soundfile
// Added normalizer for soundfiles in BufferPools
// SoundScratcher has a better sounding Warp function (with controllable grain duration and speed)
// SoundScratcher has constrained vertical and horizontal drawing (by pressing "v" or "h")
// MasterEQ added in Filters
// NEW UTILITY: EventRecorder (useful in apps such as Gridder and Toshiomorpher)
// NEW Filters: Spreader, RMShelf, RMEQ, BPeakEQ, BBandStop, BBandPass, BLowShelf, BHiShelf
// NEW Instrument: ToshioMorpher
// Using NumberBox instead of XiiSNBox
// NEW: EffectGUIs are now controllable from code: 
/*
{Out.ar(20, Decay2.ar(Impulse.ar(0.5, 0.25), 0.01, 0.6, Saw.ar(222))!2)}.play
{Out.ar(22, Decay2.ar(Impulse.ar(0.5, 0.25), 0.01, 0.2, Saw.ar(999))!2)}.play

a = XiiDelay.new(s, 2).setInBus_(20).setOutBus_(0).setLoc_(600, 600).setSlider_(0, 0.11).start;
a.setSlider_(0, 0.122);
a.setSlider_(1, 0.8);
a.setInBus_(22)
b = XiiFreeverb.new(s, 2).setInBus_(30).setOutBus_(0).setLoc_(750, 600).start;
a.setOutBus_(30)
c = XiiLowpass.new(s, 2).setInBus_(20).setOutBus_(0).setLoc_(900, 600).start;
c.setOutBus_(30)
a.stop;
*/




// TODO: Create a "sine organ" (boxes that get bigger/smaller increasing amp of a sine. Grid. A setting can be stored and used in another mode where each sine "structure/combination" can be played with a mouseover or mousedown.
// TODO: Create a new app inspired by actor-network theory (where networks establish and disintigrate)
// TODO: create the synths. In them, make a "synthdef creator" which means that one can retrieve the parameters from the synth state as it is now (from GUI manipulation) and then create a new synthdef with specific name, etc. that can be later used with patterns etc.
// TODO: Create a clock (see Chronometer)
// TODO: make more Spectral plugins
// TODO: Test the Warp1MC Ugens that take and output multichannel (see mail sept 10, 2007)
// TODO: Make a mixer channel GUI
// TODO: fix and debug Dynamics: limiter, normalizer, gate, compressor, expander, etc.
// TODO: make ixiQuarks as Audio Units that could run in Logic
// TODO: add OSC output to all Instruments (so agents can send OSC out to other apps)
// TODO: Make an instrument called Picker that uses pickers to control f.ex. wavetables
// TODO: Make SSBD v3 interface as instrument (spacetime sequencer)
// TODO: Use SCLevelIndicator instead of 2D slider
// TODO: Make "Synthesisers and Samplers" as special subcategory that all instruments can use
// TODO: a = {Decimator.ar(SinOsc.ar(440, 0, 0.20), MouseX.kr(100, 44100), MouseY.kr(1, 32))}.play // make a BitCrusher???
// TODO: make flanger and phasor
// TODO: Fix modifiers for all keyboard systems (see "/Users/thor/quaziir/SC3/code/sc patches 2008/modifiers table.rtf")

// TODO: Add an aLife agent behaviour that controls sliders. (assignable... select a slider, click on "new" on the aLife thingy and the slider will automatically move) In the same GUI the agent could travel a saw, a square, a tri, a sine, and travel a trajectory drawn by the user. (might have to use a lag then in slider action)

// BUG?: Store settings - test better

// TODO: Fix EventRecorder
// TODO: Experiment with Convolver reverbs (Convolution reverb)

// TODO: Check using Coyote instead of Onsets in EventsAnalyser
// TODO: Fix timing when choosing a new sound in Player
// TODO: Support User generated synthdefs... (with default arguments)
// TODO: Explore sc3-plugins ugens, such as the mda piano synth, etc.
// TODO: Implement SwitchDelay
// TODO: Experiment with MdaPiano
// TODO: Implement the following in all instruments:
/*
	isCaps { ^this & 65536 == 65536}
	isShift { ^this & 131072 == 131072 }
	isCtrl { ^this & 262144 == 262144 }
	isAlt { ^this & 524288 == 524288 }
	isCmd { ^this & 1048576 == 1048576 } 
	(Methods of Integer)
*/

// TODO: Try out WarpZ (new ugen from Josh in JoshUgens) for SoundScratcher (zero-crossing)

// TODO: Make Onsets work in TrigRecorder synthdefs

// TODO: Sounds in Predators should change in the dropdown menu not in the red field
// TODO: Yes, there are a few things missing. I would love to see a way of patching an instrument through an envelope. For example, if I wanted to play the Predators for 50 seconds, it would be great to just kind of say "fade in for 10 secods, go play for 30 seconds then fade out for 10". I would then be free to do more things in real time. Maybe that functionality is there but I need to look at it more.  It would also be great to be able to patch some Basic UGens into certain parameters, for example, patch a Pink Noise UGen into panning or to amplitude, etc.
// TODO: Negative: you don't see the connections between modules (the way you do in Mulch or Bidule, for example). No need for Reason styled swinging 'cables', just a straight line indicating what feeds where. 


// BUG: In Predators, new Prey does take outbus 0 by default (bad if user has chosen say 20).
// BUG: In Predators, new Preys don't get new sound samples 
// BUG: In Predators, clicking on a prey and moving is not under cursor

// Make complex effects (such as delay where each delay is passed through a filter)


// KNOWN BUGS:
// - outbusses in mr. roque [maybe not???]
// - live coder not going through effects when created


/*
Explore:
"open /Users/thm21/quaziir/texts\ \[phd\]/PhD\ thesis/Rob\ Saunders\ Thesis/curiosity/index.html".unixCmd;
*/

/*
a = XQ.globalWidgetList[0].xiigui.getState
b = XiiDelay.new(s, 2, a);
	getState {
		^[channels, inbus, outbus, tgt, loc, param[3]];
	}
*/


/*

// HOW TO WORK WITH BUFFERPOOL IN YOUR OWN WORK....


// first load some sounds into a bufferpool

// then lets look at the global buffer dictionary of ixiQuarks:
XQ.globalBufferDict

// or if you have lots of pools:
Post << XQ.globalBufferDict 

// let's look at what pools we have open:
XQ.poolNames

// get the buffers of a pool
a = XQ.buffers('bufferPool 1')
// get the selections of a pool
a = XQ.selections('bufferPool 1')
// get the buffers and the selections of a pool
a = XQ.bufferList('bufferPool 1')

// now you can play the first buffer in your buffer pool.
a[0].play

// or (if your buffer is a mono sound)
(
x = SynthDef("help-Buffer",{ arg out = 0, bufnum;
	Out.ar( out,
		PlayBuf.ar(1, bufnum, BufRateScale.kr(bufnum))
	)
}).play(s,[ \bufnum, a[0].bufnum ]);
)

Also, in code you can do this:
a = XiiDelay.new(s, 2).setInBus_(2).start
a.setOutBus_(10)

// check for versions (Pipe locks the machine if it's not online)
(
var version, pipe;
// check if user is online: (will return a 4 digit number if not)
a = "curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt".systemCmd;
// then get the version number (from a textfile with only one number in it)
if(a==0, {
	pipe = Pipe.new("curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt", "r");
	version = pipe.getLine; 
	pipe.close;	
});
"current version is ".post; version.postln;
)


// useful for debugging gui:

(
Crucial.initLibraryItems;
Library.at(\menuItems,\tools,\guiDebugger).value;
)

*/

// BUG: Stratosampler does not run the right prelevel and reclevels if stopped and restarted 



// BUG: in BufferPlayer - Fix range sliders moving when looking at sample 




XiiQuarks {	

	var thisversion = 7;

	*new { 
		^super.new.initXiiQuarks;
	}
		
	initXiiQuarks {
	
		var win, txtv, quarks, serv, channels;
		var openButt, widgetCodeString, monoButt, stereoButt, widget;
		var name, point;
		var midi, midiControllerNumbers, midiRotateWindowChannel, midiInPorts, midiOutPorts;
		var openSndFolder;
		var chosenWidget, widgetnum, types, typesview, ixilogo;
		var settingRegister, settingNameView, storeSettingButt, comingFromFieldFlag, settingName;
		var storedSettingsPop, loadSettingButt, deleteSettingButt, clearScreenButt;
		var prefFile, preferences, synthdefs;
				
		settingRegister = XiiSettings.new; // activate the settings registry

		//GUI.cocoa;
		synthdefs = SynthDescLib(\xiiquarks);
		// read synth defs from file
		("ixiquarks/synthdefs.scd").loadPath;
	//	SynthDescLib.getLib(\xiilang).synthDescs.keys.asArray;
		{SynthDescLib.global.read}.defer(2);
		
		XQ.new; // A class containing all the settings and environment maintenance
		
		XQ.preferences; // retrieve preferences from the "preferences.ixi" file
		// add the preferences file to the preferences Menu Item.
		thisProcess.preferencesAction_({ "open 'ixiquarks/preferences/preferences.ixi'".unixCmd; });

		// Server.default.options.device = XQ.pref.device; // the audio device (soundcard)
		midi = XQ.pref.midi; // if you want to use midi or not (true or false)
		midiControllerNumbers = XQ.pref.midiControllerNumbers; // evolution mk-449c
		midiRotateWindowChannel = XQ.pref.midiRotateWindowChannel;
		midiInPorts = XQ.pref.midiInPorts;
		midiOutPorts = XQ.pref.midiOutPorts;
		if(XQ.pref.emailSent == false, {
			"open ixiquarks/preferences/email.html".unixCmd;
		});
		
		XiiACDropDownChannels.numChannels_( XQ.pref.numberOfChannels ); // NUMBER OF AUDIO BUSSES

		//////////////////////////////////////////////

		XiiSynthDefs.new(Server.default);

		name = " ixi quarks";
		point = XiiWindowLocation.new(name);
		
		win = GUI.window.new(name, Rect(point.x, point.y, 275, 224), resizable:false);
		
		comingFromFieldFlag = false;
		settingName = "preset_0";
		
		quarks = [ 
			["AudioIn", "Recorder", "Player", "BufferPool", "PoolManager", 
			"EventRecorder", "FreqScope", "WaveScope", "Spectrogram", "EQMeter", "MixerNode", 
			"ChannelSplitter", "Amplifier", "ChannelMeter", "TrigRecorder", 
			"MusicTheory"],
	
			["SoundScratcher", "ToshioMorpher", "SoundSculptor",  "Slicer", "SoundDrops", 
			"Predators", "Gridder", "PolyMachine", "GrainBox", "Quanoon", "StratoSampler", 
			"BufferPlayer", "Mushrooms", "ScaleSynth", "LiveCoder"], 
			
			["Delay", "Freeverb", "AdCVerb", "Distortion", "ixiReverb", "Chorus",
			"Octave", "Equalizer", "CyberPunk", "Tremolo", "BitCrusher", "CombVocoder", 
			"RandomPanner", "MRRoque", "MultiDelay"],
			
			["MasterEQ", "Bandpass", "Lowpass", "Highpass", "RLowpass", "RHighpass", 
			"Resonant", "Klanks", "Spreader", "RMShelf", "RMEQ", "BPeakEQ", 
			"BBandStop", "BBandPass", "BLowShelf", "BHiShelf", "MoogVCF", "MoogVCFFF"],
			
			["SpectralEQ", "MagClip", "MagSmear", "MagShift", "MagFreeze", 
			"RectComb", "BinScramble", "BinShift", "SpectralDelay"],
			
			["Limiter", "Normalizer", "Gate", "Compressor", "Sustainer", "NoiseGate", "Expander"],
			
			["Noise", "Oscillators", "Drawer"]

//			["Noise", "Oscillators", "Drawer", "Mp3Player", "ImageConverter"],
			
//			["SpaceMachine", "TimeMachine", "MindMachine", "IceMachine", "GlitchMachine"] // add machines to types below
		];
		
		types = ["utilities", "instruments", "effects", "filters", "spectral", "dynamics", "other"];
		
		ixilogo = [ // the ixi logo
			Point(1,7), Point(8, 1), Point(15,1), Point(15,33),Point(24, 23), Point(15,14), 
			Point(15,1), Point(23,1),Point(34,13), Point(45,1), Point(61,1), Point(66,6), 
			Point(66,37), Point(59,43), Point(53,43), Point(53,12), Point(44,22), Point(53,33), 
			Point(53,43), Point(42,43), Point(34,32),Point(24,43), Point(7,43), Point(1,36), Point(1,8)
			];

		channels = 2;
		widget = "AudioIn";

		typesview = GUI.listView.new(win,Rect(10,10, 120, 108))
			.items_(types)
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				txtv.items_(quarks[sbs.value]);
				txtv.value_(0);
				widget = quarks[sbs.value][txtv.value];
			})
			.enterKeyAction_({|view|
				txtv.value_(0);
				txtv.focus(true);
			})
			// KEYCODE HERE AT THE END WILL MAKE IT Qt COMPATIBLE
			.keyDownAction_({arg view, char, modifiers, unicode, keycode;
				if (unicode == 16rF700, { typesview.valueAction = typesview.value - 1;  });
				if (unicode == 16rF703, { txtv.focus(true); });
				if (unicode == 16rF701, { typesview.valueAction = typesview.value + 1;  });
				if (unicode == 16rF702, { typesview.valueAction = typesview.value - 1;  });
			});

		if(GUI.id == \cocoa, {	typesview.focusColor_(XiiColors.darkgreen.alpha_(0.9)) });

		storedSettingsPop = GUI.popUpMenu.new(win, Rect(10, 128, 78, 16)) // 550
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.items_(settingRegister.getSettingsList)
			.background_(Color.white);

		loadSettingButt = GUI.button.new(win, Rect(95, 128, 35, 17))
			.states_([["load", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.loadSetting(storedSettingsPop.items[storedSettingsPop.value]);
			});

		settingNameView = GUI.textView.new(win, Rect(10, 151, 78, 14))
			.font_(GUI.font.new("Helvetica", 9))
			.string_(settingName = PathName(settingName).nextName)
			.keyDownAction_({arg view, key, mod, unicode; 
				if(unicode ==13, {
					comingFromFieldFlag = true;
					storeSettingButt.focus(true);
				});
			});
		
		storeSettingButt = GUI.button.new(win, Rect(95, 150, 35, 17))
			.states_([["store", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingName = PathName(settingNameView.string).nextName;
				settingRegister.storeSetting(settingNameView.string);
				storedSettingsPop.items_(settingRegister.getSettingsList);
				settingNameView.string_(settingName);
			})
			.keyDownAction_({arg view, key, mod, unicode; // if RETURN on bufNameView
				if(unicode == 13, {
					if(comingFromFieldFlag, {
						"not storing setting".postln;
						comingFromFieldFlag = false;
					},{
						settingRegister.storeSetting(settingNameView.string);
						storedSettingsPop.items_(settingRegister.getSettingsList);
					})
				});
				settingName = PathName(settingNameView.string).nextName;
				settingNameView.string_(settingName);
			});

		deleteSettingButt = GUI.button.new(win, Rect(95, 172, 35, 17))
			.states_([["delete", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.removeSetting(storedSettingsPop.items[storedSettingsPop.value]);
				storedSettingsPop.items_(settingRegister.getSettingsList);
			});

		clearScreenButt = GUI.button.new(win, Rect(95, 194, 35, 17))
			.states_([["clear", Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({arg butt; 
				settingRegister.clearixiQuarks;
			});

		txtv = GUI.listView.new(win,Rect(140,10, 120, 152))
			.items_(quarks[0])
			.hiliteColor_(XiiColors.darkgreen) //Color.new255(155, 205, 155)
			.background_(XiiColors.listbackground)
			.selectedStringColor_(Color.black)
			.action_({ arg sbs;
				("Xii"++quarks[typesview.value][sbs.value]).postln;
				widget = quarks[typesview.value][sbs.value];
			})
			.enterKeyAction_({|view|
				widget = quarks[typesview.value][view.value];
				widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
				XQ.globalWidgetList.add(widgetCodeString.interpret);
			})
			.mouseDownAction_({|view, x, y, modifiers, buttonNumber, clickCount|
				if(clickCount == 2, {
					widget = quarks[typesview.value][view.value];
					widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(widgetCodeString.interpret);
			    })
			})
			.keyDownAction_({arg view, char, modifiers, unicode;
				if(unicode == 13, {
					widget = quarks[typesview.value][view.value];
					widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(widgetCodeString.interpret);
				});
				if (unicode == 16rF700, { txtv.valueAction = txtv.value - 1;  });
				if (unicode == 16rF703, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF701, { txtv.valueAction = txtv.value + 1;  });
				if (unicode == 16rF702, { typesview.focus(true);  });
			});
			
		if(GUI.id == \cocoa, {	txtv.focusColor_(XiiColors.darkgreen.alpha_(0.9)) });
		
		stereoButt = OSCIIRadioButton(win, Rect(140, 174, 12, 12), "stereo")
					.value_(1)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
							channels = 2;
							monoButt.value_(0);
							});
					});

		monoButt = OSCIIRadioButton(win, Rect(140, 192, 12, 12), "mono ")
					.value_(0)
					.font_(GUI.font.new("Helvetica", 9))
					.action_({ arg butt;
							if(butt.value == 1, {
								channels = 1;
								stereoButt.value_(0);
							});	
					});

		openSndFolder = GUI.button.new(win, Rect(195, 184, 13, 18))
				.states_([["f",Color.black,Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					"open sounds/ixiquarks/".unixCmd
				});
								
		openButt = GUI.button.new(win, Rect(210, 184, 50, 18))
				.states_([["Open",Color.black,Color.clear]])
				.font_(GUI.font.new("Helvetica", 9))
				.canFocus_(false)
				.action_({ arg butt;
					widgetCodeString = "Xii"++widget++".new(Server.default,"++channels++")";
					XQ.globalWidgetList.add(widgetCodeString.interpret);
				});
				
		// MIDI control of sliders		
		if(midi == true, {
			"MIDI is ON".postln;
			MIDIIn.control = { arg src, chan, num, val;
				var wcnt;					
				if(num == midiRotateWindowChannel, {
					{
					wcnt = GUI.window.allWindows.size;
					if(XQ.globalWidgetList.size > 0, {
						chosenWidget = val % wcnt;
						GUI.window.allWindows.at(chosenWidget).front;
						XQ.globalWidgetList.do({arg widget, i;
							if(widget.xiigui.isKindOf(XiiEffectGUI), {
								if(GUI.window.allWindows.at(chosenWidget) === widget.xiigui.win, {
									widgetnum = i;
								});
							});
						});
					});
					}.defer;
				},{
				{
				XQ.globalWidgetList[widgetnum].xiigui.setSlider_(
					midiControllerNumbers.detectIndex({arg i; i == num}), val/127);
				}.defer;
				});
			};
			
			MIDIClient.init(midiInPorts,midiOutPorts);
			midiInPorts.do({ arg i; 
				MIDIIn.connect(i, MIDIClient.sources.at(i));
			});
		});
		
		win.onClose_({ 
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		}); 
	
		txtv.focus(true);
		
		win.drawHook = {
			GUI.pen.color = XiiColors.ixiorange;
			GUI.pen.width = 3;
			GUI.pen.translate(30,182);
			GUI.pen.scale(0.6,0.6);
			GUI.pen.moveTo(1@7);
			ixilogo.do({arg point;
				GUI.pen.lineTo(point+0.5);
			});
			GUI.pen.stroke;
		};
		win.refresh;
		win.front;
		if (GUI.id == \cocoa, { this.addixiHelpMenu }, { this.addixiHelpMenu_J });

	}
	
	
	addixiHelpMenu {
		// add feedback mechanism, suggestions etc.
		// update ixiQuarks - check if this is the latest version
		
		var a;
		a = SCMenuGroup(nil, "ixi", 10);
		
		SCMenuItem(a, "Feedback")
			.action_({
		"open http://www.ixi-audio.net/content/download/ixiquarks/ixiq_feedback.html".unixCmd
			});
		SCMenuSeparator(a, 1); // add a separator
			
		SCMenuItem(a, "Check for Update")
			.action_({
				var latestversion, pipe;
				// check if user is online: (will return a 4 digit number if not)
				a = "curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt".systemCmd;
				// then get the version number (from a textfile with only one number in it)
				if(a==0, {
					pipe = Pipe.new("curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt", "r");
					latestversion = pipe.getLine; 
					pipe.close;
					[\latestversion, latestversion, \thisversion, thisversion].postln;
					if(latestversion.asInteger>thisversion, {
						XiiAlert.new("New version (version "++latestversion++") is available on the ixi website");
						{"open http://www.ixi-audio.net".unixCmd}.defer(2.5); // allow for time to read
					});
				});
			});

		//b.setShortCut("o", true, false);//cmd-opt-o
	}

	addixiHelpMenu_J {
		// add feedback mechanism, suggestions etc.
		// update ixiQuarks - check if this is the latest version
		
		var a;
		//a = JSCMenuGroup(nil, "ixi", 10);
		a = JSCMenuGroup( JSCMenuRoot.new(nil), "ixi", 10);
		

		JSCMenuItem(a, "Feedback")
			.action_({
		"open http://www.ixi-audio.net/content/download/ixiquarks/ixiq_feedback.html".unixCmd
			});
		JSCMenuSeparator(a, 1); // add a separator
			
		JSCMenuItem(a, "Check for Update")
			.action_({
				var latestversion, pipe;
				// check if user is online: (will return a 4 digit number if not)
				a = "curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt".systemCmd;
				// then get the version number (from a textfile with only one number in it)
				if(a==0, {
					pipe = Pipe.new("curl http://www.ixi-audio.net/content/download/ixiquarks/version.txt", "r");
					latestversion = pipe.getLine; 
					pipe.close;
					[\latestversion, latestversion, \thisversion, thisversion].postln;
					if(latestversion.asInteger>thisversion, {
						XiiAlert.new("New version (version "++latestversion++") is available on the ixi website");
						{"open http://www.ixi-audio.net".unixCmd}.defer(2.5); // allow for time to read
					});
				});
			});

		//b.setShortCut("o", true, false);//cmd-opt-o
	}
}
