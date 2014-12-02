
VoicerGUI : ObjectGui {
// override gui and guify -- this object should make NO VIEWS
// *new should return not a VoicerGUI but a VoicerProxyGui
// perhaps misleading but I want aVoicer.gui to set up the proxy and the gui transparently
// so that none of my old code gets broken

	*new { arg model;
		model.proxy.isNil.if({
			/* model.proxy = */ VoicerProxy.new(model);  // creating object assigns into voicer
		});
		^VoicerProxyGui.new(model.proxy);  // then Object-gui calls .gui on the proxyGui
	}

	smallGui {}	// need this to trick Object-smallGui into thinking I implement the method

}

// todo: check all references to model

VoicerProxyGui : ObjectGui {
		// manages gui editing for voicers
		// includes a flowview for global controls
		// and a flowview for player processes which can be assigned to buttons
		// presently not resizeable (until I learn more about flowviews)
	
	classvar	height = 500, width1 = 300,
			width2 = 140;		// width1 is for controlView; width2 for processView

		// allow pre-population of gui objects for non-existent controls
	classvar	<>drawEmptyControlProxies = true;
	
	var	<mainView,
		<panicButton, <runButton,
		<controlView, <processView,		// flowviews
		<dragSink,					// you'll be dropping things in here
		<masterLayout, <layout, iMadeMasterLayout = false;
	
	var	myModel;

	guify { arg lay,bounds,title, small=false;
		if(lay.isNil,{
			masterLayout = lay = ResizeHeightFlowWindow
				(title ?? { model.asString.copyRange(0,50) },
				Rect(0, 0, width1 + if(small, 10, width2 + 30), height));
			iMadeMasterLayout = true;	// now when I'm removed, I'll close the window too
		},{
			masterLayout = lay;	// should only pass in the FixedWidthMultiPageLayout
			lay = lay.asPageLayout(title,bounds);
		});
		// i am not really a view in the hierarchy
		lay.removeOnClose(this);
		^lay
	}

		// if doResize is false, view will be left too big
		// use that if you want to create many guis in the same window
		// and resize them all at the end
	guiBody { arg lay, backgr, controlBackgr, processBackgr, doResize = true;
		mainView.isNil.if({	// init the views only if we need a new window
			layout = lay;
			dragSink = GUI.dragSink.new(layout, Rect(0, 0, 30, 20))
				.action_({ arg rec;	// receiver is an SCDragSource
						// different classes will handle this differently
						// do nothing if it's not a supported object
					rec.object.tryPerform(\draggedIntoVoicerGUI, this);
				});
			panicButton = ActionButton(layout, "panic", {
				model.voicer.panic
			});
				// ActionButton.defaultHeight is deprecated for future sc versions
				// but needed for backward (i.e., 3.2) compatibility here
			runButton = GUI.button.new(layout, Rect(0, 0, 31, ActionButton.defaultHeight))
				.states_([["run"], ["idle"]])
				.font_(GUI.font.new(*GUI.skin.fontSpecs))
				.action_({ |v|
					model.run(v.value == 0);
				});
			layout.startRow;

			mainView = FixedWidthFlowView.new(layout, Rect(0, 0, width1 + width2 + 10, height),
				margin: 2@2)
				.background_(backgr ? Color.grey);
			controlView = FixedWidthFlowView.new(mainView, Rect(0, 0, width1, height), margin: 2@2)
				.background_(controlBackgr ? Color.grey);
			processView = FixedWidthFlowView.new(mainView, Rect(0, 0, width2, height), margin: 2@2)
				.background_(processBackgr ? Color.grey);
			processView.decorator.nextLine;
			
			model.editor = this;
			this.makeViews;
			doResize.if({
				{ this.sizeWindow; }.defer(0.25);
			});

			myModel = model;
		});
	}

	smallGuiBody { arg lay, backgr, controlBackgr, processBackgr, doResize = true;
		mainView.isNil.if({	// init the views only if we need a new window
			layout = lay;
			dragSink = GUI.dragSink.new(layout, Rect(0, 0, 30, 20))
				.action_({ arg rec;	// receiver is an SCDragSource
						// different classes will handle this differently
						// do nothing if it's not a supported object
					rec.object.tryPerform(\draggedIntoVoicerGUI, this);
				});
			panicButton = ActionButton(layout, "panic", {
				model.voicer.panic
			});
			runButton = GUI.button.new(layout, Rect(0, 0, 31, ActionButton.defaultHeight))
				.states_([["run"], ["idle"]])
				.font_(GUI.font.new(*GUI.skin.fontSpecs))
				.action_({ |v|
					model.run(v.value == 0);
				});

			layout.startRow;

			mainView = FixedWidthFlowView.new(layout, Rect(0, 0, width1 + 10, height), margin: 2@2)
				.background_(backgr ? Color.grey);
			controlView = FixedWidthFlowView.new(mainView, Rect(0, 0, width1, height), margin: 2@2)
				.background_(controlBackgr ? Color.grey);
			
			model.editor = this;
			this.makeViews;
			doResize.if({
				{ this.sizeWindow; }.defer(0.25);
			});

		});
	}
	
	writeName { arg layout;
		var n;
		n = model.asString;
		block { |break|
			['InspButton', 'InspectorLink'].do { |classname|
				if(classname.asClass.notNil) {
					classname.asClass.icon(model, layout);
					break.();
				};
			};
		};
		dragSource = GUI.dragSource.new(layout,Rect(0,0,(n.size * 7.5).max(160),17))
			.stringColor_(Color.new255(70, 130, 200))
			.background_(Color.white)
			.align_(\center)
			.beginDragAction_({ model })
			.object_(n);	
	}
	
	makeViews {
		drawEmptyControlProxies.if({ model.controlProxies }, { model.globalControlsByCreation })
			.do({ arg gc;
				gc.allowGUI.if({
						// "this" is needed because gc might be inactive, and
						// the gc gui object needs to know about the VoicerProxyGui
					gc.makeGUI(false, this);
				});
			});
		processView.notNil.if({
			model.processes.do({ arg p;
				p.makeGUI;
			});
		});
	}
	
	sizeWindow {
			// if I'm the creator of the window, resize the whole window
			// otherwise just resize my container view
		{	iMadeMasterLayout.if({
				masterLayout.recursiveResize;
			}, {
				mainView.recursiveResize;
			});
			nil
		}.defer;
	}
	
	remove {
		if(myModel.notNil) {
			myModel.controlProxies.do({ arg gc;
				gc.gui.remove(false, false);	// don't remove views or resize
											// just break connections--why?
				gc.gui = nil;					// -- view.remove removes all its children
			});
			myModel.editor = nil;
			myModel = nil;
			(view.notNil and: { view.notClosed }).if({
				view.remove;
			});

			iMadeMasterLayout.if({
				masterLayout.close;
			});

			// garbage
			panicButton = runButton = controlView = processView = dragSink =
				masterLayout = myModel = nil;
			
		};
	}
	
	updateStatus {
		var	i, oldNumGCs;
			// if model is nil, I must have been removed already
		if(model.notNil) {
			oldNumGCs = model.controlProxies.size;
			{ 	dragSource.object_(model.asString);    // cf ObjectGui-writeName
				runButton.value = model.isRunning.not.binaryValue;
				nil
			}.defer;
			i = 0;	// index to controlProxies
				// proxies should all be updated by this point
				// only gui guiable controls; sort them in order of creation
			model.globalControlsByCreation.do({ arg gc;
				gc.makeGUI(false);	// don't resize now; makeGUI adds gcproxy to voicerproxy
					// note that makeGUI doesn't make a gui if there already is one
				i = i+1;
			});
				// resize the window only if the number of controlproxies has changed
			(oldNumGCs != model.controlProxies.size).if({
				this.sizeWindow;
			});
		};
	}
	
	refresh { 
		this.sizeWindow;
	}
	
	minNameWidth { ^200 }

}
