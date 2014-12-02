
+ Object {
	isSVGPathSegment { ^false }
	getUnits { |default| ^default }
	interpretVal { ^this.asString.interpretVal }
	asSVGTransform { ^SVGTransform( this ); }
	}

+ String {
	asColor { |alpha = 1| var color;
		color = Color.newName( this );
		if( color.notNil ) { color.alpha_( alpha ); };
		^color; }
	
	asWebColorString { |default| 
		var color;
		color = this.asColor;
		if( color.notNil )
			{ ^color.asWebColorString }
			{ ^default };
		}
		
	firstIsLower { ^this[0].isLower } // Char-isLower is in string-nameconversion.sc
	firstIsUpper { ^this[0].isUpper }
	
	interpretVal { // return first number found as Integer or Float
		^this.trim.splitNumbers.select({ |item| item.isNumber; })[0]
		}
	
	getUnits { |default| // return text after number as symbol ( " 100px " -> 'px' )
		var out;
		out = this.trim.splitNumbers[1];
		if( out.notNil )
			{ ^out.trim.asSymbol }
			{ ^default };
		}
		
	asSVGTransform { ^SVGTransform( this ); }
	
	
	// direct drawing functions:
	asPenFunction { ^SVGPath( this ).asPenLinesFunc; }
	
	fillPath { this.asPenFunction.value; GUI.pen.fill; }
	strokePath { this.asPenFunction.value; GUI.pen.stroke; }
	clipPath { this.asPenFunction.value; GUI.pen.clip; }
	
	}

+ Symbol {
	asColor { |alpha = 1| var color;
		color = Color.newName( this );
		if( color.notNil ) { color.alpha_( alpha ); };
		^color; }
	asWebColorString { |default| ^this.asString.asWebColorString( default ); }
	
	toLower { ^this.asString.toLower.asSymbol }
	toUpper { ^this.asString.toUpper.asSymbol }
	
	firstToLower { ^this.asString.firstToLower.asSymbol }
	firstToUpper { ^this.asString.firstToUpper.asSymbol }
	
	firstIsLower { ^this.asString[0].isLower }
	firstIsUpper { ^this.asString[0].isUpper }
	}
	
+ Char {
	firstToLower { ^this.toLower }
	firstToUpper { ^this.toUpper }
	}
	
	
+ SimpleNumber {
	asColor { |alpha| ^Color.newHex( this ).alpha_( alpha ); }
	asWebColorString { |default| ^this.asColor.asWebColorString( default ); }
	interpretVal { ^this }
	interpret { ^this }
	}
	
+ Nil {
	asColor { ^nil }
	asWebColorString { |default| ^default }
	interpret { ^nil }
	asPenFunction { ^nil }
	asSVGPathSegment { ^nil }
	interpretVal { ^nil }
	asSVGTransform { ^nil }
	}
	
+ Color {
	asColor { ^this }
	}
	
+ Collection {
	asSVGPathSegment { ^SVGPathSegment( *this ) }
	asSVGPath { ^SVGPath( this ) }
	asSVGTransform { ^SVGTransform( this ); }
	
	// direct drawing functions
	asPenFunction { ^SVGPath( this ).asPenLinesFunc; }
	
	fillPath { this.asPenFunction.value; GUI.pen.fill; }
	strokePath { this.asPenFunction.value; GUI.pen.stroke; }
	clipPath { this.asPenFunction.value; GUI.pen.clip; }
	
		
	}
	
+ Point {
	asSVGPathSegment { |type ... extra|
		^SVGPathSegment( type ? \LineTo, *( extra ++ [ x, y ]) );
		}
	}
	
+ DOMElement { asPenFunction { ^nil } }