//redFrik

//--related:
//RedMixerChannel RedEffectModuleGUI

RedMixerChannelGUI {
	classvar <>width= 110, <>height= 280;
	var <redMixerChannel, <parent, position,
		<views, <mirror, win,
		insert0Efx, insert1Efx, insert0But, insert1But, insert0Win, insert1Win;
	*new {|redMixerChannel, parent, position, name|
		^super.newCopyArgs(redMixerChannel, parent, position).initRedMixerChannelGUI(false, name);
	}
	*newMirror {|redMixerChannel, parent, position, name|
		^super.newCopyArgs(redMixerChannel, parent, position).initRedMixerChannelGUI(true, name);
	}
	*initClass {
		Class.initClassTree(CV);
		StartUp.add({
			if('SCButton'.asClass.notNil, {
				CV.viewDictionary.put(SCButton, CVSyncValue);
			});
			if('JSCButton'.asClass.notNil, {
				CV.viewDictionary.put(JSCButton, CVSyncValue);
			});
			if('QButton'.asClass.notNil, {
				CV.viewDictionary.put(QButton, CVSyncValue);
			});
			if('SCLevelIndicator'.asClass.notNil, {
				CV.viewDictionary.put(SCLevelIndicator, CVSyncValue);
			});
			//if('JLevelIndicator'.asClass.notNil, {	//for swing todo!!!
			//	CV.viewDictionary.put(JLevelIndicator, CVSyncValue);
			//});
			if('QLevelIndicator'.asClass.notNil, {
				CV.viewDictionary.put(QLevelIndicator, CVSyncValue);
			});
		});
	}
	initRedMixerChannelGUI {|argMirror, name|
		var cmp, classes, cmpTmp, tmp;
		views= [];
		mirror= argMirror;
		if(mirror, {
			classes= (
				\slider: RedGUICVSliderMirror,
				\knob: RedGUICVKnobMirror,
				\button: RedGUICVButtonMirror,
				\sliderNumber: RedGUICVSliderNumberMirror,
				\sliderNumberName: RedGUICVSliderNumberNameMirror
			);
		}, {
			classes= (
				\slider: RedGUICVSlider,
				\knob: RedGUICVKnob,
				\button: RedGUICVButton,
				\sliderNumber: RedGUICVSliderNumber,
				\sliderNumberName: RedGUICVSliderNumberName
			);
		});
		cmp= this.prContainer;
		
		//--effect inserts
		tmp= (cmp.bounds.width*0.7)@14;
		RedPopUpMenu(cmp, tmp)
			.items_(["_inserts_"]++RedEffectModule.subclasses.collect{|x| x.name})
			.action_{|view|
				if(insert0Efx.notNil, {
					if(insert0Win.notNil, {
						insert0Win.close;
						insert0Win= nil;
						insert0But.value= 0;
					});
					redMixerChannel.remove(insert0Efx);
					insert0Efx= nil;
				});
				if(view.value>0, {
					Routine({
						insert0Efx= RedEffectModule.subclasses[view.value-1].new;
						redMixerChannel.insert(insert0Efx, \addToHead);
					}).play(AppClock);
				});
			};
		tmp= cmp.decorator.indentedRemaining.width@14;
		insert0But= RedButton(cmp, tmp, "o", "o")
			.action_{|view|
				if(view.value==1, {
					if(insert0Efx.notNil, {
						insert0Win= redMixerChannel.inserts.detect{|x| x==insert0Efx}
							.gui(nil, parent.bounds.right@(parent.bounds.bottom-80));
					});
				}, {
					if(insert0Win.notNil, {
						insert0Win.close;
						insert0Win= nil;
					});
				});
			};
		cmp.decorator.nextLine;
		tmp= (cmp.bounds.width*0.7)@14;
		RedPopUpMenu(cmp, tmp)
			.items_(["_inserts_"]++RedEffectModule.subclasses.collect{|x| x.name})
			.action_{|view|
				if(insert1Efx.notNil, {
					if(insert1Win.notNil, {
						insert1Win.close;
						insert1Win= nil;
						insert1But.value= 0;
					});
					redMixerChannel.remove(insert1Efx);
					insert1Efx= nil;
				});
				if(view.value>0, {
					Routine({
						insert1Efx= RedEffectModule.subclasses[view.value-1].new;
						redMixerChannel.insert(insert1Efx, \addToTail);
					}).play(AppClock);
				});
			};
		tmp= cmp.decorator.indentedRemaining.width@14;
		insert1But= RedButton(cmp, tmp, "o", "o")
			.action_{|view|
				if(view.value==1, {
					if(insert1Efx.notNil, {
						insert1Win= redMixerChannel.inserts.detect{|x| x==insert1Efx}
							.gui(nil, parent.bounds.right@(parent.bounds.bottom-180));
					});
				}, {
					if(insert1Win.notNil, {
						insert1Win.close;
						insert1Win= nil;
					});
				});
			};
		cmp.decorator.nextLine;
		
		//--equaliser
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.hiFreq);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.hiBand);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.hiGain);
		tmp= cmp.decorator.indentedRemaining.width@14;
		/*views= views++*/classes[\button].new(cmp, tmp, redMixerChannel.cvs.eqHi, "hi", "hi");
		cmp.decorator.nextLine;
		
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.miFreq);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.miBand);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.miGain);
		tmp= cmp.decorator.indentedRemaining.width@14;
		/*views= views++*/classes[\button].new(cmp, tmp, redMixerChannel.cvs.eqMi, "mi", "mi");
		cmp.decorator.nextLine;
		
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.loFreq);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.loBand);
		views= views++classes[\knob].new(cmp, 20@20, redMixerChannel.cvs.loGain);
		tmp= cmp.decorator.indentedRemaining.width@14;
		/*views= views++*/classes[\button].new(cmp, tmp, redMixerChannel.cvs.eqLo, "lo", "lo");
		cmp.decorator.nextLine;
		
		//--balance and mute
		views= views++classes[\slider].new(cmp, (cmp.bounds.width*0.7)@14, redMixerChannel.cvs.bal);
		views.last.slider.mouseUpAction_{|view, x, y, mod|
			if(mod&262144==262144, {			//ctrl to reset balance
				{redMixerChannel.cvs.bal.value= 0}.defer(0.1);
			});
		};
		tmp= cmp.decorator.indentedRemaining.width@14;
		RedButton(cmp, tmp, "m", "m")
			.action_{|view| redMixerChannel.mute(view.value.booleanValue)};
		cmp.decorator.nextLine;
		
		//--volume
		tmp= cmp.decorator.indentedRemaining;
		views= views++if(name.isNil, {
			classes[\sliderNumber];
		}, {
			classes[\sliderNumberName];
		}).new(cmp, (tmp.width*0.5)@tmp.height, redMixerChannel.cvs.vol, name);
		views.last.slider.mouseUpAction_{|view, x, y, mod|
			if(mod&262144==262144, {			//ctrl to reset volume
				{redMixerChannel.cvs.vol.value= 0}.defer(0.1);
			});
		};
		tmp= 12@cmp.decorator.indentedRemaining.height;
		cmpTmp= CompositeView(cmp, tmp);
		tmp= cmpTmp.bounds.height*(1-redMixerChannel.cvs.vol.spec.unmap(0))-6+14;
		RedStaticText(cmpTmp,"-u", Rect(0, tmp, 12, 12));
		
		//--meters
		cmpTmp= CompositeView(cmp, cmp.decorator.indentedRemaining);
		tmp= cmpTmp.bounds.width*0.5;
		redMixerChannel.cvs.peaked0.connect(RedButton(cmpTmp, Rect(0, 0, tmp, 14), "", "").canFocus_(false));
		redMixerChannel.cvs.peaked1.connect(RedButton(cmpTmp, Rect(tmp, 0, tmp, 14), "", "").canFocus_(false));
		if(name.isNil, {
			redMixerChannel.cvs.peak0.connect(RedLevelIndicator(cmpTmp, Rect(0, tmp, tmp, cmpTmp.bounds.height-tmp)).canFocus_(false));
			redMixerChannel.cvs.peak1.connect(RedLevelIndicator(cmpTmp, Rect(tmp, tmp, tmp, cmpTmp.bounds.height-tmp)).canFocus_(false));
		}, {
			redMixerChannel.cvs.peak0.connect(RedLevelIndicator(cmpTmp, Rect(0, tmp, tmp, cmpTmp.bounds.height-tmp-14)).canFocus_(false));
			redMixerChannel.cvs.peak1.connect(RedLevelIndicator(cmpTmp, Rect(tmp, tmp, tmp, cmpTmp.bounds.height-tmp-14)).canFocus_(false));
		});
	}
	close {
		if(insert0Win.notNil, {insert0Win.close});
		if(insert1Win.notNil, {insert1Win.close});
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}
	
	//--private
	prContainer {
		var cmp, width, height, margin= 4@4, gap= 4@4;
		position= position ?? {500@500};
		width= RedMixerChannelGUI.width;
		height= RedMixerChannelGUI.height;
		if(parent.isNil, {
			parent= Window(redMixerChannel.class.name, Rect(position.x, position.y, width, height), false);
			win= parent;
			if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
				win.alpha= GUI.skins.redFrik.unfocus;
			});
			win.front;
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
		});
		cmp= CompositeView(parent, width@height)
			.background_(GUI.skins.redFrik.background);
		cmp.decorator= FlowLayout(cmp.bounds, margin, gap);
		cmp.onClose= {this.close};
		^cmp;
	}
}
