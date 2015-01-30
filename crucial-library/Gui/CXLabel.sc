


CXAbstractLabel : SCViewHolder {

	*new { arg layout,string,width,height,minWidth=15,font;
		string = string.asString;
		if(font.isNil,{ font =  GUI.font.new(*GUI.skin.fontSpecs) });

		^super.new.init(layout, Rect(0,0,
			width ?? {(string.bounds(font).width + 6).max(minWidth)} ,
			height ?? {GUI.skin.buttonHeight}))
			.font_(font)
			.label_(string)
	}
	init { |layout, bounds, string|
		view = this.class.viewClass.new(layout, bounds);
	}
	*viewClass { ^GUI.staticText }
	label_ { arg string;
		view.string_(" " ++ string ++ " ");
	}
	bold { arg fontSize=11;
		this.font = GUI.font.new("Helvetica-Bold",fontSize);
	}
}


CXLabel : CXAbstractLabel {

	classvar <>bgcolor;

	*new { arg layout,string,width,height,minWidth=15,font;
		var new;
		new = super.new(layout,string,width,height,minWidth,font);
		new.background_(Color(0.9843137254902, 0.9843137254902, 0.9843137254902, 1.0))
			.align_(\left);
		^new
	}
}




ArgNameLabel : CXAbstractLabel {
	
	*new { arg name,layout,minWidth=130;
		^super.new(layout,name,minWidth: minWidth)
			.background_(Color( 0.47843137254902, 0.72941176470588, 0.50196078431373 ))
			.align_(\left)
	}
}


