//redFrik

//related:
//RedEffectModule RedInstrumentModuleGUI RedMixGUI RedMixerChannelGUI

RedEffectModuleGUI {
	var <redEffectModule, <parent, position,
		<views, <mirror, win;
	*new {|redEffectModule, parent, position|
		^super.newCopyArgs(redEffectModule, parent, position).initRedEffectModuleGUI(false);
	}
	*newMirror {|redEffectModule, parent, position|
		^super.newCopyArgs(redEffectModule, parent, position).initRedEffectModuleGUI(true);
	}
	initRedEffectModuleGUI {|argMirror|
		var cmp, classes, params;
		mirror= argMirror;
		if(mirror, {
			classes= (
				\slider: RedGUICVSliderMirror,
				\knob: RedGUICVKnobNumberNameMirror
			);
		}, {
			classes= (
				\slider: RedGUICVSlider,
				\knob: RedGUICVKnobNumberName
			);
		});
		params= redEffectModule.def.metadata[\order].reject{|x| x.key==\out};
		cmp= this.prContainer(params.size-1);
		views= params.collect{|x|
			if(x.key==\mix, {
				classes[\slider].new(cmp, nil, redEffectModule.cvs[x.value]);
			}, {
				classes[\knob].new(cmp, nil, redEffectModule.cvs[x.value], x.value);
			});
		};
	}
	close {
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}
	
	//--private
	prContainer {|size|
		var cmp, width, height, margin= 4@4, gap= 4@4;
		position= position ?? {400@400};
		width= margin.x+RedGUICVSlider.defaultWidth+((RedGUICVKnobNumberName.defaultWidth+gap.x)*size)+margin.x;
		height= margin.y+RedGUICVSlider.defaultHeight.max(RedGUICVKnobNumberName.defaultHeight)+margin.y;
		if(parent.isNil, {
			parent= Window(redEffectModule.class.name, Rect(position.x, position.y, width, height), false);
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
		^cmp;
	}
}
