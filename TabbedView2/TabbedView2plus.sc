/******* by jostM http://www.glyph.de *******/
/******* Part of TabbedView2 Quark *******/

+ TabbedView2{

	// use these as examples to make your own class extentions according to your needs
	*newBasic{ arg parent, bounds;
		var q;
		q=this.new(parent, bounds);
		if( GUI.id !== \swing)  {
			q.labelColors_([Color.white.alpha_(0.3)]);
			q.backgrounds_([Color.white.alpha_(0.3)]);
		}{
			q.labelColors_([Color(0.9,0.9,0.9)]);
			q.backgrounds_([Color(0.9,0.9,0.9)]);
			q.unfocusedColors_([Color(0.8,0.8,0.8)]);
		};
		^q;
	}
	
		
	*newColorLabels{ arg parent, bounds;
		var q;
		q=this.newBasic(parent, bounds);
		q.labelColors_([Color.red,Color.blue,Color.yellow]);
		if( GUI.id !== \swing)  {
			q.backgrounds_([Color.white.alpha_(0.3)]);
		}{
			q.backgrounds_([Color(0.9,0.9,0.9)]);
			q.unfocusedColors_([Color(0.9,0.75,0.75),
							Color(0.75,0.75,0.9),
							Color(0.9,0.9,0.75)]);
		};
		^q;
	}
	
		
	*newColor{ arg parent, bounds;
		var q;
		q=this.new(parent, bounds);
		
		if( GUI.id !== \swing)  {
			q.backgrounds_([Color.red.alpha_(0.1),
								Color.blue.alpha_(0.1),
								Color.yellow.alpha_(0.1)]);
			q.unfocusedColors_([Color.red.alpha_(0.2),
								Color.blue.alpha_(0.2),
								Color.yellow.alpha_(0.2)]);
		}{
			q.backgrounds_([Color(0.9,0.85,0.85),
								Color(0.85,0.85,0.9),
								Color(0.9,0.9,0.85)]);
			q.unfocusedColors_([Color(0.9,0.75,0.75),
								Color(0.75,0.75,0.9),
								Color(0.9,0.9,0.75)]);
		};
		q.labelColors_([Color.red,Color.blue,Color.yellow]);
		^q;
	}
	
	*newFlat{ arg parent, bounds;
		var q;
		q=this.newBasic(parent, bounds);
		q.tabHeight=14;
		q.tabWidth= 70;
		q.tabCurve=3;
		^q;
	}
	
	*newTall{ arg parent, bounds;
		var q;
		q=this.newBasic(parent, bounds);
		q.tabHeight= 30;
		q.tabWidth= 70;
		q.tabCurve=3;
	^q;
	}
	
	*newTransparent{ arg parent, bounds;
		var q;
		q=this.new(parent, bounds);
		if( GUI.id !== \swing)  {
			q.labelColors_([Color.white.alpha_(0.3)]);
		}{	
			q.labelColors_([Color(0.9,0.9,0.9)]);
			q.unfocusedColors_([Color(0.8,0.8,0.8)]);
		};
		q.backgrounds_([Color.clear]);
		^q;
	}
	
	*newPacked{ arg parent, bounds;
		var q;
		q=this.new(parent, bounds);
		if( GUI.id !== \swing)  {
			q.labelColors_([Color.white.alpha_(0.3)]);
			q.backgrounds_([Color.white.alpha_(0.3)]);
			}{
			q.labelColors_([Color(0.85,0.85,0.85)]);
			q.backgrounds_([Color(0.85,0.85,0.85)]);
			q.unfocusedColors_([Color(0.8,0.8,0.8)]);
		};
		q.tabCurve=3;
		q.labelPadding=8;
		q.tabHeight=14;
		^q;
	}
	
	demo{arg i=3, string="tab";
		
		i.do{|i| this.add(string++(i+1).asString)};
		
	}

}
