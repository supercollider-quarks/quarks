GeoDocument : CocoaDocument {

	var graphParser ;
		
	*new { arg graphParser, alpha = 0.9 ;  
		^super.new.initGeoDocument(graphParser, alpha) 
	}

	initGeoDocument { arg aGraphParser, anAlpha = 0.9 ;
		graphParser = aGraphParser ;
		this
			.title_("God bless Nim Chimpsky:"+graphParser.runner.name);
		this
			.string_("// ctrl + P --> speak ixno ")
			.keyDownAction_({ arg doc, key, modifiers, keycode ;
				if ([modifiers, keycode] == [ 262401, 16 ], {
					graphParser.parse(this.getLine) 
				}) 
			})
			.stringColor_(Color(0.9,0.9,0.9))
			.background_(Color(0, 0, 0.3, anAlpha)) ;
	
	}
}