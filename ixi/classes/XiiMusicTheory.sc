
// fix the pitchcircle - needs to rotate with the fundamental

XiiMusicTheory {

	var <>xiigui;
	var <>win, params;
	
	*new { arg server, channels, setting = nil;
		^super.new.initXiiMusicTheory(server, channels, setting);
		}
		

	initXiiMusicTheory {
		var bounds, chord, chords, chordnames;
		var scale, scales, scalenames;
		var chordmenu, scalemenu, play;
		var fString, fundamental=60;
		var setting, point, k, scaleOrChord, scaleChordString;
		var playMode, fundNoteString;
		var noteArray, noteRecFlag;
		var playmodeSC = "chord";
		var pitchCircle;

		
		playMode = false;
		bounds = Rect(20, 5, 1000, 222);
		noteRecFlag = false;
		noteArray = [];
		
		point = if(setting.isNil, {Point(110, 250)}, {setting[1]});
		xiigui = nil; // not using window server class here
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});

		win = GUI.window.new("- basic music theory -", 
						Rect(point.x, point.y, bounds.width+20, bounds.height+10), resizable:false);
		
		k = MIDIKeyboard.new(win, Rect(10, 60, 790, 160), 4, 48);
		
		k.keyDownAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midinotename);
								if(playMode, {
									note.postln;
									Synth(\midikeyboardsine, [\freq, note.midicps]);
									if(noteRecFlag, {noteArray = noteArray.add(note)});
								}, {
									//pitchCircle.offset_((note%12).neg);
									k.showScale(chord, fundamental, Color.new255(103, 148, 103));									scaleChordString.string_((fundamental+chord).midinotename.asString);
									chord.postln;
								});
						});
		k.keyTrackAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midinotename);
								if(playMode, {
									note.postln;
									Synth(\midikeyboardsine, [\freq, note.midicps]);
									if(noteRecFlag, {noteArray = noteArray.add(note)});
								},{	
									k.showScale(chord, fundamental, Color.new255(103, 148, 103));
									scaleChordString.string_((fundamental+chord).midinotename.asString);
								});
						});
		k.keyUpAction_({arg note; fundamental = note; 
								fString.string_(note.asString++"  :  "++note.midinotename);
								if(playMode.not, {
									k.showScale(chord, fundamental, Color.new255(103, 148, 103));
									scaleChordString.string_((fundamental+chord).midinotename.asString);
								});
						});
		
		
		pitchCircle = XiiPitchCircle.new(12, size:200, win: win);
		
		
		fundNoteString = GUI.staticText.new(win, Rect(300, 10, 100, 20)).string_("Fundamental :")
						.font_(GUI.font.new("Helvetica", 9));
						
		fString = GUI.staticText.new(win, Rect(370, 10, 50, 20))
					.string_(fundamental.asString++"  :  "++fundamental.midinotename)
					.font_(GUI.font.new("Helvetica", 9));

		scaleOrChord = GUI.staticText.new(win, Rect(300, 30, 100, 20)).string_("Chord :")
						.font_(GUI.font.new("Helvetica", 9));
		scaleChordString = GUI.staticText.new(win, Rect(340, 30, 150, 20))
						.string_(fundamental.asString++"  :  "++fundamental.midinotename)
						.font_(GUI.font.new("Helvetica", 9));
		
		chords = XiiTheory.chords;
		scales = XiiTheory.scales;
		
		chordnames = [];
		chords.do({arg item; chordnames = chordnames.add(item[0])});
		chord = chords[0][1];
		
		scalenames = [];
		scales.do({arg item; scalenames = scalenames.add(item[0])});
		scale = scales[0][1];
		
			
		chordmenu = GUI.popUpMenu.new(win,Rect(500,10,140,16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(chordnames)
				.background_(Color.white)
				.action_({arg item;
					play.states_([["play chord", Color.black, Color.clear]]);
					chord = chords[item.value][1];
					scaleOrChord.string_("Chord :");
					scaleChordString.string_((fundamental+chord).midinotename.asString);
					k.showScale(chord, fundamental, Color.new255(103, 148, 103));
					playmodeSC = "chord";
					pitchCircle.drawSet(chord);
					chord.postln;
					win.refresh;
				})
				.keyDownAction_({arg view, key, mod, unicode; 
					if (unicode == 13, { play.valueAction_(1) });
					if (unicode == 16rF700, { view.valueAction_(view.value+1) });
					if (unicode == 16rF703, { view.valueAction_(view.value+1) });
					if (unicode == 16rF701, { view.valueAction_(view.value-1) });
					if (unicode == 16rF702, { view.valueAction_(view.value-1) });
				});

		
		scalemenu = GUI.popUpMenu.new(win,Rect(500,31,140,16))
				.font_(GUI.font.new("Helvetica", 9))
				.items_(scalenames)
				.background_(Color.white)
				.action_({arg item;
					play.states_([["play scale", Color.black, Color.clear]]);
					chord = scales[item.value][1];
					scaleOrChord.string_("Scale :");
					scaleChordString.string_((fundamental+chord).midinotename.asString);
					k.showScale(chord, fundamental, Color.new255(103, 148, 103));
					playmodeSC = "scale";
					pitchCircle.drawSet(chord);
					chord.postln;
					win.refresh;
				})
				.keyDownAction_({arg view, key, mod, unicode; 
					if (unicode == 13, { play.valueAction_(1) });
					if (unicode == 16rF700, { view.valueAction_(view.value+1) });
					if (unicode == 16rF703, { view.valueAction_(view.value+1) });
					if (unicode == 16rF701, { view.valueAction_(view.value-1) });
					if (unicode == 16rF702, { view.valueAction_(view.value-1) });
				});

		OSCIIRadioButton.new(win, Rect(680, 10, 12, 12), "play mode")
			.font_(GUI.font.new("Helvetica", 9))
			.value_(0)
			.action_({arg sl; 
				playMode = sl.value.booleanValue;
				if(playMode, {
					k.clear;
					fundNoteString.string_("Note :")
				}, {
					fundNoteString.string_("Fundamental :")
				});
			});

		play = GUI.button.new(win,Rect(680,31,60,16))
			.font_(GUI.font.new("Helvetica", 9))
			.states_([["play scale", Color.black, Color.clear]])
			.action_({
				var tempchord;
				chord.postln;
				Task({
					if(playmodeSC == "chord", {
						chord.do({arg note;
							note = note + fundamental;
							Synth(\midikeyboardsine, [\freq, note.midicps]);
							0.4.wait;
						});
						0.6.wait;
						chord.do({arg note;
							note = note + fundamental;
							Synth(\midikeyboardsine, [\freq, note.midicps, \amp, 0.1]);
						});
					}, {
						tempchord = chord ++ 12;
						tempchord.mirror.do({arg note;
							note = note + fundamental;
							Synth(\midikeyboardsine, [\freq, note.midicps]);
							0.3.wait;
						});
					})
				}).start;
			});
		
				// plot the frequency of strings played
		win.view.keyDownAction_({|me, char|
			if(char == $a, {
				noteRecFlag = true;
			})	
		});
		
		win.view.keyUpAction_({|me, char|
			if(char == $a, {
				" ************ your recorded midi note array is : ".postln;
				noteArray.postln;
				noteRecFlag = false;
				noteArray = [];
			})	
		});
		
		win.front;
		
	}

	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}
