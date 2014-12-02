ColPenGUI { // wslib 2007 - for use with ColPen to make { ... }.asGUICode possible
	classvar	<inited = false;
	classvar <>redirectScheme;
	
	*initClass { |mustInitGUI = true|
		// StartUp.defer({	
			inited = true;
			mustInitGUI.if({ Class.initClassTree( GUI ); });
			redirectScheme = GUI.schemes[ thisProcess.platform.defaultGUIScheme ];
			GUI.schemes.put( this.id, this );
		// });
	}
	
	*id { ^\colpen }
	
	*pen { ^ColPen }

	*doesNotUnderstand { arg selector ... args;
		if( redirectScheme.notNil ) { 
			^redirectScheme.perform( selector, *args )
		}{  
			// DoesNotUnderstandError(this, selector, args).throw; 
		};
	}
}