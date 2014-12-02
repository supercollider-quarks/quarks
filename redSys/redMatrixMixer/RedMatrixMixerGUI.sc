//redFrik

//--related:
//RedEffectsRackGUI

//--todo:
//time should also be a cv or?

//--ideas:
//multisliderview inte continuerligt
//allt i db istället?
//knobs+numberbox eller annat sätt att visa exakta värden?
//preset med mus xy gui där cirklar visar hur mycket varje kanal.  slider att styra omfång, max.  jmf processing exempel

RedMatrixMixerGUI {
	var <win, <redMatrixMixer, <time, lastTime= 0, mainGUI, mirrorGUI,
		position;
	*new {|redMatrixMixer, position|
		^super.new.initRedMatrixMixerGUI(redMatrixMixer, position);
	}
	initRedMatrixMixerGUI {|argRedMatrixMixer, argPosition|
		Routine({
			var tab, winWidth, winHeight, tabWidth, tabHeight, topHeight, tabOffset,
				savePosLeft, savePosTop,
				macroMenu, macroFunctions,
				margin= 4@4, gap= 4@4,
				inNumbers, outNumbers, lagBox,
				nIn= argRedMatrixMixer.nIn,
				nOut= argRedMatrixMixer.nOut;
			while({argRedMatrixMixer.isReady.not}, {0.02.wait});
			
			redMatrixMixer= argRedMatrixMixer;
			position= argPosition ?? {700@400};
			
			tabWidth= RedGUICVMultiSliderView.defaultWidth+gap.x*nOut;
			tabHeight= RedGUICVMultiSliderView.defaultHeight*nIn+14;
			topHeight= 14+gap.y;
			tabOffset= "o99".bounds(RedFont.new).width;
			winWidth= (tabWidth+(margin.x*2)+tabOffset).max(280);
			winHeight= tabHeight+topHeight+(margin.y*2)+18;
			win= Window(
				"redMatrixMixer nIn"++nIn+"nOut"++nOut,
				Rect(position.x, position.y, winWidth, winHeight),
				false
			);
			if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
				win.alpha= GUI.skins.redFrik.unfocus;
			});
			win.front;
			win.view.background= GUI.skins.redFrik.background;
			win.view.decorator= FlowLayout(win.view.bounds, margin, gap);
			
			redMatrixMixer.cvs.in.connect(RedNumberBox(win));
			redMatrixMixer.cvs.in.action= {|cv| inNumbers.do{|x, i| x.string= "i"++(i+cv.value)}};
			win.view.decorator.shift(-2, 0);
			RedStaticText(win, "in");
			win.view.decorator.shift(2, 0);
			
			redMatrixMixer.cvs.out.connect(RedNumberBox(win));
			redMatrixMixer.cvs.out.action= {|cv| outNumbers.do{|x, i| x.string= "o"++(i+cv.value)}};
			win.view.decorator.shift(-2, 0);
			RedStaticText(win, "out");
			win.view.decorator.shift(2, 0);
			
			lagBox= RedNumberBox(win);
			redMatrixMixer.cvs.lag.connect(lagBox);
			win.view.decorator.shift(-2, 0);
			RedStaticText(win, "lag");
			win.view.decorator.shift(2, 0);
			
			macroFunctions= {|index|
				var gui;
				if(tab.activeTab==0, {
					gui= mainGUI;
				}, {
					gui= mirrorGUI;
				});
				[
					{},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(i, 1)});
						x.value= arr;
					}},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(nIn-1-i, 1)});
						x.value= arr;
					}},
					{gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(i, 1).put(nIn-i-1, 1)});
						x.value= arr;
					}},
					{gui.do{|x| x.value= x.value.scramble}},
					{gui.do{|x| x.value= 0.dup(nIn)}},
					{var tmp= (0..nIn-1).scramble; gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(tmp[i], 1)});
						x.value= arr;
					}},
					{var tmp= (0..nIn-1).scramble; gui.do{|x, i|
						var arr= 0.dup(nIn);
						if(i<nIn, {arr= arr.put(tmp[i], 1.0.rand)});
						x.value= arr;
					}},
					{gui.do{|x, i| x.value= {0.2.coin.binaryValue}.dup(nIn)}},
					{gui.do{|x, i| x.value= {if(0.2.coin, {1.0.rand}, {0})}.dup(nIn)}},
					{gui.do{|x|
						x.value= x.value.rotate(-1);
					}},
					{gui.do{|x|
						x.value= x.value.rotate(1);
					}},
					{var tmp= gui.collect{|x| x.value}; gui.do{|x, i| x.value= tmp.wrapAt(i+1)}},
					{var tmp= gui.collect{|x| x.value}; gui.do{|x, i| x.value= tmp.wrapAt(i-1)}},
					{Dialog.savePanel{|x| redMatrixMixer.cvs.writeArchive(x)}},
					{Dialog.getPaths{|x| x.do{|y| Object.readArchive(y).keysValuesDo{|k, v| redMatrixMixer.cvs[k].value= v.value}}}}
				][index].value;
			};
			macroMenu= RedPopUpMenu(win)
				.items_([
					"_macros_",
					"default",
					"backwards",
					"cross",
					"scramble",
					"clear",
					"urn binary",
					"urn float",
					"random binary",
					"random float",
					"shift up",
					"shift down",
					"shift left",
					"shift right",
					"save preset",
					"load preset"
				])
				.action_{|view| macroFunctions.value(view.value)};
			RedButton(win, 14@14, "<").action= {macroFunctions.value(macroMenu.value)};
			win.view.decorator.nextLine;
			
			win.view.decorator.shift(100+tabOffset, 0);
			time= RedSlider(win)
				.action_{|view|
					if(tab.activeTab==0, {
						mainGUI.do{|x, i|
							if(lastTime==0 and:{view.value>0}, {x.save});
							x.interp(view.value, mirrorGUI[i].value);
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
			
			win.view.decorator.shift(-100-tabOffset-time.bounds.width-4, 2);
			savePosLeft= win.view.decorator.left+tabOffset;
			savePosTop= win.view.decorator.top;
			inNumbers= {|i|
				var v= RedStaticText(win, "i"++i);
				v.bounds= Rect(
					v.bounds.left,
					savePosTop+14+(i*RedGUICVMultiSliderView.defaultHeight)+(RedGUICVMultiSliderView.defaultHeight*0.2),
					tabOffset,
					v.bounds.height
				);
				win.view.decorator.nextLine;
				v;
			}.dup(nIn);
			
			win.view.decorator.left= savePosLeft;
			win.view.decorator.top= savePosTop;
			
			tab= TabbedView(
				win,
				tabWidth@tabHeight,
				#[\now, \later],
				[Color.grey(0.2, 0.2), GUI.skins.redFrik.background]
			);
			tab.font= RedFont.new;
			tab.stringFocusedColor= GUI.skins.redFrik.foreground;
			tab.stringColor= GUI.skins.redFrik.foreground;
			tab.backgrounds= [GUI.skins.redFrik.foreground, GUI.skins.redFrik.background];
			tab.unfocusedColors= [Color.grey(0.2, 0.2), GUI.skins.redFrik.background];
			tab.focusActions= [
				{mainGUI.do{|x| x.save}; time.valueAction= 0},
				{mirrorGUI.do{|x| x.sync}; time.value= 1}
			];
			tab.views[0].flow{|v|
				mainGUI= {|i|
					RedGUICVMultiSliderView(v, nil, redMatrixMixer.cvs[("o"++i).asSymbol]);
				}.dup(nOut);
			};
			tab.views[1].flow{|v|
				mirrorGUI= {|i|
					RedGUICVMultiSliderViewMirror(v, nil, redMatrixMixer.cvs[("o"++i).asSymbol]);
				}.dup(nOut);
			};
			
			win.view.decorator.nextLine;
			outNumbers= {|i|
				var v= RedStaticText(win, "o"++i);
				v.bounds= Rect(
					i*(RedGUICVMultiSliderView.defaultWidth+gap.x)+(RedGUICVMultiSliderView.defaultWidth*0.2)+savePosLeft,
					v.bounds.top,
					tabOffset,
					v.bounds.height
				);
			}.dup(nOut);
			
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		}).play(AppClock);
	}
	close {
		if(win.isClosed.not, {win.close});
	}
}
