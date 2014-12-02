// part of wslib 2005

+ String { 
	
	// general flexibility improvements:
	
	collectJoin { | function, joiner | 
		^this.collectAs( function, Array ).join( joiner );
	}
	
	*fillJoin { | size, function, joiner, excludeNil = true | // any output for function is accepted
		var array;
		array = Array.fill( size, function );
		if( excludeNil ) { array = array.select(_.notNil) };
		^array.join( joiner )
	}

	removeItems { |items = "\n\t"| // defaults to removing returns and tabs
		// !! not in place !!
		items = items.asString;
		^this.collectJoin({ |char| if( items.includes(char) ) {""} {char} });
	}
	
	replaceItems { |items = "\n\t", replaceWith = "  "| 
		// !! not in place !!
		// replaceWith can also be array of strings
		// similar to tr, but replaces multiple chars
		// "hello".replaceItems("ho", ["oh h",$!])
		^this.collectJoin({ |char|
			var index = items.indexOf(char);
			if( index.notNil ) { 
				replaceWith[index] 
			} { 
				char 
			}
		});
	}
		
	// shortcuts:
	
	removeBrackets { ^this.removeItems("()") } // for SCPopUpMenu items
	replaceBrackets { arg replaceWith = "[]"; 
		^this.replaceItems("()", replaceWith) }
	replaceSpaces { arg replaceWith = "_";
		^this.replaceItems(" ", replaceWith) }
}