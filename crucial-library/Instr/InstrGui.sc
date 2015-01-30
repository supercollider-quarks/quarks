

InstrGui : ObjectGui {
	
	guiBody { arg layout;
		var defs,h,w,specWidth;
        var width,test;

        width = min(layout.indentedRemaining.width,600);
		specWidth = width - 150 - 100 - 6;

		model.argNames.do({ arg a,i;
			layout.startRow;
			ArgNameLabel(  a ,layout,150);
			CXLabel(layout, " = " ++ model.defArgAt(i).asString,100);
			model.specs.at(i).asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);
		});
		layout.startRow;
		CXLabel(layout,"outSpec:",150);
		CXLabel(layout, model.outSpec.asString, 100);
		model.outSpec.asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);

		if(model.path.notNil,{
			CXLabel(layout.startRow,model.path,width);
		});

		layout.startRow;
		if(model.path.notNil and: { File.exists(model.path) },{
			ActionButton(layout,"open file",{ model.path.openTextFile });
		});
		ActionButton(layout,"make a Patch",{ Patch(model).topGui });
		ToggleButton(layout,"Test",{ 
		    test = Patch(model).rand;
		    test.play
		},{
		    test.free;
		});
		ActionButton(layout,"post Instr name",{
		    model.dotNotation.post;
		}).beginDragAction = {model.dotNotation};

        this.sourceGui(layout,width);
	}
	sourceGui { arg layout,width;
	    var source,up,funcDef;
		var tf,lines,height,f;

		funcDef = model.funcDef;
	    if(funcDef.notNil,{
			layout.startRow;
			source = funcDef.sourceCode;
			if(source.notNil,{
				f = GUI.font.new("Courier",12.0);
				height = source.split(Char.nl).size * 15 + 20;
				tf = TextView(layout,Rect(0,0,width,height));
				tf.string = source;
				tf.font_(f);
				tf.syntaxColorize;
				up = Updater(model,{
				    source = model.funcDef.sourceCode;
				    if(tf.isClosed,{
				        up.remove //sc remove gui is easily breakable
				    },{
					    tf.string = source;
					    tf.syntaxColorize;
					});
		        }).removeOnClose(layout)
			},{
			    CXLabel(layout,"Source code is nil",width);
			});
		})
	}
}


UGenInstrGui : InstrGui {
	
	sourceGui { arg layout;
	    ActionButton(layout,"Help",{
	        model.ugenClass.openHelpFile
	    });
	}
}


PappliedInstrGui : InstrGui {

    writeName { arg layout;
        CXLabel(layout,"Partial application of:");
        ActionButton(layout,model.a.dotNotation,{
            model.a.gui
        });
    }
}


CompositeInstrGui : InstrGui {
	
	guiBody { arg layout;
		var defs,h,w,specWidth;
        var width;

        width = min(layout.indentedRemaining.width,600);
		specWidth = width - 150 - 100 - 6;

		model.argNames.do({ arg a,i;
			if(i == 0,{
				ActionButton(layout.startRow,model.a.dotNotation,{
					model.a.gui
				});
			});
			if(i == model.a.argsSize,{
				ActionButton(layout.startRow,model.b.dotNotation,{
					model.b.gui
				});
			});
			layout.startRow;
			ArgNameLabel(  a ,layout,150);
			CXLabel(layout, " = " ++ model.defArgAt(i).asString,100);
			model.specs.at(i).asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);
		});
		layout.startRow;
		CXLabel(layout,"outSpec:",150);
		CXLabel(layout, model.outSpec.asString, 100);
		model.outSpec.asCompileString.gui(layout,specWidth@GUI.skin.buttonHeight);

		layout.startRow;
		ActionButton(layout,"make a Patch",{ Patch(model).topGui });
		// model.a.  sourceGui
		// model.b  sourceGui
	}	
}

