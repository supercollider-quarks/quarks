//redFrik

RedToolsMenu {
	classvar <>list;
	*initClass {
		StartUp.add({
			
			//--list of tools in the format: [ [category, name], function, [category, name], function, ... ]
			list= List[
//				['redSys', 'RedDiskInPlayer'], {
//					RedDiskInPlayer.new;
//				},
				['redSys', '~redEfx'], {
					if(~redEfx.isKindOf(RedEffectsRack), {
						"overwrote ~redEfx with a new".warn;
						~redEfx.free;
					});
					~redEfx= RedEffectsRack(
						RedEffectsRack.defaultClasses
					);
					RedEffectsRackGUI(~redEfx);
				},
				['redSys', '~redEfx2'], {
					if(~redEfx2.isKindOf(RedEffectsRack), {
						"overwrote ~redEfx2 with a new".warn;
						~redEfx2.free;
					});
					if(~redEfx.notNil, {
						~redEfx2= RedEffectsRack(RedEffectModule.subclasses, 0, ~redEfx.group);
					}, {
						~redEfx2= RedEffectsRack(RedEffectModule.subclasses);//bus 0, after defaultGroup
					});
					RedEffectsRackGUI(~redEfx2, 595@333);
				},
				['redSys', '~redMatrixMixer'], {
					if(~redMatrixMixer.isKindOf(RedMatrixMixer), {
						"overwrote ~redMatrixMixer with a new".warn;
					});
					~redMatrixMixer= RedMatrixMixer.new;
					RedMatrixMixerGUI(~redMatrixMixer);
				},
				['redSys', '~redMixer'], {
					if(~redMixer.isKindOf(RedMixer), {
						"overwrote ~redMixer with a new".warn;
					});
					~redMixer= RedMixer.new;
					RedMixerGUI(~redMixer);
				},
				['redSys', '~redMixStereo'], {
					if(~redMixStereo.isKindOf(RedMixStereo), {
						"overwrote ~redMixStereo with a new".warn;
					});
					~redMixStereo= RedMixStereo.new;
					RedMixGUI(~redMixStereo);
				},
				['redSys', 'RedTapTempoGUI'], {
					RedTapTempoGUI(TempoClock(1));		//or which clock to use?
				},
				['redSys', 'RedTempoClockGUI'], {
					RedTempoClockGUI.new;
				},
				['redSys', 'RedTest'], {
					RedTest.openHelpFile;
				},
				['redSys', 'Redraw'], {
					Redraw.new;
				},
				['redSys', 'redSys overview'], {
					RedSys.openHelpFile;
				},
				['system', 'SynthDescLib read+browse'], {
					SynthDescLib.read.global.browse;
				},
				['system', 'post Event defaults'], {
					Event.default.parent.associationsDo(_.postln);
				},
				['system', 'post path'], {
					Dialog.getPaths{|paths| paths.do{|x| x.postln}};
				},
				['system', 'post specs'], {
					Spec.specs.keysValuesDo{|key, val| if(val.class==ControlSpec, {[key, val].postln})};
				},
				['system', 'count characters'], {
					("there are"+Document.current.selectedText.size+"characters in the current selection").postln;
				},
				['system', 'post all window positions'], {
					Window.allWindows.do{|x| x.name.post; "   ".post; x.bounds.postln};
				},
				['system', 'post all document positions'], {
					Document.allDocuments.do{|x| x.title.post; "   ".post; x.bounds.postln};
				},
				['template', 'post all incoming osc'], {
					Document(
						"listen to all incoming osc",
						"//start (sc3.4)\nthisProcess.recvOSCfunc= {|time, addr, msg| if(msg[0].asString.contains(\"status.reply\").not, {(\"time:\"+time+\"sender:\"+addr+\"\\nmessage:\"+msg).postln})};\n//stop\nthisProcess.recvOSCfunc= nil;\n\n//for sc3.5\nOSCFunc.trace(true);\nOSCFunc.trace(false);"
					).syntaxColorize;
				},
				['template', 'normalize soundfile'], {
					Document(
						"normalize soundfile",
						"//--edit paths and evaluate the code below.  it will take a while for large files\nSoundFile.normalize(\n\t\"~/Music/SuperCollider Recordings/SC_090410_125330.aiff\".standardizePath,\n\t\"~/Music/SuperCollider Recordings/SC_090410_125330+.aiff\".standardizePath,\n\tnil, //\"AIFF\" \"WAVE\"\n\t\"int16\"\n)"
					).syntaxColorize;
				},
				['template', 'userview'], {
					Document(
						"userview",
						"(\nvar width= 500, height= 500;\nvar win= Window(\"animation template\", Rect(300, 300, width, height), false);\nvar usr= UserView(win, Rect(0, 0, width, height));\nusr.background= Color.white;\nusr.clearOnRefresh= true;\nusr.animate= true;\nusr.drawFunc= {\n\tPen.smoothing= true;\n\tPen.width= 1;\n\tPen.fillColor= Color.red;\n\tPen.fillOval(Rect(usr.frame*3%width, usr.frame*4%height, 20, 20));\n};\nwin.front;\nCmdPeriod.doOnce({if(win.isClosed.not, {win.close})});\n)"
					).syntaxColorize;
				},
				['extras', 'random helpfile'], {
					var files;
					if(Main.versionAtLeast(3, 5), {
						files= List.new;
						PathName(Help.dir).filesDo{|x| if(x.extension=="html", {files.add(x)})};
						files.choose.fileNameWithoutExtension.openHelpFile;
					}, {
						Document.open(PathName("Help").deepFiles.reject{|x| #[\jpg, \png, \qtz].includes(x.extension.asSymbol)}.choose.fullPath);
					});
				},
				['extras', 'swing boot'], {
					if('SwingOSC'.asClass.notNil, {
						SwingOSC.default.boot;
						GUI.swing;
					}, {
						"swingosc not installed".warn;
					});
				},
				['extras', 'Quarks.gui'], {
					Quarks.gui;
				},
				['extras', 'Quarks.checkoutAll'], {
					Quarks.checkoutAll;
				},
				['extras', 'open RedToolsMenu.sc'], {
					RedToolsMenu.openCodeFile
				},
				['extras', 'open startup'], {
					var extensions= #[".scd", ".rtf"];
					extensions.do{|x|
						var path= PathName(Platform.userExtensionDir).pathOnly++"startup"++x;
						if(File.exists(path), {
							Document.open(path);
						});
					};
				}
			];
			
			RedToolsMenu.listToMenuLibrary;
		});
	}
	
	//--add tools to Library
	*listToLibrary {
		list.pairsDo{|x, y| Library.putList([\redTools]++x, y)}
	}
	
	//--add tools to Library menu (osx only) to open with cmd+r shortcut
	*listToMenuLibrary {
		Platform.case(\osx, {
			if(GUI.id==\cocoa, {
				list.pairsDo{|x, y| CocoaMenuItem.add(([\redTools]++x).collect{|x| x.asString}, y)};
				CocoaMenuItem.add(["redToolsMenu"], {RedToolsMenu.makeWindow}).setShortCut("r", false, false);
			});
		});
	}
	
	//--adds tools to list
	*add {|nameArray, func|
		list.add(nameArray);
		list.add(func);
	}
	
	//--create separate window with listview
	*makeWindow {|position|
		var w, names= [], fnt= RedFont.new, width= 0, height;
		position= position ?? {6@50};
		list.pairsDo{|x, y|
			var tempWidth= x[1].asString.bounds(fnt).width;
			names= names.add(x[1]);
			if(width<tempWidth, {
				width= tempWidth;
			});
		};
		switch(GUI.id,
			\cocoa, {
				width= width+"aaa".bounds(fnt).width;
				height= "".bounds(fnt).height+3*names.size;
			},
			\swing, {
				width= width+"aaaa".bounds(fnt).width;
				height= "".bounds(fnt).height+3*names.size;
			},
			\qt, {
				width= width+"aaa".bounds(fnt).width;
				height= " ".bounds(fnt).height+2.14*names.size;
			}
		);
		w= Window("_redTools", Rect(position.x, position.y, width, height), false)
			.front;
		if(Main.versionAtMost(3, 4) and:{GUI.scheme!=\cocoa}, {
			w.alpha_(GUI.skins[\redFrik].unfocus);
		});
		ListView(w, w.view.bounds.width@w.view.bounds.height)
			.font_(RedFont.new)
			.focus
			.background_(GUI.skins[\redFrik].background)
			.stringColor_(GUI.skins[\redFrik].foreground)
			.hiliteColor_(GUI.skins[\redFrik].selection)
			.items_(names)
			.enterKeyAction_{|view|
				(list[view.value*2+1]).value;
				w.close;
			};
	}
}
