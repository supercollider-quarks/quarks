

XiiQuanoon {

	var <>xiigui;
	var <>win, params;
		
	*new { arg server, channels, setting = nil;
		^super.new.initXiiQuanoon(server, channels, setting);
		}
		
	initXiiQuanoon {arg server, channels, setting;

	var strings, scale, scalenames, scaleObject;
	var thisX, lastX, thisY, lastY;
	var stringRecent, point, userView, tablet;
	var fundamental, octave, outbus, vol, fundamentalWin;
	var tmppoints;
	var playdstrings = {0} ! 8; // array counting how often strings are plucked
	var stringwidth = {1.3} ! 8; // widths of the strin
	var fundwin;
	var mouseMoveFunc;

	var name = "- quanoon -";

		xiigui = nil; // not using window server class here
		point = if(setting.isNil, {Point(500, 20)}, {setting[1]});
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});

	stringRecent = true;
	fundamental = 110;
	octave = 1;
	scalenames = [\ajam, \jiharkah, \shawqAfza, \sikah, \huzam, \iraq, \bastanikar, \mustar, \bayati, \karjighar,\husseini, \nahawand, \farahfaza, \murassah, \ushaqMashri, \rast, \suznak, \nairuz, \yakah, \mahur, \hijaz, \zanjaran, \zanjaran, \saba, \zamzam, \kurd, \kijazKarKurd, \nawaAthar, \nikriz, \atharKurd, \major, \ionian, \dorian, \phrygian, \lydian, \mixolydian, \aeolian, \minor, \locrian, \harmonicMinor, \harmonicMajor, \melodicMinor, \melodicMajor, \bartok, \todi, \purvi, \marva, \bhairav, \ahirbhairav, \superLocrian, \romanianMinor, \hungarianMinor, \neapolitanMinor, \enigmatic, \spanish];
	
	scaleObject = XiiScale.new;
	scale = scaleObject.scale_(scalenames[params[0]]).ratios.add(2);
	outbus = 0;
	vol = 1;
	
	strings = 8.collect({|i| Rect(((i+1)*30),10, 4, 680) });
	
	thisX=0;
	lastX=1;
	thisY=0;
	lastY=1;
	tmppoints = [];
	
	win = GUI.window.new(name, Rect(point.x, point.y, 290, 734), resizable:false).front;

/*
// not possible in SwingOSC
	5.do({|i| GUI.staticText.new(win, Rect(20, 21+(i*132), 250, 132))
				.background_(Gradient.new(XiiColors.listbackground, XiiColors.darkgreen.alpha_(0.6), \v, 19)) 
	}); // octave bands
*/
	5.do({|i| GUI.staticText.new(win, Rect(20, 21+(i*132), 250, 132))
				.background_( XiiColors.darkgreen.alpha_((0.8*(i/5))+0.1) ) 
	});

	userView = GUI.userView.new(win, Rect(10,10,280, 680))
		.canFocus_(false)
		.drawFunc_({
			if(GUI.id==\cocoa, {Pen.setShadow(1@1, 5, Color.black) });
			GUI.pen.translate(0.5,0.5);
			8.do({|i| GUI.pen.line(Point(30+(i*30), 10), Point(30+(i*30), 670)) }); // strings
			6.do({|i| GUI.pen.line(Point(10, 10+(i*132)), Point(260, 10+(i*132))) }); // octave bands
			GUI.pen.line(Point(10, 6), Point(260, 6));
			GUI.pen.line(Point(10, 674), Point(260, 674));
			GUI.pen.stroke;
			//.userView.drawFunc_({nil}) // don't draw the strings again
		});

	
	if(GUI.id == \cocoa, {
		tablet = TabletView.new(win, Rect(10,10,280, 680));
	},{
		tablet = UserView.new(win, Rect(10,10,280, 680));
	});
		

		
	tablet	
		.canFocus_(false)
		.background_(Color.clear)
		.mouseDownAction_({ arg  view, x, y, pressure;
			lastX = x; // jump between strings without playing all in between
			strings.do({|string, i|
				if(string.contains(Point(x, y)), {
					playdstrings[i] = playdstrings[i] + 1;
					Synth(\xiiQuanoon, [
						\freq, (fundamental*scale[i]*[1,2,4,8,16][(5-(y/132).floor(1))]), 
						\dur, ((y/135)-(y/135).floor(1)*6),
						\amp, vol, 
						\outbus, outbus]);
				});
			});
		});

		mouseMoveFunc = { arg view, x, y, pressure;
			thisX = x;
			thisY = y;
			//t.background = Color(x / 300,y / 300,pressure, 0.1);
			if((thisX-lastX).abs > 3, {
				Task({ // don't play all the strings at once
					strings.do({|str, i|
						if(	((str.left>lastX) && (str.left<thisX)) || 
							((str.left<lastX) && (str.left>thisX)), {
							playdstrings[i] = playdstrings[i] + 1;
							Synth(\xiiQuanoon, [
								\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]), 
								\dur, ((y/135)-(y/135).floor(1)*6),
								\amp, vol, 
								\outbus, outbus]);
							0.01.wait;
							lastX = x;
						});
					});
				}).play;
			});
			if((thisY-lastY).abs > 4, { // allow for sliding down string (repeated playing)
				strings.do({|str, i|
					if(	((str.left>(lastX-2)) && (str.left<(thisX+2))) || 
						((str.left<(lastX+2)) && (str.left>(thisX-2))), {
						playdstrings[i] = playdstrings[i] + 1;
						Synth(\xiiQuanoon, [
							\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]),
							\dur, ((y/135)-(y/135).floor(1)*6), 
							\amp, vol,
							\outbus, outbus]);
						lastY = y;
					});
				});
			});
		};
	
	if(GUI.id == \cocoa, {
		tablet.action_(mouseMoveFunc);
	},{
		tablet.mouseMoveAction_(mouseMoveFunc);
	});
		
		GUI.popUpMenu.new(win, Rect(20, 700, 80, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(scalenames)
			.value_(params[0])
			.background_(Color.new255(255, 255, 255))
			.action_({ arg popup; 
				scale = scaleObject.scale_(scalenames[popup.value]).ratios.add(2);
				params[0] = popup.value;
			});
	
		GUI.staticText.new(win, Rect(110, 700, 44, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("out:");
			
		GUI.popUpMenu.new(win, Rect(130, 700, 44, 16)) // outbusses
			.items_( XiiACDropDownChannels.getStereoChnList )
			.value_(params[1])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; params[1] = ch.value; outbus = ch.value * 2; });
	
		OSCIISlider(win, Rect(184, 700, 70, 8), "vol", 0, 1, 1, 0.01)
			.canFocus_(false)
			.value_(params[2])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg slider; 
				vol = slider.value;
				params[2] = vol;
			});

		GUI.button.new(win, Rect(260, 700, 14, 14))
			.states_([["k", Color.black, Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt;
			 	fundamentalWin.value;
			});
		
		
		// plot the frequency of strings played
		win.view.keyDownAction_({|me, char|
			if(char == $p, {
				playdstrings.ixiplot(discrete:true);
			})	
		});
		
		win.onClose_({
			var t;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
			try{ fundwin.close };
		});

		fundamentalWin = {
			var m, fund;
			fund = fundamental.cpsmidi.round(1);
			
			fundwin = GUI.window.new("k", Rect(win.bounds.left+win.bounds.width+20, win.bounds.top, 100, 80), resizable:false).front;
			m = MIDIKeyboard.new(fundwin, Rect(10, 10, 80, 60), 1, 36)
				.keyDown(fund)
				.keyDownAction_({arg note; 
					m.keyUp(fundamental.cpsmidi.round(1)); 
					fundamental = note.midicps;
				})
				.keyUpAction_({arg note;
					m.keyDown(note);
				});
			
		}

	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}

/*
XiiQuanoon {

	var <>xiigui;
	var <>win, params;
		
	*new { arg server, channels, setting = nil;
		^super.new.initXiiQuanoon(server, channels, setting);
		}
		
	initXiiQuanoon {arg server, channels, setting;

	var strings, scale, scalenames, scaleObject;
	var thisX, lastX, thisY, lastY;
	var stringRecent, point, userView;
	var fundamental, octave, outbus, vol, fundamentalWin;
	var tmppoints;
	var playdstrings = {0} ! 8; // array counting how often strings are plucked
	var stringwidth = {1.3} ! 8; // widths of the strings
	var fundwin;
	var name = "- quanoon -";

		xiigui = nil; // not using window server class here
		point = if(setting.isNil, {Point(500, 20)}, {setting[1]});
		params = if(setting.isNil, {[0,0,1]}, {setting[2]});

	stringRecent = true;
	fundamental = 110;
	octave = 1;
	scalenames = [\ajam, \jiharkah, \shawqAfza, \sikah, \huzam, \iraq, \bastanikar, \mustar, \bayati, \karjighar,\husseini, \nahawand, \farahfaza, \murassah, \ushaqMashri, \rast, \suznak, \nairuz, \yakah, \mahur, \hijaz, \zanjaran, \zanjaran, \saba, \zamzam, \kurd, \kijazKarKurd, \nawaAthar, \nikriz, \atharKurd, \major, \ionian, \dorian, \phrygian, \lydian, \mixolydian, \aeolian, \minor, \locrian, \harmonicMinor, \harmonicMajor, \melodicMinor, \melodicMajor, \bartok, \todi, \purvi, \marva, \bhairav, \ahirbhairav, \superLocrian, \romanianMinor, \hungarianMinor, \neapolitanMinor, \enigmatic, \spanish];
	
	scaleObject = XiiScale.new;
	scale = scaleObject.scale_(scalenames[params[0]]).ratios.add(2);
	outbus = params[1] * 2; // due to stereo busses
	vol = params[2];
	
	strings = 8.collect({|i| Rect(30 +(i*30),10, 4, 680) });
	
	thisX=0;
	lastX=1;
	thisY=0;
	lastY=1;
	tmppoints = [];
	
	win = GUI.window.new(name, Rect(point.x, point.y, 290, 734), resizable:false).front;

	5.do({|i| GUI.staticText.new(win, Rect(20, 21+(i*132), 250, 132))
				.background_(Gradient.new(XiiColors.listbackground, XiiColors.darkgreen.alpha_(0.6), \v, 19)) 
	}); // octave bands
	
	userView = SCUserView.new(win, Rect(10,10,280, 680))
		.relativeOrigin_(true)
		.clearOnRefresh_(false)
		.canFocus_(false)
		.drawFunc_({
			Pen.setShadow(1@1, 5, Color.black); // XXX

			GUI.pen.translate(0.5,0.5);
			8.do({|i| GUI.pen.line(Point(30+(i*30), 10), Point(30+(i*30), 670)) }); // strings
			6.do({|i| GUI.pen.line(Point(10, 10+(i*132)), Point(260, 10+(i*132))) }); // octave bands
			GUI.pen.line(Point(10, 6), Point(260, 6));
			GUI.pen.line(Point(10, 674), Point(260, 674));
			GUI.pen.stroke;
			userView.drawFunc_({nil}) // don't draw the strings again
		});
	
		
	GUI.tabletView.new(win, Rect(10,10,280, 680))
		.canFocus_(false)
		.background_(Color.clear)
		.mouseDownAction_({ arg  view, x, y, pressure;
			lastX = x; // jump between strings without playing all in between
			strings.do({|string, i|
				if(string.contains(Point(x, y)), {
					playdstrings[i] = playdstrings[i] + 1;
					Synth(\xiiQuanoon, [
						\freq, (fundamental*scale[i]*[1,2,4,8,16][(5-(y/132).floor(1))]), 
						\dur, ((y/135)-(y/135).floor(1)*6),
						\amp, pressure*vol, 
						\outbus, outbus]);
				});
			});
		})
		.action_({ arg view, x, y, pressure;
			thisX = x;
			thisY = y;
			if(thisY< 675, { // if cursor is above the control bar at the bottom
				//t.background = Color(x / 300,y / 300,pressure, 0.1);
				if((thisX-lastX).abs > 3, {
					Task({ // don't play all the strings at once
						strings.do({|str, i|
							if(	((str.left>lastX) && (str.left<thisX)) || 
								((str.left<lastX) && (str.left>thisX)), {
								playdstrings[i] = playdstrings[i] + 1;
								Synth(\xiiQuanoon, [
									\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]), 
									\dur, ((y/135)-(y/135).floor(1)*6),
									\amp, pressure*vol, 
									\outbus, outbus]);
								0.01.wait;
								lastX = x;
							});
						});
					}).play;
				});
				if((thisY-lastY).abs > 4, { // allow for sliding down string (repeated playing)
					strings.do({|str, i|
						if(	((str.left>(lastX-2)) && (str.left<(thisX+2))) || 
							((str.left<(lastX+2)) && (str.left>(thisX-2))), {
							playdstrings[i] = playdstrings[i] + 1;
							Synth(\xiiQuanoon, [
								\freq, (fundamental*scale[i]*[1,2,4,8,16][(4-(y/135).floor(1))]),
								\dur, ((y/135)-(y/135).floor(1)*6), 
								\amp, pressure*vol,
								\outbus, outbus]);
							lastY = y;
						});
					});
				});
			});
		});
		
		GUI.popUpMenu.new(win, Rect(20, 700, 80, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.items_(scalenames)
			.value_(params[0])
			.background_(Color.new255(255, 255, 255))
			.action_({ arg popup; 
				scale = scaleObject.scale_(scalenames[popup.value]).ratios.add(2);
				params[0] = popup.value;
			});
	
		GUI.staticText.new(win, Rect(110, 700, 44, 16))
			.font_(GUI.font.new("Helvetica", 9))
			.string_("out:");
			
		GUI.popUpMenu.new(win, Rect(130, 700, 44, 16)) // outbusses
			.items_( XiiACDropDownChannels.getStereoChnList )
			.value_(params[1])
			.font_(GUI.font.new("Helvetica", 9))
			.background_(Color.white)
			.canFocus_(false)
			.action_({ arg ch; params[1] = ch.value; outbus = ch.value * 2; });
	
		OSCIISlider(win, Rect(184, 700, 70, 8), "vol", 0, 1, 1, 0.01)
			.canFocus_(false)
			.value_(params[2])
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg slider; 
				vol = slider.value;
				params[2] = vol;
			});

		GUI.button.new(win, Rect(260, 700, 14, 14))
			.states_([["k", Color.black, Color.clear]])
			.canFocus_(false)
			.font_(GUI.font.new("Helvetica", 9))
			.action_({arg butt;
			 	fundamentalWin.value;
			});
		
		// plot the frequency of strings played
		win.view.keyDownAction_({|me, char|
			if(char == $p, {
				playdstrings.ixiplot(discrete:true);
			})	
		});
		
		win.onClose_({
			var t;
			XQ.globalWidgetList.do({arg widget, i; if(widget === this, { t = i})});
			try{XQ.globalWidgetList.removeAt(t)};
			// write window position to archive.sctxar
			point = Point(win.bounds.left, win.bounds.top);
			XiiWindowLocation.storeLoc(name, point);
			try{ fundwin.close };
		});
		
		fundamentalWin = {
			var m, fund;
			fund = fundamental.cpsmidi.round(1);
			
			fundwin = GUI.window.new("k", Rect(win.bounds.left+win.bounds.width+20, win.bounds.top, 100, 80), resizable:false).front;
			m = MIDIKeyboard.new(fundwin, Rect(10, 10, 80, 60), 1, 36)
				.keyDown(fund)
				.keyDownAction_({arg note; 
					m.keyUp(fundamental.cpsmidi.round(1)); 
					fundamental = note.midicps;
				})
				.keyUpAction_({arg note;
					m.keyDown(note);
				});
			
		}

	}
	
	getState { // for save settings
		var point;		
		point = Point(win.bounds.left, win.bounds.top);
		^[2, point, params];
	}

}
*/
