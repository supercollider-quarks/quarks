

// abstract
// SC/Qt/Swing all use this
SCButtonAdapter : SCViewHolder {

	*initClass {
		Class.initClassTree(GUI);
	}
	makeView { arg layout,x,y;
		var rect;
		if((layout.isNil or: { layout.isKindOf(PageLayout) }),{ layout = layout.asFlowView; });
		this.view = GUI.button.new(layout,Rect(0,0,x,y ? GUI.skin.buttonHeight));
		if(consumeKeyDowns,{ this.view.keyDownAction_({true}) });
	}
	flowMakeView { arg layout,x,y;
		this.view = GUI.button.new(layout.asFlowView,Rect(0,0,x,y ? GUI.skin.buttonHeight));
		if(consumeKeyDowns,{ this.view.keyDownAction_({true}); });
	}

	makeViewWithStringSize { arg layout,optimalWidth,minWidth,minHeight;
		this.makeView( layout,(optimalWidth + 10).max(minWidth?20),(minHeight ) )
	}
	initOneState { arg name,textcolor,backcolor;
		view.states_([[name,textcolor ? Color.black, backcolor ? Color.white]])
	}
	// sets all states
	label_ { arg string;
		view.states = view.states.collect({ arg st;
			st.put(0,string.asString);
			st
		});
	}
	// assumes 1 state
	background_ { arg color;
		var s;
		s = view.states;
		s.at(0).put(2,color);
		view.states = s;
		view.refresh;
	}
	background {
		^view.states[0][2]
	}
	labelColor_ { arg color;
		var s;
		s = view.states;
		s.at(0).put(1,color);
		view.states = s;
		view.refresh;
	}
	*defaultHeight { ^GUI.skin.buttonHeight }
}


// a one state button
ActionButton : SCButtonAdapter {

	var <action;

	*new { arg layout,title,function,minWidth=20,minHeight,color,backcolor,font;
		^super.new.init(layout,title,function,minWidth,minHeight,color,backcolor,font)
	}
	init { arg layout,title,function,minWidth=20,minHeight,color,backcolor,font;
		var optimalWidth,skin;
		skin = GUI.skin;
		title = title.asString;
		if(title.size > 40,{ title = title.copyRange(0,40) });
		if(font.isNil,{ font = GUI.font.new(*skin.fontSpecs) });
		optimalWidth = title.bounds(font).width;
		this.makeViewWithStringSize(layout,optimalWidth,minWidth,minHeight);
		view.states_([[title,color ?? {skin.fontColor},
			backcolor ?? {skin.background}]]);
		view.font_(font);
		view.action_(function);
		view.focusColor_((skin.focusColor ?? {Color.grey(0.5,0.1)}).alpha_(0.1));
		if(consumeKeyDowns,{ this.keyDownAction = {true}; });
	}
}


ToggleButton : SCButtonAdapter {

	var <state,<>onFunction,<>offFunction;

	*new { arg layout,title,onFunction,offFunction,init=false,minWidth=20,minHeight,onColor,offColor;
			^super.new.init(layout,init, title,minWidth,minHeight,onColor,offColor)
				.onFunction_(onFunction).offFunction_(offFunction)
	}
	value { ^state }
	value_ { arg way;
		this.toggle(way,false)
	}
	toggle { arg way,doAction = true;
		if(doAction,{
			this.prSetState(way ? state.not)
		},{
			state = way ? state.not;
		});
		view.setProperty(\value,state.binaryValue);
	}
	// private
	init { arg layout,init,title,minWidth,minHeight,onc,offc;
		var font,skin;
		skin = GUI.skin;
		font = GUI.font.new(*GUI.skin.fontSpecs);
		this.makeViewWithStringSize(layout,title.bounds(font).width,minWidth,minHeight);
		view.states = [
			[title,skin.fontColor,offc ? skin.offColor],
			[title,skin.fontColor,onc ? skin.onColor]
		];
		state=init;
		view.value_(state.binaryValue);
		view.action_({this.prSetState(state.not)});
		view.font = font;
		view.focusColor = skin.focusColor ?? {Color(0.0, 0.85714285714286, 1.0, 0.4)};
	}
	prSetState { arg newstate;
		state = newstate;
		if(state,{
			onFunction.value(this,state)
		},{
			// if there is no offFunction value the onFunction
			(offFunction ? onFunction).value(this,state)
		});
	}
}


Tile : ActionButton { // the name comes from Squeak

	*new { arg  target,layout,minWidth=100;
		if(target.guiClass == StringGui,{
			^target.gui(layout);
		});
		^super.new(layout,target.asString,{
				target.gui;
				//#F6F9F5
			},minWidth,GUI.skin.buttonHeight, Color.black,			Color.new255(248, 248, 255))
	}

}

