//redFrik

//--changes090829:
//drag and drop from finder using a dragsink
//--changes090613:
//no longer use the GUI redirect (GUI.button.new became Button)
//--changes090123:
//changed boxColor_ to background_
//ctrl click to reset volume to 0db

//--todo:
//* colours and font from skin?
//* avoid n_set node not found when setting volume

RedDiskInPlayer {
	var <sampler, <isPlaying= false, <soundFiles, <win,
		playIndex, bgcol, fgcol, incdecTask,
		incView, decView, progressView, infoView, volNumView, volSldView,
		envNumView, envSldView, listView, busView, loopView, filterView;
	*new {|server, bus= 0, numItems= 10|
		^super.new.initRedDiskInPlayer(server, bus, numItems);
	}
	initRedDiskInPlayer {|argServer, argBus, argNumItems|
		var
			server= argServer ?? Server.default,
			w= 160,								//widget max width
			h= 18,								//widget height
			fnt= Font("Monaco", 9),					//later from skin
			volSpec= [-90, 6, \db].asSpec;
		
		bgcol= Color.red(0.8);						//later from skin
		fgcol= Color.black;						//later from skin
		soundFiles= [];
		
		win= Window(this.class.name, Rect(500, 200, w+10, h*15), false);
		if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
			win.alpha_(0.9);
		});
		win.view.background= bgcol;
		win.view.decorator= FlowLayout(win.view.bounds);
		
		volNumView= NumberBox(win, Rect(0, 0, w*0.25, h))
			.background_(bgcol)
			.typingColor_(Color.white)
			.value_(0)
			.action_{|view|
				volSldView.value= volSpec.unmap(view.value);
				if(isPlaying, {sampler.amp= volNumView.value.dbamp});
			};
		volSldView= Slider(win, Rect(0, 0, w*0.6, h))
			.knobColor_(fgcol)
			.value_(volSpec.unmap(0))
			.action_{|view|
				volNumView.value= volSpec.map(view.value).round(0.1);
				if(isPlaying, {sampler.amp= volNumView.value.dbamp});
			}
			.mouseUpAction_{|view, x, y, mod|
				if(mod&262144==262144, {			//ctrl to reset
					{view.valueAction= volSpec.unmap(0)}.defer(0.1);
				});
			};
		StaticText(win, Rect(0, 0, "vol".bounds(fnt).width, h))
			.string_("vol");
		win.view.decorator.nextLine;
		
		envNumView= NumberBox(win, Rect(0, 0, w*0.25, h))
			.background_(bgcol)
			.typingColor_(Color.white)
			.value_(0.05)
			.action_{|view|
				view.value= view.value.max(0);
				envSldView.value= (view.value/10).min(1);
			};
		envSldView= Slider(win, Rect(0, 0, w*0.6, h))
			.knobColor_(fgcol)
			.action_{|view|
				envNumView.value= (view.value*10).round(0.1);
			};
		StaticText(win, Rect(0, 0, "env".bounds(fnt).width, h))
			.string_("env");
		win.view.decorator.nextLine;
		
		busView= NumberBox(win, Rect(0, 0, w*0.25, h))
			.background_(bgcol)
			.typingColor_(Color.white)
			.value_(argBus)
			.action_{|view|
				view.value= view.value.asInteger.max(0);
			};
		StaticText(win, Rect(0, 0, "bus".bounds(fnt).width, h))
			.string_("bus");
		win.view.decorator.shift(10, 0);
		loopView= Button(win, Rect(0, 0, w*0.4, h))
			.states_([["loop", fgcol, Color.clear], ["loop", bgcol, fgcol]]);
		win.view.decorator.nextLine;
		
		win.view.decorator.shift(10, 0);
		incView= StaticText(win, Rect(0, 0, w*0.4, h)).string_("0:00");
		win.view.decorator.shift(10, 0);
		decView= StaticText(win, Rect(0, 0, w*0.4, h)).string_("0:00.0");
		win.view.decorator.nextLine;
		
		progressView= MultiSliderView(win, Rect(0, 0, w, h))
			.indexIsHorizontal_(false)
			.editable_(false)
			.indexThumbSize_(h)
			.valueThumbSize_(0)
			.isFilled_(true)
			.canFocus_(false)
			.value_([0]);
		win.view.decorator.nextLine;
		
		infoView= StaticText(win, Rect(0, 0, w, h));
		win.view.decorator.nextLine;
		
		listView= ListView(win, Rect(0, 0, w, h*argNumItems))
			.canReceiveDragHandler_{View.currentDrag.isKindOf(Array)}
			.receiveDragHandler_{|view|
				var path= PathName(View.currentDrag[0]);
				if(path.isFolder, {
					soundFiles= SoundFile.collect(path.fullPath++"/*");
				}, {
					soundFiles= SoundFile.collect(path.pathOnly++"*");
				});
				if(filterView.value>0, {
					soundFiles= soundFiles.select{|x| x.numChannels==filterView.value};
				});
				soundFiles.do{|x, i|
					sampler.prepareForPlay(i, x.path);
				};
				view.items= soundFiles.collect{|x| PathName(x.path).fileName};
				this.prUpdateInfo(0);
			}
			.background_(bgcol)
			.hiliteColor_(bgcol)
			.selectedStringColor_(Color.white)
			.action_{|view|
				this.prUpdateInfo(view.value);
				if(isPlaying, {
					this.prStopFunc(view);
				});
			}
			.enterKeyAction_{|view|
				if(soundFiles[view.value].notNil, {
					if(isPlaying, {
						this.prStopFunc(view);
					}, {
						this.prPlayFunc(view);
					});
				});
			};
		win.view.decorator.nextLine;
		
		Button(win, Rect(0, 0, w*0.4, h))
			.states_([["folder...", fgcol, Color.clear]])
			.action_{
				if(sampler.notNil, {sampler.free});
				Dialog.getPaths({|x|
					var path= PathName(x[0]);
					soundFiles= SoundFile.collect(path.pathOnly++"*");
					if(filterView.value>0, {
						soundFiles= soundFiles.select{|x| x.numChannels==filterView.value};
					});
					soundFiles.do{|x, i|
						sampler.prepareForPlay(i, x.path);
					};
					listView.items= soundFiles.collect{|x| PathName(x.path).fileName};
					this.prUpdateInfo(0);
				});
				listView.focus;
			}.focus;
		win.view.decorator.shift(10, 0);
		filterView= NumberBox(win, Rect(0, 0, w*0.2, h))
			.background_(bgcol)
			.typingColor_(Color.white)
			.value_(0)
			.action_{|view|
				view.value= view.value.max(0).round;
			};
		StaticText(win, Rect(0, 0, "filter".bounds(fnt).width, h))
			.string_("filter");
		
		win.view.children.do{|x| if(x.respondsTo('font_'), {x.font_(fnt)})};
		win.bounds= win.bounds.setExtent(win.bounds.width, win.view.decorator.currentBounds.height+4);
		Routine.run{
			server.bootSync;
			sampler= RedDiskInSampler(server);
			server.sync;
			
			CmdPeriod.doOnce({if(win.isClosed.not, {win.close})});
			
			defer{
				win.onClose= {incdecTask.stop; sampler.free};
				win.front;
			};
		};
	}
	bus {
		^busView.value;
	}
	free {
		win.close;
	}
	
	//--private
	prPlayFunc {|view|
		isPlaying= true;
		playIndex= view.value;
		view.selectedStringColor_(Color.red);
		view.hiliteColor_(fgcol);
		sampler.play(
			playIndex,
			envNumView.value,
			nil,
			envNumView.value,
			volNumView.value.dbamp,
			busView.value,
			nil,
			loopView.value
		);
		incdecTask.stop;
		incdecTask= Routine({
			var startTime= SystemClock.seconds;
			var stopTime= sampler.length(playIndex);
			decView.string= stopTime.asTimeString;
			inf.do{
				var now= SystemClock.seconds-startTime;
				incView.string= now.round.asTimeString;
				10.do{
					0.1.wait;
					now= SystemClock.seconds-startTime;
					progressView.value= [(now/stopTime).min(1)];
				};
			};
		}).play(AppClock);
	}
	prStopFunc {|view|
		isPlaying= false;
		if(sampler.playingKeys.notEmpty, {
			sampler.stop(playIndex, envNumView.value);
		});
		incdecTask.stop;
		view.selectedStringColor_(Color.white);
		view.hiliteColor_(bgcol);
		progressView.value= #[0];
		incView.string= "0:00";
	}
	prUpdateInfo {|index|
		var sf= soundFiles[index];
		if(sf.notNil, {
			decView.string= sf.duration.asTimeString(0.01);
			infoView.string= "".scatList([
				sf.numChannels,
				sf.headerFormat,
				sf.sampleFormat,
				sf.sampleRate
			]);
		});
	}
}
/*
RedDiskInPlayer.new(bus:1, numItems:20)
*/