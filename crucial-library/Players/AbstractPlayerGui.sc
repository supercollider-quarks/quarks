

AbstractPlayerGui : ObjectGui {

	gui { arg parent,bounds ... args;
		var layout,inner;

		layout = this.guify(parent,bounds);
		if(parent.isNil,{
			// top level controls
			this.synthConsole(layout);
			this.saveConsole(layout);
			layout.startRow;
		});

		inner = layout.flow({ arg layout;
			this.view = layout;
			this.writeName(layout);
			this.guiBody(layout);
		},bounds).background_(this.background);
		
		this.enableKeyDowns;

		if(parent.isNil,{ 
			layout.resizeToFit 
		});

		if(parent.isNil,{
			layout.front;
			view.focus;
		})
	}
	background { ^Color.yellow(0.3,alpha:0.1) }
	topGui { arg parent,bounds ... args;
		var layout;
		layout=this.guify(parent,bounds);
		// top level controls
		this.synthConsole(layout);
		this.saveConsole(layout);
		layout.startRow;
		this.performList(\gui,[layout,bounds] ++ args);

		this.enableKeyDowns;
		if(parent.isNil,{
			layout.resizeToFit;
			layout.front;
			view.focus;
		})
	}

	writeName { arg layout;
		this.prWriteName(layout,this.model.asString);
		if(\InspButton.asClass.notNil,{
			InspButton.icon(model,layout)
		});
		if(model.path.notNil,{
			ActionButton(layout,"edit source",{
				model.path.openTextFile;
			});
		});
	}
	viewDidClose {
		if(model.isPlaying.not,{
			model.free
		});
		super.viewDidClose;
	}
	
	keyDownResponder {
		var k;
		k = KeyCodeResponder.new;
		k.registerKeycode(KeyCodeResponder.normalModifier,49,{
			if(model.isPlaying,{
				model.stop
			},{
				model.play
			})
		});
		^k
	}
	saveConsole { arg layout;
		^SaveConsole(model,model.path,layout).save
			.saveAs({arg path;
				model.didSaveAs(path)
			})
			.printPath
			.print;
	}

	synthConsole { arg layout;
		var s, server;
		server = model.server.asTarget.server;
		server.gui(layout).meters(layout);
		s = SynthConsole(model,layout).play.record.stop.free.tempo;
		//ServerErrorGui(server).gui(layout);
	}

	durationString { // time
		var dur,div;
		dur = model.timeDuration;
		if(dur.notNil,{
			^dur.asTimeString;
		},{
			^"inf"
		});
	}
	durationGui { arg layout;
		var durb;
		var durv;
		// make switchable between beats and secs

		durv= CXLabel(layout, "dur:_____");

		layout.removeOnClose(
			Updater(model,{  arg changed,changer;// any change to model at all
				durv.label_("dur:" + this.durationString).refresh;
			}).update;
		);

		layout.removeOnClose(
			Updater(Tempo.default,{ // any change to Tempo
				durv.label_("dur:" + this.durationString).refresh;
			}).update;
		);
	}
}


KrPlayerGui : AbstractPlayerGui {}


