// W. Snoei 2005

+ SCWindow {

	// SC2 methods
	views { ^view.children }
	at { |index| ^view.at(index) }
	@ { |index| ^view.at(index) }
	
	
	//// window code generator:
	
	// only the window
	asCode { |varName = "w"|
		^( varName.asString ++ " = GUI.window.new(\"" ++ 
			name ++ "\", " ++ 
			this.bounds 
			++ " ).front;\n" ++ this.view.asCode( varName ) ++
			( if( this.respondsTo( \drawHook ) ) {
				if( this.drawHook.notNil ) {
					varName.asString ++ ".drawHook = {\n" ++ 
						this.drawHook.asGUICode( "GUI.pen", 1, this ) ++
						"\t};\n" 
				} { 
					"" 
				};
			} {
				if( this.drawFunc.notNil ) {
					varName.asString ++ ".drawHook = {\n" ++ 
						this.drawFunc.asGUICode( "GUI.pen", 1, this ) ++
						"\t};\n" 
				} {
					 "" 
				};
			}
			)
		);
	}
	
	// the window plus it's views, between brackets	
	asFullCode { |varName = "w", additionalVarNames|
		var outString, containerViewCount = 0;
		additionalVarNames = additionalVarNames ??
			{ "cdefhijklmnopqrtuvwa" }; // default support for 20 containerviews
		outString = "(\n" ++ this.asCode( varName );
		view.children.do({
			|item|
			if( item.respondsTo( \asFullCode ) )
				{ outString = outString ++ "\n" ++ 
					item.asFullCode( additionalVarNames[ containerViewCount ].asString,
						varName );
					containerViewCount = containerViewCount + 1  }
				{ outString = outString ++ "\n" ++ item.asCode( varName ) };
				}); 
		^outString ++ "\n);";
		}

}

+ SCView {

	guiName { ^"GUI." ++ this.class.asString[2].toLower ++ this.class.asString[3..] }

	// basic code generator
	asCode { |varName = "w", additionalMethods|
		var outString;	
		outString =  this.guiName ++ ".new( " ++ varName ++ ", " ++ 
			this.bounds.asCompileString ++ " )";
		additionalMethods = additionalMethods.asCollection ++
			[\background];
		additionalMethods.do({
			|itemArray|
			var item, default;
			itemArray = itemArray.asCollection;
			item = itemArray[0];
			default = itemArray[1];
			if(  this.perform(item.asSymbol) !== default )
			{ outString = outString ++ "\n\t." ++ item ++ 
				"_( " ++ this.perform(item.asSymbol).asCode ++ " )"; };
			});
		^outString ++ ";";
		}
	}

+ SCTopView { 
	asCode { |varName = "w", extraMethods|
		// special case
		var outstring;
		outstring = "";
		
		if( this.decorator.notNil )
			{ outstring = outstring ++
				(varName ++ ".view.decorator_(" ++ this.decorator.asCode ++ ");\n") };
		
		if( this.background.notNil )
			{ outstring = outstring ++
				(varName ++ ".view.background_(" ++ this.background.asCode ++ ");\n") };
		
		^outstring; }
	}

+ SCContainerView {
	
	//  SC2 methods
	views { ^children }
	at { |index| ^children.asCollection.at(index) }
	@ { |index| ^children.asCollection.at(index) }
	
	asCode { |varName = "w", extraMethods|
		^super.asCode( varName, [ \decorator ] ++ extraMethods ); }
		
	asFullCode { |varName = "c", parentVarName = "w"|
		var outString;
		outString = varName.asString ++ " = " ++ this.asCode( parentVarName ) ++ "\n";
		children.do({ |item| outString = outString ++ "\n" ++ item.asCode( varName ) });
		^outString;
		}

	}
	

// properties to produce code for per view type

+ SCButton {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, 
			[ [\value, 0.0], \states, \font, \action] 
				++ extraMethods.asCollection) }
	}

+ SCSliderBase {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, 
			[[\step, 0.0], \knobColor, \action] 
				++ extraMethods.asCollection) }
		}
		
+ SCSlider {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, 
			[[\value, 0.0], [\thumbSize, 12.0] ] ++ extraMethods.asCollection) }
	}

+ SCRangeSlider { 
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, [[\lo, 0.0], [\hi, 0.0]] ++ extraMethods.asCollection) 
		
		}
	}

+ SC2DSlider {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, 
			[[\x, 0.0], [\y, 0.0]] ++ extraMethods.asCollection) }
	}

+ SCPopUpMenu {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, [[\value, 0], \items, \font, \stringColor, \action] 
			++ extraMethods.asCollection)
		}
	}

+ SCStaticText {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, [\string, \font, \stringColor] ++ extraMethods.asCollection)
		}
	}

+ SCNumberBox {
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName, [[\value, 0], [\step, 0], 
			\font, \align, \stringColor, \boxColor,				\typingColor, \normalColor, \action] ++ extraMethods.asCollection)
		}
	}
	
		
+ SmoothSlider {

	guiName { ^this.class.asString; }
	asCode { |varName = "w", extraMethods|
		^super.asCode(varName,
			[[\value, 0.0], [\step, 0.01], [\mode, \jump], [\centered,false],
			[\thumbSize, 0], [\knobSize, 0.25], \action, 
			[\hilightColor, Color.blue.alpha_(0.5) ],  // doesn't work, why?
			[\knobColor, Color.black.alpha_(0.7) ]] // doesn't work, why?
				++ extraMethods.asCollection) }
	}
