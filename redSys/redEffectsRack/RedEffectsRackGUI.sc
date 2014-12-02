//redFrik

//time should also be a cv or?
//lag box?
//window height fix

//--related:
//RedEffectsRack RedEffectModule RedMixerGUI

RedEffectsRackGUI {
	classvar <>numEffectsBeforeScroll= 9;
	var <win, <redEffectsRack, <time, lastTime= 0, mainGUI, mirrorGUI,
		width1= 264, height1;
	*new {|redEffectsRack, position|
		^super.new.initRedEffectsRackGUI(redEffectsRack, position);
	}
	initRedEffectsRackGUI {|argRedEffectsRack, position|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight,
				macroMenu, macroFunctions,
				margin= 4@4, gap= 4@4;
			while({argRedEffectsRack.isReady.not}, {0.02.wait});
			
			redEffectsRack= argRedEffectsRack;
			position= position ?? {300@360};
			
			redEffectsRack.efxs.do{|x|
				var num= x.def.metadata[\order].count{|x| x.key!=\out}-1;
				var temp= gap.x+RedGUICVSlider.defaultWidth+gap.x+(RedGUICVKnobNumberName.defaultWidth+gap.x*num)+gap.x;
				if(temp>width1, {width1= temp});
			};
			height1= gap.y+RedGUICVSlider.defaultHeight+gap.y;
			
			tabWidth= width1;
			topHeight= 14;
			tabHeight= height1+gap.y*redEffectsRack.efxs.size.min(numEffectsBeforeScroll)+topHeight;
			winWidth= tabWidth+(margin.x*2);
			winHeight= tabHeight+topHeight+gap.y+(margin.y*2);
			win= Window(
				redEffectsRack.class.name.asString.put(0, $r),
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
				win.alpha= GUI.skins.redFrik.unfocus;
			});
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);
			
			redEffectsRack.out.connect(RedNumberBox(win));
			RedStaticText(win, "out");
			win.view.decorator.shift(10, 0);
			RedButton(win, nil, "monitor", "monitor").action_{|view| "todo!".postln};
			RedNumberBox(win).value_(7).action_{|view| "todo!".postln};
			
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
					{gui.do{|x| x.views.do{|y| y.value= 1.0.rand}}},
					{gui.do{|x| x.views.do{|y| if(0.3.coin, {y.value= 1.0.rand})}}},
					{gui.do{|x| x.views.do{|y| y.value= y.value+0.08.rand2}}},
					{gui.do{|x| x.views.do{|y| if(0.3.coin, {y.value= y.value+0.08.rand2})}}},
					{gui.collect{|x| x.views.choose}.choose.value= #[0, 0.5, 1].choose},
					{gui.do{|x| x.views.do{|x| x.value= 0}}},
					{Dialog.savePanel{|x| redEffectsRack.cvs.writeArchive(x)}},
					{Dialog.getPaths{|x| x.do{|y| Object.readArchive(y).keysValuesDo{|k, v| redEffectsRack.cvs[k].value= v.value}}}}
				][index].value;
			};
			macroMenu= RedPopUpMenu(win)
				.items_([
					"_macros_",
					"defaults",
					"randomize all",
					"randomize some",
					"vary all",
					"vary some",
					"surprise",
					"clear",
					"save preset",
					"load preset",
					"#1", "#2", "#3", "#4", "#5"
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
						tab.backgrounds_([
							GUI.skins.redFrik.foreground.copy.alpha_(1-view.value),
							GUI.skins.redFrik.background
						]);
					}, {
						view.value= 1;
					});
					lastTime= view.value;
				};
			win.view.decorator.shift(-100-time.bounds.width-gap.x, 0);
			
			tab= TabbedView(
				win,
				tabWidth@tabHeight,
				#[\now, \later],
				[Color.grey(0.2, 0.2), GUI.skins.redFrik.background],
				scroll: true
			);
			tab.views.do{|x| x.hasHorizontalScroller= false};
			if(redEffectsRack.efxs.size<=numEffectsBeforeScroll, {
				tab.views.do{|x| x.hasVerticalScroller= false};
			});
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
				mainGUI= redEffectsRack.efxs.collect{|x|
					var gui= RedEffectModuleGUI(x, v);
					v.view.decorator.nextLine;
					gui;
				};
			};
			tab.views[1].flow{|v|
				mirrorGUI= redEffectsRack.efxs.collect{|x|
					var gui= RedEffectModuleGUI.newMirror(x, v);
					v.view.decorator.nextLine;
					gui;
				};
			};
			
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
	}
}
