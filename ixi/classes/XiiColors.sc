
XiiColors {
	
	classvar <focus= 0.98, <unfocus= 0.93;
	
	*darkgreen { arg alpha=255;
		^Color.new255(103, 148, 103, alpha:alpha);
	}
	
	*lightgreen {
		^Color.new255(155, 205, 155);
	}

	*selectioncolor {
		^Color.new255(105, 185, 125);
	}
	
	*ixiorange {
		^Color.new255(255, 102, 0, 160);
	}
	
	*listbackground {
		^Color.new255(155, 205, 155, 60);
	}
	
	*onbutton {
		^Color.green(alpha:0.2);
	}
}

