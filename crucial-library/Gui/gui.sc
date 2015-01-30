
+ Object {

	// return an array of symbols specifying which vars to put up on the gui
	*instVarsForGui { ^[] }
	*publicInstVars {
		^this.instVarNames.select({ |ivar| this.findRespondingMethodFor(ivar).notNil })
	}

	topGui { arg ... args;
		^this.guiClass.new(this).performList(\topGui,args);
	}

	smallGui { arg  ... args;
		var class;
		class = this.guiClass;
		if(class.findMethod(\smallGui).notNil,{
			^this.guiClass.new(this).performList(\smallGui,args);
		});
		while ({ class = class.superclass; class !== Object },{
			if(class.findMethod(\smallGui).notNil,{
				^this.guiClass.new(this).performList(\smallGui,args);
			});
		});
		^this.guiClass.new(this).performList('gui',args);
	}
}

+ ObjectGui {

	topGui { arg ... args;
		this.performList(\gui, args);
	}
	defer { arg function;
		if(this.canCallOS,function,{function.defer})
	}
}

+ Pattern {
	// by default gui all public inst vars
	*instVarsForGui {
		^this.publicInstVars
	}
}

+ Pbind {
	guiBody { |f|
		var endval = patternpairs.size-1;
		forBy (0, endval, 2) { arg i;
			f.startRow;
			ArgNameLabel(patternpairs[i],f);
			patternpairs[i+1].gui(f);
		};
	}
}

+ Pswitch  {
//	guiClass { ^PswitchGui }
	guiBody { arg layout;
		this.list.do({ arg l;
			l.gui(layout.startRow);
		});
		this.which.gui(layout);
	}
}

+ Pstutter {
	guiBody { arg layout;
		pattern.gui(layout);
		n.gui(layout.startRow);
	}
}

+ Dictionary {
	//guiClass { ^DictionaryGui }
	guiBody { arg layout;
		this.keysValuesDo({ arg k,v,i;
			CXLabel(layout.startRow,k,minWidth: 100);
			Tile(v,layout,200);
		})
	}
}

+ Node {
//	guiClass { ^NodeGui }
	guiBody { arg layout;
		Tile(this.server,layout);
		Tile(this.group,layout);
		ActionButton(layout,"trace",{
			this.trace;
		});
		ActionButton(layout,"query",{
			this.query;
		});
	}
}

+ Synth {
//	guiClass { ^SynthGui }
	guiBody { arg layout;
		CXLabel(layout,this.defName);
		super.guiBody(layout);
	}
}

	