

ClassNameLabel : SimpleButton {

	*new { arg  class,layout,minWidth=200,minHeight,font;
		^super.prNew(layout,class.name.asString,[minWidth,minHeight],font)
			.action_({class.ginsp})
			.color_(Color.white)
			.background_(Color( 0.52156862745098, 0.75686274509804, 0.90196078431373 ))
	}
	*newBig { arg  class,layout,minWidth=200,minHeight=30;
		^this.new(class,layout,minWidth,minHeight,GUI.font.new("Helvetica-Bold",18))
	}
}


MethodLabel : SimpleButton {

	*new { arg  method,layout,minWidth=300;
		^this.prBasicNew(method, method.ownerClass.name.asString ++ "-" ++ method.name.asString,layout,minWidth)
	}
	*withoutClass { arg  method,layout,minWidth=100;
		^this.prBasicNew(method, method.name.asString,layout,minWidth)
	}
	*classMethod { arg  method,layout,minWidth=100;
		^this.prBasicNew(method, "*" ++ method.name.asString,layout,minWidth)
	}
	*prBasicNew { arg  method,desc,layout,minWidth=300;
		^super.prNew(layout,
				desc,
				Rect(0,0,minWidth,GUI.skin.buttonHeight),
				GUI.font.new("Monaco",9))
			.action_({method.ginsp})
			.background_(Color.new255(245, 222, 179))
	}
}


InspButton : SimpleButton {

	*new { arg  target,layout,minWidth=150;
		^super.prNew(layout,target.asString,minWidth, GUI.font.new("Helvetica",12) )
			.action_({ target.insp; InspManager.front; })
			.color_(Color.new255(70, 130, 200))
			.background_(Color.white)
	}
	*big { arg  target,layout,minWidth=200;
		^super.prNew(layout,target.asString,minWidth,GUI.font.new("Helvetica-Bold",18))
			.action_({target.insp; InspManager.front; })
			.color_(Color.black)
			.background_(Color.white)
	}
	*icon { arg target,layout;
		^GUI.button.new(layout,Rect(0,0,30,GUI.skin.buttonHeight))
			.action_({ target.insp; InspManager.front })
			.states_([["insp",Color.black,Color.white]]);
	}
	*captioned { arg caption,target,layout,minWidth=150;
		SimpleLabel(layout,caption,minWidth:minWidth);
		this.new(target,layout);
	}
}

// bw compat
InspectorLink : InspButton {}

DefNameLabel {

	*new { arg name,server,layout,minWidth=130;
		var def;
		if('InstrSynthDef'.asClass.notNil,{
			def = InstrSynthDef.cacheAt(name,server);
		});
		if(def.isNil,{
			^SimpleLabel(layout,name,minWidth)
		},{
			^InspButton(def,layout,minWidth)
		})
	}
}


ArgName : SimpleLabel {

	*new { arg name,layout,minWidth=130;
		^super.new(layout,name,minWidth)
			.background_(Color( 0.47843137254902, 0.72941176470588, 0.50196078431373 ))
			.align_(\left)
	}
}


VariableNameLabel : SimpleLabel {

	*new { arg name,layout,minWidth=120;
		^super.new(layout,name,minWidth)
			.background_(Color( 1, 0.86666666666667, 0.38039215686275 ))
			.align_(\right)
	}
}


