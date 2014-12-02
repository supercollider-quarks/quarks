//redFrik

//--related:
//RedEffectsRackGUI

//--todo:
//differentiate mixers and channels
//fix the rest of the presets
//add eq buttons again to views?
//peakRate box?
//textviews at the bottom
//multiple channels (at least mono/stero)
//disable views in later tab1 that i can't change (eg peak meters)
//preview/monitor synth and connect to numbox
//why not connected: lag and the rest of the redMixer.cvs does not save in preset load/save

RedMixerGUI {
	classvar <>numMixerChannelsBeforeScroll= 11;		//todo!!!
	var <win, <redMixer, <time, lastTime= 0, mainGUI, mirrorGUI, mainGUImixer, mirrorGUImixer;
	*new {|redMixer, position|
		^super.new.initRedMixerGUI(redMixer, position);
	}
	initRedMixerGUI {|argRedMixer, position|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight,
				macroMenu, macroFunctions,
				margin= 4@4, gap= 4@4;
			while({argRedMixer.isReady.not}, {0.02.wait});
			
			redMixer= argRedMixer;
			position= position ?? {300@200};
			
			tabWidth= RedMixerChannelGUI.width+gap.x*(redMixer.mixers.size+redMixer.channels.size);
			topHeight= 14;
			tabHeight= RedMixerChannelGUI.height+2+topHeight;
			
			winWidth= tabWidth+(margin.x*2);	//perhaps clip here if only 2 channels or so
			winHeight= tabHeight+topHeight+gap.y+(margin.y*2);
			win= Window(
				redMixer.class.name.asString.put(0, $r),
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
				win.alpha= GUI.skins.redFrik.unfocus;
			});
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);
			
			//lagBox= RedNumberBox(win);
			//redMixer.cvs.lag.connect(lagBox);
			//win.view.decorator.shift(-2, 0);
			//RedStaticText(win, "lag");
			//win.view.decorator.shift(2, 0);
			//peakRateBox= RedNumberBox(win);
			//redMixer.cvs.peakRate.connect(peakRateBox);
			//win.view.decorator.shift(-2, 0);
			//RedStaticText(win, "peakRate");
			//win.view.decorator.shift(2, 0);
			RedButton(win, nil, "monitor", "monitor").action_{|view| "todo".postln};
			RedNumberBox(win).value_(7).action_{|view| "todo".postln};
			
			macroFunctions= {|index|
				var gui;
				if(tab.activeTab==0, {
					gui= mainGUI;
				}, {
					gui= mirrorGUI;
				});
				[
					{},
					{gui.do{|x| x.views.do{|y| y.value= y.cv.spec.unmap(y.cv.spec.default)}}},
					{gui.do{|x| x.views.do{|y| y.value =1.0.rand}}},
					{gui.do{|x| x.views.do{|y| if(0.3.coin, {y.value= 1.0.rand})}}},
					{gui.do{|x| x.views.do{|y| y.value= y.value+0.08.rand2}}},
					{gui.do{|x| x.views.do{|y| if(0.3.coin, {y.value= y.value+0.08.rand2})}}}
					//{gui.do{|x| x.valueEq= {|y, cv| cv.spec.unmap(cv.spec.default)}}},
					//{gui.do{|x| x.valueEq= {1.0.rand}}},
					//{gui.do{|x| x.valueEq= {|y| if(0.3.coin, {1.0.rand}, {y.value})}}},
					//{gui.do{|x| x.valueEq= {|y| y.value+0.08.rand2}}},
					//{gui.do{|x| x.valueEq= {|y| if(0.3.coin, {y.value+0.08.rand2}, {y.value})}}},
					//{Dialog.savePanel{|x|
					//	var arr= ([redMixer]++redMixer.channels++[redMixer.mixer]).collect{|x| x.cvs};
					//	arr.writeArchive(x);
					//}},
					//{Dialog.getPaths{|x| x.do{|y|
					//	var arr= ([redMixer]++redMixer.channels++[redMixer.mixer]).collect{|x| x.cvs};
					//	Object.readArchive(y).do{|z, i|
					//		z.keysValuesDo{|k, v| arr[i][k].value= v.value};
					//	};
					//}}}
				][index].value;
			};
			macroMenu= RedPopUpMenu(win)
				.items_([
					"_macros_",
					"defaults",
					"randomize all",
					"randomize some",
					"vary all",
					"vary some"/*,
					"eq: defaults",
					"eq: randomize all",
					"eq: randomize some",
					"eq: vary all",
					"eq: vary some",
					"save preset",
					"load preset"*/
				])
				.action_{|view| macroFunctions.value(view.value)};
			RedButton(win, 14@14, "<").action= {macroFunctions.value(macroMenu.value)};
			win.view.decorator.nextLine;
			
			win.view.decorator.shift(100, 0);
			time= RedSlider(win)
				.action_{|view|
					if(tab.activeTab==0, {
						mainGUI.do{|x, i|
							x.views.do{|y, j|
								if(lastTime==0 and:{view.value>0}, {y.save});
								y.interp(view.value, mirrorGUI[i].views[j].value);
							};
						};
						tab.backgrounds= [
							GUI.skins.redFrik.foreground.copy.alpha_(1-view.value),
							GUI.skins.redFrik.background
						];
					}, {
						view.value= 1;
					});
					lastTime= view.value;
				};
			win.view.decorator.shift(-100, -16);
			
			tab= TabbedView(
				win,
				tabWidth@tabHeight,
				#[\now, \later],
				[Color.grey(0.2, 0.2), GUI.skins.redFrik.background],
				scroll: true
			);
			tab.views.do{|x| x.hasHorizontalScroller= false};
			tab.font= RedFont.new;
			tab.stringFocusedColor= GUI.skins.redFrik.foreground;
			tab.stringColor= GUI.skins.redFrik.foreground;
			tab.backgrounds= [GUI.skins.redFrik.foreground, GUI.skins.redFrik.background];
			tab.unfocusedColors= [Color.grey(0.2, 0.2), GUI.skins.redFrik.background];
			tab.focusActions= [
				{mainGUI.do{|x| x.views.do{|y| y.save}}; time.valueAction= 0},
				{mirrorGUI.do{|x| x.views.do{|y| y.sync}}; time.value= 1}
			];
			tab.views[0].flow{|v|
				mainGUI= [];
				mainGUI= mainGUI++redMixer.mixers.collect{|x, i|
					RedMixerChannelGUI(x, v);
				};
				mainGUI= mainGUI++redMixer.channels.collect{|x, i|
					RedMixerChannelGUI(x, v);
				};
			};
			tab.views[1].flow{|v|
				mirrorGUI= [];
				mirrorGUI= mirrorGUI++redMixer.mixers.collect{|x, i|
					RedMixerChannelGUI.newMirror(x, v);
				};
				mirrorGUI= mirrorGUI++redMixer.channels.collect{|x, i|
					RedMixerChannelGUI.newMirror(x, v);
				};
			};
			
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
	}
}
