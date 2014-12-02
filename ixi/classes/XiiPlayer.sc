XiiPlayer {	

	var <>xiigui;
	var <>win, params;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiPlayer(server, channels, setting);
		}
		
	initXiiPlayer {arg server, channels, setting;
		var bgColor, foreColor, spec, outbus;
		var s, name, point;
		var txtv, refreshButton, playButton, r, filename, timeText, secTask;
		var soundfile, player, buffer, task, volSlider, openButton, volume;
		var folderSounds, addedSounds, addedSoundNames, sounds, soundNames;
		var cmdPeriodFunc, group, openFolderButton;
		
		addedSounds = []; // Cocoa dialog will fill this.
		addedSoundNames = [];
//		folderSounds = Cocoa.getPathsInDirectory("sounds/ixiquarks"); // the default sounds folder
		folderSounds = "sounds/ixiquarks/*".pathMatch; // the default sounds folder
		folderSounds = folderSounds.collect({|path| path.basename});
		if(folderSounds[0]==".DS_Store", {folderSounds.removeAt(0)}); // get rid of this shit
		soundNames = folderSounds.copy;
		soundNames.do({arg item, i; soundNames[i] = soundNames[i].splitext[0]; });

		folderSounds.do({arg item, i; folderSounds[i] = "sounds/ixiquarks/"++item; });
		sounds = folderSounds;
		
		filename = "";
		name = "- player -";
		s = server ? Server.default;
		
		xiigui = nil; // not using window server class here
		point = if(setting.isNil, {XiiWindowLocation.new(name)}, {setting[1]});
		params = if(setting.isNil, {[0, 0, sounds[0]]}, {setting[2]});
		soundfile = params[2]; // default soundfile
		volume = params[1];
		outbus = params[0]*2;
		
		bgColor = Color.new255(155, 205, 155);
		foreColor = Color.new255(103, 148, 103);
		
		win = GUI.window.new(name, Rect(point.x, point.y, 222, 195), false);

		timeText = GUI.staticText.new(win, Rect(60, 130, 40, 18))
					.string_("00:00");
					
		txtv = GUI.listView.new(win, Rect(10,10, 200, 110))
			.items_(soundNames)
			.background_(Color.new255(155, 205, 155, 60))
			.hiliteColor_(Color.new255(103, 148, 103)) //Color.new255(155, 205, 155)
			.selectedStringColor_(Color.black)
			.value_(folderSounds.loop({arg snd, i, break; if(snd == soundfile, { break.(i) }) }))
			.action_({ arg sbs;
				soundfile = sounds[sbs.value];
				params[2] = soundfile;
			})
			.canReceiveDragHandler_(true)
			.receiveDragHandler_({ 
				//v.items = v.items.addAll(SCView.currentDrag);
				//this.loadBuffers(SCView.currentDrag);
				SCView.currentDrag.do({ arg p;
					addedSounds = addedSounds.add(p);
					addedSoundNames = addedSoundNames.add(p.basename);
				});
				
				//folderSounds = Cocoa.getPathsInDirectory("sounds/ixiquarks");
				//folderSounds = "sounds/ixiquarks/*".pathMatch;
				//if(folderSounds[0]==".DS_Store", {folderSounds.removeAt(0)});
				//folderSounds = folderSounds.collect({|path| path.basename});
				
				soundNames = soundNames ++ addedSoundNames;
				sounds = folderSounds ++ addedSounds;
				txtv.items_(soundNames);
				if(sounds.size == 1, {
					soundfile = sounds[0];
				});

			});


		GUI.popUpMenu.new(win, Rect(10, 130, 44, 16))
			.items_(XiiACDropDownChannels.getStereoChnList)
			.value_(params[0])
			.background_(Color.white)
			.font_(GUI.font.new("Helvetica", 9))
			.canFocus_(false)
			.action_({ arg ch;
				outbus = ch.value * 2;
				player.set(\outbus, outbus);
				params[0] = ch.value;
			});

		refreshButton = GUI.button.new(win, Rect(96, 130, 45, 18))
			.states_([["Refresh",Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ 
				//folderSounds = Cocoa.getPathsInDirectory("sounds/ixiquarks");
				folderSounds = "sounds/ixiquarks/*".pathMatch; // the default sounds folder
				sounds = folderSounds ++ addedSounds;
				
				folderSounds = folderSounds.collect({|path| path.basename});
				if(folderSounds[0]==".DS_Store", {folderSounds.removeAt(0)});
				soundNames = folderSounds.copy ++ addedSoundNames;
				// take the file extension away
				soundNames = soundNames.collect({arg snd; snd.splitext[0]; });
				txtv.items_(soundNames);
			});	
			
		openFolderButton = GUI.button.new(win, Rect(147, 130, 18, 18))
			.states_([["f",Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt;
				"open sounds/ixiquarks/".unixCmd;
			});
				
		openButton = GUI.button.new(win, Rect(170, 130, 40, 18))
			.states_([["Open",Color.black, Color.clear]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt;
				GUI.dialog.getPaths({ arg paths;

					paths.do({ arg p;
						addedSounds = addedSounds.add(p);
						addedSoundNames = addedSoundNames.add(p.basename);
					});
					
					//folderSounds = Cocoa.getPathsInDirectory("sounds/ixiquarks");
					folderSounds = "sounds/ixiquarks/*".pathMatch;
					if(folderSounds[0]==".DS_Store", {folderSounds.removeAt(0)});
					folderSounds = folderSounds.collect({|path| path.basename});
					sounds = folderSounds ++ addedSounds;
					txtv.items_(soundNames);
					if(sounds.size == 1, {
						soundfile = sounds[0];
					});
				},{
					"open soundfile cancelled".postln;
				});
			});
				
		playButton = GUI.button.new(win, Rect(160, 158, 50, 18))
			.states_([["Play",Color.black, Color.clear], ["Stop",Color.black,Color.green(alpha:0.2)]])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({ arg butt; var a, chNum, dur, filepath, pFunc;
				if(txtv.items.size > 0, {
					[\soundfile, soundfile].postln;
					if(PathName(soundfile).isFolder.not, {
					if(butt.value == 1, {
						group = Group.new(server, \addToHead);
						a = SoundFile.new;
						a.openRead(soundfile);
						chNum = a.numChannels;
						//[\chNum, chNum].postln;
						dur = (a.numFrames/s.sampleRate); // get the duration - add / chNum
						a.close;											buffer = Buffer.cueSoundFile(s, soundfile, 0, chNum);
						// play it
						task = Task({
							inf.do({ 	
								if(chNum == 1, {
									player = Synth(\xiiPlayer1, 
									[\outbus, outbus, \bufnum, buffer.bufnum, \vol, volume],
									group, \addToHead);
									}, {
									player = Synth(\xiiPlayer2, 
									[\outbus, outbus, \bufnum, buffer.bufnum, \vol, volume],
									group, \addToHead);
									});
								dur.wait; 
								player.free; 
								buffer.close;	
								buffer.close( buffer.cueSoundFileMsg(soundfile, 0))
								});
						}).play;
						secTask.start;
					}, {
						task.stop; 
						secTask.stop;
						buffer.close;
						buffer.free; // you need to do all these to properly cleanup
						player.free; 
					});
				});
				});
			});	

		volSlider = OSCIISlider.new(win, Rect(10, 160, 140, 10), "- vol", 0, 1, params[1], 0.001, \amp)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg sl; 
						volume = sl.value; 
						if(player != nil, {player.set(\vol, sl.value)});
						params[1] = sl.value;
					});
			
		// updating the seconds text		
		secTask = Task({var sec, min, secstring, minstring;
			sec = 0;
			min = 0;
			inf.do({arg i; 
				sec = sec + 1;
				if(sec > 59, {min = min+1; sec = 0;});
				if(min < 10, {minstring = "0"++min.asString}, {minstring = min.asString});
				if(sec < 10, {secstring = "0"++sec.asString}, {secstring = sec.asString});
				{timeText.string_(minstring++":"++secstring)}.defer;
				1.wait;
			});

		});
		
		cmdPeriodFunc = { playButton.valueAction_(0)};
		CmdPeriod.add(cmdPeriodFunc);

		win.front;
		win.onClose_({
			var t;
			playButton.valueAction_(0);
			CmdPeriod.remove(cmdPeriodFunc);
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
		});
	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}
}