//redFrik

//--related:
//RedAbstractMix RedEffectModuleGUI RedMixerGUI RedMatrixMixerGUI RedTapTempoGUI

RedMixGUI {
	var <redMix, <parent, position,
		win;
	*new {|redMix, parent, position|
		^super.newCopyArgs(redMix, parent, position).initRedMixGUI;
	}
	initRedMixGUI {
		var cmp= this.prContainer;
		
		redMix.cvs.inA.connect(RedNumberBox(cmp));
		RedStaticText(cmp, "inA ("++redMix.def.metadata.info.inA++")");
		
		cmp.decorator.nextLine;
		redMix.cvs.inB.connect(RedNumberBox(cmp));
		RedStaticText(cmp, "inB ("++redMix.def.metadata.info.inB++")");
		
		cmp.decorator.nextLine;
		redMix.cvs.out.connect(RedNumberBox(cmp));
		RedStaticText(cmp, "out (stereo)");
		
		cmp.decorator.nextLine;
		redMix.cvs.lag.connect(RedNumberBox(cmp));
		RedStaticText(cmp, "lag");
		
		cmp.decorator.nextLine;
		[redMix.cvs.mix, redMix.cvs.amp].connect(
			Red2DSlider(
				cmp,
				cmp.decorator.indentedRemaining.width@cmp.decorator.indentedRemaining.height
			).mouseUpAction_{|view, x, y, mod|
				if(mod.bitAnd(262144)==262144, {
					{redMix.cvs.mix.value= 0}.defer(0.1);
				});
			}
		);
	}
	close {
		if(win.notNil and:{win.isClosed.not}, {win.close});
	}
	
	//--private
	prContainer {
		var cmp, width, height, margin= 4@4, gap= 4@4;
		position= position ?? {600@400};
		width= 120;
		height= 140;
		if(parent.isNil, {
			parent= Window(redMix.class.name, Rect(position.x, position.y, width, height), false);
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
