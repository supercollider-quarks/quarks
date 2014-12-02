//redFrik

//--related:
//RedTapTempoGUI

RedTempoClockGUI {
	var <>win;
	*new {|position|
		^super.new.initRedTempoClockGUI(position);
	}
	initRedTempoClockGUI {|position|
		var cmp, pop, num, bpm, sld;
		var spec= #[0.5, 5].asSpec, index= 0, clocks= [];
		position= position ?? {100@200};
		win= Window("TempoClock.all", Rect(position.x, position.y, 214, 60), false);
		cmp= CompositeView(win, Rect(0, 0, win.bounds.width, win.bounds.height));
		cmp.background= GUI.skins.redFrik.background;
		cmp.decorator= FlowLayout(cmp.bounds);
		pop= RedPopUpMenu(cmp, 200@14);
		cmp.decorator.nextLine;
		RedStaticText(cmp, "bps:", 30@14);
		num= RedNumberBox(cmp, 40@14);
		bpm= RedStaticText(cmp, "(bpm:)", 128@14);
		cmp.decorator.nextLine;
		sld= RedSlider(cmp, 200@14);
		Routine({
			inf.do{
				if(clocks!=TempoClock.all.collect{|x| x.hash}, {
					(this.class.name++": new TempoClock detected").postln;
					clocks= TempoClock.all.collect{|x| x.hash};
					pop.items= TempoClock.all.collect{|x, i| "TempoClock["++i++"]"};
				});
				1.wait;
			};
		}).play(AppClock);
		num.action= {|view|
			sld.value= spec.unmap(view.value);
			bpm.string= "(bpm:"++(view.value*60)++")";
			TempoClock.all[index].tempo= view.value;
		};
		sld.action= {|view|
			num.valueAction_(spec.map(view.value));
		};
		pop.action= {|view|
			index= view.value;
			num.valueAction_(TempoClock.all[index].tempo);
		};
		if(TempoClock.all.notEmpty, {
			num.valueAction_(TempoClock.all[0].tempo);
		});
		win.front;
		CmdPeriod.doOnce({win.close});
	}
	close {
		if(win.isClosed.not, {
			win.close;
		});
	}
}
