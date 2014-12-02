

ObjectInsp : ObjectGui {

	classvar displayHooks;

	writeName { arg layout;
		ClassNameLabel.newBig(model.class,layout);
		GUI.dragSource.new(layout,Rect(0,0,500,30))
			.object_(model)
			.background_(Color.white)
			.align_(\center)
			.font_(GUI.font.new("Helvetica-Bold",18));
	}

	guiBody { arg layout;
		var vert,list,listItems,actions,val;
		listItems = List.new;
		actions = List.new;

		displayHooks.at(model.class).value(model,layout);

		layout.startRow;
		this.instVarsGui(listItems,actions);

		// slotAt
		if(model.isArray,{
			min(model.slotSize,2048).do({arg i;
				var iv;
				listItems.add( ("@" ++ i).as(Array).extend(25,$ ).as(String) ++"=  " + model.slotAt(i).asString );
				actions.add({ model.slotAt(i).insp(model,"@"++i) });
			});
			if(model.slotSize > 300,{
				SimpleLabel(layout,"... slotSize is" ++ model.slotSize.asString,160).bold;
			});
		});

		list = ListView(layout,(layout.bounds.width-20)@600);
		list.font = GUI.font.new("Courier",10);
		list.background = Color(0.96777196663153, 0.97014925373134, 0.95566941412341);
		list.items = listItems.array.add("");
		list.action = {
			val = actions[list.value];
		};
		list.mouseUpAction = {
			val.value
		};
		list.enterKeyAction = {
			val.value
		};
		list.value = list.items.size - 1;
		this.dependantsGui(layout);
		this.actionsGui(layout);
	}

	instVarsGui { arg listItems,actions;
		var iNames;
		iNames = model.class.instVarNames;
		if(iNames.notNil,{
			iNames.do({arg v,i;
				var iv,str;
				iv=model.instVarAt(i);
				{
	    			str = v.asString;
				}.try({ arg err;
					str = "ERROR: during asString " + v.class.name;
	    		});
				listItems.add( str.as(Array).extend(25,$ ).as(String) ++ "=  " + iv);
				actions.add({ iv.insp(model,v) });
			});
		});
	}
	dependantsGui { arg layout;
		SimpleLabel(layout.startRow,"dependants:",layout.bounds.width - 30).bold;
		layout.startRow;
		model.dependants.do({ arg d;
			InspButton(d,layout);
		});
	}
	actionsGui { arg layout;
		layout.startRow;
		SimpleButton(layout,"re-inspect now",{ model.insp });
		SimpleButton(layout,"gui",{ model.gui });
		SimpleButton(layout,"post",{ model.asCompileString.postln });
		SimpleButton(layout,"dump",{ model.dump });
		SimpleButton(layout,"assign to var x",{
			thisProcess.interpreter.perform('x_',model);
		});
		SimpleButton(layout,"assign to var y",{
			thisProcess.interpreter.perform('y_',model);
		});
		SimpleButton(layout,"open class file",{
			model.class.openCodeFile;
		});
	}

	*initClass {
		displayHooks = IdentityDictionary.new;

		// some standard library hooks
		this.registerHook(Function,{ arg model,layout;
			layout.startRow;
			if(model.def.sourceCode.notNil,{
				this.sourceCodeGui(model.def.sourceCode,layout,700);
			})
		});
		this.registerHook(Dictionary,{ arg model,layout;
			layout.startRow;
			model.keysValuesDo({ arg k,v;
				layout.startRow;
				SimpleLabel(layout,k.asString,200);
				InspButton(v,layout,300);
			});
		});

		this.registerHook(Synth,{ arg model,layout;
			var sd;
			layout.startRow;
			ServerLog.guiMsgsForSynth(model,layout);
			if(\Annotations.asClass.notNil,{
				Annotations.guiFindNode(model.nodeID,layout);
			});
			if(\InstrSynthDef.asClass.notNil,{
				if(model.defName.notNil,{
					sd = InstrSynthDef.cacheAt(model.defName,model.server);
					if(sd.notNil,{
						InspButton(sd,layout);
					})
				})
			})
		});
		this.registerHook(Bus,{ arg bus,layout;
			layout.startRow;
			SimpleButton(layout,"log...",{
				ServerLog.guiMsgsForBus(bus.index,bus.rate,nil,bus.server);
			});
			if(\Annotations.asClass.notNil,{
				Annotations.guiFindBus(bus.index,bus.rate,layout);
			});
		});
		this.registerHook(SynthDef,{ arg def,layout;
			def.allControlNames.do { arg cn,i;
				ArgName(cn.name,layout.startRow);
				SimpleLabel(layout,cn.rate,100);
				SimpleLabel(layout,cn.defaultValue,300);
			};
			layout.startRow;
			ArgName("Num UGens",layout);
			SimpleLabel(layout,def.children.size.asString,100);
			SimpleButton(layout,"graph...",{
				SynthDefGraph(def)
			},100);

			if(\InstrSynthDef.asClass.notNil and: {def.isKindOf(InstrSynthDef)},{
				InspButton(def.instrName.asInstr ? def.instrName,layout.startRow);
			});
			if(def.name.notNil,{
				SimpleButton(layout.startRow,"search ServerLog...",{
					ServerLog.guiMsgsForSynthDef(def.name);
				})
			})
		});
	}
	*registerHook { arg class,displayFunction,includeSubclasses=true;
		if(includeSubclasses,{
			class.allSubclasses.add(class).do { arg klass;
				if(displayHooks[klass].isNil,{
					displayHooks[klass] = displayFunction;// arg object, layout
				})
			}
		},{
			displayHooks[class] = displayFunction;// arg object, layout
		})
	}

	*sourceCodeGui { arg sourceCode, layout,width=700;
		var f,height,tf;
		f = GUI.font.new("Courier",12.0);
		height = sourceCode.split(Char.nl).size * 15 + 20;
		tf = TextView(layout,Rect(0,0,width,height));
		tf.string = sourceCode;
		tf.font_(f);
		tf.syntaxColorize;
		^tf
	}
}


ClassGui : ObjectInsp {

	writeName {}

	guiBody { arg layout;

		var iNames,supers,scale,width,layoutWidth;

		layout.scroll({ arg layout;
			layout.flow({ arg layout;
				layoutWidth = layout.bounds.width - 30;
				if(model.superclass.notNil,{
					width = layoutWidth / (model.superclasses.size + 1);
				},{
					width = layoutWidth;
				});
				SimpleLabel(layout,model.asString,[width,30],font:Font("Helvetica-Bold",18));
				layout.startRow;
				SimpleButton(layout, "Source code",{
					model.openCodeFile;
				}).font_(GUI.font.new("Monaco",9.0));
				SimpleButton(layout,"Help file",{
					var path;
					model.openHelpFile;
				});

				if(model.superclass.notNil,{
					SimpleLabel(layout.startRow,"superclasses:",layoutWidth).bold;
					supers = model.superclasses;
					scale = supers.size;
					supers.do({ arg sup,i;
						ClassNameLabel(sup,layout,width,30);
					})
				});

				// explicit references
				/*
				SimpleLabel(layout.startRow,"classes explicitly referenced in source:");
				layout.startRow;
				model.explicitClassReferences.do({ arg c;
					ClassNameLabel(c.asClass,layout,200);
				});
				*/

				// classVarnames
				if(model.classVarNames.size > 0,{
					SimpleLabel(layout.startRow,"classvars:",layoutWidth).bold;
					model.classVarNames.size.do({ arg cvi;
						var iv;
						VariableNameLabel(model.classVarNames.at(cvi),layout.startRow);
						iv=model.classVars.at(cvi);
						//ClassNameLabel(iv.class,layout);
						InspButton(iv,layout);
					});
				});

				//instance vars
				if(model.instVarNames.size > 0,{
					SimpleLabel(layout.startRow,"vars:",layoutWidth).bold;
					layout.startRow;
					model.instVarNames.size.do({ arg ivi;
						VariableNameLabel(model.instVarNames.at(ivi),layout,minWidth:width);
						// iprototype
					});
				});

				SimpleLabel(layout.startRow,"methods:",layoutWidth).bold;
				layout.startRow;
				// meta_class methods
				if(model.class.methods.size > 0,{
					model.class.methods.size.do({ arg cmi;
						MethodLabel.classMethod(model.class.methods.at(cmi),layout.startRow,minWidth:width);
					});
				});

				// cprototype
				// filenameSymbol
				// MethodBrowser(class)
				// show up to 50 methods, up the superclass chain (stop at object ?)
				// show when methods are overriden by subclass
				//if(model.methods.size < 50,{
					this.displayMethodsOf(model,layout,true,width);
				//},{
				//	SimpleButton(layout.startRow,"display instance methods (" + model.methods.size + ")",{
				//		this.displayMethodsOf(model);
				//	},minWidth:width)
				//});

				SimpleButton(layout,"vertical find method...",{
					var w,b,callback = { arg string;
						var found;
						string = string.asSymbol;
						found = ([model] ++ model.superclasses).any({ arg class;
									var method;
									method = class.findMethod(string);
									if(method.notNil,{
										method.gui;
										true
									 },{
										false
									 });
								});
						if(found.not,{
							// needs a gui report in the dialog
							("Method not found:" + string).inform;
						});
					};
					w = PageLayout("method?",Rect(200,200,170,40),scroll:false);
					b = 	TextField(w,Rect(5,5,150,30));
					b.string = "";
					b.action = { arg field; callback.value(field.value); w.close; };
					{ b.focus }.defer;
				});
				this.dependantsGui(layout);

				SimpleLabel(layout.startRow,"subclasses:",layoutWidth).bold;
				layout.startRow;
				this.displaySubclassesOf(model,layout,0,50);

				SimpleLabel(layout.startRow,"classes referenced by" + model ++ ":",layoutWidth).bold;
				layout.startRow;
				model.referencesClasses.do { arg klass;
					ClassNameLabel(klass,layout.startRow)
				};

				SimpleLabel(layout.startRow,"references to" + model ++ ":",layoutWidth).bold;
				layout.startRow;
				model.referencesTo.do { arg klass;
					ClassNameLabel(klass,layout.startRow)
				};
			});
		});
	}

	displayMethodsOf { arg class,f,withoutClass = true,width=160,indent=0;
		var methods;
		methods = class.methods;
		if(methods.notNil,{
			f = f.asFlowView;
			methods.do({ arg meth;
				if(withoutClass,{
					MethodLabel.withoutClass(meth,f.startRow,minWidth:width)
				},{
					MethodLabel(meth,f.startRow,minWidth:width);
				});
				if(class.superclass.notNil,{
					class.superclasses.do({ arg superclass;
						var supermethod;
						supermethod = superclass.findMethod(meth.name);
						if(supermethod.notNil,{
							MethodLabel(supermethod,f,minWidth:width)
						},{
							// leave space
							GUI.staticText.new(f,Rect(0,0,width,GUI.skin.buttonHeight))
						});
					})
				});
			})
		});
	}

	displaySubclassesOf { arg class,layout,shown,limit,indent = 50;
		var subclasses;
		layout = layout.asFlowView;
		if(class.subclasses.notNil,{
			subclasses = class.subclasses.as(Array)
				.sort({arg a,b; a.name.asString < b.name.asString});
			shown = shown + subclasses.size;
			subclasses.do({ arg c,i;
					layout.startRow;
					SimpleLabel(layout,"",indent).background = Color.clear;
					ClassNameLabel(c,layout,200);

					indent = indent + 50;
					if(c.subclasses.size + shown < limit,{
						this.displaySubclassesOf(c,layout,shown,limit,indent);
					},{
						if(c.subclasses.size > 0,{
							SimpleLabel(layout,c.subclasses.size.asString + " subclasses",80);
						});
					});
					indent = indent - 50;
				})
		});
	}
}


MethodGui : ObjectGui {

	source {
		var classSource,myIndex,nextMethod,tillChar;
		classSource = File(model.filenameSymbol.asString,"r").readAllString;
		myIndex = model.ownerClass.methods.indexOf(model);
		nextMethod = model.ownerClass.methods[myIndex+1];
		if(nextMethod.notNil,{
			tillChar = nextMethod.charPos;
		},{
			tillChar = model.charPos + 500; // actually till the next class begins on this page or the page ends
		});
		^classSource.copyRange(model.charPos,tillChar)
	}

	guiBody { arg layout;
		var prototypeFrame;
		var started=false,supers;

		SimpleButton(layout,"Source",{
			model.openCodeFile;
		});
		SimpleButton(layout,"Help",{
			model.openHelpFile;
		});

		// from Object down...
		layout.startRow;
		if(model.ownerClass.superclass.notNil,{
			supers = model.ownerClass.superclasses;
			supers.reverse.do({ arg class;
				var supermethod;
				supermethod = class.findMethod(model.name);
				if(supermethod.notNil,{
					ClassNameLabel(class,layout.startRow,minWidth:300);
					MethodLabel(supermethod,layout,minWidth:300);
				});
			})
		});

		//layout.hr;
		ClassNameLabel(model.ownerClass,layout.startRow,minWidth:300);
		MethodLabel(model,layout,minWidth:300);

		// show subclass implementations
		// other class implementations of this message (command-y)
		SimpleLabel(layout.startRow,"References to this message:",300);
		Class.findAllReferences(model.name).do({ |r|
			if(r.isKindOf(FunctionDef),{
				InspButton(r,layout.startRow,300);
			},{
				MethodLabel(r,layout.startRow,300);
			});
		});
		// would rather look at the source code for most things

		SimpleLabel(layout.startRow,"classes referenced:",300).bold;
		layout.startRow;
		model.referencesClasses.do { arg klass;
			ClassNameLabel(klass,layout.startRow)
		};
	}
}


